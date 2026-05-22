-- ================================
-- 系统配置表 - 用于存储店铺、订单、小程序等配置
-- ================================
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

-- ================================
-- 初始化默认配置
-- ================================
INSERT INTO system_config (config_key, config_value, description) VALUES
-- 店铺配置
('shop.name',           '寻野',                              '店铺名称'),
('shop.slogan',         '乘兴而去，尽兴而归。',                '店铺标语'),
('shop.address',        '广东省深圳市南山区科技路88号',         '店铺地址'),
('shop.phone',          '0755-88888888',                     '联系电话'),
('shop.contact_wechat', 'xunye_bar',                         '联系微信'),
('shop.business_hours', '18:00 - 02:00',                     '营业时间'),
('shop.notice',         '未成年人禁止饮酒，请理性消费。',         '店铺公告'),
-- 订单配置
('order.payment_methods', '["WECHAT","ALIPAY","CASH"]',        '支持的支付方式'),
('order.receipt_bar',     '{"enabled":true,"printer":"USB"}',    '吧台打印配置'),
('order.receipt_kitchen', '{"enabled":false,"printer":"NETWORK"}', '后厨打印配置'),
('order.cancel_timeout',  '30',                                '订单自动取消时间（分钟）'),
-- 小程序配置
('miniapp.homepage_title',      '寻野酒吧',                   '首页标题'),
('miniapp.homepage_subtitle',   '乘兴而去，尽兴而归。',        '首页副标题'),
('miniapp.menu_display',        'all',                        '菜单展示模式：all全部/on_sale在售'),
('miniapp.scan_to_order',       'true',                       '是否开启扫码点单'),
('miniapp.banner_images',       '[]',                           '首页轮播图（图片URL数组）');
