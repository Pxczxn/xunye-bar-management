'use client';

import { useEffect, useState, useCallback } from 'react';
import { Modal, Form, Input, InputNumber, Select, App } from 'antd';
import {
  getCategoryList,
  createCategory,
  updateCategory,
  deleteCategory,
} from '@/api/category';
import type { ProductCategory, CategoryFormData } from '@/types/api';
import { Plus, Edit, Trash2, RotateCcw } from 'lucide-react';
import { darkSelectProps } from '@/constants/antdTheme';

export default function CategoriesPage() {
  const { message } = App.useApp();
  const [data, setData] = useState<ProductCategory[]>([]);
  const [loading, setLoading] = useState(false);
  const [modalOpen, setModalOpen] = useState(false);
  const [editingItem, setEditingItem] = useState<ProductCategory | null>(null);
  const [submitting, setSubmitting] = useState(false);
  const [form] = Form.useForm();

  const fetchData = useCallback(async () => {
    setLoading(true);
    try {
      const res = await getCategoryList();
      setData(Array.isArray(res) ? res : []);
    } catch (err: any) {
      message.error(err.message || '获取分类列表失败');
      setData([]);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchData();
  }, [fetchData]);

  const openAddModal = () => {
    setEditingItem(null);
    form.resetFields();
    form.setFieldsValue({ sort: 0, status: 1 });
    setModalOpen(true);
  };

  const openEditModal = (item: ProductCategory) => {
    setEditingItem(item);
    form.setFieldsValue({
      name: item.name,
      sort: item.sort,
      status: item.status,
    });
    setModalOpen(true);
  };

  const handleSubmit = async () => {
    try {
      const values = await form.validateFields();
      setSubmitting(true);
      const payload: CategoryFormData = {
        name: values.name,
        sort: values.sort || 0,
        status: values.status,
      };
      if (editingItem) {
        await updateCategory(editingItem.id, payload);
        message.success('修改成功');
      } else {
        await createCategory(payload);
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

  const handleDelete = (item: ProductCategory) => {
    Modal.confirm({
      rootClassName: 'xunye-confirm-modal',
      title: '确认删除',
      content: `确定要删除分类「${item.name}」吗？`,
      okText: '删除',
      okButtonProps: { danger: true },
      cancelText: '取消',
      onOk: async () => {
        try {
          await deleteCategory(item.id);
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
          <h1 className="text-2xl font-serif font-bold text-text-main tracking-wider mb-1">商品分类</h1>
          <p className="text-[10px] text-brand-gold uppercase tracking-widest font-medium">Product Categories</p>
        </div>
        <button
          onClick={openAddModal}
          className="flex items-center space-x-2 bg-brand-gold text-page-bg px-4 py-2 rounded-lg font-semibold text-sm hover:bg-brand-gold/90 transition-colors shrink-0 tracking-widest uppercase"
        >
          <Plus size={16} />
          <span>新增分类</span>
        </button>
      </div>

      {/* 数据表格 */}
      <div className="bg-card-bg border border-border-dark rounded-xl overflow-hidden shadow-xl">
        <div className="p-4 border-b border-border-dark flex justify-between items-center">
          <h3 className="text-sm font-semibold text-text-main">分类列表</h3>
          <button
            onClick={fetchData}
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
                <th className="px-6 py-3 font-medium">分类名称</th>
                <th className="px-6 py-3 font-medium text-center">排序</th>
                <th className="px-6 py-3 font-medium text-center">状态</th>
                <th className="px-6 py-3 font-medium text-center">操作</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-border-dark/50 text-xs">
              {loading ? (
                <tr>
                  <td colSpan={4} className="px-6 py-16 text-center text-text-weak">
                    <div className="flex items-center justify-center space-x-2">
                      <div className="w-4 h-4 border-2 border-brand-gold border-t-transparent rounded-full animate-spin" />
                      <span>加载中...</span>
                    </div>
                  </td>
                </tr>
              ) : data.length === 0 ? (
                <tr>
                  <td colSpan={4} className="px-6 py-16 text-center text-text-weak font-serif italic">
                    暂无分类数据
                  </td>
                </tr>
              ) : (
                data.map((item) => (
                  <tr key={item.id} className="hover:bg-border-dark/20 transition-colors">
                    <td className="px-6 py-3 font-medium text-text-main tracking-wide">{item.name}</td>
                    <td className="px-6 py-3 text-center text-text-sub">{item.sort}</td>
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
                ))
              )}
            </tbody>
          </table>
        </div>
      </div>

      {/* 新增 / 编辑弹窗 */}
      <Modal
        title={
          <span className="text-brand-gold font-serif tracking-wider">
            {editingItem ? '编辑分类' : '新增分类'}
          </span>
        }
        open={modalOpen}
        onCancel={() => setModalOpen(false)}
        onOk={handleSubmit}
        confirmLoading={submitting}
        okText={editingItem ? '保存' : '新增'}
        cancelText="取消"
        width={480}
        destroyOnHidden
        styles={{
          content: { background: '#1A1A1F', border: '1px solid #2A2A31' },
          header: { background: '#1A1A1F', borderBottom: '1px solid #2A2A31', paddingBottom: 16 },
          body: { background: '#1A1A1F', paddingTop: 20 },
          footer: { background: '#1A1A1F', borderTop: '1px solid #2A2A31' },
        }}
      >
        <Form form={form} layout="vertical" initialValues={{ sort: 0, status: 1 }}>
          <Form.Item
            name="name"
            label={<span className="text-text-sub text-xs uppercase tracking-wider">分类名称</span>}
            rules={[{ required: true, message: '请输入分类名称' }]}
            className="mb-3"
          >
            <Input placeholder="如：威士忌" />
          </Form.Item>
          <Form.Item
            name="sort"
            label={<span className="text-text-sub text-xs uppercase tracking-wider">排序</span>}
            tooltip="数值越小越靠前"
            className="mb-3"
          >
            <InputNumber min={0} className="!w-full" placeholder="0" />
          </Form.Item>
          <Form.Item
            name="status"
            label={<span className="text-text-sub text-xs uppercase tracking-wider">状态</span>}
            className="mb-0"
          >
            <Select
              options={[
                { label: '启用', value: 1 },
                { label: '禁用', value: 0 },
              ]}
              {...darkSelectProps}
            />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
}
