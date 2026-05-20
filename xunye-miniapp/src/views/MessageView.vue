<script lang="ts" setup>
function formatTodayTime(hour: number, minute = 0) {
  const now = new Date()
  const month = String(now.getMonth() + 1).padStart(2, '0')
  const day = String(now.getDate()).padStart(2, '0')
  const hh = String(hour).padStart(2, '0')
  const mm = String(minute).padStart(2, '0')
  return `${month}-${day} ${hh}:${mm}`
}

const orderMessageTime = formatTodayTime(22, 30)
const activityMessageTime = formatTodayTime(21)
</script>

<template>
  <view class="view">
    <view class="content">
      <view class="message-card">
        <view class="message-row">
          <text class="message-tag">订单通知</text>
          <text class="muted mini">{{ orderMessageTime }}</text>
        </view>
        <view class="bold">您的订单已制作完成</view>
        <view class="muted small">A08 桌台的饮品已完成，请留意服务员送达。</view>
      </view>
      <view class="message-card">
        <view class="message-row">
          <text class="message-tag">活动推送</text>
          <text class="muted mini">{{ activityMessageTime }}</text>
        </view>
        <view class="bold">爵士现场即将开始</view>
        <view class="muted small">今晚特调买二送一，适合和朋友一起微醺。</view>
      </view>
    </view>
  </view>
</template>

<style scoped>
.content { padding-top: calc(var(--xunye-safe-top, 44px) + 18px); }
.message-card {
  padding: 14px 16px;
  margin-bottom: 10px;
  background: rgba(21, 23, 27, 0.94);
  border: 1px solid var(--xunye-line);
  border-radius: 16px;
}
.message-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 4px;
}
.message-tag {
  padding: 2px 8px;
  background: rgba(210, 168, 95, 0.14);
  border-radius: 4px;
  font-size: 11px;
  color: var(--xunye-gold);
}
</style>
