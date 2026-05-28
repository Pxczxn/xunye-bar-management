"""
实时浏览器自动化测试 - 交互式测试
"""
import asyncio
from playwright.async_api import async_playwright
import sys
import io

if sys.platform == 'win32':
    sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding='utf-8')

async def interactive_test():
    """交互式测试网站"""

    async with async_playwright() as p:
        # 启动浏览器（可见模式）
        browser = await p.chromium.launch(headless=False, slow_mo=1000)
        context = await browser.new_context(viewport={'width': 1920, 'height': 1080})
        page = await context.new_page()

        print("=" * 60)
        print("开始交互式测试...")
        print("=" * 60)

        try:
            # 打开网站
            print("\n[1] 打开网站首页...")
            await page.goto('http://localhost:8847')
            await page.wait_for_load_state('networkidle')
            print("    页面已加载")

            # 查看登录表单
            print("\n[2] 检查登录表单...")
            username_input = await page.query_selector('input[type="text"], input[placeholder*="账号"], input[placeholder*="USERNAME"]')
            password_input = await page.query_selector('input[type="password"], input[placeholder*="密码"], input[placeholder*="PASSWORD"]')

            if username_input and password_input:
                print("    找到登录表单")

                # 尝试登录
                print("\n[3] 测试登录功能...")
                print("    输入测试账号...")
                await username_input.fill('admin')
                await page.wait_for_timeout(500)

                print("    输入测试密码...")
                await password_input.fill('123456')
                await page.wait_for_timeout(500)

                # 查找登录按钮
                login_button = await page.query_selector('button')
                if login_button:
                    print("    点击登录按钮...")
                    await login_button.click()
                    await page.wait_for_timeout(3000)

                    # 检查是否登录成功
                    current_url = page.url
                    print(f"    当前URL: {current_url}")

                    # 截图
                    await page.screenshot(path='D:/Codeing/Project/XUNYE/screenshots/after_login.png')
                    print("    登录后截图已保存")

                    # 查看页面内容
                    body_text = await page.inner_text('body')
                    print(f"\n[4] 页面内容预览:")
                    print(f"    {body_text[:300]}...")

                    # 查找所有可点击元素
                    print("\n[5] 查找可交互元素...")
                    buttons = await page.query_selector_all('button')
                    links = await page.query_selector_all('a')
                    print(f"    按钮数量: {len(buttons)}")
                    print(f"    链接数量: {len(links)}")

                    # 等待用户观察
                    print("\n[6] 保持浏览器打开30秒供观察...")
                    await page.wait_for_timeout(30000)

            else:
                print("    未找到登录表单")

        except Exception as e:
            print(f"\n[ERROR] {e}")
            import traceback
            traceback.print_exc()

        finally:
            await browser.close()
            print("\n测试结束")

if __name__ == "__main__":
    asyncio.run(interactive_test())
