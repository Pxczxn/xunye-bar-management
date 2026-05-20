<script lang="ts" setup>
import { ref } from 'vue'
import { createCustomerOrder } from '@/api/customer'
import { useShellState } from '@/composables/useShellState'
import { useXunyeStore } from '@/store'
import { useCustomerProfileStore } from '@/store/customerProfile'

const store = useXunyeStore()
const customerProfileStore = useCustomerProfileStore()
const { back, push, showToast } = useShellState()
const remark = ref('')
const submitting = ref(false)

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
      <view v-if="store.activeCoupon" class="panel">
        <view class="discount-line">
          <view>
            <view class="bold">
              {{ store.activeCoupon.title }}
            </view>
            <view class="muted small">
              {{ store.activeCoupon.rule }}
            </view>
          </view>
          <button class="plain-button gold" hover-class="none" @tap="store.removeCoupon">
            不用了
          </button>
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
  padding: 8px 12px;
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
</style>
