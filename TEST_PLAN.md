# 寻野酒吧管理系统 (XUNYE) - 完整测试规划文档

> 生成日期: 2026-05-28
> 项目版本: v1.0.0

---

## 一、项目概况

| 维度 | 技术选型 |
|------|---------|
| 后端 | Spring Boot 3.2.5 + MyBatis-Plus 3.5.6 + MySQL + Redis (Java 21) |
| 管理后台 | React 19 + TypeScript 5.9 + Ant Design 6 + Vite 8 + TailwindCSS 4 |
| 小程序 | uni-app (Vue 3) → 微信小程序 |
| 数据库 | MySQL 8+, 15+ 张表 |
| 认证 | 自定义 Token + BCrypt + 角色拦截器 (BOSS / MANAGER / STAFF) |
| 支付 | Mock / 微信支付 Provider 模式 |
| 后端端口 | 8848 |
| 前端端口 | 8847 (Vite dev) |

### 1.1 数据库表清单

| 序号 | 表名 | 说明 |
|------|------|------|
| 1 | product_category | 商品分类 |
| 2 | product | 酒水商品 |
| 3 | product_brand | 商品品牌 |
| 4 | inventory_record | 库存流水 |
| 5 | table_area | 桌台区域 |
| 6 | bar_table | 桌台 |
| 7 | order_info | 订单 |
| 8 | order_item | 订单项 |
| 9 | payment_order | 支付单 |
| 10 | staff_user | 员工用户 |
| 11 | customer | 顾客 |
| 12 | customer_coupon | 顾客优惠券 |
| 13 | customer_points_record | 顾客积分记录 |
| 14 | customer_message | 顾客消息 |
| 15 | member_activity | 会员活动 |
| 16 | member_level_config | 会员等级配置 |
| 17 | coupon_template | 优惠券模板 |
| 18 | discount_rule | 折扣规则 |
| 19 | audit_log | 操作日志 |
| 20 | system_config | 系统配置 |

### 1.2 角色权限矩阵

| 功能模块 | BOSS | MANAGER | STAFF |
|---------|------|---------|-------|
| Dashboard 仪表盘 | Y | Y | - |
| 商品管理 | Y | Y | - |
| 库存管理 | Y | Y | - |
| 分类管理 | Y | Y | - |
| 会员管理 | Y | Y | - |
| 活动管理 | Y | Y | - |
| 优惠券模板 | Y | Y | - |
| 会员等级配置 | Y | Y | - |
| 折扣规则 | Y | Y | - |
| 员工管理 | Y | - | - |
| 系统设置 | Y | - | - |
| 厨房出单 | Y | Y | Y |
| 订单管理 | Y | Y | Y |
| POS 收银 | Y | Y | Y |
| 桌台管理 | Y | Y | Y |

---

## 二、现有测试现状

### 2.1 后端已有测试文件 (13 个)

| 文件 | 类型 | 覆盖范围 |
|------|------|---------|
| HashTest.java | 工具测试 | 密码哈希 |
| AuthControllerTest.java | 集成测试 | 登录认证 |
| BarTableClearControllerTest.java | 集成测试 | 清台 |
| CustomerAcceptanceTest.java | 集成测试 | 顾客端验收 |
| CustomerPaymentControllerTest.java | 集成测试 | 顾客支付 |
| DashboardControllerTest.java | 集成测试 | 仪表盘 |
| FullApiTest.java | 集成测试 | 全量 API |
| OrderControllerTest.java | 集成测试 | 订单 |
| OrderSourceTest.java | 集成测试 | 订单来源 |
| StaffAccountControllerTest.java | 集成测试 | 员工账户 |
| StaffLoginFlowTest.java | 集成测试 | 员工登录流程 |
| StaffPageBugTest.java | 集成测试 | 员工分页 Bug |
| TokenAuthFlowTest.java | 集成测试 | Token 认证流程 |

### 2.2 覆盖率评估

| 模块 | 单元测试 | 集成测试 | 接口测试 | 覆盖率 |
|------|---------|---------|---------|--------|
| AuthService | 无 | 部分 | 有 | ~20% |
| OrderService | 无 | 部分 | 有 | ~15% |
| ProductService | 无 | 无 | 无 | 0% |
| InventoryService | 无 | 无 | 无 | 0% |
| CustomerService | 无 | 部分 | 部分 | ~10% |
| StaffService | 无 | 部分 | 部分 | ~10% |
| BarTableService | 无 | 部分 | 部分 | ~10% |
| 会员/营销 Service | 无 | 无 | 无 | 0% |
| DashboardService | 无 | 无 | 部分 | ~5% |
| 前端 | - | - | - | 0% |
| 小程序 | - | - | - | 0% |

---

## 三、测试分层策略 (四层金字塔)

```
              / E2E 测试 \           手动 / Playwright 关键业务流
             / 接口测试   \          REST API 全量覆盖 (MockMvc)
            / 集成测试     \         Service + Mapper + DB 联动
           / 单元测试       \        Service / Util 纯逻辑
```

| 层级 | 工具 | 目标覆盖率 | 执行频率 |
|------|------|-----------|---------|
| 单元测试 | JUnit 5 + Mockito | Service 层 ≥ 80% | 每次提交 |
| 集成测试 | @SpringBootTest + H2/Testcontainers | 核心流程 100% | 每次 PR |
| 接口测试 | MockMvc + @AutoConfigureMockMvc | Controller ≥ 70% | 每次 PR |
| E2E 测试 | 手动 + 可选 Playwright | 核心 6 条流程 | 每次发版 |

---

## 四、后端测试计划

### 4.1 单元测试 (Service 纯逻辑)

> 工具: JUnit 5 + Mockito
> 路径: `src/test/java/com/xunye/admin/service/`

#### 4.1.1 AuthServiceTest

| 编号 | 用例 | 输入 | 期望输出 |
|------|------|------|---------|
| UT-AUTH-001 | 生成 Token | userId + role | 非空 Token 字符串 |
| UT-AUTH-002 | 验证有效 Token | 有效 Token | null (无错误) |
| UT-AUTH-003 | 验证过期 Token | 过期 Token | 错误信息 |
| UT-AUTH-004 | 验证伪造 Token | 随机字符串 | 错误信息 |
| UT-AUTH-005 | 从 Token 提取 Role | 有效 Token | 正确 Role |
| UT-AUTH-006 | BCrypt 密码加密 | 明文密码 | BCrypt 哈希 |
| UT-AUTH-007 | BCrypt 密码校验 (正确) | 明文 + 正确哈希 | true |
| UT-AUTH-008 | BCrypt 密码校验 (错误) | 明文 + 错误哈希 | false |

#### 4.1.2 OrderServiceTest

| 编号 | 用例 | 输入 | 期望输出 |
|------|------|------|---------|
| UT-ORD-001 | 订单编号生成格式 | - | 匹配 `ORDyyyyMMddHHmmssXXXX` |
| UT-ORD-002 | 订单编号唯一性 | 100 次生成 | 全部不同 |
| UT-ORD-003 | 金额计算 (单商品) | 1 商品 x 数量 3 | price * 3 |
| UT-ORD-004 | 金额计算 (多商品) | 3 个不同商品 | 各项金额之和 |
| UT-ORD-005 | 状态转换 UNPAID → PAID | 支付操作 | 成功 |
| UT-ORD-006 | 状态转换 UNPAID → CANCELLED | 取消操作 | 成功 |
| UT-ORD-007 | 非法状态转换 PAID → UNPAID | 操作 | 抛异常 |
| UT-ORD-008 | 非法状态转换 CANCELLED → PAID | 操作 | 抛异常 |
| UT-ORD-009 | 折扣金额计算 | 原价 100, 折扣 10% | discount=10, total=90 |
| UT-ORD-010 | 优惠券抵扣计算 | 满 100 减 20 | discount=20, total=80 |

#### 4.1.3 InventoryServiceTest

| 编号 | 用例 | 输入 | 期望输出 |
|------|------|------|---------|
| UT-INV-001 | 入库增加库存 | 当前 50, 入 30 | after=80 |
| UT-INV-002 | 出库减少库存 | 当前 50, 出 20 | after=30 |
| UT-INV-003 | 出库超出库存 | 当前 50, 出 60 | 抛异常 |
| UT-INV-004 | 安全库存预警 | stock=10, safeStock=20 | 预警列表包含 |
| UT-INV-005 | 库存为零出库 | stock=0, 出 1 | 抛异常 |
| UT-INV-006 | 负数入库 | 入 -10 | 抛异常 |
| UT-INV-007 | 盘点调整 | 当前 50, 调为 30 | after=30, type=ADJUST |
| UT-INV-008 | 损耗记录 | 损耗 5 | type=LOSS, after 减 5 |

#### 4.1.4 DiscountRuleServiceTest

| 编号 | 用例 | 输入 | 期望输出 |
|------|------|------|---------|
| UT-DISC-001 | 满减规则匹配 | 金额 150, 满 100 减 10 | discount=10 |
| UT-DISC-002 | 满减规则不匹配 | 金额 80, 满 100 减 10 | discount=0 |
| UT-DISC-003 | 折扣规则计算 | 金额 100, 8 折 | discount=20 |
| UT-DISC-004 | 多规则优先级 | 两条规则 | 取最优 |
| UT-DISC-005 | 规则为空 | 无匹配规则 | discount=0 |

#### 4.1.5 CustomerMemberServiceTest

| 编号 | 用例 | 输入 | 期望输出 |
|------|------|------|---------|
| UT-MEM-001 | 默认等级 | 新注册 | REGULAR |
| UT-MEM-002 | 自动升级 REGULAR → VIP | 累计消费 1000 | VIP |
| UT-MEM-003 | 自动升级 VIP → SVIP | 累计消费 5000 | SVIP |
| UT-MEM-004 | VIP 折扣率 | VIP 会员 | 95% |
| UT-MEM-005 | SVIP 折扣率 | SVIP 会员 | 90% |
| UT-MEM-006 | 积分倍率 VIP | 消费 100 | 积分 = 100 * 150% |
| UT-MEM-007 | 积分倍率 SVIP | 消费 100 | 积分 = 100 * 200% |

---

### 4.2 集成测试 (Service + Mapper + MySQL)

> 工具: @SpringBootTest + H2 内存数据库 或 Testcontainers
> 路径: `src/test/java/com/xunye/admin/integration/`
> 每个测试用 @Transactional + @Rollback 保证隔离

#### 4.2.1 商品管理集成测试 - ProductIntegrationTest

| 编号 | 用例 | 步骤 | 断言 |
|------|------|------|------|
| IT-PROD-001 | 创建商品并查询 | 创建 → 查询 | 字段一致 |
| IT-PROD-002 | 修改商品上下架 | ON_SALE → OFF_SALE | status 变更 |
| IT-PROD-003 | 逻辑删除商品 | 删除 → 查询列表 | 不在列表中 |
| IT-PROD-004 | 按分类筛选 | 2 个分类各创建 1 个 | 筛选结果正确 |
| IT-PROD-005 | 按关键词搜索 | name 含关键词 | 命中 |
| IT-PROD-006 | 库存扣减 | 创建后扣库存 | stock 减少 |
| IT-PROD-007 | 库存回滚 | 取消订单后 | stock 恢复 |

#### 4.2.2 订单全流程集成测试 - OrderIntegrationTest

| 编号 | 用例 | 步骤 | 断言 |
|------|------|------|------|
| IT-ORD-001 | POS 创建订单 → 支付 | 创建 + 支付 | order_info.status=PAID, payment_order.status=SUCCESS |
| IT-ORD-002 | 创建订单扣库存 | 创建含 2 商品 | product.stock 各减少 |
| IT-ORD-003 | 取消订单回滚库存 | 创建 → 取消 | product.stock 恢复 |
| IT-ORD-004 | 订单项完整性 | 创建含 3 项 | order_item 有 3 条记录 |
| IT-ORD-005 | 桌台状态联动 | 创建订单 | bar_table.status=USING |
| IT-ORD-006 | 完成订单 | 支付 → 制作 → 完成 | serve_status=FINISHED |
| IT-ORD-007 | 小程序下单 | source=CUSTOMER_MINI | source 字段正确 |
| IT-ORD-008 | 订单金额含折扣 | VIP 会员下单 | discount_amount > 0 |
| IT-ORD-009 | 优惠券下单 | 使用优惠券 | coupon_id 有值, 金额抵扣 |
| IT-ORD-010 | 重复支付 | PAID 状态再支付 | 抛异常 |

#### 4.2.3 顾客会员集成测试 - CustomerMemberIntegrationTest

| 编号 | 用例 | 步骤 | 断言 |
|------|------|------|------|
| IT-CUST-001 | 注册 → 下单 → 累积积分 | 注册 + 下单 100 元 | points 增加 |
| IT-CUST-002 | 积分累积自动升级 | 累计消费 1000 | member_level=VIP |
| IT-CUST-003 | 优惠券发放 | 发放模板 | customer_coupon 有记录 |
| IT-CUST-004 | 优惠券使用 | 下单使用 | used=1, used_at 非空 |
| IT-CUST-005 | 优惠券过期 | 使用过期券 | 拒绝 |
| IT-CUST-006 | 积分兑换 | 用积分换券 | points 减少 + 券发放 |
| IT-CUST-007 | 微信登录 | openid 登录 | 返回顾客信息 |
| IT-CUST-008 | 手机号登录 | 验证码登录 | 返回 token |

#### 4.2.4 库存管理集成测试 - InventoryIntegrationTest

| 编号 | 用例 | 步骤 | 断言 |
|------|------|------|------|
| IT-INV-001 | 入库 | 初始 50, 入库 30 | stock=80, record type=IN |
| IT-INV-002 | 出库 | 初始 50, 出库 20 | stock=30, record type=OUT |
| IT-INV-003 | 损耗 | 损耗 5 | stock 减少, record type=LOSS |
| IT-INV-004 | 盘点调整 | 调整为指定值 | stock 更新, record type=ADJUST |
| IT-INV-005 | 流水记录完整性 | 多次操作 | record 条数正确 |
| IT-INV-006 | 安全库存预警 | 低于 safe_stock | 返回预警列表 |

#### 4.2.5 桌台管理集成测试 - TableIntegrationTest

| 编号 | 用例 | 步骤 | 断言 |
|------|------|------|------|
| IT-TBL-001 | 创建区域 + 桌台 | 创建 | 关联正确 |
| IT-TBL-002 | 桌台状态流转 | EMPTY → USING → CLEANING → EMPTY | 各状态正确 |
| IT-TBL-003 | 清台 | USING → EMPTY | 状态更新 |
| IT-TBL-004 | 停用桌台 | 设为 DISABLED | 不可下单 |
| IT-TBL-005 | 删除使用中桌台 | USING 状态删除 | 拒绝 |

---

### 4.3 接口测试 (Controller MockMvc)

> 工具: @WebMvcTest + MockMvc + @MockBean
> 路径: `src/test/java/com/xunye/admin/controller/`

#### 4.3.1 认证模块 - AuthControllerApiTest

**端点: `/api/admin/auth`**

| 编号 | 用例 | 请求 | 断言 |
|------|------|------|------|
| API-AUTH-001 | 正确账密登录 | POST /login {admin, admin123} | 200, data.token 非空, data.role=BOSS |
| API-AUTH-002 | 错误密码 | POST /login {admin, wrong} | 401 |
| API-AUTH-003 | 不存在的用户 | POST /login {noexist, x} | 401 |
| API-AUTH-004 | 禁用账号登录 | POST /login {disabled_user, disabled123} | 401 |
| API-AUTH-005 | 缺少用户名 | POST /login {password: x} | 400 |
| API-AUTH-006 | 缺少密码 | POST /login {username: x} | 400 |
| API-AUTH-007 | 无 Token 访问 | GET /api/admin/products | 401 |
| API-AUTH-008 | 伪造 Token | Header: Bearer fake.token.here | 401 |
| API-AUTH-009 | STAFF 访问 BOSS 接口 | GET /api/admin/employees (staff token) | 403 |
| API-AUTH-010 | MANAGER 访问 BOSS 接口 | GET /api/admin/settings (manager token) | 403 |
| API-AUTH-011 | BOSS 访问所有接口 | GET /api/admin/employees (boss token) | 200 |

#### 4.3.2 商品管理 - ProductControllerApiTest

**端点: `/api/admin/products`**

| 编号 | 用例 | 请求 | 断言 |
|------|------|------|------|
| API-PROD-001 | 创建商品 | POST (正常数据) | 200, 返回 ID |
| API-PROD-002 | 创建商品 - 名称为空 | POST {name: ""} | 400 |
| API-PROD-003 | 创建商品 - 价格为负 | POST {price: -1} | 400 |
| API-PROD-004 | 创建商品 - 价格为零 | POST {price: 0} | 200 (允许免费) 或 400 |
| API-PROD-005 | 创建商品 - 分类不存在 | POST {categoryId: 9999} | 4xx |
| API-PROD-006 | 分页查询 | GET ?page=1&size=10 | 200, records 长度 ≤ 10 |
| API-PROD-007 | 按分类筛选 | GET ?categoryId=1 | 返回均为该分类 |
| API-PROD-008 | 按关键词搜索 | GET ?keyword=百威 | 包含百威商品 |
| API-PROD-009 | 修改商品 | PUT /{id} | 200, 字段更新 |
| API-PROD-010 | 修改状态 (下架) | PATCH /{id}/status | status=OFF_SALE |
| API-PROD-011 | 逻辑删除 | DELETE /{id} | 200, 查询不到 |
| API-PROD-012 | 查询已删除商品 | GET /{id} | 404 或空 |
| API-PROD-013 | 上传图片 | POST /upload (multipart) | 200, 返回 URL |

#### 4.3.3 订单管理 - OrderControllerApiTest

**端点: `/api/admin/orders`**

| 编号 | 用例 | 请求 | 断言 |
|------|------|------|------|
| API-ORD-001 | 创建订单 (正常) | POST {tableId, items[]} | 200, 返回 orderId |
| API-ORD-002 | 创建订单 - 无商品 | POST {items: []} | 400 |
| API-ORD-003 | 创建订单 - 桌台不存在 | POST {tableId: 9999} | 4xx |
| API-ORD-004 | 创建订单 - 库存不足 | POST (超出库存商品) | 4xx |
| API-ORD-005 | 查询订单列表 | GET ?page=1&size=10 | 200, 分页数据 |
| API-ORD-006 | 按状态筛选 | GET ?status=UNPAID | 全部 UNPAID |
| API-ORD-007 | 查询订单详情 | GET /{id} | 200, 含 orderItems |
| API-ORD-008 | 支付订单 (现金) | PATCH /{id}/pay {method: CASH} | 200 |
| API-ORD-009 | 支付订单 (微信) | PATCH /{id}/pay {method: WECHAT} | 200 (mock) |
| API-ORD-010 | 重复支付 | 支付后再次 PATCH /pay | 4xx |
| API-ORD-011 | 支付已取消订单 | CANCELLED 状态 PATCH /pay | 4xx |
| API-ORD-012 | 取消订单 | PATCH /{id}/cancel | 200 |
| API-ORD-013 | 开始制作 | PATCH /{id}/making | 200, serve_status=MAKING |
| API-ORD-014 | 完成订单 | PATCH /{id}/finish | 200, serve_status=FINISHED |
| API-ORD-015 | 最近订单 | GET /recent | 200, 按时间排序 |

#### 4.3.4 员工管理 - StaffControllerApiTest

**端点: `/api/admin/staff`**

| 编号 | 用例 | 请求 | 断言 |
|------|------|------|------|
| API-STAFF-001 | 创建员工 | POST {正常数据} | 200 |
| API-STAFF-002 | 创建重复用户名 | POST {已存在 username} | 4xx |
| API-STAFF-003 | 创建 - 用户名为空 | POST {username: ""} | 400 |
| API-STAFF-004 | 查询列表 | GET ?page=1&size=10 | 200 |
| API-STAFF-005 | 按角色筛选 | GET ?role=STAFF | 仅 STAFF |
| API-STAFF-006 | 修改员工 | PUT /{id} | 200 |
| API-STAFF-007 | 启用/禁用 | PATCH /{id}/status | status 变更 |
| API-STAFF-008 | 重置密码 | PATCH /{id}/password | 200 |
| API-STAFF-009 | 删除员工 | DELETE /{id} | 200 |
| API-STAFF-010 | STAFF 角色调用 | 以上接口 (staff token) | 403 |

#### 4.3.5 桌台管理 - BarTableControllerApiTest

**端点: `/api/admin/tables` + `/api/admin/table-areas`**

| 编号 | 用例 | 请求 | 断言 |
|------|------|------|------|
| API-TBL-001 | 创建区域 | POST /table-areas | 200 |
| API-TBL-002 | 区域列表 | GET /table-areas | 200 |
| API-TBL-003 | 创建桌台 | POST /tables | 200 |
| API-TBL-004 | 桌台列表 (含区域筛选) | GET /tables?areaId=1 | 该区域桌台 |
| API-TBL-005 | 修改桌台 | PUT /tables/{id} | 200 |
| API-TBL-006 | 修改桌台状态 | PATCH /tables/{id}/status | status 变更 |
| API-TBL-007 | 清台 | PATCH /tables/{id}/clear | status=EMPTY |

#### 4.3.6 库存管理 - InventoryControllerApiTest

**端点: `/api/admin/inventory`**

| 编号 | 用例 | 请求 | 断言 |
|------|------|------|------|
| API-INV-001 | 入库 | POST /adjust {type: IN, qty: 50} | 200, stock 增加 |
| API-INV-002 | 出库 | POST /adjust {type: OUT, qty: 10} | 200, stock 减少 |
| API-INV-003 | 超量出库 | POST /adjust {type: OUT, qty: 9999} | 4xx |
| API-INV-004 | 损耗 | POST /adjust {type: LOSS, qty: 5} | 200 |
| API-INV-005 | 盘点调整 | POST /adjust {type: ADJUST, qty: 100} | 200, stock=100 |
| API-INV-006 | 流水查询 | GET /records?productId=1 | 返回该商品流水 |
| API-INV-007 | 库存预警 | GET /warnings | stock < safe_stock 的商品 |

#### 4.3.7 会员营销模块

**优惠券模板 - CouponTemplateControllerApiTest**

| 编号 | 用例 | 请求 | 断言 |
|------|------|------|------|
| API-COUP-001 | 创建优惠券模板 | POST | 200 |
| API-COUP-002 | 列表查询 | GET | 200 |
| API-COUP-003 | 修改模板 | PUT /{id} | 200 |
| API-COUP-004 | 删除模板 | DELETE /{id} | 200 |

**活动管理 - MemberActivityControllerApiTest**

| 编号 | 用例 | 请求 | 断言 |
|------|------|------|------|
| API-ACT-001 | 创建活动 | POST | 200 |
| API-ACT-002 | 活动列表 | GET | 200 |
| API-ACT-003 | 修改活动 | PUT /{id} | 200 |
| API-ACT-004 | 发布活动 (草稿→进行中) | PATCH /{id}/status | status=1 |

**会员等级配置 - MemberLevelConfigControllerApiTest**

| 编号 | 用例 | 请求 | 断言 |
|------|------|------|------|
| API-MLV-001 | 获取等级列表 | GET | 200, 含 REGULAR/VIP/SVIP |
| API-MLV-002 | 修改等级配置 | PUT /{id} | 200 |
| API-MLV-003 | 修改折扣率 | PUT {discount: 85} | 更新成功 |

**折扣规则 - DiscountRuleControllerApiTest**

| 编号 | 用例 | 请求 | 断言 |
|------|------|------|------|
| API-DR-001 | 创建折扣规则 | POST | 200 |
| API-DR-002 | 规则列表 | GET | 200 |
| API-DR-003 | 修改规则 | PUT /{id} | 200 |
| API-DR-004 | 删除规则 | DELETE /{id} | 200 |

#### 4.3.8 顾客端 API - CustomerControllerApiTest

**端点: `/api/customer`**

| 编号 | 用例 | 请求 | 断言 |
|------|------|------|------|
| API-C-001 | 获取店铺信息 | GET /shop/info | 200 |
| API-C-002 | 获取桌台列表 | GET /tables | 200 |
| API-C-003 | 获取桌台详情 | GET /tables/{tableCode} | 200 |
| API-C-004 | 获取分类 | GET /categories | 200 |
| API-C-005 | 获取商品列表 | GET /products | 200 |
| API-C-006 | 按分类筛选商品 | GET /products?categoryId=1 | 该分类商品 |
| API-C-007 | 搜索商品 | GET /products?keyword=百威 | 包含百威 |
| API-C-008 | 商品详情 | GET /products/{id} | 200 |
| API-C-009 | 微信登录 | POST /member/wx-login | 200, token |
| API-C-010 | 注册会员 | POST /member/register | 200 |
| API-C-011 | 手机号+密码登录 | POST /member/phone-login-by-password | 200 |
| API-C-012 | 错误密码登录 | POST /member/phone-login-by-password | 401 |
| API-C-013 | 创建订单 | POST /orders | 200 |
| API-C-014 | 订单列表 | GET /orders | 200 |
| API-C-015 | 订单详情 | GET /orders/{orderNo} | 200 |
| API-C-016 | 订单日期标记 | GET /orders/date-markers | 200 |
| API-C-017 | 消息列表 | GET /messages?phone=xxx | 200 |
| API-C-018 | 会员信息 | GET /member/info?phone=xxx | 200 |
| API-C-019 | 修改资料 | PUT /member/profile | 200 |
| API-C-020 | 上传头像 | POST /member/avatar (multipart) | 200 |
| API-C-021 | 优惠券列表 | GET /coupons?phone=xxx | 200 |
| API-C-022 | 积分兑换 | POST /points/rewards/{id}/exchange | 200 |
| API-C-023 | 积分记录 | GET /points/records?phone=xxx | 200 |
| API-C-024 | 会员等级列表 | GET /member/levels | 200 |
| API-C-025 | 活动列表 | GET /activities | 200 |

#### 4.3.9 Dashboard - DashboardControllerApiTest

**端点: `/api/admin/dashboard`**

| 编号 | 用例 | 请求 | 断言 |
|------|------|------|------|
| API-DASH-001 | 获取总览 | GET /summary | 200, 含 todayOrders 等 |
| API-DASH-002 | 销售趋势 | GET /sales-trend?days=7 | 200, 7 条数据 |
| API-DASH-003 | 热销商品 | GET /hot-products | 200, 按销量排序 |

#### 4.3.10 系统配置 - SystemConfigControllerApiTest

**端点: `/api/admin/config`**

| 编号 | 用例 | 请求 | 断言 |
|------|------|------|------|
| API-CFG-001 | 获取配置 | GET | 200 |
| API-CFG-002 | 更新配置 | PUT | 200 |
| API-CFG-003 | 小程序配置 | GET /miniapp | 200 |

#### 4.3.11 品牌管理 - ProductBrandControllerApiTest

| 编号 | 用例 | 请求 | 断言 |
|------|------|------|------|
| API-BRAND-001 | 创建品牌 | POST | 200 |
| API-BRAND-002 | 品牌列表 | GET | 200 |
| API-BRAND-003 | 修改品牌 | PUT /{id} | 200 |
| API-BRAND-004 | 删除品牌 | DELETE /{id} | 200 |
| API-BRAND-005 | 重复品牌名 | POST {已存在 name} | 4xx |

#### 4.3.12 分类管理 - ProductCategoryControllerApiTest

| 编号 | 用例 | 请求 | 断言 |
|------|------|------|------|
| API-CAT-001 | 创建分类 | POST | 200 |
| API-CAT-002 | 分类列表 | GET | 200 |
| API-CAT-003 | 修改分类 | PUT /{id} | 200 |
| API-CAT-004 | 删除分类 | DELETE /{id} | 200 |
| API-CAT-005 | 分类下有商品时删除 | DELETE /{id} | 拒绝或提示 |

#### 4.3.13 区域管理 - TableAreaControllerApiTest

| 编号 | 用例 | 请求 | 断言 |
|------|------|------|------|
| API-AREA-001 | 创建区域 | POST | 200 |
| API-AREA-002 | 区域列表 | GET | 200 |
| API-AREA-003 | 修改区域 | PUT /{id} | 200 |
| API-AREA-004 | 删除区域 (含桌台) | DELETE /{id} | 拒绝或提示 |

---

## 五、前端测试计划

### 5.1 工具选型

| 工具 | 用途 |
|------|------|
| Vitest | 测试运行器 (与 Vite 天然集成) |
| @testing-library/react | 组件测试 |
| MSW (Mock Service Worker) | API 请求拦截 |
| jsdom | DOM 环境 |

### 5.2 配置文件

```
xunye-web/
├── vitest.config.ts
├── src/
│   ├── __tests__/
│   │   ├── api/           # API 层测试
│   │   ├── router/        # 路由权限测试
│   │   ├── components/    # 组件测试
│   │   └── mocks/         # MSW handlers
│   └── ...
```

### 5.3 API 层测试 - request.ts

| 编号 | 用例 | 输入 | 断言 |
|------|------|------|------|
| FE-API-001 | Token 注入 | localStorage 有 token | 请求头含 Authorization: Bearer xxx |
| FE-API-002 | 无 Token | localStorage 无 token | 请求头无 Authorization |
| FE-API-003 | 401 自动跳转 | 响应 code=401 | 跳转 /login, 清除 localStorage |
| FE-API-004 | 403 提示 | 响应 code=403 | 提示"无权限访问" |
| FE-API-005 | 500 提示 | 响应 status=500 | 提示"服务器错误" |
| FE-API-006 | 超时提示 | 请求超时 | 提示"请求超时" |
| FE-API-007 | 网络错误 | 无网络 | 提示"网络连接失败" |
| FE-API-008 | GET 请求去重 | 相同 URL 并发 2 次 | 只发 1 次请求 |
| FE-API-009 | 参数清理 | params 含 null/undefined/"" | 被过滤掉 |

### 5.4 路由权限测试 - router.test.tsx

| 编号 | 用例 | 角色 | 访问路径 | 断言 |
|------|------|------|---------|------|
| FE-RT-001 | 未登录访问管理页 | - | /dashboard | 跳转 /login |
| FE-RT-002 | 已登录访问登录页 | BOSS | /login | 跳转 /dashboard |
| FE-RT-003 | BOSS 默认首页 | BOSS | / | 跳转 /dashboard |
| FE-RT-004 | STAFF 默认首页 | STAFF | / | 跳转 /kitchen |
| FE-RT-005 | STAFF 访问 dashboard | STAFF | /dashboard | 跳转 /kitchen |
| FE-RT-006 | STAFF 访问 employees | STAFF | /employees | 跳转 /kitchen |
| FE-RT-007 | STAFF 访问 kitchen | STAFF | /kitchen | 正常渲染 |
| FE-RT-008 | STAFF 访问 orders | STAFF | /orders | 正常渲染 |
| FE-RT-009 | STAFF 访问 pos | STAFF | /pos | 正常渲染 |
| FE-RT-010 | MANAGER 访问 dashboard | MANAGER | /dashboard | 正常渲染 |
| FE-RT-011 | MANAGER 访问 employees | MANAGER | /employees | 跳转 /dashboard |
| FE-RT-012 | MANAGER 访问 settings | MANAGER | /settings | 跳转 /dashboard |
| FE-RT-013 | BOSS 访问所有页面 | BOSS | /employees, /settings | 正常渲染 |
| FE-RT-014 | 404 路由 | 任意 | /nonexist | 跳转首页 |

### 5.5 组件测试

| 编号 | 组件 | 用例 | 断言 |
|------|------|------|------|
| FE-CMP-001 | Login | 正确提交表单 | 调用登录 API |
| FE-CMP-002 | Login | 用户名为空 | 显示验证错误 |
| FE-CMP-003 | Login | 密码为空 | 显示验证错误 |
| FE-CMP-004 | Login | 登录失败 | 显示错误信息 |
| FE-CMP-005 | Sidebar | BOSS 显示所有菜单 | 含员工管理/设置 |
| FE-CMP-006 | Sidebar | STAFF 显示受限菜单 | 仅含厨房/订单/POS/桌台 |
| FE-CMP-007 | Sidebar | MANAGER 显示管理菜单 | 不含员工管理/设置 |
| FE-CMP-008 | Pagination | 切换页码 | 调用 onPageChange |
| FE-CMP-009 | Loading | 显示加载状态 | 有 loading 元素 |
| FE-CMP-010 | ErrorState | 显示错误 | 有错误信息文本 |

---

## 六、小程序测试计划

### 6.1 工具选型

| 工具 | 用途 |
|------|------|
| Vitest | 测试运行器 |
| @vue/test-utils | Vue 组件测试 |

### 6.2 Store 测试 - token.ts

| 编号 | 用例 | 断言 |
|------|------|------|
| MP-TOK-001 | 保存 Token | wx.setStorageSync 被调用 |
| MP-TOK-002 | 获取 Token | 返回存储的值 |
| MP-TOK-003 | 清除 Token | wx.removeStorageSync 被调用 |
| MP-TOK-004 | Token 有效性检查 | 有 token 返回 true |

### 6.3 Store 测试 - customerProfile.ts

| 编号 | 用例 | 断言 |
|------|------|------|
| MP-PROF-001 | 保存顾客信息 | 数据持久化 |
| MP-PROF-002 | 获取顾客信息 | 返回完整对象 |
| MP-PROF-003 | 清除信息 | 数据清空 |
| MP-PROF-004 | 更新部分字段 | 仅更新指定字段 |

### 6.4 HTTP 拦截器测试 - interceptor.ts

| 编号 | 用例 | 断言 |
|------|------|------|
| MP-HTTP-001 | 请求注入 Token | header 含 Authorization |
| MP-HTTP-002 | 无 Token 请求 | header 无 Authorization |
| MP-HTTP-003 | 401 响应处理 | 跳转登录页 |
| MP-HTTP-004 | 非 200 响应 | 错误提示 |

### 6.5 API 层测试

| 编号 | 模块 | 用例 | 断言 |
|------|------|------|------|
| MP-API-001 | login.ts | 微信登录 | 调用正确端点 |
| MP-API-002 | customer.ts | 获取商品列表 | 参数正确 |
| MP-API-003 | customer.ts | 创建订单 | 请求体正确 |
| MP-API-004 | customer.ts | 获取订单列表 | 分页参数正确 |
| MP-API-005 | membership.ts | 获取会员信息 | 端点正确 |

---

## 七、E2E / 手动测试计划

### 7.1 核心业务流程

#### E2E-001: POS 完整下单流程

```
前置条件: BOSS 账号登录, 有可用商品和空闲桌台

步骤:
1. 登录管理后台 (admin / admin123)
2. 进入 POS 收银页面
3. 选择桌台 A1
4. 添加商品: 百威啤酒 x2, 长岛冰茶 x1
5. 确认下单
6. 选择支付方式: 现金
7. 确认支付
8. 进入厨房页面, 查看订单
9. 点击"开始制作"
10. 点击"完成"

验证点:
- 订单状态: UNPAID → PAID
- 履约状态: PENDING → MAKING → FINISHED
- 桌台状态: EMPTY → USING (下单后)
- 库存: 百威 -2, 长岛冰茶 -1
- 支付单: status=SUCCESS
```

#### E2E-002: 小程序扫码点单流程

```
前置条件: 顾客已注册, 桌台 A1 空闲

步骤:
1. 扫描桌台 A1 二维码
2. 进入菜单页
3. 浏览分类: 啤酒, 鸡尾酒
4. 搜索商品: "百威"
5. 添加百威啤酒 x3 到购物车
6. 确认下单
7. 查看订单列表
8. 查看订单详情

验证点:
- 订单 source = CUSTOMER_MINI
- 订单 tableId = A1
- 商品列表正确
- 订单列表有新订单
```

#### E2E-003: 库存管理全流程

```
步骤:
1. 登录 (BOSS)
2. 查看库存预警 (百威 stock=86, safe_stock=20, 不应预警)
3. 入库: 百威啤酒 +50
4. 验证库存: 86 + 50 = 136
5. 出库: 百威啤酒 -10
6. 验证库存: 136 - 10 = 126
7. 记录损耗: 百威啤酒 -3
8. 验证库存: 126 - 3 = 123
9. 盘点调整: 设为 100
10. 验证库存: 100
11. 查看流水记录: 共 4 条

验证点:
- 每次操作后库存正确
- 流水记录类型正确 (IN/OUT/LOSS/ADJUST)
- 流水记录前后库存值正确
```

#### E2E-004: 会员生命周期

```
步骤:
1. 小程序注册新会员 (手机号)
2. 验证默认等级: REGULAR
3. 下单消费 500 元
4. 验证积分增加
5. 继续消费至累计 1000 元
6. 验证等级自动升级: VIP
7. 验证 VIP 折扣生效 (95%)
8. 继续消费至累计 5000 元
9. 验证等级自动升级: SVIP
10. 验证 SVIP 折扣生效 (90%)
11. 使用积分兑换优惠券
12. 验证积分扣减 + 优惠券发放

验证点:
- 等级升降级正确
- 折扣率正确应用
- 积分倍率正确 (REGULAR 1x, VIP 1.5x, SVIP 2x)
- 积分兑换正常
```

#### E2E-005: 优惠券完整流程

```
步骤:
1. 管理后台创建优惠券模板: "满100减20"
2. 发放优惠券给顾客 (手机号)
3. 小程序查看优惠券: 有 1 张
4. 下单消费 150 元, 选择使用优惠券
5. 验证实付: 150 - 20 = 130 元
6. 验证优惠券状态: 已使用

验证点:
- 优惠券金额抵扣正确
- 低于最低消费不可用
- 使用后标记为已用
```

#### E2E-006: 员工权限验证

```
步骤:
1. BOSS 登录 → 可见所有菜单 → 访问 /employees 成功
2. MANAGER 登录 → 不可见"员工管理"和"系统设置" → 访问 /employees 被拒
3. STAFF 登录 → 仅可见"厨房/订单/POS/桌台" → 访问 /dashboard 被拒
4. 用 STAFF token 调用 POST /api/admin/staff → 403
5. 禁用账号登录 → 401

验证点:
- 前端菜单隐藏正确
- 前端路由拦截正确
- 后端 API 权限拦截正确
```

---

## 八、安全测试计划

### 8.1 认证安全

| 编号 | 测试项 | 方法 | 风险 | 断言 |
|------|--------|------|------|------|
| SEC-001 | Token 伪造 | 修改 JWT payload | 高 | 401 拒绝 |
| SEC-002 | Token 篡改 | 修改签名部分 | 高 | 401 拒绝 |
| SEC-003 | 过期 Token | 使用过期 Token | 高 | 401 拒绝 |
| SEC-004 | 空 Token | 不传 Authorization | 中 | 401 拒绝 |
| SEC-005 | 密码存储安全 | 查看数据库 password 字段 | 高 | BCrypt 哈希, 不可逆 |

### 8.2 注入安全

| 编号 | 测试项 | 方法 | 风险 | 断言 |
|------|--------|------|------|------|
| SEC-006 | SQL 注入 - 查询 | keyword=' OR 1=1 -- | 高 | 返回空或正常结果 |
| SEC-007 | SQL 注入 - 登录 | username=' OR '1'='1 | 高 | 登录失败 |
| SEC-008 | XSS - 商品名 | name=<script>alert(1)</script> | 中 | 存储原样, 渲染转义 |
| SEC-009 | XSS - 订单备注 | remark=<img onerror=alert(1)> | 中 | 渲染转义 |
| SEC-010 | XSS - 昵称 | nickname=<script> | 中 | 渲染转义 |

### 8.3 越权安全

| 编号 | 测试项 | 方法 | 风险 | 断言 |
|------|--------|------|------|------|
| SEC-011 | 水平越权 - 订单 | 用 A 的 token 查 B 的订单 | 高 | 无数据或 403 |
| SEC-012 | 水平越权 - 顾客 | 用 A 的 token 改 B 的资料 | 高 | 拒绝 |
| SEC-013 | 垂直越权 - STAFF | STAFF token 操作员工管理 | 高 | 403 |
| SEC-014 | 垂直越权 - MANAGER | MANAGER token 操作系统设置 | 高 | 403 |

### 8.4 文件上传安全

| 编号 | 测试项 | 方法 | 风险 | 断言 |
|------|--------|------|------|------|
| SEC-015 | 上传可执行文件 | 上传 .jsp/.php 文件 | 高 | 拒绝或存储为静态 |
| SEC-016 | 超大文件 | 上传 > 10MB 文件 | 中 | 拒绝 |
| SEC-017 | 空文件 | 上传 0 字节 | 低 | 拒绝 |
| SEC-018 | 文件类型限制 | 上传 .exe | 中 | 仅允许图片类型 |

### 8.5 接口安全

| 编号 | 测试项 | 方法 | 风险 | 断言 |
|------|--------|------|------|------|
| SEC-019 | Rate Limiting | 快速连续请求 100 次 | 中 | 触发限流 |
| SEC-020 | CORS 配置 | 跨域请求 | 中 | 仅允许配置的域名 |
| SEC-021 | 审计日志 | 敏感操作后查 audit_log | 低 | 有对应记录 |

---

## 九、性能测试计划

### 9.1 工具选型

| 工具 | 用途 |
|------|------|
| JMeter / k6 | HTTP 压测 |
| MySQL Slow Query Log | 慢查询监控 |
| HikariCP Metrics | 连接池监控 |

### 9.2 压测场景

| 编号 | 场景 | 并发数 | 持续时间 | 目标指标 |
|------|------|--------|---------|---------|
| PERF-001 | 商品列表查询 | 50 | 60s | P99 < 200ms, 错误率 < 0.1% |
| PERF-002 | 创建订单 | 20 | 60s | P99 < 500ms, 错误率 < 0.1% |
| PERF-003 | Dashboard 统计 | 10 | 60s | P99 < 1s |
| PERF-004 | 顾客端商品浏览 | 100 | 60s | P99 < 300ms |
| PERF-005 | 登录接口 | 30 | 30s | P99 < 500ms |
| PERF-006 | 文件上传 (2MB) | 10 | 30s | P99 < 2s |

### 9.3 关注指标

| 指标 | 说明 |
|------|------|
| P50 / P90 / P99 延迟 | 响应时间分布 |
| TPS (每秒事务数) | 吞吐量 |
| 错误率 | 非 200 响应比例 |
| CPU / 内存使用 | 服务器资源 |
| 数据库连接池 | HikariCP active / idle / pending |
| 慢查询 | > 500ms 的 SQL |

---

## 十、测试数据管理

### 10.1 基础数据 (来自 init.sql)

| 数据 | 说明 |
|------|------|
| 4 个员工 | admin(BOSS), manager(MANAGER), staff(STAFF), disabled_user(STAFF-禁用) |
| 默认密码 | admin123 / manager123 / staff123 / disabled123 (均为 BCrypt) |
| 6 个分类 | 啤酒, 鸡尾酒, 威士忌, 利口酒, 小食, 辅料 |
| 3 个区域 | 大厅, 包厢, 露台 |
| 8 张桌台 | A1-A4, VIP1-V2, T1-T2 |
| 5 个商品 | 百威啤酒, 长岛冰茶, 野格, 青柠, 薯条 |
| 3 个会员等级 | REGULAR, VIP(满1000), SVIP(满5000) |
| 3 个活动 | 周二特惠, 新客专享, 积分翻倍 |

### 10.2 测试辅助数据 (各测试自行创建)

| 数据 | 场景 |
|------|------|
| 测试顾客 | 手机号 13800000001, REGULAR 等级 |
| VIP 顾客 | 手机号 13800000002, 累计消费 1500 |
| 测试订单 | 用于订单查询/支付测试 |
| 测试优惠券 | 用于发放/使用测试 |

---

## 十一、实施路线图

### Phase 1: 核心保障 (P0, 预计 3-5 天)

```
Day 1:
  ├── [后端] 创建测试基础设施 (测试配置, H2, Fixture)
  ├── [后端] AuthServiceTest (8 个用例)
  └── [后端] AuthControllerApiTest (11 个用例)

Day 2:
  ├── [后端] OrderServiceTest (10 个用例)
  └── [后端] OrderControllerApiTest (15 个用例)

Day 3:
  ├── [后端] InventoryServiceTest (8 个用例)
  ├── [后端] InventoryControllerApiTest (7 个用例)
  └── [后端] ProductControllerApiTest (13 个用例)

Day 4:
  ├── [后端] OrderIntegrationTest (10 个用例)
  ├── [后端] ProductIntegrationTest (7 个用例)
  └── [前端] Vitest 配置 + request.ts 测试 (9 个用例)

Day 5:
  ├── [前端] 路由权限测试 (14 个用例)
  └── [后端] 运行全量测试, 修复失败
```

**Phase 1 产出: 113 个用例, 覆盖核心交易链路**

### Phase 2: 业务覆盖 (P1, 预计 3-5 天)

```
Day 6-7:
  ├── [后端] StaffControllerApiTest (10 个用例)
  ├── [后端] BarTableControllerApiTest (7 个用例)
  ├── [后端] CustomerControllerApiTest (25 个用例)
  └── [后端] CustomerMemberIntegrationTest (8 个用例)

Day 8-9:
  ├── [后端] InventoryIntegrationTest (6 个用例)
  ├── [后端] TableIntegrationTest (5 个用例)
  ├── [后端] DiscountRuleServiceTest (5 个用例)
  ├── [后端] CustomerMemberServiceTest (7 个用例)
  └── [后端] 营销模块 API 测试 (16 个用例)

Day 10:
  ├── [前端] 组件测试 (10 个用例)
  ├── [小程序] Store + API 测试 (13 个用例)
  └── [后端] Dashboard + Config + Brand + Category 测试 (14 个用例)
```

**Phase 2 产出: +126 个用例, 业务覆盖 80%+**

### Phase 3: 质量加固 (P2, 预计 2-3 天)

```
Day 11:
  ├── [安全] 认证安全测试 (5 个用例)
  ├── [安全] 注入安全测试 (5 个用例)
  └── [安全] 越权安全测试 (4 个用例)

Day 12:
  ├── [安全] 文件上传安全 (4 个用例)
  ├── [安全] 接口安全 (3 个用例)
  └── [E2E] 手动执行 6 条核心流程

Day 13:
  ├── [性能] 基线压测 (6 个场景)
  └── [修复] 发现问题修复 + 回归
```

**Phase 3 产出: +27 个安全用例 + 6 条 E2E + 6 个性能场景**

---

## 十二、总览统计

| 类别 | 用例数 | 优先级 |
|------|--------|--------|
| 后端单元测试 | 46 | P0-P1 |
| 后端集成测试 | 36 | P0-P1 |
| 后端接口测试 | 142 | P0-P1 |
| 前端测试 | 33 | P0-P2 |
| 小程序测试 | 13 | P1 |
| 安全测试 | 21 | P2 |
| E2E 手动测试 | 6 条流程 | P1-P2 |
| 性能测试 | 6 个场景 | P2 |
| **合计** | **~303 用例 + 6 E2E + 6 性能** | |

---

## 十三、测试基础设施建议

### 13.1 后端测试配置

```yaml
# application-test.yml
spring:
  datasource:
    url: jdbc:h2:mem:xunye_test;DB_CLOSE_DELAY=-1;MODE=MySQL
    driver-class-name: org.h2.Driver
  data:
    redis:
      host: localhost
      port: 6379  # 或用 EmbeddedRedis
```

### 13.2 CI 门禁规则

| 检查项 | 阈值 | 动作 |
|--------|------|------|
| 单元测试通过率 | 100% | 不通过则阻断 |
| 集成测试通过率 | 100% | 不通过则阻断 |
| 接口测试通过率 | 100% | 不通过则阻断 |
| 后端覆盖率 | ≥ 70% | 警告 |
| 前端测试通过率 | 100% | 不通过则阻断 |

### 13.3 覆盖率目标

| 模块 | 目标 |
|------|------|
| Service 层 | ≥ 80% |
| Controller 层 | ≥ 70% |
| Util 层 | ≥ 90% |
| 前端 API 层 | ≥ 80% |
| 前端路由 | 100% |
