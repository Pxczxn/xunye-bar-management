# XUNYE Bar Management System

[中文说明](README.zh-CN.md)

XUNYE is a full-stack bar management system for daily store operations. It covers staff accounts, product and inventory management, table management, POS ordering, customer mini-program ordering, payment flow, kitchen workflow, order tracking, and business dashboard analytics.

## Features

- Role-based admin system for `BOSS`, `MANAGER`, and `STAFF`.
- React admin dashboard with POS ordering, order ledger, table management, inventory warning, product management, and kitchen workflow.
- WeChat Mini Program customer flow for table scanning, menu browsing, cart checkout, payment simulation, and order status tracking.
- Spring Boot backend with token authentication, MyBatis-Plus persistence, MySQL schema initialization, and acceptance tests.
- Order lifecycle support: unpaid, paid, cancelled, pending production, making, and production finished.
- Dashboard statistics based on paid orders, including revenue, average order value, sales trend, payment methods, and hot products.

## Tech Stack

| Layer | Stack |
| --- | --- |
| Backend | Java 21, Spring Boot 3, MyBatis-Plus, Maven |
| Admin Web | React, TypeScript, Vite, Ant Design, ECharts |
| Mini Program | uni-app, Vue 3, TypeScript, Vite, UnoCSS |
| Database | MySQL 8 |

## Project Structure

```text
XUNYE/
├── xunye-backend/   # Spring Boot backend service
├── xunye-web/       # React admin web app
├── xunye-miniapp/   # uni-app customer mini-program app
├── doc/             # Project documents
└── README.zh-CN.md  # Chinese README
```

## Local Development

### 1. Initialize Database

Create the MySQL database and run:

```text
xunye-backend/src/main/resources/db/init.sql
```

Default local database credentials are configured in `application.yml`.

### 2. Start Backend

```bash
cd xunye-backend
mvn spring-boot:run
```

Backend URL:

```text
http://localhost:8848
```

### 3. Start Admin Web

```bash
cd xunye-web
npm install
npm run dev
```

Admin web URL:

```text
http://localhost:8847
```

### 4. Run Mini Program

```bash
cd xunye-miniapp
pnpm install
pnpm dev:mp
```

Open WeChat Developer Tools and import `xunye-miniapp/dist/dev/mp-weixin`.

## Demo Accounts

| Username | Password | Role |
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

Current verified status:

- Backend tests pass.
- Admin web production build passes.
- Core customer order flow, POS payment flow, kitchen workflow, and dashboard accounting rules are covered.

## Notes

- Customer APIs under `/api/customer/**` are public in the current MVP flow.
- Payment uses a mock provider in development. Real WeChat Pay integration should be implemented through provider configuration, callback verification, and payment status query.
- Dashboard revenue and related payment statistics only count paid orders with valid payment time.
