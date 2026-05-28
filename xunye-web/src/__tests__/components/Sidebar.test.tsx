import { describe, it, expect, vi, beforeEach } from 'vitest';
import React from 'react';
import { render, screen } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { Sidebar } from '@/components/Sidebar';

// Mock lucide-react - 每个图标返回一个简单span
vi.mock('lucide-react', () => {
  const mockIcon = (name: string) => (props: any) =>
    React.createElement('span', { 'data-testid': `icon-${name}`, ...props });
  return {
    LayoutDashboard: mockIcon('LayoutDashboard'),
    Coffee: mockIcon('Coffee'),
    ChefHat: mockIcon('ChefHat'),
    Receipt: mockIcon('Receipt'),
    Wine: mockIcon('Wine'),
    Tags: mockIcon('Tags'),
    Archive: mockIcon('Archive'),
    LayoutGrid: mockIcon('LayoutGrid'),
    Users: mockIcon('Users'),
    Settings: mockIcon('Settings'),
    Crown: mockIcon('Crown'),
    Gift: mockIcon('Gift'),
    Ticket: mockIcon('Ticket'),
    Award: mockIcon('Award'),
    Percent: mockIcon('Percent'),
    ChevronDown: mockIcon('ChevronDown'),
    ChevronRight: mockIcon('ChevronRight'),
  };
});

const renderSidebar = (role: string) => {
  localStorage.setItem('token', 'test-token');
  localStorage.setItem('user', JSON.stringify({ role, nickname: '测试用户' }));

  return render(
    <MemoryRouter initialEntries={['/dashboard']}>
      <Sidebar />
    </MemoryRouter>
  );
};

describe('FE-CMP-Sidebar: 侧边栏组件测试', () => {
  beforeEach(() => {
    localStorage.clear();
    vi.clearAllMocks();
  });

  describe('FE-CMP-005: BOSS显示所有菜单', () => {
    it('BOSS角色应能看到仪表盘菜单', () => {
      renderSidebar('BOSS');
      expect(screen.getByText('营业看板')).toBeInTheDocument();
    });

    it('BOSS应能看到员工账号菜单', () => {
      renderSidebar('BOSS');
      expect(screen.getByText('员工账号')).toBeInTheDocument();
    });

    it('BOSS应能看到系统设置菜单', () => {
      renderSidebar('BOSS');
      expect(screen.getByText('系统设置')).toBeInTheDocument();
    });
  });

  describe('FE-CMP-006: STAFF显示受限菜单', () => {
    it('STAFF角色不应看到员工账号', () => {
      renderSidebar('STAFF');
      expect(screen.queryByText('员工账号')).not.toBeInTheDocument();
    });

    it('STAFF不应看到系统设置', () => {
      renderSidebar('STAFF');
      expect(screen.queryByText('系统设置')).not.toBeInTheDocument();
    });

    it('STAFF不应看到营业看板', () => {
      renderSidebar('STAFF');
      expect(screen.queryByText('营业看板')).not.toBeInTheDocument();
    });
  });

  describe('FE-CMP-007: MANAGER显示管理菜单', () => {
    it('MANAGER应能看到营业看板', () => {
      renderSidebar('MANAGER');
      expect(screen.getByText('营业看板')).toBeInTheDocument();
    });

    it('MANAGER不应看到员工账号', () => {
      renderSidebar('MANAGER');
      expect(screen.queryByText('员工账号')).not.toBeInTheDocument();
    });

    it('MANAGER不应看到系统设置', () => {
      renderSidebar('MANAGER');
      expect(screen.queryByText('系统设置')).not.toBeInTheDocument();
    });
  });

  describe('FE-CMP: 退出登录', () => {
    it('应显示退出登录按钮', () => {
      renderSidebar('BOSS');
      expect(screen.getByText('退出登录')).toBeInTheDocument();
    });
  });
});
