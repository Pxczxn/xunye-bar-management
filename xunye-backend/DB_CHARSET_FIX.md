# 数据库字符集乱码修复说明

## 问题原因
MySQL 客户端默认使用 GBK 字符集，而数据库使用 UTF8MB4，导致中文注释显示为乱码。

## 解决方案

### 方法1：使用修复脚本（推荐）
```bash
mysql -u pxczxn -ppxczxn --default-character-set=utf8mb4 xunye_bar < fix_db_charset.sql
```

### 方法2：配置 MySQL 客户端
在 `my.ini` 或 `my.cnf` 中添加：
```ini
[mysql]
default-character-set=utf8mb4

[client]
default-character-set=utf8mb4
```

### 方法3：Navicat 连接设置
1. 打开连接设置
2. 点击"高级"选项卡
3. 设置"编码"为 `utf8mb4`
4. 重新连接

## 验证
执行以下命令验证字符集：
```sql
SHOW VARIABLES LIKE 'character_set%';
SHOW FULL COLUMNS FROM discount_rule;
```

## 已修复的表
- ✅ discount_rule - 折扣规则管理表
- ✅ coupon_template - 优惠券模板表
- ✅ 其他所有表的表注释

## 日期
2026-05-26
