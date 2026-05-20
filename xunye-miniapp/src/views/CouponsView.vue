<script lang="ts" setup>
import type { CustomerCouponVO } from '@/api/customer'
import { computed, onMounted, ref } from 'vue'
import { listCustomerCoupons } from '@/api/customer'
import { useShellState } from '@/composables/useShellState'
import { useCustomerProfileStore, useXunyeStore } from '@/store'

const { back, goPage, showToast } = useShellState()
const store = useXunyeStore()
const customerProfileStore = useCustomerProfileStore()

const activeTab = ref(0)
const couponTabs = [{ name: '可用' }, { name: '已用' }]
const coupons = ref<CustomerCouponVO[]>([])
const loading = ref(false)

const filteredCoupons = computed(() => coupons.value.filter(item => item.used === (activeTab.value === 1)))

onMounted(() => {
  fetchCoupons()
})

async function fetchCoupons() {
  loading.value = true
  try {
    coupons.value = await listCustomerCoupons(customerProfileStore.profile.phone)
  }
  catch {
    showToast('优惠券读取失败')
  }
  finally {
    loading.value = false
  }
}

function useCoupon(id: number) {
  const coupon = coupons.value.find(item => item.id === id)
  if (!coupon || coupon.used)
    return
  store.applyCoupon({
    id: coupon.id,
    title: coupon.title,
    rule: coupon.rule,
    discountAmount: Number(coupon.discountAmount),
  })
  showToast('已自动使用优惠券')
  goPage('menu')
}

function changeTab(item: { index: number }) {
  activeTab.value = item.index
}
</script>

<template>
  <view class="view">
    <view class="topbar">
      <button class="icon-button" hover-class="none" @tap="back">
        ‹
      </button>
      <view class="top-title">
        优惠券
      </view>
      <button class="icon-button ghost" hover-class="none">
        ‹
      </button>
    </view>
    <view class="coupon-tabs">
      <uv-tabs
        :list="couponTabs"
        :current="activeTab"
        :scrollable="false"
        line-color="#d2a85f"
        :active-style="{ color: '#f7f1e8', fontWeight: 800 }"
        :inactive-style="{ color: '#8d929d' }"
        :item-style="{ height: '40px' }"
        @change="changeTab"
      />
    </view>
    <scroll-view scroll-y class="content view-scroll" enhanced show-scrollbar="false">
      <view v-if="loading" class="coupon-card">
        <view class="muted small">
          正在读取优惠券...
        </view>
      </view>
      <view v-for="coupon in filteredCoupons" v-else :key="coupon.id" class="coupon-card" :class="{ disabled: coupon.used, active: store.activeCoupon?.id === coupon.id }">
        <view class="coupon-main">
          <view class="coupon-title">
            {{ coupon.title }}
          </view>
          <view class="muted small">
            {{ coupon.rule }}
          </view>
          <view class="muted mini">
            有效期至 {{ coupon.validUntil }}
          </view>
        </view>
        <button v-if="!coupon.used" class="coupon-action" hover-class="none" @tap="useCoupon(coupon.id)">
          {{ store.activeCoupon?.id === coupon.id ? '已选' : '去用' }}
        </button>
        <text v-else class="muted small">已使用</text>
      </view>
    </scroll-view>
  </view>
</template>

<style scoped>
.coupon-tabs {
  margin: 0 16px 12px;
  padding: 0 16px 12px;
  overflow: hidden;
  background: rgba(21, 23, 27, 0.94);
  border: 1px solid var(--xunye-line);
  border-radius: 16px;
}
.coupon-card {
  display: flex;
  align-items: center;
  gap: 12px;
  box-sizing: border-box;
  width: 100%;
  padding: 14px;
  margin-bottom: 10px;
  background: rgba(21, 23, 27, 0.94);
  border: 1px solid var(--xunye-line);
  border-radius: 16px;
}
.coupon-card.disabled {
  opacity: 0.48;
}
.coupon-card.active {
  border-color: var(--xunye-gold);
}
.coupon-main {
  flex: 1;
  min-width: 0;
}
.coupon-title {
  margin-bottom: 4px;
  color: var(--xunye-gold);
  font-size: 18px;
  font-weight: 800;
}
.coupon-action {
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  width: 64px;
  height: 34px;
  color: #090909;
  font-size: 13px;
  font-weight: 700;
  background: var(--xunye-gold);
  border-radius: 999px;
}
</style>
