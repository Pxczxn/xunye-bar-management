import { useState, useEffect } from 'react';
import ReactEChartsCore from 'echarts-for-react/lib/core';
import * as echarts from 'echarts/core';
import { LineChart, PieChart } from 'echarts/charts';
import { GridComponent, LegendComponent, TooltipComponent } from 'echarts/components';
import { CanvasRenderer } from 'echarts/renderers';
import { CreditCard, UtensilsCrossed, TrendingUp, AlertTriangle } from 'lucide-react';
import { getDashboardSummary, getSalesTrend, getPaymentMethods, getHotProducts } from '@/api/dashboard';
import { Loading } from '@/components/Loading';
import { ErrorState } from '@/components/ErrorState';
import { DashboardSummary, SalesTrend, PaymentMethod, HotProduct } from '@/types/api';

echarts.use([LineChart, PieChart, GridComponent, LegendComponent, TooltipComponent, CanvasRenderer]);

export default function Dashboard() {
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [summary, setSummary] = useState<DashboardSummary | null>(null);
  const [trend, setTrend] = useState<SalesTrend[]>([]);
  const [payments, setPayments] = useState<PaymentMethod[]>([]);
  const [products, setProducts] = useState<HotProduct[]>([]);

  const fetchData = async () => {
    try {
      setLoading(true); setError(null);
      
      const [_summary, _trend, _payments, _products] = await Promise.all([
        getDashboardSummary(),
        getSalesTrend(),
        getPaymentMethods(),
        getHotProducts(),
      ]);

      setSummary(_summary || null);
      setTrend(Array.isArray(_trend) ? _trend : []);
      setPayments(Array.isArray(_payments) ? _payments : []);
      setProducts(Array.isArray(_products) ? _products : []);
    } catch (err: any) {
      setError(err.message || '由于你尚未启动或者连接真实的 8848 后端接口服务，数据请求失败。请先确保后端启动，或者提供测试数据。');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchData();
  }, []);

  if (loading) return <Loading />;
  if (error) return <ErrorState message={error} onRetry={fetchData} />;

  const trendOption = {
    tooltip: { trigger: 'axis', backgroundColor: '#1A1A1F', borderColor: '#2A2A31', textStyle: { color: '#F4EBDD' } },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: { type: 'category', boundaryGap: false, data: trend.map(t => t.date), axisLine: { lineStyle: { color: '#6F6A63' } } },
    yAxis: [
      { type: 'value', name: '营业额', splitLine: { lineStyle: { color: 'rgba(255,255,255,0.05)', type: 'dashed' } }, axisLine: { show: false }, axisLabel: { color: '#AFA79B', formatter: '¥{value}' } },
      { type: 'value', name: '订单数', splitLine: { show: false }, axisLine: { show: false }, axisLabel: { color: '#AFA79B' } }
    ],
    series: [
      { name: '营业额', type: 'line', data: trend.map(t => t.revenue), itemStyle: { color: '#D6A85A' }, lineStyle: { width: 3 }, areaStyle: { color: { type: 'linear', x: 0, y: 0, x2: 0, y2: 1, colorStops: [{ offset: 0, color: 'rgba(214, 168, 90, 0.4)' }, { offset: 1, color: 'rgba(214, 168, 90, 0)' }] } } },
      { name: '订单数', type: 'line', yAxisIndex: 1, data: trend.map(t => t.orderCount), itemStyle: { color: '#F4EBDD' } }
    ]
  };

  const paymentOption = {
    tooltip: { trigger: 'item', backgroundColor: '#1A1A1F', borderColor: '#2A2A31', textStyle: { color: '#F4EBDD' } },
    legend: { bottom: '5%', left: 'center', textStyle: { color: '#AFA79B' } },
    series: [
      {
        name: '支付方式', type: 'pie', radius: ['50%', '70%'], avoidLabelOverlap: false, label: { show: false, position: 'center' },
        emphasis: { label: { show: true, fontSize: 16, fontWeight: 'bold', formatter: '{b}\n¥{c}' } }, labelLine: { show: false },
        data: payments.map(p => ({ value: p.amount, name: p.method, itemStyle: { color: p.method === '微信' ? '#D6A85A' : p.method === '支付宝' ? '#F4EBDD' : '#6F6A63' } }))
      }
    ]
  };

  return (
    <div className="space-y-6">
      <div className="flex flex-col mb-2">
        <h1 className="text-2xl font-serif font-bold text-text-main tracking-wider mb-1">营业看板</h1>
        <p className="text-[10px] text-text-sub uppercase tracking-widest font-medium">Data Overview</p>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
        <SummaryCard title="今日营业额" value={'¥' + (summary?.todayRevenue || 0)} icon={<TrendingUp size={16} />} highlight />
        <SummaryCard title="今日订单数" value={summary?.todayOrderCount || 0} icon={<UtensilsCrossed size={16} />} />
        <SummaryCard title="平均客单价" value={'¥' + (summary?.averageOrderValue || 0)} icon={<CreditCard size={16} />} />
        <SummaryCard title="待处理预警" value={summary?.inventoryWarningCount || 0} icon={<AlertTriangle size={16} />} danger />
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        <div className="col-span-1 lg:col-span-2 bg-card-bg border border-border-dark p-6 rounded-xl">
          <div className="flex justify-between items-center mb-6">
            <h3 className="text-sm font-semibold text-text-main">最近 7 天营业趋势</h3>
            <span className="text-text-weak text-[10px] font-normal">最近运行周期</span>
          </div>
          <div className="h-[300px] w-full">
             <ReactEChartsCore echarts={echarts} option={trendOption} style={{ height: '100%', width: '100%' }} />
          </div>
        </div>
        <div className="bg-card-bg border border-border-dark p-6 rounded-xl">
          <h3 className="text-sm font-semibold text-text-main mb-6">支付方式占比</h3>
          <div className="h-[300px] w-full">
            <ReactEChartsCore echarts={echarts} option={paymentOption} style={{ height: '100%', width: '100%' }} />
          </div>
        </div>
      </div>

      <div className="bg-card-bg border border-border-dark rounded-xl overflow-hidden">
        <div className="p-4 border-b border-border-dark flex justify-between items-center">
          <h3 className="text-sm font-semibold text-text-main">热销商品排行</h3>
          <button className="text-brand-gold text-xs hover:underline">查看全部</button>
        </div>
        <div className="overflow-x-auto">
          <table className="w-full text-left">
            <thead>
              <tr className="bg-sidebar-bg text-text-weak uppercase text-[10px] tracking-wider">
                <th className="px-6 py-3 font-medium w-16">排名</th>
                <th className="px-6 py-3 font-medium">商品名称</th>
                <th className="px-6 py-3 font-medium text-right">销量</th>
                <th className="px-6 py-3 font-medium text-right">销售额</th>
              </tr>
            </thead>
            <tbody className="text-xs">
              {products.map((product, idx) => (
                <tr key={idx} className="border-b border-border-dark/50 last:border-0 hover:bg-border-dark/20 transition-colors">
                  <td className="px-6 py-3">
                    <span className={"inline-flex items-center justify-center w-5 h-5 rounded-sm text-[10px] font-bold font-sans " + (idx < 3 ? 'bg-brand-gold/10 text-brand-gold border border-brand-gold/20' : 'text-text-sub')}>
                      {idx + 1}
                    </span>
                  </td>
                  <td className="px-6 py-3 text-text-main tracking-wide">{product.productName}</td>
                  <td className="px-6 py-3 text-right text-text-weak font-sans font-medium">{product.salesCount}</td>
                  <td className="px-6 py-3 text-right text-brand-gold font-sans font-bold tracking-wide">¥{product.salesAmount.toFixed(2)}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}

function SummaryCard({ title, value, icon, highlight, danger }: { title: string, value: string | number, icon: React.ReactNode, highlight?: boolean, danger?: boolean }) {
  return (
    <div className={"p-5 rounded-xl border shadow-xl " + (danger ? 'border-danger/20 bg-card-bg' : 'border-border-dark bg-card-bg')}>
      <div className="flex items-center justify-between mb-2">
        <p className={"text-xs font-semibold uppercase tracking-wider " + (danger ? 'text-danger' : 'text-text-weak')}>
          {title}
        </p>
      </div>
      <div className="flex items-baseline space-x-1 mt-1">
        <span className={"text-2xl font-bold " + (highlight ? 'text-brand-gold' : danger ? 'text-danger' : 'text-brand-gold')}>
          {value}
        </span>
      </div>
    </div>
  );
}
