-- ============================================================
-- 寻野酒吧管理系统 - 完整数据库初始化脚本
-- 数据库: xunye_bar
-- 用户: pxczxn / pxczxn
-- 说明: 合并 init.sql + 所有 migration，一次执行即可还原完整数据库
-- 生成时间: 2026-05-29
-- ============================================================

CREATE DATABASE IF NOT EXISTS xunye_bar DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE xunye_bar;

-- ============================================================
-- 1. 商品分类表
-- ============================================================
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

-- ============================================================
-- 2. 商品品牌表
-- ============================================================
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

-- ============================================================
-- 3. 酒水商品表
-- ============================================================
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

-- ============================================================
-- 4. 库存流水表
-- ============================================================
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

-- ============================================================
-- 5. 桌台区域表
-- ============================================================
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

-- ============================================================
-- 6. 桌台表
-- ============================================================
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

-- ============================================================
-- 7. 员工用户表
-- ============================================================
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

-- ============================================================
-- 8. 顾客表 (合并 init.sql + 20260520_customer_profile + 20260523_customer_password + V3)
-- ============================================================
DROP TABLE IF EXISTS customer;
CREATE TABLE customer (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    customer_no VARCHAR(32) NOT NULL COMMENT '顾客唯一编号',
    openid VARCHAR(64) DEFAULT NULL COMMENT '微信openid',
    phone VARCHAR(20) NOT NULL COMMENT '手机号',
    nickname VARCHAR(50) COMMENT '昵称',
    avatar VARCHAR(255) COMMENT '头像URL',
    birthday DATE COMMENT '生日',
    gender VARCHAR(20) COMMENT '性别',
    favorite_taste VARCHAR(100) COMMENT '口味偏好',
    favorite_table VARCHAR(50) COMMENT '常用桌台',
    member_level VARCHAR(20) NOT NULL DEFAULT 'REGULAR' COMMENT '会员等级: REGULAR-普通, VIP-VIP, SVIP-超级VIP',
    points DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '积分',
    balance DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '余额',
    password VARCHAR(128) DEFAULT NULL COMMENT 'BCrypt加密密码',
    total_orders INT NOT NULL DEFAULT 0 COMMENT '总订单数',
    total_amount DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '总消费金额',
    last_visit_at DATETIME COMMENT '最后访问时间',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除标记(0=未删除,1=已删除)',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_customer_no (customer_no),
    UNIQUE KEY uk_openid (openid),
    UNIQUE KEY uk_phone (phone),
    INDEX idx_member_level (member_level)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='顾客表';

-- ============================================================
-- 9. 订单表 (合并 init.sql + 20260520_customer_profile)
-- ============================================================
DROP TABLE IF EXISTS order_info;
CREATE TABLE order_info (
    id              BIGINT        NOT NULL AUTO_INCREMENT COMMENT '订单ID',
    order_no        VARCHAR(32)   NOT NULL COMMENT '订单编号',
    table_id        BIGINT        NOT NULL COMMENT '桌台ID',
    table_name      VARCHAR(64)   NOT NULL COMMENT '桌台名称',
    customer_id     BIGINT        DEFAULT NULL COMMENT '顾客ID',
    customer_phone  VARCHAR(20)   DEFAULT NULL COMMENT '顾客手机号',
    total_amount    DECIMAL(10,2) NOT NULL COMMENT '订单总金额',
    original_amount DECIMAL(10,2) DEFAULT NULL COMMENT '订单原价',
    discount_amount DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '优惠金额',
    coupon_id       BIGINT        DEFAULT NULL COMMENT '使用优惠券ID',
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

-- ============================================================
-- 10. 订单项表
-- ============================================================
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

-- ============================================================
-- 11. 支付单表
-- ============================================================
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

-- ============================================================
-- 12. 顾客优惠券表 (合并 init.sql + V1 coupon_template 引用)
-- ============================================================
DROP TABLE IF EXISTS customer_coupon;
CREATE TABLE customer_coupon (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '优惠券ID',
    phone VARCHAR(20) NOT NULL COMMENT '顾客手机号',
    title VARCHAR(64) NOT NULL COMMENT '优惠券标题',
    rule_text VARCHAR(128) DEFAULT NULL COMMENT '使用规则',
    discount_amount DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '优惠金额',
    min_amount DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '最低使用金额',
    used TINYINT NOT NULL DEFAULT 0 COMMENT '是否已使用',
    valid_until DATE DEFAULT NULL COMMENT '有效期',
    used_at DATETIME DEFAULT NULL COMMENT '使用时间',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_phone (phone),
    KEY idx_used (used)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='顾客优惠券表';

-- ============================================================
-- 13. 顾客积分记录表
-- ============================================================
DROP TABLE IF EXISTS customer_points_record;
CREATE TABLE customer_points_record (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '积分记录ID',
    phone VARCHAR(20) NOT NULL COMMENT '顾客手机号',
    title VARCHAR(64) NOT NULL COMMENT '记录标题',
    amount INT NOT NULL COMMENT '积分变动',
    related_order_no VARCHAR(32) DEFAULT NULL COMMENT '关联订单号',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_phone (phone),
    KEY idx_order_no (related_order_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='顾客积分记录表';

-- ============================================================
-- 14. 顾客消息表
-- ============================================================
DROP TABLE IF EXISTS customer_message;
CREATE TABLE customer_message (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
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

-- ============================================================
-- 15. 操作日志表
-- ============================================================
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

-- ============================================================
-- 16. 系统配置表 (来自 20260523_system_config)
-- ============================================================
DROP TABLE IF EXISTS system_config;
CREATE TABLE system_config (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    config_key  VARCHAR(100) NOT NULL COMMENT '配置键',
    config_value TEXT        NULL COMMENT '配置值（JSON格式）',
    description VARCHAR(255) DEFAULT NULL COMMENT '配置说明',
    created_at  DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at  DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_config_key (config_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统配置表';

-- ============================================================
-- 17. 活动管理表 (合并 init.sql + 20260523_member_activity_settings)
-- ============================================================
DROP TABLE IF EXISTS member_activity;
CREATE TABLE member_activity (
    id          BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    title       VARCHAR(100)  NOT NULL COMMENT '活动标题',
    description TEXT          COMMENT '活动描述',
    type        VARCHAR(30)   NOT NULL DEFAULT 'DISCOUNT' COMMENT '活动类型: DISCOUNT-折扣, COUPON-优惠券, POINTS-积分, SPECIAL-特惠',
    start_date  DATETIME      DEFAULT NULL COMMENT '开始时间',
    end_date    DATETIME      DEFAULT NULL COMMENT '结束时间',
    cover_image VARCHAR(500)  DEFAULT NULL COMMENT '封面图',
    settings    JSON          DEFAULT NULL COMMENT '活动配置JSON',
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

-- ============================================================
-- 18. 会员等级权益配置表 (合并 init.sql + 20260526_discount_system)
-- ============================================================
DROP TABLE IF EXISTS member_level_config;
CREATE TABLE member_level_config (
    id            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    level         VARCHAR(20)  NOT NULL COMMENT '会员等级: REGULAR-普通, VIP-VIP, SVIP-超级VIP',
    name          VARCHAR(50)  NOT NULL COMMENT '等级名称',
    min_amount    DECIMAL(10,2) DEFAULT 0 COMMENT '升级所需累计消费',
    upgrade_orders INT         DEFAULT 0 COMMENT '升级所需订单数',
    discount      DECIMAL(5,2)  DEFAULT 100.00 COMMENT '折扣率(百分比, 100为无折扣)',
    points_rate   DECIMAL(5,2)  DEFAULT 100.00 COMMENT '积分倍率(百分比, 100为1倍)',
    description   VARCHAR(500) DEFAULT NULL COMMENT '等级描述',
    benefits      TEXT         DEFAULT NULL COMMENT '会员权益JSON',
    icon          VARCHAR(200) DEFAULT NULL COMMENT '等级图标URL',
    color         VARCHAR(20)  DEFAULT NULL COMMENT '等级颜色',
    sort          INT          DEFAULT 0 COMMENT '排序',
    status        TINYINT      DEFAULT 1 COMMENT '状态：0禁用，1启用',
    deleted       TINYINT      DEFAULT 0 COMMENT '是否删除',
    created_at    DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at    DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_level (level)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='会员等级配置表';

-- ============================================================
-- 19. 优惠券模板表 (来自 V1__add_coupon_template)
-- ============================================================
DROP TABLE IF EXISTS coupon_template;
CREATE TABLE coupon_template (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '模板ID',
    name VARCHAR(100) NOT NULL COMMENT '优惠券名称',
    title VARCHAR(64) NOT NULL COMMENT '优惠券标题（显示给用户）',
    description VARCHAR(500) DEFAULT NULL COMMENT '优惠券描述',
    type VARCHAR(20) NOT NULL DEFAULT 'AMOUNT' COMMENT '类型：AMOUNT满减，DISCOUNT折扣',
    discount_amount DECIMAL(10,2) DEFAULT 0 COMMENT '优惠金额（满减券）',
    discount_rate DECIMAL(5,2) DEFAULT 0 COMMENT '折扣率（折扣券，如8.8表示8.8折）',
    min_amount DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '最低使用金额',
    scope_type VARCHAR(20) NOT NULL DEFAULT 'ALL' COMMENT '适用范围：ALL全场，PRODUCT指定商品，CATEGORY指定分类',
    scope_config JSON DEFAULT NULL COMMENT '范围配置',
    issue_type VARCHAR(20) NOT NULL DEFAULT 'MANUAL' COMMENT '发放类型：MANUAL手动，AUTO_NEW_USER新用户，AUTO_POINTS积分兑换',
    issue_config JSON DEFAULT NULL COMMENT '发放配置',
    valid_days INT NOT NULL DEFAULT 30 COMMENT '有效天数',
    max_use_count INT DEFAULT NULL COMMENT '每人最多使用次数（NULL表示不限）',
    total_count INT DEFAULT NULL COMMENT '总发放数量（NULL表示不限）',
    issued_count INT NOT NULL DEFAULT 0 COMMENT '已发放数量',
    used_count INT NOT NULL DEFAULT 0 COMMENT '已使用数量',
    member_level_limit VARCHAR(100) DEFAULT NULL COMMENT '会员等级限制（逗号分隔，NULL表示不限）',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1启用，0禁用',
    sort INT DEFAULT 0 COMMENT '排序',
    deleted TINYINT DEFAULT 0 COMMENT '是否删除：0未删除，1已删除',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_type (type),
    KEY idx_status (status),
    KEY idx_issue_type (issue_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='优惠券模板表';

-- ============================================================
-- 20. 折扣规则表 (来自 entity + 20260526_discount_system)
-- ============================================================
DROP TABLE IF EXISTS discount_rule;
CREATE TABLE discount_rule (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '规则ID',
    name VARCHAR(100) NOT NULL COMMENT '规则名称',
    description VARCHAR(500) DEFAULT NULL COMMENT '规则描述',
    rule_type VARCHAR(30) NOT NULL COMMENT '规则类型：MEMBER会员折扣、ACTIVITY活动折扣、COUPON优惠券折扣',
    priority INT NOT NULL DEFAULT 0 COMMENT '优先级（数值越大越优先）',
    conditions TEXT DEFAULT NULL COMMENT '条件配置JSON',
    exclusive_groups VARCHAR(200) DEFAULT NULL COMMENT '互斥组',
    stackable TINYINT DEFAULT 1 COMMENT '是否可叠加：0不可叠加，1可叠加',
    max_discount_amount DECIMAL(10,2) DEFAULT NULL COMMENT '最大优惠金额',
    min_pay_amount DECIMAL(10,2) DEFAULT NULL COMMENT '最低支付金额',
    status TINYINT DEFAULT 1 COMMENT '状态：0禁用，1启用',
    deleted TINYINT DEFAULT 0 COMMENT '是否删除：0未删除，1已删除',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_rule_type (rule_type),
    KEY idx_status (status),
    KEY idx_priority (priority)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='折扣规则表';


-- ============================================================
-- 初始化种子数据
-- ============================================================

-- ----------------------------
-- 商品分类
-- ----------------------------
INSERT INTO product_category (name, sort, status) VALUES
('啤酒', 1, 1),
('鸡尾酒', 2, 1),
('威士忌', 3, 1),
('利口酒', 4, 1),
('小食', 5, 1),
('辅料', 6, 1);

-- ----------------------------
-- 桌台区域
-- ----------------------------
INSERT INTO table_area (name, sort, status) VALUES
('大厅', 1, 1),
('包厢', 2, 1),
('露台', 3, 1);

-- ----------------------------
-- 桌台
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
-- 商品
-- ----------------------------
INSERT INTO product (category_id, name, brand, spec, price, cost_price, stock, safe_stock, unit, description, status) VALUES
(1, '百威啤酒', 'Budweiser', '330ml/瓶', 30.00, 8.00, 86, 20, '瓶', '经典瓶装啤酒', 'ON_SALE'),
(2, '长岛冰茶', '寻野特调', '500ml/杯', 60.00, 15.00, 85, 20, '杯', '经典鸡尾酒，口感浓烈', 'ON_SALE'),
(4, '野格', 'Jägermeister', '70ml/杯', 60.00, 20.00, 45, 10, '杯', '德国草本利口酒', 'ON_SALE'),
(6, '青柠', '新鲜水果', '个', 2.00, 0.50, 200, 50, '个', '鸡尾酒辅料', 'ON_SALE'),
(5, '薯条', '寻野小食', '份', 25.00, 8.00, 999, 20, '份', '经典美式薯条', 'ON_SALE');

-- ----------------------------
-- 员工用户 (默认密码均为 pxczxn，BCrypt加密)
-- ----------------------------
INSERT INTO staff_user (username, password, nickname, role, status) VALUES
('admin',   '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '店长',   'BOSS',    1),
('manager', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '经理',   'MANAGER', 1),
('staff',   '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '员工',   'STAFF',   1);

-- ----------------------------
-- 系统配置
-- ----------------------------
INSERT INTO system_config (config_key, config_value, description) VALUES
('shop.name',           '寻野',                              '店铺名称'),
('shop.slogan',         '乘兴而去，尽兴而归。',                '店铺标语'),
('shop.address',        '广东省深圳市南山区科技路88号',         '店铺地址'),
('shop.phone',          '0755-88888888',                     '联系电话'),
('shop.contact_wechat', 'xunye_bar',                         '联系微信'),
('shop.business_hours', '18:00 - 02:00',                     '营业时间'),
('shop.notice',         '未成年人禁止饮酒，请理性消费。',         '店铺公告'),
('order.payment_methods', '["WECHAT","ALIPAY","CASH"]',        '支持的支付方式'),
('order.receipt_bar',     '{"enabled":true,"printer":"USB"}',    '吧台打印配置'),
('order.receipt_kitchen', '{"enabled":false,"printer":"NETWORK"}', '后厨打印配置'),
('order.cancel_timeout',  '30',                                '订单自动取消时间（分钟）'),
('miniapp.homepage_title',      '寻野酒吧',                   '首页标题'),
('miniapp.homepage_subtitle',   '乘兴而去，尽兴而归。',        '首页副标题'),
('miniapp.menu_display',        'all',                        '菜单展示模式：all全部/on_sale在售'),
('miniapp.scan_to_order',       'true',                       '是否开启扫码点单'),
('miniapp.banner_images',       '[]',                           '首页轮播图（图片URL数组）');

-- ----------------------------
-- 会员等级配置 (合并含扩展列的完整数据)
-- ----------------------------
INSERT INTO member_level_config (level, name, min_amount, upgrade_orders, discount, points_rate, description, benefits, color, sort, status) VALUES
('REGULAR', '普通会员', 0, 0, 100.00, 100.00, '新注册默认会员等级', '{"description": "Basic member benefits"}', '#95a5a6', 1, 1),
('VIP', 'VIP会员', 1000, 5, 95.00, 150.00, '累计消费满1000元自动升级', '{"description": "2% off + 1.2x points"}', '#c0c0c0', 2, 1),
('SVIP', 'SVIP会员', 5000, 20, 90.00, 200.00, '累计消费满5000元自动升级', '{"description": "5% off + 1.5x points + birthday gift"}', '#ffd700', 3, 1);

-- ----------------------------
-- 活动管理
-- ----------------------------
INSERT INTO member_activity (title, description, type, start_date, end_date, settings, status, sort) VALUES
('周二特惠日', '每周二所有鸡尾酒享8折优惠', 'DISCOUNT', '2026-01-01 00:00:00', '2026-12-31 23:59:59', JSON_OBJECT('discountRate', 8.0, 'minAmount', 0), 1, 1),
('新客专享', '首次消费满100减20', 'COUPON', '2026-01-01 00:00:00', '2026-12-31 23:59:59', JSON_OBJECT('discountAmount', 20, 'minAmount', 100), 1, 2),
('积分翻倍', '周末消费积分双倍送', 'POINTS', '2026-01-01 00:00:00', '2026-06-30 23:59:59', JSON_OBJECT('pointsMultiplier', 2), 1, 3);

-- ----------------------------
-- 优惠券模板
-- ----------------------------
INSERT INTO coupon_template (name, title, description, type, discount_amount, discount_rate, min_amount, scope_type, issue_type, issue_config, valid_days, max_use_count, status, sort) VALUES
('新用户专享券', '满99减10', '新用户注册即送，全场酒水可用', 'AMOUNT', 10.00, 0, 99.00, 'ALL', 'NEW_USER', '{"auto_issue": true}', 30, 1, 1, 100),
('满减优惠券', '满200减30', '全场通用，消费满200元立减30元', 'AMOUNT', 30.00, 0, 200.00, 'ALL', 'MANUAL', NULL, 45, 1, 1, 90),
('会员专享折扣', '全场9折', '会员专享，全场酒水9折优惠', 'DISCOUNT', 0, 0.90, 0.00, 'ALL', 'MANUAL', NULL, 60, 3, 1, 80),
('生日特权券', '满150减50', '生日月专享，全场酒水可用', 'AMOUNT', 50.00, 0, 150.00, 'ALL', 'ACTIVITY', '{"trigger": "birthday"}', 30, 1, 1, 70),
('积分兑换券', '满100减15', '使用500积分兑换', 'AMOUNT', 15.00, 0, 100.00, 'ALL', 'POINTS', '{"points_cost": 500}', 30, 1, 1, 60),
('周末狂欢券', '满300减60', '周末专享，消费满300元立减60元', 'AMOUNT', 60.00, 0, 300.00, 'ALL', 'ACTIVITY', '{"valid_days": [5, 6, 0]}', 15, 1, 1, 50),
('无门槛立减券', '立减8元', '全场通用，无门槛立减', 'AMOUNT', 8.00, 0, 0.00, 'ALL', 'MANUAL', NULL, 20, 1, 1, 40),
('VIP专属券', '全场85折', 'VIP会员专享，全场85折', 'DISCOUNT', 0, 0.85, 0.00, 'ALL', 'MANUAL', NULL, 90, 5, 1, 30);

-- ----------------------------
-- 折扣规则
-- ----------------------------
INSERT INTO discount_rule (name, description, rule_type, priority, conditions, exclusive_groups, stackable, status) VALUES
('会员等级折扣', '根据会员等级自动应用折扣', 'MEMBER', 100, '{"auto_apply": true}', 'member_discount', 1, 1),
('活动折扣', '营销活动折扣', 'ACTIVITY', 90, '{"requires_activity": true}', 'activity_discount', 1, 1),
('优惠券折扣', '使用优惠券', 'COUPON', 80, '{"requires_coupon": true}', '', 1, 1);
