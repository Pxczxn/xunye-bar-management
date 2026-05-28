const automator = require('miniprogram-automator')
const { spawn } = require('child_process')
const path = require('path')

const PROJECT_PATH = 'D:\\Codeing\\Project\\XUNYE\\xunye-miniapp\\dist\\dev\\mp-weixin'
const CLI_PATH = 'D:\\Codeing\\Software\\微信web开发者工具\\cli.bat'
const SERVICE_PORT = 23445

function withTimeout(promise, ms, label) {
  return Promise.race([
    promise,
    new Promise((_, reject) =>
      setTimeout(() => reject(new Error(`${label} 超时 (${ms}ms)`)), ms)
    ),
  ])
}

async function main() {
  console.log('=== 小程序自动化连接测试 ===')
  console.log('项目路径:', PROJECT_PATH)
  console.log('WebSocket 端口: 9420')
  console.log('')

  try {
    // 直接连接已运行的开发者工具
    console.log('连接开发者工具 (ws://127.0.0.1:9420)...')
    const miniProgram = await automator.connect({
      wsEndpoint: 'ws://127.0.0.1:9420'
    })
    console.log('[OK] 连接成功!')

    // 测试 pageStack
    try {
      const stack = await withTimeout(miniProgram.pageStack(), 10000, 'pageStack')
      console.log('[OK] pageStack 长度:', stack.length)
    } catch (e) {
      console.log('[WARN] pageStack:', e.message)
    }

    // 测试 currentPage
    try {
      const page = await withTimeout(miniProgram.currentPage(), 10000, 'currentPage')
      console.log('[OK] currentPage:', page.path)

      const data = await withTimeout(page.data(), 5000, 'page.data')
      console.log('[OK] 页面数据 keys:', Object.keys(data))

      const elements = await withTimeout(page.$$('view'), 5000, 'page.$$')
      console.log('[OK] view 元素数量:', elements.length)
    } catch (e) {
      console.log('[WARN]', e.message)
    }

    // 截图
    try {
      const sp = path.resolve(__dirname, '..', 'automation', 'screenshot.png')
      await withTimeout(miniProgram.screenshot({ path: sp }), 10000, 'screenshot')
      console.log('[OK] 截图已保存:', sp)
    } catch (e) {
      console.log('[WARN] screenshot:', e.message)
    }

    console.log('\n=== 测试通过! ===')
    await miniProgram.close()
  } catch (err) {
    console.error('[ERROR]', err.message)
    console.error('\n请确认:')
    console.error('1. 微信开发者工具已开启服务端口 (设置 -> 安全设置)')
    console.error('2. 小程序已编译 (dist/dev/mp-weixin 目录存在)')
    process.exit(1)
  }
}

main()
