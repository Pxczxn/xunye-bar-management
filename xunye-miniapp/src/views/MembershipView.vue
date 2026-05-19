<script lang="ts" setup>
import { ref } from 'vue'
import { useShellState } from '@/composables/useShellState'

const { back } = useShellState()

const currentMemberLevel = ref('普通会员')
const currentMemberLevelRaw = ref('REGULAR')
const memberPoints = ref(128)
const memberBalance = ref(0)

const memberLevels = ref([
  { level: 'REGULAR', name: '普通会员', minAmount: 0, discount: 100, pointsRate: 100, description: '新注册默认会员等级' },
  { level: 'VIP', name: 'VIP会员', minAmount: 1000, discount: 95, pointsRate: 150, description: '累计消费满1000元自动升级' },
  { level: 'SVIP', name: 'SVIP会员', minAmount: 5000, discount: 90, pointsRate: 200, description: '累计消费满5000元自动升级' },
])

const activeActivities = ref([
  { id: 1, title: '周二特惠日', description: '每周二所有鸡尾酒享8折优惠', type: 'DISCOUNT' },
  { id: 2, title: '新客专享', description: '首次消费满100减20', type: 'COUPON' },
  { id: 3, title: '积分翻倍', description: '周末消费积分双倍送', type: 'POINTS' },
])
</script>

<template>
  <view class="view">
    <view class="topbar">
      <button class="icon-button" hover-class="none" @tap="back">
        <uv-icon name="arrow-left" color="#f7f1e8" size="20" />
      </button>
      <view class="top-title">会员权益</view>
    </view>
    <scroll-view scroll-y class="scroll-view" enhanced show-scrollbar="false">
      <view class="scroll-inner">
      <view class="member-card">
        <view class="member-avatar">
          <uv-icon name="account-fill" color="#d2a85f" size="26" />
        </view>
        <view class="member-info">
          <text class="member-name">寻野会员</text>
          <text class="member-level-badge">{{ currentMemberLevel }}</text>
        </view>
        <view class="member-points">
          <text class="points-num">{{ memberPoints }}</text>
          <text class="points-label">积分</text>
        </view>
      </view>

      <view class="section-wrap">
        <text class="section-title">等级权益</text>
        <view v-for="level in memberLevels" :key="level.level" class="level-card" :class="{ 'level-active': level.level === currentMemberLevelRaw }">
          <view class="level-header">
            <text class="level-name">{{ level.name }}</text>
            <text v-if="level.level !== 'SVIP'" class="level-requirement">累计消费 ¥{{ level.minAmount }}</text>
            <text v-else class="level-requirement">最高等级</text>
          </view>
          <view class="level-benefits">
            <view class="benefit-item">
              <text class="benefit-icon">%</text>
              <text class="benefit-text">{{ 100 - level.discount }}% 折扣</text>
            </view>
            <view class="benefit-item">
              <text class="benefit-icon">×</text>
              <text class="benefit-text">{{ level.pointsRate / 100 }}x 积分倍率</text>
            </view>
          </view>
        </view>
      </view>

      <view class="section-wrap">
        <view class="balance-row">
          <view class="balance-item">
            <text class="balance-num gold">{{ memberPoints }}</text>
            <text class="balance-label">可用积分</text>
          </view>
          <view class="balance-divider" />
          <view class="balance-item">
            <text class="balance-num gold">{{ memberBalance }}</text>
            <text class="balance-label">账户余额(¥)</text>
          </view>
        </view>
      </view>

      <view class="section-wrap">
        <text class="section-title">进行中的活动</text>
        <view v-if="activeActivities.length === 0" class="empty-hint"><text>暂无活动</text></view>
        <view v-for="act in activeActivities" :key="act.id" class="activity-card">
          <text class="activity-title">{{ act.title }}</text>
          <text class="activity-desc">{{ act.description }}</text>
        </view>
      </view>

      <view style="height: 20px" />
      </view>
    </scroll-view>
  </view>
</template>

<style scoped>
.topbar {
  display: flex;
  align-items: center;
  padding: calc(var(--xunye-safe-top, 44px) + 5px) 12px 8px;
}
.scroll-view {
  flex: 1;
  width: 100%;
  min-width: 0;
  overflow: hidden;
}
.scroll-inner {
  box-sizing: border-box;
  width: 100%;
  padding: 0 16px;
}
.member-card {
  box-sizing: border-box;
  width: 100%;
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 20px;
  background: rgba(21, 23, 27, 0.94);
  border: 1px solid var(--xunye-line);
  border-radius: 18px;
  margin-bottom: 20px;
}
.member-avatar {
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(210, 168, 95, 0.14);
  border-radius: 999px;
  font-size: 24px;
}
.member-info { flex: 1; }
.member-name { font-size: 18px; font-weight: 700; display: block; }
.member-level-badge {
  display: inline-block;
  margin-top: 4px;
  padding: 2px 10px;
  background: rgba(210, 168, 95, 0.14);
  border-radius: 999px;
  font-size: 11px;
  color: var(--xunye-gold);
}
.member-points { text-align: center; }
.points-num { font-size: 24px; font-weight: 800; color: var(--xunye-gold); display: block; }
.points-label { font-size: 11px; color: var(--xunye-muted); }

.section-wrap { box-sizing: border-box; width: 100%; margin-bottom: 20px; }
.section-title { font-size: 16px; font-weight: 700; display: block; margin-bottom: 10px; }

.level-card {
  box-sizing: border-box;
  width: 100%;
  padding: 14px;
  margin-bottom: 8px;
  background: rgba(21, 23, 27, 0.94);
  border-radius: 16px;
  border: 1px solid var(--xunye-line);
}
.level-card.level-active { border-color: var(--xunye-gold); background: rgba(210, 168, 95, 0.08); }
.level-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px; }
.level-name { font-size: 15px; font-weight: 600; }
.level-requirement { font-size: 12px; color: var(--xunye-muted); }
.level-benefits { display: flex; gap: 16px; }
.benefit-item { display: flex; align-items: center; gap: 4px; }
.benefit-icon {
  width: 20px; height: 20px;
  display: flex; align-items: center; justify-content: center;
  background: rgba(210, 168, 95, 0.14); border-radius: 999px;
  font-size: 11px; color: var(--xunye-gold);
}
.benefit-text { font-size: 12px; color: #ccc; }

.balance-row { box-sizing: border-box; width: 100%; display: flex; background: rgba(21, 23, 27, 0.94); border: 1px solid var(--xunye-line); border-radius: 16px; padding: 16px; }
.balance-item { flex: 1; text-align: center; }
.balance-num { font-size: 22px; font-weight: 700; display: block; }
.balance-label { font-size: 11px; color: var(--xunye-muted); }
.balance-divider { width: 1px; background: var(--xunye-line); margin: 0 12px; }

.activity-card {
  box-sizing: border-box;
  width: 100%;
  padding: 12px;
  margin-bottom: 8px;
  background: rgba(21, 23, 27, 0.94);
  border: 1px solid var(--xunye-line);
  border-left: 3px solid var(--xunye-gold);
  border-radius: 14px;
}
.activity-title { font-size: 14px; font-weight: 600; display: block; margin-bottom: 4px; }
.activity-desc { font-size: 12px; color: var(--xunye-muted); display: block; }

.empty-hint { padding: 20px 0; text-align: center; color: #555; font-size: 14px; }
.gold { color: var(--xunye-gold); }
</style>
