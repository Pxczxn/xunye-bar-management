-- ================================
-- 补丁 v2：新增 serve_status 和 source 字段
-- 适用于：已执行旧版 init.sql（不含这两列）的数据库
-- 执行前请确认 order_info 表是否已有这两列：
--   SHOW COLUMNS FROM order_info LIKE 'serve_status';
--   SHOW COLUMNS FROM order_info LIKE 'source';
-- 如果已存在，跳过对应 ALTER 语句，避免报错。
-- ================================

ALTER TABLE order_info
    ADD COLUMN serve_status VARCHAR(32) NOT NULL DEFAULT 'PENDING'
    COMMENT '履约状态：PENDING待处理、MAKING制作中、FINISHED已完成'
    AFTER status;

ALTER TABLE order_info
    ADD COLUMN source VARCHAR(32) NOT NULL DEFAULT 'ADMIN_POS'
    COMMENT '订单来源：ADMIN_POS吧台点单、CUSTOMER_MINI顾客小程序'
    AFTER serve_status;
