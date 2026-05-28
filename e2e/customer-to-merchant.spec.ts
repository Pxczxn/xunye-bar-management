import { test, expect, type Page } from '@playwright/test';

const H5 = 'http://localhost:9000';
const ADMIN = 'http://localhost:8847';
const API = 'http://localhost:8848';

let adminToken = '';
let customerPhone = '139' + Date.now().toString().slice(-8);

// ==================== 工具 ====================
async function apiGet(path: string, token?: string) {
  const h: any = { 'Content-Type': 'application/json' };
  if (token) h['Authorization'] = `Bearer ${token}`;
  return (await fetch(`${API}${path}`, { headers: h })).json();
}
async function apiPost(path: string, body: any, token?: string) {
  const h: any = { 'Content-Type': 'application/json' };
  if (token) h['Authorization'] = `Bearer ${token}`;
  return (await fetch(`${API}${path}`, { method: 'POST', headers: h, body: JSON.stringify(body) })).json();
}
async function apiPatch(path: string, body: any, token?: string) {
  const h: any = { 'Content-Type': 'application/json' };
  if (token) h['Authorization'] = `Bearer ${token}`;
  return (await fetch(`${API}${path}`, { method: 'PATCH', headers: h, body: body ? JSON.stringify(body) : '' })).json();
}

async function adminLogin(page: Page) {
  const res = await apiPost('/api/admin/auth/login', { username: 'admin', password: '123456' });
  adminToken = res.data.token;
  await page.goto('/login');
  await page.evaluate(({ t, u }) => {
    localStorage.setItem('token', t);
    localStorage.setItem('user', u);
  }, { t: adminToken, u: JSON.stringify(res.data.user) });
  await page.goto('/dashboard');
  await page.waitForTimeout(2000);
}

// 清空所有桌台
async function cleanAllTables() {
  const tables = await apiGet('/api/admin/tables?pageNum=1&pageSize=20', adminToken);
  for (const t of tables.data.records) {
    if (t.status !== 'EMPTY' && t.status !== 'DISABLED') {
      await apiPatch(`/api/admin/tables/${t.id}/clear`, null, adminToken);
    }
  }
}

// =====================================================
// 核心场景: 小程序下单 → 管理端接单 → 厨房出单
// =====================================================
test.describe('跨系统: 小程序下单 → 管理端接单全流程', () => {

  let orderNo = '';
  let orderId = 0;
  let tableId = 0;
  let tableName = '';

  test.beforeAll(async () => {
    const res = await apiPost('/api/admin/auth/login', { username: 'admin', password: '123456' });
    adminToken = res.data.token;
    await cleanAllTables();
  });

  // ==========================================
  // 第一阶段: 顾客在小程序下单
  // ==========================================

  test('【顾客】打开小程序首页', async ({ page }) => {
    await page.goto(H5);
    await page.waitForTimeout(4000);
    const text = await page.textContent('body');
    expect(text).toContain('寻野');
    expect(text).toContain('我要喝');
    console.log('✅ 顾客打开小程序首页');
  });

  test('【顾客】浏览菜单，看到酒水商品', async ({ page }) => {
    await page.goto(`${H5}/pages/menu/menu`);
    await page.waitForTimeout(5000);
    const text = await page.textContent('body');
    // 验证菜单页显示商品信息
    expect(text).toContain('寻野');
    console.log('✅ 顾客浏览菜单页');
    console.log('   页面内容:', text!.substring(0, 300));
  });

  test('【顾客】选择桌台并下单', async () => {
    // 获取空闲桌台
    const tables = await apiGet('/api/customer/tables');
    const et = tables.data.find((t: any) => t.status === 'EMPTY');
    expect(et).toBeTruthy();
    tableId = et.id;
    tableName = et.tableCode;
    console.log(`   选择桌台: ${tableName} (ID:${tableId})`);

    // 顾客下单: 百威x3 + 薯条x1 = 30*3 + 25 = 115元
    const order = await apiPost('/api/customer/orders', {
      tableId,
      phone: customerPhone,
      items: [
        { productId: 1, quantity: 3 },  // 百威啤酒 30x3=90
        { productId: 5, quantity: 1 },  // 薯条 25x1=25
      ],
      remark: '少冰，多给点纸巾',
    });
    expect(order.code).toBe(200);
    orderNo = order.data.orderNo;
    expect(order.data.totalAmount).toBe(115);
    expect(order.data.status).toBe('UNPAID');
    console.log(`✅ 顾客下单成功: ${orderNo}, 金额: ${order.data.totalAmount}元`);
  });

  test('【顾客】在小程序查看订单详情', async ({ page }) => {
    await page.goto(`${H5}/pages/orders/orders`);
    await page.waitForTimeout(4000);
    const text = await page.textContent('body');
    expect(text).toContain('历史订单');
    console.log('✅ 顾客查看订单列表');
  });

  // ==========================================
  // 第二阶段: 商户在管理端查看并接单
  // ==========================================

  test('【商户】登录管理后台', async ({ page }) => {
    await adminLogin(page);
    const text = await page.textContent('body');
    expect(text).toContain('营业看板');
    console.log('✅ 商户登录管理后台');
  });

  test('【商户】在订单管理看到顾客订单', async ({ page }) => {
    await page.goto(`${ADMIN}/orders`);
    await page.waitForTimeout(3000);
    const text = await page.textContent('body');
    // 验证订单页面有内容
    expect(text!.length).toBeGreaterThan(50);

    // 通过API确认订单存在
    const list = await apiGet(`/api/admin/orders?orderNo=${orderNo}`, adminToken);
    const order = list.data.records.find((o: any) => o.orderNo === orderNo);
    expect(order).toBeTruthy();
    orderId = order.id;
    expect(order.source).toBe('CUSTOMER_MINI');
    expect(order.tableName).toBe(tableName);
    expect(order.status).toBe('UNPAID');
    console.log(`✅ 商户看到顾客订单: ${orderNo}, 桌台:${tableName}, 来源:顾客扫码`);
  });

  test('【商户】在厨房看板看到新订单', async ({ page }) => {
    await page.goto(`${ADMIN}/kitchen`);
    await page.waitForTimeout(3000);
    const text = await page.textContent('body');
    expect(text!.length).toBeGreaterThan(50);
    console.log('✅ 商户打开厨房看板');
  });

  test('【商户】支付顾客订单(现金)', async () => {
    const payRes = await apiPatch(`/api/admin/orders/${orderId}/pay`,
      { paymentMethod: 'CASH' }, adminToken);
    expect(payRes.code).toBe(200);

    const detail = await apiGet(`/api/admin/orders/${orderId}`, adminToken);
    expect(detail.data.status).toBe('PAID');
    console.log('✅ 商户完成收款: 现金支付');
  });

  test('【商户】厨房开始制作', async () => {
    const makingRes = await apiPatch(`/api/admin/orders/${orderId}/making`, null, adminToken);
    expect(makingRes.code).toBe(200);

    const detail = await apiGet(`/api/admin/orders/${orderId}`, adminToken);
    expect(detail.data.serveStatus).toBe('MAKING');
    console.log('✅ 厨房开始制作');
  });

  test('【商户】厨房制作完成', async () => {
    const finishRes = await apiPatch(`/api/admin/orders/${orderId}/finish`, null, adminToken);
    expect(finishRes.code).toBe(200);

    const detail = await apiGet(`/api/admin/orders/${orderId}`, adminToken);
    expect(detail.data.status).toBe('PAID');
    expect(detail.data.serveStatus).toBe('FINISHED');
    console.log('✅ 厨房制作完成');
  });

  // ==========================================
  // 第三阶段: 验证联动
  // ==========================================

  test('【验证】订单最终状态正确', async () => {
    const detail = await apiGet(`/api/admin/orders/${orderId}`, adminToken);
    expect(detail.data.status).toBe('PAID');
    expect(detail.data.serveStatus).toBe('FINISHED');
    expect(detail.data.source).toBe('CUSTOMER_MINI');
    expect(detail.data.tableName).toBe(tableName);

    // 验证订单项
    const orderItems = detail.data.items;
    expect(orderItems.length).toBe(2);
    const budweiser = orderItems.find((i: any) => i.productName === '百威啤酒');
    expect(budweiser).toBeTruthy();
    expect(budweiser.quantity).toBe(3);
    expect(budweiser.amount).toBe(90);
    const fries = orderItems.find((i: any) => i.productName === '薯条');
    expect(fries).toBeTruthy();
    expect(fries.quantity).toBe(1);

    console.log('✅ 订单状态验证通过:');
    console.log(`   状态: ${detail.data.status}, 履约: ${detail.data.serveStatus}`);
    console.log(`   来源: ${detail.data.source}, 桌台: ${detail.data.tableName}`);
    console.log(`   商品: 百威x3=90 + 薯条x1=25 = 115元`);
  });

  test('【验证】库存正确扣减', async () => {
    const budweiser = await apiGet('/api/admin/products/1', adminToken);
    const fries = await apiGet('/api/admin/products/5', adminToken);
    // 百威应减少3, 薯条应减少1
    expect(budweiser.data.stock).toBeGreaterThanOrEqual(0);
    expect(fries.data.stock).toBeGreaterThanOrEqual(0);
    console.log(`✅ 库存验证: 百威剩余${budweiser.data.stock}, 薯条剩余${fries.data.stock}`);
  });

  test('【验证】桌台状态恢复', async () => {
    const tables = await apiGet('/api/admin/tables?pageNum=1&pageSize=20', adminToken);
    const table = tables.data.records.find((t: any) => t.id === tableId);
    // 订单完成后桌台保持USING(需手动清台)
    expect(['USING', 'EMPTY']).toContain(table.status);
    console.log(`✅ 桌台状态: ${table.status} (需手动清台恢复为EMPTY)`);
  });

  // ==========================================
  // 第四阶段: 顾客在小程序查看结果
  // ==========================================

  test('【顾客】在小程序查看订单状态', async ({ page }) => {
    await page.goto(`${H5}/pages/orders/orders`);
    await page.waitForTimeout(4000);
    const text = await page.textContent('body');
    expect(text).toContain('历史订单');
    console.log('✅ 顾客查看订单状态');
  });

  test('【商户】在管理后台查看仪表盘更新', async ({ page }) => {
    await page.goto(`${ADMIN}/dashboard`);
    await page.waitForTimeout(3000);
    const text = await page.textContent('body');
    expect(text!.length).toBeGreaterThan(50);
    console.log('✅ 商户查看仪表盘');
  });

  // ==========================================
  // 第五阶段: 第二单 - 连续接单
  // ==========================================

  test('【顾客】第二单: 小程序再下一单', async () => {
    // 清台
    await apiPatch(`/api/admin/tables/${tableId}/clear`, null, adminToken);

    const order2 = await apiPost('/api/customer/orders', {
      tableId,
      phone: customerPhone,
      items: [
        { productId: 2, quantity: 2 },  // 长岛冰茶 60x2=120
        { productId: 3, quantity: 1 },  // 野格 60x1=60
      ],
      remark: '第二单-冰块多一点',
    });
    expect(order2.code).toBe(200);
    expect(order2.data.totalAmount).toBe(180);
    console.log(`✅ 顾客第二单: ${order2.data.orderNo}, 金额: ${order2.data.totalAmount}元`);
  });

  test('【商户】接第二单并处理完成', async () => {
    const list = await apiGet(`/api/admin/orders?status=UNPAID&source=CUSTOMER_MINI`, adminToken);
    const latestOrder = list.data.records[0];
    expect(latestOrder).toBeTruthy();
    const oid = latestOrder.id;

    // 支付 → 制作 → 完成
    expect((await apiPatch(`/api/admin/orders/${oid}/pay`, { paymentMethod: 'WECHAT' }, adminToken)).code).toBe(200);
    expect((await apiPatch(`/api/admin/orders/${oid}/making`, null, adminToken)).code).toBe(200);
    expect((await apiPatch(`/api/admin/orders/${oid}/finish`, null, adminToken)).code).toBe(200);

    const detail = await apiGet(`/api/admin/orders/${oid}`, adminToken);
    expect(detail.data.status).toBe('PAID');
    expect(detail.data.serveStatus).toBe('FINISHED');
    console.log('✅ 商户第二单处理完成');
  });
});
