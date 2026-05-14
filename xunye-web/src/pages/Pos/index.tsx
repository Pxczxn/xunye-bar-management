'use client';

import { useEffect, useState, useCallback, useMemo } from 'react';
import { Input, message } from 'antd';
import { useNavigate } from 'react-router-dom';
import { getTablePage } from '@/api/table';
import { getProductPage } from '@/api/product';
import { createOrder, payOrder } from '@/api/order';
import type { BarTable, ProductItem } from '@/types/api';
import {
  Search,
  Plus,
  Minus,
  Trash2,
  ShoppingCart,
  Send,
  RefreshCw,
  XCircle,
} from 'lucide-react';

interface CartItem {
  productId: number;
  productName: string;
  price: number;
  quantity: number;
  stock: number;
  unit: string;
}

interface LastOrder {
  id: number;
  paid: boolean;
}

const TABLE_STATUS_MAP: Record<string, { label: string; color: string; bg: string; border: string }> = {
  EMPTY: { label: '空闲', color: 'text-success', bg: 'bg-success/10', border: 'border-success/30' },
  USING: { label: '使用中', color: 'text-danger', bg: 'bg-danger/10', border: 'border-danger/30' },
  CLEANING: { label: '清洁中', color: 'text-brand-gold', bg: 'bg-brand-gold/10', border: 'border-brand-gold/30' },
};

export default function PosPage() {
  const navigate = useNavigate();
  const [tables, setTables] = useState<BarTable[]>([]);
  const [tablesLoading, setTablesLoading] = useState(false);
  const [selectedTable, setSelectedTable] = useState<BarTable | null>(null);

  const [products, setProducts] = useState<ProductItem[]>([]);
  const [productsLoading, setProductsLoading] = useState(false);
  const [searchKeyword, setSearchKeyword] = useState('');

  const [cart, setCart] = useState<CartItem[]>([]);
  const [remark, setRemark] = useState('');
  const [lastOrder, setLastOrder] = useState<LastOrder | null>(null);
  const [submitting, setSubmitting] = useState(false);
  const [paymentMethod, setPaymentMethod] = useState<'WECHAT' | 'ALIPAY' | 'CASH'>('WECHAT');

  const fetchTables = useCallback(async () => {
    setTablesLoading(true);
    try {
      const res = await getTablePage({ pageNum: 1, pageSize: 100 });
      const allTables = res?.records ?? [];
      setTables(allTables.filter((t) => t.status !== 'DISABLED'));
    } catch (err: any) {
      message.error(err.message || '获取桌台列表失败');
      setTables([]);
    } finally {
      setTablesLoading(false);
    }
  }, []);

  const fetchProducts = useCallback(async () => {
    setProductsLoading(true);
    try {
      const res = await getProductPage({ pageNum: 1, pageSize: 100, status: 'ON_SALE' });
      setProducts(res?.records ?? []);
    } catch (err: any) {
      message.error(err.message || '获取商品列表失败');
      setProducts([]);
    } finally {
      setProductsLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchTables();
    fetchProducts();
  }, [fetchTables, fetchProducts]);

  const filteredProducts = useMemo(() => {
    if (!searchKeyword.trim()) return products;
    const kw = searchKeyword.trim().toLowerCase();
    return products.filter((p) => p.name.toLowerCase().includes(kw));
  }, [products, searchKeyword]);

  const addToCart = (product: ProductItem) => {
    if (product.stock <= 0) return;
    setCart((prev) => {
      const existing = prev.find((item) => item.productId === product.id);
      if (existing) {
        if (existing.quantity >= product.stock) {
          message.warning('已达到库存上限');
          return prev;
        }
        return prev.map((item) =>
          item.productId === product.id
            ? { ...item, quantity: item.quantity + 1 }
            : item
        );
      }
      return [
        ...prev,
        {
          productId: product.id,
          productName: product.name,
          price: product.price,
          quantity: 1,
          stock: product.stock,
          unit: product.unit,
        },
      ];
    });
  };

  const updateQuantity = (productId: number, delta: number) => {
    setCart((prev) =>
      prev
        .map((item) => {
          if (item.productId !== productId) return item;
          const next = item.quantity + delta;
          if (next < 1) return item;
          if (next > item.stock) {
            message.warning('已达到库存上限');
            return item;
          }
          return { ...item, quantity: next };
        })
        .filter(Boolean) as CartItem[]
    );
  };

  const removeFromCart = (productId: number) => {
    setCart((prev) => prev.filter((item) => item.productId !== productId));
  };

  const clearCart = () => {
    setCart([]);
    setRemark('');
  };

  const totalAmount = useMemo(
    () => cart.reduce((sum, item) => sum + item.price * item.quantity, 0),
    [cart]
  );

  const handleSubmitOrder = async (payImmediately = false) => {
    if (!selectedTable) {
      message.warning('请先选择桌台');
      return;
    }
    if (cart.length === 0) {
      message.warning('请先添加商品');
      return;
    }
    setSubmitting(true);
    try {
      const orderId = await createOrder({
        tableId: selectedTable.id,
        remark: remark.trim() || undefined,
        items: cart.map((item) => ({
          productId: item.productId,
          quantity: item.quantity,
        })),
      });
      if (payImmediately) {
        await payOrder(orderId, { paymentMethod });
        message.success('下单并收款成功');
      } else {
        message.success('下单成功，订单已挂起待收款');
      }
      setCart([]);
      setRemark('');
      setLastOrder({ id: orderId, paid: payImmediately });
      setSelectedTable(null);
      fetchTables();
      fetchProducts();
    } catch (err: any) {
      const msg = err.message || '下单失败';
      if (msg.includes('库存不足') || msg.includes('stock')) {
        message.error('库存不足，请调整商品数量');
      } else {
        message.error(msg);
      }
    } finally {
      setSubmitting(false);
    }
  };

  const handleRefresh = () => {
    fetchTables();
    fetchProducts();
  };

  return (
    <div className="h-[calc(100vh-7rem)] flex flex-col gap-4 overflow-hidden">
      <div className="flex flex-col sm:flex-row justify-between items-start sm:items-end gap-4 shrink-0">
        <div>
          <h1 className="text-2xl font-serif font-bold text-text-main tracking-wider mb-1">吧台点单</h1>
          <p className="text-[10px] text-brand-gold uppercase tracking-widest font-medium">POS</p>
        </div>
        <button
          onClick={handleRefresh}
          className="flex items-center space-x-2 border border-border-dark text-text-sub px-4 py-2 rounded-lg text-sm hover:border-brand-gold/50 hover:text-text-main transition-colors"
        >
          <RefreshCw size={16} />
          <span>刷新</span>
        </button>
      </div>

      <div className="flex-1 flex gap-4 min-h-0 overflow-hidden">
        {/* Left: Table Selection */}
        <div className="w-1/4 bg-card-bg border border-border-dark rounded-xl flex flex-col overflow-hidden">
          <div className="p-4 border-b border-border-dark shrink-0">
            <h3 className="text-sm font-semibold text-text-main">选择桌台</h3>
            {selectedTable && (
              <p className="text-[10px] text-brand-gold mt-1">
                当前：{selectedTable.name}
              </p>
            )}
          </div>
          <div className="flex-1 overflow-y-auto p-3 space-y-2">
            {tablesLoading ? (
              <div className="flex items-center justify-center py-12">
                <div className="flex items-center space-x-2 text-text-weak">
                  <div className="w-4 h-4 border-2 border-brand-gold border-t-transparent rounded-full animate-spin" />
                  <span className="text-xs">加载中...</span>
                </div>
              </div>
            ) : tables.length === 0 ? (
              <div className="flex flex-col items-center justify-center py-12 text-text-weak">
                <XCircle size={24} className="mb-2" />
                <span className="text-xs">暂无可用桌台</span>
              </div>
            ) : (
              tables.map((table) => {
                const statusInfo = TABLE_STATUS_MAP[table.status];
                const isSelected = selectedTable?.id === table.id;
                return (
                  <button
                    key={table.id}
                    onClick={() => setSelectedTable(isSelected ? null : table)}
                    className={`w-full text-left p-3 rounded-lg border transition-all ${
                      isSelected
                        ? 'bg-brand-gold/15 border-brand-gold/50'
                        : 'bg-sidebar-bg border-border-dark hover:border-brand-gold/30'
                    }`}
                  >
                    <div className="flex items-center justify-between mb-1.5">
                      <span className={`text-sm font-medium ${isSelected ? 'text-brand-gold' : 'text-text-main'}`}>
                        {table.name}
                      </span>
                      <span className="text-[10px] text-text-weak">{table.areaName}</span>
                    </div>
                    <div className="flex items-center gap-2">
                      <span className={`inline-flex items-center px-2 py-0.5 rounded text-[10px] ${statusInfo?.bg} ${statusInfo?.color} border ${statusInfo?.border}`}>
                        {statusInfo?.label}
                      </span>
                      <span className="text-[10px] text-text-weak">{table.capacity}人</span>
                    </div>
                  </button>
                );
              })
            )}
          </div>
        </div>

        {/* Middle: Product List */}
        <div className="w-[40%] bg-card-bg border border-border-dark rounded-xl flex flex-col overflow-hidden">
          <div className="p-4 border-b border-border-dark shrink-0">
            <div className="flex items-center justify-between mb-3">
              <h3 className="text-sm font-semibold text-text-main">商品列表</h3>
              <span className="text-[10px] text-text-weak">{filteredProducts.length} 件商品</span>
            </div>
            <Input
              placeholder="搜索商品名称"
              value={searchKeyword}
              onChange={(e) => setSearchKeyword(e.target.value)}
              allowClear
              prefix={<Search size={14} className="text-text-weak" />}
              className="!bg-sidebar-bg !border-border-dark !text-text-main !placeholder-text-weak"
            />
          </div>
          <div className="flex-1 overflow-y-auto p-3 space-y-2">
            {productsLoading ? (
              <div className="flex items-center justify-center py-12">
                <div className="flex items-center space-x-2 text-text-weak">
                  <div className="w-4 h-4 border-2 border-brand-gold border-t-transparent rounded-full animate-spin" />
                  <span className="text-xs">加载中...</span>
                </div>
              </div>
            ) : filteredProducts.length === 0 ? (
              <div className="flex flex-col items-center justify-center py-12 text-text-weak">
                <XCircle size={24} className="mb-2" />
                <span className="text-xs">{searchKeyword ? '无匹配商品' : '暂无可售商品'}</span>
              </div>
            ) : (
              filteredProducts.map((product) => {
                const cartItem = cart.find((c) => c.productId === product.id);
                const outOfStock = product.stock <= 0;
                return (
                  <div
                    key={product.id}
                    className="flex items-center justify-between p-3 rounded-lg bg-sidebar-bg border border-border-dark hover:border-brand-gold/20 transition-colors"
                  >
                    <div className="flex-1 min-w-0 mr-3">
                      <div className="flex items-center gap-2 mb-1">
                        <span className="text-sm font-medium text-text-main truncate">{product.name}</span>
                        {cartItem && (
                          <span className="shrink-0 inline-flex items-center justify-center w-5 h-5 rounded-full bg-brand-gold/20 text-brand-gold text-[10px] font-bold">
                            {cartItem.quantity}
                          </span>
                        )}
                      </div>
                      <div className="flex items-center gap-3 text-[10px] text-text-weak">
                        <span>{product.categoryName}</span>
                        <span className="text-brand-gold font-sans">¥{product.price}</span>
                        <span>库存: {product.stock} {product.unit}</span>
                      </div>
                    </div>
                    <button
                      onClick={() => addToCart(product)}
                      disabled={outOfStock}
                      className={`shrink-0 flex items-center justify-center w-8 h-8 rounded-lg transition-colors ${
                        outOfStock
                          ? 'bg-border-dark/30 text-text-weak cursor-not-allowed'
                          : 'bg-brand-gold/15 text-brand-gold hover:bg-brand-gold/25 border border-brand-gold/30'
                      }`}
                    >
                      <Plus size={16} />
                    </button>
                  </div>
                );
              })
            )}
          </div>
        </div>

        {/* Right: Cart */}
        <div className="w-[35%] bg-card-bg border border-border-dark rounded-xl flex flex-col overflow-hidden">
          <div className="p-4 border-b border-border-dark shrink-0">
            <div className="flex items-center justify-between">
              <div className="flex items-center gap-2">
                <ShoppingCart size={16} className="text-brand-gold" />
                <h3 className="text-sm font-semibold text-text-main">购物车</h3>
              </div>
              {cart.length > 0 && (
                <button
                  onClick={clearCart}
                  className="text-[10px] text-text-weak hover:text-danger transition-colors"
                >
                  清空
                </button>
              )}
            </div>
          </div>

          {lastOrder && (
            <div className="mx-3 mt-3 rounded-xl border border-brand-gold/30 bg-brand-gold/10 p-3 text-xs">
              <div className="flex items-start justify-between gap-3">
                <div>
                  <p className="font-semibold text-brand-gold">
                    {lastOrder.paid ? '订单已收款' : '订单已挂起'}
                  </p>
                  <p className="mt-1 text-text-sub">
                    订单 #{lastOrder.id} {lastOrder.paid ? '已进入出品队列' : '仍需在订单流水中完成收款'}
                  </p>
                </div>
                <button
                  onClick={() => setLastOrder(null)}
                  className="text-text-weak transition-colors hover:text-text-main"
                  type="button"
                >
                  继续点单
                </button>
              </div>
              <div className="mt-3 grid grid-cols-2 gap-2">
                <button
                  onClick={() => navigate('/orders')}
                  className="rounded-lg border border-border-dark px-3 py-2 text-text-sub transition-colors hover:border-brand-gold/40 hover:text-brand-gold"
                  type="button"
                >
                  查看订单流水
                </button>
                <button
                  onClick={() => navigate('/kitchen')}
                  className="rounded-lg bg-brand-gold px-3 py-2 font-semibold text-page-bg transition-colors hover:bg-brand-gold/90 disabled:cursor-not-allowed disabled:opacity-40"
                  disabled={!lastOrder.paid}
                  type="button"
                >
                  去出品看板
                </button>
              </div>
            </div>
          )}

          <div className="flex-1 overflow-y-auto p-3 space-y-2">
            {cart.length === 0 ? (
              <div className="flex flex-col items-center justify-center py-12 text-text-weak">
                <ShoppingCart size={24} className="mb-2 opacity-30" />
                <span className="text-xs">购物车为空</span>
              </div>
            ) : (
              cart.map((item) => (
                <div
                  key={item.productId}
                  className="p-3 rounded-lg bg-sidebar-bg border border-border-dark"
                >
                  <div className="flex items-start justify-between mb-2">
                    <div className="flex-1 min-w-0 mr-2">
                      <span className="text-sm font-medium text-text-main block truncate">{item.productName}</span>
                      <span className="text-[10px] text-text-weak">¥{item.price} / {item.unit}</span>
                    </div>
                    <button
                      onClick={() => removeFromCart(item.productId)}
                      className="shrink-0 p-1 rounded hover:bg-border-dark/40 text-text-weak hover:text-danger transition-colors"
                    >
                      <Trash2 size={12} />
                    </button>
                  </div>
                  <div className="flex items-center justify-between">
                    <div className="flex items-center gap-2">
                      <button
                        onClick={() => updateQuantity(item.productId, -1)}
                        disabled={item.quantity <= 1}
                        className="w-7 h-7 flex items-center justify-center rounded border border-border-dark text-text-sub hover:border-brand-gold/50 disabled:opacity-30 disabled:cursor-not-allowed transition-colors"
                      >
                        <Minus size={12} />
                      </button>
                      <span className="w-8 text-center text-sm font-sans text-text-main">{item.quantity}</span>
                      <button
                        onClick={() => updateQuantity(item.productId, 1)}
                        disabled={item.quantity >= item.stock}
                        className="w-7 h-7 flex items-center justify-center rounded border border-border-dark text-text-sub hover:border-brand-gold/50 disabled:opacity-30 disabled:cursor-not-allowed transition-colors"
                      >
                        <Plus size={12} />
                      </button>
                    </div>
                    <span className="text-sm font-sans text-brand-gold font-medium">
                      ¥{(item.price * item.quantity).toFixed(2)}
                    </span>
                  </div>
                </div>
              ))
            )}
          </div>

          <div className="p-4 border-t border-border-dark shrink-0 space-y-3">
            <Input.TextArea
              value={remark}
              onChange={(e) => setRemark(e.target.value)}
              placeholder="订单备注，例如少冰、先上、客人要求..."
              autoSize={{ minRows: 2, maxRows: 3 }}
              maxLength={120}
              showCount
              className="!bg-sidebar-bg !border-border-dark !text-text-main !placeholder-text-weak"
            />
            <div className="flex items-center justify-between">
              <span className="text-sm text-text-sub">合计</span>
              <span className="text-lg font-sans font-bold text-brand-gold">
                ¥{totalAmount.toFixed(2)}
              </span>
            </div>
            <div className="grid grid-cols-3 gap-2">
              {[
                { key: 'WECHAT' as const, label: '微信' },
                { key: 'ALIPAY' as const, label: '支付宝' },
                { key: 'CASH' as const, label: '现金' },
              ].map((item) => (
                <button
                  key={item.key}
                  onClick={() => setPaymentMethod(item.key)}
                  className={`py-2 rounded-lg border text-xs font-medium transition-colors ${
                    paymentMethod === item.key
                      ? 'border-brand-gold bg-brand-gold/10 text-brand-gold'
                      : 'border-border-dark text-text-sub hover:border-brand-gold/40 hover:text-text-main'
                  }`}
                  type="button"
                >
                  {item.label}
                </button>
              ))}
            </div>
            <button
              onClick={() => handleSubmitOrder(true)}
              disabled={submitting || cart.length === 0 || !selectedTable}
              className="w-full flex items-center justify-center gap-2 bg-brand-gold text-page-bg px-4 py-3 rounded-lg font-semibold text-sm hover:bg-brand-gold/90 disabled:opacity-40 disabled:cursor-not-allowed transition-colors tracking-wider uppercase"
            >
              {submitting ? (
                <>
                  <div className="w-4 h-4 border-2 border-page-bg border-t-transparent rounded-full animate-spin" />
                  <span>提交中...</span>
                </>
              ) : (
                <>
                  <Send size={16} />
                  <span>下单并收款</span>
                </>
              )}
            </button>
            <button
              onClick={() => handleSubmitOrder(false)}
              disabled={submitting || cart.length === 0 || !selectedTable}
              className="w-full flex items-center justify-center gap-2 border border-border-dark text-text-sub px-4 py-2.5 rounded-lg font-medium text-sm hover:border-brand-gold/50 hover:text-text-main disabled:opacity-40 disabled:cursor-not-allowed transition-colors"
              type="button"
            >
              仅下单，稍后收款
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}
