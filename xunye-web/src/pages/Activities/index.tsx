'use client';

import { useEffect, useState, useCallback } from 'react';
import { Modal, Form, Input, Select, DatePicker, InputNumber, message } from 'antd';
import { getActivityPage, createActivity, updateActivity, deleteActivity } from '@/api/activities';
import type { ActivityItem, ActivityFormData } from '@/types/api';
import { Plus, Edit, Trash2, Search, RotateCcw } from 'lucide-react';
import dayjs from 'dayjs';

const { TextArea } = Input;
const { RangePicker } = DatePicker;

const TYPE_OPTIONS = [
  { label: '折扣', value: 'DISCOUNT' },
  { label: '优惠券', value: 'COUPON' },
  { label: '积分', value: 'POINTS' },
  { label: '特惠', value: 'SPECIAL' },
];

const STATUS_OPTIONS = [
  { label: '全部状态', value: '' },
  { label: '草稿', value: '0' },
  { label: '进行中', value: '1' },
  { label: '已结束', value: '2' },
];

const STATUS_FILTER_OPTIONS = [
  { label: '全部状态', value: '' },
  { label: '草稿', value: 0 },
  { label: '进行中', value: 1 },
  { label: '已结束', value: 2 },
];

const STATUS_MAP: Record<number, { label: string; color: string; bg: string }> = {
  0: { label: '草稿', color: 'text-text-sub', bg: 'bg-border-dark' },
  1: { label: '进行中', color: 'text-success', bg: 'bg-success/10' },
  2: { label: '已结束', color: 'text-danger', bg: 'bg-danger/10' },
};

const TYPE_MAP: Record<string, string> = {
  DISCOUNT: '折扣',
  COUPON: '优惠券',
  POINTS: '积分',
  SPECIAL: '特惠',
};

const darkSelectProps = {
  className: 'xunye-select',
  classNames: { popup: { root: 'xunye-select-dropdown' } },
  styles: {
    root: { backgroundColor: '#101014', border: '1px solid #2A2A31' },
    content: { color: '#F4EBDD' },
    suffix: { color: '#AFA79B' },
    popup: { root: { backgroundColor: '#1A1A1F', border: '1px solid #2A2A31' } },
  },
} as const;

const modalStyles = {
  content: { background: '#1A1A1F', border: '1px solid #2A2A31' },
  header: { background: '#1A1A1F', borderBottom: '1px solid #2A2A31', paddingBottom: 16 },
  body: { background: '#1A1A1F', paddingTop: 20 },
  footer: { background: '#1A1A1F', borderTop: '1px solid #2A2A31' },
};

export default function ActivitiesPage() {
  const [data, setData] = useState<ActivityItem[]>([]);
  const [loading, setLoading] = useState(false);
  const [total, setTotal] = useState(0);
  const [pageNum, setPageNum] = useState(1);
  const [pageSize, setPageSize] = useState(10);
  const [keyword, setKeyword] = useState('');
  const [filterType, setFilterType] = useState<string | undefined>();
  const [filterStatus, setFilterStatus] = useState<number | undefined>();
  const [searchInput, setSearchInput] = useState('');

  const [modalOpen, setModalOpen] = useState(false);
  const [editingItem, setEditingItem] = useState<ActivityItem | null>(null);
  const [submitting, setSubmitting] = useState(false);
  const [form] = Form.useForm();

  const fetchData = useCallback(async (page = pageNum, size = pageSize) => {
    setLoading(true);
    try {
      const params: any = { pageNum: page, pageSize: size };
      if (keyword) params.keyword = keyword;
      if (filterType) params.type = filterType;
      if (filterStatus !== undefined) params.status = filterStatus;
      const res = await getActivityPage(params);
      setData(res?.records ?? []);
      setTotal(res?.total ?? 0);
    } catch (err: any) {
      message.error(err.message || '获取活动列表失败');
      setData([]);
    } finally {
      setLoading(false);
    }
  }, [pageNum, pageSize, keyword, filterType, filterStatus]);

  useEffect(() => {
    fetchData();
  }, [fetchData]);

  const handleSearch = () => {
    setKeyword(searchInput);
    setPageNum(1);
  };

  const handleReset = () => {
    setSearchInput('');
    setKeyword('');
    setFilterType(undefined);
    setFilterStatus(undefined);
    setPageNum(1);
  };

  const totalPages = Math.ceil(total / pageSize) || 1;

  const openAddModal = () => {
    setEditingItem(null);
    form.resetFields();
    form.setFieldsValue({ type: 'DISCOUNT', status: 0, sort: 0 });
    setModalOpen(true);
  };

  const openEditModal = (item: ActivityItem) => {
    setEditingItem(item);
    form.setFieldsValue({
      title: item.title,
      description: item.description,
      type: item.type,
      status: item.status,
      sort: item.sort,
      dateRange: item.startDate && item.endDate
        ? [dayjs(item.startDate), dayjs(item.endDate)]
        : undefined,
    });
    setModalOpen(true);
  };

  const handleSubmit = async () => {
    try {
      const values = await form.validateFields();
      setSubmitting(true);
      const payload: ActivityFormData = {
        title: values.title,
        description: values.description || '',
        type: values.type,
        startDate: values.dateRange?.[0]?.format('YYYY-MM-DD HH:mm:ss') || null,
        endDate: values.dateRange?.[1]?.format('YYYY-MM-DD HH:mm:ss') || null,
        coverImage: '',
        status: values.status ?? 0,
        sort: values.sort ?? 0,
      };
      if (editingItem) {
        await updateActivity(editingItem.id, payload);
        message.success('修改成功');
      } else {
        await createActivity(payload);
        message.success('新增成功');
      }
      setModalOpen(false);
      fetchData();
    } catch (err: any) {
      if (err?.errorFields) return;
      message.error(err.message || '操作失败');
    } finally {
      setSubmitting(false);
    }
  };

  const handleDelete = (item: ActivityItem) => {
    Modal.confirm({
      title: '确认删除',
      content: `确定要删除活动「${item.title}」吗？`,
      okText: '删除',
      okButtonProps: { danger: true },
      cancelText: '取消',
      styles: modalStyles,
      onOk: async () => {
        try {
          await deleteActivity(item.id);
          message.success('删除成功');
          fetchData();
        } catch (err: any) {
          message.error(err.message || '删除失败');
        }
      },
    });
  };

  return (
    <div className="space-y-6">
      {/* 标题栏 */}
      <div className="flex flex-col sm:flex-row justify-between items-start sm:items-end gap-6 mb-6">
        <div>
          <h1 className="text-2xl font-serif font-bold text-text-main tracking-wider mb-1">活动管理</h1>
          <p className="text-[10px] text-brand-gold uppercase tracking-widest font-medium">Activities</p>
        </div>
        <button
          onClick={openAddModal}
          className="flex items-center space-x-2 bg-brand-gold text-page-bg px-4 py-2 rounded-lg font-semibold text-sm hover:bg-brand-gold/90 transition-colors shrink-0 tracking-widest uppercase"
        >
          <Plus size={16} />
          <span>新增活动</span>
        </button>
      </div>

      {/* 搜索栏 */}
      <div className="bg-card-bg border border-border-dark rounded-xl p-4">
        <div className="flex flex-wrap items-center gap-3">
          <div className="relative flex-1 min-w-[200px]">
            <Search size={14} className="absolute left-3 top-1/2 -translate-y-1/2 text-text-weak" />
            <input
              type="text"
              placeholder="搜索活动标题..."
              value={searchInput}
              onChange={e => setSearchInput(e.target.value)}
              onKeyDown={e => e.key === 'Enter' && handleSearch()}
              className="w-full h-9 bg-page-bg border border-border-dark rounded-lg pl-9 pr-3 text-xs text-text-main placeholder:text-text-weak outline-none focus:border-brand-gold/50 transition-colors"
            />
          </div>
          <Select
            placeholder="活动类型"
            allowClear
            value={filterType}
            onChange={val => setFilterType(val || undefined)}
            options={TYPE_OPTIONS}
            style={{ width: 120, height: 36 }}
            {...darkSelectProps}
          />
          <Select
            placeholder="活动状态"
            allowClear
            value={filterStatus}
            onChange={val => setFilterStatus(val !== undefined ? val : undefined)}
            options={STATUS_FILTER_OPTIONS}
            style={{ width: 120, height: 36 }}
            {...darkSelectProps}
          />
          <button
            onClick={handleSearch}
            className="h-9 px-4 bg-brand-gold text-page-bg rounded-lg text-xs font-semibold hover:bg-brand-gold/90 transition-colors tracking-wider"
          >
            查询
          </button>
          <button
            onClick={handleReset}
            className="h-9 px-4 border border-border-dark text-text-sub rounded-lg text-xs hover:text-text-main hover:border-text-sub transition-colors"
          >
            重置
          </button>
        </div>
      </div>

      {/* 数据表格 */}
      <div className="bg-card-bg border border-border-dark rounded-xl overflow-hidden shadow-xl">
        <div className="p-4 border-b border-border-dark flex justify-between items-center">
          <h3 className="text-sm font-semibold text-text-main">活动列表</h3>
          <button
            onClick={() => fetchData()}
            className="flex items-center space-x-1.5 text-text-sub hover:text-brand-gold transition-colors"
          >
            <RotateCcw size={14} />
            <span className="text-xs">刷新</span>
          </button>
        </div>
        <div className="overflow-x-auto hide-scrollbar">
          <table className="w-full text-left text-sm whitespace-nowrap">
            <thead>
              <tr className="bg-sidebar-bg text-text-weak uppercase text-[10px] tracking-wider">
                <th className="px-6 py-3 font-medium">活动标题</th>
                <th className="px-6 py-3 font-medium text-center">类型</th>
                <th className="px-6 py-3 font-medium text-center">状态</th>
                <th className="px-6 py-3 font-medium text-center">时间</th>
                <th className="px-6 py-3 font-medium text-center">排序</th>
                <th className="px-6 py-3 font-medium text-center">操作</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-border-dark/50 text-xs">
              {loading ? (
                <tr>
                  <td colSpan={6} className="px-6 py-16 text-center text-text-weak">
                    <div className="flex items-center justify-center space-x-2">
                      <div className="w-4 h-4 border-2 border-brand-gold border-t-transparent rounded-full animate-spin" />
                      <span>加载中...</span>
                    </div>
                  </td>
                </tr>
              ) : data.length === 0 ? (
                <tr>
                  <td colSpan={6} className="px-6 py-16 text-center text-text-weak font-serif italic">
                    暂无活动数据
                  </td>
                </tr>
              ) : (
                data.map((item) => {
                  const statusInfo = STATUS_MAP[item.status] || STATUS_MAP[0];
                  return (
                    <tr key={item.id} className="hover:bg-border-dark/20 transition-colors">
                      <td className="px-6 py-3">
                        <span className="font-medium text-text-main tracking-wide">{item.title}</span>
                      </td>
                      <td className="px-6 py-3 text-center">
                        <span className="text-text-sub">{TYPE_MAP[item.type] || item.type}</span>
                      </td>
                      <td className="px-6 py-3 text-center">
                        <span className={`inline-flex items-center px-2 py-0.5 rounded text-[10px] ${statusInfo.bg} ${statusInfo.color}`}>
                          {statusInfo.label}
                        </span>
                      </td>
                      <td className="px-6 py-3 text-center text-text-sub font-mono text-[10px]">
                        {item.startDate ? (
                          <>
                            {item.startDate.slice(5, 10)}<br />~ {item.endDate?.slice(5, 10)}
                          </>
                        ) : '--'}
                      </td>
                      <td className="px-6 py-3 text-center text-text-sub">{item.sort}</td>
                      <td className="px-6 py-3">
                        <div className="flex items-center justify-center space-x-3 text-text-sub">
                          <button
                            onClick={() => openEditModal(item)}
                            className="hover:text-brand-gold transition-colors"
                            title="编辑"
                            type="button"
                          >
                            <Edit size={15} />
                          </button>
                          <button
                            onClick={() => handleDelete(item)}
                            className="hover:text-danger transition-colors"
                            title="删除"
                            type="button"
                          >
                            <Trash2 size={15} />
                          </button>
                        </div>
                      </td>
                    </tr>
                  );
                })
              )}
            </tbody>
          </table>
        </div>

        {/* 分页 */}
        {!loading && data.length > 0 && (
          <div className="px-6 py-3 border-t border-border-dark flex items-center justify-between text-xs text-text-sub">
            <span>第 {pageNum} / {totalPages} 页，共 {total} 条</span>
            <div className="flex items-center space-x-2">
              <button
                disabled={pageNum <= 1}
                onClick={() => setPageNum(p => Math.max(1, p - 1))}
                className="px-3 py-1.5 rounded border border-border-dark disabled:opacity-30 hover:border-text-sub transition-colors"
              >
                上一页
              </button>
              {Array.from({ length: Math.min(5, totalPages) }, (_, i) => {
                const start = Math.max(1, Math.min(pageNum - 2, totalPages - 4));
                const page = start + i;
                if (page > totalPages) return null;
                return (
                  <button
                    key={page}
                    onClick={() => setPageNum(page)}
                    className={`w-7 h-7 rounded text-xs transition-colors ${page === pageNum ? 'bg-brand-gold text-page-bg' : 'border border-border-dark text-text-sub hover:border-text-sub'}`}
                  >
                    {page}
                  </button>
                );
              })}
              <button
                disabled={pageNum >= totalPages}
                onClick={() => setPageNum(p => Math.min(totalPages, p + 1))}
                className="px-3 py-1.5 rounded border border-border-dark disabled:opacity-30 hover:border-text-sub transition-colors"
              >
                下一页
              </button>
              <select
                value={pageSize}
                onChange={e => { setPageSize(Number(e.target.value)); setPageNum(1); }}
                className="bg-page-bg border border-border-dark rounded px-2 py-1.5 text-text-sub text-xs outline-none"
              >
                <option value={10}>10条/页</option>
                <option value={20}>20条/页</option>
                <option value={50}>50条/页</option>
              </select>
            </div>
          </div>
        )}
      </div>

      {/* 新增 / 编辑弹窗 */}
      <Modal
        title={
          <span className="text-brand-gold font-serif tracking-wider">
            {editingItem ? '编辑活动' : '新增活动'}
          </span>
        }
        open={modalOpen}
        onCancel={() => setModalOpen(false)}
        onOk={handleSubmit}
        confirmLoading={submitting}
        okText={editingItem ? '保存' : '新增'}
        cancelText="取消"
        width={560}
        destroyOnClose
        styles={modalStyles}
      >
        <Form form={form} layout="vertical" initialValues={{ type: 'DISCOUNT', status: 0, sort: 0 }}>
          <Form.Item
            name="title"
            label={<span className="text-text-sub text-xs uppercase tracking-wider">活动标题</span>}
            rules={[{ required: true, message: '请输入活动标题' }]}
            className="mb-3"
          >
            <Input placeholder="如：周二特惠日" />
          </Form.Item>
          <Form.Item
            name="description"
            label={<span className="text-text-sub text-xs uppercase tracking-wider">活动描述</span>}
            className="mb-3"
          >
            <TextArea rows={3} placeholder="描述活动内容..." />
          </Form.Item>
          <div className="flex gap-3 mb-3">
            <Form.Item
              name="type"
              label={<span className="text-text-sub text-xs uppercase tracking-wider">活动类型</span>}
              rules={[{ required: true, message: '请选择活动类型' }]}
              className="flex-1 mb-0"
            >
              <Select options={TYPE_OPTIONS} {...darkSelectProps} />
            </Form.Item>
            <Form.Item
              name="status"
              label={<span className="text-text-sub text-xs uppercase tracking-wider">状态</span>}
              className="flex-1 mb-0"
            >
              <Select options={STATUS_OPTIONS} {...darkSelectProps} />
            </Form.Item>
          </div>
          <Form.Item
            name="dateRange"
            label={<span className="text-text-sub text-xs uppercase tracking-wider">活动时间</span>}
            className="mb-3"
          >
            <RangePicker
              showTime
              className="!w-full"
              style={{ backgroundColor: '#101014', borderColor: '#2A2A31', color: '#F4EBDD' }}
              popupStyle={{ backgroundColor: '#1A1A1F', border: '1px solid #2A2A31' }}
            />
          </Form.Item>
          <Form.Item
            name="sort"
            label={<span className="text-text-sub text-xs uppercase tracking-wider">排序</span>}
            tooltip="数值越小越靠前"
            className="mb-0"
          >
            <InputNumber min={0} className="!w-full" placeholder="0" />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
}
