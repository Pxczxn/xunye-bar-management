# Ant Design Message 修复报告

## 修复内容
将所有页面组件中的静态 `message` 调用改为使用 `App.useApp()` hook，以支持动态主题和消除警告。

## 修复的文件 (17个)
1. ✅ App.tsx - 添加 `<App>` 组件包裹
2. ✅ Activities/index.tsx
3. ✅ Categories/index.tsx
4. ✅ CouponTemplates/index.tsx
5. ✅ DiscountRules/index.tsx
6. ✅ Inventory/index.tsx
7. ✅ Kitchen/index.tsx
8. ✅ Login/index.tsx
9. ✅ MemberLevelConfigs/index.tsx
10. ✅ Members/index.tsx
11. ✅ Orders/index.tsx
12. ✅ Pos/index.tsx
13. ✅ Products/index.tsx
14. ✅ Settings/index.tsx
15. ✅ Staff/index.tsx
16. ✅ Tables/index.tsx

## 修复的警告
- ❌ `[antd: Modal] destroyOnClose is deprecated. Please use destroyOnHidden instead.`
- ❌ `[antd: message] Static function can not consume context like dynamic theme. Please use 'App' component instead.`

## 修改说明
### 1. App.tsx
```tsx
// 添加 App 组件
import { App as AntdApp } from 'antd';

<AntdApp>
  <RouterProvider router={router} />
</AntdApp>
```

### 2. 各页面组件
```tsx
// 修改前
import { message } from 'antd';

// 修改后
import { App } from 'antd';

const ComponentName = () => {
  const { message } = App.useApp();
  // ... 其他代码
}
```

### 3. Modal 组件
```tsx
// 修改前
<Modal destroyOnClose>

// 修改后
<Modal destroyOnHidden>
```

## 测试建议
1. 刷新浏览器，确认控制台警告消失
2. 测试所有页面的 message 提示功能是否正常
3. 测试 Modal 弹窗的销毁行为是否正常

## 日期
2026-05-26
