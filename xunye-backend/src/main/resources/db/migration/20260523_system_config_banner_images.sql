-- ================================
-- 补充轮播图配置项（针对已执行初始配置迁移的环境）
-- ================================
INSERT IGNORE INTO system_config (config_key, config_value, description)
VALUES ('miniapp.banner_images', '[]', '首页轮播图（图片URL数组）');
