<script lang="ts" setup>
import type { CustomerTableVO } from '@/api/customer'
import { onMounted, ref } from 'vue'
import { getCustomerTableByCode, listCustomerTables } from '@/api/customer'
import { useShellState } from '@/composables/useShellState'
import { useXunyeStore } from '@/store'

const store = useXunyeStore()
const { back, push, goPage, showToast } = useShellState()
const tableInput = ref('')
const tables = ref<CustomerTableVO[]>([])

onMounted(() => {
  fetchTables()
})

async function fetchTables() {
  try {
    tables.value = await listCustomerTables()
  }
  catch {
    showToast('桌台读取失败')
  }
}

function useTable(table: CustomerTableVO) {
  const code = table.tableCode || table.name
  store.selectTable({ id: table.id, area: table.areaName || '大厅', code })
  showToast(`已选择桌台: ${code}`)
  setTimeout(() => goPage('menu'), 450)
}

async function confirmTable() {
  const code = tableInput.value.trim().toUpperCase()
  if (!code) {
    showToast('请输入桌号')
    return
  }
  try {
    const table = await getCustomerTableByCode(code)
    useTable(table)
  }
  catch {
    showToast('桌台不存在或已停用')
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
        选择桌台
      </view>
      <view class="icon-button ghost" />
    </view>
    <view class="content">
      <view class="scan-card">
        <view class="scan-icon">
          <uv-icon name="scan" color="#d2a85f" size="34" />
        </view>
        <view class="bold">
          扫码选桌
        </view>
        <view class="muted small">
          扫描桌面二维码快速点单
        </view>
      </view>
      <view class="sub-title">
        手动输入桌号
      </view>
      <view class="table-input-row">
        <input v-model="tableInput" class="table-input" placeholder="例如: A08" placeholder-class="placeholder">
        <uv-button
          text="确认"
          color="linear-gradient(135deg, #d2a85f, #bc8945)"
          custom-style="width: 96px; height: 52px; color: #111318; font-weight: 800; border-radius: 14px;"
          @click="confirmTable"
        />
      </view>
      <view class="sub-title">
        可用区域展示
      </view>
      <view class="table-grid">
        <view
          v-for="table in tables"
          :key="table.id"
          class="table-cell"
          :class="{ active: store.currentTable?.id === table.id, disabled: table.status !== 'EMPTY' }"
          @tap="table.status === 'EMPTY' && useTable(table)"
        >
          <text v-if="store.currentTable?.id === table.id" class="recent">已选</text>
          <text class="muted mini">{{ table.areaName || '大厅' }}</text>
          <text class="table-no" :class="{ gold: store.currentTable?.id === table.id }">{{ table.tableCode || table.name }}</text>
          <text class="muted mini">{{ table.status === 'EMPTY' ? '可用' : table.status === 'USING' ? '使用中' : '清洁中' }}</text>
        </view>
      </view>
    </view>
  </view>
</template>

<style scoped>
.scan-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  min-height: 178px;
  padding: 24px 16px;
  margin-bottom: 18px;
  background: rgba(21, 23, 27, 0.94);
  border: 1px dashed rgba(210, 168, 95, 0.3);
  border-radius: 18px;
}
.scan-icon {
  width: 68px;
  height: 68px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(210, 168, 95, 0.14);
  border-radius: 999px;
  font-size: 28px;
}
.sub-title {
  margin: 18px 0 10px;
  font-size: 14px;
  font-weight: 700;
  color: var(--xunye-muted);
}
.table-input-row {
  display: flex;
  align-items: center;
  gap: 12px;
}
.table-input {
  box-sizing: border-box;
  flex: 1;
  height: 52px;
  padding: 0 16px;
  background: rgba(21, 23, 27, 0.94);
  border: 1px solid var(--xunye-line);
  border-radius: 14px;
  color: var(--xunye-text);
  font-size: 16px;
}
.table-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}
.table-cell {
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 6px;
  min-height: 92px;
  padding: 14px 0;
  background: rgba(21, 23, 27, 0.94);
  border-radius: 16px;
  border: 1px solid var(--xunye-line);
}
.table-cell.active {
  border-color: var(--xunye-gold);
  background: rgba(210, 168, 95, 0.08);
}
.table-cell.disabled {
  opacity: 0.48;
}
.table-no {
  font-size: 24px;
  font-weight: 800;
}
.recent {
  position: absolute;
  top: -10px;
  right: -8px;
  padding: 4px 10px;
  background: #eb5757;
  border-radius: 999px;
  font-size: 10px;
  color: #fff;
  line-height: 1;
}
</style>
