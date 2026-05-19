<script lang="ts" setup>
</script>

<template>
  <view class="view">
    <view class="simple-head">
      <text>消息</text>
    </view>
    <view class="content">
      <view class="message-card">
        <view class="message-row">
          <text class="message-tag">订单通知</text>
          <text class="muted mini">刚刚</text>
        </view>
        <view class="bold">您的订单已制作完成</view>
        <view class="muted small">A08 桌台的饮品已完成，请留意服务员送达。</view>
      </view>
      <view class="message-card">
        <view class="message-row">
          <text class="message-tag">活动推送</text>
          <text class="muted mini">21:00</text>
        </view>
        <view class="bold">爵士现场即将开始</view>
        <view class="muted small">今晚特调买二送一，适合和朋友一起微醺。</view>
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
