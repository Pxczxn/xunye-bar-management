-- 更新会员等级配置表结构
-- 检查并添加缺失的列

-- 添加 upgrade_orders 列
SET @col_exists = 0;
SELECT COUNT(*) INTO @col_exists FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = 'xunye_bar' AND TABLE_NAME = 'member_level_config' AND COLUMN_NAME = 'upgrade_orders';
SET @sql = IF(@col_exists = 0,
  'ALTER TABLE `member_level_config` ADD COLUMN `upgrade_orders` INT DEFAULT 0 COMMENT ''Upgrade required orders'' AFTER `min_amount`',
  'SELECT ''Column upgrade_orders already exists'' AS msg');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 添加 benefits 列
SET @col_exists = 0;
SELECT COUNT(*) INTO @col_exists FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = 'xunye_bar' AND TABLE_NAME = 'member_level_config' AND COLUMN_NAME = 'benefits';
SET @sql = IF(@col_exists = 0,
  'ALTER TABLE `member_level_config` ADD COLUMN `benefits` TEXT COMMENT ''Member benefits JSON'' AFTER `description`',
  'SELECT ''Column benefits already exists'' AS msg');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 添加 icon 列
SET @col_exists = 0;
SELECT COUNT(*) INTO @col_exists FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = 'xunye_bar' AND TABLE_NAME = 'member_level_config' AND COLUMN_NAME = 'icon';
SET @sql = IF(@col_exists = 0,
  'ALTER TABLE `member_level_config` ADD COLUMN `icon` VARCHAR(200) COMMENT ''Level icon URL'' AFTER `benefits`',
  'SELECT ''Column icon already exists'' AS msg');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 添加 color 列
SET @col_exists = 0;
SELECT COUNT(*) INTO @col_exists FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = 'xunye_bar' AND TABLE_NAME = 'member_level_config' AND COLUMN_NAME = 'color';
SET @sql = IF(@col_exists = 0,
  'ALTER TABLE `member_level_config` ADD COLUMN `color` VARCHAR(20) COMMENT ''Level color'' AFTER `icon`',
  'SELECT ''Column color already exists'' AS msg');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 添加 status 列
SET @col_exists = 0;
SELECT COUNT(*) INTO @col_exists FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = 'xunye_bar' AND TABLE_NAME = 'member_level_config' AND COLUMN_NAME = 'status';
SET @sql = IF(@col_exists = 0,
  'ALTER TABLE `member_level_config` ADD COLUMN `status` TINYINT DEFAULT 1 COMMENT ''Status: 0-disabled, 1-enabled'' AFTER `sort`',
  'SELECT ''Column status already exists'' AS msg');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 更新现有会员等级数据
UPDATE `member_level_config` SET
  `upgrade_orders` = 0,
  `benefits` = '{"description": "Basic member benefits"}',
  `color` = '#95a5a6',
  `status` = 1
WHERE `level` = 'REGULAR';

UPDATE `member_level_config` SET
  `upgrade_orders` = 5,
  `benefits` = '{"description": "2% off + 1.2x points"}',
  `color` = '#c0c0c0',
  `status` = 1
WHERE `level` = 'SILVER';

UPDATE `member_level_config` SET
  `upgrade_orders` = 20,
  `benefits` = '{"description": "5% off + 1.5x points + birthday gift"}',
  `color` = '#ffd700',
  `status` = 1
WHERE `level` = 'GOLD';

UPDATE `member_level_config` SET
  `upgrade_orders` = 50,
  `benefits` = '{"description": "8% off + 2x points + exclusive events"}',
  `color` = '#e5e4e2',
  `status` = 1
WHERE `level` = 'PLATINUM';

UPDATE `member_level_config` SET
  `upgrade_orders` = 100,
  `benefits` = '{"description": "12% off + 3x points + VIP service"}',
  `color` = '#b9f2ff',
  `status` = 1
WHERE `level` = 'DIAMOND';

-- 插入默认折扣规则（如果不存在）
INSERT INTO `discount_rule` (`name`, `description`, `rule_type`, `priority`, `conditions`, `exclusive_groups`, `stackable`, `status`)
SELECT 'Member Level Discount', 'Auto apply discount based on member level', 'MEMBER', 100, '{"auto_apply": true}', 'member_discount', 1, 1
WHERE NOT EXISTS (SELECT 1 FROM `discount_rule` WHERE `name` = 'Member Level Discount');

INSERT INTO `discount_rule` (`name`, `description`, `rule_type`, `priority`, `conditions`, `exclusive_groups`, `stackable`, `status`)
SELECT 'Activity Discount', 'Marketing activity discount', 'ACTIVITY', 90, '{"requires_activity": true}', 'activity_discount', 1, 1
WHERE NOT EXISTS (SELECT 1 FROM `discount_rule` WHERE `name` = 'Activity Discount');

INSERT INTO `discount_rule` (`name`, `description`, `rule_type`, `priority`, `conditions`, `exclusive_groups`, `stackable`, `status`)
SELECT 'Coupon Discount', 'Use coupon', 'COUPON', 80, '{"requires_coupon": true}', '', 1, 1
WHERE NOT EXISTS (SELECT 1 FROM `discount_rule` WHERE `name` = 'Coupon Discount');
