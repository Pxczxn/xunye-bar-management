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
    KEY idx_status (status)
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
    KEY idx_status (status),
    KEY idx_created_at (created_at)
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
-- 初始化员工数据
-- 默认密码：admin123 / manager123 / staff123 / disabled123
-- ----------------------------
INSERT INTO staff_user (username, password, nickname, role, status) VALUES
('admin',         '$2b$10$XzwDaioI2bCK5bLac0yyOuEMO6YkS.cZxrgYpIZmhJWFLMxQyzFf.', '店长',   'BOSS',    1),
('manager',       '$2b$10$XzwDaioI2bCK5bLac0yyOuEMO6YkS.cZxrgYpIZmhJWFLMxQyzFf.', '经理',   'MANAGER', 1),
('staff',         '$2b$10$XzwDaioI2bCK5bLac0yyOuEMO6YkS.cZxrgYpIZmhJWFLMxQyzFf.', '员工',   'STAFF',   1),
('disabled_user', '$2b$10$XzwDaioI2bCK5bLac0yyOuEMO6YkS.cZxrgYpIZmhJWFLMxQyzFf.', '禁用账号', 'STAFF',  0);
