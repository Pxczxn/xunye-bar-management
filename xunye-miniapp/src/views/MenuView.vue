<script lang="ts" setup>
import type { CustomerCategoryVO, CustomerProductVO } from '@/api/customer'
import type { MenuProduct } from '@/store'
import { computed, nextTick, onMounted, ref } from 'vue'
import { listCustomerCategories, listCustomerProducts } from '@/api/customer'
import { useShellState } from '@/composables/useShellState'
import { useXunyeStore } from '@/store'

const store = useXunyeStore()
const { push, showToast, consumeMenuAnchorProductId } = useShellState()
const allCategoryId = 0
const categories = ref<CustomerCategoryVO[]>([])
const products = ref<MenuProduct[]>([])
const activeCategoryId = ref(allCategoryId)
const scrollIntoViewId = ref('')
const categoryOptions = computed(() => [{ id: allCategoryId, name: '全部' }, ...categories.value])
const currentCategoryName = computed(() => categoryOptions.value.find(item => item.id === activeCategoryId.value)?.name)
const visibleProducts = computed(() => {
  if (activeCategoryId.value === allCategoryId)
    return products.value
  return products.value.filter(item => item.categoryId === activeCategoryId.value)
})

onMounted(async () => {
  await fetchMenu()
  const productId = consumeMenuAnchorProductId()
  if (!productId)
    return
  activeCategoryId.value = allCategoryId
  nextTick(() => {
    scrollIntoViewId.value = `menu-product-${productId}`
  })
})

function toMenuProduct(product: CustomerProductVO): MenuProduct {
  return {
    id: product.id,
    categoryId: product.categoryId,
    name: product.name,
    description: product.description || '',
    price: Number(product.price || 0),
    image: product.imageUrl || '/static/images/products/xunye-mist.png',
  }
}

async function fetchMenu() {
  try {
    const [categoryList, productList] = await Promise.all([
      listCustomerCategories(),
      listCustomerProducts(),
    ])
    categories.value = categoryList
    products.value = productList.map(toMenuProduct)
  }
  catch {
    showToast('菜单读取失败')
  }
}

function ensureTable() {
  if (store.currentTable)
    return true
  showToast('请先选择桌台')
  setTimeout(() => push('table'), 700)
  return false
}

function addProduct(product: any) {
  if (!ensureTable())
    return
  store.addProduct(product)
}

function selectCategory(categoryId: number) {
  activeCategoryId.value = categoryId
  scrollIntoViewId.value = ''
}
</script>

<template>
  <view class="view">
    <view class="menu-head">
      <view class="menu-brand">
        寻野 XUNYE
      </view>
      <view class="menu-status">
        <view class="table-pill">
          <uv-icon name="map-fill" color="#d2a85f" size="14" />
          {{ store.currentTable?.code || '未选桌' }}
        </view>
        <view class="search-dot">
          <uv-icon name="search" color="#8d929d" size="16" />
        </view>
      </view>
    </view>
    <view class="menu-layout">
      <scroll-view class="category-list" scroll-y enhanced show-scrollbar="false">
        <view
          v-for="category in categoryOptions"
          :key="category.id"
          class="category-item"
          :class="{ selected: activeCategoryId === category.id }"
          @tap="selectCategory(category.id)"
        >
          {{ category.name }}
        </view>
      </scroll-view>
      <scroll-view class="product-list" scroll-y enhanced show-scrollbar="false" :scroll-into-view="scrollIntoViewId" scroll-with-animation>
        <view class="category-label">
          {{ currentCategoryName }}
        </view>
        <view
          v-for="product in visibleProducts"
          :id="`menu-product-${product.id}`"
          :key="product.id"
          class="product-item"
        >
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
              <text class="price">¥{{ product.price }}</text>
              <view class="stepper">
                <button v-if="store.getQty(product.id)" class="minus" @tap="store.decreaseProduct(product.id)">
                  <uv-icon name="minus" color="#f7f1e8" size="13" />
                </button>
                <text v-if="store.getQty(product.id)" class="qty">{{ store.getQty(product.id) }}</text>
                <button class="round-plus" @tap="addProduct(product)">
                  <uv-icon name="plus" color="#111318" size="13" />
                </button>
              </view>
            </view>
          </view>
        </view>
      </scroll-view>
    </view>
  </view>
</template>

<style scoped>
.menu-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: calc(var(--xunye-safe-top, 44px) + 8px) 16px 8px;
}
.menu-brand {
  font-size: 20px;
  font-weight: 800;
  color: var(--xunye-gold);
  letter-spacing: 2px;
}
.menu-status {
  display: flex;
  align-items: center;
  gap: 8px;
}
.table-pill {
  padding: 4px 12px;
  display: flex;
  align-items: center;
  gap: 4px;
  background: rgba(21, 23, 27, 0.92);
  border: 1px solid var(--xunye-line);
  border-radius: 999px;
  font-size: 12px;
}
.search-dot {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(21, 23, 27, 0.92);
  border: 1px solid var(--xunye-line);
  border-radius: 999px;
  font-size: 14px;
}
.menu-layout {
  flex: 1;
  display: flex;
  overflow: hidden;
}
.category-list {
  width: 30%;
  background: rgba(13, 14, 17, 0.72);
  padding-top: 8px;
  flex-shrink: 0;
}
.category-item {
  box-sizing: border-box;
  width: 100%;
  padding: 14px 8px;
  text-align: center;
  font-size: 12px;
  color: var(--xunye-muted);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.category-item.selected {
  color: var(--xunye-gold);
  background: rgba(210, 168, 95, 0.08);
  font-weight: 600;
  position: relative;
}
.category-item.selected::before {
  content: '';
  position: absolute;
  left: 0;
  top: 8px;
  bottom: 8px;
  width: 3px;
  background: var(--xunye-gold);
  border-radius: 0 3px 3px 0;
}
.product-list {
  width: 70%;
  flex: none;
  box-sizing: border-box;
  padding: 8px 12px;
}
.category-label {
  font-size: 16px;
  font-weight: 700;
  margin-bottom: 10px;
}
.product-item {
  display: flex;
  gap: 12px;
  padding: 12px 0;
  border-bottom: 1px solid var(--xunye-line);
}
.product-img {
  width: 64px;
  height: 64px;
  border-radius: 10px;
  background: var(--xunye-surface-2);
  flex-shrink: 0;
}
.product-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}
.product-desc {
  font-size: 12px;
  color: var(--xunye-muted);
  margin-top: 2px;
}
</style>
