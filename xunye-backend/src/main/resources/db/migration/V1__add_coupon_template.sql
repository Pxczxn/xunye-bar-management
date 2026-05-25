-- ================================
-- 优惠券模板表
-- ================================

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

    -- 适用范围
    scope_type VARCHAR(20) NOT NULL DEFAULT 'ALL' COMMENT '适用范围：ALL全场，PRODUCT指定商品，CATEGORY指定分类',
    scope_config JSON DEFAULT NULL COMMENT '范围配置：{"productIds":[1,2,3],"categoryIds":[1,2]}',

    -- 发放规则
    issue_type VARCHAR(20) NOT NULL DEFAULT 'MANUAL' COMMENT '发放类型：MANUAL手动，AUTO_NEW_USER新用户，AUTO_POINTS积分兑换',
    issue_config JSON DEFAULT NULL COMMENT '发放配置：{"pointsCost":100,"maxCount":1000}',

    -- 使用限制
    valid_days INT NOT NULL DEFAULT 30 COMMENT '有效天数',
    max_use_count INT DEFAULT NULL COMMENT '每人最多使用次数（NULL表示不限）',
    total_count INT DEFAULT NULL COMMENT '总发放数量（NULL表示不限）',
    issued_count INT NOT NULL DEFAULT 0 COMMENT '已发放数量',
    used_count INT NOT NULL DEFAULT 0 COMMENT '已使用数量',

    -- 会员等级限制
    member_level_limit VARCHAR(100) DEFAULT NULL COMMENT '会员等级限制：REGULAR,VIP,SVIP（逗号分隔，NULL表示不限）',

    -- 状态
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

-- Modify customer_coupon table, add template_id if not exists
-- ALTER TABLE customer_coupon ADD COLUMN template_id BIGINT DEFAULT NULL COMMENT 'Template ID' AFTER id;
-- ALTER TABLE customer_coupon ADD KEY idx_template_id (template_id);

-- Insert sample coupon templates
INSERT INTO coupon_template (name, title, description, type, discount_amount, min_amount, scope_type, issue_type, valid_days, status, sort) VALUES
('new_user_99_10', 'man 99 jian 10', 'quan chang jiu shui ke yong', 'AMOUNT', 10.00, 99.00, 'ALL', 'AUTO_NEW_USER', 30, 1, 1),
('snack_18', 'xiao shi li jian 18', 'zuo jiu xiao shi ke yong', 'AMOUNT', 18.00, 0.00, 'CATEGORY', 'MANUAL', 30, 1, 2),
('special_half_price', 'te tiao di er bei ban jia', 'zhao pai te tiao ke yong', 'AMOUNT', 34.00, 68.00, 'CATEGORY', 'MANUAL', 20, 1, 3);

