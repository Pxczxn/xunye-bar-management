-- ================================
-- 快速更新店铺公告和轮播图
-- ================================

-- 1. 更新店铺公告
UPDATE system_config
SET config_value = '公告：今晚21:00至1:00全场酒水7折，套餐8折！特惠至今晚！'
WHERE config_key = 'shop.notice';

-- 2. 更新营业时间
UPDATE system_config
SET config_value = '18:00 - 02:00'
WHERE config_key = 'shop.business_hours';

-- 3. 更新店铺名称
UPDATE system_config
SET config_value = '寻野 XUNYE'
WHERE config_key = 'shop.name';

-- 4. 更新店铺标语
UPDATE system_config
SET config_value = '乘兴而去，尽兴而归。'
WHERE config_key = 'shop.slogan';

-- 5. 更新轮播图（JSON数组格式）
-- 注意：图片需要先通过管理后台上传，或者手动放到 images/miniapp 目录
UPDATE system_config
SET config_value = '["/images/miniapp/banner1.jpg", "/images/miniapp/banner2.jpg", "/images/miniapp/banner3.jpg"]'
WHERE config_key = 'miniapp.banner_images';

-- ================================
-- 查询当前配置
-- ================================
SELECT config_key, config_value, description
FROM system_config
WHERE config_key LIKE 'shop.%' OR config_key LIKE 'miniapp.%'
ORDER BY config_key;
