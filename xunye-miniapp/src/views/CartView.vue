<script lang="ts" setup>
import { computed, onMounted, ref, watch } from 'vue'
import type { CustomerCouponVO } from '@/api/customer'
import { createCustomerOrder, listCustomerCoupons } from '@/api/customer'
import { useShellState } from '@/composables/useShellState'
import { useXunyeStore } from '@/store'
import { useCustomerProfileStore } from '@/store/customerProfile'

const store = useXunyeStore()
const customerProfileStore = useCustomerProfileStore()
const { back, push, showToast } = useShellState()
const remark = ref('')
const submitting = ref(false)

const coupons = ref<CustomerCouponVO[]>([])
const loadingCoupons = ref(false)

const mappedCoupons = computed(() => {
  const total = store.totalAmount
  return coupons.value.map(c => ({
    ...c,
    isUsable: !c.used && c.minAmount <= total,
  }))
})

function getCouponAfterPrice(coupon: CustomerCouponVO) {
  return Math.max(0, store.totalAmount - coupon.discountAmount)
}

function getCouponStatusText(coupon: CustomerCouponVO) {
  if (coupon.used)
    return '已使用'
  if (coupon.minAmount > store.totalAmount) {
    const diff = coupon.minAmount - store.totalAmount
    return `还差 ¥${diff.toFixed(2)} 可用`
  }
  return ''
}

function findBestCoupon(): CustomerCouponVO | null {
  const total = store.totalAmount
  let best: CustomerCouponVO | null = null
  for (const c of coupons.value) {
    if (!c.used && c.minAmount <= total) {
      if (!best || c.discountAmount > best.discountAmount) {
        best = c
      }
    }
  }
  return best
}

function applyBestCoupon() {
  const best = findBestCoupon()
  if (best) {
    store.applyCoupon({
      id: best.id,
      title: best.title,
      rule: best.rule,
      discountAmount: best.discountAmount,
    })
  }
  else {
    store.removeCoupon()
  }
}

function selectCoupon(coupon: any) {
  if (!coupon.isUsable)
    return
  if (store.activeCoupon?.id === coupon.id) {
    store.removeCoupon()
  }
  else {
    store.applyCoupon({
      id: coupon.id,
      title: coupon.title,
      rule: coupon.rule,
      discountAmount: coupon.discountAmount,
    })
  }
}

// Watch totalAmount: if the current active coupon becomes unusable, automatically select the next best one
watch(() => store.totalAmount, (newTotal) => {
  if (store.activeCoupon) {
    const currentCouponObj = coupons.value.find(c => c.id === store.activeCoupon?.id)
    if (!currentCouponObj || currentCouponObj.minAmount > newTotal) {
      applyBestCoupon()
    }
  }
})

function addProduct(product: any) {
  store.addProduct(product)
  showToast(`已添加 ${product.name}`)
}

function clearCart() {
  store.clearCart()
  showToast('已清空购物车')
}

async function submitOrder() {
  if (!store.totalQty) {
    showToast('请先选择商品')
    return
  }
  if (!store.currentTable?.id) {
    showToast('请先选择桌台')
    return
  }
  if (submitting.value)
    return
  submitting.value = true
  try {
    const order = await createCustomerOrder({
      tableId: store.currentTable.id,
      phone: customerProfileStore.profile.phone,
      couponId: store.activeCoupon?.id || null,
      items: store.cartItems.map(item => ({
        productId: item.id,
        quantity: item.qty,
      })),
      remark: remark.value,
    })
    store.setLastOrder({
      orderNo: order.orderNo,
      createdAt: new Date().toLocaleString('zh-CN', { hour12: false }).replace(/\//g, '-'),
      table: store.currentTable,
      items: store.cartItems.map(item => ({ ...item })),
      originalAmount: Number(order.originalAmount || order.totalAmount),
      discountAmount: Number(order.discountAmount || 0),
      activityDiscountAmount: Number(order.activityDiscountAmount || 0),
      couponDiscountAmount: Number(order.couponDiscountAmount || 0),
      activityName: order.activityName || null,
      totalAmount: Number(order.totalAmount),
      coupon: store.activeCoupon ? { ...store.activeCoupon } : null,
      remark: remark.value,
      status: order.status,
      serveStatus: 'PENDING',
    })
    showToast('订单已提交')
    push('payment')
  }
  catch {
    showToast('订单提交失败')
  }
  finally {
    submitting.value = false
  }
}

onMounted(async () => {
  loadingCoupons.value = true
  const phone = customerProfileStore.profile.phone
  if (phone) {
    try {
      const all = await listCustomerCoupons(phone)
      coupons.value = all
      applyBestCoupon()
    }
    catch {
      // silent
    }
    finally {
      loadingCoupons.value = false
    }
  }
})
</script>

<template>
  <view class="view">
    <view class="topbar">
      <button class="icon-button" hover-class="none" @tap="back">
        <uv-icon name="arrow-left" color="#f7f1e8" size="20" />
      </button>
      <view class="top-title">
        确认订单
      </view>
      <uv-button text="清空" type="info" plain size="mini" shape="circle" custom-style="height: 28px; color: #8d929d; border-color: rgba(255,255,255,0.12); background: transparent;" @click="clearCart" />
    </view>
    <scroll-view class="content cart-content" scroll-y enhanced show-scrollbar="false">
      <view class="summary-card">
        <view class="summary-icon">
          🪷
        </view>
        <view>
          <view class="muted small">
            就餐桌台
          </view>
          <view class="bold">
            {{ store.currentTable?.code || '未选桌' }}
          </view>
        </view>
      </view>
      <view class="panel">
        <view class="panel-title">
          已选商品
        </view>
        <view v-if="!store.totalQty" class="empty-line">
          购物车空空如也
        </view>
        <view v-for="item in store.cartItems" :key="item.id" class="cart-line">
          <view>
            <view class="product-name">
              {{ item.name }}
            </view>
            <view class="price">
              ¥{{ item.price }}
            </view>
          </view>
          <view class="stepper">
            <button class="minus gray" @tap="store.decreaseProduct(item.id)">
              <uv-icon name="minus" color="#f7f1e8" size="13" />
            </button>
            <text class="qty">{{ item.qty }}</text>
            <button class="round-plus" @tap="addProduct(item)">
              <uv-icon name="plus" color="#111318" size="13" />
            </button>
          </view>
        </view>
      </view>
      <view class="panel">
        <view class="remark-row">
          <text>订单备注</text>
          <input v-model="remark" class="remark-input" placeholder="口味、偏好等要求" placeholder-class="placeholder">
        </view>
      </view>
      <!-- 优惠券选择面板 -->
      <view class="panel">
        <view class="panel-header">
          <text class="panel-title">我的优惠券</text>
          <text v-if="store.activeCoupon" class="active-badge">已享最佳优惠</text>
        </view>
        
        <view v-if="loadingCoupons" class="loading-state">
          <text class="muted small">正在加载优惠券...</text>
        </view>
        <view v-else-if="coupons.length === 0" class="empty-state">
          <text class="muted small">暂无优惠券</text>
        </view>
        <view v-else class="coupon-list">
          <view
            v-for="coupon in mappedCoupons"
            :key="coupon.id"
            class="coupon-card"
            :class="{ 
              active: store.activeCoupon?.id === coupon.id,
              disabled: !coupon.isUsable 
            }"
            @tap="selectCoupon(coupon)"
          >
            <view class="coupon-left">
              <view class="coupon-price">
                <text class="symbol">¥</text>
                <text class="val">{{ coupon.discountAmount }}</text>
              </view>
              <view class="coupon-condition">
                {{ coupon.minAmount > 0 ? `满${coupon.minAmount}可用` : '无门槛' }}
              </view>
            </view>
            <view class="coupon-body">
              <view class="coupon-title">
                {{ coupon.title }}
              </view>
              <view class="coupon-desc">
                {{ coupon.rule }}
              </view>
              <view v-if="coupon.isUsable" class="coupon-after">
                券后: <text class="after-price">¥{{ getCouponAfterPrice(coupon).toFixed(2) }}</text>
              </view>
              <view v-else class="coupon-status-text">
                {{ getCouponStatusText(coupon) }}
              </view>
            </view>
            <view class="coupon-right">
              <view class="check-circle" :class="{ checked: store.activeCoupon?.id === coupon.id }">
                <text v-if="store.activeCoupon?.id === coupon.id">✓</text>
              </view>
            </view>
          </view>
        </view>
      </view>
      <!-- 价格明细面板 -->
      <view class="panel">
        <view class="panel-title">
          价格明细
        </view>
        <view class="price-detail-list">
          <view class="price-detail-row">
            <text class="detail-label">商品原价</text>
            <text class="detail-value">¥{{ store.totalAmount.toFixed(2) }}</text>
          </view>
          <view v-if="store.activeCoupon" class="price-detail-row discount-row">
            <text class="detail-label">
              <text class="discount-icon">🎫</text>
              优惠券「{{ store.activeCoupon.title }}」
            </text>
            <text class="detail-value discount-value">-¥{{ store.discountAmount.toFixed(2) }}</text>
          </view>
          <view class="price-detail-hint">
            <text class="hint-icon">💡</text>
            <text class="hint-text">会员活动折扣将在提交订单后自动计算</text>
          </view>
          <view class="price-detail-divider" />
          <view class="price-detail-row total-row">
            <text class="detail-label bold">预估金额</text>
            <text class="detail-value total-value">¥{{ store.payableAmount.toFixed(2) }}</text>
          </view>
        </view>
      </view>
    </scroll-view>
    <view class="bottom-pay">
      <view>
        <view v-if="store.discountAmount" class="muted small">
          已优惠 ¥{{ store.discountAmount.toFixed(2) }}
        </view>
        <view v-else class="muted small">
          合计
        </view>
        <view class="pay-total">
          ¥{{ store.payableAmount.toFixed(2) }}
        </view>
      </view>
      <uv-button
        :text="submitting ? '提交中...' : '提交订单'"
        color="linear-gradient(135deg, #d2a85f, #bc8945)"
        shape="circle"
        custom-style="height: 44px; padding: 0 28px; color: #111318; font-weight: 800;"
        @click="submitOrder"
      />
    </view>
  </view>
</template>

<style scoped>
.cart-content {
  flex: 1;
}
.summary-card {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  margin-bottom: 12px;
  background: rgba(21, 23, 27, 0.94);
  border: 1px solid var(--xunye-line);
  border-radius: 16px;
}
.summary-icon {
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(210, 168, 95, 0.14);
  border-radius: 999px;
  font-size: 18px;
}
.empty-line {
  padding: 24px 0;
  text-align: center;
  color: #858585;
  font-size: 14px;
}
.cart-line {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 0;
  border-bottom: 1px solid var(--xunye-line);
}
.remark-row {
  display: flex;
  align-items: center;
  gap: 10px;
}
.remark-input {
  flex: 1;
  height: 38px;
  line-height: 38px;
  padding: 0 12px;
  background: rgba(255, 255, 255, 0.06);
  border-radius: 8px;
  color: #fff;
  font-size: 13px;
}
.discount-line {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}
.bottom-pay {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  background: #1c1c1c;
  border-top: 1px solid #2a2a2a;
}
.pay-total {
  font-size: 22px;
  font-weight: 700;
  color: #c79f62;
}
.minus.gray {
  background: var(--xunye-surface-2);
  color: #fff;
}
.panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}
.active-badge {
  font-size: 11px;
  color: var(--xunye-gold);
  background: rgba(210, 168, 95, 0.15);
  padding: 2px 8px;
  border-radius: 99px;
  border: 1px solid rgba(210, 168, 95, 0.3);
}
.loading-state, .empty-state {
  text-align: center;
  padding: 16px 0;
}
.coupon-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.coupon-card {
  display: flex;
  align-items: center;
  padding: 12px;
  background: rgba(255, 255, 255, 0.03);
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: 12px;
  transition: all 0.2s ease;
}
.coupon-card.active {
  border-color: rgba(210, 168, 95, 0.6);
  background: rgba(210, 168, 95, 0.06);
}
.coupon-card.disabled {
  opacity: 0.45;
}
.coupon-left {
  flex-shrink: 0;
  width: 76px;
  text-align: center;
  border-right: 1px dashed rgba(255, 255, 255, 0.12);
  padding-right: 10px;
  margin-right: 10px;
}
.coupon-price {
  color: var(--xunye-gold);
  font-weight: 800;
  display: flex;
  align-items: baseline;
  justify-content: center;
}
.coupon-price .symbol {
  font-size: 12px;
}
.coupon-price .val {
  font-size: 20px;
}
.coupon-condition {
  font-size: 10px;
  color: #8d929d;
  margin-top: 2px;
}
.coupon-body {
  flex: 1;
  min-width: 0;
}
.coupon-title {
  font-size: 13px;
  font-weight: 700;
  color: #f7f1e8;
  margin-bottom: 2px;
}
.coupon-desc {
  font-size: 11px;
  color: #8d929d;
  margin-bottom: 4px;
}
.coupon-after {
  font-size: 11px;
  color: #8d929d;
}
.coupon-after .after-price {
  color: var(--xunye-gold);
  font-weight: 700;
  font-size: 13px;
  margin-left: 2px;
}
.coupon-status-text {
  font-size: 11px;
  color: #858585;
}
.coupon-right {
  flex-shrink: 0;
  margin-left: 10px;
}
.check-circle {
  width: 18px;
  height: 18px;
  border-radius: 99px;
  border: 1px solid rgba(255, 255, 255, 0.2);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 11px;
  font-weight: bold;
  color: transparent;
  transition: all 0.2s ease;
}
.check-circle.checked {
  border-color: var(--xunye-gold);
  background: var(--xunye-gold);
  color: #111318;
}
.price-detail-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.price-detail-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.detail-label {
  font-size: 14px;
  color: #8d929d;
  display: flex;
  align-items: center;
  gap: 6px;
}
.detail-value {
  font-size: 14px;
  color: #f7f1e8;
  font-weight: 600;
}
.discount-row .detail-label {
  color: var(--xunye-gold);
}
.discount-icon {
  font-size: 16px;
}
.discount-value {
  color: var(--xunye-gold);
}
.price-detail-hint {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 12px;
  background: rgba(210, 168, 95, 0.08);
  border-radius: 8px;
  margin: 4px 0;
}
.hint-icon {
  font-size: 14px;
}
.hint-text {
  font-size: 12px;
  color: #8d929d;
  line-height: 1.4;
}
.price-detail-divider {
  height: 1px;
  background: var(--xunye-line);
  margin: 4px 0;
}
.total-row .detail-label {
  font-size: 15px;
  color: #f7f1e8;
}
.total-row .detail-value {
  font-size: 18px;
  color: var(--xunye-gold);
  font-weight: 800;
}
</style>
