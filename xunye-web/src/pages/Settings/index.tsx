'use client';

import { useCallback, useEffect, useState } from 'react';
import {
  Input, message, Modal, Tag, Switch, Select, Button, Spin, InputNumber, Upload,
} from 'antd';
import { getBrandList, createBrand, deleteBrand } from '@/api/brand';
import {
  getShopConfig,
  getOrderConfig,
  getMiniappConfig,
  updateConfigs,
  uploadMiniappImage,
  deleteMiniappImage,
} from '@/api/settings';
import type { ProductBrand } from '@/types/api';
import type { ShopConfig, OrderConfig, MiniappConfig } from '@/api/settings';
import {
  Package,
  Store,
  Receipt,
  Smartphone,
  Plus,
  Trash2,
  Save,
  Upload as UploadIcon,
  X,
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

function ConfigSection({
  title,
  desc,
  loading,
  children,
  onSave,
  saving,
}: {
  title: string;
  desc?: string;
  loading?: boolean;
  children: React.ReactNode;
  onSave?: () => void;
  saving?: boolean;
}) {
  return (
    <div className="bg-card-bg border border-border-dark rounded-xl p-6">
      <div className="flex items-center justify-between mb-5">
        <div>
          <h3 className="text-base font-semibold text-text-main tracking-wide">{title}</h3>
          {desc && <p className="text-xs text-text-weak mt-1">{desc}</p>}
        </div>
        {onSave && (
          <Button
            onClick={onSave}
            loading={saving}
            className="!bg-brand-gold !text-page-bg !border-none !font-semibold hover:!opacity-90 flex items-center"
            icon={<Save size={14} />}
          >
            保存
          </Button>
        )}
      </div>
      {loading ? (
        <div className="flex items-center justify-center py-12">
          <Spin />
        </div>
      ) : (
        <div className="space-y-5">{children}</div>
      )}
    </div>
  );
}

function ConfigField({
  label,
  description,
  children,
}: {
  label: string;
  description?: string;
  children: React.ReactNode;
}) {
  return (
    <div className="flex items-start justify-between gap-6">
      <div className="min-w-[140px] shrink-0 pt-1.5">
        <span className="text-sm font-medium text-text-main">{label}</span>
        {description && <p className="text-[11px] text-text-weak mt-0.5">{description}</p>}
      </div>
      <div className="flex-1 max-w-[420px]">{children}</div>
    </div>
  );
}

const PAYMENT_OPTIONS = [
  { value: 'WECHAT', label: '微信支付' },
  { value: 'ALIPAY', label: '支付宝' },
  { value: 'CASH', label: '现金' },
];

const PRINTER_OPTIONS = [
  { value: 'USB', label: 'USB 打印机' },
  { value: 'NETWORK', label: '网络打印机' },
  { value: 'BLUETOOTH', label: '蓝牙打印机' },
];

const MENU_DISPLAY_OPTIONS = [
  { value: 'all', label: '展示全部商品' },
  { value: 'on_sale', label: '仅展示在售商品' },
];

export default function SettingsPage() {
  const [activeCategory, setActiveCategory] = useState('shop');

  // --- Product Config (Brands) ---
  const [brands, setBrands] = useState<ProductBrand[]>([]);
  const [loadingBrands, setLoadingBrands] = useState(false);
  const [brandModalOpen, setBrandModalOpen] = useState(false);
  const [newBrandName, setNewBrandName] = useState('');
  const [adding, setAdding] = useState(false);

  // --- Shop Config ---
  const [shopConfig, setShopConfig] = useState<ShopConfig | null>(null);
  const [shopLoading, setShopLoading] = useState(false);
  const [shopSaving, setShopSaving] = useState(false);

  // --- Order Config ---
  const [orderConfig, setOrderConfig] = useState<OrderConfig | null>(null);
  const [orderLoading, setOrderLoading] = useState(false);
  const [orderSaving, setOrderSaving] = useState(false);

  // --- Miniapp Config ---
  const [miniappConfig, setMiniappConfig] = useState<MiniappConfig | null>(null);
  const [miniappLoading, setMiniappLoading] = useState(false);
  const [miniappSaving, setMiniappSaving] = useState(false);
  const [uploadingImage, setUploadingImage] = useState(false);

  // --- Brand APIs ---
  const fetchBrands = useCallback(async () => {
    setLoadingBrands(true);
    try {
      const res = await getBrandList();
      setBrands(Array.isArray(res) ? res : []);
    } catch {
      // silent
    } finally {
      setLoadingBrands(false);
    }
  }, []);

  useEffect(() => {
    fetchBrands();
  }, [fetchBrands]);

  // --- Shop Config APIs ---
  const fetchShopConfig = useCallback(async () => {
    setShopLoading(true);
    try {
      const config = await getShopConfig();
      setShopConfig(config);
    } catch {
      // silent
    } finally {
      setShopLoading(false);
    }
  }, []);

  useEffect(() => {
    if (activeCategory === 'shop') {
      fetchShopConfig();
    }
  }, [activeCategory, fetchShopConfig]);

  // --- Order Config APIs ---
  const fetchOrderConfig = useCallback(async () => {
    setOrderLoading(true);
    try {
      const config = await getOrderConfig();
      setOrderConfig({
        ...config,
        paymentMethods: config.paymentMethods ?? [],
        receiptBar: config.receiptBar ?? { enabled: false, printer: 'USB' },
        receiptKitchen: config.receiptKitchen ?? { enabled: false, printer: 'USB' },
      });
    } catch {
      // silent
    } finally {
      setOrderLoading(false);
    }
  }, []);

  useEffect(() => {
    if (activeCategory === 'order') {
      fetchOrderConfig();
    }
  }, [activeCategory, fetchOrderConfig]);

  // --- Miniapp Config APIs ---
  const fetchMiniappConfig = useCallback(async () => {
    setMiniappLoading(true);
    try {
      const config = await getMiniappConfig();
      setMiniappConfig({
        ...config,
        bannerImages: config.bannerImages ?? [],
      });
    } catch {
      // silent
    } finally {
      setMiniappLoading(false);
    }
  }, []);

  useEffect(() => {
    if (activeCategory === 'miniapp') {
      fetchMiniappConfig();
    }
  }, [activeCategory, fetchMiniappConfig]);

  // ========== Product Config ==========
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

  // ========== Shop Config ==========
  const handleSaveShop = async () => {
    if (!shopConfig) return;
    setShopSaving(true);
    try {
      await updateConfigs({
        'shop.name': shopConfig.name,
        'shop.slogan': shopConfig.slogan,
        'shop.address': shopConfig.address,
        'shop.phone': shopConfig.phone,
        'shop.contact_wechat': shopConfig.contactWechat,
        'shop.business_hours': shopConfig.businessHours,
        'shop.notice': shopConfig.notice,
      });
      message.success('店铺配置已保存');
    } catch (err: any) {
      message.error(err.message || '保存失败');
    } finally {
      setShopSaving(false);
    }
  };

  // ========== Order Config ==========
  const handleSaveOrder = async () => {
    if (!orderConfig) return;
    setOrderSaving(true);
    try {
      await updateConfigs({
        'order.payment_methods': JSON.stringify(orderConfig.paymentMethods),
        'order.receipt_bar': JSON.stringify(orderConfig.receiptBar),
        'order.receipt_kitchen': JSON.stringify(orderConfig.receiptKitchen),
        'order.cancel_timeout': orderConfig.cancelTimeout,
      });
      message.success('订单配置已保存');
    } catch (err: any) {
      message.error(err.message || '保存失败');
    } finally {
      setOrderSaving(false);
    }
  };

  // ========== Miniapp Config ==========
  const handleUploadBannerImage = async (file: File) => {
    if (!miniappConfig) return false;
    setUploadingImage(true);
    try {
      const imageUrl = await uploadMiniappImage(file);
      setMiniappConfig({
        ...miniappConfig,
        bannerImages: [...miniappConfig.bannerImages, imageUrl],
      });
      message.success('图片上传成功');
    } catch (err: any) {
      message.error(err.message || '图片上传失败');
    } finally {
      setUploadingImage(false);
    }
    return false; // 阻止 Upload 默认上传行为
  };

  const handleDeleteBannerImage = (imageUrl: string) => {
    if (!miniappConfig) return;
    Modal.confirm({
      title: '确认删除',
      content: '确定要删除该展示图片吗？',
      okText: '删除',
      okButtonProps: { danger: true },
      cancelText: '取消',
      onOk: async () => {
        try {
          // 先移除本地显示
          setMiniappConfig({
            ...miniappConfig,
            bannerImages: miniappConfig.bannerImages.filter((url) => url !== imageUrl),
          });
          // 后端删除文件
          await deleteMiniappImage(imageUrl);
          message.success('图片已删除');
        } catch (err: any) {
          message.error(err.message || '删除失败');
          // 回滚：重新拉取
          fetchMiniappConfig();
        }
      },
    });
  };

  const handleSaveMiniapp = async () => {
    if (!miniappConfig) return;
    setMiniappSaving(true);
    try {
      await updateConfigs({
        'miniapp.homepage_title': miniappConfig.homepageTitle,
        'miniapp.homepage_subtitle': miniappConfig.homepageSubtitle,
        'miniapp.menu_display': miniappConfig.menuDisplay,
        'miniapp.scan_to_order': String(miniappConfig.scanToOrder),
        'miniapp.banner_images': JSON.stringify(miniappConfig.bannerImages),
      });
      message.success('小程序配置已保存');
    } catch (err: any) {
      message.error(err.message || '保存失败');
    } finally {
      setMiniappSaving(false);
    }
  };

  // ========== Render: Product Config ==========
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
        {loadingBrands ? (
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

  // ========== Render: Shop Config ==========
  const renderShopConfig = () => {
    if (!shopConfig) return null;

    return (
      <ConfigSection
        title="店铺基础信息"
        desc="配置店铺名称、地址、联系方式等对外展示信息"
        loading={shopLoading}
        onSave={handleSaveShop}
        saving={shopSaving}
      >
        <ConfigField label="店铺名称" description="对外展示的店铺名称">
          <Input
            value={shopConfig.name}
            onChange={(e) => setShopConfig({ ...shopConfig, name: e.target.value })}
            className="!bg-sidebar-bg !border-border-dark !text-text-main !placeholder-text-weak"
          />
        </ConfigField>

        <ConfigField label="店铺标语" description="显示在店招上的短句">
          <Input
            value={shopConfig.slogan}
            onChange={(e) => setShopConfig({ ...shopConfig, slogan: e.target.value })}
            className="!bg-sidebar-bg !border-border-dark !text-text-main !placeholder-text-weak"
          />
        </ConfigField>

        <ConfigField label="营业时间" description="例：18:00 - 02:00">
          <Input
            value={shopConfig.businessHours}
            onChange={(e) => setShopConfig({ ...shopConfig, businessHours: e.target.value })}
            className="!bg-sidebar-bg !border-border-dark !text-text-main !placeholder-text-weak"
          />
        </ConfigField>

        <ConfigField label="店铺地址">
          <Input
            value={shopConfig.address}
            onChange={(e) => setShopConfig({ ...shopConfig, address: e.target.value })}
            className="!bg-sidebar-bg !border-border-dark !text-text-main !placeholder-text-weak"
          />
        </ConfigField>

        <ConfigField label="联系电话">
          <Input
            value={shopConfig.phone}
            onChange={(e) => setShopConfig({ ...shopConfig, phone: e.target.value })}
            className="!bg-sidebar-bg !border-border-dark !text-text-main !placeholder-text-weak"
          />
        </ConfigField>

        <ConfigField label="联系微信">
          <Input
            value={shopConfig.contactWechat}
            onChange={(e) => setShopConfig({ ...shopConfig, contactWechat: e.target.value })}
            className="!bg-sidebar-bg !border-border-dark !text-text-main !placeholder-text-weak"
          />
        </ConfigField>

        <ConfigField label="店铺公告" description="显示在顾客端首页">
          <Input.TextArea
            value={shopConfig.notice}
            onChange={(e) => setShopConfig({ ...shopConfig, notice: e.target.value })}
            rows={3}
            className="!bg-sidebar-bg !border-border-dark !text-text-main !placeholder-text-weak"
          />
        </ConfigField>
      </ConfigSection>
    );
  };

  // ========== Render: Order Config ==========
  const renderOrderConfig = () => {
    if (!orderConfig) return null;

    const togglePaymentMethod = (method: string) => {
      const current = [...orderConfig.paymentMethods];
      const idx = current.indexOf(method);
      if (idx >= 0) {
        current.splice(idx, 1);
      } else {
        current.push(method);
      }
      setOrderConfig({ ...orderConfig, paymentMethods: current });
    };

    return (
      <div className="space-y-4">
        <ConfigSection
          title="支付方式"
          desc="开启后顾客可在小程序中选择对应方式支付"
          loading={orderLoading}
        >
          <div className="flex flex-wrap gap-3">
            {PAYMENT_OPTIONS.map((opt) => {
              const enabled = orderConfig.paymentMethods.includes(opt.value);
              return (
                <div
                  key={opt.value}
                  onClick={() => togglePaymentMethod(opt.value)}
                  className={`flex items-center gap-2 px-4 py-2.5 rounded-lg border cursor-pointer transition-all text-sm ${
                    enabled
                      ? 'border-brand-gold bg-brand-gold/10 text-brand-gold'
                      : 'border-border-dark text-text-weak hover:text-text-sub hover:border-border-dark/60'
                  }`}
                >
                  <div
                    className={`w-4 h-4 rounded-full border-2 flex items-center justify-center transition-colors ${
                      enabled ? 'border-brand-gold' : 'border-text-weak'
                    }`}
                  >
                    {enabled && <div className="w-2 h-2 rounded-full bg-brand-gold" />}
                  </div>
                  <span>{opt.label}</span>
                </div>
              );
            })}
          </div>
        </ConfigSection>

        <ConfigSection title="小票打印" desc="配置吧台和后厨的打印规则">
          <ConfigField label="吧台打印">
            <div className="flex items-center gap-3">
              <Switch
                checked={orderConfig.receiptBar.enabled}
                onChange={(checked) =>
                  setOrderConfig({
                    ...orderConfig,
                    receiptBar: { ...orderConfig.receiptBar, enabled: checked },
                  })
                }
                className="[&.ant-switch-checked]:!bg-brand-gold"
              />
              <Select
                value={orderConfig.receiptBar.printer}
                onChange={(val) =>
                  setOrderConfig({
                    ...orderConfig,
                    receiptBar: { ...orderConfig.receiptBar, printer: val },
                  })
                }
                options={PRINTER_OPTIONS}
                className="!w-40 xunye-select"
                popupClassName="xunye-select-dropdown"
              />
            </div>
          </ConfigField>

          <ConfigField label="后厨打印">
            <div className="flex items-center gap-3">
              <Switch
                checked={orderConfig.receiptKitchen.enabled}
                onChange={(checked) =>
                  setOrderConfig({
                    ...orderConfig,
                    receiptKitchen: { ...orderConfig.receiptKitchen, enabled: checked },
                  })
                }
                className="[&.ant-switch-checked]:!bg-brand-gold"
              />
              <Select
                value={orderConfig.receiptKitchen.printer}
                onChange={(val) =>
                  setOrderConfig({
                    ...orderConfig,
                    receiptKitchen: { ...orderConfig.receiptKitchen, printer: val },
                  })
                }
                options={PRINTER_OPTIONS}
                className="!w-40 xunye-select"
                popupClassName="xunye-select-dropdown"
              />
            </div>
          </ConfigField>
        </ConfigSection>

        <ConfigSection
          title="订单规则"
          desc="配置订单自动取消时间等规则"
          onSave={handleSaveOrder}
          saving={orderSaving}
        >
          <ConfigField label="自动取消时间" description="下单后未支付订单的自动取消时间（分钟）">
            <InputNumber
              value={Number(orderConfig.cancelTimeout)}
              onChange={(val) =>
                setOrderConfig({ ...orderConfig, cancelTimeout: String(val || 30) })
              }
              min={1}
              max={1440}
              addonAfter="分钟"
              className="!w-48 [&_.ant-input-number-group-addon]:!bg-sidebar-bg [&_.ant-input-number-group-addon]:!text-text-weak [&_.ant-input-number-group-addon]:!border-border-dark"
              variant="outlined"
            />
          </ConfigField>
        </ConfigSection>
      </div>
    );
  };

  // ========== Render: Miniapp Config ==========
  const renderMiniappConfig = () => {
    if (!miniappConfig) return null;

    return (
      <div className="space-y-4">
        <ConfigSection
          title="首页文案"
          desc="配置顾客端小程序首页展示文案"
          loading={miniappLoading}
        >
          <ConfigField label="首页标题">
            <Input
              value={miniappConfig.homepageTitle}
              onChange={(e) =>
                setMiniappConfig({ ...miniappConfig, homepageTitle: e.target.value })
              }
              className="!bg-sidebar-bg !border-border-dark !text-text-main !placeholder-text-weak"
            />
          </ConfigField>

          <ConfigField label="首页副标题">
            <Input
              value={miniappConfig.homepageSubtitle}
              onChange={(e) =>
                setMiniappConfig({ ...miniappConfig, homepageSubtitle: e.target.value })
              }
              className="!bg-sidebar-bg !border-border-dark !text-text-main !placeholder-text-weak"
            />
          </ConfigField>
        </ConfigSection>

        {/* 首页轮播图 */}
        <ConfigSection
          title="首页轮播图"
          desc="上传酒水小吃展示图片（建议尺寸 750×400px），将在顾客端首页轮播展示"
        >
          <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 gap-3">
            {miniappConfig.bannerImages.map((url) => (
              <div
                key={url}
                className="relative group aspect-[15/8] bg-sidebar-bg rounded-lg overflow-hidden border border-border-dark"
              >
                <img
                  src={url}
                  alt="轮播图"
                  className="w-full h-full object-cover"
                />
                <div
                  onClick={() => handleDeleteBannerImage(url)}
                  className="absolute inset-0 bg-black/0 group-hover:bg-black/50 transition-colors flex items-center justify-center cursor-pointer"
                >
                  <div className="opacity-0 group-hover:opacity-100 transition-opacity p-1.5 bg-danger/90 rounded-full">
                    <Trash2 size={14} className="text-white" />
                  </div>
                </div>
              </div>
            ))}

            {/* 上传按钮 */}
            <Upload
              accept="image/*"
              showUploadList={false}
              beforeUpload={handleUploadBannerImage as any}
              disabled={uploadingImage}
            >
              <div
                className={`aspect-[15/8] border-2 border-dashed border-border-dark rounded-lg flex flex-col items-center justify-center gap-1.5 cursor-pointer transition-colors hover:border-brand-gold hover:bg-brand-gold/5 ${
                  uploadingImage ? 'opacity-50 pointer-events-none' : ''
                }`}
              >
                {uploadingImage ? (
                  <Spin size="small" />
                ) : (
                  <>
                    <UploadIcon size={20} className="text-text-weak" />
                    <span className="text-[11px] text-text-weak">上传图片</span>
                  </>
                )}
              </div>
            </Upload>
          </div>

          {miniappConfig.bannerImages.length === 0 && !uploadingImage && (
            <p className="text-[11px] text-text-weak mt-2">暂无轮播图，请点击上方按钮上传</p>
          )}
        </ConfigSection>

        <ConfigSection
          title="菜单展示"
          desc="配置小程序商品菜单展示规则"
          onSave={handleSaveMiniapp}
          saving={miniappSaving}
        >
          <ConfigField label="展示模式">
            <Select
              value={miniappConfig.menuDisplay}
              onChange={(val) => setMiniappConfig({ ...miniappConfig, menuDisplay: val })}
              options={MENU_DISPLAY_OPTIONS}
              className="!w-56 xunye-select"
              popupClassName="xunye-select-dropdown"
            />
          </ConfigField>

          <ConfigField label="扫码点单" description="开启后顾客可通过扫描桌台二维码自助下单">
            <Switch
              checked={miniappConfig.scanToOrder}
              onChange={(checked) =>
                setMiniappConfig({ ...miniappConfig, scanToOrder: checked })
              }
              className="[&.ant-switch-checked]:!bg-brand-gold"
            />
          </ConfigField>
        </ConfigSection>
      </div>
    );
  };

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
        <div className="flex-1 min-w-0 overflow-y-auto space-y-4">
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
          content: { background: '#1A1A1F', border: '1px solid #2A2A31' },
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
          {loadingBrands ? (
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
