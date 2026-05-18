# 配置文件敏感信息处理说明

## 概述
为了提高系统安全性，已将配置文件中的敏感信息（如数据库密码、API密钥等）改为通过环境变量注入。

## 配置方式

### 方式1：使用环境变量（推荐生产环境）

在系统环境变量或启动脚本中设置：

```bash
# Linux/Mac
export DB_HOST=localhost
export DB_PORT=3306
export DB_NAME=xunye_bar
export DB_USERNAME=your_username
export DB_PASSWORD=your_password
export PAYMENT_PROVIDER=mock

# Windows PowerShell
$env:DB_HOST="localhost"
$env:DB_PORT="3306"
$env:DB_NAME="xunye_bar"
$env:DB_USERNAME="your_username"
$env:DB_PASSWORD="your_password"
$env:PAYMENT_PROVIDER="mock"
```

### 方式2：使用.env文件（推荐开发环境）

1. 复制 `.env.example` 文件为 `.env`：
   ```bash
   cp .env.example .env
   ```

2. 编辑 `.env` 文件，填入实际的配置值：
   ```properties
   DB_HOST=localhost
   DB_PORT=3306
   DB_NAME=xunye_bar
   DB_USERNAME=pxczxn
   DB_PASSWORD=pxczxn
   PAYMENT_PROVIDER=mock
   ```

3. 确保 `.env` 文件已被 `.gitignore` 忽略，不会提交到版本控制

### 方式3：使用IDE环境变量配置

在IDEA中配置运行环境变量：
1. Run -> Edit Configurations
2. 选择你的Spring Boot应用
3. 在 Environment variables 中添加配置

## 配置项说明

| 配置项 | 说明 | 默认值 |
|--------|------|--------|
| DB_HOST | 数据库主机地址 | localhost |
| DB_PORT | 数据库端口 | 3306 |
| DB_NAME | 数据库名称 | xunye_bar |
| DB_USERNAME | 数据库用户名 | pxczxn |
| DB_PASSWORD | 数据库密码 | pxczxn |
| PAYMENT_PROVIDER | 支付提供方 | mock |
| REDIS_HOST | Redis主机地址 | localhost |
| REDIS_PORT | Redis端口 | 6379 |
| REDIS_PASSWORD | Redis密码 | (空) |
| REDIS_DATABASE | Redis数据库编号 | 0 |

## 注意事项

1. **不要将 `.env` 文件提交到版本控制系统**
2. **生产环境建议使用系统环境变量或密钥管理服务**
3. **定期更换数据库密码和API密钥**
4. **确保配置文件权限设置正确，避免未授权访问**
5. **如果没有设置环境变量，系统会使用默认值（仅适用于开发环境）**

## 安全建议

1. 生产环境使用强密码
2. 使用专用的数据库账户，仅授予必要的权限
3. 启用数据库访问白名单
4. 定期审计配置文件和环境变量
5. 考虑使用密钥管理服务（如AWS Secrets Manager、Azure Key Vault等）
