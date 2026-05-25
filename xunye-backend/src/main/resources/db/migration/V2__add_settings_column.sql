-- 添加 settings 列到 member_activity 表
ALTER TABLE member_activity 
ADD COLUMN settings JSON NULL COMMENT '活动配置(JSON格式)' AFTER cover_image;
