'use client';

import { useEffect, useState } from 'react';
import { getRecentOrders } from '@/api/order';
import { Loading } from '@/components/Loading';
import { ErrorState } from '@/components/ErrorState';
import { Eye } from 'lucide-react';

export default function OrdersPage() {
  const [data, setData] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const fetchData = async () => {
    setLoading(true);
    setError(null);
    try {
      const data = await getRecentOrders();
      setData(Array.isArray(data) ? data : []);
    } catch (err: any) {
      setData([]);
      setError(err.message || '获取订单流水失败');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchData();
  }, []);

  if (loading) return <Loading />;
  if (error) return <ErrorState message={error} onRetry={fetchData} />;

  const getStatusDisplay = (status: string) => {
    switch(status) {
      case 'PAID': return <span className="inline-flex items-center px-2 py-1 rounded-full text-xs font-medium bg-success/10 text-success border border-success/20">已支付</span>;
      case 'UNPAID': return <span className="inline-flex items-center px-2 py-1 rounded-full text-xs font-medium bg-brand-gold/10 text-brand-gold border border-brand-gold/20">未支付</span>;
      case 'CANCELLED': return <span className="inline-flex items-center px-2 py-1 rounded-full text-xs font-medium bg-border-dark text-text-sub border border-border-dark/50">已取消</span>;
      default: return <span>{status}</span>;
    }
  };

  return (
    <div className="space-y-6">
      <div className="mb-6">
        <h1 className="text-2xl font-serif font-bold text-text-main tracking-wider mb-1">订单流水</h1>
        <p className="text-[10px] text-brand-gold uppercase tracking-widest font-medium">Recent Transactions</p>
      </div>

      <div className="bg-card-bg border border-border-dark rounded-xl overflow-hidden shadow-xl">
        <div className="p-4 border-b border-border-dark flex justify-between items-center bg-card-bg">
           <h3 className="text-sm font-semibold text-text-main">全部流水记录</h3>
        </div>
        <div className="overflow-x-auto hide-scrollbar">
          <table className="w-full text-left text-sm whitespace-nowrap">
            <thead>
              <tr className="bg-sidebar-bg text-text-weak uppercase text-[10px] tracking-wider">
                <th className="px-6 py-3 font-medium">订单编号</th>
                <th className="px-6 py-3 font-medium">桌台</th>
                <th className="px-6 py-3 font-medium text-right">订单金额</th>
                <th className="px-6 py-3 font-medium">支付方式</th>
                <th className="px-6 py-3 font-medium">状态</th>
                <th className="px-6 py-3 font-medium">下单时间</th>
                <th className="px-6 py-3 font-medium text-center">操作</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-border-dark/50 text-xs">
              {data.map((order, idx) => (
                <tr key={idx} className="hover:bg-border-dark/20 transition-colors">
                  <td className="px-6 py-3 font-sans text-text-sub tracking-wider">{order.orderNo}</td>
                  <td className="px-6 py-3 text-text-main font-medium">{order.tableName}</td>
                  <td className="px-6 py-3 text-right text-brand-gold tracking-wide font-bold">¥{order.amount.toFixed(2)}</td>
                  <td className="px-6 py-3 text-text-sub">{order.paymentMethod}</td>
                  <td className="px-6 py-3">{getStatusDisplay(order.status)}</td>
                  <td className="px-6 py-3 text-text-sub font-sans tracking-wider">{order.createdAt}</td>
                  <td className="px-6 py-3 text-center">
                    <button className="text-text-sub hover:text-brand-gold transition-colors inline-flex items-center justify-center">
                      <Eye size={16} />
                    </button>
                  </td>
                </tr>
              ))}
              {data.length === 0 && (
                <tr>
                  <td colSpan={7} className="px-6 py-12 text-center text-text-weak font-serif italic">
                    暂无订单记录
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}
