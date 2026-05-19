-- ================================
-- 寻野酒吧管理系统 - 数据库初始化脚本
-- ================================

CREATE DATABASE IF NOT EXISTS xunye_bar DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE xunye_bar;

-- ----------------------------
-- 1. 商品分类表
-- ----------------------------
DROP TABLE IF EXISTS product_category;
CREATE TABLE product_category (
    id         BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    name       VARCHAR(50) NOT NULL COMMENT '分类名称',
    sort       INT         DEFAULT 0 COMMENT '排序',
    status     TINYINT     DEFAULT 1 COMMENT '状态：1启用，0禁用',
    deleted    TINYINT     DEFAULT 0 COMMENT '是否删除：0未删除，1已删除',
    created_at DATETIME    DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品分类表';

-- ----------------------------
-- 2. 酒水商品表
-- ----------------------------
DROP TABLE IF EXISTS product;
CREATE TABLE product (
    id          BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    category_id BIGINT        NOT NULL COMMENT '分类ID',
    name        VARCHAR(100)  NOT NULL COMMENT '商品名称',
    brand       VARCHAR(100)  DEFAULT NULL COMMENT '品牌',
    spec        VARCHAR(50)   DEFAULT NULL COMMENT '规格',
    price       DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '售价',
    cost_price  DECIMAL(10,2) DEFAULT 0.00 COMMENT '成本价',
    stock       INT           DEFAULT 0 COMMENT '当前库存',
    safe_stock  INT           DEFAULT 0 COMMENT '安全库存',
    unit        VARCHAR(20)   DEFAULT NULL COMMENT '单位',
    image_url   VARCHAR(255)  DEFAULT NULL COMMENT '图片地址',
    description VARCHAR(500)  DEFAULT NULL COMMENT '描述',
    status      VARCHAR(20)   DEFAULT 'ON_SALE' COMMENT '状态：ON_SALE上架，OFF_SALE下架',
    deleted     TINYINT       DEFAULT 0 COMMENT '是否删除：0未删除，1已删除',
    created_at  DATETIME      DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at  DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_category_id (category_id),
    KEY idx_status (status),
    KEY idx_status_stock (status, stock),
    CONSTRAINT chk_stock CHECK (stock >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='酒水商品表';

-- ----------------------------
-- 3. 库存流水表
-- ----------------------------
DROP TABLE IF EXISTS inventory_record;
CREATE TABLE inventory_record (
    id              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    product_id      BIGINT       NOT NULL COMMENT '商品ID',
    product_name    VARCHAR(100) NOT NULL COMMENT '商品名称',
    type            VARCHAR(20)  NOT NULL COMMENT '操作类型：IN入库、OUT出库、LOSS损耗、ADJUST盘点调整',
    change_quantity INT          NOT NULL COMMENT '变动数量',
    before_stock    INT          NOT NULL COMMENT '变动前库存',
    after_stock     INT          NOT NULL COMMENT '变动后库存',
    reason          VARCHAR(255) DEFAULT NULL COMMENT '操作原因',
    operator_name   VARCHAR(50)  DEFAULT NULL COMMENT '操作人',
    created_at      DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_product_id (product_id),
    KEY idx_type (type),
    KEY idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='库存流水表';

-- ----------------------------
-- 4. 桌台区域表
-- ----------------------------
DROP TABLE IF EXISTS table_area;
CREATE TABLE table_area (
    id         BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    name       VARCHAR(50) NOT NULL COMMENT '区域名称',
    sort       INT         DEFAULT 0 COMMENT '排序',
    status     TINYINT     DEFAULT 1 COMMENT '状态：1启用，0禁用',
    deleted    TINYINT     DEFAULT 0 COMMENT '是否删除：0未删除，1已删除',
    created_at DATETIME    DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='桌台区域表';

-- ----------------------------
-- 5. 桌台表
-- ----------------------------
DROP TABLE IF EXISTS bar_table;
CREATE TABLE bar_table (
    id         BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    area_id    BIGINT      NOT NULL COMMENT '区域ID',
    name       VARCHAR(50) NOT NULL COMMENT '桌台名称',
    capacity   INT         DEFAULT 1 COMMENT '容纳人数',
    status     VARCHAR(20) DEFAULT 'EMPTY' COMMENT '状态：EMPTY空闲、USING使用中、CLEANING清洁中、DISABLED停用',
    deleted    TINYINT     DEFAULT 0 COMMENT '是否删除：0未删除，1已删除',
    created_at DATETIME    DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_area_id (area_id),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='桌台表';

-- ----------------------------
-- 6. 订单表
-- ----------------------------
DROP TABLE IF EXISTS order_info;
CREATE TABLE order_info (
    id              BIGINT        NOT NULL AUTO_INCREMENT COMMENT '订单ID',
    order_no        VARCHAR(32)   NOT NULL COMMENT '订单编号',
    table_id        BIGINT        NOT NULL COMMENT '桌台ID',
    table_name      VARCHAR(64)   NOT NULL COMMENT '桌台名称',
    customer_id     BIGINT        DEFAULT NULL COMMENT '顾客ID',
    customer_phone  VARCHAR(20)   DEFAULT NULL COMMENT '顾客手机号',
    total_amount    DECIMAL(10,2) NOT NULL COMMENT '订单总金额',
    status          VARCHAR(16)   NOT NULL DEFAULT 'UNPAID' COMMENT '订单状态：UNPAID、PAID、CANCELLED、FINISHED',
    serve_status    VARCHAR(32)   NOT NULL DEFAULT 'PENDING' COMMENT '履约状态：PENDING待处理、MAKING制作中、FINISHED已完成',
    payment_method  VARCHAR(16)   DEFAULT NULL COMMENT '支付方式：WECHAT微信、ALIPAY支付宝、CASH现金',
    source          VARCHAR(32)   NOT NULL DEFAULT 'ADMIN_POS' COMMENT '订单来源：ADMIN_POS吧台点单、CUSTOMER_MINI顾客扫码',
    remark          VARCHAR(512)  DEFAULT NULL COMMENT '订单备注',
    created_at      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    paid_at         DATETIME      DEFAULT NULL COMMENT '支付时间',
    cancelled_at    DATETIME      DEFAULT NULL COMMENT '取消时间',
    deleted         TINYINT       NOT NULL DEFAULT 0 COMMENT '删除标志：0未删除，1已删除',
    PRIMARY KEY (id),
    UNIQUE KEY uk_order_no (order_no),
    KEY idx_table_id (table_id),
    KEY idx_customer_id (customer_id),
    KEY idx_customer_phone (customer_phone),
    KEY idx_status (status),
    KEY idx_created_at (created_at),
    KEY idx_status_paid_at (status, paid_at),
    KEY idx_serve_status (serve_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';

-- ----------------------------
-- 7. 订单项表
-- ----------------------------
DROP TABLE IF EXISTS order_item;
CREATE TABLE order_item (
    id           BIGINT        NOT NULL AUTO_INCREMENT COMMENT '订单项ID',
    order_id     BIGINT        NOT NULL COMMENT '关联订单ID',
    product_id   BIGINT        NOT NULL COMMENT '商品ID',
    product_name VARCHAR(128)  NOT NULL COMMENT '商品名称',
    quantity     INT           NOT NULL COMMENT '商品数量',
    price        DECIMAL(10,2) NOT NULL COMMENT '商品单价',
    amount       DECIMAL(10,2) NOT NULL COMMENT '商品金额',
    created_at   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_order_id (order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单项表';

-- ----------------------------
-- 8. 支付单表
-- ----------------------------
DROP TABLE IF EXISTS payment_order;
CREATE TABLE payment_order (
    id             BIGINT        NOT NULL AUTO_INCREMENT COMMENT '支付单ID',
    payment_no     VARCHAR(64)   NOT NULL COMMENT '支付单号',
    order_id       BIGINT        NOT NULL COMMENT '关联订单ID',
    order_no       VARCHAR(64)   NOT NULL COMMENT '订单号',
    amount         DECIMAL(10,2) NOT NULL COMMENT '支付金额',
    provider       VARCHAR(20)   NOT NULL COMMENT '支付提供方：MOCK、WECHAT',
    status         VARCHAR(20)   NOT NULL DEFAULT 'PENDING' COMMENT '支付状态：PENDING、SUCCESS、FAILED、CLOSED',
    transaction_id VARCHAR(128)  DEFAULT NULL COMMENT '第三方交易号',
    created_at     DATETIME      NOT NULL COMMENT '创建时间',
    paid_at        DATETIME      DEFAULT NULL COMMENT '支付时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_payment_no (payment_no),
    KEY idx_order_id (order_id),
    KEY idx_order_no (order_no),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='支付单表';

-- ----------------------------
-- 9. 员工用户表
-- ----------------------------
DROP TABLE IF EXISTS staff_user;
CREATE TABLE staff_user (
    id            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    username      VARCHAR(50)  NOT NULL COMMENT '登录账号',
    password      VARCHAR(100) NOT NULL COMMENT '密码',
    nickname      VARCHAR(50)  NOT NULL COMMENT '显示名称',
    role          VARCHAR(20)  NOT NULL DEFAULT 'STAFF' COMMENT '角色：BOSS、MANAGER、STAFF',
    status        TINYINT      NOT NULL DEFAULT 1 COMMENT '账号状态：1启用，0禁用',
    last_login_at DATETIME     DEFAULT NULL COMMENT '最后登录时间',
    deleted       TINYINT      DEFAULT 0 COMMENT '是否删除：0未删除，1已删除',
    created_at    DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at    DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='员工用户表';

-- ----------------------------
-- 10. 顾客表
-- ----------------------------
DROP TABLE IF EXISTS customer;
CREATE TABLE customer (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    phone VARCHAR(20) NOT NULL UNIQUE COMMENT '手机号',
    nickname VARCHAR(50) COMMENT '昵称',
    avatar VARCHAR(255) COMMENT '头像URL',
    member_level VARCHAR(20) NOT NULL DEFAULT 'REGULAR' COMMENT '会员等级: REGULAR-普通, VIP-VIP, SVIP-超级VIP',
    points DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '积分',
    balance DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '余额',
    total_orders INT NOT NULL DEFAULT 0 COMMENT '总订单数',
    total_amount DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '总消费金额',
    last_visit_at DATETIME COMMENT '最后访问时间',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_phone (phone),
    INDEX idx_member_level (member_level)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='顾客表';

-- ----------------------------
-- 11. 顾客消息表
-- ----------------------------
DROP TABLE IF EXISTS customer_message;
CREATE TABLE customer_message (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    phone VARCHAR(20) COMMENT '顾客手机号',
    title VARCHAR(100) NOT NULL COMMENT '消息标题',
    content TEXT COMMENT '消息内容',
    type VARCHAR(20) NOT NULL DEFAULT 'SYSTEM' COMMENT '消息类型: SYSTEM-系统消息, ORDER-订单消息, PROMOTION-促销消息',
    is_read TINYINT NOT NULL DEFAULT 0 COMMENT '是否已读: 0-未读, 1-已读',
    related_order_id BIGINT COMMENT '关联订单ID',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_phone (phone),
    INDEX idx_created_at (created_at),
    INDEX idx_is_read (is_read)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='顾客消息表';

-- ----------------------------
-- 12. 操作日志表
-- ----------------------------
DROP TABLE IF EXISTS audit_log;
CREATE TABLE audit_log (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    user_id BIGINT COMMENT '操作用户ID',
    username VARCHAR(50) COMMENT '操作用户名',
    operation VARCHAR(100) NOT NULL COMMENT '操作类型',
    module VARCHAR(50) NOT NULL COMMENT '操作模块',
    method VARCHAR(200) COMMENT '请求方法',
    params TEXT COMMENT '请求参数',
    ip VARCHAR(50) COMMENT '操作IP',
    result VARCHAR(20) COMMENT '操作结果: SUCCESS/FAILURE',
    error_msg TEXT COMMENT '错误信息',
    execution_time INT COMMENT '执行时长(毫秒)',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    INDEX idx_user_id (user_id),
    INDEX idx_module (module),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志表';

-- ----------------------------
-- 初始化分类数据
-- ----------------------------
INSERT INTO product_category (name, sort, status) VALUES
('啤酒', 1, 1),
('鸡尾酒', 2, 1),
('威士忌', 3, 1),
('利口酒', 4, 1),
('小食', 5, 1),
('辅料', 6, 1);

-- ----------------------------
-- 初始化区域数据
-- ----------------------------
INSERT INTO table_area (name, sort, status) VALUES
('大厅', 1, 1),
('包厢', 2, 1),
('露台', 3, 1);

-- ----------------------------
-- 初始化桌台数据
-- ----------------------------
INSERT INTO bar_table (area_id, name, capacity, status) VALUES
(1, 'A1', 4, 'EMPTY'),
(1, 'A2', 4, 'EMPTY'),
(1, 'A3', 6, 'USING'),
(1, 'A4', 2, 'EMPTY'),
(2, 'VIP1', 8, 'EMPTY'),
(2, 'VIP2', 10, 'USING'),
(3, 'T1', 4, 'EMPTY'),
(3, 'T2', 4, 'CLEANING');

-- ----------------------------
-- 初始化商品数据
-- ----------------------------
INSERT INTO product (category_id, name, brand, spec, price, cost_price, stock, safe_stock, unit, description, status) VALUES
(1, '百威啤酒', 'Budweiser', '330ml/瓶', 30.00, 8.00, 86, 20, '瓶', '经典瓶装啤酒', 'ON_SALE'),
(2, '长岛冰茶', '寻野特调', '500ml/杯', 60.00, 15.00, 85, 20, '杯', '经典鸡尾酒，口感浓烈', 'ON_SALE'),
(4, '野格', 'Jägermeister', '70ml/杯', 60.00, 20.00, 45, 10, '杯', '德国草本利口酒', 'ON_SALE'),
(6, '青柠', '新鲜水果', '个', 2.00, 0.50, 200, 50, '个', '鸡尾酒辅料', 'ON_SALE'),
(5, '薯条', '寻野小食', '份', 25.00, 8.00, 999, 20, '份', '经典美式薯条', 'ON_SALE');

-- ----------------------------
-- 13. 商品品牌表
-- ----------------------------
DROP TABLE IF EXISTS product_brand;
CREATE TABLE product_brand (
    id         BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    name       VARCHAR(100) NOT NULL COMMENT '品牌名称',
    sort       INT          DEFAULT 0 COMMENT '排序',
    deleted    TINYINT      DEFAULT 0 COMMENT '是否删除：0未删除，1已删除',
    created_at DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品品牌表';

-- ----------------------------
-- 14. 活动管理表
-- ----------------------------
DROP TABLE IF EXISTS member_activity;
CREATE TABLE member_activity (
    id          BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    title       VARCHAR(100)  NOT NULL COMMENT '活动标题',
    description TEXT          COMMENT '活动描述',
    type        VARCHAR(30)   NOT NULL DEFAULT 'DISCOUNT' COMMENT '活动类型: DISCOUNT-折扣, COUPON-优惠券, POINTS-积分, SPECIAL-特惠',
    start_date  DATETIME      DEFAULT NULL COMMENT '开始时间',
    end_date    DATETIME      DEFAULT NULL COMMENT '结束时间',
    cover_image VARCHAR(500)  DEFAULT NULL COMMENT '封面图',
    status      TINYINT       DEFAULT 0 COMMENT '状态: 0-草稿, 1-进行中, 2-已结束',
    sort        INT           DEFAULT 0 COMMENT '排序',
    deleted     TINYINT       DEFAULT 0 COMMENT '是否删除：0未删除，1已删除',
    created_at  DATETIME      DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at  DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_type (type),
    KEY idx_status (status),
    KEY idx_start_date (start_date),
    KEY idx_end_date (end_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='活动管理表';

-- ----------------------------
-- 15. 会员等级权益配置表
-- ----------------------------
DROP TABLE IF EXISTS member_level_config;
CREATE TABLE member_level_config (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    level       VARCHAR(20)  NOT NULL COMMENT '会员等级: REGULAR-普通, VIP-VIP, SVIP-超级VIP',
    name        VARCHAR(50)  NOT NULL COMMENT '等级名称',
    min_amount  DECIMAL(10,2) DEFAULT 0 COMMENT '升级所需累计消费',
    discount    DECIMAL(5,2)  DEFAULT 100.00 COMMENT '折扣率(百分比, 100为无折扣)',
    points_rate DECIMAL(5,2)  DEFAULT 100.00 COMMENT '积分倍率(百分比, 100为1倍)',
    description VARCHAR(500) DEFAULT NULL COMMENT '等级描述',
    sort        INT          DEFAULT 0 COMMENT '排序',
    deleted     TINYINT      DEFAULT 0 COMMENT '是否删除',
    created_at  DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at  DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_level (level)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='会员等级配置表';

INSERT INTO member_level_config (level, name, min_amount, discount, points_rate, description, sort) VALUES
('REGULAR', '普通会员', 0, 100.00, 100.00, '新注册默认会员等级', 1),
('VIP', 'VIP会员', 1000, 95.00, 150.00, '累计消费满1000元自动升级', 2),
('SVIP', 'SVIP会员', 5000, 90.00, 200.00, '累计消费满5000元自动升级', 3);

-- ----------------------------
-- 初始化活动示例数据
-- ----------------------------
INSERT INTO member_activity (title, description, type, start_date, end_date, status, sort) VALUES
('周二特惠日', '每周二所有鸡尾酒享8折优惠', 'DISCOUNT', '2026-01-01 00:00:00', '2026-12-31 23:59:59', 1, 1),
('新客专享', '首次消费满100减20', 'COUPON', '2026-01-01 00:00:00', '2026-12-31 23:59:59', 1, 2),
('积分翻倍', '周末消费积分双倍送', 'POINTS', '2026-01-01 00:00:00', '2026-06-30 23:59:59', 1, 3);

-- ----------------------------
-- 初始化员工数据
-- 默认密码：admin123 / manager123 / staff123 / disabled123
-- ----------------------------
INSERT INTO staff_user (username, password, nickname, role, status) VALUES
('admin',         '$2b$10$XzwDaioI2bCK5bLac0yyOuEMO6YkS.cZxrgYpIZmhJWFLMxQyzFf.', '店长',   'BOSS',    1),
('manager',       '$2b$10$XzwDaioI2bCK5bLac0yyOuEMO6YkS.cZxrgYpIZmhJWFLMxQyzFf.', '经理',   'MANAGER', 1),
('staff',         '$2b$10$XzwDaioI2bCK5bLac0yyOuEMO6YkS.cZxrgYpIZmhJWFLMxQyzFf.', '员工',   'STAFF',   1),
('disabled_user', '$2b$10$XzwDaioI2bCK5bLac0yyOuEMO6YkS.cZxrgYpIZmhJWFLMxQyzFf.', '禁用账号', 'STAFF',  0);
