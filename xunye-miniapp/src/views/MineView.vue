<script lang="ts" setup>
import { useShellState } from '@/composables/useShellState'

const { goPage, push } = useShellState()
</script>

<template>
  <view class="view">
    <view class="simple-head">
      <text>我的</text>
    </view>
    <view class="content">
      <view class="profile-card">
        <image class="avatar" src="/static/images/avatar.jpg" mode="aspectFill" lazy-load />
        <view>
          <view class="bold profile-name">寻野会员</view>
          <view class="muted small">微醺等级 Lv.2</view>
        </view>
      </view>
      <view class="stats-card">
        <button class="stat-item" hover-class="none" @tap="push('points')">
          <view class="gold stat-num">128</view>
          <view class="muted mini">积分</view>
        </button>
        <view class="divider" />
        <button class="stat-item" hover-class="none" @tap="push('coupons')">
          <view class="gold stat-num">3</view>
          <view class="muted mini">优惠券</view>
        </button>
        <view class="divider" />
        <button class="stat-item" hover-class="none" @tap="push('membership')">
          <view class="gold stat-num">Lv.2</view>
          <view class="muted mini">微醺等级</view>
        </button>
      </view>
      <view class="mine-cell-panel">
        <uv-cell-group :border="false">
          <uv-cell
            title="历史订单"
            icon="order"
            is-link
            clickable
            :border="true"
            :title-style="{ color: '#fff', fontSize: '15px', fontWeight: 600 }"
            :icon-style="{ color: '#c79f62' }"
            :cell-style="{ background: '#161616', padding: '15px 0' }"
            @click="goPage('orders')"
          />
          <uv-cell
            title="会员权益"
            icon="integral"
            is-link
            clickable
            :border="true"
            :title-style="{ color: '#fff', fontSize: '15px', fontWeight: 600 }"
            :icon-style="{ color: '#c79f62' }"
            :cell-style="{ background: '#161616', padding: '15px 0' }"
            @click="push('membership')"
          />
          <uv-cell
            title="联系客服"
            icon="kefu-ermai"
            is-link
            clickable
            :border="false"
            :title-style="{ color: '#fff', fontSize: '15px', fontWeight: 600 }"
            :icon-style="{ color: '#c79f62' }"
            :cell-style="{ background: '#161616', padding: '15px 0' }"
            @click="push('contact')"
          />
        </uv-cell-group>
      </view>
    </view>
  </view>
</template>

<style scoped>
.simple-head {
  box-sizing: border-box;
  width: 100%;
  min-height: calc(var(--xunye-safe-top, 44px) + 45px);
  padding: calc(var(--xunye-safe-top, 44px) + 8px) calc(var(--xunye-menu-right, 0px) + var(--xunye-menu-width, 0px) + 12px) 12px 16px;
  font-size: 20px;
  font-weight: 700;
  text-align: center;
}
.profile-card {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 16px;
  background: #161616;
  border-radius: 16px;
  margin-bottom: 12px;
}
.avatar {
  width: 52px;
  height: 52px;
  border-radius: 999px;
  background: #222;
}
.profile-name { font-size: 18px; }
.stats-card {
  display: flex;
  align-items: center;
  justify-content: space-around;
  padding: 16px;
  background: #161616;
  border-radius: 14px;
  margin-bottom: 12px;
}
.stat-num { font-size: 22px; font-weight: 700; text-align: center; }
.stat-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-width: 0;
  background: transparent;
}
.divider { width: 1px; height: 36px; background: #222; }
.mine-cell-panel {
  box-sizing: border-box;
  padding: 0 16px;
  overflow: hidden;
  background: #161616;
  border-radius: 14px;
}
.gold { color: #c79f62; }
</style>
