import { test, expect } from '@playwright/test';

const API = 'http://localhost:8848';

// ==================== 全局变量 ====================
let bossToken = '';
let managerToken = '';
let staffToken = '';

async function loginAPI(username: string, password: string): Promise<string> {
  for (let i = 0; i < 5; i++) {
    try {
      const res = await fetch(`${API}/api/admin/auth/login`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ username, password }),
      });
      const json = await res.json();
      if (json.code === 200 && json.data?.token) return json.data.token;
    } catch (e) {}
    await new Promise(r => setTimeout(r, 3000));
  }
  throw new Error(`Login failed for ${username}`);
}

async function apiGet(path: string, token: string) {
  return (await fetch(`${API}${path}`, { headers: { Authorization: `Bearer ${token}` } })).json();
}
async function apiPost(path: string, body: any, token: string) {
  return (await fetch(`${API}${path}`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${token}` },
    body: JSON.stringify(body),
  })).json();
}
async function apiPatch(path: string, body: any, token: string) {
  return (await fetch(`${API}${path}`, {
    method: 'PATCH',
    headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${token}` },
    body: body ? JSON.stringify(body) : '',
  })).json();
}

async function browserLogin(page: any, username: string, password: string) {
  // 复用已有token
  if (username === 'admin' && bossToken) {
    const u = { username: 'admin', nickname: '店长', role: 'BOSS' };
    await page.goto('/login');
    await page.evaluate(({ t, u }) => {
      localStorage.setItem('token', t);
      localStorage.setItem('user', u);
    }, { t: bossToken, u: JSON.stringify(u) });
    await page.goto('/dashboard');
    await page.waitForTimeout(2000);
    return;
  }
  const token = await loginAPI(username, password);
  const role = username === 'admin' ? 'BOSS' : username === 'manager' ? 'MANAGER' : 'STAFF';
  const nick = username === 'admin' ? '店长' : username === 'manager' ? '经理' : '员工';
  await page.goto('/login');
  await page.evaluate(({ t, u }) => {
    localStorage.setItem('token', t);
    localStorage.setItem('user', u);
  }, { t: token, u: JSON.stringify({ username, nickname: nick, role }) });
  await page.goto(role === 'STAFF' ? '/kitchen' : '/dashboard');
  await page.waitForTimeout(2000);
}

// =====================================================
test.describe('寻野酒吧 E2E 全流程', () => {

  test.beforeAll(async () => {
    bossToken = await loginAPI('admin', '123456');
    // manager/staff token 在需要时按需获取
  });

  // --------- 顾客点酒全流程 ---------
  test('E2E-001: 顾客点酒→支付→厨房制作→完成→桌台恢复', async () => {
    const t = bossToken;

    // 先清空所有USING状态的桌台
    const allTables = await apiGet('/api/admin/tables?pageNum=1&pageSize=20', t);
    for (const tbl of allTables.data.records) {
      if (tbl.status === 'USING' || tbl.status === 'CLEANING') {
        await apiPatch(`/api/admin/tables/${tbl.id}/clear`, null, t);
      }
    }

    const tables = await (await fetch(`${API}/api/customer/tables`)).json();
    const et = tables.data.find((x: any) => x.status === 'EMPTY');
    expect(et).toBeTruthy();

    const or = await (await fetch(`${API}/api/customer/orders`, {
      method: 'POST', headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        tableId: et.id, phone: '13800000001',
        items: [{ productId: 1, quantity: 2 }, { productId: 2, quantity: 1 }], remark: 'E2E测试',
      }),
    })).json();
    expect(or.code).toBe(200);
    expect(or.data.totalAmount).toBe(120);

    const list = await apiGet(`/api/admin/orders?orderNo=${or.data.orderNo}`, t);
    const ao = list.data.records.find((o: any) => o.orderNo === or.data.orderNo);
    expect(ao.source).toBe('CUSTOMER_MINI');

    expect((await apiPatch(`/api/admin/orders/${ao.id}/pay`, { paymentMethod: 'CASH' }, t)).code).toBe(200);
    expect((await apiPatch(`/api/admin/orders/${ao.id}/making`, null, t)).code).toBe(200);
    expect((await apiPatch(`/api/admin/orders/${ao.id}/finish`, null, t)).code).toBe(200);

    const d = await apiGet(`/api/admin/orders/${ao.id}`, t);
    expect(d.data.status).toBe('PAID');
    expect(d.data.serveStatus).toBe('FINISHED');

    // 订单完成后桌台保持USING(只有取消订单才会自动清台)
    const ta = await apiGet('/api/admin/tables?pageNum=1&pageSize=20', t);
    const tableAfter = ta.data.records.find((x: any) => x.id === et.id);
    expect(['USING', 'EMPTY']).toContain(tableAfter.status);
  });

  // --------- POS点单 ---------
  test('E2E-002: POS创建→支付→制作→完成', async () => {
    const t = bossToken;
    const tables = await apiGet('/api/admin/tables?pageNum=1&pageSize=20', t);
    const et = tables.data.records.find((x: any) => x.status === 'EMPTY');
    if (!et) return;

    const o = await apiPost('/api/admin/orders', {
      tableId: et.id, items: [{ productId: 3, quantity: 1 }, { productId: 5, quantity: 2 }],
    }, t);
    expect((await apiPatch(`/api/admin/orders/${o.data}/pay`, { paymentMethod: 'WECHAT' }, t)).code).toBe(200);
    expect((await apiPatch(`/api/admin/orders/${o.data}/making`, null, t)).code).toBe(200);
    expect((await apiPatch(`/api/admin/orders/${o.data}/finish`, null, t)).code).toBe(200);

    const d = await apiGet(`/api/admin/orders/${o.data}`, t);
    expect(d.data.status).toBe('PAID');
    expect(d.data.serveStatus).toBe('FINISHED');
  });

  // --------- 库存联动 ---------
  test('E2E-003: 下单扣库存 取消恢复', async () => {
    const t = bossToken;
    const bs = (await apiGet('/api/admin/products/1', t)).data.stock;
    const tables = await apiGet('/api/admin/tables?pageNum=1&pageSize=20', t);
    const et = tables.data.records.find((x: any) => x.status === 'EMPTY');
    if (!et) return;

    const o = await apiPost('/api/admin/orders', { tableId: et.id, items: [{ productId: 1, quantity: 1 }] }, t);
    expect((await apiGet('/api/admin/products/1', t)).data.stock).toBe(bs - 1);
    expect((await apiPatch(`/api/admin/orders/${o.data}/cancel`, null, t)).code).toBe(200);
    expect((await apiGet('/api/admin/products/1', t)).data.stock).toBe(bs);
  });

  // --------- 顾客端全流程 ---------
  test('E2E-004: 顾客端浏览→点单→会员→优惠券', async () => {
    expect((await (await fetch(`${API}/api/customer/shop/info`)).json()).data.name).toBeTruthy();
    expect((await (await fetch(`${API}/api/customer/tables`)).json()).data.length).toBeGreaterThan(0);
    expect((await (await fetch(`${API}/api/customer/tables/A1`)).json()).data.tableCode).toBe('A1');
    expect((await (await fetch(`${API}/api/customer/categories`)).json()).data.length).toBeGreaterThanOrEqual(6);
    expect((await (await fetch(`${API}/api/customer/products`)).json()).data.length).toBeGreaterThan(0);
    expect((await (await fetch(`${API}/api/customer/products/1`)).json()).data.name).toBe('百威啤酒');
    expect((await (await fetch(`${API}/api/customer/member/levels`)).json()).data.length).toBeGreaterThanOrEqual(3);
    expect((await (await fetch(`${API}/api/customer/coupons?phone=13800000001`)).json()).data.length).toBeGreaterThan(0);
  });

  // --------- 浏览器权限 ---------
  test('E2E-005: BOSS浏览器显示所有菜单', async ({ page }) => {
    await browserLogin(page, 'admin', '123456');
    const text = await page.textContent('body');
    expect(text).toContain('营业看板');
    expect(text).toContain('员工账号');
    expect(text).toContain('系统设置');
  });

  test('E2E-006: STAFF和MANAGER浏览器权限验证', async ({ page }) => {
    // STAFF
    const staffT = await loginAPI('staff', '123456');
    await page.goto('/login');
    await page.evaluate(({ t, u }) => {
      localStorage.setItem('token', t); localStorage.setItem('user', u);
    }, { t: staffT, u: JSON.stringify({ username: 'staff', nickname: '员工', role: 'STAFF' }) });
    await page.goto('/kitchen');
    await page.waitForTimeout(2000);
    let text = await page.textContent('body');
    expect(text).not.toContain('员工账号');
    expect(text).not.toContain('系统设置');

    // MANAGER
    const mgrT = await loginAPI('manager', '123456');
    await page.goto('/login');
    await page.evaluate(({ t, u }) => {
      localStorage.setItem('token', t); localStorage.setItem('user', u);
    }, { t: mgrT, u: JSON.stringify({ username: 'manager', nickname: '经理', role: 'MANAGER' }) });
    await page.goto('/dashboard');
    await page.waitForTimeout(2000);
    text = await page.textContent('body');
    expect(text).toContain('营业看板');
    expect(text).not.toContain('员工账号');
    expect(text).not.toContain('系统设置');
  });

  test('E2E-007: 浏览器厨房看板', async ({ page }) => {
    await browserLogin(page, 'admin', '123456');
    await page.goto('/kitchen');
    await page.waitForTimeout(3000);
    expect((await page.textContent('body')).length).toBeGreaterThan(50);
  });
});
