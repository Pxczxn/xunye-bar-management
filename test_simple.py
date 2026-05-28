"""
寻野酒吧系统 - 简单自动化测试
使用 Playwright 进行浏览器自动化测试
"""
import asyncio
from playwright.async_api import async_playwright
import os
import sys

# 设置输出编码为 UTF-8
if sys.platform == 'win32':
    import io
    sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding='utf-8')

async def test_website():
    """测试寻野酒吧系统网站"""

    print("开始测试寻野酒吧系统...")
    print("=" * 60)

    async with async_playwright() as p:
        # 启动浏览器（可见模式）
        browser = await p.chromium.launch(headless=False)
        context = await browser.new_context(
            viewport={'width': 1920, 'height': 1080}
        )
        page = await context.new_page()

        try:
            # 1. 打开网站首页
            print("\n[步骤 1] 打开网站首页...")
            await page.goto('http://localhost:8847', wait_until='networkidle')
            await page.wait_for_timeout(2000)

            # 获取页面标题
            title = await page.title()
            print(f"   [OK] 页面标题: {title}")

            # 截图
            screenshot_path = 'D:/Codeing/Project/XUNYE/screenshots/homepage.png'
            os.makedirs(os.path.dirname(screenshot_path), exist_ok=True)
            await page.screenshot(path=screenshot_path, full_page=True)
            print(f"   [OK] 截图已保存: {screenshot_path}")

            # 2. 检查页面内容
            print("\n[步骤 2] 检查页面主要元素...")

            # 获取页面文本内容
            body_text = await page.inner_text('body')
            print(f"   [OK] 页面包含文本内容 (前200字符): {body_text[:200]}...")

            # 3. 查找所有链接
            print("\n[步骤 3] 检查页面链接...")
            links = await page.query_selector_all('a')
            print(f"   [OK] 找到 {len(links)} 个链接")

            # 4. 查找所有按钮
            print("\n[步骤 4] 检查页面按钮...")
            buttons = await page.query_selector_all('button')
            print(f"   [OK] 找到 {len(buttons)} 个按钮")

            # 5. 检查导航菜单
            print("\n[步骤 5] 检查导航菜单...")
            nav_elements = await page.query_selector_all('nav, [role="navigation"]')
            print(f"   [OK] 找到 {len(nav_elements)} 个导航元素")

            # 6. 检查表单
            print("\n[步骤 6] 检查表单元素...")
            forms = await page.query_selector_all('form')
            inputs = await page.query_selector_all('input')
            print(f"   [OK] 找到 {len(forms)} 个表单")
            print(f"   [OK] 找到 {len(inputs)} 个输入框")

            # 7. 检查控制台错误
            print("\n[步骤 7] 监听控制台消息...")
            console_messages = []

            def handle_console(msg):
                console_messages.append({
                    'type': msg.type,
                    'text': msg.text
                })

            page.on('console', handle_console)

            # 刷新页面以捕获控制台消息
            await page.reload(wait_until='networkidle')
            await page.wait_for_timeout(2000)

            errors = [m for m in console_messages if m['type'] == 'error']
            warnings = [m for m in console_messages if m['type'] == 'warning']

            print(f"   [OK] 控制台错误: {len(errors)} 个")
            print(f"   [OK] 控制台警告: {len(warnings)} 个")

            if errors:
                print("\n   [WARNING] 控制台错误详情:")
                for err in errors[:5]:  # 只显示前5个
                    print(f"      - {err['text']}")

            # 8. 测试页面响应性
            print("\n[步骤 8] 测试页面响应性...")
            await page.set_viewport_size({'width': 375, 'height': 667})  # 手机尺寸
            await page.wait_for_timeout(1000)
            await page.screenshot(path='D:/Codeing/Project/XUNYE/screenshots/mobile.png')
            print("   [OK] 移动端截图已保存")

            await page.set_viewport_size({'width': 768, 'height': 1024})  # 平板尺寸
            await page.wait_for_timeout(1000)
            await page.screenshot(path='D:/Codeing/Project/XUNYE/screenshots/tablet.png')
            print("   [OK] 平板端截图已保存")

            # 9. 性能测试
            print("\n[步骤 9] 性能测试...")
            await page.goto('http://localhost:8847', wait_until='load')
            performance = await page.evaluate('''() => {
                const timing = performance.timing;
                return {
                    loadTime: timing.loadEventEnd - timing.navigationStart,
                    domReady: timing.domContentLoadedEventEnd - timing.navigationStart,
                    responseTime: timing.responseEnd - timing.requestStart
                };
            }''')

            print(f"   [OK] 页面加载时间: {performance['loadTime']}ms")
            print(f"   [OK] DOM就绪时间: {performance['domReady']}ms")
            print(f"   [OK] 响应时间: {performance['responseTime']}ms")

            print("\n" + "=" * 60)
            print("[SUCCESS] 测试完成！")
            print("=" * 60)

            # 保持浏览器打开10秒，让你查看
            print("\n浏览器将在10秒后关闭...")
            await page.wait_for_timeout(10000)

        except Exception as e:
            print(f"\n[ERROR] 测试过程中出现错误：{e}")
            import traceback
            traceback.print_exc()

        finally:
            await browser.close()

if __name__ == "__main__":
    asyncio.run(test_website())
