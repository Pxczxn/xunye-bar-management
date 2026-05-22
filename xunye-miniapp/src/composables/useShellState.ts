import { ref } from 'vue'

export type ViewName
  = | 'index'
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
    | 'profile'
    | 'points'
    | 'coupons'
    | 'contact'

export const mainTabs: ViewName[] = ['index', 'menu', 'orders', 'message', 'mine']

const pageRoutes: Partial<Record<ViewName, string>> = {
  index: '/pages/index/index',
  menu: '/pages/menu/menu',
  orders: '/pages/orders/orders',
  message: '/pages/message/message',
  mine: '/pages/mine/mine',
}

// Singleton state shared across all views
const activeView = ref<ViewName>('index')
const history = ref<ViewName[]>(['index'])
const toastText = ref('')
const toastVisible = ref(false)
let toastTimer: ReturnType<typeof setTimeout> | null = null
const menuAnchorProductId = ref<number | null>(null)

export function useShellState() {
  function showToast(message: string) {
    if (toastTimer)
      clearTimeout(toastTimer)
    toastText.value = message
    toastVisible.value = true
    toastTimer = setTimeout(() => {
      toastVisible.value = false
      toastTimer = null
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
    if (mainTabs.includes(view)) {
      switchTab(view)
      return
    }

    const url = pageRoutes[view]
    if (view === activeView.value) {
      switchTab(view)
      return
    }
    if (url) {
      uni.redirectTo({ url })
      return
    }
    show(view)
  }

  function handleTabTap(view: ViewName) {
    goPage(view)
  }

  function goMenuProduct(productId: number) {
    menuAnchorProductId.value = productId
    goPage('menu')
  }

  function consumeMenuAnchorProductId() {
    const productId = menuAnchorProductId.value
    menuAnchorProductId.value = null
    return productId
  }

  return {
    activeView,
    history,
    toastText,
    toastVisible,
    menuAnchorProductId,
    showToast,
    show,
    push,
    replace,
    back,
    switchTab,
    goPage,
    handleTabTap,
    goMenuProduct,
    consumeMenuAnchorProductId,
  }
}
