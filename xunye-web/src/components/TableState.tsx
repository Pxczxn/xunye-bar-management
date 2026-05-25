import React from 'react';

interface TableStateProps {
  loading: boolean;
  empty: boolean;
  error?: string | null;
  colSpan: number;
  loadingText?: string;
  emptyText?: string;
  children?: React.ReactNode;
}

/**
 * 表格状态组件 - 统一处理加载、空状态、错误状态
 */
export default function TableState({
  loading,
  empty,
  error,
  colSpan,
  loadingText = '加载中...',
  emptyText = '暂无数据',
  children,
}: TableStateProps) {
  if (loading) {
    return (
      <tr>
        <td colSpan={colSpan} className="px-6 py-16 text-center text-text-weak">
          <div className="flex items-center justify-center space-x-2">
            <div className="w-4 h-4 border-2 border-brand-gold border-t-transparent rounded-full animate-spin" />
            <span>{loadingText}</span>
          </div>
        </td>
      </tr>
    );
  }

  if (error) {
    return (
      <tr>
        <td colSpan={colSpan} className="px-6 py-16 text-center">
          <div className="flex flex-col items-center space-y-2">
            <span className="text-danger">⚠️ {error}</span>
          </div>
        </td>
      </tr>
    );
  }

  if (empty) {
    return (
      <tr>
        <td colSpan={colSpan} className="px-6 py-16 text-center text-text-weak font-serif italic">
          {emptyText}
        </td>
      </tr>
    );
  }

  return <>{children}</>;
}
