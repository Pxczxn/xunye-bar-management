'use client';

import { useEffect, useState } from 'react';
import { Input, message, Modal, Tag } from 'antd';
import { getBrandList, createBrand, deleteBrand } from '@/api/brand';
import type { ProductBrand } from '@/types/api';
import {
  Package,
  Store,
  Receipt,
  Smartphone,
  Plus,
  Trash2,
} from 'lucide-react';

const CATEGORIES = [
  { key: 'product', label: '商品配置', icon: Package },
  { key: 'shop', label: '店铺配置', icon: Store },
  { key: 'order', label: '订单配置', icon: Receipt },
  { key: 'miniapp', label: '小程序配置', icon: Smartphone },
];

function ClickableCard({ onClick, children }: { onClick: () => void; children: React.ReactNode }) {
  return (
    <div
      onClick={onClick}
      className="group bg-card-bg border border-border-dark rounded-xl p-5 cursor-pointer transition-all duration-200 hover:border-brand-gold hover:bg-[rgba(214,168,90,0.06)]"
    >
      {children}
    </div>
  );
}

function PlaceholderCard({ title, desc }: { title: string; desc: string }) {
  return (
    <div className="bg-card-bg border border-border-dark rounded-xl p-5 transition-colors duration-200 hover:border-border-dark/60">
      <div className="flex items-center justify-between mb-2">
        <h4 className="text-sm font-semibold text-text-main">{title}</h4>
        <span className="text-[10px] text-text-weak border border-border-dark rounded px-1.5 py-0.5">后续开放</span>
      </div>
      <p className="text-xs text-text-weak leading-relaxed">{desc}</p>
    </div>
  );
}

export default function SettingsPage() {
  const [activeCategory, setActiveCategory] = useState('product');
  const [brands, setBrands] = useState<ProductBrand[]>([]);
  const [loading, setLoading] = useState(false);
  const [brandModalOpen, setBrandModalOpen] = useState(false);
  const [newBrandName, setNewBrandName] = useState('');
  const [adding, setAdding] = useState(false);

  const fetchBrands = async () => {
    setLoading(true);
    try {
      const res = await getBrandList();
      setBrands(Array.isArray(res) ? res : []);
    } catch {
      // silent
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchBrands();
  }, []);

  const handleAddBrand = async () => {
    const name = newBrandName.trim();
    if (!name) {
      message.warning('请输入品牌名称');
      return;
    }
    setAdding(true);
    try {
      await createBrand({ name, sort: 0 });
      message.success('新增成功');
      setNewBrandName('');
      fetchBrands();
    } catch (err: any) {
      message.error(err.message || '新增失败');
    } finally {
      setAdding(false);
    }
  };

  const handleDeleteBrand = (brand: ProductBrand) => {
    Modal.confirm({
      title: '确认删除',
      content: `确定要删除品牌「${brand.name}」吗？`,
      okText: '删除',
      okButtonProps: { danger: true },
      cancelText: '取消',
      onOk: async () => {
        try {
          await deleteBrand(brand.id);
          message.success('删除成功');
          fetchBrands();
        } catch (err: any) {
          message.error(err.message || '删除失败');
        }
      },
    });
  };

  const renderProductConfig = () => (
    <div className="grid grid-cols-1 lg:grid-cols-2 gap-4">
      <ClickableCard onClick={() => setBrandModalOpen(true)}>
        <div className="flex items-center justify-between mb-2">
          <h4 className="text-sm font-semibold text-text-main">品牌历史</h4>
          <span className="text-xs text-text-weak group-hover:text-text-sub transition-colors">{brands.length} 个品牌</span>
        </div>
        <p className="text-xs text-text-weak leading-relaxed mb-4">
          用于商品录入时的品牌建议，新增商品时可快速选择。
        </p>
        {loading ? (
          <div className="flex items-center gap-2 text-text-weak text-xs mb-4">
            <div className="w-3 h-3 border-2 border-brand-gold border-t-transparent rounded-full animate-spin" />
            <span>加载中...</span>
          </div>
        ) : brands.length === 0 ? (
          <p className="text-xs text-text-weak italic mb-4">暂无品牌记录</p>
        ) : (
          <div className="flex flex-wrap gap-1.5 mb-4">
            {brands.slice(0, 6).map((b) => (
              <Tag
                key={b.id}
                className="!bg-sidebar-bg !border-border-dark !text-text-sub !text-xs !rounded !m-0"
              >
                {b.name}
              </Tag>
            ))}
            {brands.length > 6 && (
              <Tag className="!bg-sidebar-bg !border-border-dark !text-text-weak !text-xs !rounded !m-0">
                +{brands.length - 6}
              </Tag>
            )}
          </div>
        )}
      </ClickableCard>
    </div>
  );

  const renderShopConfig = () => (
    <div className="grid grid-cols-1 lg:grid-cols-2 gap-4">
      <PlaceholderCard title="店铺信息" desc="后续可配置店铺名称、地址、联系电话等基础信息。" />
      <PlaceholderCard title="营业时间" desc="后续可配置营业开始时间、打烊时间和特殊营业日。" />
      <PlaceholderCard title="联系方式" desc="后续可配置电话、微信、地址等对外展示信息。" />
    </div>
  );

  const renderOrderConfig = () => (
    <div className="grid grid-cols-1 lg:grid-cols-2 gap-4">
      <PlaceholderCard title="支付方式" desc="后续可配置微信、支付宝、现金等支付方式。" />
      <PlaceholderCard title="小票打印" desc="后续可配置吧台小票和后厨打印规则。" />
      <PlaceholderCard title="订单规则" desc="后续可配置订单取消、退款、备注等规则。" />
    </div>
  );

  const renderMiniappConfig = () => (
    <div className="grid grid-cols-1 lg:grid-cols-2 gap-4">
      <PlaceholderCard title="首页文案" desc="后续可配置顾客端小程序首页展示文案。" />
      <PlaceholderCard title="菜单展示" desc="后续可配置小程序商品菜单展示规则。" />
      <PlaceholderCard title="扫码点单" desc="后续可配置是否开放顾客扫码点单。" />
    </div>
  );

  const renderContent = () => {
    switch (activeCategory) {
      case 'product':
        return renderProductConfig();
      case 'shop':
        return renderShopConfig();
      case 'order':
        return renderOrderConfig();
      case 'miniapp':
        return renderMiniappConfig();
      default:
        return null;
    }
  };

  return (
    <div className="h-full flex flex-col">
      {/* 页面标题 */}
      <div className="mb-6">
        <h1 className="text-2xl font-serif font-bold text-text-main tracking-wider mb-1">系统设置</h1>
        <p className="text-[10px] text-brand-gold uppercase tracking-widest font-medium">System Settings</p>
      </div>

      {/* 主体布局 */}
      <div className="flex-1 flex gap-6 min-h-0">
        {/* 左侧分类导航 */}
        <div className="w-[220px] shrink-0">
          <div className="bg-card-bg border border-border-dark rounded-xl p-2 sticky top-6">
            {CATEGORIES.map((cat) => {
              const Icon = cat.icon;
              const isActive = activeCategory === cat.key;
              return (
                <button
                  key={cat.key}
                  onClick={() => setActiveCategory(cat.key)}
                  className={`w-full flex items-center space-x-3 px-3 py-2.5 rounded-lg text-sm transition-colors ${
                    isActive
                      ? 'bg-brand-gold/10 text-brand-gold'
                      : 'text-text-sub hover:text-text-main hover:bg-border-dark/30'
                  }`}
                >
                  <Icon size={16} strokeWidth={isActive ? 2 : 1.5} />
                  <span className="tracking-wide">{cat.label}</span>
                </button>
              );
            })}
          </div>
        </div>

        {/* 右侧内容区 */}
        <div className="flex-1 min-w-0 overflow-y-auto">
          {renderContent()}
        </div>
      </div>

      {/* 品牌管理 Modal */}
      <Modal
        title={
          <span className="text-brand-gold font-serif tracking-wider">品牌历史管理</span>
        }
        open={brandModalOpen}
        onCancel={() => setBrandModalOpen(false)}
        footer={null}
        width={520}
        destroyOnHidden
        styles={{
          root: { background: 'transparent' },
          wrapper: { background: 'rgba(0, 0, 0, 0.65)' },
          container: { background: '#1A1A1F' },
          header: { background: '#1A1A1F', borderBottom: '1px solid #2A2A31', paddingBottom: 16 },
          body: { background: '#1A1A1F', paddingTop: 16 },
        }}
      >
        <div className="flex gap-2 mb-4">
          <Input
            placeholder="输入新品牌名称"
            value={newBrandName}
            onChange={(e) => setNewBrandName(e.target.value)}
            onPressEnter={handleAddBrand}
            className="!bg-sidebar-bg !border-border-dark !text-text-main !placeholder-text-weak"
          />
          <button
            onClick={handleAddBrand}
            disabled={adding}
            className="flex items-center space-x-1.5 bg-brand-gold text-page-bg px-4 py-2 rounded-lg font-semibold text-sm hover:bg-brand-gold/90 transition-colors disabled:opacity-50 shrink-0"
          >
            <Plus size={14} />
            <span>新增</span>
          </button>
        </div>
        <div className="max-h-[400px] overflow-y-auto border border-border-dark rounded-lg">
          {loading ? (
            <div className="px-4 py-12 text-center text-text-weak">
              <div className="flex items-center justify-center space-x-2">
                <div className="w-4 h-4 border-2 border-brand-gold border-t-transparent rounded-full animate-spin" />
                <span>加载中...</span>
              </div>
            </div>
          ) : brands.length === 0 ? (
            <div className="px-4 py-12 text-center text-text-weak font-serif italic">
              暂无品牌记录
            </div>
          ) : (
            <div className="divide-y divide-border-dark/50">
              {brands.map((b) => (
                <div
                  key={b.id}
                  className="flex items-center justify-between px-4 py-2.5 hover:bg-border-dark/20 transition-colors"
                >
                  <span className="text-text-main text-sm">{b.name}</span>
                  <button
                    onClick={() => handleDeleteBrand(b)}
                    className="text-text-weak hover:text-danger transition-colors"
                    title="删除"
                    type="button"
                  >
                    <Trash2 size={14} />
                  </button>
                </div>
              ))}
            </div>
          )}
        </div>
      </Modal>
    </div>
  );
}
