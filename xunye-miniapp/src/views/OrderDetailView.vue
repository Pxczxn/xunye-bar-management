<script lang="ts" setup>
import type { OrderPageVO } from '@/api/customer'
import { computed, onMounted, ref } from 'vue'
import { getCustomerOrderDetail } from '@/api/customer'
import { useShellState } from '@/composables/useShellState'
import { useXunyeStore } from '@/store'

const store = useXunyeStore()
const { back, showToast } = useShellState()
const remoteOrder = ref<OrderPageVO | null>(null)
const loading = ref(false)

const displayOrder = computed(() => store.lastOrder || {
  orderNo: '',
  createdAt: '',
  table: store.currentTable || { area: '大厅', code: 'A08' },
  items: store.cartItems,
  totalAmount: store.totalAmount,
  remark: '',
  status: '制作中',
})

const displayRemoteOrder = computed(() => remoteOrder.value
  ? {
      orderNo: remoteOrder.value.orderNo,
      createdAt: remoteOrder.value.createdAt?.replace('T', ' ') || '',
      table: { area: '', code: remoteOrder.value.tableName },
      items: remoteOrder.value.items.map(item => ({
        id: item.productId,
        name: item.productName,
        qty: item.quantity,
        price: Number(item.price || 0),
      })),
      totalAmount: Number(remoteOrder.value.totalAmount || 0),
      remark: remoteOrder.value.remark || '',
      status: remoteOrder.value.status === 'PAID' ? '已支付' : remoteOrder.value.status === 'UNPAID' ? '待支付' : remoteOrder.value.status,
      paymentMethod: remoteOrder.value.paymentMethod || '未支付',
    }
  : displayOrder.value)

onMounted(() => {
  fetchDetail()
})

async function fetchDetail() {
  const orderNo = store.selectedOrderNo || store.lastOrder?.orderNo
  if (!orderNo)
    return
  loading.value = true
  try {
    remoteOrder.value = await getCustomerOrderDetail(orderNo)
  }
  catch {
    showToast('订单详情读取失败')
  }
  finally {
    loading.value = false
  }
}
</script>

<template>
  <view class="view">
    <view class="topbar">
      <button class="icon-button" hover-class="none" @tap="back">
        <uv-icon name="arrow-left" color="#f7f1e8" size="20" />
      </button>
      <view class="top-title">
        订单详情
      </view>
      <view class="icon-button ghost" />
    </view>
    <scroll-view class="content" scroll-y enhanced show-scrollbar="false">
      <view v-if="loading" class="status-banner">
        <view class="muted small">
          正在读取订单详情...
        </view>
      </view>
      <view class="status-banner">
        <view>
          <view class="bold">
            {{ displayRemoteOrder.status }}
          </view>
          <view class="muted small">
            吧台正在处理您的订单
          </view>
        </view>
        <uv-icon name="order" color="#d2a85f" size="34" />
      </view>
      <view class="panel">
        <view class="order-panel-head">
          <text class="bold">商品明细</text>
          <text class="table-tag">{{ displayRemoteOrder.table?.code }}</text>
        </view>
        <view v-for="item in displayRemoteOrder.items" :key="item.id" class="detail-line">
          <text>{{ item.name }} <text class="muted">x{{ item.qty }}</text></text>
          <text>¥{{ (item.price * item.qty).toFixed(2) }}</text>
        </view>
        <view class="detail-total">
          <text>合计</text>
          <text class="gold">¥{{ displayRemoteOrder.totalAmount.toFixed(2) }}</text>
        </view>
      </view>
      <view class="panel info-panel">
        <view class="panel-title">
          订单信息
        </view>
        <view class="info-line">
          <text>订单编号</text><text>{{ displayRemoteOrder.orderNo }}</text>
        </view>
        <view class="info-line">
          <text>下单时间</text><text>{{ displayRemoteOrder.createdAt }}</text>
        </view>
        <view class="info-line">
          <text>支付方式</text><text>{{ displayRemoteOrder.paymentMethod }}</text>
        </view>
        <view class="info-line">
          <text>订单备注</text><text>{{ displayRemoteOrder.remark || '无' }}</text>
        </view>
      </view>
    </scroll-view>
  </view>
</template>

<style scoped>
.status-banner {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px;
  background: rgba(21, 23, 27, 0.94);
  border: 1px solid var(--xunye-line);
  border-radius: 18px;
  margin-bottom: 16px;
}
.status-icon {
  font-size: 36px;
}
.order-panel-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}
.table-tag {
  padding: 2px 10px;
  background: rgba(210, 168, 95, 0.14);
  border-radius: 999px;
  font-size: 12px;
  color: var(--xunye-gold);
}
.detail-line {
  display: flex;
  justify-content: space-between;
  padding: 6px 0;
  font-size: 14px;
}
.detail-total {
  display: flex;
  justify-content: space-between;
  padding: 10px 0 0;
  margin-top: 6px;
  border-top: 1px solid var(--xunye-line);
  font-weight: 700;
  font-size: 15px;
}
.info-panel {
  margin-top: 12px;
}
.info-line {
  display: flex;
  justify-content: space-between;
  padding: 8px 0;
  font-size: 13px;
}
.gold {
  color: var(--xunye-gold);
}
</style>
