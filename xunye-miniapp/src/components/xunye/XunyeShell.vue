<script lang="ts" setup>
import { computed } from 'vue'
import { useShellState, mainTabs, type ViewName } from '@/composables/useShellState'
import { useXunyeStore } from '@/store'
import CartView from '@/views/CartView.vue'
import ContactView from '@/views/ContactView.vue'
import IndexView from '@/views/IndexView.vue'
import MembershipView from '@/views/MembershipView.vue'
import MenuView from '@/views/MenuView.vue'
import MessageView from '@/views/MessageView.vue'
import MineView from '@/views/MineView.vue'
import OrderDetailView from '@/views/OrderDetailView.vue'
import OrderResultView from '@/views/OrderResultView.vue'
import OrdersView from '@/views/OrdersView.vue'
import PaymentView from '@/views/PaymentView.vue'
import CouponsView from '@/views/CouponsView.vue'
import PointsView from '@/views/PointsView.vue'
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

const tabItems: Array<{ view: ViewName; label: string; icon: string; activeIcon: string }> = [
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

switchTab(props.initialView)
</script>

<template>
  <view class="prototype-page">
    <view class="phone-shell">
      <view class="notch" />
      <view class="app-screen" :style="shellVars">
        <IndexView v-if="activeView === 'index'" />
        <TableView v-if="activeView === 'table'" />
        <MenuView v-if="activeView === 'menu'" />
        <CartView v-if="activeView === 'cart'" />
        <PaymentView v-if="activeView === 'payment'" />
        <OrderResultView v-if="activeView === 'orderResult'" />
        <OrderDetailView v-if="activeView === 'orderDetail'" />
        <OrdersView v-if="activeView === 'orders'" />
        <MessageView v-if="activeView === 'message'" />
        <MineView v-if="activeView === 'mine'" />
        <MembershipView v-if="activeView === 'membership'" />
        <PointsView v-if="activeView === 'points'" />
        <CouponsView v-if="activeView === 'coupons'" />
        <ContactView v-if="activeView === 'contact'" />

        <view v-if="showCartFloat" class="cart-float" @tap="push('cart')">
          <view class="cart-icon">
            <uv-icon name="shopping-cart-fill" color="#d2a85f" size="22" />
            <uv-badge class="cart-badge" :value="store.totalQty" bg-color="#d2a85f" color="#111318" />
          </view>
          <view>
            <view class="cart-price">¥{{ store.totalAmount.toFixed(2) }}</view>
            <view class="muted tiny">另需支付服务费 ¥0</view>
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

/* ========== Shell ========== */
.prototype-page {
  min-height: 100vh;
  background: #090909;
  display: block;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif;
}
.phone-shell {
  width: 100vw;
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
  background: var(--xunye-bg);
}
.view-scroll {
  overflow-y: auto;
  padding-bottom: 110px;
}

/* ========== Typography ========== */
.gold { color: var(--xunye-gold); }
.muted { color: var(--xunye-muted); }
.small { font-size: 13px; }
.mini { font-size: 11px; }
.tiny { font-size: 10px; }
.bold { font-weight: 700; }
.truncate {
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
}

/* ========== Top Bar ========== */
.topbar {
  display: flex;
  align-items: center;
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
.icon-button.ghost { visibility: hidden; }
.plain-button {
  font-size: 14px;
  color: var(--xunye-muted);
}
.plain-button.gold { color: var(--xunye-gold); }
.content {
  flex: 1;
  padding: 0 16px;
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
.gold-button:active { opacity: 0.85; }
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
.small-button { padding: 8px 16px; border-radius: 999px; font-size: 13px; }
.checkout-small { padding: 10px 20px; border-radius: 999px; font-size: 14px; font-weight: 600; }
.submit-button { padding: 12px 32px; border-radius: 999px; font-size: 16px; font-weight: 600; }
.confirm-button { padding: 12px 20px; border-radius: 10px; font-size: 14px; white-space: nowrap; }
.flex-button { flex: 1; padding: 12px 0; border-radius: 10px; font-size: 14px; font-weight: 600; }
.full-button { width: 100%; padding: 14px; border-radius: 12px; font-size: 16px; font-weight: 600; }

/* ========== Price & Stepper ========== */
.price { color: var(--xunye-gold); font-size: 15px; font-weight: 700; }
.stepper { display: flex; align-items: center; gap: 6px; }
.minus {
  width: 24px; height: 24px;
  display: flex; align-items: center; justify-content: center;
  background: var(--xunye-surface-2); border-radius: 999px;
  font-size: 14px; color: #ccc;
}
.round-plus {
  width: 24px; height: 24px;
  display: flex; align-items: center; justify-content: center;
  background: var(--xunye-gold); border-radius: 999px;
  font-size: 16px; color: #111318; font-weight: 700;
}
.qty { font-size: 14px; font-weight: 600; min-width: 16px; text-align: center; }
.placeholder { color: #555; }

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
.tab-item.active { color: var(--xunye-gold); }

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
  transition: opacity 0.25s ease, transform 0.25s ease;
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
.pay-total { font-size: 22px; font-weight: 800; color: var(--xunye-gold); }

/* ========== Info Line ========== */
.info-line {
  display: flex;
  justify-content: space-between;
  padding: 8px 0;
  font-size: 14px;
}

/* #ifdef MP-WEIXIN */
.prototype-page {
  display: block;
  min-height: 100vh;
  background: #0b0c0f;
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
