"""
寻野酒吧系统 - 自动化测试脚本
使用 browser-use 进行 AI 驱动的浏览器自动化测试
"""
import asyncio
from browser_use import Agent
from langchain_openai import ChatOpenAI
import os

async def test_website():
    """测试寻野酒吧系统网站"""

    # 使用 DeepSeek API
    llm = ChatOpenAI(
        model="deepseek-chat",
        api_key=os.getenv("DEEPSEEK_API_KEY", "sk-xxx"),  # 需要设置环境变量
        base_url="https://api.deepseek.com",
        temperature=0.7,
    )

    # 创建 AI Agent
    agent = Agent(
        task="""
        请帮我测试这个本地网站：http://localhost:8847

        测试内容：
        1. 打开网站首页，检查页面是否正常加载
        2. 查看页面标题和主要内容
        3. 检查导航菜单和主要功能模块
        4. 测试页面上的交互元素（按钮、链接等）
        5. 截图记录关键页面
        6. 检查是否有明显的错误或问题

        请详细描述你看到的内容和测试结果。
        """,
        llm=llm,
        use_vision=True,  # 启用视觉识别
    )

    print("🚀 开始测试寻野酒吧系统...")
    print("=" * 60)

    try:
        # 运行测试
        result = await agent.run()

        print("\n" + "=" * 60)
        print("✅ 测试完成！")
        print("=" * 60)
        print("\n测试结果：")
        print(result)

    except Exception as e:
        print(f"\n❌ 测试过程中出现错误：{e}")
        import traceback
        traceback.print_exc()

if __name__ == "__main__":
    # 运行测试
    asyncio.run(test_website())
