# 寻野酒吧顾客端小程序

这是寻野酒吧管理系统的顾客端小程序，基于 `uni-app + Vue 3 + TypeScript + Vite + UnoCSS` 构建。它承接顾客扫码点餐、菜单浏览、购物车、模拟支付、订单跟踪、会员中心和消息入口等流程。

## 功能

- 首页品牌展示与快捷点餐入口。
- 菜单分类浏览和商品选择。
- 订单列表与订单状态查看。
- 会员中心、会员等级和活动信息入口。
- 消息页面用于承接订单通知和活动提醒。
- 请求层接入后端顾客端接口。

## 技术栈

| 模块 | 技术 |
| --- | --- |
| 框架 | uni-app, Vue 3 |
| 语言 | TypeScript |
| 构建 | Vite |
| 样式 | UnoCSS, SCSS |
| 状态 | Pinia |
| 请求 | alova / uni request 封装 |

## 本地运行

```bash
pnpm install
pnpm dev:mp
```

微信开发者工具导入：

```text
dist/dev/mp-weixin
```

## 构建

```bash
pnpm build:mp
```

构建产物：

```text
dist/build/mp-weixin
```

## 目录

```text
xunye-miniapp/
├── src/api/          # 接口定义
├── src/http/         # 请求封装
├── src/pages/        # 页面
├── src/store/        # Pinia 状态
├── src/static/       # 静态资源
├── src/tabbar/       # 自定义 tabbar
└── src/utils/        # 工具方法
```

## 说明

- 小程序当前面向微信小程序调试和构建。
- 后端服务默认运行在 `http://localhost:8848`。
- 支付流程当前为开发环境模拟支付，正式接入微信支付前需要补充支付配置、回调验签和状态查询。
