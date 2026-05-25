# 活动功能开发总结

## 完成时间
2026-05-26

## 问题修复

### 1. LocalDateTime 反序列化错误
**问题**: 前端发送的日期格式 `"2026-05-25 22:51:00"` 无法被 Jackson 反序列化

**解决方案**: 
- 创建 `JacksonConfig` 配置类
- 使用 `Jackson2ObjectMapperBuilderCustomizer` 配置自定义的 LocalDateTime 序列化器和反序列化器
- 支持 `yyyy-MM-dd HH:mm:ss` 格式

**文件**: `xunye-backend/src/main/java/com/xunye/admin/config/JacksonConfig.java`

### 2. 数据库表结构缺失

#### member_activity 表缺少 settings 列
**问题**: 插入活动时报错 `Unknown column 'settings' in 'field list'`

**解决方案**:
- 添加 JSON 类型的 `settings` 列用于存储活动配置
- 创建数据库迁移脚本 `V2__add_settings_column.sql`

#### customer 表缺少 deleted 列
**问题**: 查询客户时报错 `Unknown column 'deleted' in 'field list'`

**解决方案**:
- 添加 `deleted` 列用于逻辑删除
- 创建数据库迁移脚本 `V3__add_deleted_column_to_customer.sql`

## 新增功能

### 小程序活动展示
在小程序首页添加"活动专区"模块，展示进行中的活动。

**功能特性**:
- 自动获取进行中的活动列表
- 显示活动类型标签（折扣、优惠券、积分、特惠）
- 展示活动标题、描述、规则和时间范围
- 不同活动类型使用不同的颜色标识

**涉及文件**:
- `xunye-miniapp/src/api/customer.ts` - 添加 `ActivityVO` 接口和 `listActiveActivities()` API
- `xunye-miniapp/src/views/IndexView.vue` - 添加活动展示区域和样式

**API 接口**: `GET /api/customer/activities`

## 技术细节

### Jackson 配置
```java
@Configuration
public class JacksonConfig {
    private static final String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jsonCustomizer() {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(DATE_TIME_PATTERN);
        return builder -> {
            builder.serializerByType(LocalDateTime.class, new LocalDateTimeSerializer(dateTimeFormatter));
            builder.deserializerByType(LocalDateTime.class, new LocalDateTimeDeserializer(dateTimeFormatter));
        };
    }
}
```

### 活动类型映射
- `DISCOUNT` - 折扣活动（金色 #d2a85f）
- `COUPON` - 优惠券（红色 #ff6b6b）
- `POINTS` - 积分活动（青色 #4ecdc4）
- `SPECIAL` - 特惠单品（橙色 #ff9f43）

## 数据库变更

### V2__add_settings_column.sql
```sql
ALTER TABLE member_activity 
ADD COLUMN settings JSON NULL COMMENT '活动配置(JSON格式)' AFTER cover_image;
```

### V3__add_deleted_column_to_customer.sql
```sql
ALTER TABLE customer
ADD COLUMN deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除标记(0=未删除,1=已删除)' AFTER last_visit_at;
```

## 测试建议

1. **后端测试**:
   - 创建不同类型的活动
   - 验证日期时间格式正确序列化和反序列化
   - 测试活动列表查询接口

2. **小程序测试**:
   - 在小程序首页查看活动展示
   - 验证不同活动类型的颜色和标签
   - 测试活动时间显示格式

## 后续优化建议

1. **活动详情页**: 添加活动详情页面，点击活动卡片可查看完整信息
2. **活动筛选**: 支持按活动类型筛选
3. **活动提醒**: 活动即将结束时提醒用户
4. **活动分享**: 支持分享活动给好友
5. **活动统计**: 统计活动参与人数和效果

## Git 提交记录

- `c9af09b` - fix: configure Jackson to handle LocalDateTime with space-separated format
- `e7b5cb9` - fix: add settings column to member_activity table
- `e9109bf` - fix: add deleted column to customer table
- `e193a01` - feat: add activity display in miniapp home page
