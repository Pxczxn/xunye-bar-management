# XUNYE Bar Management System

[中文说明](README.zh-CN.md)

XUNYE Bar Management System is a full-stack management solution designed for bar operations, covering staff accounts, product inventory, table management, bar POS, customer mini-program ordering, payment processing, production workflow, order tracking, and business dashboard analytics.

## Features

- Admin panel supports three roles: `BOSS`, `MANAGER`, and `STAFF`.
- React admin dashboard includes POS ordering, order history, table management, inventory alerts, product management, membership, promotions, and production workflow.
- Customer mini-program rebuilt with uni-app, supporting QR code table selection, menu browsing, shopping cart, simulated payment, order status, membership center, and messaging.
- Spring Boot backend provides token authentication, MyBatis-Plus persistence, MySQL initialization scripts, and public customer API endpoints.
- Order lifecycle covers unpaid, paid, cancelled, pending production, in production, and production completed.
- Business dashboard calculates revenue, average order value, sales trends, payment methods, and top-selling products based on paid orders only.

## Tech Stack

| Layer | Stack |
| --- | --- |
| Backend | Java 21, Spring Boot 3, MyBatis-Plus, Maven |
| Admin Web | React, TypeScript, Vite, Ant Design, ECharts |
| Customer Mini Program | uni-app, Vue 3, TypeScript, Vite, UnoCSS |
| Database | MySQL 8 |

## Project Structure

```text
XUNYE/
├── xunye-backend/   # Spring Boot backend service
├── xunye-web/       # React admin web app
├── xunye-miniapp/   # uni-app customer mini-program
├── doc/             # Project documents
└── README.zh-CN.md  # Chinese README
```

## Local Development

### 1. Initialize Database

Create a MySQL database and execute:

```text
xunye-backend/src/main/resources/db/init.sql
```

### 2. Start Backend

```bash
cd xunye-backend
mvn spring-boot:run
```

Backend address:

```text
http://localhost:8848
```

### 3. Start Admin Web

```bash
cd xunye-web
npm install
npm run dev
```

Admin web address:

```text
http://localhost:8847
```

### 4. Start Customer Mini Program

```bash
cd xunye-miniapp
pnpm install
pnpm dev:mp
```

Then open WeChat Developer Tools and import:

```text
xunye-miniapp/dist/dev/mp-weixin
```

## Demo Accounts

| Account | Password | Role |
| --- | --- | --- |
| `admin` | `123456` | BOSS |
| `manager` | `123456` | MANAGER |
| `staff` | `123456` | STAFF |

## Verification

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

## Notes

- Customer API endpoints `/api/customer/**` are publicly accessible in the current MVP phase.
- Payment currently uses simulated payment. Official WeChat Pay integration requires merchant configuration, callback signature verification, and payment status queries.
- Business dashboard revenue and payment statistics only count paid orders with valid payment timestamps.
