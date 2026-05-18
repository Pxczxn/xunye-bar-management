# 寻野酒吧管理系统

[English](README.md)

寻野酒吧管理系统是一套面向酒吧日常经营的全栈管理系统，覆盖员工账号、商品库存、桌台管理、吧台 POS、顾客小程序点单、支付流程、出品处理、订单跟踪和营业看板统计。

## 功能特性

- 管理端支持 `BOSS`、`MANAGER`、`STAFF` 三类角色。
- React 管理后台包含 POS 点单、订单流水、桌台管理、库存预警、商品管理、会员管理、活动管理和出品流程。
- 顾客端小程序基于 uni-app 重构，支持扫码选桌、菜单浏览、购物车、模拟支付、订单状态、会员中心和消息入口。
- Spring Boot 后端提供 Token 鉴权、MyBatis-Plus 持久化、MySQL 初始化脚本和顾客端公开接口。
- 订单生命周期覆盖未支付、已支付、已取消、待制作、制作中和制作完成。
- 营业看板按已支付订单统计营收、客单价、销售趋势、支付方式和热销商品。

## 技术栈

| 层级 | 技术 |
| --- | --- |
| 后端 | Java 21, Spring Boot 3, MyBatis-Plus, Maven |
| 管理后台 | React, TypeScript, Vite, Ant Design, ECharts |
| 顾客小程序 | uni-app, Vue 3, TypeScript, Vite, UnoCSS |
| 数据库 | MySQL 8 |

## 项目结构

```text
XUNYE/
├── xunye-backend/   # Spring Boot 后端服务
├── xunye-web/       # React 管理后台
├── xunye-miniapp/   # uni-app 顾客端小程序
├── doc/             # 项目文档
└── miniapp设计.md   # 顾客端小程序设计文档
```

## 本地开发

### 1. 初始化数据库

创建 MySQL 数据库并执行：

```text
xunye-backend/src/main/resources/db/init.sql
```

### 2. 启动后端

```bash
cd xunye-backend
mvn spring-boot:run
```

后端地址：

```text
http://localhost:8848
```

### 3. 启动管理后台

```bash
cd xunye-web
npm install
npm run dev
```

管理后台地址：

```text
http://localhost:8847
```

### 4. 启动顾客小程序

```bash
cd xunye-miniapp
pnpm install
pnpm dev:mp
```

随后打开微信开发者工具，导入：

```text
xunye-miniapp/dist/dev/mp-weixin
```

## 演示账号

| 账号 | 密码 | 角色 |
| --- | --- | --- |
| `admin` | `123456` | BOSS |
| `manager` | `123456` | MANAGER |
| `staff` | `123456` | STAFF |

## 验证

```bash
cd xunye-backend
mvn test
```

```bash
cd xunye-web
npm run build
```

```bash
cd xunye-miniapp
pnpm build:mp
```

## 说明

- 顾客端接口 `/api/customer/**` 当前 MVP 流程中公开访问。
- 支付当前使用模拟支付，正式接入微信支付时需要补充商户配置、回调验签和支付状态查询。
- 营业看板中的营收和支付统计仅统计有有效支付时间的已支付订单。
