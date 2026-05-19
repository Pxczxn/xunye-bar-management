import { computed, ref } from 'vue'
import { defineStore } from 'pinia'

export interface TableInfo {
  area: string
  code: string
}

export interface MenuCategory {
  id: number
  name: string
}

export interface MenuProduct {
  id: number
  categoryId: number
  name: string
  description: string
  price: number
  image: string
}

export interface CartItem extends MenuProduct {
  qty: number
}

export interface CouponInfo {
  id: number
  title: string
  rule: string
  discountAmount: number
}

export interface OrderSnapshot {
  orderNo: string
  createdAt: string
  table: TableInfo | null
  items: CartItem[]
  originalAmount: number
  discountAmount: number
  totalAmount: number
  coupon: CouponInfo | null
  remark: string
  status: string
}

const productImages = {
  xunyeMist: '/static/images/products/xunye-mist.png',
  sunsetBoulevard: '/static/images/products/sunset-boulevard.png',
  truffleFries: '/static/images/products/black-truffle-fries.png',
  longIsland: '/static/images/products/long-island-iced-tea.png',
  macallan12: '/static/images/products/macallan-12.png',
  budweiser: '/static/images/products/budweiser-beer.png',
} as const

export const categories: MenuCategory[] = [
  { id: 1, name: '招牌特调' },
  { id: 2, name: '经典鸡尾酒' },
  { id: 3, name: '单一麦芽' },
  { id: 4, name: '精酿啤酒' },
  { id: 5, name: '佐酒小食' },
]

export const products: MenuProduct[] = [
  {
    id: 1,
    categoryId: 1,
    name: '寻野特调迷雾',
    description: '琴酒基底，融入迷迭香与接骨木花',
    price: 68,
    image: productImages.xunyeMist,
  },
  {
    id: 2,
    categoryId: 1,
    name: '日落大道',
    description: '龙舌兰、西柚汁与海盐，清爽微酸',
    price: 55,
    image: productImages.sunsetBoulevard,
  },
  {
    id: 3,
    categoryId: 5,
    name: '黑松露薯条',
    description: '酥脆薯条搭配特制黑松露酱',
    price: 38,
    image: productImages.truffleFries,
  },
  {
    id: 4,
    categoryId: 2,
    name: '长岛冰茶',
    description: '伏特加、朗姆与可乐的经典组合',
    price: 60,
    image: productImages.longIsland,
  },
  {
    id: 5,
    categoryId: 3,
    name: '麦卡伦12年单杯',
    description: '雪莉桶风味，顺滑带有果干香气',
    price: 88,
    image: productImages.macallan12,
  },
  {
    id: 6,
    categoryId: 4,
    name: '百威啤酒',
    description: '经典美式淡拉格，冰镇畅饮',
    price: 30,
    image: productImages.budweiser,
  },
]

function formatOrderNo() {
  const now = new Date()
  const date = `${now.getFullYear()}${String(now.getMonth() + 1).padStart(2, '0')}${String(now.getDate()).padStart(2, '0')}`
  const time = `${String(now.getHours()).padStart(2, '0')}${String(now.getMinutes()).padStart(2, '0')}${String(now.getSeconds()).padStart(2, '0')}`
  return `ORD${date}${time}`
}

export const useXunyeStore = defineStore(
  'xunye',
  () => {
    const currentTable = ref<TableInfo | null>(null)
    const cart = ref<Record<number, CartItem>>({})
    const activeCoupon = ref<CouponInfo | null>(null)
    const lastOrder = ref<OrderSnapshot | null>(null)

    const cartItems = computed(() => Object.values(cart.value))
    const totalQty = computed(() => cartItems.value.reduce((sum, item) => sum + item.qty, 0))
    const totalAmount = computed(() => cartItems.value.reduce((sum, item) => sum + item.price * item.qty, 0))
    const discountAmount = computed(() => {
      if (!activeCoupon.value || totalAmount.value <= 0) return 0
      return Math.min(activeCoupon.value.discountAmount, totalAmount.value)
    })
    const payableAmount = computed(() => Math.max(0, totalAmount.value - discountAmount.value))

    function selectTable(table: TableInfo) {
      currentTable.value = table
    }

    function getQty(productId: number) {
      return cart.value[productId]?.qty || 0
    }

    function addProduct(product: MenuProduct) {
      const old = cart.value[product.id]
      cart.value = {
        ...cart.value,
        [product.id]: {
          ...product,
          qty: (old?.qty || 0) + 1,
        },
      }
    }

    function decreaseProduct(productId: number) {
      const old = cart.value[productId]
      if (!old) {
        return
      }

      const next = { ...cart.value }
      if (old.qty <= 1) {
        delete next[productId]
      }
      else {
        next[productId] = { ...old, qty: old.qty - 1 }
      }
      cart.value = next
    }

    function clearCart() {
      cart.value = {}
    }

    function applyCoupon(coupon: CouponInfo) {
      activeCoupon.value = coupon
    }

    function removeCoupon() {
      activeCoupon.value = null
    }

    function createOrderSnapshot(remark = '') {
      const order: OrderSnapshot = {
        orderNo: formatOrderNo(),
        createdAt: '2026-05-16 21:30:15',
        table: currentTable.value,
        items: cartItems.value.map(item => ({ ...item })),
        originalAmount: totalAmount.value,
        discountAmount: discountAmount.value,
        totalAmount: payableAmount.value,
        coupon: activeCoupon.value ? { ...activeCoupon.value } : null,
        remark,
        status: '制作中',
      }
      lastOrder.value = order
      return order
    }

    function completePayment(remark = '') {
      const order = lastOrder.value || createOrderSnapshot(remark)
      clearCart()
      removeCoupon()
      return order
    }

    return {
      currentTable,
      cart,
      activeCoupon,
      lastOrder,
      cartItems,
      totalQty,
      totalAmount,
      discountAmount,
      payableAmount,
      selectTable,
      getQty,
      addProduct,
      decreaseProduct,
      clearCart,
      applyCoupon,
      removeCoupon,
      createOrderSnapshot,
      completePayment,
    }
  },
  {
    persist: true,
  },
)
