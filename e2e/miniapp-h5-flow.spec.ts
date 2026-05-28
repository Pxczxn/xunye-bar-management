import { test, expect } from '@playwright/test';

const H5 = 'http://localhost:9000';
const API = 'http://localhost:8848';

const TEST_PHONE = '139' + Date.now().toString().slice(-8);

async function apiPost(path: string, body: any) {
  return (await fetch(`${API}${path}`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(body),
  })).json();
}
async function apiGet(path: string) {
  return (await fetch(`${API}${path}`)).json();
}

// =====================================================
// 小程序 H5: 注册 → 登录 → 浏览 → 点单 全流程
// =====================================================
test.describe('小程序H5: 注册登录点单全流程', () => {

  // 1. 首页加载
  test('STEP-1: 打开小程序首页', async ({ page }) => {
    await page.goto(H5);
    await page.waitForTimeout(4000);
    const text = await page.textContent('body');
    expect(text!.length).toBeGreaterThan(20);
    console.log('首页内容长度:', text!.length);
  });

  // 2. 注册会员
  test('STEP-2: 注册新会员', async () => {
    const profile = await (await fetch(`${API}/api/customer/member/profile`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ phone: TEST_PHONE, nickname: 'H5测试酒客' }),
    })).json();
    expect(profile.code).toBe(200);
    expect(profile.data.phone).toBe(TEST_PHONE);
    console.log('注册成功:', TEST_PHONE);
  });

  // 3. 浏览菜单页
  test('STEP-3: 浏览菜单页(点酒)', async ({ page }) => {
    await page.goto(`${H5}/pages/menu/menu`);
    await page.waitForTimeout(4000);
    const text = await page.textContent('body');
    expect(text!.length).toBeGreaterThan(20);
    console.log('菜单页内容:', text!.substring(0, 200));
  });

  // 4. API验证商品列表
  test('STEP-4: 验证商品数据', async () => {
    const prods = await apiGet('/api/customer/products');
    expect(prods.data.length).toBeGreaterThan(0);
    // 顾客端API只返回上架商品，验证有价格和库存
    prods.data.forEach((p: any) => {
      expect(p.price).toBeGreaterThan(0);
      expect(p.stock).toBeGreaterThanOrEqual(0);
    });
    console.log('商品数:', prods.data.length);
  });

  // 5. 下单
  test('STEP-5: 顾客下单 百威x2+长岛冰茶x1', async () => {
    const tables = await apiGet('/api/customer/tables');
    const et = tables.data.find((t: any) => t.status === 'EMPTY');
    if (!et) {
      // 清台
      const boss = await apiPost('/api/admin/auth/login', { username: 'admin', password: '123456' });
      const allT = await (await fetch(`${API}/api/admin/tables?pageNum=1&pageSize=20`, {
        headers: { Authorization: `Bearer ${boss.data.token}` },
      })).json();
      for (const t of allT.data.records) {
        if (t.status !== 'EMPTY' && t.status !== 'DISABLED') {
          await fetch(`${API}/api/admin/tables/${t.id}/clear`, {
            method: 'PATCH',
            headers: { Authorization: `Bearer ${boss.data.token}` },
          });
        }
      }
    }
    const retryTables = await apiGet('/api/customer/tables');
    const emptyTable = retryTables.data.find((t: any) => t.status === 'EMPTY');
    expect(emptyTable).toBeTruthy();

    const order = await apiPost('/api/customer/orders', {
      tableId: emptyTable.id,
      phone: TEST_PHONE,
      items: [
        { productId: 1, quantity: 2 },
        { productId: 2, quantity: 1 },
      ],
      remark: 'H5小程序点单测试',
    });
    expect(order.code).toBe(200);
    expect(order.data.totalAmount).toBe(120);
    console.log('下单成功:', order.data.orderNo, '金额:', order.data.totalAmount);
  });

  // 6. 查看订单
  test('STEP-6: 查看订单列表', async () => {
    const orders = await apiGet('/api/customer/orders');
    expect(orders.data.length).toBeGreaterThan(0);
    console.log('订单数:', orders.data.length);
  });

  // 7. 浏览订单页
  test('STEP-7: 浏览订单页面', async ({ page }) => {
    await page.goto(`${H5}/pages/orders/orders`);
    await page.waitForTimeout(4000);
    const text = await page.textContent('body');
    expect(text!.length).toBeGreaterThan(10);
    console.log('订单页内容:', text!.substring(0, 200));
  });

  // 8. 浏览"我的"页面
  test('STEP-8: 浏览"我的"页面', async ({ page }) => {
    await page.goto(`${H5}/pages/mine/mine`);
    await page.waitForTimeout(4000);
    const text = await page.textContent('body');
    expect(text!.length).toBeGreaterThan(10);
    console.log('我的页面:', text!.substring(0, 200));
  });

  // 9. 查看优惠券
  test('STEP-9: 查看优惠券', async () => {
    const coupons = await apiGet(`/api/customer/coupons?phone=${TEST_PHONE}`);
    expect(coupons.data.length).toBeGreaterThan(0);
    console.log('优惠券:', coupons.data.length, '张');
  });

  // 10. 查看会员等级
  test('STEP-10: 查看会员等级', async () => {
    const levels = await apiGet('/api/customer/member/levels');
    expect(levels.data.length).toBeGreaterThanOrEqual(3);
    levels.data.forEach((l: any) => {
      console.log(`  ${l.name}: 折扣${l.discount}%, 积分倍率${l.pointsRate}%`);
    });
  });
});
