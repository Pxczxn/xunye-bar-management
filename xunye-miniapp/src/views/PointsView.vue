<script lang="ts" setup>
import { ref } from 'vue'
import { useShellState } from '@/composables/useShellState'

const { back, showToast } = useShellState()

const points = ref(128)
const selectedReward = ref<number | null>(null)

const records = [
  { id: 1, title: '完成消费', time: '2026-05-16 21:30', amount: 16 },
  { id: 2, title: '周末双倍积分', time: '2026-05-10 22:14', amount: 18 },
  { id: 3, title: '兑换优惠券', time: '2026-05-02 20:05', amount: -50 },
]

const rewards = [
  { id: 1, title: '满 99 减 10', cost: 80, desc: '下次到店消费可用' },
  { id: 2, title: '小食抵扣券', cost: 120, desc: '任选小食立减 18' },
]

function exchangeReward(reward: typeof rewards[number]) {
  if (points.value < reward.cost) {
    showToast('积分还差一点')
    return
  }
  points.value -= reward.cost
  selectedReward.value = reward.id
  showToast('兑换成功')
}
</script>

<template>
  <view class="view">
    <view class="topbar">
      <button class="icon-button" hover-class="none" @tap="back">
        <uv-icon name="arrow-left" color="#f7f1e8" size="20" />
      </button>
      <view class="top-title">积分</view>
      <button class="icon-button ghost" hover-class="none">‹</button>
    </view>
    <scroll-view scroll-y class="content view-scroll" enhanced show-scrollbar="false">
      <view class="points-hero">
        <view class="muted small">当前可用</view>
        <view class="points-num">{{ points }}</view>
        <view class="muted small">消费 1 元得 1 积分，周末到店双倍</view>
      </view>

      <view class="panel">
        <view class="panel-title">积分兑换</view>
        <view v-for="reward in rewards" :key="reward.id" class="reward-row">
          <view>
            <view class="bold">{{ reward.title }}</view>
            <view class="muted small">{{ reward.desc }} · {{ reward.cost }} 积分</view>
          </view>
          <uv-button
            :text="selectedReward === reward.id ? '已兑' : '兑换'"
            color="linear-gradient(135deg, #d2a85f, #bc8945)"
            shape="circle"
            size="small"
            custom-style="height: 32px; padding: 0 14px; color: #111318; font-weight: 800;"
            @click="exchangeReward(reward)"
          />
        </view>
      </view>

      <view class="panel">
        <view class="panel-title">积分明细</view>
        <view v-for="item in records" :key="item.id" class="record-row">
          <view>
            <view>{{ item.title }}</view>
            <view class="muted mini">{{ item.time }}</view>
          </view>
          <text :class="item.amount > 0 ? 'gold' : 'muted'">{{ item.amount > 0 ? '+' : '' }}{{ item.amount }}</text>
        </view>
      </view>
    </scroll-view>
  </view>
</template>

<style scoped>
.points-hero {
  padding: 22px 16px;
  margin-bottom: 12px;
  background: rgba(21, 23, 27, 0.94);
  border: 1px solid var(--xunye-line);
  border-radius: 18px;
}
.points-num {
  margin: 6px 0;
  color: var(--xunye-gold);
  font-size: 40px;
  font-weight: 800;
}
.reward-row,
.record-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 12px 0;
  border-bottom: 1px solid var(--xunye-line);
}
.reward-row:last-child,
.record-row:last-child { border-bottom: 0; }
.small-button { flex-shrink: 0; }
</style>
