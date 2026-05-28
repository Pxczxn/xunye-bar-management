import { describe, it, expect, vi, beforeEach } from 'vitest';
import React from 'react';
import { render, screen } from '@testing-library/react';
import { Loading } from '@/components/Loading';
import { ErrorState } from '@/components/ErrorState';

// Mock lucide-react
vi.mock('lucide-react', () => ({
  AlertCircle: (props: any) => <span data-testid="alert-icon" {...props} />,
  Loader2: (props: any) => <span data-testid="loader-icon" {...props} />,
}));

describe('FE-CMP-Loading: Loading组件测试', () => {
  it('FE-CMP-009: Loading应渲染', () => {
    const { container } = render(<Loading />);
    expect(container).toBeTruthy();
  });
});

describe('FE-CMP-ErrorState: ErrorState组件测试', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('FE-CMP-010: ErrorState应显示错误信息', () => {
    render(<ErrorState message="测试错误信息" />);
    expect(screen.getByText('测试错误信息')).toBeInTheDocument();
  });

  it('FE-CMP-010b: ErrorState应显示默认文本', () => {
    render(<ErrorState />);
    expect(screen.getByText('加载失败')).toBeInTheDocument();
  });

  it('FE-CMP-010c: ErrorState应支持重试按钮', () => {
    const onRetry = vi.fn();
    render(<ErrorState message="错误" onRetry={onRetry} />);
    const retryBtn = screen.getByText('重新尝试');
    expect(retryBtn).toBeInTheDocument();
  });
});
