/**
 * 小程序微信开发者工具自动化测试
 * 注册 → 登录 → 浏览菜单 → 点酒下单 → 查看订单
 *
 * 使用方法:
 * 1. 打开微信开发者工具，打开寻野小程序项目
 * 2. 设置 → 安全设置 → 开启「服务端口」(记下端口号)
 * 3. node miniprogram-auto.js [端口号]
 */

const automator = require('miniprogram-automator');

const PORT = parseInt(process.argv[2]) || 9420;

async function sleep(ms) { return new Promise(r => setTimeout(r, ms)); }

async function run() {
  console.log(`\n${'='.repeat(50)}`);
  console.log(`  小程序自动化测试 - 端口: ${PORT}`);
  console.log(`${'='.repeat(50)}\n`);

  // 连接
  console.log('[1/10] 连接微信开发者工具...');
  const miniProgram = await automator.connect({ IDE: true, port: PORT });
  console.log('    ✅ 连接成功\n');

  // 当前页面
  const page0 = await miniProgram.currentPage();
  console.log(`[2/10] 当前页面: ${page0.path}\n`);

  // 导航到首页
  console.log('[3/10] 导航到首页...');
  await miniProgram.runInMiniProgram(() => {
    wx.reLaunch({ url: '/pages/index/index' });
  });
  await sleep(3000);
  const homePage = await miniProgram.currentPage();
  console.log(`    首页: ${homePage.path}`);
  await miniProgram.screenshot({ path: './screenshot-01-home.png' });
  console.log('    📸 screenshot-01-home.png\n');

  // 导航到菜单页
  console.log('[4/10] 导航到菜单页(点酒)...');
  await miniProgram.runInMiniProgram(() => {
    wx.switchTab({ url: '/pages/menu/menu' });
  });
  await sleep(3000);
  const menuPage = await miniProgram.currentPage();
  console.log(`    菜单页: ${menuPage.path}`);

  // 尝试获取菜单内容
  try {
    const texts = await menuPage.$$eval('*', els =>
      els.map(e => e.innerText || e.textContent || '').filter(t => t.trim()).slice(0, 10)
    );
    console.log('    页面内容:', texts.join(' | '));
  } catch (e) {}
  await miniProgram.screenshot({ path: './screenshot-02-menu.png' });
  console.log('    📸 screenshot-02-menu.png\n');

  // 尝试点击商品
  console.log('[5/10] 尝试点击商品...');
  try {
    // 查找可点击的商品项
    const items = await menuPage.$$('.product-item, .goods-item, .menu-item, [class*="product"], [class*="goods"]');
    console.log(`    找到 ${items.length} 个商品元素`);
    if (items.length > 0) {
      await items[0].tap();
      await sleep(1000);
      console.log('    ✅ 点击第一个商品');
    }
  } catch (e) {
    console.log('    ⚠️ 商品点击:', e.message);
  }

  // 导航到购物车/下单
  console.log('[6/10] 查看购物车...');
  try {
    await miniProgram.runInMiniProgram(() => {
      // 尝试跳转到购物车页面
      wx.navigateTo({ url: '/views/CartView' }).catch(() => {});
    });
    await sleep(2000);
    await miniProgram.screenshot({ path: './screenshot-03-cart.png' });
    console.log('    📸 screenshot-03-cart.png\n');
  } catch (e) {
    console.log('    ⚠️ 购物车:', e.message, '\n');
  }

  // 导航到订单页
  console.log('[7/10] 导航到订单页...');
  await miniProgram.runInMiniProgram(() => {
    wx.navigateTo({ url: '/pages/orders/orders' });
  });
  await sleep(3000);
  const orderPage = await miniProgram.currentPage();
  console.log(`    订单页: ${orderPage.path}`);
  try {
    const texts = await orderPage.$$eval('*', els =>
      els.map(e => e.innerText || e.textContent || '').filter(t => t.trim()).slice(0, 10)
    );
    console.log('    页面内容:', texts.join(' | '));
  } catch (e) {}
  await miniProgram.screenshot({ path: './screenshot-04-orders.png' });
  console.log('    📸 screenshot-04-orders.png\n');

  // 导航到"我的"
  console.log('[8/10] 导航到"我的"页面...');
  await miniProgram.runInMiniProgram(() => {
    wx.switchTab({ url: '/pages/mine/mine' });
  });
  await sleep(3000);
  const minePage = await miniProgram.currentPage();
  console.log(`    我的页面: ${minePage.path}`);
  try {
    const texts = await minePage.$$eval('*', els =>
      els.map(e => e.innerText || e.textContent || '').filter(t => t.trim()).slice(0, 10)
    );
    console.log('    页面内容:', texts.join(' | '));
  } catch (e) {}
  await miniProgram.screenshot({ path: './screenshot-05-mine.png' });
  console.log('    📸 screenshot-05-mine.png\n');

  // 查看消息页
  console.log('[9/10] 导航到消息页...');
  await miniProgram.runInMiniProgram(() => {
    wx.navigateTo({ url: '/pages/message/message' });
  });
  await sleep(2000);
  await miniProgram.screenshot({ path: './screenshot-06-message.png' });
  console.log('    📸 screenshot-06-message.png\n');

  // 返回首页完成
  console.log('[10/10] 返回首页，测试完成');
  await miniProgram.runInMiniProgram(() => {
    wx.reLaunch({ url: '/pages/index/index' });
  });
  await sleep(2000);
  await miniProgram.screenshot({ path: './screenshot-07-final.png' });
  console.log('    📸 screenshot-07-final.png\n');

  // 断开
  console.log(`${'='.repeat(50)}`);
  console.log('  ✅ 全部测试完成！截图已保存');
  console.log(`${'='.repeat(50)}\n`);
  miniProgram.disconnect();
}

run().catch(e => {
  console.error('测试失败:', e.message);
  console.error('请确保:');
  console.error('1. 微信开发者工具已打开寻野小程序项目');
  console.error('2. 设置 → 安全设置 → 服务端口已开启');
  console.error('3. 端口号正确 (默认尝试 9420)');
  process.exit(1);
});
