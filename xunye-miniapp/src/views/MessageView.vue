<script lang="ts" setup>
import type { CustomerMessageVO } from '@/api/customer'
import { onMounted, ref, watch } from 'vue'
import { listCustomerMessages } from '@/api/customer'
import { useShellState } from '@/composables/useShellState'
import { useCustomerProfileStore } from '@/store/customerProfile'

const customerProfileStore = useCustomerProfileStore()
const { activeView } = useShellState()

// 切换到该 tab 时自动刷新消息，30 秒冷却
let lastTabFetchTime = 0
watch(activeView, (val) => {
  if (val === 'message') {
    const now = Date.now()
    if (now - lastTabFetchTime > 30000) {
      lastTabFetchTime = now
      fetchMessages()
    }
  }
})
const messages = ref<CustomerMessageVO[]>([])
const loading = ref(false)

const typeLabels: Record<string, string> = {
  ORDER: '订单通知',
  SYSTEM: '系统消息',
  PROMOTION: '活动推送',
}

function formatTime(createdAt: string) {
  if (!createdAt)
    return ''
  const d = new Date(createdAt.replace('T', ' ').replace(/-/g, '/'))
  const month = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  const hh = String(d.getHours()).padStart(2, '0')
  const mm = String(d.getMinutes()).padStart(2, '0')
  return `${month}-${day} ${hh}:${mm}`
}

onMounted(() => {
  lastTabFetchTime = Date.now()
  fetchMessages()
})

async function fetchMessages() {
  const phone = customerProfileStore.profile.phone
  if (!phone)
    return
  loading.value = true
  try {
    messages.value = await listCustomerMessages(phone)
  }
  finally {
    loading.value = false
  }
}
</script>

<template>
  <view class="view">
    <scroll-view class="content" scroll-y enhanced show-scrollbar="false" refresher-enabled :refresher-triggered="loading" @refresherrefresh="fetchMessages">
      <view v-if="loading && !messages.length" class="empty-state">
        <uv-loading-icon mode="circle" color="#d2a85f" text="加载中" text-color="#8d929d" />
      </view>
      <view v-else-if="!messages.length" class="empty-state">
        <uv-empty mode="message" text="暂无消息" icon-color="#2b2f38" text-color="#8d929d" />
      </view>
      <view
        v-for="msg in messages" :key="msg.id"
        class="message-card" :class="{ unread: !msg.isRead }"
      >
        <view class="message-row">
          <text class="message-tag" :class="msg.type?.toLowerCase()">
            {{ typeLabels[msg.type] || msg.type || '通知' }}
          </text>
          <text class="muted mini">{{ formatTime(msg.createdAt) }}</text>
        </view>
        <view class="bold">
          {{ msg.title }}
        </view>
        <view class="muted small">
          {{ msg.content }}
        </view>
      </view>
    </scroll-view>
  </view>
</template>

<style scoped>
.content {
  flex: 1;
  padding: calc(var(--xunye-safe-top, 44px) + 18px) 16px 110px;
}
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 60px 0;
}
.message-card {
  padding: 14px 16px;
  margin-bottom: 10px;
  background: rgba(21, 23, 27, 0.94);
  border: 1px solid var(--xunye-line);
  border-radius: 16px;
}
.message-card.unread {
  border-color: rgba(210, 168, 95, 0.25);
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
.message-tag.order {
  background: rgba(76, 175, 80, 0.14);
  color: #4caf50;
}
.message-tag.system {
  background: rgba(33, 150, 243, 0.14);
  color: #2196f3;
}
</style>
