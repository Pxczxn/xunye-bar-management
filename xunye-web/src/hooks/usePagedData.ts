import { useState, useCallback, useEffect } from 'react';
import { message } from 'antd';

interface UsePagedDataOptions<T, F> {
  fetchFn: (params: F & { pageNum: number; pageSize: number }) => Promise<{ records: T[]; total: number }>;
  filters?: F;
  initialPageSize?: number;
  autoFetch?: boolean;
}

interface UsePagedDataResult<T, F> {
  data: T[];
  total: number;
  loading: boolean;
  pageNum: number;
  pageSize: number;
  setPageNum: (page: number) => void;
  setPageSize: (size: number) => void;
  fetchData: (page?: number, size?: number) => Promise<void>;
  refresh: () => void;
  updateFilters: (newFilters: Partial<F>) => void;
}

/**
 * 通用分页数据获取 Hook
 * @param options 配置选项
 * @returns 分页数据和操作方法
 */
export function usePagedData<T = any, F = Record<string, any>>(
  options: UsePagedDataOptions<T, F>
): UsePagedDataResult<T, F> {
  const {
    fetchFn,
    filters: initialFilters,
    initialPageSize = 10,
    autoFetch = true,
  } = options;

  const [data, setData] = useState<T[]>([]);
  const [total, setTotal] = useState(0);
  const [loading, setLoading] = useState(false);
  const [pageNum, setPageNum] = useState(1);
  const [pageSize, setPageSize] = useState(initialPageSize);
  const [filters, setFilters] = useState<F | undefined>(initialFilters);

  const fetchData = useCallback(
    async (page = pageNum, size = pageSize) => {
      setLoading(true);
      try {
        const params = {
          pageNum: page,
          pageSize: size,
          ...(filters || {}),
        } as F & { pageNum: number; pageSize: number };

        const res = await fetchFn(params);
        setData(res?.records ?? []);
        setTotal(res?.total ?? 0);
      } catch (err: any) {
        message.error(err.message || '获取列表失败');
        setData([]);
        setTotal(0);
      } finally {
        setLoading(false);
      }
    },
    [pageNum, pageSize, filters, fetchFn]
  );

  const refresh = useCallback(() => {
    fetchData(pageNum, pageSize);
  }, [fetchData, pageNum, pageSize]);

  const updateFilters = useCallback((newFilters: Partial<F>) => {
    setFilters((prev) => ({ ...prev, ...newFilters } as F));
    setPageNum(1);
  }, []);

  useEffect(() => {
    if (autoFetch) {
      fetchData();
    }
  }, [fetchData, autoFetch]);

  return {
    data,
    total,
    loading,
    pageNum,
    pageSize,
    setPageNum,
    setPageSize,
    fetchData,
    refresh,
    updateFilters,
  };
}
