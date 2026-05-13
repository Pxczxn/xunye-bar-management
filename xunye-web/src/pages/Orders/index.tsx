'use client';

import { useState, useEffect, useCallback, useRef } from 'react';
import { Select, Modal, message } from 'antd';
import { Search, RotateCcw, RefreshCw, Eye, CreditCard, XCircle, CheckCircle } from 'lucide-react';
import { getOrderPage, getOrderDetail, payOrder, cancelOrder, finishOrder, startMaking } from '@/api/order';
import type { OrderPageVO } from '@/types/api';

const darkSelectProps = {
  className: 'xunye-select',
  classNames: {
    popup: { root: 'xunye-select-dropdown' },
  },
  styles: {
    root: {
      width: '100%',
      backgroundColor: '#101014',
      border: '1px solid #2A2A31',
    },
    content: { color: '#F4EBDD' },
    suffix: { color: '#AFA79B' },
    popup: {
      root: {
        backgroundColor: '#1A1A1F',
        border: '1px solid #2A2A31',
      },
    },
  },
} as const;

const serveStatusText: Record<string, string> = {
  PENDING: '待处理',
  MAKING: '制作中',
  FINISHED: '制作完成',
};

const statusText: Record<string, string> = {
  UNPAID: '未支付',
  PAID: '已支付',
  CANCELLED: '已取消',
  FINISHED: '已完成',
};

const payMethodText: Record<string, string> = {
  WECHAT: '微信',
  ALIPAY: '支付宝',
  CASH: '现金',
};

const getSourceLabel = (source: string | null | undefined) => {
  switch (source) {
    case 'ADMIN_POS': return '吧台点单';
    case 'CUSTOMER_MINI': return '顾客扫码';
    default: return '吧台点单';
  }
};

export default function Orders() {
  const [records, setRecords] = useState<OrderPageVO[]>([]);
  const [total, setTotal] = useState(0);
  const [loading, setLoading] = useState(false);
  const [pageNum, setPageNum] = useState(1);
  const [pageSize, setPageSize] = useState(10);
  const [filterOrderNo, setFilterOrderNo] = useState('');
  const [filterTableName, setFilterTableName] = useState('');
  const [filterStatus, setFilterStatus] = useState<string | undefined>();

  const [queryOrderNo, setQueryOrderNo] = useState('');
  const [queryTableName, setQueryTableName] = useState('');
  const [queryStatus, setQueryStatus] = useState<string | undefined>();
  const [querySource, setQuerySource] = useState<string | undefined>();
  const [queryServeStatus, setQueryServeStatus] = useState<string | undefined>();
  const [queryExcludeStatus, setQueryExcludeStatus] = useState<string | undefined>();

  const [error, setError] = useState<string | null>(null);

  const [detailOpen, setDetailOpen] = useState(false);
  const [detailOrder, setDetailOrder] = useState<OrderPageVO | null>(null);
  const [detailLoading, setDetailLoading] = useState(false);

  const [payOpen, setPayOpen] = useState(false);
  const [payOrder_, setPayOrder_] = useState<OrderPageVO | null>(null);
  const [payMethod, setPayMethod] = useState<'WECHAT' | 'ALIPAY' | 'CASH' | undefined>();
  const [paying, setPaying] = useState(false);

  const [pendingCount, setPendingCount] = useState(0);
  const prevPendingRef = useRef(0);

  const fetchOrders = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const params: Record<string, unknown> = { pageNum, pageSize };
      if (queryOrderNo) params.orderNo = queryOrderNo;
      if (queryTableName) params.tableName = queryTableName;
      if (queryStatus) params.status = queryStatus;
      if (querySource) params.source = querySource;
      if (queryServeStatus) params.serveStatus = queryServeStatus;
      if (queryExcludeStatus) params.excludeStatus = queryExcludeStatus;
      const res = await getOrderPage(params as never);
      setRecords(res.records || []);
      setTotal(res.total || 0);
    } catch (e: unknown) {
      setError(e instanceof Error ? e.message : '加载失败');
    } finally {
      setLoading(false);
    }
  }, [pageNum, pageSize, queryOrderNo, queryTableName, queryStatus, querySource, queryServeStatus, queryExcludeStatus]);

  const fetchPendingCount = useCallback(async () => {
    try {
      const res = await getOrderPage({ pageNum: 1, pageSize: 1, source: 'CUSTOMER_MINI', status: 'PAID', serveStatus: 'PENDING', excludeStatus: 'CANCELLED' } as never);
      const count = res.total || 0;
      if (count > prevPendingRef.current) {
        message.warning({
          content: `有新的顾客订单待处理（共 ${count} 单）`,
          duration: 5,
        });
      }
      prevPendingRef.current = count;
      setPendingCount(count);
    } catch { /* 静默失败 */ }
  }, []);

  useEffect(() => {
    fetchPendingCount();
    const timer = setInterval(fetchPendingCount, 10000);
    return () => clearInterval(timer);
  }, [fetchPendingCount]);

  useEffect(() => { fetchOrders(); }, [fetchOrders]);

  const handleRefreshOrders = () => {
    fetchOrders();
    fetchPendingCount();
  };

  const handleQuery = () => {
    setQueryOrderNo(filterOrderNo);
    setQueryTableName(filterTableName);
    setQueryStatus(filterStatus);
    setPageNum(1);
  };

  const handleReset = () => {
    setFilterOrderNo('');
    setFilterTableName('');
    setFilterStatus(undefined);
    setQueryOrderNo('');
    setQueryTableName('');
    setQueryStatus(undefined);
    setQuerySource(undefined);
    setQueryServeStatus(undefined);
    setQueryExcludeStatus(undefined);
    setPageNum(1);
  };

  const openDetail = async (order: OrderPageVO) => {
    setDetailOpen(true);
    setDetailLoading(true);
    try {
      const data = await getOrderDetail(order.id);
      setDetailOrder(data);
    } catch {
      setDetailOrder(order);
    } finally {
      setDetailLoading(false);
    }
  };

  const refreshDetailOrder = async () => {
    if (!detailOrder) return;
    setDetailLoading(true);
    try {
      const data = await getOrderDetail(detailOrder.id);
      setDetailOrder(data);
      message.success('订单状态已刷新');
    } catch (e: unknown) {
      message.error(e instanceof Error ? e.message : '刷新失败');
    } finally {
      setDetailLoading(false);
    }
  };

  const openPay = (order: OrderPageVO) => {
    setPayOrder_(order);
    setPayMethod(undefined);
    setPayOpen(true);
  };

  const handlePay = async () => {
    if (!payOrder_ || !payMethod) return;
    setPaying(true);
    try {
      await payOrder(payOrder_.id, { paymentMethod: payMethod });
      message.success('收款成功');
      setPayOpen(false);
      fetchOrders();
    } catch (e: unknown) {
      message.error(e instanceof Error ? e.message : '收款失败');
    } finally {
      setPaying(false);
    }
  };

  const handleCancel = (order: OrderPageVO) => {
    Modal.confirm({
      title: '确认取消订单',
      content: `确定要取消订单 ${order.orderNo} 吗？取消后库存将自动恢复。`,
      okText: '确认取消',
      cancelText: '返回',
      rootClassName: 'xunye-order-confirm-modal',
      okButtonProps: { danger: true },
      onOk: async () => {
        try {
          await cancelOrder(order.id);
          message.success('订单已取消');
          fetchOrders();
        } catch (e: unknown) {
          message.error(e instanceof Error ? e.message : '取消失败');
        }
      },
    });
  };

  const handleStartMaking = (order: OrderPageVO) => {
    Modal.confirm({
      title: '确认开始制作',
      content: `确定要开始制作订单 ${order.orderNo} 吗？`,
      okText: '确认',
      cancelText: '返回',
      rootClassName: 'xunye-order-confirm-modal',
      onOk: async () => {
        try {
          await startMaking(order.id);
          message.success('已开始制作');
          fetchOrders();
          fetchPendingCount();
          if (detailOpen && detailOrder?.id === order.id) {
            const data = await getOrderDetail(order.id);
            setDetailOrder(data);
          }
        } catch (e: unknown) {
          message.error(e instanceof Error ? e.message : '操作失败');
        }
      },
    });
  };

  const handleFinish = (order: OrderPageVO) => {
    Modal.confirm({
      title: '确认制作完成',
      content: `确定订单 ${order.orderNo} 已制作完成并送达桌台吗？`,
      okText: '确认完成',
      cancelText: '返回',
      rootClassName: 'xunye-order-confirm-modal',
      onOk: async () => {
        try {
          await finishOrder(order.id);
          message.success('已确认制作完成');
          fetchOrders();
          if (detailOpen && detailOrder?.id === order.id) {
            const data = await getOrderDetail(order.id);
            setDetailOrder(data);
          }
        } catch (e: unknown) {
          message.error(e instanceof Error ? e.message : '操作失败');
        }
      },
    });
  };

  const totalPages = Math.ceil(total / pageSize);

  const modalStyles = {
    header: { background: '#1a1a2e', borderBottom: '1px solid rgba(214,168,90,0.2)' },
    body: { background: '#1a1a2e' },
    content: { background: '#1a1a2e', border: '1px solid rgba(214,168,90,0.3)' },
  };

  return (
    <div className="min-h-screen bg-main-bg p-4 sm:p-6 lg:p-8">
      <div className="mb-6 flex items-center justify-between">
        <div>
          <h1 className="text-xl font-serif text-text-main flex items-center gap-2">
            <CreditCard className="text-brand-gold" size={24} />
            订单流水
          </h1>
          <p className="text-xs text-text-weak mt-1 font-mono uppercase tracking-wider">ORDERS</p>
        </div>
        <div className="flex gap-2">
          {pendingCount > 0 && (
            <button
              onClick={() => {
                setFilterOrderNo(''); setFilterTableName(''); setFilterStatus(undefined);
                setQueryOrderNo(''); setQueryTableName(''); setQueryStatus('PAID');
                setPageNum(1);
                setQuerySource('CUSTOMER_MINI'); setQueryServeStatus('PENDING'); setQueryExcludeStatus('CANCELLED');
              }}
              className="flex items-center gap-1.5 px-3 py-2 rounded-lg bg-orange-500/10 border border-orange-500/30 text-orange-400 text-sm hover:bg-orange-500/20 transition-colors"
            >
              待处理顾客订单 {pendingCount}
            </button>
          )}
          <button
            onClick={handleRefreshOrders}
            className="p-2 rounded-lg bg-card-bg border border-border-dark text-text-sub hover:text-brand-gold hover:border-brand-gold/50 transition-colors"
            title="刷新"
          >
            <RefreshCw size={16} />
          </button>
        </div>
      </div>

      <div className="mb-4 bg-card-bg border border-border-dark rounded-xl p-4 flex flex-wrap gap-3 items-center">
        <input
          value={filterOrderNo}
          onChange={(e) => setFilterOrderNo(e.target.value)}
          placeholder="订单号"
          className="bg-sidebar-bg border border-border-dark rounded-lg px-3 py-2 text-sm text-text-main placeholder-text-weak/50 focus:outline-none focus:border-brand-gold/50 w-40"
        />
        <input
          value={filterTableName}
          onChange={(e) => setFilterTableName(e.target.value)}
          placeholder="桌台名"
          className="bg-sidebar-bg border border-border-dark rounded-lg px-3 py-2 text-sm text-text-main placeholder-text-weak/50 focus:outline-none focus:border-brand-gold/50 w-32"
        />
        <Select
          value={filterStatus}
          onChange={v => setFilterStatus(v)}
          allowClear
          placeholder="订单状态"
          className="!w-36"
          size="large"
          {...darkSelectProps}
        >
          <Select.Option value="UNPAID">未支付</Select.Option>
          <Select.Option value="PAID">已支付</Select.Option>
          <Select.Option value="CANCELLED">已取消</Select.Option>
          <Select.Option value="FINISHED">已完成</Select.Option>
        </Select>
        <button
          onClick={handleQuery}
          className="flex items-center gap-1.5 bg-brand-gold hover:bg-brand-gold/90 text-main-bg font-semibold px-4 py-2 rounded-lg text-sm transition-colors"
        >
          <Search size={14} /> 查询
        </button>
        <button
          onClick={handleReset}
          className="flex items-center gap-1.5 bg-card-bg border border-border-dark text-text-sub hover:text-brand-gold px-4 py-2 rounded-lg text-sm transition-colors"
        >
          <RotateCcw size={14} /> 重置
        </button>
      </div>

      <div className="bg-card-bg border border-border-dark rounded-xl overflow-hidden shadow-xl">
        <div className="overflow-x-auto">
          <table className="w-full">
            <thead>
              <tr className="bg-sidebar-bg border-b border-border-dark">
                <th className="text-left py-3 px-4 text-xs font-mono text-text-weak uppercase tracking-wider">订单号</th>
                <th className="text-left py-3 px-4 text-xs font-mono text-text-weak uppercase tracking-wider">订单来源</th>
                <th className="text-left py-3 px-4 text-xs font-mono text-text-weak uppercase tracking-wider">桌台</th>
                <th className="text-left py-3 px-4 text-xs font-mono text-text-weak uppercase tracking-wider">总金额</th>
                <th className="text-left py-3 px-4 text-xs font-mono text-text-weak uppercase tracking-wider">支付状态</th>
                <th className="text-left py-3 px-4 text-xs font-mono text-text-weak uppercase tracking-wider">制作状态</th>
                <th className="text-left py-3 px-4 text-xs font-mono text-text-weak uppercase tracking-wider">支付方式</th>
                <th className="text-left py-3 px-4 text-xs font-mono text-text-weak uppercase tracking-wider">创建时间</th>
                <th className="text-right py-3 px-4 text-xs font-mono text-text-weak uppercase tracking-wider">操作</th>
              </tr>
            </thead>
            <tbody>
              {loading ? (
                <tr>
                  <td colSpan={8} className="text-center py-20">
                    <div className="flex items-center justify-center gap-3">
                      <div className="w-5 h-5 border-2 border-brand-gold border-t-transparent rounded-full animate-spin" />
                      <span className="text-text-weak text-sm font-mono">加载中...</span>
                    </div>
                  </td>
                </tr>
              ) : error ? (
                <tr>
                  <td colSpan={8} className="text-center py-20">
                    <div className="flex flex-col items-center gap-3">
                      <span className="text-red-400 text-sm">{error}</span>
                      <button
                        onClick={handleRefreshOrders}
                        className="px-4 py-1.5 bg-brand-gold/20 text-brand-gold rounded-lg text-sm hover:bg-brand-gold/30 transition-colors"
                      >
                        重试
                      </button>
                    </div>
                  </td>
                </tr>
              ) : records.length === 0 ? (
                <tr>
                  <td colSpan={8} className="text-center py-20 text-text-weak text-sm font-serif italic">
                    暂无订单数据
                  </td>
                </tr>
              ) : (
                records.map((r) => (
                  <tr key={r.id} className="border-b border-border-dark/50 hover:bg-sidebar-bg/50 transition-colors">
                    <td className="py-3 px-4 text-xs font-mono text-brand-gold">{r.orderNo}</td>
                    <td className="py-3 px-4">
                      <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs ${
                        r.source === 'CUSTOMER_MINI' ? 'bg-yellow-500/10 text-yellow-400' : 'bg-brand-gold/10 text-brand-gold'
                      }`}>
                        {getSourceLabel(r.source)}
                      </span>
                    </td>
                    <td className="py-3 px-4 text-sm text-text-sub">{r.tableName}</td>
                    <td className="py-3 px-4 text-sm font-semibold text-brand-gold">
                      ¥{Number(r.totalAmount).toFixed(2)}
                    </td>
                    <td className="py-3 px-4">
                      <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs ${
                        r.status === 'PAID' ? 'bg-green-500/10 text-green-400' :
                        r.status === 'UNPAID' ? 'bg-yellow-500/10 text-yellow-400' :
                        r.status === 'FINISHED' ? 'bg-blue-500/10 text-blue-400' :
                        'bg-gray-500/10 text-gray-400'
                      }`}>
                        {statusText[r.status] || r.status}
                      </span>
                    </td>
                    <td className="py-3 px-4">
                      <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs ${
                        (r.serveStatus || 'PENDING') === 'FINISHED' ? 'bg-blue-500/10 text-blue-400' :
                        (r.serveStatus || 'PENDING') === 'MAKING' ? 'bg-orange-500/10 text-orange-400' :
                        'bg-gray-500/10 text-gray-400'
                      }`}>
                        {serveStatusText[r.serveStatus || 'PENDING'] || '待处理'}
                      </span>
                    </td>
                    <td className="py-3 px-4 text-sm text-text-sub">
                      {r.paymentMethod ? (payMethodText[r.paymentMethod] || r.paymentMethod) : '-'}
                    </td>
                    <td className="py-3 px-4 text-xs font-mono text-text-weak">{r.createdAt}</td>
                    <td className="py-3 px-4 text-right">
                      <div className="flex items-center justify-end gap-1">
                        <button
                          onClick={() => openDetail(r)}
                          className="p-1.5 rounded-lg hover:bg-brand-gold/10 text-text-weak hover:text-brand-gold transition-colors"
                          title="查看详情"
                        >
                          <Eye size={15} />
                        </button>
                        {r.status === 'UNPAID' && (
                          <>
                            <button
                              onClick={() => openPay(r)}
                              className="p-1.5 rounded-lg hover:bg-green-500/10 text-text-weak hover:text-green-400 transition-colors"
                              title="收款"
                            >
                              <CreditCard size={15} />
                            </button>
                            <button
                              onClick={() => handleCancel(r)}
                              className="p-1.5 rounded-lg hover:bg-red-500/10 text-text-weak hover:text-red-400 transition-colors"
                              title="取消"
                            >
                              <XCircle size={15} />
                            </button>
                          </>
                        )}
                        {r.status === 'PAID' && (r.serveStatus || 'PENDING') === 'PENDING' && (
                          <button
                            onClick={() => handleStartMaking(r)}
                            className="p-1.5 rounded-lg hover:bg-orange-500/10 text-text-weak hover:text-orange-400 transition-colors"
                            title="开始制作"
                          >
                            <CheckCircle size={15} />
                          </button>
                        )}
                        {r.status === 'PAID' && (r.serveStatus || 'PENDING') === 'MAKING' && (
                          <button
                            onClick={() => handleFinish(r)}
                            className="p-1.5 rounded-lg hover:bg-blue-500/10 text-text-weak hover:text-blue-400 transition-colors"
                            title="确认制作完成"
                          >
                            <CheckCircle size={15} />
                          </button>
                        )}
                      </div>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>

        {total > 0 && (
          <div className="flex flex-col sm:flex-row items-center justify-between px-4 py-3 border-t border-border-dark bg-sidebar-bg/30 gap-3">
            <div className="flex items-center gap-4 text-xs text-text-weak font-mono">
              <span>共 {total} 条</span>
              <span>第 {pageNum}/{totalPages} 页</span>
            </div>
            <div className="flex items-center gap-1">
              <select
                value={pageSize}
                onChange={e => { setPageSize(Number(e.target.value)); setPageNum(1); }}
                className="bg-card-bg border border-border-dark rounded-lg px-2 py-1.5 text-xs text-text-main focus:outline-none focus:border-brand-gold/50 mr-2"
              >
                {[10, 20, 50].map(n => (
                  <option key={n} value={n}>{n} 条/页</option>
                ))}
              </select>
              <button
                onClick={() => setPageNum(p => Math.max(1, p - 1))}
                disabled={pageNum === 1}
                className="px-3 py-1.5 rounded-lg border border-border-dark text-xs text-text-sub hover:bg-brand-gold/10 hover:text-brand-gold hover:border-brand-gold/30 transition-colors disabled:opacity-30 disabled:cursor-not-allowed"
              >
                上一页
              </button>
              {(() => {
                const pages: number[] = [];
                const start = Math.max(1, pageNum - 2);
                const end = Math.min(totalPages, pageNum + 2);
                for (let i = start; i <= end; i++) pages.push(i);
                return pages.map(p => (
                  <button
                    key={p}
                    onClick={() => setPageNum(p)}
                    className={`px-3 py-1.5 rounded-lg border text-xs transition-colors ${
                      p === pageNum
                        ? 'bg-brand-gold/20 border-brand-gold/50 text-brand-gold'
                        : 'border-border-dark text-text-sub hover:bg-brand-gold/10 hover:text-brand-gold hover:border-brand-gold/30'
                    }`}
                  >
                    {p}
                  </button>
                ));
              })()}
              <button
                onClick={() => setPageNum(p => Math.min(totalPages, p + 1))}
                disabled={pageNum === totalPages}
                className="px-3 py-1.5 rounded-lg border border-border-dark text-xs text-text-sub hover:bg-brand-gold/10 hover:text-brand-gold hover:border-brand-gold/30 transition-colors disabled:opacity-30 disabled:cursor-not-allowed"
              >
                下一页
              </button>
            </div>
          </div>
        )}
      </div>

      <Modal
        title={
          <div className="flex items-center justify-between pr-8">
            <span className="text-brand-gold text-base font-semibold">订单详情</span>
            {detailOrder && (
              <button
                onClick={refreshDetailOrder}
                disabled={detailLoading}
                className="inline-flex items-center gap-1.5 rounded-lg border border-brand-gold/30 px-2.5 py-1 text-xs text-brand-gold hover:bg-brand-gold/10 disabled:opacity-50 disabled:cursor-not-allowed"
              >
                <RefreshCw size={13} className={detailLoading ? 'animate-spin' : ''} />
                刷新状态
              </button>
            )}
          </div>
        }
        open={detailOpen}
        onCancel={() => setDetailOpen(false)}
        footer={null}
        width={640}
        rootClassName="xunye-order-detail-modal"
        styles={modalStyles}
      >
        {detailLoading ? (
          <div className="flex items-center justify-center py-12">
            <div className="w-5 h-5 border-2 border-brand-gold border-t-transparent rounded-full animate-spin" />
          </div>
        ) : detailOrder && (
          <div className="space-y-4 pt-2">
            <div className="grid grid-cols-2 gap-3 text-sm">
              <div>
                <span className="text-text-weak">订单号：</span>
                <span className="text-brand-gold font-mono ml-1">{detailOrder.orderNo}</span>
              </div>
              <div>
                <span className="text-text-weak">桌台：</span>
                <span className="text-text-main ml-1">{detailOrder.tableName}</span>
              </div>
              <div>
                <span className="text-text-weak">订单来源：</span>
                <span className={`inline-flex items-center px-2 py-0.5 rounded-full text-xs ml-1 ${
                  detailOrder.source === 'CUSTOMER_MINI' ? 'bg-yellow-500/10 text-yellow-400' : 'bg-brand-gold/10 text-brand-gold'
                }`}>
                  {getSourceLabel(detailOrder.source)}
                </span>
              </div>
              <div>
                <span className="text-text-weak">状态：</span>
                <span className={`ml-1 ${
                  detailOrder.status === 'PAID' ? 'text-green-400' :
                  detailOrder.status === 'UNPAID' ? 'text-yellow-400' :
                  detailOrder.status === 'FINISHED' ? 'text-blue-400' :
                  'text-gray-400'
                }`}>
                  {statusText[detailOrder.status] || detailOrder.status}
                </span>
              </div>
              <div>
                <span className="text-text-weak">制作状态：</span>
                <span className={`ml-1 ${
                  (detailOrder.serveStatus || 'PENDING') === 'FINISHED' ? 'text-blue-400' :
                  (detailOrder.serveStatus || 'PENDING') === 'MAKING' ? 'text-orange-400' :
                  'text-gray-400'
                }`}>
                  {serveStatusText[detailOrder.serveStatus || 'PENDING'] || '待处理'}
                </span>
              </div>
              <div>
                <span className="text-text-weak">总金额：</span>
                <span className="text-brand-gold font-semibold ml-1">¥{Number(detailOrder.totalAmount).toFixed(2)}</span>
              </div>
              <div>
                <span className="text-text-weak">支付方式：</span>
                <span className="text-text-main ml-1">
                  {detailOrder.paymentMethod ? (payMethodText[detailOrder.paymentMethod] || detailOrder.paymentMethod) : '-'}
                </span>
              </div>
              <div>
                <span className="text-text-weak">创建时间：</span>
                <span className="text-text-main font-mono text-xs ml-1">{detailOrder.createdAt}</span>
              </div>
              {detailOrder.paidAt && (
                <div>
                  <span className="text-text-weak">支付时间：</span>
                  <span className="text-text-main font-mono text-xs ml-1">{detailOrder.paidAt}</span>
                </div>
              )}
              {detailOrder.remark && (
                <div className="col-span-2">
                  <span className="text-text-weak">备注：</span>
                  <span className="text-text-main ml-1">{detailOrder.remark}</span>
                </div>
              )}
            </div>

            <div className="border-t border-border-dark pt-3">
              <h4 className="text-xs font-mono text-text-weak uppercase tracking-wider mb-2">商品明细</h4>
              <table className="w-full text-sm">
                <thead>
                  <tr className="text-xs text-text-weak border-b border-border-dark/50">
                    <th className="text-left py-2 font-normal">商品</th>
                    <th className="text-right py-2 font-normal">单价</th>
                    <th className="text-right py-2 font-normal">数量</th>
                    <th className="text-right py-2 font-normal">小计</th>
                  </tr>
                </thead>
                <tbody>
                  {(detailOrder.items || []).map(item => (
                    <tr key={item.id} className="border-b border-border-dark/30">
                      <td className="py-2 text-text-main">{item.productName}</td>
                      <td className="py-2 text-right text-text-sub">¥{Number(item.price).toFixed(2)}</td>
                      <td className="py-2 text-right text-text-sub">×{item.quantity}</td>
                      <td className="py-2 text-right text-brand-gold font-semibold">¥{Number(item.amount).toFixed(2)}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
              {(!detailOrder.items || detailOrder.items.length === 0) && (
                <p className="text-center text-text-weak text-xs py-4 italic">暂无商品明细</p>
              )}
            </div>
          </div>
        )}
      </Modal>

      <Modal
        title={<span className="text-brand-gold text-base font-semibold">收款</span>}
        open={payOpen}
        onCancel={() => setPayOpen(false)}
        onOk={handlePay}
        okText="确认收款"
        cancelText="取消"
        confirmLoading={paying}
        okButtonProps={{ disabled: !payMethod }}
        rootClassName="xunye-order-pay-modal"
        styles={modalStyles}
      >
        {payOrder_ && (
          <div className="space-y-4 pt-2">
            <div className="text-sm">
              <span className="text-text-weak">订单：</span>
              <span className="text-brand-gold font-mono ml-1">{payOrder_.orderNo}</span>
              <span className="text-text-weak ml-3">金额：</span>
              <span className="text-brand-gold font-semibold ml-1">¥{Number(payOrder_.totalAmount).toFixed(2)}</span>
            </div>
            <div>
              <label className="text-sm text-text-main block mb-2">选择支付方式</label>
              <div className="grid grid-cols-3 gap-2">
                {[
                  { key: 'WECHAT' as const, label: '微信', color: 'green' },
                  { key: 'ALIPAY' as const, label: '支付宝', color: 'blue' },
                  { key: 'CASH' as const, label: '现金', color: 'yellow' },
                ].map(opt => (
                  <button
                    key={opt.key}
                    onClick={() => setPayMethod(opt.key)}
                    className={`py-3 rounded-lg border text-sm font-medium transition-all ${
                      payMethod === opt.key
                        ? 'border-brand-gold bg-brand-gold/10 text-brand-gold'
                        : 'border-border-dark text-text-sub hover:border-brand-gold/30 hover:text-text-main'
                    }`}
                  >
                    {opt.label}
                  </button>
                ))}
              </div>
            </div>
          </div>
        )}
      </Modal>
    </div>
  );
}
