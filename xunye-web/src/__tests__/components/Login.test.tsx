import { describe, it, expect, vi, beforeEach } from 'vitest';
import React from 'react';
import { render, screen } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';

// Mock antd App.useApp
vi.mock('antd', async () => {
  const actual = await vi.importActual('antd');
  return {
    ...actual,
    App: Object.assign(
      ({ children }: any) => <div>{children}</div>,
      {
        useApp: () => ({
          message: { success: vi.fn(), error: vi.fn(), warning: vi.fn() },
          notification: { success: vi.fn(), error: vi.fn() },
        }),
      }
    ),
  };
});

// Mock auth API
vi.mock('@/api/auth', () => ({
  login: vi.fn().mockResolvedValue({ token: 'test-token', user: { role: 'BOSS' } }),
}));

import LoginPage from '@/pages/Login';

describe('FE-CMP-Login: 登录页面组件测试', () => {
  beforeEach(() => {
    localStorage.clear();
    vi.clearAllMocks();
  });

  it('FE-CMP-001: 登录页面应渲染标题', () => {
    render(
      <MemoryRouter>
        <LoginPage />
      </MemoryRouter>
    );
    expect(screen.getByText('寻野酒吧管理系统')).toBeInTheDocument();
  });

  it('FE-CMP-002: 应有用户名输入框', () => {
    render(
      <MemoryRouter>
        <LoginPage />
      </MemoryRouter>
    );
    const input = screen.getByPlaceholderText('请输入用户名');
    expect(input).toBeInTheDocument();
  });

  it('FE-CMP-003: 应有密码输入框', () => {
    render(
      <MemoryRouter>
        <LoginPage />
      </MemoryRouter>
    );
    const input = screen.getByPlaceholderText('请输入密码');
    expect(input).toBeInTheDocument();
  });

  it('FE-CMP-004: 应有登录按钮', () => {
    render(
      <MemoryRouter>
        <LoginPage />
      </MemoryRouter>
    );
    expect(screen.getByText('进入系统')).toBeInTheDocument();
  });

  it('FE-CMP-005: 应有副标题', () => {
    render(
      <MemoryRouter>
        <LoginPage />
      </MemoryRouter>
    );
    expect(screen.getByText('Bar Management')).toBeInTheDocument();
  });
});
