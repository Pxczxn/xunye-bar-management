# 微信小程序自动化测试配置指南

## 第一步：安装微信开发者工具

1. 下载微信开发者工具：https://developers.weixin.qq.com/miniprogram/dev/devtools/download.html
2. 安装到默认路径（推荐）

## 第二步：开启自动化端口

1. 打开微信开发者工具
2. 点击菜单栏：设置 -> 安全设置
3. 勾选 "服务端口" 选项
4. 记下端口号（默认是 9420）

## 第三步：配置项目

你的小程序项目路径：
```
D:/Codeing/Project/XUNYE/xunye-miniapp/dist/dev/mp-weixin
```

项目配置文件已存在：
- AppID: wx7795347bab1a6d34
- 项目名称: unibest

## 第四步：运行测试脚本

已为你创建测试脚本：
- `test_miniprogram.js` - 自动化测试脚本
- `test_miniprogram_config.json` - 配置文件

运行命令：
```bash
cd D:/Codeing/Project/XUNYE
node test_miniprogram.js
```

## 常见问题

### 1. 找不到微信开发者工具
确保已安装并记录安装路径，在配置文件中指定 `cliPath`

### 2. 端口连接失败
- 确保微信开发者工具已打开
- 确保已开启"服务端口"
- 检查端口号是否正确（默认 9420）

### 3. 项目打开失败
- 确保项目路径正确
- 确保项目已编译（运行 `npm run dev:mp-weixin`）

## 下一步

1. 先手动打开微信开发者工具
2. 开启服务端口
3. 运行测试脚本
