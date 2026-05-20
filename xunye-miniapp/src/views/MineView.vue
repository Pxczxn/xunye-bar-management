<script lang="ts" setup>
import { onMounted } from 'vue'
import { useShellState } from '@/composables/useShellState'
import { useCustomerProfileStore } from '@/store/customerProfile'

const { goPage, push } = useShellState()
const customerProfileStore = useCustomerProfileStore()
const profile = customerProfileStore.profile

onMounted(() => {
  customerProfileStore.cleanProfile()
  customerProfileStore.fetchProfile().catch(() => {})
})
</script>

<template>
  <view class="view">
    <view class="content">
      <view class="profile-card" @tap="() => push('profile')">
        <image class="avatar" :src="profile.avatar || '/static/images/avatar.jpg'" mode="aspectFill" lazy-load />
        <view class="profile-main">
          <view class="bold profile-name">
            {{ profile.nickname }}
          </view>
          <view class="muted small">
            {{ profile.levelText }}
          </view>
          <view class="muted mini">
            ID {{ profile.customerNo || '生成中' }}
          </view>
        </view>
        <uv-icon name="arrow-right" color="#8d929d" size="16" />
      </view>
      <view class="stats-card">
        <button class="stat-item" hover-class="none" @tap="() => push('points')">
          <view class="gold stat-num">
            {{ Number(profile.points || 0) }}
          </view>
          <view class="muted mini">
            积分
          </view>
        </button>
        <view class="divider" />
        <button class="stat-item" hover-class="none" @tap="() => push('coupons')">
          <view class="gold stat-num">
            3
          </view>
          <view class="muted mini">
            优惠券
          </view>
        </button>
        <view class="divider" />
        <button class="stat-item" hover-class="none" @tap="() => push('membership')">
          <view class="gold stat-num">
            Lv.2
          </view>
          <view class="muted mini">
            {{ profile.memberLevelName }}
          </view>
        </button>
      </view>
      <view class="mine-cell-panel">
        <button class="mine-row" hover-class="none" @tap="() => goPage('orders')">
          <view class="mine-row-title">
            <uv-icon name="order" color="#c79f62" size="16" />
            <text>历史订单</text>
          </view>
          <uv-icon name="arrow-right" color="#8d929d" size="15" />
        </button>
        <button class="mine-row" hover-class="none" @tap="() => push('membership')">
          <view class="mine-row-title">
            <uv-icon name="integral" color="#c79f62" size="16" />
            <text>会员权益</text>
          </view>
          <uv-icon name="arrow-right" color="#8d929d" size="15" />
        </button>
        <button class="mine-row last" hover-class="none" @tap="() => push('contact')">
          <view class="mine-row-title">
            <uv-icon name="kefu-ermai" color="#c79f62" size="16" />
            <text>联系客服</text>
          </view>
          <uv-icon name="arrow-right" color="#8d929d" size="15" />
        </button>
      </view>
    </view>
  </view>
</template>

<style scoped>
.content {
  padding-top: calc(var(--xunye-safe-top, 44px) + 18px);
}
.profile-card {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 16px;
  background: rgba(21, 23, 27, 0.94);
  border: 1px solid var(--xunye-line);
  border-radius: 18px;
  margin-bottom: 12px;
}
.avatar {
  width: 52px;
  height: 52px;
  border-radius: 999px;
  background: #222;
}
.profile-main {
  flex: 1;
  min-width: 0;
}
.profile-name {
  font-size: 18px;
}
.stats-card {
  display: flex;
  align-items: center;
  justify-content: space-around;
  padding: 16px;
  background: rgba(21, 23, 27, 0.94);
  border: 1px solid var(--xunye-line);
  border-radius: 18px;
  margin-bottom: 12px;
}
.stat-num {
  font-size: 22px;
  font-weight: 700;
  text-align: center;
}
.stat-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-width: 0;
  background: transparent;
}
.divider {
  width: 1px;
  height: 36px;
  background: var(--xunye-line);
}
.mine-cell-panel {
  box-sizing: border-box;
  padding: 0 16px;
  overflow: hidden;
  background: rgba(21, 23, 27, 0.94);
  border: 1px solid var(--xunye-line);
  border-radius: 18px;
}
.mine-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  box-sizing: border-box;
  width: 100%;
  min-height: 52px;
  padding: 0;
  color: #fff;
  background: transparent;
  border-bottom: 1px solid var(--xunye-line);
}
.mine-row.last {
  border-bottom: 0;
}
.mine-row-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 15px;
  font-weight: 700;
}
.gold {
  color: var(--xunye-gold);
}
</style>
