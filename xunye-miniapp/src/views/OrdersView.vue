<script lang="ts" setup>
import { ref, reactive, computed } from 'vue'
import { useShellState } from '@/composables/useShellState'

const { push } = useShellState()

interface OrderCard {
  id: number; no: string; date: string; time: string; table: string
  status: string; amount: number; count: number; month: string
  week: boolean; items: string[]
}

const orderCards = reactive<OrderCard[]>([
  { id: 1, no: 'XN202605160021', date: '2026-05-16', time: '21:30', table: 'A08', status: '已完成', amount: 161, count: 3, month: '2026-05', week: true, items: ['寻野特调迷雾', '日落大道', '黑松露薯条'] },
  { id: 2, no: 'XN202605100018', date: '2026-05-10', time: '22:14', table: 'V01', status: '已完成', amount: 176, count: 2, month: '2026-05', week: false, items: ['麦卡伦12年单杯', '长岛冰茶'] },
  { id: 3, no: 'XN202604280011', date: '2026-04-28', time: '20:46', table: 'B05', status: '已完成', amount: 98, count: 2, month: '2026-04', week: false, items: ['百威啤酒', '黑松露薯条'] },
])

const currentYear = ref(2026)
const currentMonth = 5
const selectedFilter = ref('all')
const selectedStatus = ref('all')
const selectedTable = ref('all')
const selectedItemType = ref('all')
const selectedProduct = ref('all')
const dateSheetOpen = ref(false)
const filterSheet = ref<'status' | 'table' | 'type' | 'product' | null>(null)

const months = computed(() => Array.from({ length: 12 }, (_, i) => {
  const value = `${currentYear.value}-${String(i + 1).padStart(2, '0')}`
  const count = orderCards.filter(o => o.month === value).length
  const isFuture = currentYear.value > 2026 || (currentYear.value === 2026 && i + 1 > currentMonth)
  return { value, label: `${i + 1}月`, count, isFuture }
}))

const productOptions = computed(() => {
  const drinks = ['寻野特调迷雾', '日落大道', '麦卡伦12年单杯', '长岛冰茶', '百威啤酒']
  const snacks = ['黑松露薯条']
  if (selectedItemType.value === 'drink') return drinks
  if (selectedItemType.value === 'snack') return snacks
  return [...drinks, ...snacks]
})

const filteredOrders = computed(() => {
  let result = selectedFilter.value === 'all'
    ? orderCards
    : selectedFilter.value === 'week'
      ? orderCards.filter(item => item.week)
      : orderCards.filter(item => item.month === selectedFilter.value)
  if (selectedStatus.value !== 'all') result = result.filter(o => o.status === selectedStatus.value)
  if (selectedTable.value !== 'all') result = result.filter(o => o.table.startsWith(selectedTable.value))
  if (selectedItemType.value !== 'all') {
    const snackNames = ['黑松露薯条']
    result = result.filter(o => selectedItemType.value === 'snack'
      ? o.items.some(i => snackNames.some(s => i.includes(s)))
      : o.items.some(i => !snackNames.some(s => i.includes(s))))
  }
  if (selectedProduct.value !== 'all') result = result.filter(o => o.items.some(i => i.includes(selectedProduct.value)))
  return result
})

function openDatePicker() { dateSheetOpen.value = true }
function closeDatePicker() { dateSheetOpen.value = false }
function selectFilter(type: string) { selectedFilter.value = type }
function selectMonth(value: string) {
  const [y, m] = value.split('-').map(Number)
  if (y > 2026 || (y === 2026 && m > currentMonth)) return
  selectedFilter.value = value; closeDatePicker()
}
function resetDateFilter() { selectedFilter.value = 'all'; closeDatePicker() }
function resetOrderFilters() {
  selectedStatus.value = 'all'; selectedTable.value = 'all'
  selectedItemType.value = 'all'; selectedProduct.value = 'all'
  selectedFilter.value = 'all'; closeDatePicker()
}
function openFilterSheet(type: 'status' | 'table' | 'type' | 'product') { filterSheet.value = type }
function closeFilterSheet() { filterSheet.value = null }
function chooseStatus(v: string) { selectedStatus.value = v; closeFilterSheet() }
function chooseTable(v: string) { selectedTable.value = v; closeFilterSheet() }
function chooseItemType(v: string) { selectedItemType.value = v; selectedProduct.value = 'all'; closeFilterSheet() }
function chooseProduct(v: string) { selectedProduct.value = v; closeFilterSheet() }
</script>

<template>
  <view class="view">
    <view class="orders-head">
      <view class="top-title">历史订单</view>
      <button class="date-capsule" @tap="openDatePicker">
        <view class="date-capsule-inner">
          <text>2026年5月</text>
          <text class="date-arrow">▾</text>
        </view>
      </button>
    </view>
    <scroll-view class="filter-bar" scroll-x enhanced show-scrollbar="false">
      <view class="filter-row">
        <button class="chip" :class="{ active: selectedFilter === 'all' }" @tap="selectFilter('all')">全部</button>
        <button class="chip" :class="{ active: selectedFilter === 'week' }" @tap="selectFilter('week')">近一周</button>
        <button class="chip" :class="{ active: selectedFilter === '2026-05' }" @tap="selectFilter('2026-05')">5月</button>
        <button class="chip" :class="{ active: selectedFilter === '2026-04' }" @tap="selectFilter('2026-04')">4月</button>
        <button class="chip more-chip" @tap="openDatePicker">更多</button>
      </view>
    </scroll-view>
    <view class="select-filter-row">
      <button class="select-pill" @tap="openFilterSheet('type')">{{ selectedItemType === 'all' ? '全部类型' : selectedItemType === 'drink' ? '酒水' : '小食' }} ▾</button>
      <button class="select-pill" @tap="openFilterSheet('table')">{{ selectedTable === 'all' ? '全部桌台' : `${selectedTable}区` }} ▾</button>
      <button class="select-pill" @tap="openFilterSheet('status')">{{ selectedStatus === 'all' ? '全部状态' : selectedStatus }} ▾</button>
      <button class="select-pill product-select" @tap="openFilterSheet('product')">{{ selectedProduct === 'all' ? '全部条目' : selectedProduct }} ▾</button>
      <button class="reset-filter" @tap="resetOrderFilters">重置</button>
    </view>
    <scroll-view class="orders-list" scroll-y enhanced show-scrollbar="false">
      <view v-if="!filteredOrders.length" class="orders-empty">
        <uv-empty mode="order" text="未找到订单" icon-color="#2b2f38" text-color="#8d929d" />
        <uv-button
          text="查看全部订单"
          plain
          shape="circle"
          size="small"
          custom-style="height: 34px; padding: 0 18px; margin-top: 8px; background: transparent; border-color: rgba(210,168,95,0.45); color: #d2a85f;"
          @click="resetDateFilter"
        />
      </view>
      <view v-for="order in filteredOrders" :key="order.id" class="order-card" @tap="push('orderDetail')">
        <view class="order-card-top">
          <view>
            <view class="bold">{{ order.date }} {{ order.time }}</view>
            <view class="muted small">{{ order.no }}</view>
          </view>
          <text class="status-pill">{{ order.status }}</text>
        </view>
        <view class="order-items">{{ order.items.join(' / ') }}</view>
        <view class="order-card-bottom">
          <text class="muted small">桌台 {{ order.table }} · 共 {{ order.count }} 件</text>
          <text class="order-amount">¥{{ order.amount.toFixed(2) }}</text>
        </view>
      </view>
    </scroll-view>

    <!-- Date Sheet -->
    <view v-if="dateSheetOpen" class="sheet-mask" @tap="closeDatePicker">
      <view class="date-panel" @tap.stop>
        <view class="sheet-grip" />
        <view class="sheet-head">
          <button class="plain-button" @tap="closeDatePicker">取消</button>
          <view class="bold">选择筛选月份</view>
          <button class="plain-button gold" @tap="resetDateFilter">查看全部</button>
        </view>
        <view class="year-row">
          <button class="icon-button" @tap="currentYear--">‹</button>
          <text>{{ currentYear }} 年</text>
          <button class="icon-button" :class="{ disabled: currentYear >= 2026 }" @tap="currentYear < 2026 && currentYear++">›</button>
        </view>
        <view class="month-grid">
          <view v-for="month in months" :key="month.value" class="month-cell"
            :class="{ active: selectedFilter === month.value && !month.isFuture, disabled: !month.count, future: month.isFuture }"
            @tap="selectMonth(month.value)">
            <view v-if="month.count" class="month-dot" />
            <view class="month-name">{{ month.label }}</view>
            <view class="month-tip">{{ month.count ? `${month.count}笔订单` : '无订单' }}</view>
          </view>
        </view>
        <view class="sheet-note"><text class="inline-dot" /> 选择特定月份，即可快速拉取该月在寻野酒吧的完整消费记录</view>
      </view>
    </view>

    <!-- Filter Sheet -->
    <view v-if="filterSheet" class="sheet-mask compact-mask" @tap="closeFilterSheet">
      <view class="filter-panel" @tap.stop>
        <view class="sheet-grip" />
        <view class="filter-panel-title">{{ filterSheet === 'status' ? '选择订单状态' : filterSheet === 'table' ? '选择桌台区域' : filterSheet === 'type' ? '选择类型' : '选择条目' }}</view>
        <view v-if="filterSheet === 'status'" class="filter-options">
          <button class="filter-option" :class="{ active: selectedStatus === 'all' }" @tap="chooseStatus('all')"><text>全部状态</text><text v-if="selectedStatus === 'all'">✓</text></button>
          <button class="filter-option" :class="{ active: selectedStatus === '已完成' }" @tap="chooseStatus('已完成')"><text>已完成</text><text v-if="selectedStatus === '已完成'">✓</text></button>
          <button class="filter-option" :class="{ active: selectedStatus === '制作中' }" @tap="chooseStatus('制作中')"><text>制作中</text><text v-if="selectedStatus === '制作中'">✓</text></button>
        </view>
        <view v-else-if="filterSheet === 'table'" class="filter-options">
          <button class="filter-option" :class="{ active: selectedTable === 'all' }" @tap="chooseTable('all')"><text>全部桌台</text><text v-if="selectedTable === 'all'">✓</text></button>
          <button class="filter-option" :class="{ active: selectedTable === 'A' }" @tap="chooseTable('A')"><text>A区</text><text v-if="selectedTable === 'A'">✓</text></button>
          <button class="filter-option" :class="{ active: selectedTable === 'V' }" @tap="chooseTable('V')"><text>V区</text><text v-if="selectedTable === 'V'">✓</text></button>
          <button class="filter-option" :class="{ active: selectedTable === 'B' }" @tap="chooseTable('B')"><text>B区</text><text v-if="selectedTable === 'B'">✓</text></button>
        </view>
        <view v-else-if="filterSheet === 'type'" class="filter-options">
          <button class="filter-option" :class="{ active: selectedItemType === 'all' }" @tap="chooseItemType('all')"><text>全部类型</text><text v-if="selectedItemType === 'all'">✓</text></button>
          <button class="filter-option" :class="{ active: selectedItemType === 'drink' }" @tap="chooseItemType('drink')"><text>酒水</text><text v-if="selectedItemType === 'drink'">✓</text></button>
          <button class="filter-option" :class="{ active: selectedItemType === 'snack' }" @tap="chooseItemType('snack')"><text>小食</text><text v-if="selectedItemType === 'snack'">✓</text></button>
        </view>
        <view v-else class="filter-options">
          <button class="filter-option" :class="{ active: selectedProduct === 'all' }" @tap="chooseProduct('all')"><text>全部条目</text><text v-if="selectedProduct === 'all'">✓</text></button>
          <button v-for="opt in productOptions" :key="opt" class="filter-option" :class="{ active: selectedProduct === opt }" @tap="chooseProduct(opt)"><text>{{ opt }}</text><text v-if="selectedProduct === opt">✓</text></button>
        </view>
      </view>
    </view>
  </view>
</template>

<style scoped>
.orders-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  min-height: 44px;
  padding: calc(var(--xunye-safe-top, 44px) + 8px) calc(var(--xunye-menu-right, 0px) + var(--xunye-menu-width, 0px) + 12px) 12px 16px;
  border-bottom: 1px solid var(--xunye-line);
}
.orders-head .top-title {
  flex: 1;
  min-width: 0;
  text-align: left;
  font-size: 22px;
  font-weight: 800;
}
.date-capsule {
  display: flex;
  align-items: center;
  justify-content: center;
  min-width: 116px;
  height: 36px;
  padding: 0 14px;
  background: var(--xunye-gold);
  color: #111318;
  border-radius: 999px;
  font-size: 14px;
  font-weight: 700;
  white-space: nowrap;
}
.date-capsule::after { display: none; }
.date-capsule-inner {
  display: flex;
  align-items: center;
  gap: 4px;
}
.date-arrow { color: #090909; font-size: 10px; }
.filter-bar {
  box-sizing: border-box;
  width: 100%;
  height: 55px;
  padding: 12px 16px 8px;
  border-bottom: 1px solid #151515;
  white-space: nowrap;
}
.filter-bar::-webkit-scrollbar {
  display: none;
  width: 0;
  height: 0;
}
.filter-row {
  display: block;
  min-width: 100%;
  white-space: nowrap;
}
.chip {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 76px;
  height: 34px;
  padding: 0 14px;
  margin-right: 8px;
  background: rgba(21, 23, 27, 0.94);
  border: 1px solid var(--xunye-line);
  border-radius: 999px;
  font-size: 13px;
  white-space: nowrap;
}
.chip.active {
  background: var(--xunye-gold);
  border-color: var(--xunye-gold);
  color: #111318;
  font-weight: 700;
}
.more-chip {
  color: var(--xunye-gold);
  border-color: rgba(210, 168, 95, 0.45);
}
.select-filter-row {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 8px;
  padding: 8px 16px 12px;
  border-bottom: 1px solid var(--xunye-line);
}
.select-pill {
  display: flex;
  align-items: center;
  justify-content: center;
  min-width: 0;
  height: 36px;
  padding: 0 10px;
  background: rgba(21, 23, 27, 0.94);
  border: 1px solid var(--xunye-line);
  border-radius: 999px;
  font-size: 12px;
  font-weight: 700;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.product-select {
  grid-column: span 2;
  max-width: none;
}
.reset-filter {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 36px;
  color: var(--xunye-gold);
  font-size: 12px;
  font-weight: 700;
  border: 1px solid rgba(210, 168, 95, 0.45);
  border-radius: 999px;
}
.orders-list {
  flex: 1;
  box-sizing: border-box;
  padding: 0 16px 104px;
  overflow-y: auto;
}
.orders-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 48px 0;
}
.order-card {
  padding: 14px;
  margin-bottom: 10px;
  background: rgba(21, 23, 27, 0.94);
  border: 1px solid var(--xunye-line);
  border-radius: 16px;
}
.order-card-top {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}
.status-pill {
  padding: 2px 10px;
  background: rgba(76, 175, 80, 0.12);
  border-radius: 999px;
  font-size: 11px;
  color: #4caf50;
}
.order-items {
  margin: 8px 0;
  font-size: 14px;
  color: #ccc;
}
.order-card-bottom {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.order-amount {
  font-size: 18px;
  font-weight: 700;
  color: var(--xunye-gold);
}

/* Date Sheet */
.sheet-mask {
  position: fixed;
  inset: 0;
  z-index: 50;
  background: rgba(0, 0, 0, 0.6);
  display: flex;
  align-items: flex-end;
}
.sheet-mask.compact-mask {
  align-items: center;
  justify-content: center;
}
.date-panel {
  width: 100%;
  max-height: 70%;
  background: var(--xunye-surface-2);
  border-radius: 20px 20px 0 0;
  padding: 8px 20px 24px;
}
.sheet-grip {
  width: 36px;
  height: 4px;
  background: rgba(255, 255, 255, 0.18);
  border-radius: 999px;
  margin: 0 auto 12px;
}
.sheet-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}
.year-row {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 16px;
  margin-bottom: 12px;
  font-size: 16px;
}
.month-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 8px;
}
.month-cell {
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 12px 0;
  background: rgba(21, 23, 27, 0.94);
  border-radius: 12px;
  border: 1px solid #222;
}
.month-cell.active { border-color: var(--xunye-gold); background: rgba(210, 168, 95, 0.08); }
.month-cell.disabled { opacity: 0.3; }
.month-cell.future { opacity: 0.2; }
.month-dot {
  width: 6px;
  height: 6px;
  background: var(--xunye-gold);
  border-radius: 999px;
  margin-bottom: 4px;
}
.month-name { font-size: 14px; font-weight: 600; }
.month-tip { font-size: 10px; color: #858585; margin-top: 2px; }
.sheet-note {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-top: 16px;
  font-size: 12px;
  color: #555;
}
.inline-dot {
  width: 5px;
  height: 5px;
  background: #555;
  border-radius: 999px;
  flex-shrink: 0;
}
.filter-panel {
  width: 80%;
  max-height: 60%;
  background: var(--xunye-surface-2);
  border-radius: 20px;
  padding: 8px 20px 24px;
}
.filter-panel-title {
  text-align: center;
  font-weight: 600;
  font-size: 15px;
  margin-bottom: 12px;
}
.filter-options {
  display: flex;
  flex-direction: column;
  gap: 2px;
}
.filter-option {
  display: flex;
  justify-content: space-between;
  padding: 12px 8px;
  border-radius: 8px;
  font-size: 14px;
}
.filter-option.active { color: var(--xunye-gold); }
.filter-option:active { background: #222; }

/* button reset */
button {
  padding: 0; margin: 0;
  color: inherit; font: inherit;
  line-height: 1; background: transparent;
  border: 0;
}
</style>
