'use client';

import { useCallback, useEffect, useMemo, useRef, useState } from 'react';
import { message } from 'antd';
import { ChefHat, Clock3, CheckCircle, RefreshCw, UtensilsCrossed } from 'lucide-react';
import { finishOrder, getOrderPage, startMaking } from '@/api/order';
import type { OrderPageVO, OrderQueryParams } from '@/types/api';

const SERVE_STATUS_TEXT: Record<string, string> = {
  PENDING: '待制作',
  MAKING: '制作中',
  FINISHED: '制作完成',
};

const SOURCE_TEXT: Record<string, string> = {
  ADMIN_POS: '吧台点单',
  CUSTOMER_MINI: '顾客扫码',
};

const COLUMN_CONFIG = [
  {
    key: 'PENDING',
    title: '待制作',
    sub: '已付款，等待吧台处理',
    icon: Clock3,
    tone: 'orange',
  },
  {
    key: 'MAKING',
    title: '制作中',
    sub: '正在出品，完成后确认送达',
    icon: ChefHat,
    tone: 'gold',
  },
  {
    key: 'FINISHED',
    title: '已完成',
    sub: '今日已确认制作完成',
    icon: CheckCircle,
    tone: 'blue',
  },
] as const;

const getSourceLabel = (source: string | null | undefined) =>
  SOURCE_TEXT[source || ''] || '吧台点单';

const formatAmount = (value: number | string) => Number(value || 0).toFixed(2);

const getMinutesSince = (createdAt: string | null | undefined) => {
  if (!createdAt) return null;
  const time = new Date(createdAt.replace(/-/g, '/')).getTime();
  if (Number.isNaN(time)) return null;
  return Math.max(0, Math.floor((Date.now() - time) / 60000));
};

function OrderCard({
  order,
  operatingId,
  onStart,
  onFinish,
}: {
  order: OrderPageVO;
  operatingId: number | null;
  onStart: (order: OrderPageVO) => void;
  onFinish: (order: OrderPageVO) => void;
}) {
  const serveStatus = order.serveStatus || 'PENDING';
  const minutes = getMinutesSince(order.createdAt);
  const disabled = operatingId === order.id;

  return (
    <article className="rounded-2xl border border-border-dark bg-card-bg/90 p-4 shadow-lg shadow-black/10 transition-colors hover:border-brand-gold/30">
      <div className="mb-3 flex items-start justify-between gap-3">
        <div>
          <p className="font-mono text-xs text-brand-gold">{order.orderNo}</p>
          <h3 className="mt-1 text-lg font-semibold text-text-main">{order.tableName || '-'}</h3>
        </div>
        <span className="rounded-full border border-brand-gold/25 bg-brand-gold/10 px-2.5 py-1 text-xs text-brand-gold">
          {getSourceLabel(order.source)}
        </span>
      </div>

      <div className="mb-3 grid grid-cols-2 gap-2 text-xs">
        <div className="rounded-xl bg-sidebar-bg px-3 py-2">
          <p className="text-text-weak">金额</p>
          <p className="mt-1 font-semibold text-brand-gold">¥{formatAmount(order.totalAmount)}</p>
        </div>
        <div className="rounded-xl bg-sidebar-bg px-3 py-2">
          <p className="text-text-weak">等待</p>
          <p className="mt-1 font-semibold text-text-main">{minutes === null ? '-' : `${minutes} 分钟`}</p>
        </div>
      </div>

      {!!order.items?.length && (
        <div className="mb-4 space-y-2 border-t border-border-dark pt-3">
          {order.items.map((item) => (
            <div key={item.id} className="flex items-start justify-between gap-3 text-sm">
              <div>
                <p className="text-text-main">{item.productName}</p>
                <p className="mt-0.5 text-xs text-text-weak">单价 ¥{formatAmount(item.price)}</p>
              </div>
              <span className="font-mono text-brand-gold">x{item.quantity}</span>
            </div>
          ))}
        </div>
      )}

      {order.remark && (
        <p className="mb-4 rounded-xl border border-orange-500/20 bg-orange-500/10 px-3 py-2 text-xs text-orange-300">
          备注：{order.remark}
        </p>
      )}

      <div className="flex items-center justify-between gap-3">
        <span className="text-xs text-text-weak">{SERVE_STATUS_TEXT[serveStatus] || serveStatus}</span>
        {serveStatus === 'PENDING' && (
          <button
            disabled={disabled}
            onClick={() => onStart(order)}
            className="rounded-lg bg-brand-gold px-3 py-2 text-sm font-semibold text-main-bg transition-colors hover:bg-brand-gold/90 disabled:cursor-not-allowed disabled:opacity-50"
          >
            开始制作
          </button>
        )}
        {serveStatus === 'MAKING' && (
          <button
            disabled={disabled}
            onClick={() => onFinish(order)}
            className="rounded-lg border border-blue-400/40 bg-blue-500/10 px-3 py-2 text-sm font-semibold text-blue-300 transition-colors hover:bg-blue-500/20 disabled:cursor-not-allowed disabled:opacity-50"
          >
            确认完成
          </button>
        )}
      </div>
    </article>
  );
}

export default function Kitchen() {
  const [orders, setOrders] = useState<OrderPageVO[]>([]);
  const [loading, setLoading] = useState(false);
  const [operatingId, setOperatingId] = useState<number | null>(null);
  const [lastUpdated, setLastUpdated] = useState<string>('-');
  const previousPendingRef = useRef(0);

  const fetchOrders = useCallback(async (silent = false) => {
    if (!silent) setLoading(true);
    try {
      const queryByServeStatus = (serveStatus: string, pageSize: number): OrderQueryParams => ({
        pageNum: 1,
        pageSize,
        status: 'PAID',
        serveStatus,
        excludeStatus: 'CANCELLED',
      });

      const [pending, making, finished] = await Promise.all([
        getOrderPage(queryByServeStatus('PENDING', 50)),
        getOrderPage(queryByServeStatus('MAKING', 50)),
        getOrderPage(queryByServeStatus('FINISHED', 30)),
      ]);

      const merged = [
        ...(pending.records || []),
        ...(making.records || []),
        ...(finished.records || []),
      ];
      setOrders(merged);
      setLastUpdated(new Date().toLocaleTimeString('zh-CN', { hour12: false }));

      const pendingCount = pending.total || 0;
      if (silent && pendingCount > previousPendingRef.current) {
        message.warning(`有新的待制作订单（${pendingCount} 单）`);
      }
      previousPendingRef.current = pendingCount;
    } catch (e: unknown) {
      message.error(e instanceof Error ? e.message : '加载出品看板失败');
    } finally {
      if (!silent) setLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchOrders();
    const timer = setInterval(() => fetchOrders(true), 10000);
    return () => clearInterval(timer);
  }, [fetchOrders]);

  const grouped = useMemo(() => {
    const map: Record<string, OrderPageVO[]> = { PENDING: [], MAKING: [], FINISHED: [] };
    orders.forEach((order) => {
      const status = order.serveStatus || 'PENDING';
      if (map[status]) map[status].push(order);
    });
    return map;
  }, [orders]);

  const handleStart = async (order: OrderPageVO) => {
    setOperatingId(order.id);
    try {
      await startMaking(order.id);
      message.success(`${order.tableName} 已开始制作`);
      fetchOrders(true);
    } catch (e: unknown) {
      message.error(e instanceof Error ? e.message : '操作失败');
    } finally {
      setOperatingId(null);
    }
  };

  const handleFinish = async (order: OrderPageVO) => {
    setOperatingId(order.id);
    try {
      await finishOrder(order.id);
      message.success(`${order.tableName} 已确认制作完成`);
      fetchOrders(true);
    } catch (e: unknown) {
      message.error(e instanceof Error ? e.message : '操作失败');
    } finally {
      setOperatingId(null);
    }
  };

  const pendingCount = grouped.PENDING.length;
  const makingCount = grouped.MAKING.length;

  return (
    <div className="min-h-screen bg-main-bg p-4 sm:p-6 lg:p-8">
      <div className="mb-6 flex flex-col gap-4 lg:flex-row lg:items-end lg:justify-between">
        <div>
          <div className="mb-2 inline-flex items-center gap-2 rounded-full border border-brand-gold/25 bg-brand-gold/10 px-3 py-1 text-xs text-brand-gold">
            <UtensilsCrossed size={14} />
            出品工作台
          </div>
          <h1 className="text-2xl font-serif font-bold tracking-wider text-text-main">待制作看板</h1>
          <p className="mt-1 text-xs uppercase tracking-[0.24em] text-text-weak">Kitchen Workflow</p>
        </div>

        <div className="flex flex-wrap items-center gap-3">
          <div className="rounded-xl border border-border-dark bg-card-bg px-4 py-2">
            <p className="text-[10px] uppercase tracking-wider text-text-weak">待制作</p>
            <p className="mt-0.5 text-lg font-semibold text-orange-300">{pendingCount}</p>
          </div>
          <div className="rounded-xl border border-border-dark bg-card-bg px-4 py-2">
            <p className="text-[10px] uppercase tracking-wider text-text-weak">制作中</p>
            <p className="mt-0.5 text-lg font-semibold text-brand-gold">{makingCount}</p>
          </div>
          <button
            onClick={() => fetchOrders()}
            disabled={loading}
            className="inline-flex items-center gap-2 rounded-xl border border-border-dark bg-card-bg px-4 py-3 text-sm text-text-sub transition-colors hover:border-brand-gold/50 hover:text-brand-gold disabled:cursor-not-allowed disabled:opacity-50"
          >
            <RefreshCw size={16} className={loading ? 'animate-spin' : ''} />
            刷新
          </button>
        </div>
      </div>

      <div className="mb-4 rounded-xl border border-border-dark bg-card-bg/70 px-4 py-3 text-xs text-text-weak">
        自动每 10 秒刷新一次，最后更新：<span className="font-mono text-text-main">{lastUpdated}</span>
      </div>

      <div className="grid gap-4 xl:grid-cols-3">
        {COLUMN_CONFIG.map((column) => {
          const Icon = column.icon;
          const list = grouped[column.key] || [];

          return (
            <section key={column.key} className="rounded-2xl border border-border-dark bg-sidebar-bg/40 p-3">
              <div className="mb-3 flex items-center justify-between px-1">
                <div className="flex items-center gap-2">
                  <span className="flex h-9 w-9 items-center justify-center rounded-xl border border-brand-gold/25 bg-brand-gold/10 text-brand-gold">
                    <Icon size={18} />
                  </span>
                  <div>
                    <h2 className="text-sm font-semibold text-text-main">{column.title}</h2>
                    <p className="text-xs text-text-weak">{column.sub}</p>
                  </div>
                </div>
                <span className="rounded-full bg-card-bg px-2.5 py-1 text-xs font-semibold text-text-main">{list.length}</span>
              </div>

              <div className="space-y-3">
                {list.length === 0 ? (
                  <div className="rounded-2xl border border-dashed border-border-dark px-4 py-12 text-center text-sm text-text-weak">
                    暂无订单
                  </div>
                ) : (
                  list.map((order) => (
                    <OrderCard
                      key={order.id}
                      order={order}
                      operatingId={operatingId}
                      onStart={handleStart}
                      onFinish={handleFinish}
                    />
                  ))
                )}
              </div>
            </section>
          );
        })}
      </div>
    </div>
  );
}
