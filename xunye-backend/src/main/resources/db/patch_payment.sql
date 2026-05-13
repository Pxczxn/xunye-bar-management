CREATE TABLE IF NOT EXISTS `payment_order` (
    `id`             BIGINT       NOT NULL AUTO_INCREMENT,
    `payment_no`     VARCHAR(64)  NOT NULL UNIQUE,
    `order_id`       BIGINT       NOT NULL,
    `order_no`       VARCHAR(64)  NOT NULL,
    `amount`         DECIMAL(10,2) NOT NULL,
    `provider`       VARCHAR(20)  NOT NULL COMMENT 'MOCK / WECHAT',
    `status`         VARCHAR(20)  NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING / SUCCESS / FAILED / CLOSED',
    `transaction_id` VARCHAR(128) DEFAULT NULL,
    `created_at`     DATETIME     NOT NULL,
    `paid_at`        DATETIME     DEFAULT NULL,
    PRIMARY KEY (`id`),
    KEY `idx_order_id` (`order_id`),
    KEY `idx_order_no` (`order_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
