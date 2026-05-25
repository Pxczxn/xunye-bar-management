-- 添加 deleted 列到 customer 表
ALTER TABLE customer
ADD COLUMN deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除标记(0=未删除,1=已删除)' AFTER last_visit_at;
