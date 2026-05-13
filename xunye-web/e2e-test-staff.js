const { chromium } = require('playwright');

(async () => {
  const browser = await chromium.launch({ headless: true });
  const page = await browser.newPage();
  const results = [];

  try {
    // 登录
    console.log('正在登录...');
    await page.goto('http://localhost:8847/login', { waitUntil: 'domcontentloaded', timeout: 15000 });
    await page.waitForSelector('input[placeholder="账号"]', { timeout: 10000 });
    await page.fill('input[placeholder="账号"]', 'admin');
    await page.fill('input[type="password"]', 'admin123');
    await page.click('button:has-text("登录")');
    await page.waitForTimeout(2000);
    await page.goto('http://localhost:8847/admin/staff', { waitUntil: 'domcontentloaded', timeout: 15000 });
    await page.waitForTimeout(2000);
    console.log('登录成功，进入员工管理页面');

    // 场景1：新增员工
    console.log('\n--- 场景1：新增员工 ---');
    try {
      const addBtn = await page.$('button:has-text("新增员工")');
      if (!addBtn) throw new Error('未找到新增员工按钮');
      await addBtn.click();
      await page.waitForTimeout(500);

      await page.fill('input[placeholder="登录账号"]', 'test_e2e_001');
      await page.fill('input[placeholder="登录密码"]', 'test123');
      await page.fill('input[placeholder="显示昵称"]', 'E2E测试员工');

      // 角色选择 - 点击Select
      const roleSelect = page.locator('.ant-select').filter({ hasText: '请选择角色' });
      if (await roleSelect.count() > 0) {
        await roleSelect.click();
        await page.waitForTimeout(300);
        await page.click('.ant-select-item-option:has-text("员工")');
        await page.waitForTimeout(300);
      }

      // 状态选择
      const statusSelect = page.locator('.ant-select').filter({ hasText: '请选择状态' });
      if (await statusSelect.count() > 0) {
        await statusSelect.click();
        await page.waitForTimeout(300);
        await page.click('.ant-select-item-option:has-text("启用")');
        await page.waitForTimeout(300);
      }

      await page.click('.ant-modal-footer button:has-text("新增")');
      await page.waitForTimeout(2000);

      // 验证：检查是否出现成功提示或列表中有新记录
      const successMsg = await page.$('.ant-message-success:has-text("新增成功")');
      const recordExists = await page.$('td:text("test_e2e_001")');
      if (successMsg || recordExists) {
        results.push({ scenario: '新增员工', status: '通过', msg: '新增成功，列表刷新显示新记录' });
      } else {
        // 尝试截图看看弹窗是否还在
        await page.screenshot({ path: 'e2e-add.png', fullPage: true });
        results.push({ scenario: '新增员工', status: '失败', msg: '未检测到新增成功提示或新记录' });
      }
    } catch (e) {
      results.push({ scenario: '新增员工', status: '失败', msg: e.message });
    }

    // 场景2：编辑员工
    console.log('\n--- 场景2：编辑员工 ---');
    try {
      // 先找到第一行记录的编辑按钮
      const editBtn = await page.$('tbody tr:first-child .hover\\:text-brand-gold transition-colors');
      if (!editBtn) throw new Error('未找到编辑按钮');
      await editBtn.click();
      await page.waitForTimeout(1000);

      // 修改昵称
      await page.fill('input[placeholder="显示昵称"]', 'E2E编辑后昵称');

      await page.click('.ant-modal-footer button:has-text("保存")');
      await page.waitForTimeout(2000);

      const successMsg = await page.$('.ant-message-success:has-text("修改成功")');
      const nicknameUpdated = await page.$('td:text("E2E编辑后昵称")');
      if (successMsg || nicknameUpdated) {
        results.push({ scenario: '编辑员工', status: '通过', msg: '修改成功，列表刷新显示更新' });
      } else {
        results.push({ scenario: '编辑员工', status: '失败', msg: '未检测到修改成功提示或更新记录' });
      }
    } catch (e) {
      results.push({ scenario: '编辑员工', status: '失败', msg: e.message });
    }

    // 场景3：禁用/启用
    console.log('\n--- 场景3：禁用/启用 ---');
    try {
      // 找到状态切换按钮（Toggle图标）
      const toggleBtn = await page.$('tbody tr:first-child .hover\\:text-danger, tbody tr:first-child .hover\\:text-success');
      if (!toggleBtn) throw new Error('未找到状态切换按钮');
      await toggleBtn.click();
      await page.waitForTimeout(500);

      // 确认弹窗
      const confirmBtn = await page.$('.ant-modal .ant-btn:has-text("确认")');
      if (confirmBtn) {
        await confirmBtn.click();
      } else {
        await page.click('.ant-modal .ant-btn-primary:has-text("确认")');
      }
      await page.waitForTimeout(2000);

      const successMsg = await page.$('.ant-message-success');
      if (successMsg) {
        const text = await page.$eval('.ant-message-success', el => el.textContent);
        if (text.includes('成功')) {
          results.push({ scenario: '禁用/启用', status: '通过', msg: '状态切换成功' });
        } else {
          results.push({ scenario: '禁用/启用', status: '失败', msg: '提示内容异常: ' + text });
        }
      } else {
        results.push({ scenario: '禁用/启用', status: '失败', msg: '未检测到操作成功提示' });
      }
    } catch (e) {
      results.push({ scenario: '禁用/启用', status: '失败', msg: e.message });
    }

    // 场景4：重置密码
    console.log('\n--- 场景4：重置密码 ---');
    try {
      // 找到锁图标按钮
      const lockBtn = await page.$('tbody tr:first-child .hover\\:text-brand-gold:nth-of-type(2)');
      if (!lockBtn) throw new Error('未找到重置密码按钮');
      await lockBtn.click();
      await page.waitForTimeout(1000);

      // 输入新密码
      const pwdInput = await page.$('#password');
      if (pwdInput) {
        await pwdInput.fill('newpass456');
      } else {
        await page.fill('input[type="password"][placeholder="请输入新密码"]', 'newpass456');
      }

      await page.click('.ant-modal-footer button:has-text("确认重置")');
      await page.waitForTimeout(2000);

      const successMsg = await page.$('.ant-message-success:has-text("密码重置成功")');
      if (successMsg) {
        results.push({ scenario: '重置密码', status: '通过', msg: '密码重置成功' });
      } else {
        results.push({ scenario: '重置密码', status: '失败', msg: '未检测到密码重置成功提示' });
      }
    } catch (e) {
      results.push({ scenario: '重置密码', status: '失败', msg: e.message });
    }

    // 场景5：删除员工
    console.log('\n--- 场景5：删除员工 ---');
    try {
      // 找到删除按钮（Trash图标）
      const deleteBtn = await page.$('tbody tr:first-child .hover\\:text-danger:last-of-type');
      if (!deleteBtn) throw new Error('未找到删除按钮');
      await deleteBtn.click();
      await page.waitForTimeout(500);

      // 确认弹窗
      const confirmBtn = await page.$('.ant-modal .ant-btn-danger:has-text("删除"), .ant-modal .ant-btn:has-text("删除")');
      if (confirmBtn) {
        await confirmBtn.click();
      }
      await page.waitForTimeout(2000);

      const successMsg = await page.$('.ant-message-success:has-text("删除成功")');
      if (successMsg) {
        results.push({ scenario: '删除员工', status: '通过', msg: '删除成功，列表刷新' });
      } else {
        results.push({ scenario: '删除员工', status: '失败', msg: '未检测到删除成功提示' });
      }
    } catch (e) {
      results.push({ scenario: '删除员工', status: '失败', msg: e.message });
    }

    // 场景6：筛选分页
    console.log('\n--- 场景6：筛选分页 ---');
    try {
      // 输入关键词
      await page.fill('input[placeholder="账号/昵称"]', 'admin');
      await page.click('button:has-text("查询")');
      await page.waitForTimeout(2000);

      // 验证筛选结果
      const hasRecords = await page.$('tbody tr');
      if (hasRecords) {
        const rows = await page.$$('tbody tr');
        if (rows.length > 0) {
          results.push({ scenario: '筛选分页', status: '通过', msg: `筛选成功，表格显示 ${rows.length} 条结果` });
        } else {
          results.push({ scenario: '筛选分页', status: '通过', msg: '筛选完成，无匹配数据（正常）' });
        }
      } else {
        results.push({ scenario: '筛选分页', status: '失败', msg: '筛选后表格无数据' });
      }
    } catch (e) {
      results.push({ scenario: '筛选分页', status: '失败', msg: e.message });
    }

    // 输出结果
    console.log('\n\n==================== 测试结果 ====================');
    results.forEach((r, i) => {
      const icon = r.status === '通过' ? '✅' : '❌';
      console.log(`${i + 1}. ${icon} ${r.scenario}: ${r.msg}`);
    });
    console.log('=================================================');

  } catch (error) {
    console.error('测试执行出错:', error);
    await page.screenshot({ path: 'e2e-error.png', fullPage: true });
  } finally {
    await browser.close();
  }
})();
