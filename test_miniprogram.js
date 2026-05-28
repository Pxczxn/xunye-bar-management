/**
 * 微信小程序自动化测试脚本
 * 使用 miniprogram-automator 进行自动化测试
 */

const automator = require('miniprogram-automator');

// 配置
const config = {
  // 小程序项目路径（编译后的）
  projectPath: 'D:/Codeing/Project/XUNYE/xunye-miniapp/dist/dev/mp-weixin',

  // 微信开发者工具端口（默认 9420）
  port: 9420,

  // 测试超时时间
  timeout: 60000
};

async function runTests() {
  console.log('='.repeat(60));
  console.log('开始微信小程序自动化测试');
  console.log('='.repeat(60));

  let miniProgram;

  try {
    // 1. 启动小程序
    console.log('\n[步骤 1] 连接微信开发者工具...');
    miniProgram = await automator.launch({
      projectPath: config.projectPath,
      port: config.port
    });
    console.log('   [OK] 已连接到微信开发者工具');

    // 2. 获取首页
    console.log('\n[步骤 2] 打开小程序首页...');
    const page = await miniProgram.reLaunch('/pages/index/index');
    await page.waitFor(2000);
    console.log('   [OK] 首页已加载');

    // 3. 截图
    console.log('\n[步骤 3] 截图保存...');
    await page.screenshot({
      path: 'D:/Codeing/Project/XUNYE/screenshots/miniprogram_home.png'
    });
    console.log('   [OK] 截图已保存');

    // 4. 获取页面元素
    console.log('\n[步骤 4] 检查页面元素...');
    const title = await page.$('.title');
    if (title) {
      const titleText = await title.text();
      console.log(`   [OK] 找到标题: ${titleText}`);
    }

    // 5. 查找按钮
    const buttons = await page.$$('button');
    console.log(`   [OK] 找到 ${buttons.length} 个按钮`);

    // 6. 测试登录（如果有登录页面）
    console.log('\n[步骤 5] 测试登录功能...');

    // 查找输入框
    const inputs = await page.$$('input');
    console.log(`   [OK] 找到 ${inputs.length} 个输入框`);

    if (inputs.length >= 2) {
      // 填写账号
      await inputs[0].input('admin');
      console.log('   [OK] 已输入账号');

      // 填写密码
      await inputs[1].input('123456');
      console.log('   [OK] 已输入密码');

      // 点击登录按钮
      if (buttons.length > 0) {
        await buttons[0].tap();
        console.log('   [OK] 已点击登录按钮');

        // 等待跳转
        await page.waitFor(3000);

        // 截图登录后页面
        await page.screenshot({
          path: 'D:/Codeing/Project/XUNYE/screenshots/miniprogram_after_login.png'
        });
        console.log('   [OK] 登录后截图已保存');
      }
    }

    // 7. 获取页面数据
    console.log('\n[步骤 6] 获取页面数据...');
    const data = await page.data();
    console.log('   [OK] 页面数据:', JSON.stringify(data, null, 2).substring(0, 200) + '...');

    // 8. 测试导航
    console.log('\n[步骤 7] 测试页面导航...');
    const pages = await miniProgram.pageStack();
    console.log(`   [OK] 当前页面栈: ${pages.length} 个页面`);
    pages.forEach((p, i) => {
      console.log(`      ${i + 1}. ${p.path}`);
    });

    // 9. 测试完成
    console.log('\n' + '='.repeat(60));
    console.log('[SUCCESS] 测试完成！');
    console.log('='.repeat(60));

    // 保持小程序打开 10 秒
    console.log('\n小程序将在 10 秒后关闭...');
    await page.waitFor(10000);

  } catch (error) {
    console.error('\n[ERROR] 测试失败:', error.message);
    console.error('\n可能的原因:');
    console.error('1. 微信开发者工具未打开');
    console.error('2. 未开启"服务端口"（设置 -> 安全设置 -> 服务端口）');
    console.error('3. 端口号不正确（默认 9420）');
    console.error('4. 项目路径不正确');
    console.error('5. 项目未编译（需要先运行 npm run dev:mp-weixin）');
  } finally {
    // 关闭小程序
    if (miniProgram) {
      await miniProgram.close();
      console.log('\n小程序已关闭');
    }
  }
}

// 运行测试
runTests().catch(console.error);
