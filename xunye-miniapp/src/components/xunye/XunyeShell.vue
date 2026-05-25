<script lang="ts" setup>
import type { ViewName } from '@/composables/useShellState'
import { computed, ref, watch } from 'vue'
import { mainTabs, useShellState } from '@/composables/useShellState'
import { useXunyeStore } from '@/store'
import CartView from '@/views/CartView.vue'
import ContactView from '@/views/ContactView.vue'
import CouponsView from '@/views/CouponsView.vue'
import IndexView from '@/views/IndexView.vue'
import MembershipView from '@/views/MembershipView.vue'
import MenuView from '@/views/MenuView.vue'
import MessageView from '@/views/MessageView.vue'
import MineView from '@/views/MineView.vue'
import OrderDetailView from '@/views/OrderDetailView.vue'
import OrderResultView from '@/views/OrderResultView.vue'
import OrdersView from '@/views/OrdersView.vue'
import PaymentView from '@/views/PaymentView.vue'
import PointsView from '@/views/PointsView.vue'
import ProfileView from '@/views/ProfileView.vue'
import TableView from '@/views/TableView.vue'

defineOptions({ name: 'XunyeShell' })

const props = withDefaults(defineProps<{
  initialView?: ViewName
}>(), {
  initialView: 'index',
})

const {
  activeView,
  toastText,
  toastVisible,
  push,
  switchTab,
  handleTabTap,
} = useShellState()
const store = useXunyeStore()
const showCartFloat = computed(() => ['index', 'menu'].includes(activeView.value) && store.totalQty > 0)

const tabItems: Array<{ view: ViewName, label: string, icon: string, activeIcon: string }> = [
  { view: 'index', label: '首页', icon: 'home', activeIcon: 'home-fill' },
  { view: 'menu', label: '我要喝', icon: 'grid', activeIcon: 'grid-fill' },
  { view: 'orders', label: '我买过', icon: 'order', activeIcon: 'file-text-fill' },
  { view: 'message', label: '我得看', icon: 'email', activeIcon: 'email-fill' },
  { view: 'mine', label: '我？', icon: 'account', activeIcon: 'account-fill' },
]

const shellVars = computed(() => {
  const vars = {
    '--xunye-safe-top': '44px',
    '--xunye-menu-width': '0px',
    '--xunye-menu-right': '0px',
    '--xunye-menu-bottom': '0px',
  }

  // #ifdef MP-WEIXIN
  try {
    const windowInfo = uni.getWindowInfo()
    const menuButton = uni.getMenuButtonBoundingClientRect()
    vars['--xunye-safe-top'] = `${windowInfo.statusBarHeight || windowInfo.safeArea?.top || 44}px`
    vars['--xunye-menu-width'] = `${menuButton.width || 0}px`
    vars['--xunye-menu-right'] = `${Math.max(0, windowInfo.windowWidth - menuButton.right)}px`
    vars['--xunye-menu-bottom'] = `${menuButton.bottom || 0}px`
  }
  catch {
    // Keep the conservative defaults for unsupported runtimes.
  }
  // #endif

  return vars
})

const cartExpanded = ref(false)

watch(showCartFloat, (newVal) => {
  if (!newVal) {
    cartExpanded.value = false
  }
})

// 监听页面切换，关闭购物车弹窗
watch(activeView, () => {
  cartExpanded.value = false
})

function toggleCartExpanded() {
  if (store.totalQty > 0) {
    cartExpanded.value = !cartExpanded.value
  }
}

function handleClearCart() {
  uni.showModal({
    title: '提示',
    content: '确定要清空购物车吗？',
    confirmColor: '#d2a85f',
    success: (res) => {
      if (res.confirm) {
        store.clearCart()
        cartExpanded.value = false
        uni.showToast({
          title: '已清空购物车',
          icon: 'none',
        })
      }
    },
  })
}

switchTab(props.initialView)
</script>

<template>
  <view class="prototype-page">
    <view class="phone-shell">
      <view class="notch" />
      <view class="app-screen" :style="shellVars">
        <!-- 5 个主 tab 用 v-show 保活，避免 tab 切换时销毁重建 -->
        <IndexView v-show="activeView === 'index'" />
        <MenuView v-show="activeView === 'menu'" />
        <OrdersView v-show="activeView === 'orders'" />
        <MessageView v-show="activeView === 'message'" />
        <MineView v-show="activeView === 'mine'" />
        <!-- 导航/详情页生命周期短，用 v-if 按需创建 -->
        <TableView v-if="activeView === 'table'" />
        <CartView v-if="activeView === 'cart'" />
        <PaymentView v-if="activeView === 'payment'" />
        <OrderResultView v-if="activeView === 'orderResult'" />
        <OrderDetailView v-if="activeView === 'orderDetail'" />
        <MembershipView v-if="activeView === 'membership'" />
        <ProfileView v-if="activeView === 'profile'" />
        <PointsView v-if="activeView === 'points'" />
        <CouponsView v-if="activeView === 'coupons'" />
        <ContactView v-if="activeView === 'contact'" />

        <!-- 购物车遮罩层 -->
        <view
          v-if="cartExpanded && showCartFloat"
          class="cart-popup-mask"
          @tap="cartExpanded = false"
        />

        <!-- 已选商品弹窗内容 -->
        <view
          v-if="cartExpanded && showCartFloat"
          class="cart-popup-content"
        >
          <view class="cart-popup-header">
            <text class="cart-popup-title">已选商品</text>
            <button class="cart-clear-btn" hover-class="none" @tap="handleClearCart">
              <uv-icon name="trash" color="#8d929d" size="14" />
              <text class="clear-text">清空购物车</text>
            </button>
          </view>
          <scroll-view class="cart-popup-list" scroll-y>
            <view v-for="item in store.cartItems" :key="item.id" class="cart-popup-item">
              <view class="item-info">
                <view class="item-name">
                  {{ item.name }}
                </view>
                <view class="item-price">
                  ¥{{ item.price }}
                </view>
              </view>
              <view class="stepper">
                <button class="minus" @tap="store.decreaseProduct(item.id)">
                  <uv-icon name="minus" color="#f7f1e8" size="13" />
                </button>
                <text class="qty">{{ item.qty }}</text>
                <button class="round-plus" @tap="store.addProduct(item)">
                  <uv-icon name="plus" color="#111318" size="13" />
                </button>
              </view>
            </view>
          </scroll-view>
        </view>

        <view v-if="showCartFloat" class="cart-float" @tap="toggleCartExpanded">
          <view class="cart-icon">
            <uv-icon name="shopping-cart-fill" color="#d2a85f" size="22" />
            <uv-badge class="cart-badge" :value="store.totalQty" bg-color="#d2a85f" color="#111318" />
          </view>
          <view>
            <view class="cart-price">
              ¥{{ store.totalAmount.toFixed(2) }}
            </view>
            <view class="muted tiny">
              另需支付服务费 ¥0
            </view>
          </view>
          <view class="checkout-small">
            <uv-button
              text="去结算"
              color="linear-gradient(135deg, #d2a85f, #bc8945)"
              shape="circle"
              size="small"
              custom-style="height: 34px; padding: 0 18px; color: #111318; font-weight: 800;"
              @click.stop="push('cart')"
            />
          </view>
        </view>

        <view v-if="mainTabs.includes(activeView)" class="tabbar">
          <button
            v-for="item in tabItems"
            :key="item.view"
            class="tab-item"
            hover-class="none"
            :class="{ active: activeView === item.view }"
            @tap.stop="handleTabTap(item.view)"
          >
            <uv-icon
              :name="activeView === item.view ? item.activeIcon : item.icon"
              :color="activeView === item.view ? '#d2a85f' : '#8d929d'"
              size="21"
            />
            <text>{{ item.label }}</text>
          </button>
        </view>

        <view class="toast" :class="{ show: toastVisible }">
          {{ toastText }}
        </view>
      </view>
    </view>
  </view>
</template>

<style>
/* ========== Reset ========== */
button {
  padding: 0;
  margin: 0;
  color: inherit;
  font: inherit;
  line-height: 1;
  background: transparent;
  border: 0;
}

button::after {
  display: none;
}

view,
scroll-view,
swiper,
swiper-item,
image,
text,
button,
input,
textarea,
picker {
  box-sizing: border-box;
  min-width: 0;
  max-width: 100%;
}

scroll-view {
  display: block;
  width: 100%;
  overflow-x: hidden;
}

/* ========== Shell ========== */
.prototype-page {
  width: 100%;
  min-height: 100vh;
  overflow-x: hidden;
  background: #090909;
  display: block;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif;
}
.phone-shell {
  width: 100%;
  height: 100vh;
  max-height: none;
  background: #090909;
  position: relative;
  overflow: hidden;
  border: 0;
  border-radius: 0;
  box-shadow: none;
}
.notch {
  display: none;
}
.app-screen {
  --xunye-safe-top: 44px;
  --xunye-menu-width: 0px;
  --xunye-menu-right: 0px;
  --xunye-menu-bottom: 0px;
  --xunye-bg: #0b0c0f;
  --xunye-surface: #15171b;
  --xunye-surface-2: #1d2026;
  --xunye-line: rgba(255, 255, 255, 0.08);
  --xunye-text: #f7f1e8;
  --xunye-muted: #8d929d;
  --xunye-gold: #d2a85f;
  --xunye-gold-2: #bc8945;
  position: relative;
  width: 100%;
  max-width: 100%;
  height: 100%;
  overflow: hidden;
  color: var(--xunye-text);
  background:
    radial-gradient(circle at 15% 0%, rgba(210, 168, 95, 0.14), transparent 30%),
    linear-gradient(180deg, #101116 0%, var(--xunye-bg) 42%);
}

/* ========== View ========== */
.view {
  position: absolute;
  inset: 0;
  z-index: 2;
  display: flex;
  flex-direction: column;
  width: 100%;
  max-width: 100%;
  overflow: hidden;
  background: var(--xunye-bg);
}
.view-scroll {
  width: 100%;
  max-width: 100%;
  overflow-x: hidden;
  overflow-y: auto;
  padding-bottom: 110px;
}

/* ========== Typography ========== */
.gold {
  color: var(--xunye-gold);
}
.muted {
  color: var(--xunye-muted);
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
.truncate {
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
}

/* ========== Top Bar ========== */
.topbar {
  display: flex;
  align-items: center;
  box-sizing: border-box;
  width: 100%;
  max-width: 100%;
  overflow: hidden;
  padding: calc(var(--xunye-safe-top, 44px) + 6px) 14px 10px;
}
.top-title {
  flex: 1;
  text-align: center;
  font-size: 17px;
  font-weight: 600;
}
.icon-button {
  width: 38px;
  height: 38px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--xunye-text);
  font-size: 22px;
  background: rgba(255, 255, 255, 0.06);
  border-radius: 999px;
}
.icon-button.ghost {
  visibility: hidden;
}
.plain-button {
  font-size: 14px;
  color: var(--xunye-muted);
}
.plain-button.gold {
  color: var(--xunye-gold);
}
.content {
  box-sizing: border-box;
  flex: 1;
  width: 100%;
  max-width: 100%;
  padding: 0 16px;
  overflow-x: hidden;
  overflow-y: auto;
}

/* ========== Panel ========== */
.panel {
  background: rgba(21, 23, 27, 0.96);
  border: 1px solid var(--xunye-line);
  border-radius: 16px;
  padding: 14px 16px;
  margin-bottom: 12px;
}
.panel-title {
  font-size: 15px;
  font-weight: 600;
  margin-bottom: 6px;
}

/* ========== Buttons ========== */
.gold-button {
  background: linear-gradient(135deg, var(--xunye-gold), var(--xunye-gold-2));
  color: #111318;
  font-weight: 700;
  border: 0;
  padding: 8px 16px;
  border-radius: 999px;
}
.gold-button:active {
  opacity: 0.85;
}
.outline-button {
  border: 1px solid var(--xunye-line);
  border-radius: 10px;
  background: transparent;
  color: var(--xunye-muted);
  font-size: 14px;
  padding: 10px 0;
  text-align: center;
}
.outline-gold {
  border: 1px solid var(--xunye-gold);
  border-radius: 8px;
  color: var(--xunye-gold);
  font-size: 13px;
  padding: 8px 20px;
}
.small-button {
  padding: 8px 16px;
  border-radius: 999px;
  font-size: 13px;
}
.checkout-small {
  padding: 10px 20px;
  border-radius: 999px;
  font-size: 14px;
  font-weight: 600;
}
.submit-button {
  padding: 12px 32px;
  border-radius: 999px;
  font-size: 16px;
  font-weight: 600;
}
.confirm-button {
  padding: 12px 20px;
  border-radius: 10px;
  font-size: 14px;
  white-space: nowrap;
}
.flex-button {
  flex: 1;
  padding: 12px 0;
  border-radius: 10px;
  font-size: 14px;
  font-weight: 600;
}
.full-button {
  width: 100%;
  padding: 14px;
  border-radius: 12px;
  font-size: 16px;
  font-weight: 600;
}

/* ========== Price & Stepper ========== */
.price {
  color: var(--xunye-gold);
  font-size: 15px;
  font-weight: 700;
}
.stepper {
  display: flex;
  align-items: center;
  gap: 6px;
}
.minus {
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--xunye-surface-2);
  border-radius: 999px;
  font-size: 14px;
  color: #ccc;
  order: 3;
}
.round-plus {
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--xunye-gold);
  border-radius: 999px;
  font-size: 16px;
  color: #111318;
  font-weight: 700;
  order: 1;
}
.qty {
  font-size: 14px;
  font-weight: 600;
  min-width: 16px;
  text-align: center;
  order: 2;
}
.placeholder {
  color: rgba(247, 241, 232, 0.32);
}

/* ========== Floating Cart ========== */
.cart-float {
  position: absolute;
  right: 16px;
  bottom: calc(env(safe-area-inset-bottom, 8px) + 70px);
  left: 16px;
  z-index: 40;
  display: flex;
  align-items: center;
  gap: 10px;
  box-sizing: border-box;
  min-height: 58px;
  padding: 9px 10px;
  background: rgba(29, 32, 38, 0.96);
  border: 1px solid rgba(210, 168, 95, 0.2);
  border-radius: 20px;
  box-shadow: 0 14px 35px rgba(0, 0, 0, 0.45);
}
.cart-icon {
  position: relative;
  width: 44px;
  height: 44px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  background: #111318;
  border-radius: 999px;
  font-size: 20px;
}
.cart-badge {
  position: absolute;
  top: -2px;
  right: -2px;
}
.cart-price {
  font-size: 18px;
  font-weight: 700;
  color: var(--xunye-gold);
}
.checkout-small,
.cart-float .checkout-small {
  margin-left: auto;
  flex-shrink: 0;
}

/* ========== Tab Bar ========== */
.tabbar {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  z-index: 30;
  display: flex;
  background: rgba(13, 14, 17, 0.96);
  border-top: 1px solid var(--xunye-line);
  padding: 7px 0 env(safe-area-inset-bottom, 8px);
  backdrop-filter: blur(12px);
}
.tab-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
  font-size: 10px;
  color: var(--xunye-muted);
  background: transparent;
  border-radius: 0;
}
.tab-item.active {
  color: var(--xunye-gold);
}

/* ========== Toast ========== */
.toast {
  position: fixed;
  bottom: 120px;
  left: 50%;
  z-index: 100;
  transform: translateX(-50%) translateY(10px);
  padding: 10px 24px;
  background: rgba(0, 0, 0, 0.85);
  color: #fff;
  font-size: 14px;
  border-radius: 999px;
  opacity: 0;
  pointer-events: none;
  transition:
    opacity 0.25s ease,
    transform 0.25s ease;
  white-space: nowrap;
}
.toast.show {
  opacity: 1;
  transform: translateX(-50%) translateY(0);
}

/* ========== Bottom Pay ========== */
.bottom-pay {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px calc(env(safe-area-inset-bottom, 8px) + 12px);
  background: rgba(21, 23, 27, 0.98);
  border-top: 1px solid var(--xunye-line);
}
.pay-total {
  font-size: 22px;
  font-weight: 800;
  color: var(--xunye-gold);
}

/* ========== Info Line ========== */
.info-line {
  display: flex;
  justify-content: space-between;
  padding: 8px 0;
  font-size: 14px;
}

/* ========== Cart Popup ========== */
.cart-popup-mask {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 35;
  background: rgba(0, 0, 0, 0.65);
  backdrop-filter: blur(4px);
  animation: fade-in 0.25s ease forwards;
}

.cart-popup-content {
  position: absolute;
  left: 16px;
  right: 16px;
  bottom: calc(env(safe-area-inset-bottom, 8px) + 142px);
  z-index: 38;
  max-height: 45vh;
  background: #15171b;
  border: 1px solid rgba(210, 168, 95, 0.2);
  border-radius: 20px;
  padding: 16px 16px 12px;
  display: flex;
  flex-direction: column;
  box-shadow:
    0 -10px 30px rgba(0, 0, 0, 0.5),
    0 14px 35px rgba(0, 0, 0, 0.45);
  animation: slide-up 0.25s cubic-bezier(0.25, 1, 0.5, 1) forwards;
  will-change: transform, opacity;
  backface-visibility: hidden;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}

.cart-popup-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.06);
}

.cart-popup-title {
  font-size: 15px;
  font-weight: 700;
  color: var(--xunye-text);
  -webkit-font-smoothing: antialiased;
}

.cart-clear-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  color: var(--xunye-muted);
  font-size: 12px;
  background: transparent;
  padding: 0;
  margin: 0;
}

.clear-text {
  margin-left: 2px;
}

.cart-popup-list {
  height: auto;
  max-height: 240px;
}

.cart-popup-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 0;
  border-bottom: 1px solid rgba(255, 255, 255, 0.04);
}

.cart-popup-item:last-child {
  border-bottom: none;
}

.item-info {
  flex: 1;
  min-width: 0;
  margin-right: 12px;
}

.item-name {
  font-size: 14px;
  font-weight: 600;
  color: var(--xunye-text);
  margin-bottom: 4px;
  -webkit-font-smoothing: antialiased;
}

.item-price {
  font-size: 13px;
  font-weight: 700;
  color: var(--xunye-gold);
  -webkit-font-smoothing: antialiased;
}

@keyframes slide-up {
  from {
    transform: translate3d(0, 20px, 0);
    opacity: 0;
  }
  to {
    transform: translate3d(0, 0, 0);
    opacity: 1;
  }
}

@keyframes fade-in {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

/* #ifdef MP-WEIXIN */
.prototype-page {
  display: block;
  min-height: 100vh;
  background: #0b0c0f;
}

.phone-shell {
  width: 100%;
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
