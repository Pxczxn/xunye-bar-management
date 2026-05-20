<script lang="ts" setup>
import { computed } from 'vue'
import { useShellState } from '@/composables/useShellState'
import { useXunyeStore } from '@/store'

const store = useXunyeStore()
const { goPage, replace } = useShellState()

const displayOrder = computed(() => store.lastOrder || {
  orderNo: '',
  totalAmount: store.totalAmount,
  table: store.currentTable || { area: '大厅', code: 'A08' },
})
</script>

<template>
  <view class="view result-view">
    <view class="success-circle">
      <uv-icon name="checkmark" color="#111318" size="30" />
    </view>
    <view class="result-title">
      支付成功
    </view>
    <view class="muted result-copy">
      吧台已收到您的订单<br>正在为您精心制作中
    </view>
    <view class="panel wide">
      <view class="info-line">
        <text>支付金额</text><text class="gold">¥{{ displayOrder.totalAmount.toFixed(2) }}</text>
      </view>
      <view class="info-line">
        <text>桌台</text><text>{{ displayOrder.table?.code }}</text>
      </view>
    </view>
    <view class="result-actions">
      <uv-button
        text="返回首页"
        plain
        shape="circle"
        custom-style="flex: 1; height: 44px; background: transparent; border-color: rgba(255,255,255,0.14); color: #8d929d;"
        @click="goPage('index')"
      />
      <uv-button
        text="查看订单"
        color="linear-gradient(135deg, #d2a85f, #bc8945)"
        shape="circle"
        custom-style="flex: 1; height: 44px; color: #111318; font-weight: 800;"
        @click="replace('orderDetail')"
      />
    </view>
  </view>
</template>

<style scoped>
.result-view {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 24px;
}
.success-circle {
  width: 64px;
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #d2a85f;
  border-radius: 999px;
  font-size: 28px;
  margin-bottom: 16px;
}
.result-title {
  font-size: 22px;
  font-weight: 700;
  margin-bottom: 6px;
}
.result-copy {
  text-align: center;
  line-height: 1.6;
  margin-bottom: 24px;
}
.panel.wide {
  width: 100%;
  margin-bottom: 32px;
}
.result-actions {
  display: flex;
  gap: 12px;
  width: 100%;
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
