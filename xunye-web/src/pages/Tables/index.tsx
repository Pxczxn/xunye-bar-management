'use client';

import { useEffect, useState, useCallback } from 'react';
import { Modal, Form, Input, Select, InputNumber, message, Popconfirm } from 'antd';
import {
  getTableAreas,
  createTableArea,
  updateTableArea,
  deleteTableArea,
  getTablePage,
  createTable,
  updateTable,
  updateTableStatus,
  clearTable,
  deleteTable,
} from '@/api/table';
import type {
  TableArea,
  TableAreaSaveParams,
  BarTable,
  BarTableSaveParams,
} from '@/types/api';
import {
  Plus,
  Search,
  RotateCcw,
  RefreshCw,
  Edit,
  Trash2,
  ToggleRight,
  Eraser,
} from 'lucide-react';

const TABLE_STATUS_MAP: Record<string, { label: string; color: string }> = {
  EMPTY: { label: '空闲', color: 'text-success' },
  USING: { label: '使用中', color: 'text-brand-gold' },
  CLEANING: { label: '清洁中', color: 'text-text-sub' },
  DISABLED: { label: '停用', color: 'text-danger' },
};

const TABLE_STATUS_OPTIONS = [
  { label: '全部状态', value: '' },
  { label: '空闲', value: 'EMPTY' },
  { label: '使用中', value: 'USING' },
  { label: '清洁中', value: 'CLEANING' },
  { label: '停用', value: 'DISABLED' },
];

const TABLE_STATUS_FORM_OPTIONS = [
  { label: '空闲', value: 'EMPTY' },
  { label: '使用中', value: 'USING' },
  { label: '清洁中', value: 'CLEANING' },
  { label: '停用', value: 'DISABLED' },
];

const AREA_STATUS_OPTIONS = [
  { label: '启用', value: 1 },
  { label: '禁用', value: 0 },
];

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

const modalStyles = {
  root: { background: 'transparent' },
  wrapper: { background: 'rgba(0, 0, 0, 0.65)' },
  container: { background: '#1A1A1F' },
  header: { background: '#1A1A1F', borderBottom: '1px solid #2A2A31', paddingBottom: 16 },
  body: { background: '#1A1A1F', paddingTop: 20 },
  footer: { background: '#1A1A1F', borderTop: '1px solid #2A2A31' },
};

const displayValue = (value: string | number | null | undefined) =>
  value !== null && value !== undefined && value !== '' ? String(value) : '-';

export default function TablesPage() {
  const [activeTab, setActiveTab] = useState<'tables' | 'areas'>('tables');

  const [areas, setAreas] = useState<TableArea[]>([]);

  const [tables, setTables] = useState<BarTable[]>([]);
  const [tablesTotal, setTablesTotal] = useState(0);
  const [tablesLoading, setTablesLoading] = useState(false);
  const [pageNum, setPageNum] = useState(1);
  const [pageSize, setPageSize] = useState(10);
  const [filterKeyword, setFilterKeyword] = useState('');
  const [filterAreaId, setFilterAreaId] = useState<number | undefined>();
  const [filterStatus, setFilterStatus] = useState<string | undefined>();

  const [tableModalOpen, setTableModalOpen] = useState(false);
  const [editingTable, setEditingTable] = useState<BarTable | null>(null);
  const [tableSubmitting, setTableSubmitting] = useState(false);
  const [tableForm] = Form.useForm();

  const [statusModalOpen, setStatusModalOpen] = useState(false);
  const [statusTarget, setStatusTarget] = useState<BarTable | null>(null);
  const [statusSubmitting, setStatusSubmitting] = useState(false);
  const [statusForm] = Form.useForm();

  const [areasLoading, setAreasLoading] = useState(false);
  const [areaModalOpen, setAreaModalOpen] = useState(false);
  const [editingArea, setEditingArea] = useState<TableArea | null>(null);
  const [areaSubmitting, setAreaSubmitting] = useState(false);
  const [areaForm] = Form.useForm();

  const fetchAreas = useCallback(async () => {
    setAreasLoading(true);
    try {
      const data = await getTableAreas();
      setAreas(Array.isArray(data) ? data : []);
    } catch (err: any) {
      message.error(err.message || '获取区域列表失败');
      setAreas([]);
    } finally {
      setAreasLoading(false);
    }
  }, []);

  const fetchTables = useCallback(async (page = pageNum, size = pageSize) => {
    setTablesLoading(true);
    try {
      const params: any = { pageNum: page, pageSize: size };
      if (filterKeyword) params.keyword = filterKeyword;
      if (filterAreaId !== undefined) params.areaId = filterAreaId;
      if (filterStatus) params.status = filterStatus;
      const res = await getTablePage(params);
      setTables(res?.records ?? []);
      setTablesTotal(res?.total ?? 0);
    } catch (err: any) {
      message.error(err.message || '获取桌台列表失败');
      setTables([]);
      setTablesTotal(0);
    } finally {
      setTablesLoading(false);
    }
  }, [pageNum, pageSize, filterKeyword, filterAreaId, filterStatus]);

  useEffect(() => {
    fetchAreas();
  }, [fetchAreas]);

  useEffect(() => {
    if (activeTab === 'tables') {
      fetchTables();
    }
  }, [activeTab, fetchTables]);

  const handleRefresh = () => {
    if (activeTab === 'tables') {
      fetchTables();
    } else {
      fetchAreas();
    }
  };

  const handleSearch = () => {
    setPageNum(1);
    fetchTables(1, pageSize);
  };

  const handleReset = () => {
    setFilterKeyword('');
    setFilterAreaId(undefined);
    setFilterStatus(undefined);
    setPageNum(1);
    setPageSize(10);
  };

  const handlePageChange = (page: number, size: number) => {
    setPageNum(page);
    setPageSize(size);
  };

  const openTableModal = (record?: BarTable) => {
    setEditingTable(record || null);
    tableForm.resetFields();
    if (record) {
      tableForm.setFieldsValue({
        areaId: record.areaId,
        name: record.name,
        capacity: record.capacity,
        status: record.status,
      });
    }
    setTableModalOpen(true);
  };

  const handleTableSubmit = async () => {
    try {
      const values = await tableForm.validateFields();
      setTableSubmitting(true);
      const payload: BarTableSaveParams = {
        areaId: values.areaId,
        name: values.name,
        capacity: values.capacity,
        status: values.status,
      };
      if (editingTable) {
        await updateTable(editingTable.id, payload);
        message.success('桌台修改成功');
      } else {
        await createTable(payload);
        message.success('桌台新增成功');
      }
      setTableModalOpen(false);
      fetchTables();
    } catch (err: any) {
      if (err?.errorFields) return;
      message.error(err.message || '操作失败');
    } finally {
      setTableSubmitting(false);
    }
  };

  const handleDeleteTable = async (id: number) => {
    try {
      await deleteTable(id);
      message.success('桌台删除成功');
      fetchTables();
    } catch (err: any) {
      message.error(err.message || '删除失败');
    }
  };

  const handleClearTable = async (record: BarTable) => {
    try {
      await clearTable(record.id);
      message.success(`${record.name} 已清台`);
      fetchTables();
    } catch (err: any) {
      message.error(err.message || '清台失败');
    }
  };

  const openStatusModal = (record: BarTable) => {
    setStatusTarget(record);
    statusForm.resetFields();
    statusForm.setFieldsValue({ status: record.status });
    setStatusModalOpen(true);
  };

  const handleStatusSubmit = async () => {
    if (!statusTarget) return;
    try {
      const values = await statusForm.validateFields();
      setStatusSubmitting(true);
      await updateTableStatus(statusTarget.id, { status: values.status });
      message.success('桌台状态修改成功');
      setStatusModalOpen(false);
      fetchTables();
    } catch (err: any) {
      if (err?.errorFields) return;
      message.error(err.message || '状态修改失败');
    } finally {
      setStatusSubmitting(false);
    }
  };

  const openAreaModal = (record?: TableArea) => {
    setEditingArea(record || null);
    areaForm.resetFields();
    if (record) {
      areaForm.setFieldsValue({
        name: record.name,
        sort: record.sort,
        status: record.status,
      });
    }
    setAreaModalOpen(true);
  };

  const handleAreaSubmit = async () => {
    try {
      const values = await areaForm.validateFields();
      setAreaSubmitting(true);
      const payload: TableAreaSaveParams = {
        name: values.name,
        sort: values.sort,
        status: values.status,
      };
      if (editingArea) {
        await updateTableArea(editingArea.id, payload);
        message.success('区域修改成功');
      } else {
        await createTableArea(payload);
        message.success('区域新增成功');
      }
      setAreaModalOpen(false);
      fetchAreas();
    } catch (err: any) {
      if (err?.errorFields) return;
      message.error(err.message || '操作失败');
    } finally {
      setAreaSubmitting(false);
    }
  };

  const handleDeleteArea = async (id: number) => {
    try {
      await deleteTableArea(id);
      message.success('区域删除成功');
      fetchAreas();
      fetchTables();
    } catch (err: any) {
      message.error(err.message || '删除失败');
    }
  };

  const getStatusDisplay = (status: string) => {
    const info = TABLE_STATUS_MAP[status];
    if (!info) return <span className="text-text-sub">{status}</span>;
    return <span className={info.color}>{info.label}</span>;
  };

  const totalPages = Math.ceil(tablesTotal / pageSize);

  const areaFilterOptions = [
    { label: '全部区域', value: '' },
    ...areas.map((a) => ({ label: a.name, value: a.id })),
  ];

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row justify-between items-start sm:items-end gap-6 mb-6">
        <div>
          <h1 className="text-2xl font-serif font-bold text-text-main tracking-wider mb-1">桌台区域</h1>
          <p className="text-[10px] text-brand-gold uppercase tracking-widest font-medium">Tables</p>
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
            onClick={() => activeTab === 'tables' ? openTableModal() : openAreaModal()}
            className="flex items-center space-x-2 bg-brand-gold text-page-bg px-4 py-2 rounded-lg font-semibold text-sm hover:bg-brand-gold/90 transition-colors tracking-widest uppercase"
          >
            <Plus size={16} />
            <span>{activeTab === 'tables' ? '新增桌台' : '新增区域'}</span>
          </button>
        </div>
      </div>

      <div className="flex border-b border-border-dark">
        <button
          onClick={() => setActiveTab('tables')}
          className={`px-6 py-3 text-sm font-medium transition-colors ${
            activeTab === 'tables'
              ? 'text-brand-gold border-b-2 border-brand-gold'
              : 'text-text-sub hover:text-text-main'
          }`}
        >
          桌台列表
        </button>
        <button
          onClick={() => setActiveTab('areas')}
          className={`px-6 py-3 text-sm font-medium transition-colors ${
            activeTab === 'areas'
              ? 'text-brand-gold border-b-2 border-brand-gold'
              : 'text-text-sub hover:text-text-main'
          }`}
        >
          区域管理
        </button>
      </div>

      {activeTab === 'tables' && (
        <>
          <div className="bg-card-bg border border-border-dark rounded-xl p-4">
            <div className="flex flex-col sm:flex-row items-end gap-3 flex-wrap">
              <div className="w-full sm:w-56">
                <label className="block text-[10px] text-text-weak uppercase tracking-wider mb-1">桌台名称</label>
                <Input
                  placeholder="输入关键词搜索"
                  value={filterKeyword}
                  onChange={(e) => setFilterKeyword(e.target.value)}
                  allowClear
                  className="!bg-sidebar-bg !border-border-dark !text-text-main !placeholder-text-weak"
                />
              </div>
              <div className="w-full sm:w-40">
                <label className="block text-[10px] text-text-weak uppercase tracking-wider mb-1">所属区域</label>
                <Select
                  placeholder="全部区域"
                  value={filterAreaId}
                  onChange={(v) => setFilterAreaId(v || undefined)}
                  allowClear
                  {...darkSelectProps}
                  options={areaFilterOptions}
                />
              </div>
              <div className="w-full sm:w-36">
                <label className="block text-[10px] text-text-weak uppercase tracking-wider mb-1">桌台状态</label>
                <Select
                  placeholder="全部状态"
                  value={filterStatus}
                  onChange={(v) => setFilterStatus(v || undefined)}
                  allowClear
                  {...darkSelectProps}
                  options={TABLE_STATUS_OPTIONS}
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
              <h3 className="text-sm font-semibold text-text-main">桌台列表</h3>
              <span className="text-xs text-text-weak">共 {tablesTotal} 条</span>
            </div>
            <div className="overflow-x-auto hide-scrollbar">
              <table className="w-full text-left text-sm whitespace-nowrap">
                <thead>
                  <tr className="bg-sidebar-bg text-text-weak uppercase text-[10px] tracking-wider">
                    <th className="px-6 py-3 font-medium">桌台名称</th>
                    <th className="px-6 py-3 font-medium">所属区域</th>
                    <th className="px-6 py-3 font-medium text-right">容纳人数</th>
                    <th className="px-6 py-3 font-medium">状态</th>
                    <th className="px-6 py-3 font-medium">创建时间</th>
                    <th className="px-6 py-3 font-medium text-right">操作</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-border-dark/50 text-xs">
                  {tablesLoading ? (
                    <tr>
                      <td colSpan={6} className="px-6 py-16 text-center text-text-weak">
                        <div className="flex items-center justify-center space-x-2">
                          <div className="w-4 h-4 border-2 border-brand-gold border-t-transparent rounded-full animate-spin" />
                          <span>加载中...</span>
                        </div>
                      </td>
                    </tr>
                  ) : tables.length === 0 ? (
                    <tr>
                      <td colSpan={6} className="px-6 py-16 text-center text-text-weak font-serif italic">
                        暂无桌台数据
                      </td>
                    </tr>
                  ) : (
                    tables.map((item) => (
                      <tr key={item.id} className="hover:bg-border-dark/20 transition-colors">
                        <td className="px-6 py-3 text-text-main font-medium tracking-wide">{displayValue(item.name)}</td>
                        <td className="px-6 py-3 text-text-sub">{displayValue(item.areaName)}</td>
                        <td className="px-6 py-3 text-right text-text-main font-sans tracking-wide">{item.capacity}</td>
                        <td className="px-6 py-3">{getStatusDisplay(item.status)}</td>
                        <td className="px-6 py-3 text-text-sub">{displayValue(item.createdAt)}</td>
                        <td className="px-6 py-3 text-right">
                          <div className="flex items-center justify-end gap-2">
                            <button
                              onClick={() => openTableModal(item)}
                              className="p-1.5 rounded-md hover:bg-border-dark/40 text-text-sub hover:text-brand-gold transition-colors"
                              title="编辑"
                            >
                              <Edit size={14} />
                            </button>
                            <button
                              onClick={() => openStatusModal(item)}
                              className="p-1.5 rounded-md hover:bg-border-dark/40 text-text-sub hover:text-brand-gold transition-colors"
                              title="修改状态"
                            >
                              <ToggleRight size={14} />
                            </button>
                            {item.status === 'USING' && (
                              <Popconfirm
                                title="确认清台"
                                description="清台会将桌台恢复为空闲；若仍有待处理或制作中的订单，系统会阻止操作。"
                                onConfirm={() => handleClearTable(item)}
                                okText="清台"
                                cancelText="取消"
                              >
                                <button
                                  className="p-1.5 rounded-md hover:bg-border-dark/40 text-text-sub hover:text-success transition-colors"
                                  title="清台"
                                >
                                  <Eraser size={14} />
                                </button>
                              </Popconfirm>
                            )}
                            <Popconfirm
                              title="确认删除"
                              description="确定要删除该桌台吗？"
                              onConfirm={() => handleDeleteTable(item.id)}
                              okText="删除"
                              cancelText="取消"
                            >
                              <button
                                className="p-1.5 rounded-md hover:bg-border-dark/40 text-text-sub hover:text-danger transition-colors"
                                title="删除"
                              >
                                <Trash2 size={14} />
                              </button>
                            </Popconfirm>
                          </div>
                        </td>
                      </tr>
                    ))
                  )}
                </tbody>
              </table>
            </div>

            {tablesTotal > 0 && (
              <div className="p-4 border-t border-border-dark flex flex-col sm:flex-row justify-between items-center gap-3">
                <span className="text-xs text-text-weak">
                  第 {pageNum} / {totalPages} 页，共 {tablesTotal} 条
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
                      <option key={s} value={s}>{s} 条/页</option>
                    ))}
                  </select>
                </div>
              </div>
            )}
          </div>
        </>
      )}

      {activeTab === 'areas' && (
        <div className="bg-card-bg border border-border-dark rounded-xl overflow-hidden shadow-xl">
          <div className="p-4 border-b border-border-dark flex justify-between items-center">
            <h3 className="text-sm font-semibold text-text-main">区域列表</h3>
            <span className="text-xs text-text-weak">共 {areas.length} 条</span>
          </div>
          <div className="overflow-x-auto hide-scrollbar">
            <table className="w-full text-left text-sm whitespace-nowrap">
              <thead>
                <tr className="bg-sidebar-bg text-text-weak uppercase text-[10px] tracking-wider">
                  <th className="px-6 py-3 font-medium">区域名称</th>
                  <th className="px-6 py-3 font-medium text-right">排序</th>
                  <th className="px-6 py-3 font-medium">状态</th>
                  <th className="px-6 py-3 font-medium text-right">操作</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-border-dark/50 text-xs">
                {areasLoading ? (
                  <tr>
                    <td colSpan={4} className="px-6 py-16 text-center text-text-weak">
                      <div className="flex items-center justify-center space-x-2">
                        <div className="w-4 h-4 border-2 border-brand-gold border-t-transparent rounded-full animate-spin" />
                        <span>加载中...</span>
                      </div>
                    </td>
                  </tr>
                ) : areas.length === 0 ? (
                  <tr>
                    <td colSpan={4} className="px-6 py-16 text-center text-text-weak font-serif italic">
                      暂无区域数据
                    </td>
                  </tr>
                ) : (
                  areas.map((item) => (
                    <tr key={item.id} className="hover:bg-border-dark/20 transition-colors">
                      <td className="px-6 py-3 text-text-main font-medium tracking-wide">{displayValue(item.name)}</td>
                      <td className="px-6 py-3 text-right text-text-main font-sans tracking-wide">{item.sort}</td>
                      <td className="px-6 py-3">
                        <span className={item.status === 1 ? 'text-success' : 'text-danger'}>
                          {item.status === 1 ? '启用' : '禁用'}
                        </span>
                      </td>
                      <td className="px-6 py-3 text-right">
                        <div className="flex items-center justify-end gap-2">
                          <button
                            onClick={() => openAreaModal(item)}
                            className="p-1.5 rounded-md hover:bg-border-dark/40 text-text-sub hover:text-brand-gold transition-colors"
                            title="编辑"
                          >
                            <Edit size={14} />
                          </button>
                          <Popconfirm
                            title="确认删除"
                            description="确定要删除该区域吗？"
                            onConfirm={() => handleDeleteArea(item.id)}
                            okText="删除"
                            cancelText="取消"
                          >
                            <button
                              className="p-1.5 rounded-md hover:bg-border-dark/40 text-text-sub hover:text-danger transition-colors"
                              title="删除"
                            >
                              <Trash2 size={14} />
                            </button>
                          </Popconfirm>
                        </div>
                      </td>
                    </tr>
                  ))
                )}
              </tbody>
            </table>
          </div>
        </div>
      )}

      <Modal
        title={<span className="text-brand-gold font-serif tracking-wider">{editingTable ? '编辑桌台' : '新增桌台'}</span>}
        open={tableModalOpen}
        onCancel={() => setTableModalOpen(false)}
        onOk={handleTableSubmit}
        confirmLoading={tableSubmitting}
        okText={editingTable ? '保存修改' : '确认新增'}
        cancelText="取消"
        width={520}
        destroyOnHidden
        styles={modalStyles}
      >
        <Form form={tableForm} layout="vertical">
          <Form.Item
            name="name"
            label={<span className="text-text-sub text-xs uppercase tracking-wider">桌台名称</span>}
            rules={[{ required: true, message: '请输入桌台名称' }]}
            className="mb-4"
          >
            <Input placeholder="请输入桌台名称" className="!bg-sidebar-bg !border-border-dark !text-text-main !placeholder-text-weak" />
          </Form.Item>
          <Form.Item
            name="areaId"
            label={<span className="text-text-sub text-xs uppercase tracking-wider">所属区域</span>}
            rules={[{ required: true, message: '请选择所属区域' }]}
            className="mb-4"
          >
            <Select
              placeholder="请选择所属区域"
              {...darkSelectProps}
              options={areas.map((a) => ({ label: a.name, value: a.id }))}
            />
          </Form.Item>
          <Form.Item
            name="capacity"
            label={<span className="text-text-sub text-xs uppercase tracking-wider">容纳人数</span>}
            rules={[{ required: true, message: '请输入容纳人数' }]}
            className="mb-4"
          >
            <InputNumber min={1} className="!w-full" placeholder="请输入容纳人数" />
          </Form.Item>
          <Form.Item
            name="status"
            label={<span className="text-text-sub text-xs uppercase tracking-wider">桌台状态</span>}
            rules={[{ required: true, message: '请选择桌台状态' }]}
            className="mb-0"
          >
            <Select
              placeholder="请选择桌台状态"
              {...darkSelectProps}
              options={TABLE_STATUS_FORM_OPTIONS}
            />
          </Form.Item>
        </Form>
      </Modal>

      <Modal
        title={<span className="text-brand-gold font-serif tracking-wider">修改桌台状态</span>}
        open={statusModalOpen}
        onCancel={() => setStatusModalOpen(false)}
        onOk={handleStatusSubmit}
        confirmLoading={statusSubmitting}
        okText="确认修改"
        cancelText="取消"
        width={400}
        destroyOnHidden
        styles={modalStyles}
      >
        {statusTarget && (
          <div className="mb-4 text-text-sub text-sm">
            当前桌台：<span className="text-text-main font-medium">{statusTarget.name}</span>
          </div>
        )}
        <Form form={statusForm} layout="vertical">
          <Form.Item
            name="status"
            label={<span className="text-text-sub text-xs uppercase tracking-wider">目标状态</span>}
            rules={[{ required: true, message: '请选择目标状态' }]}
            className="mb-0"
          >
            <Select
              placeholder="请选择目标状态"
              {...darkSelectProps}
              options={TABLE_STATUS_FORM_OPTIONS}
            />
          </Form.Item>
        </Form>
      </Modal>

      <Modal
        title={<span className="text-brand-gold font-serif tracking-wider">{editingArea ? '编辑区域' : '新增区域'}</span>}
        open={areaModalOpen}
        onCancel={() => setAreaModalOpen(false)}
        onOk={handleAreaSubmit}
        confirmLoading={areaSubmitting}
        okText={editingArea ? '保存修改' : '确认新增'}
        cancelText="取消"
        width={480}
        destroyOnHidden
        styles={modalStyles}
      >
        <Form form={areaForm} layout="vertical">
          <Form.Item
            name="name"
            label={<span className="text-text-sub text-xs uppercase tracking-wider">区域名称</span>}
            rules={[{ required: true, message: '请输入区域名称' }]}
            className="mb-4"
          >
            <Input placeholder="请输入区域名称" className="!bg-sidebar-bg !border-border-dark !text-text-main !placeholder-text-weak" />
          </Form.Item>
          <Form.Item
            name="sort"
            label={<span className="text-text-sub text-xs uppercase tracking-wider">排序号</span>}
            rules={[{ required: true, message: '请输入排序号' }]}
            className="mb-4"
          >
            <InputNumber min={0} className="!w-full" placeholder="请输入排序号" />
          </Form.Item>
          <Form.Item
            name="status"
            label={<span className="text-text-sub text-xs uppercase tracking-wider">状态</span>}
            rules={[{ required: true, message: '请选择状态' }]}
            className="mb-0"
          >
            <Select
              placeholder="请选择状态"
              {...darkSelectProps}
              options={AREA_STATUS_OPTIONS}
            />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
}
