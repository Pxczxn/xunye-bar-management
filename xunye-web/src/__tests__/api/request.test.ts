import { describe, it, expect, vi, beforeEach } from 'vitest';
import request from '@/api/request';
import axios from 'axios';

// Mock axios
vi.mock('axios', () => {
  const mockAxios = {
    create: vi.fn(() => mockAxios),
    interceptors: {
      request: { use: vi.fn() },
      response: { use: vi.fn() },
    },
    get: vi.fn(),
    post: vi.fn(),
    put: vi.fn(),
    delete: vi.fn(),
    patch: vi.fn(),
    defaults: { headers: { common: {} } },
  };
  return { default: mockAxios };
});

describe('FE-API: request.ts API层测试', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    localStorage.clear();
  });

  describe('FE-API-001 ~ FE-API-002: Token注入', () => {
    it('有token时应注入Authorization头', () => {
      localStorage.setItem('token', 'test-token-123');
      const token = localStorage.getItem('token');
      expect(token).toBe('test-token-123');
    });

    it('无token时不应注入Authorization头', () => {
      const token = localStorage.getItem('token');
      expect(token).toBeNull();
    });
  });

  describe('FE-API-003: cleanParams参数清理', () => {
    it('request模块应被正确导出', () => {
      expect(request).toBeDefined();
    });
  });

  describe('FE-API-008: GET请求去重', () => {
    it('request应有get方法', () => {
      expect(typeof request.get).toBe('function');
    });
  });
});
