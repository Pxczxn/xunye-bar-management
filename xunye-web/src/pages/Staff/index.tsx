'use client';

import { useEffect, useState, useCallback } from 'react';
import { Modal, Form, Input, Select, message } from 'antd';
import {
  getStaffPage,
  createStaff,
  updateStaff,
  updateStaffStatus,
  resetStaffPassword,
  deleteStaff,
} from '@/api/staff';
import type {
  StaffItem,
  StaffCreateParams,
  StaffUpdateParams,
} from '@/types/api';
import {
  Search,
  Plus,
  Edit,
  Trash2,
  RotateCcw,
  Lock,
  ToggleLeft,
  ToggleRight,
} from 'lucide-react';

const ROLE_MAP: Record<string, string> = {
  BOSS: '老板',
  MANAGER: '店长',
  STAFF: '员工',
};

const STATUS_MAP: Record<number, string> = {
  1: '启用',
  0: '禁用',
};

export default function StaffPage() {
  const [data, setData] = useState<StaffItem[]>([]);
  const [total, setTotal] = useState(0);
  const [loading, setLoading] = useState(false);
  const [pageNum, setPageNum] = useState(1);
  const [pageSize, setPageSize] = useState(10);
  const [keyword, setKeyword] = useState('');
  const [filterRole, setFilterRole] = useState<string | undefined>();
  const [filterStatus, setFilterStatus] = useState<string | undefined>();

  const [modalOpen, setModalOpen] = useState(false);
  const [editingItem, setEditingItem] = useState<StaffItem | null>(null);
  const [submitting, setSubmitting] = useState(false);
  const [form] = Form.useForm();

  const [resetModalOpen, setResetModalOpen] = useState(false);
  const [resetTarget, setResetTarget] = useState<StaffItem | null>(null);
  const [resetting, setResetting] = useState(false);
  const [resetForm] = Form.useForm();

  const darkSelectProps = {
    className: 'xunye-select',
    classNames: {
      popup: {
        root: 'xunye-select-dropdown',
      },
    },
    styles: {
      root: {
        width: '100%',
        backgroundColor: '#101014',
        border: '1px solid #2A2A31',
      },
      content: {
        color: '#F4EBDD',
      },
      suffix: {
        color: '#AFA79B',
      },
      popup: {
        root: {
          backgroundColor: '#1A1A1F',
          border: '1px solid #2A2A31',
        },
      },
    },
  } as const;

  const fetchData = useCallback(async (page = pageNum, size = pageSize) => {
    setLoading(true);
    try {
      const params: any = { pageNum: page, pageSize: size };
      if (keyword) params.keyword = keyword;
      if (filterRole) params.role = filterRole;
      if (filterStatus) params.status = filterStatus;
      const res = await getStaffPage(params);
      setData(res?.records ?? []);
      setTotal(res?.total ?? 0);
    } catch (err: any) {
      message.error(err.message || '获取员工列表失败');
      setData([]);
      setTotal(0);
    } finally {
      setLoading(false);
    }
  }, [pageNum, pageSize, keyword, filterRole, filterStatus]);

  useEffect(() => {
    fetchData();
  }, [fetchData]);

  const handleSearch = () => {
    setPageNum(1);
    fetchData(1, pageSize);
  };

  const handleReset = () => {
    setKeyword('');
    setFilterRole(undefined);
    setFilterStatus(undefined);
    setPageNum(1);
    setPageSize(10);
  };

  const handlePageChange = (page: number, size: number) => {
    setPageNum(page);
    setPageSize(size);
  };

  const openAddModal = () => {
    setEditingItem(null);
    form.resetFields();
    form.setFieldsValue({ status: 1, role: 'STAFF' });
    setModalOpen(true);
  };

  const openEditModal = (item: StaffItem) => {
    setEditingItem(item);
    form.setFieldsValue({
      nickname: item.nickname,
      role: item.role,
      status: item.status,
    });
    setModalOpen(true);
  };

  const handleSubmit = async () => {
    try {
      const values = await form.validateFields();
      setSubmitting(true);
      if (editingItem) {
        const payload: StaffUpdateParams = {
          nickname: values.nickname,
          role: values.role,
          status: values.status,
        };
        await updateStaff(editingItem.id, payload);
        message.success('修改成功');
      } else {
        const payload: StaffCreateParams = {
          username: values.username,
          password: values.password,
          nickname: values.nickname,
          role: values.role,
          status: values.status,
        };
        await createStaff(payload);
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

  const handleToggleStatus = (item: StaffItem) => {
    const nextStatus = item.status === 1 ? 0 : 1;
    const label = STATUS_MAP[nextStatus];
    Modal.confirm({
      title: `确认${label}`,
      content: `确定要${label}「${item.nickname}」吗？`,
      okText: '确认',
      cancelText: '取消',
      onOk: async () => {
        try {
          await updateStaffStatus(item.id, nextStatus);
          message.success(`${label}成功`);
          fetchData();
        } catch (err: any) {
          message.error(err.message || `${label}失败`);
        }
      },
    });
  };

  const openResetModal = (item: StaffItem) => {
    setResetTarget(item);
    resetForm.resetFields();
    setResetModalOpen(true);
  };

  const handleResetPassword = async () => {
    try {
      const values = await resetForm.validateFields();
      setResetting(true);
      await resetStaffPassword(resetTarget!.id, values.password);
      message.success('密码重置成功');
      setResetModalOpen(false);
    } catch (err: any) {
      if (err?.errorFields) return;
      message.error(err.message || '密码重置失败');
    } finally {
      setResetting(false);
    }
  };

  const handleDelete = (item: StaffItem) => {
    Modal.confirm({
      title: '确认删除',
      content: `确定要删除员工「${item.nickname}」吗？此操作不可恢复。`,
      okText: '删除',
      okButtonProps: { danger: true },
      cancelText: '取消',
      onOk: async () => {
        try {
          await deleteStaff(item.id);
          message.success('删除成功');
          fetchData();
        } catch (err: any) {
          message.error(err.message || '删除失败');
        }
      },
    });
  };

  const totalPages = Math.ceil(total / pageSize);

  return (
    <div className="space-y-6">
      {/* 标题栏 */}
      <div className="flex flex-col sm:flex-row justify-between items-start sm:items-end gap-6 mb-6">
        <div>
          <h1 className="text-2xl font-serif font-bold text-text-main tracking-wider mb-1">员工账号</h1>
          <p className="text-[10px] text-brand-gold uppercase tracking-widest font-medium">STAFF</p>
        </div>
        <div className="shrink-0">
          <button
            onClick={openAddModal}
            className="flex items-center space-x-2 bg-brand-gold text-page-bg px-4 py-2 rounded-lg font-semibold text-sm hover:bg-brand-gold/90 transition-colors tracking-widest uppercase"
          >
            <Plus size={16} />
            <span>新增员工</span>
          </button>
        </div>
      </div>

      {/* 搜索筛选区 */}
      <div className="bg-card-bg border border-border-dark rounded-xl p-4">
        <div className="flex flex-col sm:flex-row items-end gap-3 flex-wrap">
          <div className="w-full sm:w-56">
            <label className="block text-[10px] text-text-weak uppercase tracking-wider mb-1">关键词</label>
            <Input
              placeholder="账号/昵称"
              value={keyword}
              onChange={(e) => setKeyword(e.target.value)}
              allowClear
              className="!bg-sidebar-bg !border-border-dark !text-text-main !placeholder-text-weak"
            />
          </div>
          <div className="w-full sm:w-32">
            <label className="block text-[10px] text-text-weak uppercase tracking-wider mb-1">角色</label>
            <Select
              placeholder="全部角色"
              value={filterRole}
              onChange={(v) => setFilterRole(v)}
              allowClear
              {...darkSelectProps}
              options={[
                { label: '老板', value: 'BOSS' },
                { label: '店长', value: 'MANAGER' },
                { label: '员工', value: 'STAFF' },
              ]}
            />
          </div>
          <div className="w-full sm:w-32">
            <label className="block text-[10px] text-text-weak uppercase tracking-wider mb-1">状态</label>
            <Select
              placeholder="全部状态"
              value={filterStatus}
              onChange={(v) => setFilterStatus(v)}
              allowClear
              {...darkSelectProps}
              options={[
                { label: '启用', value: '1' },
                { label: '禁用', value: '0' },
              ]}
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

      {/* 数据表格 */}
      <div className="bg-card-bg border border-border-dark rounded-xl overflow-hidden shadow-xl">
        <div className="p-4 border-b border-border-dark flex justify-between items-center">
          <h3 className="text-sm font-semibold text-text-main">员工列表</h3>
          <span className="text-xs text-text-weak">共 {total} 条</span>
        </div>
        <div className="overflow-x-auto hide-scrollbar">
          <table className="w-full text-left text-sm whitespace-nowrap">
            <thead>
              <tr className="bg-sidebar-bg text-text-weak uppercase text-[10px] tracking-wider">
                <th className="px-6 py-3 font-medium">账号</th>
                <th className="px-6 py-3 font-medium">昵称</th>
                <th className="px-6 py-3 font-medium text-center">角色</th>
                <th className="px-6 py-3 font-medium text-center">状态</th>
                <th className="px-6 py-3 font-medium">最后登录</th>
                <th className="px-6 py-3 font-medium">创建时间</th>
                <th className="px-6 py-3 font-medium text-center">操作</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-border-dark/50 text-xs">
              {loading ? (
                <tr>
                  <td colSpan={7} className="px-6 py-16 text-center text-text-weak">
                    <div className="flex items-center justify-center space-x-2">
                      <div className="w-4 h-4 border-2 border-brand-gold border-t-transparent rounded-full animate-spin" />
                      <span>加载中...</span>
                    </div>
                  </td>
                </tr>
              ) : data.length === 0 ? (
                <tr>
                  <td colSpan={7} className="px-6 py-16 text-center text-text-weak font-serif italic">
                    暂无员工数据
                  </td>
                </tr>
              ) : (
                data.map((item) => (
                  <tr key={item.id} className="hover:bg-border-dark/20 transition-colors">
                    <td className="px-6 py-3 font-medium text-text-main tracking-wide">{item.username}</td>
                    <td className="px-6 py-3 text-text-sub tracking-wider">{item.nickname}</td>
                    <td className="px-6 py-3 text-center">
                      <span className="inline-flex items-center px-2 py-0.5 rounded text-[10px] bg-brand-gold/10 text-brand-gold">
                        {ROLE_MAP[item.role]}
                      </span>
                    </td>
                    <td className="px-6 py-3 text-center">
                      {item.status === 1 ? (
                        <span className="inline-flex items-center px-2 py-0.5 rounded text-[10px] bg-success/10 text-success">
                          启用
                        </span>
                      ) : (
                        <span className="inline-flex items-center px-2 py-0.5 rounded text-[10px] bg-border-dark text-text-sub">
                          禁用
                        </span>
                      )}
                    </td>
                    <td className="px-6 py-3 text-text-sub">{item.lastLoginAt || '-'}</td>
                    <td className="px-6 py-3 text-text-sub">{item.createdAt}</td>
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
                          onClick={() => handleToggleStatus(item)}
                          className={item.status === 1 ? 'hover:text-danger transition-colors' : 'hover:text-success transition-colors'}
                          title={item.status === 1 ? '禁用' : '启用'}
                          type="button"
                        >
                          {item.status === 1 ? <ToggleRight size={15} /> : <ToggleLeft size={15} />}
                        </button>
                        <button
                          onClick={() => openResetModal(item)}
                          className="hover:text-brand-gold transition-colors"
                          title="重置密码"
                          type="button"
                        >
                          <Lock size={15} />
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
                ))
              )}
            </tbody>
          </table>
        </div>

        {/* 分页 */}
        {total > 0 && (
          <div className="p-4 border-t border-border-dark flex flex-col sm:flex-row justify-between items-center gap-3">
            <span className="text-xs text-text-weak">
              第 {pageNum} / {totalPages} 页，共 {total} 条
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

      {/* 新增 / 编辑弹窗 */}
      <Modal
        title={
          <span className="text-brand-gold font-serif tracking-wider">
            {editingItem ? '编辑员工' : '新增员工'}
          </span>
        }
        open={modalOpen}
        onCancel={() => setModalOpen(false)}
        onOk={handleSubmit}
        confirmLoading={submitting}
        okText={editingItem ? '保存' : '新增'}
        cancelText="取消"
        width={520}
        destroyOnHidden
        styles={{
          root: { background: 'transparent' },
          wrapper: { background: 'rgba(0, 0, 0, 0.65)' },
          container: { background: '#1A1A1F' },
          header: { background: '#1A1A1F', borderBottom: '1px solid #2A2A31', paddingBottom: 16 },
          body: { background: '#1A1A1F', paddingTop: 20 },
          footer: { background: '#1A1A1F', borderTop: '1px solid #2A2A31' },
        }}
      >
        <Form form={form} layout="vertical">
          {!editingItem && (
            <>
              <Form.Item
                name="username"
                label={<span className="text-text-sub text-xs uppercase tracking-wider">账号</span>}
                rules={[{ required: true, message: '请输入账号' }]}
                className="mb-3"
              >
                <Input placeholder="登录账号" />
              </Form.Item>
              <Form.Item
                name="password"
                label={<span className="text-text-sub text-xs uppercase tracking-wider">密码</span>}
                rules={[{ required: true, message: '请输入密码' }]}
                className="mb-3"
              >
                <Input.Password placeholder="登录密码" />
              </Form.Item>
            </>
          )}
          <Form.Item
            name="nickname"
            label={<span className="text-text-sub text-xs uppercase tracking-wider">昵称</span>}
            rules={[{ required: true, message: '请输入昵称' }]}
            className="mb-3"
          >
            <Input placeholder="显示昵称" />
          </Form.Item>
          <Form.Item
            name="role"
            label={<span className="text-text-sub text-xs uppercase tracking-wider">角色</span>}
            rules={[{ required: true, message: '请选择角色' }]}
            className="mb-3"
          >
            <Select
              {...darkSelectProps}
              options={[
                { label: '老板', value: 'BOSS' },
                { label: '店长', value: 'MANAGER' },
                { label: '员工', value: 'STAFF' },
              ]}
            />
          </Form.Item>
          <Form.Item
            name="status"
            label={<span className="text-text-sub text-xs uppercase tracking-wider">状态</span>}
            rules={[{ required: true, message: '请选择状态' }]}
            className="mb-0"
          >
            <Select
              {...darkSelectProps}
              options={[
                { label: '启用', value: 1 },
                { label: '禁用', value: 0 },
              ]}
            />
          </Form.Item>
        </Form>
      </Modal>

      {/* 重置密码弹窗 */}
      <Modal
        title={
          <span className="text-brand-gold font-serif tracking-wider">
            重置密码 - {resetTarget?.nickname}
          </span>
        }
        open={resetModalOpen}
        onCancel={() => setResetModalOpen(false)}
        onOk={handleResetPassword}
        confirmLoading={resetting}
        okText="确认重置"
        cancelText="取消"
        width={400}
        destroyOnHidden
        styles={{
          root: { background: 'transparent' },
          wrapper: { background: 'rgba(0, 0, 0, 0.65)' },
          container: { background: '#1A1A1F' },
          header: { background: '#1A1A1F', borderBottom: '1px solid #2A2A31', paddingBottom: 16 },
          body: { background: '#1A1A1F', paddingTop: 20 },
          footer: { background: '#1A1A1F', borderTop: '1px solid #2A2A31' },
        }}
      >
        <Form form={resetForm} layout="vertical">
          <Form.Item
            name="password"
            label={<span className="text-text-sub text-xs uppercase tracking-wider">新密码</span>}
            rules={[{ required: true, message: '请输入新密码' }]}
            className="mb-0"
          >
            <Input.Password placeholder="请输入新密码" />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
}
