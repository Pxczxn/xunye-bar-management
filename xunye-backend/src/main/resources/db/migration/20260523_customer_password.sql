ALTER TABLE customer
    ADD COLUMN password VARCHAR(128) NULL COMMENT 'BCrypt加密密码' AFTER balance;

UPDATE customer
SET password = NULL
WHERE password IS NOT NULL;
