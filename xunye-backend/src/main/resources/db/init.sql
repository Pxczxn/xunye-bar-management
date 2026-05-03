-- ================================
-- 寻野酒吧管理系统 - 数据库初始化脚本
-- ================================

-- 创建数据库（如不存在）
CREATE DATABASE IF NOT EXISTS xunye_bar DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE xunye_bar;

-- ----------------------------
-- 1. 商品分类表
-- ----------------------------
DROP TABLE IF EXISTS product_category;
CREATE TABLE product_category (
    id         BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    name       VARCHAR(50)  NOT NULL COMMENT '分类名称',
    sort       INT          DEFAULT 0 COMMENT '排序',
    status     TINYINT      DEFAULT 1 COMMENT '状态：1启用，0禁用',
    deleted    TINYINT      DEFAULT 0 COMMENT '是否删除：0未删除，1已删除',
    created_at DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
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
-- 3. 库存流水表
-- ----------------------------
DROP TABLE IF EXISTS inventory_record;
CREATE TABLE inventory_record (
    id              BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    product_id      BIGINT        NOT NULL COMMENT '商品ID',
    product_name    VARCHAR(100)  NOT NULL COMMENT '商品名称',
    type            VARCHAR(20)   NOT NULL COMMENT '操作类型：IN入库、OUT出库、LOSS损耗、ADJUST盘点调整',
    change_quantity INT           NOT NULL COMMENT '变动数量',
    before_stock    INT           NOT NULL COMMENT '变动前库存',
    after_stock     INT           NOT NULL COMMENT '变动后库存',
    reason          VARCHAR(255)  DEFAULT NULL COMMENT '操作原因',
    operator_name   VARCHAR(50)   DEFAULT NULL COMMENT '操作人',
    created_at      DATETIME      DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_product_id (product_id),
    KEY idx_type (type),
    KEY idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='库存流水表';

-- ----------------------------
-- 初始化商品数据
------------------------------
INSERT INTO product (category_id, name, brand, spec, price, cost_price, stock, safe_stock, unit, description, status) VALUES
(1, '百威啤酒', 'Budweiser', '330ml/瓶', 30.00, 8.00, 86, 20, '瓶', '经典瓶装啤酒', 'ON_SALE'),
(2, '长岛冰茶', '寻野特调', '500ml/杯', 60.00, 15.00, 85, 20, '杯', '经典鸡尾酒，口感浓烈', 'ON_SALE'),
(4, '野格', 'Jägermeister', '70ml/杯', 60.00, 20.00, 45, 10, '杯', '德国草本利口酒', 'ON_SALE'),
(6, '青柠', '新鲜水果', '个', 2.00, 0.50, 200, 50, '个', '鸡尾酒辅料', 'ON_SALE'),
(5, '薯条', '寻野小食', '份', 25.00, 8.00, 999, 20, '份', '经典美式薯条', 'ON_SALE');
