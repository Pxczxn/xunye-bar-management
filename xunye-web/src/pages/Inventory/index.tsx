'use client';

import { useEffect, useState, useCallback } from 'react';
import { Modal, Form, Input, Select, InputNumber, message } from 'antd';
import { getInventoryWarnings, getInventoryRecords, adjustInventory } from '@/api/inventory';
import { getProductPage } from '@/api/product';
import type {
  InventoryWarning,
  InventoryRecord,
  InventoryAdjustParams,
  ProductItem,
} from '@/types/api';
import {
  AlertCircle,
  Plus,
  Search,
  RotateCcw,
  RefreshCw,
} from 'lucide-react';
import { darkSelectProps } from '@/constants/antdTheme';

const { TextArea } = Input;

const ADJUST_TYPE_OPTIONS = [
  { label: '入库 (IN)', value: 'IN' },
  { label: '出库 (OUT)', value: 'OUT' },
  { label: '报损 (LOSS)', value: 'LOSS' },
  { label: '调整 (ADJUST)', value: 'ADJUST' },
];

const RECORD_TYPE_OPTIONS = [
  { label: '全部类型', value: '' },
  { label: '入库', value: 'IN' },
  { label: '出库', value: 'OUT' },
  { label: '报损', value: 'LOSS' },
  { label: '调整', value: 'ADJUST' },
];

export default function InventoryPage() {
  const [activeTab, setActiveTab] = useState<'warnings' | 'records'>('warnings');
  const [warnings, setWarnings] = useState<InventoryWarning[]>([]);
  const [warningsLoading, setWarningsLoading] = useState(true);

  const [records, setRecords] = useState<InventoryRecord[]>([]);
  const [recordsTotal, setRecordsTotal] = useState(0);
  const [recordsLoading, setRecordsLoading] = useState(false);
  const [pageNum, setPageNum] = useState(1);
  const [pageSize, setPageSize] = useState(10);
  const [filterProductName, setFilterProductName] = useState('');
  const [filterType, setFilterType] = useState<string | undefined>();

  const [modalOpen, setModalOpen] = useState(false);
  const [products, setProducts] = useState<ProductItem[]>([]);
  const [productsLoading, setProductsLoading] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [form] = Form.useForm();

  const fetchWarnings = useCallback(async () => {
    setWarningsLoading(true);
    try {
      const data = await getInventoryWarnings();
      setWarnings(Array.isArray(data) ? data : []);
    } catch (err: any) {
      message.error(err.message || '获取库存预警失败');
      setWarnings([]);
    } finally {
      setWarningsLoading(false);
    }
  }, []);

  const fetchRecords = useCallback(async (page = pageNum, size = pageSize) => {
    setRecordsLoading(true);
    try {
      const params: any = { pageNum: page, pageSize: size };
      if (filterProductName) params.productName = filterProductName;
      if (filterType) params.type = filterType;
      const res = await getInventoryRecords(params);
      setRecords(res?.records ?? []);
      setRecordsTotal(res?.total ?? 0);
    } catch (err: any) {
      message.error(err.message || '获取库存流水失败');
      setRecords([]);
      setRecordsTotal(0);
    } finally {
      setRecordsLoading(false);
    }
  }, [pageNum, pageSize, filterProductName, filterType]);

  const fetchProducts = useCallback(async () => {
    setProductsLoading(true);
    try {
      const res = await getProductPage({ pageNum: 1, pageSize: 100 });
      setProducts(res?.records ?? []);
    } catch (err: any) {
      message.error(err.message || '获取商品列表失败');
      setProducts([]);
    } finally {
      setProductsLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchWarnings();
  }, [fetchWarnings]);

  useEffect(() => {
    if (activeTab === 'records') {
      fetchRecords();
    }
  }, [activeTab, fetchRecords]);

  const handleRefresh = () => {
    if (activeTab === 'warnings') {
      fetchWarnings();
    } else {
      fetchRecords();
    }
  };

  const handleSearch = () => {
    setPageNum(1);
    fetchRecords(1, pageSize);
  };

  const handleReset = () => {
    setFilterProductName('');
    setFilterType(undefined);
    setPageNum(1);
    setPageSize(10);
  };

  const handlePageChange = (page: number, size: number) => {
    setPageNum(page);
    setPageSize(size);
  };

  const openAdjustModal = () => {
    form.resetFields();
    setModalOpen(true);
    fetchProducts();
  };

  const handleSubmit = async () => {
    try {
      const values = await form.validateFields();
      setSubmitting(true);
      const payload: InventoryAdjustParams = {
        productId: values.productId,
        type: values.type,
        quantity: values.quantity,
        reason: values.reason || '',
      };
      await adjustInventory(payload);
      message.success('库存调整成功');
      setModalOpen(false);
      fetchWarnings();
      if (activeTab === 'records') {
        fetchRecords();
      }
    } catch (err: any) {
      if (err?.errorFields) return;
      message.error(err.message || '库存调整失败');
    } finally {
      setSubmitting(false);
    }
  };

  const getWarningLevelDisplay = (level: string) => {
    switch (level) {
      case 'HIGH':
        return (
          <span className="inline-flex items-center px-2.5 py-1.5 rounded-md text-xs font-bold bg-danger/20 text-danger border border-danger/30">
            <AlertCircle size={14} className="mr-1.5" /> 严重不足
          </span>
        );
      case 'MEDIUM':
        return (
          <span className="inline-flex items-center px-2.5 py-1.5 rounded-md text-xs font-bold bg-brand-gold/10 text-brand-gold border border-brand-gold/30">
            库存偏低
          </span>
        );
      case 'LOW':
        return (
          <span className="inline-flex items-center px-2.5 py-1.5 rounded-md text-xs font-medium bg-border-dark text-text-sub">
            轻微预警
          </span>
        );
      default:
        return <span className="text-text-sub">{level}</span>;
    }
  };

  const getTypeDisplay = (type: string, typeText?: string) => {
    const typeMap: Record<string, { label: string; color: string }> = {
      IN: { label: '入库', color: 'text-success' },
      OUT: { label: '出库', color: 'text-brand-gold' },
      LOSS: { label: '报损', color: 'text-danger' },
      ADJUST: { label: '盘点调整', color: 'text-text-sub' },
    };
    const info = typeMap[type] || { label: type, color: 'text-text-sub' };
    return <span className={info.color}>{typeText || info.label}</span>;
  };

  const displayValue = (value: string | number | null | undefined) => {
    return value !== null && value !== undefined && value !== '' ? String(value) : '-';
  };

  const selectedType = Form.useWatch('type', form);
  const quantityHint = selectedType === 'ADJUST'
    ? '调整后的实际库存总量，必须为非负整数'
    : '变动的数量值，必须为正整数';

  const totalPages = Math.ceil(recordsTotal / pageSize);

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row justify-between items-start sm:items-end gap-6 mb-6">
        <div>
          <h1 className="text-2xl font-serif font-bold text-text-main tracking-wider mb-1">库存酒窖</h1>
          <p className="text-[10px] text-brand-gold uppercase tracking-widest font-medium">Inventory & Alerts</p>
        </div>
        <div className="shrink-0 flex items-center gap-3">
          <button
            onClick={handleRefresh}
            className="flex items-center space-x-2 border border-border-dark text-text-sub px-4 py-2 rounded-lg text-sm hover:border-brand-gold/50 hover:text-text-main transition-colors"
          >
            <RefreshCw size={16} />
            <span>刷新</span>
          </button>
          <button
            onClick={openAdjustModal}
            className="flex items-center space-x-2 bg-brand-gold text-page-bg px-4 py-2 rounded-lg font-semibold text-sm hover:bg-brand-gold/90 transition-colors tracking-widest uppercase"
          >
            <Plus size={16} />
            <span>库存调整</span>
          </button>
        </div>
      </div>

      <div className="flex border-b border-border-dark">
        <button
          onClick={() => setActiveTab('warnings')}
          className={`px-6 py-3 text-sm font-medium transition-colors ${
            activeTab === 'warnings'
              ? 'text-brand-gold border-b-2 border-brand-gold'
              : 'text-text-sub hover:text-text-main'
          }`}
        >
          库存预警
        </button>
        <button
          onClick={() => setActiveTab('records')}
          className={`px-6 py-3 text-sm font-medium transition-colors ${
            activeTab === 'records'
              ? 'text-brand-gold border-b-2 border-brand-gold'
              : 'text-text-sub hover:text-text-main'
          }`}
        >
          库存流水
        </button>
      </div>

      {activeTab === 'warnings' && (
        <div className="bg-card-bg border border-border-dark rounded-xl overflow-hidden shadow-xl">
          <div className="p-4 border-b border-border-dark flex items-center justify-between">
            <div className="flex items-center space-x-2 text-danger">
              <AlertCircle size={16} />
              <h3 className="text-sm font-semibold tracking-wide">当前预警记录 ({warnings.length})</h3>
            </div>
          </div>
          <div className="overflow-x-auto hide-scrollbar">
            <table className="w-full text-left text-sm whitespace-nowrap">
              <thead>
                <tr className="bg-sidebar-bg text-text-weak uppercase text-[10px] tracking-wider">
                  <th className="px-6 py-3 font-medium">商品名称</th>
                  <th className="px-6 py-3 font-medium text-right">当前库存</th>
                  <th className="px-6 py-3 font-medium text-right">安全库存</th>
                  <th className="px-6 py-3 font-medium">单位</th>
                  <th className="px-6 py-3 font-medium">预警等级</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-border-dark/50 text-xs">
                {warningsLoading ? (
                  <tr>
                    <td colSpan={5} className="px-6 py-16 text-center text-text-weak">
                      <div className="flex items-center justify-center space-x-2">
                        <div className="w-4 h-4 border-2 border-brand-gold border-t-transparent rounded-full animate-spin" />
                        <span>加载中...</span>
                      </div>
                    </td>
                  </tr>
                ) : warnings.length === 0 ? (
                  <tr>
                    <td colSpan={5} className="px-6 py-12 text-center text-text-weak font-serif italic">
                      当前库存充足，无预警信息
                    </td>
                  </tr>
                ) : (
                  warnings.map((item) => (
                    <tr key={item.productId} className="hover:bg-border-dark/20 transition-colors">
                      <td className="px-6 py-3 text-text-main font-medium tracking-wide">{displayValue(item.productName)}</td>
                      <td className="px-6 py-3 text-right">
                        <span
                          className={`font-sans tracking-wide font-bold ${
                            item.warningLevel === 'HIGH'
                              ? 'text-danger'
                              : item.warningLevel === 'MEDIUM'
                              ? 'text-brand-gold'
                              : 'text-text-main'
                          }`}
                        >
                          {item.currentStock}
                        </span>
                      </td>
                      <td className="px-6 py-3 text-right text-text-sub font-sans tracking-wide">{displayValue(item.safeStock)}</td>
                      <td className="px-6 py-3 text-text-sub tracking-wider">{displayValue(item.unit)}</td>
                      <td className="px-6 py-3">{getWarningLevelDisplay(item.warningLevel)}</td>
                    </tr>
                  ))
                )}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {activeTab === 'records' && (
        <>
          <div className="bg-card-bg border border-border-dark rounded-xl p-4">
            <div className="flex flex-col sm:flex-row items-end gap-3 flex-wrap">
              <div className="w-full sm:w-56">
                <label className="block text-[10px] text-text-weak uppercase tracking-wider mb-1">商品名称</label>
                <Input
                  placeholder="输入关键词搜索"
                  value={filterProductName}
                  onChange={(e) => setFilterProductName(e.target.value)}
                  allowClear
                  className="!bg-sidebar-bg !border-border-dark !text-text-main !placeholder-text-weak"
                />
              </div>
              <div className="w-full sm:w-40">
                <label className="block text-[10px] text-text-weak uppercase tracking-wider mb-1">交易类型</label>
                <Select
                  placeholder="全部类型"
                  value={filterType}
                  onChange={(v) => setFilterType(v)}
                  allowClear
                  {...darkSelectProps}
                  options={RECORD_TYPE_OPTIONS}
                />
              </div>
              <div className="flex gap-2">
                <button
                  onClick={handleSearch}
                  className="flex items-center space-x-1.5 bg-brand-gold text-page-bg px-4 py-[7px] rounded-lg font-semibold text-sm hover:bg-brand-gold/90 transition-colors"
                >
                  <Search size={14} />
                  <span>查询</span>
                </button>
                <button
                  onClick={handleReset}
                  className="flex items-center space-x-1.5 border border-border-dark text-text-sub px-4 py-[7px] rounded-lg text-sm hover:border-brand-gold/50 hover:text-text-main transition-colors"
                >
                  <RotateCcw size={14} />
                  <span>重置</span>
                </button>
              </div>
            </div>
          </div>

          <div className="bg-card-bg border border-border-dark rounded-xl overflow-hidden shadow-xl">
            <div className="p-4 border-b border-border-dark flex justify-between items-center">
              <h3 className="text-sm font-semibold text-text-main">库存流水</h3>
              <span className="text-xs text-text-weak">共 {recordsTotal} 条</span>
            </div>
            <div className="overflow-x-auto hide-scrollbar">
              <table className="w-full text-left text-sm whitespace-nowrap">
                <thead>
                <tr className="bg-sidebar-bg text-text-weak uppercase text-[10px] tracking-wider">
                  <th className="px-6 py-3 font-medium">商品名称</th>
                  <th className="px-6 py-3 font-medium">交易类型</th>
                  <th className="px-6 py-3 font-medium text-right">变动数量</th>
                  <th className="px-6 py-3 font-medium text-right">变动前</th>
                  <th className="px-6 py-3 font-medium text-right">变动后</th>
                  <th className="px-6 py-3 font-medium">原因</th>
                  <th className="px-6 py-3 font-medium">操作人</th>
                  <th className="px-6 py-3 font-medium">交易时间</th>
                </tr>
              </thead>
                <tbody className="divide-y divide-border-dark/50 text-xs">
                  {recordsLoading ? (
                    <tr>
                      <td colSpan={8} className="px-6 py-16 text-center text-text-weak">
                        <div className="flex items-center justify-center space-x-2">
                          <div className="w-4 h-4 border-2 border-brand-gold border-t-transparent rounded-full animate-spin" />
                          <span>加载中...</span>
                        </div>
                      </td>
                    </tr>
                  ) : records.length === 0 ? (
                    <tr>
                      <td colSpan={8} className="px-6 py-16 text-center text-text-weak font-serif italic">
                        暂无库存流水记录
                      </td>
                    </tr>
                  ) : (
                    records.map((item) => (
                      <tr key={item.id} className="hover:bg-border-dark/20 transition-colors">
                        <td className="px-6 py-3 text-text-main font-medium tracking-wide">{displayValue(item.productName)}</td>
                        <td className="px-6 py-3">{getTypeDisplay(item.type, item.typeText)}</td>
                        <td className="px-6 py-3 text-right font-sans tracking-wide">
                          <span className={item.changeQuantity > 0 ? 'text-success' : 'text-danger'}>
                            {item.changeQuantity > 0 ? '+' : ''}{item.changeQuantity}
                          </span>
                        </td>
                        <td className="px-6 py-3 text-right text-text-sub font-sans tracking-wide">{displayValue(item.beforeStock)}</td>
                        <td className="px-6 py-3 text-right text-text-main font-sans tracking-wide">{displayValue(item.afterStock)}</td>
                        <td className="px-6 py-3 text-text-sub">{displayValue(item.reason)}</td>
                        <td className="px-6 py-3 text-text-sub">{displayValue(item.operatorName)}</td>
                        <td className="px-6 py-3 text-text-sub">{displayValue(item.createdAt)}</td>
                      </tr>
                    ))
                  )}
                </tbody>
              </table>
            </div>

            {recordsTotal > 0 && (
              <div className="p-4 border-t border-border-dark flex flex-col sm:flex-row justify-between items-center gap-3">
                <span className="text-xs text-text-weak">
                  第 {pageNum} / {totalPages} 页，共 {recordsTotal} 条
                </span>
                <div className="flex items-center gap-2">
                  <button
                    disabled={pageNum <= 1}
                    onClick={() => handlePageChange(pageNum - 1, pageSize)}
                    className="px-3 py-1 text-xs border border-border-dark rounded text-text-sub hover:border-brand-gold/50 disabled:opacity-40 disabled:cursor-not-allowed transition-colors"
                  >
                    上一页
                  </button>
                  {Array.from({ length: Math.min(totalPages, 5) }, (_, i) => {
                    let page: number;
                    if (totalPages <= 5) {
                      page = i + 1;
                    } else if (pageNum <= 3) {
                      page = i + 1;
                    } else if (pageNum >= totalPages - 2) {
                      page = totalPages - 4 + i;
                    } else {
                      page = pageNum - 2 + i;
                    }
                    return (
                      <button
                        key={page}
                        onClick={() => handlePageChange(page, pageSize)}
                        className={`w-8 h-8 text-xs rounded transition-colors ${
                          page === pageNum
                            ? 'bg-brand-gold text-page-bg font-bold'
                            : 'border border-border-dark text-text-sub hover:border-brand-gold/50'
                        }`}
                      >
                        {page}
                      </button>
                    );
                  })}
                  <button
                    disabled={pageNum >= totalPages}
                    onClick={() => handlePageChange(pageNum + 1, pageSize)}
                    className="px-3 py-1 text-xs border border-border-dark rounded text-text-sub hover:border-brand-gold/50 disabled:opacity-40 disabled:cursor-not-allowed transition-colors"
                  >
                    下一页
                  </button>
                  <select
                    value={pageSize}
                    onChange={(e) => {
                      setPageSize(Number(e.target.value));
                      setPageNum(1);
                    }}
                    className="ml-2 px-2 py-1 text-xs bg-sidebar-bg border border-border-dark rounded text-text-sub"
                  >
                    {[10, 20, 50].map((s) => (
                      <option key={s} value={s}>
                        {s} 条/页
                      </option>
                    ))}
                  </select>
                </div>
              </div>
            )}
          </div>
        </>
      )}

      <Modal
        title={
          <span className="text-brand-gold font-serif tracking-wider">库存调整</span>
        }
        open={modalOpen}
        onCancel={() => setModalOpen(false)}
        onOk={handleSubmit}
        confirmLoading={submitting}
        okText="确认调整"
        cancelText="取消"
        width={600}
        destroyOnHidden
        styles={{
          root: { background: 'transparent' },
          wrapper: { background: 'rgba(0, 0, 0, 0.65)' },
          content: { background: '#1A1A1F', border: '1px solid #2A2A31' },
          header: { background: '#1A1A1F', borderBottom: '1px solid #2A2A31', paddingBottom: 16 },
          body: { background: '#1A1A1F', paddingTop: 20 },
          footer: { background: '#1A1A1F', borderTop: '1px solid #2A2A31' },
        }}
      >
        <Form form={form} layout="vertical">
          <Form.Item
            name="productId"
            label={<span className="text-text-sub text-xs uppercase tracking-wider">选择商品</span>}
            rules={[{ required: true, message: '请选择商品' }]}
            className="mb-4"
          >
            <Select
              placeholder="请选择商品"
              showSearch
              loading={productsLoading}
              filterOption={(input, option) =>
                (option?.label ?? '').toLowerCase().includes(input.toLowerCase())
              }
              {...darkSelectProps}
              options={products.map((p) => ({ label: p.name, value: p.id }))}
            />
          </Form.Item>
          <Form.Item
            name="type"
            label={<span className="text-text-sub text-xs uppercase tracking-wider">调整类型</span>}
            rules={[{ required: true, message: '请选择调整类型' }]}
            className="mb-4"
          >
            <Select
              placeholder="请选择调整类型"
              {...darkSelectProps}
              options={ADJUST_TYPE_OPTIONS}
            />
          </Form.Item>
          <Form.Item
            name="quantity"
            label={<span className="text-text-sub text-xs uppercase tracking-wider">数量</span>}
            rules={[
              { required: true, message: '请输入数量' },
              {
                validator: (_, value) => {
                  if (value === undefined || value === null) return Promise.resolve();
                  if (!Number.isInteger(value) || value < 0) {
                    return Promise.reject(new Error('必须为非负整数'));
                  }
                  if (selectedType !== 'ADJUST' && value <= 0) {
                    return Promise.reject(new Error('必须为正整数'));
                  }
                  return Promise.resolve();
                },
              },
            ]}
            className="mb-1"
          >
            <InputNumber
              min={selectedType === 'ADJUST' ? 0 : 1}
              className="!w-full"
              placeholder="请输入数量"
            />
          </Form.Item>
          <div className="text-[10px] text-text-weak mb-4 -mt-1">
            {quantityHint}
          </div>
          <Form.Item
            name="reason"
            label={<span className="text-text-sub text-xs uppercase tracking-wider">调整原因</span>}
            className="mb-0"
          >
            <TextArea
              rows={3}
              placeholder="请输入调整原因（选填）"
              maxLength={200}
              showCount
            />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
}