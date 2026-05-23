'use client';

import { useEffect, useState, useCallback } from 'react';
import { Modal, Form, Input, Select, InputNumber, AutoComplete, message } from 'antd';
import {
  getCategoryList,
  getProductPage,
  createProduct,
  updateProduct,
  updateProductStatus,
  deleteProduct,
} from '@/api/product';
import { getBrandList } from '@/api/brand';
import type {
  ProductCategory,
  ProductItem,
  ProductFormData,
  ProductBrand,
} from '@/types/api';
import {
  Search,
  Plus,
  Edit,
  Trash2,
  ArrowDownToLine,
  ArrowUpFromLine,
  RotateCcw,
} from 'lucide-react';

const { TextArea } = Input;

const SPEC_UNIT_OPTIONS = ['ml', 'L', '杯', '瓶', '份', '个', '箱', '听', '支', '套'];
const UNIT_OPTIONS = ['瓶', '杯', '份', '个', '箱', '听', '支', '套'];

const CATEGORY_DEFAULTS: Record<string, { unit: string; specValue: number; specUnit: string }> = {
  '啤酒': { unit: '瓶', specValue: 330, specUnit: 'ml' },
  '鸡尾酒': { unit: '杯', specValue: 1, specUnit: '杯' },
  '威士忌': { unit: '瓶', specValue: 700, specUnit: 'ml' },
  '利口酒': { unit: '瓶', specValue: 700, specUnit: 'ml' },
  '小食': { unit: '份', specValue: 1, specUnit: '份' },
  '辅料': { unit: '个', specValue: 1, specUnit: '个' },
};

const SPEC_PARSE_RE = /^(\d+(?:\.\d+)?)\s*(.*)$/;

function parseSpec(raw: string): { value: string; unit: string } {
  if (!raw) return { value: '', unit: '' };
  const m = raw.match(SPEC_PARSE_RE);
  if (m && m[2]) {
    return { value: m[1], unit: m[2] };
  }
  return { value: raw, unit: '' };
}

function buildSpec(value: string | number, unit: string): string {
  const v = String(value ?? '').trim();
  const u = (unit ?? '').trim();
  if (!v && !u) return '';
  if (!u) return v;
  return `${v}${u}`;
}

export default function ProductsPage() {
  const [data, setData] = useState<ProductItem[]>([]);
  const [total, setTotal] = useState(0);
  const [loading, setLoading] = useState(false);
  const [pageNum, setPageNum] = useState(1);
  const [pageSize, setPageSize] = useState(10);
  const [keyword, setKeyword] = useState('');
  const [filterCategoryId, setFilterCategoryId] = useState<number | undefined>();
  const [filterStatus, setFilterStatus] = useState<string | undefined>();
  const [categories, setCategories] = useState<ProductCategory[]>([]);
  const [brands, setBrands] = useState<ProductBrand[]>([]);

  const [modalOpen, setModalOpen] = useState(false);
  const [editingItem, setEditingItem] = useState<ProductItem | null>(null);
  const [submitting, setSubmitting] = useState(false);
  const [form] = Form.useForm();

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

  const fetchCategories = async () => {
    try {
      const res = await getCategoryList();
      setCategories(Array.isArray(res) ? res : []);
    } catch {
      // silent
    }
  };

  const fetchBrands = async () => {
    try {
      const res = await getBrandList();
      setBrands(Array.isArray(res) ? res : []);
    } catch {
      // silent
    }
  };

  const fetchData = useCallback(async (page = pageNum, size = pageSize) => {
    setLoading(true);
    try {
      const params: any = { pageNum: page, pageSize: size };
      if (keyword) params.keyword = keyword;
      if (filterCategoryId !== undefined) params.categoryId = filterCategoryId;
      if (filterStatus) params.status = filterStatus;
      const res = await getProductPage(params);
      setData(res?.records ?? []);
      setTotal(res?.total ?? 0);
    } catch (err: any) {
      message.error(err.message || '获取商品列表失败');
      setData([]);
      setTotal(0);
    } finally {
      setLoading(false);
    }
  }, [pageNum, pageSize, keyword, filterCategoryId, filterStatus]);

  useEffect(() => {
    fetchCategories();
    fetchBrands();
  }, []);

  useEffect(() => {
    fetchData();
  }, [fetchData]);

  const handleSearch = () => {
    setPageNum(1);
    fetchData(1, pageSize);
  };

  const handleReset = () => {
    setKeyword('');
    setFilterCategoryId(undefined);
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
    form.setFieldsValue({
      status: 'ON_SALE',
      stock: 0,
      safeStock: 0,
      costPrice: 0,
      price: 0,
    });
    setModalOpen(true);
  };

  const openEditModal = (item: ProductItem) => {
    setEditingItem(item);
    const { value, unit } = parseSpec(item.spec);
    form.setFieldsValue({
      name: item.name,
      categoryId: item.categoryId,
      brand: item.brand,
      specValue: value,
      specUnit: unit,
      price: item.price,
      costPrice: item.costPrice,
      stock: item.stock,
      safeStock: item.safeStock,
      unit: item.unit,
      description: item.description,
      status: item.status,
    });
    setModalOpen(true);
  };

  const handleSubmit = async () => {
    try {
      const values = await form.validateFields();
      setSubmitting(true);
      const spec = buildSpec(values.specValue, values.specUnit);
      const payload: ProductFormData = {
        categoryId: values.categoryId,
        name: values.name,
        brand: values.brand || '',
        spec,
        price: values.price,
        costPrice: values.costPrice ?? 0,
        stock: values.stock ?? 0,
        safeStock: values.safeStock ?? 0,
        unit: values.unit || '',
        imageUrl: values.imageUrl || '',
        description: values.description || '',
        status: values.status,
      };
      if (editingItem) {
        await updateProduct(editingItem.id, payload);
        message.success('修改成功');
      } else {
        await createProduct(payload);
        message.success('新增成功');
      }
      setModalOpen(false);
      fetchData();
      fetchBrands();
    } catch (err: any) {
      if (err?.errorFields) return;
      message.error(err.message || '操作失败');
    } finally {
      setSubmitting(false);
    }
  };

  const handleToggleStatus = (item: ProductItem) => {
    const nextStatus = item.status === 'ON_SALE' ? 'OFF_SALE' : 'ON_SALE';
    const label = nextStatus === 'ON_SALE' ? '上架' : '下架';
    Modal.confirm({
      rootClassName: 'xunye-confirm-modal',
      title: `确认${label}`,
      content: `确定要${label}「${item.name}」吗？`,
      okText: '确认',
      cancelText: '取消',
      onOk: async () => {
        try {
          await updateProductStatus(item.id, nextStatus);
          message.success(`${label}成功`);
          fetchData();
        } catch (err: any) {
          message.error(err.message || `${label}失败`);
        }
      },
    });
  };

  const handleDelete = (item: ProductItem) => {
    Modal.confirm({
      rootClassName: 'xunye-confirm-modal',
      title: '确认删除',
      content: `确定要删除「${item.name}」吗？此操作不可恢复。`,
      okText: '删除',
      okButtonProps: { danger: true },
      cancelText: '取消',
      onOk: async () => {
        try {
          await deleteProduct(item.id);
          message.success('删除成功');
          fetchData();
        } catch (err: any) {
          message.error(err.message || '删除失败');
        }
      },
    });
  };

  const handleCategoryChange = (categoryId: number) => {
    const category = categories.find((c) => c.id === categoryId);
    if (!category) return;
    const defaults = CATEGORY_DEFAULTS[category.name];
    if (!defaults) return;
    const currentUnit = form.getFieldValue('unit');
    const currentSpecValue = form.getFieldValue('specValue');
    if (!currentUnit) {
      form.setFieldsValue({ unit: defaults.unit });
    }
    if (!currentSpecValue) {
      form.setFieldsValue({ specValue: defaults.specValue, specUnit: defaults.specUnit });
    }
  };

  const brandOptions = brands.map((b) => ({ value: b.name }));

  const totalPages = Math.ceil(total / pageSize);

  return (
    <div className="space-y-6">
      {/* 标题栏 */}
      <div className="flex flex-col sm:flex-row justify-between items-start sm:items-end gap-6 mb-6">
        <div>
          <h1 className="text-2xl font-serif font-bold text-text-main tracking-wider mb-1">酒水管理</h1>
          <p className="text-[10px] text-brand-gold uppercase tracking-widest font-medium">Beverage Menu</p>
        </div>
        <div className="shrink-0">
          <button
            onClick={openAddModal}
            className="flex items-center space-x-2 bg-brand-gold text-page-bg px-4 py-2 rounded-lg font-semibold text-sm hover:bg-brand-gold/90 transition-colors tracking-widest uppercase"
          >
            <Plus size={16} />
            <span>新增酒水</span>
          </button>
        </div>
      </div>

      {/* 搜索筛选区 */}
      <div className="bg-card-bg border border-border-dark rounded-xl p-4">
        <div className="flex flex-col sm:flex-row items-end gap-3 flex-wrap">
          <div className="w-full sm:w-56">
            <label className="block text-[10px] text-text-weak uppercase tracking-wider mb-1">商品名称</label>
            <Input
              placeholder="输入关键词搜索"
              value={keyword}
              onChange={(e) => setKeyword(e.target.value)}
              allowClear
              className="!bg-sidebar-bg !border-border-dark !text-text-main !placeholder-text-weak"
            />
          </div>
          <div className="w-full sm:w-40">
            <label className="block text-[10px] text-text-weak uppercase tracking-wider mb-1">分类</label>
            <Select
              placeholder="全部分类"
              value={filterCategoryId}
              onChange={(v) => setFilterCategoryId(v)}
              allowClear
              {...darkSelectProps}
              options={categories.map((c) => ({ label: c.name, value: c.id }))}
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
                { label: '上架', value: 'ON_SALE' },
                { label: '下架', value: 'OFF_SALE' },
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
          <h3 className="text-sm font-semibold text-text-main">商品列表</h3>
          <span className="text-xs text-text-weak">共 {total} 条</span>
        </div>
        <div className="overflow-x-auto hide-scrollbar">
          <table className="w-full text-left text-sm whitespace-nowrap">
            <thead>
              <tr className="bg-sidebar-bg text-text-weak uppercase text-[10px] tracking-wider">
                <th className="px-6 py-3 font-medium">商品名称</th>
                <th className="px-6 py-3 font-medium">分类</th>
                <th className="px-6 py-3 font-medium">品牌</th>
                <th className="px-6 py-3 font-medium">规格</th>
                <th className="px-6 py-3 font-medium text-right">售价</th>
                <th className="px-6 py-3 font-medium text-right">成本价</th>
                <th className="px-6 py-3 font-medium text-right">库存 / 安全</th>
                <th className="px-6 py-3 font-medium text-center">单位</th>
                <th className="px-6 py-3 font-medium text-center">状态</th>
                <th className="px-6 py-3 font-medium">创建时间</th>
                <th className="px-6 py-3 font-medium text-center">操作</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-border-dark/50 text-xs">
              {loading ? (
                <tr>
                  <td colSpan={11} className="px-6 py-16 text-center text-text-weak">
                    <div className="flex items-center justify-center space-x-2">
                      <div className="w-4 h-4 border-2 border-brand-gold border-t-transparent rounded-full animate-spin" />
                      <span>加载中...</span>
                    </div>
                  </td>
                </tr>
              ) : data.length === 0 ? (
                <tr>
                  <td colSpan={11} className="px-6 py-16 text-center text-text-weak font-serif italic">
                    暂无商品数据
                  </td>
                </tr>
              ) : (
                data.map((item) => {
                  const isLowStock = item.stock < item.safeStock;
                  return (
                    <tr key={item.id} className="hover:bg-border-dark/20 transition-colors">
                      <td className="px-6 py-3 font-medium text-text-main tracking-wide">{item.name}</td>
                      <td className="px-6 py-3 text-text-sub tracking-wider">{item.categoryName}</td>
                      <td className="px-6 py-3 text-text-sub tracking-wider">{item.brand}</td>
                      <td className="px-6 py-3 text-text-sub tracking-wider">{item.spec}</td>
                      <td className="px-6 py-3 text-right text-brand-gold font-sans tracking-wide">
                        ¥{Number(item.price).toFixed(2)}
                      </td>
                      <td className="px-6 py-3 text-right text-text-sub font-sans tracking-wide">
                        ¥{Number(item.costPrice).toFixed(2)}
                      </td>
                      <td className="px-6 py-3 text-right">
                        <span
                          className={`font-sans tracking-wide ${
                            isLowStock ? 'text-danger font-bold relative' : 'text-text-main'
                          }`}
                        >
                          {item.stock}
                          {isLowStock && (
                            <span className="absolute -right-2 -top-1 flex h-2 w-2">
                              <span className="animate-ping absolute inline-flex h-full w-full rounded-full bg-danger opacity-75" />
                              <span className="relative inline-flex rounded-full h-2 w-2 bg-danger" />
                            </span>
                          )}
                        </span>
                        <span className="text-text-weak mx-1 font-serif italic">/</span>
                        <span className="font-sans text-text-sub tracking-wide">{item.safeStock}</span>
                      </td>
                      <td className="px-6 py-3 text-center text-text-sub">{item.unit}</td>
                      <td className="px-6 py-3 text-center">
                        {item.status === 'ON_SALE' ? (
                          <span className="inline-flex items-center px-2 py-0.5 rounded text-[10px] bg-success/10 text-success">
                            上架
                          </span>
                        ) : (
                          <span className="inline-flex items-center px-2 py-0.5 rounded text-[10px] bg-border-dark text-text-sub">
                            下架
                          </span>
                        )}
                      </td>
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
                          {item.status === 'ON_SALE' ? (
                            <button
                              onClick={() => handleToggleStatus(item)}
                              className="hover:text-danger transition-colors"
                              title="下架"
                              type="button"
                            >
                              <ArrowDownToLine size={15} />
                            </button>
                          ) : (
                            <button
                              onClick={() => handleToggleStatus(item)}
                              className="hover:text-success transition-colors"
                              title="上架"
                              type="button"
                            >
                              <ArrowUpFromLine size={15} />
                            </button>
                          )}
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
            {editingItem ? '编辑酒水' : '新增酒水'}
          </span>
        }
        open={modalOpen}
        onCancel={() => setModalOpen(false)}
        onOk={handleSubmit}
        confirmLoading={submitting}
        okText={editingItem ? '保存' : '新增'}
        cancelText="取消"
        width={860}
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
        <Form form={form} layout="vertical" initialValues={{ status: 'ON_SALE' }}>
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-x-6 gap-y-0">
            <Form.Item
              name="name"
              label={<span className="text-text-sub text-xs uppercase tracking-wider">商品名称</span>}
              rules={[{ required: true, message: '请输入商品名称' }]}
              className="mb-3"
            >
              <Input placeholder="如：百威啤酒" />
            </Form.Item>
            <Form.Item
              name="categoryId"
              label={<span className="text-text-sub text-xs uppercase tracking-wider">分类</span>}
              rules={[{ required: true, message: '请选择分类' }]}
              className="mb-3"
            >
              <Select
                placeholder="选择分类"
                {...darkSelectProps}
                onChange={(v) => {
                  form.setFieldsValue({ categoryId: v });
                  handleCategoryChange(v);
                }}
                options={categories.map((c) => ({ label: c.name, value: c.id }))}
              />
            </Form.Item>
            <Form.Item
              name="brand"
              label={<span className="text-text-sub text-xs uppercase tracking-wider">品牌</span>}
              className="mb-3"
            >
              <AutoComplete
                placeholder="输入或选择品牌"
                options={brandOptions}
                className="xunye-select xunye-autocomplete"
                popupClassName="xunye-autocomplete-dropdown"
              />
            </Form.Item>
            <div className="mb-3">
              <label className="block text-text-sub text-xs uppercase tracking-wider mb-1">规格</label>
              <div className="flex gap-2">
                <Form.Item name="specValue" noStyle>
                  <InputNumber
                    placeholder="数值"
                    className="!w-[55%]"
                    min={0}
                    step={1}
                  />
                </Form.Item>
                <Form.Item name="specUnit" noStyle>
                  <Select
                    placeholder="单位"
                    className="xunye-select !w-[45%]"
                    popupClassName="xunye-select-dropdown"
                    options={SPEC_UNIT_OPTIONS.map((u) => ({ label: u, value: u }))}
                  />
                </Form.Item>
              </div>
            </div>
            <Form.Item
              name="price"
              label={<span className="text-text-sub text-xs uppercase tracking-wider">售价</span>}
              rules={[{ required: true, message: '请输入售价' }]}
              className="mb-3"
            >
              <InputNumber min={0} step={0.01} precision={2} className="!w-full" placeholder="0.00" />
            </Form.Item>
            <Form.Item
              name="costPrice"
              label={<span className="text-text-sub text-xs uppercase tracking-wider">成本价</span>}
              className="mb-3"
            >
              <InputNumber min={0} step={0.01} precision={2} className="!w-full" placeholder="0.00" />
            </Form.Item>
            <Form.Item
              name="stock"
              label={<span className="text-text-sub text-xs uppercase tracking-wider">当前库存</span>}
              className="mb-3"
            >
              <InputNumber min={0} className="!w-full" placeholder="0" />
            </Form.Item>
            <Form.Item
              name="safeStock"
              label={<span className="text-text-sub text-xs uppercase tracking-wider">安全库存</span>}
              className="mb-3"
            >
              <InputNumber min={0} className="!w-full" placeholder="0" />
            </Form.Item>
            <Form.Item
              name="unit"
              label={<span className="text-text-sub text-xs uppercase tracking-wider">单位</span>}
              className="mb-3"
            >
              <AutoComplete
                placeholder="输入或选择单位"
                options={UNIT_OPTIONS.map((u) => ({ value: u }))}
                className="xunye-select xunye-autocomplete"
                popupClassName="xunye-autocomplete-dropdown"
              />
            </Form.Item>
            <Form.Item
              name="status"
              label={<span className="text-text-sub text-xs uppercase tracking-wider">状态</span>}
              className="mb-3"
            >
              <Select
                {...darkSelectProps}
                options={[
                  { label: '上架', value: 'ON_SALE' },
                  { label: '下架', value: 'OFF_SALE' },
                ]}
              />
            </Form.Item>
          </div>
          <Form.Item
            name="description"
            label={<span className="text-text-sub text-xs uppercase tracking-wider">描述</span>}
            className="mb-0"
          >
            <TextArea rows={3} placeholder="商品描述" />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
}
