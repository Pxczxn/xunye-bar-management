<script lang="ts" setup>
import { computed, nextTick, reactive, ref } from 'vue'
import { categories, products, useXunyeStore, type MenuProduct } from '@/store'

defineOptions({ name: 'XunyeShell' })

type ViewName =
  | 'index'
  | 'table'
  | 'menu'
  | 'cart'
  | 'payment'
  | 'orderResult'
  | 'orderDetail'
  | 'orders'
  | 'message'
  | 'mine'
  | 'membership'
  | 'contact'

interface OrderCard {
  id: number
  no: string
  date: string
  time: string
  table: string
  status: string
  amount: number
  count: number
  month: string
  week: boolean
  items: string[]
}

const store = useXunyeStore()
const activeView = ref<ViewName>('index')
const history = ref<ViewName[]>(['index'])
const activeCategoryId = ref(1)
const toastText = ref('')
const toastVisible = ref(false)
const remark = ref('')
const tableInput = ref('')
const dateSheetOpen = ref(false)
const selectedFilter = ref('all')
const currentYear = ref(2026)
const currentMonth = 5
const selectedStatus = ref('all')
const selectedTable = ref('all')
const selectedItemType = ref('all')
const selectedProduct = ref('all')
const filterSheet = ref<'status' | 'table' | 'type' | 'product' | null>(null)

// 会员权益数据
const memberPoints = ref(128)
const memberBalance = ref(0)
const currentMemberLevelRaw = ref('REGULAR')
const currentMemberLevel = ref('普通会员')

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

function callPhone(phone: string) {
  uni.makePhoneCall({ phoneNumber: phone })
}

function copyText(text: string) {
  uni.setClipboardData({
    data: text,
    success: () => { uni.showToast({ title: '已复制', icon: 'success', duration: 1500 }) },
  })
}

const orderCards = reactive<OrderCard[]>([
  {
    id: 1,
    no: 'XN202605160021',
    date: '2026-05-16',
    time: '21:30',
    table: 'A08',
    status: '已完成',
    amount: 161,
    count: 3,
    month: '2026-05',
    week: true,
    items: ['寻野特调迷雾', '日落大道', '黑松露薯条'],
  },
  {
    id: 2,
    no: 'XN202605100018',
    date: '2026-05-10',
    time: '22:14',
    table: 'V01',
    status: '已完成',
    amount: 176,
    count: 2,
    month: '2026-05',
    week: false,
    items: ['麦卡伦12年单杯', '长岛冰茶'],
  },
  {
    id: 3,
    no: 'XN202604280011',
    date: '2026-04-28',
    time: '20:46',
    table: 'B05',
    status: '已完成',
    amount: 98,
    count: 2,
    month: '2026-04',
    week: false,
    items: ['百威啤酒', '黑松露薯条'],
  },
])

const mainTabs: ViewName[] = ['index', 'menu', 'orders', 'message', 'mine']
const props = withDefaults(defineProps<{
  initialView?: ViewName
}>(), {
  initialView: 'index',
})
const pageRoutes: Partial<Record<ViewName, string>> = {
  index: '/pages/index/index',
  menu: '/pages/menu/menu',
  orders: '/pages/orders/orders',
  message: '/pages/message/message',
  mine: '/pages/mine/mine',
}
const currentProducts = computed(() => products.filter(item => item.categoryId === activeCategoryId.value))
const featuredProducts = computed(() => [products[0], products[4], products[5]].filter(Boolean))
const tableText = computed(() => store.currentTable?.code || '未选桌')
const cartCanSubmit = computed(() => store.totalQty > 0)
const displayOrder = computed(() => store.lastOrder || {
  orderNo: 'XN202605160021',
  createdAt: '2026-05-16 21:30:15',
  table: store.currentTable || { area: '澶у巺', code: 'A08' },
  items: store.cartItems,
  totalAmount: store.totalAmount || 161,
  remark: remark.value || '少冰，偏清爽',
  status: '制作中',
})
const filteredOrders = computed(() => {
  let result = selectedFilter.value === 'all'
    ? orderCards
    : selectedFilter.value === 'week'
      ? orderCards.filter(item => item.week)
      : orderCards.filter(item => item.month === selectedFilter.value)

  if (selectedStatus.value !== 'all') {
    result = result.filter(order => order.status === selectedStatus.value)
  }

  if (selectedTable.value !== 'all') {
    result = result.filter(order => order.table.startsWith(selectedTable.value))
  }

  if (selectedItemType.value !== 'all') {
    const snackNames = ['黑松露薯条']
    result = result.filter((order) => {
      const hasSnack = order.items.some(item => snackNames.some(name => item.includes(name)))
      return selectedItemType.value === 'snack' ? hasSnack : !hasSnack || order.items.some(item => !snackNames.some(name => item.includes(name)))
    })
  }

  if (selectedProduct.value !== 'all') {
    result = result.filter(order => order.items.some(item => item.includes(selectedProduct.value)))
  }

  return result
})

const productOptions = computed(() => {
  const drinkOptions = ['寻野特调迷雾', '日落大道', '麦卡伦12年单杯', '长岛冰茶', '百威啤酒']
  const snackOptions = ['黑松露薯条']
  if (selectedItemType.value === 'drink') {
    return drinkOptions
  }
  if (selectedItemType.value === 'snack') {
    return snackOptions
  }
  return [...drinkOptions, ...snackOptions]
})
const months = computed(() => Array.from({ length: 12 }, (_, index) => {
  const value = `${currentYear.value}-${String(index + 1).padStart(2, '0')}`
  const count = orderCards.filter(order => order.month === value).length
  const monthNumber = index + 1
  const isFuture = currentYear.value > 2026 || (currentYear.value === 2026 && monthNumber > currentMonth)
  return {
    value,
    label: `${index + 1}月`,
    count,
    isFuture,
  }
}))

function showToast(message: string) {
  toastText.value = message
  toastVisible.value = true
  setTimeout(() => {
    toastVisible.value = false
  }, 1800)
}

function show(view: ViewName) {
  activeView.value = view
}

function push(view: ViewName) {
  history.value.push(view)
  show(view)
}

function replace(view: ViewName) {
  history.value[history.value.length - 1] = view
  show(view)
}

function back() {
  if (history.value.length <= 1) {
    switchTab('index')
    return
  }
  history.value.pop()
  show(history.value[history.value.length - 1])
}

function switchTab(view: ViewName) {
  history.value = [view]
  show(view)
}

function goPage(view: ViewName) {
  const url = pageRoutes[view]
  if (!url || view === activeView.value) {
    switchTab(view)
    return
  }
  uni.redirectTo({ url })
}

function handleStartOrder() {
  if (store.currentTable) {
    goPage('menu')
  }
  else {
    push('table')
  }
}

function handleTabTap(view: ViewName) {
  goPage(view)
}

function selectTable(area: string, code: string) {
  store.selectTable({ area, code })
  showToast(`已选择桌台: ${code}`)
  setTimeout(() => goPage('menu'), 450)
}

function confirmTable() {
  const code = tableInput.value.trim().toUpperCase()
  if (!code) {
    showToast('请输入桌号')
    return
  }
  selectTable('澶у巺', code)
}

function ensureTable() {
  if (store.currentTable) {
    return true
  }
  showToast('璇峰厛閫夋嫨妗屽彴')
  setTimeout(() => push('table'), 700)
  return false
}

function addProduct(product: MenuProduct) {
  if (!ensureTable()) {
    return
  }
  store.addProduct(product)
}

function decreaseProduct(productId: number) {
  store.decreaseProduct(productId)
}

function clearCart() {
  store.clearCart()
  showToast('已清空购物车')
}

function submitOrder() {
  if (!cartCanSubmit.value) {
    showToast('璇峰厛閫夋嫨鍟嗗搧')
    return
  }
  store.createOrderSnapshot(remark.value)
  showToast('订单提交中...')
  setTimeout(() => push('payment'), 650)
}

function executePayment() {
  showToast('姝ｅ湪璋冪敤寰俊鏀粯...')
  setTimeout(() => {
    store.completePayment(remark.value)
    replace('orderResult')
  }, 900)
}

function openDatePicker() {
  dateSheetOpen.value = true
}

function closeDatePicker() {
  dateSheetOpen.value = false
}

function selectFilter(type: string) {
  selectedFilter.value = type
}

function selectMonth(value: string) {
  const [year, month] = value.split('-').map(Number)
  if (year > 2026 || (year === 2026 && month > currentMonth)) {
    showToast('涓嶈兘閫夋嫨鏈潵鏈堜唤')
    return
  }
  selectedFilter.value = value
  closeDatePicker()
  showToast(`已筛选: ${Number(value.slice(5))}月`)
}

function resetDateFilter() {
  selectedFilter.value = 'all'
  closeDatePicker()
  showToast('已显示全部订单')
}

function resetOrderFilters() {
  selectedStatus.value = 'all'
  selectedTable.value = 'all'
  selectedItemType.value = 'all'
  selectedProduct.value = 'all'
  selectedFilter.value = 'all'
  closeDatePicker()
}

function openFilterSheet(type: 'status' | 'table' | 'type' | 'product') {
  filterSheet.value = type
}

function closeFilterSheet() {
  filterSheet.value = null
}

function chooseStatus(value: string) {
  selectedStatus.value = value
  closeFilterSheet()
}

function chooseTable(value: string) {
  selectedTable.value = value
  closeFilterSheet()
}

function chooseItemType(value: string) {
  selectedItemType.value = value
  selectedProduct.value = 'all'
  closeFilterSheet()
}

function chooseProduct(value: string) {
  selectedProduct.value = value
  closeFilterSheet()
}

nextTick(() => {
  switchTab(props.initialView)
})
</script>

<template>
  <view class="prototype-page">
    <view class="phone-shell">
      <view class="notch" />
      <view class="app-screen">
        <view v-if="activeView === 'index'" class="view view-scroll">
          <view class="brand-head">
            <view class="brand-title">
              寻野 XUNYE
            </view>
            <view class="muted small">
              营业时间 18:00 - 02:00
            </view>
          </view>

          <view class="notice-pill">
            <text class="gold">
              📣
            </text>
            <text class="notice-text">
              公告：今晚 21:00 爵士现场即将开始，敬请期待！特调买二送一。
            </text>
          </view>

          <view class="table-card">
            <view>
              <view class="muted small">
                当前桌台
              </view>
              <view class="table-code">
                {{ tableText }}
              </view>
            </view>
            <button class="gold-button small-button" @tap="push('table')">
              去选桌 →
            </button>
          </view>

          <view class="action-grid">
            <view class="action-card" @tap="handleStartOrder">
              <view class="action-icon gold-bg">
                🍸
              </view>
              <view>开始点餐</view>
            </view>
            <view class="action-card" @tap="push('table')">
              <view class="action-icon blue-bg">
                ▣
              </view>
              <view>扫码点单</view>
            </view>
          </view>

          <view class="section">
            <view class="section-title">
              🔥 店长推荐
            </view>
            <scroll-view class="recommend-scroll" scroll-x enhanced show-scrollbar="false">
              <view class="recommend-row">
                <view v-for="product in featuredProducts" :key="product.id" class="recommend-card">
                  <image class="recommend-img" mode="aspectFill" lazy-load :src="product.image" />
                  <view class="recommend-info">
                    <view class="product-name truncate">
                      {{ product.name }}
                    </view>
                    <view class="price-row">
                      <text class="price">
                        ¥{{ product.price.toFixed(2) }}
                      </text>
                      <button class="round-plus" @tap="addProduct(product)">
                        +
                      </button>
                    </view>
                  </view>
                </view>
              </view>
            </scroll-view>
          </view>
        </view>

        <view v-if="activeView === 'table'" class="view">
          <view class="topbar">
            <button class="icon-button" @tap="back">
              鈥?            </button>
            <view class="top-title">
              閫夋嫨妗屽彴
            </view>
            <view class="icon-button ghost" />
          </view>
          <view class="content">
            <view class="scan-card">
              <view class="scan-icon">
                鈻?              </view>
              <view class="bold">
                鎵爜閫夋
              </view>
              <view class="muted small">
                鎵弿妗岄潰浜岀淮鐮佸揩閫熺偣鍗?              </view>
            </view>
            <view class="sub-title">
              鎵嬪姩杈撳叆妗屽彿
            </view>
            <view class="table-input-row">
              <input v-model="tableInput" class="table-input" placeholder="渚嬪: A08" placeholder-class="placeholder" />
              <button class="gold-button confirm-button" @tap="confirmTable">
                纭
              </button>
            </view>
            <view class="sub-title">
              鍙敤鍖哄煙灞曠ず
            </view>
            <view class="table-grid">
              <view class="table-cell" @tap="selectTable('澶у巺', 'A01')">
                <text class="muted mini">
                  澶у巺
                </text>
                <text class="table-no">
                  A01
                </text>
              </view>
              <view class="table-cell active" @tap="selectTable('澶у巺', 'A08')">
                <text class="recent">
                  鏈€杩?                </text>
                <text class="gold mini">
                  澶у巺
                </text>
                <text class="table-no gold">
                  A08
                </text>
              </view>
              <view class="table-cell" @tap="selectTable('鍗″骇', 'V01')">
                <text class="muted mini">
                  鍗″骇
                </text>
                <text class="table-no">
                  V01
                </text>
              </view>
              <view class="table-cell" @tap="selectTable('鍚у彴', 'B05')">
                <text class="muted mini">
                  鍚у彴
                </text>
                <text class="table-no">
                  B05
                </text>
              </view>
            </view>
          </view>
        </view>

        <view v-if="activeView === 'menu'" class="view">
          <view class="menu-head">
            <view class="menu-brand">
              瀵婚噹 XUNYE
            </view>
            <view class="menu-status">
              <view class="table-pill">
                馃搷 {{ tableText }}
              </view>
              <view class="search-dot">
                馃攳
              </view>
            </view>
          </view>
          <view class="menu-layout">
            <scroll-view class="category-list" scroll-y enhanced show-scrollbar="false">
              <view
                v-for="category in categories"
                :key="category.id"
                class="category-item"
                :class="{ selected: activeCategoryId === category.id }"
                @tap="activeCategoryId = category.id"
              >
                {{ category.name }}
              </view>
            </scroll-view>
            <scroll-view class="product-list" scroll-y enhanced show-scrollbar="false">
              <view class="category-label">
                {{ categories.find(item => item.id === activeCategoryId)?.name }}
              </view>
              <view v-for="product in currentProducts" :key="product.id" class="product-item">
                <image class="product-img" mode="aspectFill" lazy-load :src="product.image" />
                <view class="product-main">
                  <view>
                    <view class="product-name">
                      {{ product.name }}
                    </view>
                    <view class="product-desc">
                      {{ product.description }}
                    </view>
                  </view>
                  <view class="price-row">
                    <text class="price">
                      楼{{ product.price }}
                    </text>
                    <view class="stepper">
                      <button v-if="store.getQty(product.id)" class="minus" @tap="decreaseProduct(product.id)">
                        -
                      </button>
                      <text v-if="store.getQty(product.id)" class="qty">
                        {{ store.getQty(product.id) }}
                      </text>
                      <button class="round-plus" @tap="addProduct(product)">
                        +
                      </button>
                    </view>
                  </view>
                </view>
              </view>
            </scroll-view>
          </view>
          <view class="cart-float" :class="{ hidden: !store.totalQty }" @tap="push('cart')">
            <view class="cart-icon">
              馃洅
              <text class="cart-badge">
                {{ store.totalQty }}
              </text>
            </view>
            <view>
              <view class="cart-price">
                楼{{ store.totalAmount.toFixed(2) }}
              </view>
              <view class="muted tiny">
                鍙﹂渶鏀粯鏈嶅姟璐?楼0
              </view>
            </view>
            <button class="gold-button checkout-small">
              鍘荤粨绠?            </button>
          </view>
        </view>

        <view v-if="activeView === 'cart'" class="view">
          <view class="topbar">
            <button class="icon-button" @tap="back">
              鈥?            </button>
            <view class="top-title">
              纭璁㈠崟
            </view>
            <button class="plain-button" @tap="clearCart">
              娓呯┖
            </button>
          </view>
          <scroll-view class="content cart-content" scroll-y enhanced show-scrollbar="false">
            <view class="summary-card">
              <view class="summary-icon">
                馃搷
              </view>
              <view>
                <view class="muted small">
                  灏遍妗屽彴
                </view>
                <view class="bold">
                  {{ tableText }}
                </view>
              </view>
            </view>
            <view class="panel">
              <view class="panel-title">
                宸查€夊晢鍝?              </view>
              <view v-if="!store.totalQty" class="empty-line">
                璐墿杞︾┖绌哄涔?              </view>
              <view v-for="item in store.cartItems" :key="item.id" class="cart-line">
                <view>
                  <view class="product-name">
                    {{ item.name }}
                  </view>
                  <view class="price">
                    楼{{ item.price }}
                  </view>
                </view>
                <view class="stepper">
                  <button class="minus gray" @tap="decreaseProduct(item.id)">
                    -
                  </button>
                  <text class="qty">
                    {{ item.qty }}
                  </text>
                  <button class="round-plus" @tap="addProduct(item)">
                    +
                  </button>
                </view>
              </view>
            </view>
            <view class="panel">
              <view class="remark-row">
                <text>璁㈠崟澶囨敞</text>
                <input v-model="remark" class="remark-input" placeholder="鍙ｅ懗銆佸亸濂界瓑瑕佹眰" placeholder-class="placeholder" />
              </view>
            </view>
          </scroll-view>
          <view class="bottom-pay">
            <view>
              <view class="muted small">
                鍚堣
              </view>
              <view class="pay-total">
                楼{{ store.totalAmount.toFixed(2) }}
              </view>
            </view>
            <button class="gold-button submit-button" @tap="submitOrder">
              鎻愪氦璁㈠崟
            </button>
          </view>
        </view>

        <view v-if="activeView === 'payment'" class="view">
          <view class="topbar">
            <button class="icon-button" @tap="back">
              鈥?            </button>
            <view class="top-title">
              鏀粯璁㈠崟
            </view>
            <view class="icon-button ghost" />
          </view>
          <view class="payment-body">
            <view class="pay-amount">
              楼{{ displayOrder.totalAmount.toFixed(2) }}
            </view>
            <view class="muted">
              璇风‘璁よ鍗曞苟閫夋嫨鏀粯鏂瑰紡
            </view>
            <view class="panel wide">
              <view class="info-line">
                <text>璁㈠崟缂栧彿</text><text>{{ displayOrder.orderNo }}</text>
              </view>
              <view class="info-line">
                <text>妗屽彴</text><text>{{ displayOrder.table?.code }}</text>
              </view>
              <view class="info-line">
                <text>鏀粯閲戦</text><text class="gold">楼{{ displayOrder.totalAmount.toFixed(2) }}</text>
              </view>
            </view>
            <view class="pay-method active">
              <view>
                <view class="bold">
                  寰俊鏀粯
                </view>
                <view class="muted small">
                  鎺ㄨ崘浣跨敤
                </view>
              </view>
              <view class="radio-active" />
            </view>
            <view class="pay-method muted-method">
              <view>
                <view class="bold">
                  鍒板簵鏀粯
                </view>
                <view class="muted small">
                  鏆備笉鍙敤
                </view>
              </view>
              <view class="radio" />
            </view>
          </view>
          <view class="bottom-pay">
            <button class="gold-button full-button" @tap="executePayment">
              纭鏀粯
            </button>
          </view>
        </view>

        <view v-if="activeView === 'orderResult'" class="view result-view">
          <view class="success-circle">
            鉁?          </view>
          <view class="result-title">
            鏀粯鎴愬姛
          </view>
          <view class="muted result-copy">
            鍚у彴宸叉敹鍒版偍鐨勮鍗?br>姝ｅ湪涓烘偍绮惧績鍒朵綔涓?          </view>
          <view class="panel wide">
            <view class="info-line">
              <text>鏀粯閲戦</text><text class="gold">楼{{ displayOrder.totalAmount.toFixed(2) }}</text>
            </view>
            <view class="info-line">
              <text>妗屽彴</text><text>{{ displayOrder.table?.code }}</text>
            </view>
          </view>
          <view class="result-actions">
            <button class="outline-button" @tap="goPage('index')">
              杩斿洖棣栭〉
            </button>
            <button class="gold-button flex-button" @tap="replace('orderDetail')">
              鏌ョ湅璁㈠崟
            </button>
          </view>
        </view>

        <view v-if="activeView === 'orderDetail'" class="view">
          <view class="topbar">
            <button class="icon-button" @tap="back">
              鈥?            </button>
            <view class="top-title">
              璁㈠崟璇︽儏
            </view>
            <view class="icon-button ghost" />
          </view>
          <scroll-view class="content" scroll-y enhanced show-scrollbar="false">
            <view class="status-banner">
              <view>
                <view class="bold">
                  {{ displayOrder.status }}
                </view>
                <view class="muted small">
                  鍚у彴姝ｅ湪澶勭悊鎮ㄧ殑璁㈠崟
                </view>
              </view>
              <text class="status-icon">
                馃嵐
              </text>
            </view>
            <view class="panel">
              <view class="order-panel-head">
                <text class="bold">鍟嗗搧鏄庣粏</text>
                <text class="table-tag">{{ displayOrder.table?.code }}</text>
              </view>
              <view v-for="item in displayOrder.items" :key="item.id" class="detail-line">
                <text>{{ item.name }} <text class="muted">x{{ item.qty }}</text></text>
                <text>楼{{ (item.price * item.qty).toFixed(2) }}</text>
              </view>
              <view class="detail-total">
                <text>鍚堣</text>
                <text class="gold">楼{{ displayOrder.totalAmount.toFixed(2) }}</text>
              </view>
            </view>
            <view class="panel info-panel">
              <view class="panel-title">
                璁㈠崟淇℃伅
              </view>
              <view class="info-line">
                <text>璁㈠崟缂栧彿</text><text>{{ displayOrder.orderNo }}</text>
              </view>
              <view class="info-line">
                <text>涓嬪崟鏃堕棿</text><text>{{ displayOrder.createdAt }}</text>
              </view>
              <view class="info-line">
                <text>鏀粯鏂瑰紡</text><text>寰俊鏀粯</text>
              </view>
              <view class="info-line">
                <text>订单备注</text><text>{{ displayOrder.remark || '无' }}</text>
              </view>
            </view>
          </scroll-view>
        </view>

        <view v-if="activeView === 'orders'" class="view">
          <view class="orders-head">
            <view class="top-title">
              历史订单
            </view>
            <button class="date-capsule" @tap="openDatePicker">
              <view class="date-capsule-inner">
                <text>2026年5月</text>
                <text class="date-arrow">▾</text>
              </view>
            </button>
          </view>
          <view class="filter-bar">
            <button class="chip" :class="{ active: selectedFilter === 'all' }" @tap="selectFilter('all')">
              <text class="button-center">全部</text>
            </button>
            <button class="chip" :class="{ active: selectedFilter === 'week' }" @tap="selectFilter('week')">
              <text class="button-center">近一周</text>
            </button>
            <button class="chip" :class="{ active: selectedFilter === '2026-05' }" @tap="selectFilter('2026-05')">
              <text class="button-center">5月</text>
            </button>
            <button class="chip" :class="{ active: selectedFilter === '2026-04' }" @tap="selectFilter('2026-04')">
              <text class="button-center">4月</text>
            </button>
          </view>
          <view class="select-filter-row">
            <button class="select-pill" @tap="openFilterSheet('type')">
              {{ selectedItemType === 'all' ? '全部类型' : selectedItemType === 'drink' ? '酒水' : '小食' }} ▾
            </button>
            <button class="select-pill" @tap="openFilterSheet('table')">
              {{ selectedTable === 'all' ? '全部桌台' : `${selectedTable}区` }} ▾
            </button>
            <button class="select-pill" @tap="openFilterSheet('status')">
              {{ selectedStatus === 'all' ? '全部状态' : selectedStatus }} ▾
            </button>
            <button class="select-pill product-select" @tap="openFilterSheet('product')">
              {{ selectedProduct === 'all' ? '全部条目' : selectedProduct }} ▾
            </button>
            <button class="reset-filter" @tap="resetOrderFilters">
              重置
            </button>
          </view>
          <scroll-view class="orders-list" scroll-y enhanced show-scrollbar="false">
            <view v-if="!filteredOrders.length" class="orders-empty">
              <view class="empty-icon">
                ○
              </view>
              <view class="bold">
                未找到订单
              </view>
              <view class="muted small">
                换个筛选条件看看
              </view>
              <button class="outline-gold" @tap="resetDateFilter">
                查看全部订单
              </button>
            </view>
            <view v-for="order in filteredOrders" :key="order.id" class="order-card" @tap="push('orderDetail')">
              <view class="order-card-top">
                <view>
                  <view class="bold">
                    {{ order.date }} {{ order.time }}
                  </view>
                  <view class="muted small">
                    {{ order.no }}
                  </view>
                </view>
                <text class="status-pill">
                  {{ order.status }}
                </text>
              </view>
              <view class="order-items">
                {{ order.items.join(' / ') }}
              </view>
              <view class="order-card-bottom">
                <text class="muted small">桌台 {{ order.table }} · 共 {{ order.count }} 件</text>
                <text class="order-amount">¥{{ order.amount.toFixed(2) }}</text>
              </view>
            </view>
          </scroll-view>
        </view>

        <view v-if="activeView === 'message'" class="view">
          <view class="simple-head">
            消息
          </view>
          <view class="content">
            <view class="message-card">
              <view class="message-row">
                <text class="message-tag">订单通知</text>
                <text class="muted mini">刚刚</text>
              </view>
              <view class="bold">
                您的订单已制作完成
              </view>
              <view class="muted small">
                A08 桌台的饮品已完成，请留意服务员送达。
              </view>
            </view>
            <view class="message-card">
              <view class="message-row">
                <text class="message-tag">活动推送</text>
                <text class="muted mini">21:00</text>
              </view>
              <view class="bold">
                爵士现场即将开始
              </view>
              <view class="muted small">
                今晚特调买二送一，适合和朋友一起微醺。
              </view>
            </view>
          </view>
        </view>

        <view v-if="activeView === 'mine'" class="view">
          <view class="simple-head">
            我的
          </view>
          <view class="content">
            <view class="profile-card">
              <image class="avatar" src="/static/images/avatar.jpg" mode="aspectFill" lazy-load />
              <view>
                <view class="bold profile-name">
                  寻野会员
                </view>
                <view class="muted small">
                  微醺等级 Lv.2
                </view>
              </view>
            </view>
            <view class="stats-card">
              <view>
                <view class="gold stat-num">
                  128
                </view>
                <view class="muted mini">
                  积分
                </view>
              </view>
              <view class="divider" />
              <view>
                <view class="gold stat-num">
                  3
                </view>
                <view class="muted mini">
                  优惠券
                </view>
              </view>
              <view class="divider" />
              <view>
                <view class="gold stat-num">
                  12
                </view>
                <view class="muted mini">
                  订单
                </view>
              </view>
            </view>
            <view class="panel">
              <view class="mine-row" @tap="goPage('orders')">
                <text>历史订单</text><text>›</text>
              </view>
              <view class="mine-row" @tap="push('membership')">
                <text>会员权益</text><text>›</text>
              </view>
              <view class="mine-row" @tap="push('contact')">
                <text>联系客服</text><text>›</text>
              </view>
            </view>
          </view>
        </view>

        <view v-if="mainTabs.includes(activeView)" class="tabbar">
          <button class="tab-item" :class="{ active: activeView === 'index' }" @tap.stop="handleTabTap('index')">
            <text class="tab-icon">⌂</text><text>首页</text>
          </button>
          <button class="tab-item" :class="{ active: activeView === 'menu' }" @tap.stop="handleTabTap('menu')">
            <text class="tab-icon">☰</text><text>点餐</text>
          </button>
          <button class="tab-item" :class="{ active: activeView === 'orders' }" @tap.stop="handleTabTap('orders')">
            <text class="tab-icon">◎</text><text>订单</text>
          </button>
          <button class="tab-item" :class="{ active: activeView === 'message' }" @tap.stop="handleTabTap('message')">
            <text class="tab-icon">✉</text><text>消息</text>
          </button>
          <button class="tab-item" :class="{ active: activeView === 'mine' }" @tap.stop="handleTabTap('mine')">
            <text class="tab-icon">◐</text><text>我的</text>
          </button>
        </view>

        <view v-if="dateSheetOpen" class="sheet-mask" @tap="closeDatePicker">
          <view class="date-panel" @tap.stop>
            <view class="sheet-grip" />
            <view class="sheet-head">
              <button class="plain-button" @tap="closeDatePicker">
                取消
              </button>
              <view class="bold">
                选择筛选月份
              </view>
              <button class="plain-button gold" @tap="resetDateFilter">
                查看全部
              </button>
            </view>
            <view class="year-row">
              <button class="icon-button" @tap="currentYear--">
                ‹
              </button>
              <text>{{ currentYear }} 年</text>
              <button class="icon-button" :class="{ disabled: currentYear >= 2026 }" @tap="currentYear < 2026 && currentYear++">
                ›
              </button>
            </view>
            <view class="month-grid">
              <view
                v-for="month in months"
                :key="month.value"
                class="month-cell"
                :class="{ active: selectedFilter === month.value && !month.isFuture, disabled: !month.count, future: month.isFuture }"
                @tap="selectMonth(month.value)"
              >
                <view v-if="month.count" class="month-dot" />
                <view class="month-name">
                  {{ month.label }}
                </view>
                <view class="month-tip">
                  {{ month.count ? `${month.count}笔订单` : '无订单' }}
                </view>
              </view>
            </view>
            <view class="sheet-note">
              <text class="inline-dot" /> 选择特定月份，即可快速拉取该月在寻野酒吧的完整消费记录
            </view>
          </view>
        </view>

        <view v-if="filterSheet" class="sheet-mask compact-mask" @tap="closeFilterSheet">
          <view class="filter-panel" @tap.stop>
            <view class="sheet-grip" />
            <view class="filter-panel-title">
              {{ filterSheet === 'status' ? '选择订单状态' : filterSheet === 'table' ? '选择桌台区域' : filterSheet === 'type' ? '选择类型' : '选择条目' }}
            </view>
            <view v-if="filterSheet === 'status'" class="filter-options">
              <button class="filter-option" :class="{ active: selectedStatus === 'all' }" @tap="chooseStatus('all')">
                <text>全部状态</text><text v-if="selectedStatus === 'all'">✓</text>
              </button>
              <button class="filter-option" :class="{ active: selectedStatus === '已完成' }" @tap="chooseStatus('已完成')">
                <text>已完成</text><text v-if="selectedStatus === '已完成'">✓</text>
              </button>
              <button class="filter-option" :class="{ active: selectedStatus === '制作中' }" @tap="chooseStatus('制作中')">
                <text>制作中</text><text v-if="selectedStatus === '制作中'">✓</text>
              </button>
            </view>
            <view v-else-if="filterSheet === 'table'" class="filter-options">
              <button class="filter-option" :class="{ active: selectedTable === 'all' }" @tap="chooseTable('all')">
                <text>全部桌台</text><text v-if="selectedTable === 'all'">✓</text>
              </button>
              <button class="filter-option" :class="{ active: selectedTable === 'A' }" @tap="chooseTable('A')">
                <text>A区</text><text v-if="selectedTable === 'A'">✓</text>
              </button>
              <button class="filter-option" :class="{ active: selectedTable === 'V' }" @tap="chooseTable('V')">
                <text>V区</text><text v-if="selectedTable === 'V'">✓</text>
              </button>
              <button class="filter-option" :class="{ active: selectedTable === 'B' }" @tap="chooseTable('B')">
                <text>B区</text><text v-if="selectedTable === 'B'">✓</text>
              </button>
            </view>
            <view v-else-if="filterSheet === 'type'" class="filter-options">
              <button class="filter-option" :class="{ active: selectedItemType === 'all' }" @tap="chooseItemType('all')">
                <text>全部类型</text><text v-if="selectedItemType === 'all'">✓</text>
              </button>
              <button class="filter-option" :class="{ active: selectedItemType === 'drink' }" @tap="chooseItemType('drink')">
                <text>酒水</text><text v-if="selectedItemType === 'drink'">✓</text>
              </button>
              <button class="filter-option" :class="{ active: selectedItemType === 'snack' }" @tap="chooseItemType('snack')">
                <text>小食</text><text v-if="selectedItemType === 'snack'">✓</text>
              </button>
            </view>
            <view v-else class="filter-options">
              <button class="filter-option" :class="{ active: selectedProduct === 'all' }" @tap="chooseProduct('all')">
                <text>全部条目</text><text v-if="selectedProduct === 'all'">✓</text>
              </button>
              <button
                v-for="option in productOptions"
                :key="option"
                class="filter-option"
                :class="{ active: selectedProduct === option }"
                @tap="chooseProduct(option)"
              >
                <text>{{ option }}</text><text v-if="selectedProduct === option">✓</text>
              </button>
            </view>
          </view>
        </view>

        <!-- 会员权益 -->
        <view v-if="activeView === 'membership'" class="view">
          <view class="topbar">
            <button class="icon-button" @tap="back">‹</button>
            <view class="top-title">会员权益</view>
          </view>
          <scroll-view scroll-y class="scroll-view">
            <!-- 会员卡片 -->
            <view class="member-card">
              <view class="member-avatar">◐</view>
              <view class="member-info">
                <text class="member-name">寻野会员</text>
                <text class="member-level-badge">{{ currentMemberLevel }}</text>
              </view>
              <view class="member-points">
                <text class="points-num">{{ memberPoints }}</text>
                <text class="points-label">积分</text>
              </view>
            </view>

            <!-- 等级权益 -->
            <view class="section">
              <text class="section-title">等级权益</text>
              <view v-for="level in memberLevels" :key="level.level" class="level-card"
                :class="{ 'level-active': level.level === currentMemberLevelRaw }">
                <view class="level-header">
                  <text class="level-name">{{ level.name }}</text>
                  <text v-if="level.level !== 'SVIP'" class="level-requirement">
                    累计消费 ¥{{ level.minAmount }}
                  </text>
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

            <!-- 积分与余额 -->
            <view class="section">
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

            <!-- 进行中的活动 -->
            <view class="section">
              <text class="section-title">进行中的活动</text>
              <view v-if="activeActivities.length === 0" class="empty-hint">
                <text>暂无活动</text>
              </view>
              <view v-for="act in activeActivities" :key="act.id" class="activity-card">
                <text class="activity-title">{{ act.title }}</text>
                <text class="activity-desc">{{ act.description }}</text>
              </view>
            </view>

            <view style="height: 20px" />
          </scroll-view>
        </view>

        <!-- 联系客服 -->
        <view v-if="activeView === 'contact'" class="view">
          <view class="topbar">
            <button class="icon-button" @tap="back">‹</button>
            <view class="top-title">联系客服</view>
          </view>
          <scroll-view scroll-y class="scroll-view">
            <!-- 客服卡片 -->
            <view class="section">
              <view class="contact-card">
                <view class="contact-icon-wrap">
                  <text class="contact-icon">🎧</text>
                </view>
                <text class="contact-name">在线客服</text>
                <text class="contact-desc">营业时间内快速响应</text>
              </view>
            </view>

            <!-- 联系方式 -->
            <view class="section">
              <view class="contact-row" @tap="callPhone('13800000000')">
                <text class="contact-row-icon">📞</text>
                <view class="contact-row-info">
                  <text class="contact-row-label">客服电话</text>
                  <text class="contact-row-value">138-0000-0000</text>
                </view>
                <text class="contact-action">拨打</text>
              </view>
              <view class="contact-row" @tap="copyText('寻野酒吧')">
                <text class="contact-row-icon">💬</text>
                <view class="contact-row-info">
                  <text class="contact-row-label">微信公众号</text>
                  <text class="contact-row-value">寻野酒吧</text>
                </view>
                <text class="contact-action copy">复制</text>
              </view>
              <view class="contact-row">
                <text class="contact-row-icon">📍</text>
                <view class="contact-row-info">
                  <text class="contact-row-label">门店地址</text>
                  <text class="contact-row-value">示例街道 88 号</text>
                </view>
              </view>
            </view>

            <!-- 营业信息 -->
            <view class="section">
              <text class="section-title">营业信息</text>
              <view class="info-row">
                <text class="info-label">营业时间</text>
                <text class="info-value">18:00 - 02:00</text>
              </view>
              <view class="info-row">
                <text class="info-label">门店电话</text>
                <text class="info-value">138-0000-0000</text>
              </view>
              <view class="info-row last">
                <text class="info-label">温馨提示</text>
                <text class="info-value muted">未成年人禁止饮酒，请理性消费</text>
              </view>
            </view>

            <view style="height: 20px" />
          </scroll-view>
        </view>

        <view class="toast" :class="{ show: toastVisible }">
          {{ toastText }}
        </view>
      </view>
    </view>
  </view>
</template>

<style lang="scss" scoped>
button {
  padding: 0;
  margin: 0;
  color: inherit;
  font: inherit;
  line-height: 1;
  background: transparent;
  border: 0;
}

.prototype-page {
  min-height: 100vh;
  background: #f0f2f5;
  display: flex;
  justify-content: center;
  align-items: center;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif;
}

.phone-shell {
  width: min(100vw, 430px);
  height: min(100vh, 932px);
  max-height: 932px;
  background: #090909;
  position: relative;
  overflow: hidden;
  border: 8px solid #222;
  border-radius: 40px;
  box-shadow: 0 25px 50px rgba(0, 0, 0, 0.5);
}

.notch {
  position: absolute;
  top: 0;
  left: 50%;
  z-index: 99;
  width: 150px;
  height: 30px;
  transform: translateX(-50%);
  background: #222;
  border-bottom-right-radius: 20px;
  border-bottom-left-radius: 20px;
}

.app-screen {
  position: relative;
  height: 100%;
  overflow: hidden;
  color: #fff;
  background: #090909;
}

.view {
  position: absolute;
  inset: 0;
  z-index: 2;
  display: flex;
  flex-direction: column;
  background: #090909;
}

.view-scroll {
  overflow-y: auto;
  padding-bottom: 110px;
}

.brand-head {
  padding: 56px 24px 16px;
  background: linear-gradient(180deg, #161616 0%, #090909 100%);
}

.brand-title {
  color: #c79f62;
  font-size: 30px;
  font-weight: 800;
  letter-spacing: 3px;
}

.gold {
  color: #c79f62;
}

.muted {
  color: #858585;
}

.small {
  font-size: 13px;
}

.mini {
  font-size: 11px;
}

.tiny {
  font-size: 10px;
}

.bold {
  font-weight: 700;
}

.notice-pill {
  display: flex;
  align-items: center;
  gap: 10px;
  margin: 8px 16px;
  padding: 9px 16px;
  background: #161616;
  border: 1px solid rgba(199, 159, 98, 0.2);
  border-radius: 999px;
}

.notice-text {
  flex: 1;
  overflow: hidden;
  color: #d4d4d4;
  font-size: 13px;
  white-space: nowrap;
  text-overflow: ellipsis;
}

.table-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin: 16px;
  padding: 24px;
  background: #161616;
  border-radius: 16px;
}

.table-code {
  margin-top: 4px;
  font-size: 24px;
  font-weight: 800;
}

.gold-button {
  display: flex;
  align-items: center;
  justify-content: center;
  color: #090909;
  font-weight: 800;
  background: #c79f62;
  border-radius: 999px;
}

.small-button {
  height: 34px;
  padding: 0 16px;
  font-size: 13px;
}

.action-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
  margin: 24px 16px 0;
}

.action-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 20px;
  background: #161616;
  border: 1px solid #262626;
  border-radius: 12px;
}

.action-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 48px;
  height: 48px;
  margin-bottom: 8px;
  border-radius: 999px;
}

.gold-bg {
  background: rgba(199, 159, 98, 0.18);
}

.blue-bg {
  background: rgba(59, 130, 246, 0.18);
}

.section {
  margin-top: 32px;
  padding: 0 16px;
}

.section-title {
  margin-bottom: 16px;
  font-size: 18px;
  font-weight: 800;
}

.recommend-scroll {
  width: 100%;
  white-space: nowrap;
}

.recommend-row {
  display: flex;
  gap: 16px;
  padding-bottom: 16px;
}

.recommend-card {
  width: 140px;
  overflow: hidden;
  background: #161616;
  border: 1px solid #262626;
  border-radius: 12px;
}

.recommend-img {
  width: 140px;
  height: 128px;
  background: #2a2a2a;
}

.recommend-info {
  padding: 12px;
}

.product-name {
  font-size: 14px;
  font-weight: 800;
}

.truncate {
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
}

.price-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 10px;
}

.price {
  color: #c79f62;
  font-size: 15px;
  font-weight: 800;
}

.round-plus,
.minus {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 26px;
  height: 26px;
  color: #090909;
  font-weight: 900;
  background: #c79f62;
  border-radius: 999px;
}

.minus {
  color: #c79f62;
  background: transparent;
  border: 1px solid #c79f62;
}

.gray {
  color: #858585;
  border-color: #858585;
}

.topbar,
.orders-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  min-height: 44px;
  padding: 54px 16px 12px;
  background: #090909;
  border-bottom: 1px solid #262626;
}

.top-title,
.simple-head {
  font-size: 18px;
  font-weight: 800;
}

.simple-head {
  padding: 56px 16px 16px;
  text-align: center;
  border-bottom: 1px solid #262626;
}

.icon-button {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  font-size: 24px;
}

.icon-button.disabled {
  color: #3a3a3a;
}

.ghost {
  opacity: 0;
}

.content {
  box-sizing: border-box;
  width: 100%;
  flex: 1;
  padding: 24px 16px 110px;
  overflow: hidden;
}

.scan-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 32px;
  margin-bottom: 28px;
  background: #161616;
  border: 1px dashed rgba(133, 133, 133, 0.5);
  border-radius: 16px;
}

.scan-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 80px;
  height: 80px;
  margin-bottom: 14px;
  color: #c79f62;
  font-size: 40px;
  background: rgba(199, 159, 98, 0.1);
  border-radius: 999px;
}

.sub-title {
  margin: 18px 0 12px;
  color: #858585;
  font-size: 13px;
  font-weight: 800;
}

.table-input-row {
  display: flex;
  gap: 12px;
}

.table-input {
  box-sizing: border-box;
  flex: 1;
  height: 48px;
  padding: 0 16px;
  color: #fff;
  background: #161616;
  border: 1px solid #262626;
  border-radius: 12px;
}

.placeholder {
  color: #555;
}

.confirm-button {
  width: 76px;
  height: 48px;
  border-radius: 12px;
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
  gap: 5px;
  padding: 16px;
  background: #161616;
  border: 1px solid #262626;
  border-radius: 12px;
}

.table-cell.active {
  background: rgba(199, 159, 98, 0.05);
  border-color: #c79f62;
}

.recent {
  position: absolute;
  top: -8px;
  right: -8px;
  padding: 2px 8px;
  font-size: 10px;
  background: #ef4444;
  border-radius: 999px;
}

.table-no {
  font-size: 20px;
  font-weight: 800;
}

.menu-head {
  padding: 56px 16px 10px;
  border-bottom: 1px solid #262626;
}

.menu-brand {
  margin-bottom: 18px;
  font-size: 18px;
  font-weight: 800;
  text-align: center;
}

.menu-status {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.table-pill {
  padding: 7px 12px;
  font-size: 12px;
  font-weight: 800;
  background: #161616;
  border: 1px solid #262626;
  border-radius: 999px;
}

.search-dot {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  background: #161616;
  border-radius: 999px;
}

.menu-layout {
  display: flex;
  flex: 1;
  min-height: 0;
}

.category-list {
  width: 96px;
  height: 100%;
  background: #161616;
}

.category-item {
  padding: 16px 6px;
  color: #858585;
  font-size: 13px;
  text-align: center;
  border-left: 4px solid transparent;
}

.category-item.selected {
  color: #c79f62;
  font-weight: 800;
  background: #090909;
  border-left-color: #c79f62;
}

.product-list {
  box-sizing: border-box;
  flex: 1;
  height: 100%;
  padding: 12px 12px 170px;
}

.category-label {
  margin-bottom: 12px;
  color: #858585;
  font-size: 12px;
}

.product-item {
  display: flex;
  gap: 12px;
  padding: 8px;
  margin-bottom: 18px;
  background: rgba(22, 22, 22, 0.5);
  border-radius: 12px;
}

.product-img {
  width: 80px;
  height: 80px;
  background: #2a2a2a;
  border-radius: 8px;
}

.product-main {
  display: flex;
  flex: 1;
  flex-direction: column;
  justify-content: space-between;
}

.product-desc {
  margin-top: 6px;
  color: #858585;
  font-size: 10px;
}

.stepper {
  display: flex;
  align-items: center;
  gap: 8px;
}

.qty {
  min-width: 14px;
  font-size: 14px;
  font-weight: 800;
  text-align: center;
}

.cart-float {
  position: absolute;
  right: 16px;
  bottom: 92px;
  left: 16px;
  display: flex;
  align-items: center;
  gap: 12px;
  height: 56px;
  padding: 0 8px;
  background: #161616;
  border: 1px solid #262626;
  border-radius: 999px;
  box-shadow: 0 14px 35px rgba(0, 0, 0, 0.5);
  transition: transform 0.25s, opacity 0.25s;
}

.cart-float.hidden {
  pointer-events: none;
  opacity: 0;
  transform: translateY(90px);
}

.cart-icon {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 48px;
  height: 48px;
  background: #090909;
  border: 2px solid #c79f62;
  border-radius: 999px;
}

.cart-badge {
  position: absolute;
  top: -6px;
  right: -4px;
  min-width: 16px;
  height: 16px;
  color: #090909;
  font-size: 10px;
  line-height: 16px;
  text-align: center;
  background: #c79f62;
  border-radius: 999px;
}

.cart-price,
.pay-total,
.order-amount {
  color: #c79f62;
  font-size: 18px;
  font-weight: 900;
}

.checkout-small {
  width: 92px;
  height: 40px;
  margin-left: auto;
}

.summary-card,
.panel,
.message-card,
.profile-card,
.stats-card {
  box-sizing: border-box;
  width: 100%;
  padding: 16px;
  margin-bottom: 16px;
  overflow: hidden;
  background: #161616;
  border: 1px solid #262626;
  border-radius: 12px;
}

.summary-card {
  display: flex;
  align-items: center;
  gap: 12px;
}

.summary-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 42px;
  height: 42px;
  background: rgba(199, 159, 98, 0.12);
  border-radius: 999px;
}

.panel-title,
.order-panel-head {
  padding-bottom: 12px;
  margin-bottom: 12px;
  font-weight: 800;
  border-bottom: 1px solid #262626;
}

.cart-line,
.detail-line,
.info-line,
.mine-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 14px;
  padding: 10px 0;
  color: #d6d6d6;
  font-size: 13px;
  border-bottom: 1px solid #262626;
}

.info-line > text:first-child,
.detail-line > text:first-child,
.mine-row > text:first-child {
  flex: 0 0 auto;
}

.info-line > text:last-child,
.detail-line > text:last-child,
.mine-row > text:last-child {
  min-width: 0;
  flex: 1;
  text-align: right;
  overflow-wrap: anywhere;
}

.cart-line:last-child,
.detail-line:last-child,
.info-line:last-child,
.mine-row:last-child {
  border-bottom: 0;
}

.empty-line {
  padding: 22px 0;
  color: #858585;
  font-size: 13px;
  text-align: center;
}

.remark-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 14px;
}

.remark-input {
  flex: 1;
  color: #fff;
  font-size: 13px;
  text-align: right;
}

.bottom-pay {
  position: absolute;
  right: 0;
  bottom: 0;
  left: 0;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 16px 34px;
  background: #161616;
  border-top: 1px solid #262626;
}

.submit-button {
  width: 160px;
  height: 48px;
  font-size: 18px;
}

.payment-body {
  display: flex;
  flex: 1;
  flex-direction: column;
  align-items: center;
  padding: 56px 16px 110px;
}

.pay-amount {
  margin-bottom: 8px;
  color: #c79f62;
  font-size: 42px;
  font-weight: 900;
}

.wide {
  width: 100%;
  box-sizing: border-box;
  margin-top: 34px;
}

.pay-method {
  display: flex;
  align-items: center;
  justify-content: space-between;
  box-sizing: border-box;
  width: 100%;
  padding: 16px;
  margin-bottom: 12px;
  background: #161616;
  border: 1px solid #262626;
  border-radius: 12px;
}

.pay-method.active {
  border-color: rgba(199, 159, 98, 0.35);
}

.muted-method {
  opacity: 0.5;
}

.radio-active,
.radio {
  width: 20px;
  height: 20px;
  border-radius: 999px;
}

.radio-active {
  background: #090909;
  border: 5px solid #c79f62;
}

.radio {
  border: 1px solid #858585;
}

.full-button {
  width: 100%;
  height: 48px;
  font-size: 18px;
}

.result-view {
  align-items: center;
  justify-content: center;
  padding: 0 24px;
  box-sizing: border-box;
}

.success-circle {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 92px;
  height: 92px;
  margin-bottom: 20px;
  color: #090909;
  font-size: 42px;
  font-weight: 900;
  background: #c79f62;
  border-radius: 999px;
}

.result-title {
  margin-bottom: 10px;
  font-size: 24px;
  font-weight: 900;
}

.result-copy {
  margin-bottom: 28px;
  font-size: 14px;
  line-height: 1.7;
  text-align: center;
}

.result-actions {
  display: flex;
  gap: 12px;
  width: 100%;
}

.outline-button,
.outline-gold {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 46px;
  border-radius: 12px;
}

.outline-button {
  flex: 1;
  border: 1px solid #858585;
}

.flex-button {
  flex: 1;
}

.status-banner {
  box-sizing: border-box;
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 20px;
  margin-bottom: 16px;
  overflow: hidden;
  background: linear-gradient(90deg, rgba(199, 159, 98, 0.2), #161616);
  border: 1px solid rgba(199, 159, 98, 0.3);
  border-radius: 12px;
}

.status-icon {
  font-size: 34px;
}

.table-tag,
.status-pill,
.message-tag {
  padding: 4px 8px;
  font-size: 11px;
  border-radius: 6px;
}

.table-tag {
  background: #090909;
  border: 1px solid #262626;
}

.detail-total {
  display: flex;
  justify-content: space-between;
  gap: 14px;
  padding-top: 12px;
  margin-top: 4px;
  border-top: 1px solid #262626;
}

.date-capsule {
  display: flex;
  align-items: center;
  justify-content: center;
  box-sizing: border-box;
  min-width: 112px;
  height: 36px;
  padding: 0 16px;
  color: #090909;
  font-size: 13px;
  font-weight: 800;
  line-height: 1;
  background: #c79f62;
  border-radius: 999px;
}

.date-capsule::after {
  display: none;
}

.date-capsule-inner {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  width: 100%;
  height: 100%;
  line-height: 1;
}

.button-center {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
  line-height: 1;
}

.date-arrow {
  font-size: 10px;
  transform: translateY(-1px);
}

.filter-bar {
  display: flex;
  gap: 10px;
  padding: 12px 16px 8px;
  overflow-x: auto;
  background: #090909;
}

.chip {
  display: flex;
  align-items: center;
  justify-content: center;
  box-sizing: border-box;
  height: 30px;
  padding: 0 16px;
  color: #858585;
  font-size: 12px;
  font-weight: 700;
  line-height: 1;
  background: #090909;
  border: 1px solid #262626;
  border-radius: 999px;
  white-space: nowrap;
}

.chip.active {
  color: #090909;
  background: #c79f62;
  border-color: #c79f62;
}

.select-filter-row {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  align-items: center;
  gap: 8px;
  padding: 0 16px 12px;
  background: #090909;
}

.select-pill,
.reset-filter {
  display: flex;
  align-items: center;
  justify-content: center;
  box-sizing: border-box;
  height: 32px;
  min-width: 0;
  padding: 0 10px;
  color: #d6d6d6;
  font-size: 12px;
  font-weight: 700;
  line-height: 1;
  background: #161616;
  border: 1px solid #262626;
  border-radius: 999px;
  white-space: nowrap;
}

.select-pill {
  width: 100%;
}

.reset-filter {
  color: #c79f62;
  background: transparent;
  border-color: rgba(199, 159, 98, 0.45);
}

.product-select {
  grid-column: span 2;
  max-width: none;
  overflow: hidden;
  text-overflow: ellipsis;
}

.orders-list {
  flex: 1;
  padding: 0 16px 100px;
  box-sizing: border-box;
}

.order-card {
  padding: 16px;
  margin-bottom: 14px;
  background: #161616;
  border: 1px solid #262626;
  border-radius: 14px;
}

.order-card-top,
.order-card-bottom,
.message-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.status-pill,
.message-tag {
  color: #c79f62;
  background: rgba(199, 159, 98, 0.18);
}

.order-items {
  padding: 14px 0;
  color: #d6d6d6;
  font-size: 13px;
}

.orders-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 360px;
  color: #858585;
}

.empty-icon {
  margin-bottom: 12px;
  font-size: 46px;
}

.outline-gold {
  height: 38px;
  padding: 0 24px;
  margin-top: 16px;
  color: #c79f62;
  border: 1px solid #c79f62;
  border-radius: 999px;
}

.message-card {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.profile-card {
  display: flex;
  align-items: center;
  gap: 16px;
}

.avatar {
  width: 64px;
  height: 64px;
  border: 2px solid #c79f62;
  border-radius: 999px;
}

.profile-name {
  font-size: 18px;
}

.stats-card {
  display: flex;
  align-items: center;
  justify-content: space-around;
}

.stat-num {
  font-size: 20px;
  font-weight: 900;
  text-align: center;
}

.divider {
  width: 1px;
  height: 32px;
  background: #262626;
}

.tabbar {
  position: absolute;
  right: 0;
  bottom: 0;
  left: 0;
  z-index: 120;
  display: flex;
  justify-content: space-around;
  height: 80px;
  padding: 8px 8px 34px;
  box-sizing: border-box;
  background: #090909;
  border-top: 1px solid #161616;
}

.tab-item {
  display: flex;
  flex: 1;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 3px;
  min-width: 0;
  height: 100%;
  padding: 0;
  color: #858585;
  font-size: 10px;
  line-height: 1;
  background: transparent;
  border: 0;
  border-radius: 0;
}

.tab-item::after {
  display: none;
}

.tab-item.active {
  color: #c79f62;
}

.tab-icon {
  font-size: 18px;
  line-height: 1;
}

.sheet-mask {
  position: absolute;
  inset: 0;
  z-index: 50;
  display: flex;
  align-items: flex-end;
  background: rgba(0, 0, 0, 0.68);
  backdrop-filter: blur(8px);
}

.date-panel {
  width: 100%;
  padding: 10px 18px 34px;
  box-sizing: border-box;
  background: rgba(22, 22, 22, 0.96);
  border-top: 1px solid #262626;
  border-radius: 26px 26px 0 0;
}

.compact-mask {
  background: rgba(0, 0, 0, 0.55);
}

.filter-panel {
  box-sizing: border-box;
  width: 100%;
  padding: 10px 18px 34px;
  background: rgba(22, 22, 22, 0.98);
  border-top: 1px solid #262626;
  border-radius: 24px 24px 0 0;
  box-shadow: 0 -18px 40px rgba(0, 0, 0, 0.45);
}

.filter-panel-title {
  margin-bottom: 16px;
  font-size: 16px;
  font-weight: 800;
  text-align: center;
}

.filter-options {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.filter-option {
  display: flex;
  align-items: center;
  justify-content: space-between;
  box-sizing: border-box;
  width: 100%;
  height: 48px;
  padding: 0 16px;
  color: #d6d6d6;
  font-size: 14px;
  font-weight: 700;
  background: #090909;
  border: 1px solid #262626;
  border-radius: 14px;
}

.filter-option.active {
  color: #c79f62;
  background: rgba(199, 159, 98, 0.08);
  border-color: rgba(199, 159, 98, 0.5);
}

.sheet-grip {
  width: 42px;
  height: 4px;
  margin: 0 auto 16px;
  background: #3a3a3a;
  border-radius: 999px;
}

.sheet-head,
.year-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.plain-button {
  color: #858585;
  font-size: 13px;
}

.year-row {
  padding: 8px 12px;
  margin: 20px 0 18px;
  font-size: 14px;
  font-weight: 800;
  background: #090909;
  border: 1px solid #262626;
  border-radius: 16px;
}

.month-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
}

.month-cell {
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 76px;
  background: #090909;
  border: 1px solid #262626;
  border-radius: 16px;
}

.month-cell.active {
  background: rgba(199, 159, 98, 0.06);
  border-color: rgba(199, 159, 98, 0.45);
}

.month-cell.disabled {
  opacity: 0.52;
}

.month-cell.future {
  pointer-events: none;
  opacity: 0.28;
}

.month-cell.future .month-name,
.month-cell.future .month-tip {
  color: #4a4a4a;
}

.month-dot {
  position: absolute;
  top: 8px;
  right: 10px;
  width: 8px;
  height: 8px;
  background: #c79f62;
  border-radius: 999px;
}

.month-name {
  font-size: 16px;
  font-weight: 900;
}

.month-tip {
  margin-top: 6px;
  color: #858585;
  font-size: 10px;
}

.month-cell.active .month-name,
.month-cell.active .month-tip {
  color: #c79f62;
}

.sheet-note {
  margin-top: 20px;
  color: #858585;
  font-size: 11px;
  text-align: center;
}

.inline-dot {
  display: inline-block;
  width: 6px;
  height: 6px;
  margin-right: 5px;
  background: #c79f62;
  border-radius: 999px;
}

.toast {
  position: absolute;
  top: 50%;
  left: 50%;
  z-index: 80;
  max-width: 260px;
  padding: 10px 16px;
  color: #fff;
  font-size: 13px;
  text-align: center;
  pointer-events: none;
  background: rgba(0, 0, 0, 0.78);
  border-radius: 999px;
  opacity: 0;
  transform: translate(-50%, -50%);
  transition: opacity 0.2s;
}

.toast.show {
  opacity: 1;
}

@media (max-width: 460px) {
  .prototype-page {
    align-items: stretch;
    background: #090909;
  }

  .phone-shell {
    width: 100vw;
    height: 100vh;
    max-height: none;
    border: 0;
    border-radius: 0;
    box-shadow: none;
  }
}

/* ===== 会员权益 ===== */
.member-card {
  display: flex;
  align-items: center;
  padding: 18px 16px;
  margin: 12px 14px;
  background: linear-gradient(135deg, rgba(199,159,98,0.2), rgba(199,159,98,0.05));
  border: 1px solid rgba(199,159,98,0.3);
  border-radius: 16px;
}
.member-avatar {
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  background: rgba(199,159,98,0.15);
  border-radius: 50%;
  color: #c79f62;
  margin-right: 12px;
}
.member-info {
  flex: 1;
}
.member-name {
  font-size: 16px;
  font-weight: 700;
  color: #f4ebdd;
  display: block;
}
.member-level-badge {
  display: inline-block;
  margin-top: 4px;
  padding: 2px 10px;
  font-size: 10px;
  color: #c79f62;
  background: rgba(199,159,98,0.15);
  border-radius: 999px;
}
.member-points {
  text-align: right;
}
.points-num {
  font-size: 22px;
  font-weight: 900;
  color: #c79f62;
  display: block;
}
.points-label {
  font-size: 10px;
  color: #858585;
}
.section {
  margin: 12px 14px;
}
.section-title {
  font-size: 14px;
  font-weight: 700;
  color: #f4ebdd;
  display: block;
  margin-bottom: 10px;
}
.level-card {
  padding: 14px;
  margin-bottom: 8px;
  background: #161616;
  border: 1px solid #262626;
  border-radius: 12px;
}
.level-card.level-active {
  border-color: #c79f62;
  background: rgba(199,159,98,0.06);
}
.level-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}
.level-name {
  font-size: 14px;
  font-weight: 700;
  color: #f4ebdd;
}
.level-card.level-active .level-name {
  color: #c79f62;
}
.level-requirement {
  font-size: 10px;
  color: #858585;
}
.level-benefits {
  display: flex;
  gap: 8px;
}
.benefit-item {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 4px 10px;
  background: rgba(255,255,255,0.04);
  border-radius: 8px;
}
.benefit-icon {
  font-size: 12px;
  color: #c79f62;
}
.benefit-text {
  font-size: 11px;
  color: #af9f8b;
}
.balance-row {
  display: flex;
  align-items: center;
  padding: 16px;
  background: #161616;
  border: 1px solid #262626;
  border-radius: 12px;
}
.balance-item {
  flex: 1;
  text-align: center;
}
.balance-num {
  font-size: 20px;
  font-weight: 900;
  display: block;
}
.balance-num.gold {
  color: #c79f62;
}
.balance-label {
  font-size: 10px;
  color: #858585;
  margin-top: 4px;
  display: block;
}
.balance-divider {
  width: 1px;
  height: 36px;
  background: #262626;
}
.activity-card {
  padding: 12px 14px;
  margin-bottom: 8px;
  background: #161616;
  border: 1px solid #262626;
  border-radius: 12px;
}
.activity-title {
  font-size: 13px;
  font-weight: 600;
  color: #f4ebdd;
  display: block;
  margin-bottom: 4px;
}
.activity-desc {
  font-size: 11px;
  color: #858585;
  display: block;
}
.empty-hint {
  padding: 20px;
  text-align: center;
  color: #858585;
  font-size: 12px;
}
/* ===== 联系客服 ===== */
.contact-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 24px;
  background: #161616;
  border: 1px solid #262626;
  border-radius: 16px;
}
.contact-icon-wrap {
  width: 56px;
  height: 56px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  background: rgba(199,159,98,0.15);
  border-radius: 50%;
  margin-bottom: 10px;
}
.contact-name {
  font-size: 16px;
  font-weight: 700;
  color: #f4ebdd;
}
.contact-desc {
  font-size: 11px;
  color: #858585;
  margin-top: 4px;
}
.contact-row {
  display: flex;
  align-items: center;
  padding: 14px 16px;
  background: #161616;
  border: 1px solid #262626;
  border-radius: 12px;
  margin-bottom: 8px;
}
.contact-row-icon {
  font-size: 20px;
  margin-right: 12px;
}
.contact-row-info {
  flex: 1;
}
.contact-row-label {
  font-size: 11px;
  color: #858585;
  display: block;
}
.contact-row-value {
  font-size: 13px;
  color: #f4ebdd;
  font-weight: 500;
  margin-top: 2px;
  display: block;
}
.contact-action {
  padding: 6px 14px;
  font-size: 12px;
  color: #c79f62;
  background: rgba(199,159,98,0.15);
  border-radius: 8px;
}
.contact-action.copy {
  color: #af9f8b;
  background: rgba(255,255,255,0.06);
}
.info-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  background: #161616;
  border: 1px solid #262626;
  border-bottom: 0;
  border-radius: 12px 12px 0 0;
}
.info-row.last {
  border-bottom: 1px solid #262626;
  border-radius: 0 0 12px 12px;
}
.info-label {
  font-size: 12px;
  color: #858585;
}
.info-value {
  font-size: 12px;
  color: #f4ebdd;
}
.info-value.muted {
  color: #858585;
}

/* #ifdef MP-WEIXIN */
.prototype-page {
  display: block;
  min-height: 100vh;
  padding: 0;
  background: #090909;
}

.phone-shell {
  width: 100vw;
  height: 100vh;
  max-height: none;
  border: 0;
  border-radius: 0;
  box-shadow: none;
}

.notch {
  display: none;
}
/* #endif */
</style>
