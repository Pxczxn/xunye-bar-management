ALTER TABLE customer
    ADD COLUMN customer_no VARCHAR(32) NULL COMMENT '顾客唯一编号' AFTER id,
    ADD COLUMN openid VARCHAR(64) NULL COMMENT '微信openid' AFTER customer_no,
    ADD COLUMN birthday DATE NULL COMMENT '生日' AFTER avatar,
    ADD COLUMN gender VARCHAR(20) NULL COMMENT '性别' AFTER birthday,
    ADD COLUMN favorite_taste VARCHAR(100) NULL COMMENT '口味偏好' AFTER gender,
    ADD COLUMN favorite_table VARCHAR(50) NULL COMMENT '常用桌台' AFTER favorite_taste;

UPDATE customer
SET customer_no = CONCAT('XY', DATE_FORMAT(COALESCE(created_at, NOW()), '%Y%m%d'), LPAD(id, 4, '0'))
WHERE customer_no IS NULL OR customer_no = '';

ALTER TABLE customer
    MODIFY COLUMN customer_no VARCHAR(32) NOT NULL COMMENT '顾客唯一编号',
    ADD UNIQUE KEY uk_customer_no (customer_no),
    ADD UNIQUE KEY uk_openid (openid);

ALTER TABLE order_info
    ADD COLUMN original_amount DECIMAL(10,2) NULL COMMENT '订单原价' AFTER total_amount,
    ADD COLUMN discount_amount DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '优惠金额' AFTER original_amount,
    ADD COLUMN coupon_id BIGINT NULL COMMENT '使用优惠券ID' AFTER discount_amount;

CREATE TABLE IF NOT EXISTS customer_coupon (
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

CREATE TABLE IF NOT EXISTS customer_points_record (
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
