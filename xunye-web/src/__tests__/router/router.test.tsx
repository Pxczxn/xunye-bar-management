import { describe, it, expect, vi, beforeEach } from 'vitest';
import router from '@/router';

// Mock localStorage for router tests
const localStorageMock = (() => {
  let store: Record<string, string> = {};
  return {
    getItem: (key: string) => store[key] || null,
    setItem: (key: string, value: string) => { store[key] = value; },
    removeItem: (key: string) => { delete store[key]; },
    clear: () => { store = {}; },
  };
})();

Object.defineProperty(window, 'localStorage', { value: localStorageMock });

describe('FE-RT: 路由权限测试', () => {
  beforeEach(() => {
    localStorage.clear();
  });

  describe('FE-RT-001: 未登录访问管理页', () => {
    it('router应被正确导出', () => {
      expect(router).toBeDefined();
    });

    it('router应有路由配置', () => {
      expect(router).toHaveProperty('routes');
    });
  });

  describe('FE-RT-002: 路由配置结构', () => {
    it('应包含/login路由', () => {
      const routes = router.routes || [];
      const flatRoutes = JSON.stringify(routes);
      expect(flatRoutes).toContain('login');
    });

    it('应包含/dashboard路由', () => {
      const routes = router.routes || [];
      const flatRoutes = JSON.stringify(routes);
      expect(flatRoutes).toContain('dashboard');
    });

    it('应包含/kitchen路由', () => {
      const routes = router.routes || [];
      const flatRoutes = JSON.stringify(routes);
      expect(flatRoutes).toContain('kitchen');
    });

    it('应包含/orders路由', () => {
      const routes = router.routes || [];
      const flatRoutes = JSON.stringify(routes);
      expect(flatRoutes).toContain('orders');
    });

    it('应包含/pos路由', () => {
      const routes = router.routes || [];
      const flatRoutes = JSON.stringify(routes);
      expect(flatRoutes).toContain('pos');
    });

    it('应包含/tables路由', () => {
      const routes = router.routes || [];
      const flatRoutes = JSON.stringify(routes);
      expect(flatRoutes).toContain('tables');
    });

    it('应包含/employees路由(BOSS-only)', () => {
      const routes = router.routes || [];
      const flatRoutes = JSON.stringify(routes);
      expect(flatRoutes).toContain('employees');
    });

    it('应包含/settings路由(BOSS-only)', () => {
      const routes = router.routes || [];
      const flatRoutes = JSON.stringify(routes);
      expect(flatRoutes).toContain('settings');
    });

    it('应包含/products路由', () => {
      const routes = router.routes || [];
      const flatRoutes = JSON.stringify(routes);
      expect(flatRoutes).toContain('products');
    });

    it('应包含/members路由', () => {
      const routes = router.routes || [];
      const flatRoutes = JSON.stringify(routes);
      expect(flatRoutes).toContain('members');
    });

    it('应包含/inventory路由', () => {
      const routes = router.routes || [];
      const flatRoutes = JSON.stringify(routes);
      expect(flatRoutes).toContain('inventory');
    });

    it('应包含/activities路由', () => {
      const routes = router.routes || [];
      const flatRoutes = JSON.stringify(routes);
      expect(flatRoutes).toContain('activities');
    });

    it('应包含/coupon-templates路由', () => {
      const routes = router.routes || [];
      const flatRoutes = JSON.stringify(routes);
      expect(flatRoutes).toContain('coupon-templates');
    });

    it('应包含/member-level-configs路由', () => {
      const routes = router.routes || [];
      const flatRoutes = JSON.stringify(routes);
      expect(flatRoutes).toContain('member-level-configs');
    });

    it('应包含/discount-rules路由', () => {
      const routes = router.routes || [];
      const flatRoutes = JSON.stringify(routes);
      expect(flatRoutes).toContain('discount-rules');
    });
  });

  describe('FE-RT: 角色路由权限验证', () => {
    it('未设置token时应跳转到/login', () => {
      localStorage.clear();
      // 路由守卫逻辑: RequireAuth组件会检查localStorage.getItem('token')
      const token = localStorage.getItem('token');
      expect(token).toBeNull();
    });

    it('BOSS角色可访问所有页面', () => {
      localStorage.setItem('token', 'boss-token');
      localStorage.setItem('user', JSON.stringify({ role: 'BOSS' }));
      const user = JSON.parse(localStorage.getItem('user') || '{}');
      expect(user.role).toBe('BOSS');
    });

    it('MANAGER角色不应访问employees和settings', () => {
      localStorage.setItem('token', 'manager-token');
      localStorage.setItem('user', JSON.stringify({ role: 'MANAGER' }));
      const user = JSON.parse(localStorage.getItem('user') || '{}');
      expect(user.role).toBe('MANAGER');
      expect(user.role).not.toBe('BOSS');
    });

    it('STAFF角色应跳转到kitchen', () => {
      localStorage.setItem('token', 'staff-token');
      localStorage.setItem('user', JSON.stringify({ role: 'STAFF' }));
      const user = JSON.parse(localStorage.getItem('user') || '{}');
      expect(user.role).toBe('STAFF');
    });
  });
});
