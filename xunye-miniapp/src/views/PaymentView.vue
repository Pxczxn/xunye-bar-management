<script lang="ts" setup>
import { computed } from 'vue'
import { useXunyeStore } from '@/store'
import { useShellState } from '@/composables/useShellState'

const store = useXunyeStore()
const { back, replace, showToast } = useShellState()

const displayOrder = computed(() => store.lastOrder || {
  orderNo: 'XN202605160021',
  createdAt: '2026-05-16 21:30:15',
  table: store.currentTable || { area: '大厅', code: 'A08' },
  items: store.cartItems,
  originalAmount: store.totalAmount || 161,
  discountAmount: store.discountAmount || 0,
  totalAmount: store.payableAmount || 161,
  coupon: store.activeCoupon,
  remark: '',
  status: '制作中',
})

function executePayment() {
  showToast('正在调用微信支付...')
  setTimeout(() => {
    store.completePayment()
    replace('orderResult')
  }, 900)
}
</script>

<template>
  <view class="view">
    <view class="topbar">
      <button class="icon-button" hover-class="none" @tap="back">
        <uv-icon name="arrow-left" color="#f7f1e8" size="20" />
      </button>
      <view class="top-title">支付订单</view>
      <view class="icon-button ghost" />
    </view>
    <view class="payment-body">
      <view class="pay-amount">¥{{ displayOrder.totalAmount.toFixed(2) }}</view>
      <view class="muted">请确认订单并选择支付方式</view>
      <view class="panel wide">
        <view class="info-line"><text>订单编号</text><text>{{ displayOrder.orderNo }}</text></view>
        <view class="info-line"><text>桌台</text><text>{{ displayOrder.table?.code }}</text></view>
        <view class="info-line"><text>商品原价</text><text>¥{{ displayOrder.originalAmount.toFixed(2) }}</text></view>
        <view v-if="displayOrder.discountAmount" class="info-line"><text>{{ displayOrder.coupon?.title || '优惠券' }}</text><text class="gold">-¥{{ displayOrder.discountAmount.toFixed(2) }}</text></view>
        <view class="info-line"><text>支付金额</text><text class="gold">¥{{ displayOrder.totalAmount.toFixed(2) }}</text></view>
      </view>
      <view class="pay-method active">
        <view>
          <view class="pay-title"><uv-icon name="weixin-fill" color="#27c160" size="20" /> 微信支付</view>
          <view class="muted small">推荐使用</view>
        </view>
        <uv-radio-group model-value="wechat" active-color="#d2a85f">
          <uv-radio name="wechat" :label-disabled="true" />
        </uv-radio-group>
      </view>
      <view class="pay-method muted-method">
        <view>
          <view class="pay-title"><uv-icon name="bag" color="#8d929d" size="20" /> 到店支付</view>
          <view class="muted small">暂不可用</view>
        </view>
        <uv-radio-group model-value="" disabled>
          <uv-radio name="offline" :label-disabled="true" />
        </uv-radio-group>
      </view>
    </view>
    <view class="bottom-pay">
      <uv-button
        text="确认支付"
        color="linear-gradient(135deg, #d2a85f, #bc8945)"
        shape="circle"
        custom-style="height: 48px; color: #111318; font-weight: 800;"
        @click="executePayment"
      />
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
  margin: 20px 0;
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
.gold {
  color: var(--xunye-gold);
}
</style>
