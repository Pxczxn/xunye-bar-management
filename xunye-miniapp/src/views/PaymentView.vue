<script lang="ts" setup>
import type { CustomerCouponVO } from '@/api/customer'
import { computed, onMounted, ref } from 'vue'
import { confirmOrderPayment, createOrderPayment, getPaymentStatus, listCustomerCoupons } from '@/api/customer'
import { useShellState } from '@/composables/useShellState'
import { useCustomerProfileStore, useXunyeStore } from '@/store'

const store = useXunyeStore()
const customerProfileStore = useCustomerProfileStore()
const { back, push, replace, showToast } = useShellState()
const paying = ref(false)
const retrying = ref(false)
const coupons = ref<CustomerCouponVO[]>([])

const displayOrder = computed(() => store.lastOrder || {
  orderNo: '',
  createdAt: '',
  table: store.currentTable || { area: '大厅', code: 'A08' },
  items: store.cartItems,
  originalAmount: store.totalAmount,
  discountAmount: store.discountAmount || 0,
  totalAmount: store.payableAmount,
  coupon: store.activeCoupon,
  remark: '',
  status: '制作中',
})

const usableCoupons = computed(() => {
  const total = displayOrder.value.originalAmount
  return coupons.value.filter(c => !c.used && c.minAmount <= total)
})

function findBestCoupon(): CustomerCouponVO | null {
  let best: CustomerCouponVO | null = null
  for (const c of usableCoupons.value) {
    if (!best || c.discountAmount > best.discountAmount) {
      best = c
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
}

function selectCoupon(coupon: CustomerCouponVO) {
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

onMounted(async () => {
  const phone = customerProfileStore.profile.phone
  if (phone) {
    try {
      const all = await listCustomerCoupons(phone)
      coupons.value = all
      // Auto-apply best coupon if none selected
      if (!store.activeCoupon) {
        applyBestCoupon()
      }
    }
    catch {
      // silent
    }
  }
})

async function tryQueryPaymentStatus(paymentNo: string): Promise<boolean> {
  try {
    const result = await getPaymentStatus(paymentNo)
    if (result.status === 'SUCCESS') {
      return true
    }
  }
  catch {
    // query failed, try again
  }
  return false
}

async function executePayment() {
  if (!displayOrder.value.orderNo) {
    showToast('订单不存在')
    return
  }
  if (paying.value || retrying.value)
    return
  paying.value = true
  let paymentNo = ''
  try {
    showToast('正在调用微信支付...')
    const payment = await createOrderPayment(displayOrder.value.orderNo)
    paymentNo = payment.paymentNo
  }
  catch {
    showToast('创建支付单失败，请重试')
    paying.value = false
    return
  }
  try {
    await confirmOrderPayment(paymentNo)
    if (store.lastOrder) {
      store.lastOrder.paymentNo = paymentNo
      store.lastOrder.status = 'PAID'
    }
    store.completePayment()
    replace('orderResult')
  }
  catch {
    // 确认失败时先查询支付状态，可能实际已成功
    const paid = await tryQueryPaymentStatus(paymentNo)
    if (paid) {
      if (store.lastOrder) {
        store.lastOrder.paymentNo = paymentNo
        store.lastOrder.status = 'PAID'
      }
      store.completePayment()
      replace('orderResult')
      return
    }
    showToast('支付确认失败，可重试或联系客服')
    // 保存 paymentNo 以便重试
    retrying.value = true
  }
  finally {
    paying.value = false
  }
}

async function retryConfirm() {
  if (!displayOrder.value.orderNo)
    return
  retrying.value = true
  showToast('正在重试...')
  // 先尝试查询已存在的支付单
  if (store.lastOrder?.paymentNo) {
    const paid = await tryQueryPaymentStatus(store.lastOrder.paymentNo)
    if (paid) {
      store.lastOrder.status = 'PAID'
      store.completePayment()
      replace('orderResult')
      retrying.value = false
      return
    }
  }
  // 重新执行支付流程
  retrying.value = false
  executePayment()
}
</script>

<template>
  <view class="view">
    <view class="topbar">
      <button class="icon-button" hover-class="none" @tap="back">
        <uv-icon name="arrow-left" color="#f7f1e8" size="20" />
      </button>
      <view class="top-title">
        支付订单
      </view>
      <view class="icon-button ghost" />
    </view>
    <scroll-view scroll-y class="payment-body" enhanced show-scrollbar="false">
      <view class="pay-amount">
        ¥{{ displayOrder.totalAmount.toFixed(2) }}
      </view>
      <view class="muted">
        请确认订单并选择支付方式
      </view>
      <view class="panel wide">
        <view class="info-line">
          <text>订单编号</text><text>{{ displayOrder.orderNo }}</text>
        </view>
        <view class="info-line">
          <text>桌台</text><text>{{ displayOrder.table?.code }}</text>
        </view>
        <view class="info-line">
          <text>商品原价</text><text>¥{{ displayOrder.originalAmount.toFixed(2) }}</text>
        </view>
        <view v-if="displayOrder.discountAmount" class="info-line">
          <text>{{ displayOrder.coupon?.title || '优惠券' }}</text><text class="gold">-¥{{ displayOrder.discountAmount.toFixed(2) }}</text>
        </view>
        <view class="info-line total-line">
          <text>支付金额</text><text class="gold">¥{{ displayOrder.totalAmount.toFixed(2) }}</text>
        </view>
      </view>

      <!-- 优惠券区 -->
      <view class="panel wide">
        <view class="section-title">
          优惠券
          <text class="muted small">（{{ usableCoupons.length }}张可用）</text>
        </view>
        <view v-if="usableCoupons.length === 0" class="no-coupon muted small">
          暂无可用优惠券
        </view>
        <view
          v-for="coupon in usableCoupons"
          :key="coupon.id"
          class="coupon-item"
          :class="{ active: store.activeCoupon?.id === coupon.id }"
          @tap="selectCoupon(coupon)"
        >
          <view class="coupon-left">
            <view class="coupon-amount">
              ¥{{ coupon.discountAmount }}
            </view>
            <view v-if="coupon.minAmount > 0" class="coupon-condition">
              满{{ coupon.minAmount }}可用
            </view>
            <view v-else class="coupon-condition">
              无门槛
            </view>
          </view>
          <view class="coupon-right">
            <view class="coupon-title">
              {{ coupon.title }}
            </view>
            <view class="coupon-rule">
              {{ coupon.rule }}
            </view>
            <view class="coupon-expire">
              有效期至 {{ coupon.validUntil?.slice(0, 10) }}
            </view>
          </view>
          <view class="coupon-check">
            <view class="check-circle" :class="[{ checked: store.activeCoupon?.id === coupon.id }]">
              <text v-if="store.activeCoupon?.id === coupon.id">✓</text>
            </view>
          </view>
        </view>
      </view>

      <view class="pay-method active">
        <view>
          <view class="pay-title">
            <uv-icon name="weixin-fill" color="#27c160" size="20" /> 微信支付
          </view>
          <view class="muted small">
            推荐使用
          </view>
        </view>
        <uv-radio-group model-value="wechat" active-color="#d2a85f">
          <uv-radio name="wechat" :label-disabled="true" />
        </uv-radio-group>
      </view>
      <view class="pay-method muted-method">
        <view>
          <view class="pay-title">
            <uv-icon name="bag" color="#8d929d" size="20" /> 到店支付
          </view>
          <view class="muted small">
            暂不可用
          </view>
        </view>
        <uv-radio-group model-value="" disabled>
          <uv-radio name="offline" :label-disabled="true" />
        </uv-radio-group>
      </view>
      <view class="bottom-spacer" />
    </scroll-view>
    <view class="bottom-pay">
      <view v-if="!retrying" class="pay-bar">
        <button class="detail-button" hover-class="none" @tap="push('cart')">
          <text>明细</text>
          <text class="arrow">▶</text>
        </button>
        <uv-button
          :text="paying ? '支付中...' : `确认支付 ¥${displayOrder.totalAmount.toFixed(2)}`"
          color="linear-gradient(135deg, #d2a85f, #bc8945)"
          shape="circle"
          custom-style="height: 48px; color: #111318; font-weight: 800; flex: 1;"
          @click="executePayment"
        />
      </view>
      <view v-else class="retry-bar">
        <uv-button
          text="重试支付"
          color="linear-gradient(135deg, #d2a85f, #bc8945)"
          shape="circle"
          custom-style="height: 48px; color: #111318; font-weight: 800; flex: 1;"
          @click="retryConfirm"
        />
        <uv-button
          text="返回订单"
          plain
          shape="circle"
          custom-style="height: 48px; color: #8d929d; border-color: rgba(255,255,255,0.14); flex: 1;"
          @click="back"
        />
      </view>
    </view>
  </view>
</template>

<style scoped>
.payment-body {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 24px 16px;
}
.pay-amount {
  font-size: 40px;
  font-weight: 800;
  color: var(--xunye-gold);
  margin-bottom: 4px;
}
.panel.wide {
  width: 100%;
  margin: 12px 0;
  box-sizing: border-box;
  padding: 16px;
  background: rgba(21, 23, 27, 0.94);
  border: 1px solid var(--xunye-line);
  border-radius: 18px;
}
.section-title {
  font-size: 15px;
  font-weight: 800;
  margin-bottom: 10px;
  display: flex;
  align-items: center;
  gap: 6px;
}
.pay-method {
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 16px;
  margin-bottom: 10px;
  box-sizing: border-box;
  background: rgba(21, 23, 27, 0.94);
  border: 1px solid var(--xunye-line);
  border-radius: 16px;
}
.pay-method.active {
  border-color: rgba(210, 168, 95, 0.42);
  background: rgba(210, 168, 95, 0.08);
}
.pay-method.muted-method {
  opacity: 0.5;
}
.pay-title {
  display: flex;
  align-items: center;
  gap: 6px;
  font-weight: 700;
}
.info-line {
  display: flex;
  justify-content: space-between;
  padding: 8px 0;
  font-size: 14px;
}
.total-line {
  border-top: 1px solid var(--xunye-line);
  margin-top: 4px;
  padding-top: 12px;
  font-weight: 800;
  font-size: 15px;
}
.coupon-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  margin-bottom: 8px;
  background: rgba(255, 255, 255, 0.04);
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: 12px;
}
.coupon-item.active {
  border-color: rgba(210, 168, 95, 0.5);
  background: rgba(210, 168, 95, 0.08);
}
.coupon-left {
  flex-shrink: 0;
  width: 72px;
  text-align: center;
  border-right: 1px dashed rgba(255, 255, 255, 0.12);
  padding-right: 12px;
}
.coupon-amount {
  font-size: 22px;
  font-weight: 800;
  color: var(--xunye-gold);
  line-height: 1.2;
}
.coupon-condition {
  font-size: 11px;
  color: var(--xunye-muted);
  margin-top: 2px;
}
.coupon-right {
  flex: 1;
  min-width: 0;
}
.coupon-title {
  font-size: 14px;
  font-weight: 700;
  margin-bottom: 2px;
}
.coupon-rule {
  font-size: 12px;
  color: var(--xunye-muted);
  margin-bottom: 2px;
}
.coupon-expire {
  font-size: 11px;
  color: rgba(247, 241, 232, 0.35);
}
.coupon-check {
  flex-shrink: 0;
  width: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
}
.check-circle {
  width: 20px;
  height: 20px;
  border-radius: 999px;
  border: 2px solid rgba(255, 255, 255, 0.2);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 800;
  color: transparent;
}
.check-circle.checked {
  border-color: var(--xunye-gold);
  background: var(--xunye-gold);
  color: #111318;
}
.no-coupon {
  text-align: center;
  padding: 16px 0;
}
.bottom-spacer {
  height: 100px;
  flex-shrink: 0;
}
.pay-bar {
  display: flex;
  align-items: center;
  gap: 10px;
  width: 100%;
}
.detail-button {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 0;
  flex-shrink: 0;
  width: 52px;
  height: 48px;
  color: var(--xunye-muted);
  font-size: 11px;
  font-weight: 700;
  background: rgba(255, 255, 255, 0.06);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 14px;
  line-height: 1.1;
  padding: 0;
}
.detail-button .arrow {
  font-size: 8px;
  color: var(--xunye-gold);
}
.retry-bar {
  display: flex;
  gap: 10px;
  width: 100%;
}
.gold {
  color: var(--xunye-gold);
}
</style>
