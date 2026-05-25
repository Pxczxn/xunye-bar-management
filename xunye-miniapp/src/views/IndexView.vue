<script lang="ts" setup>
import type { ActivityVO, CustomerProductVO } from '@/api/customer'
import type { MenuProduct } from '@/store'
import { computed, onMounted, ref } from 'vue'
import { getShopInfo, listActiveActivities, listCustomerProducts } from '@/api/customer'
import { useShellState } from '@/composables/useShellState'
import { useXunyeStore } from '@/store'
import { getEnvBaseUrl } from '@/utils'

const store = useXunyeStore()
const { push, goPage, goMenuProduct } = useShellState()
const products = ref<MenuProduct[]>([])
const activities = ref<ActivityVO[]>([])
const loading = ref(false)
const activitiesLoading = ref(false)
const banners = ref<string[]>([])
const bannerLoading = ref(true)
const notice = ref('公告：今晚 21:00 爵士现场即将开始，敬请期待！特调买二送一。')
const shopName = ref('寻野 XUNYE')
const businessHours = ref('18:00 - 02:00')

const baseUrl = getEnvBaseUrl()

const tableText = computed(() => store.currentTable?.code || '未选桌')
const featuredProducts = computed(() => products.value.slice(0, 3))

onMounted(() => {
  fetchFeaturedProducts()
  fetchShopInfo()
  fetchActivities()
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

async function fetchFeaturedProducts() {
  loading.value = true
  const list = await listCustomerProducts().catch(() => [])
  products.value = list.map(toMenuProduct).slice(0, 6)
  loading.value = false
}

async function fetchActivities() {
  activitiesLoading.value = true
  const list = await listActiveActivities().catch(() => [])
  activities.value = list
  activitiesLoading.value = false
}

async function fetchShopInfo() {
  bannerLoading.value = true
  const shop = await getShopInfo().catch(() => null)
  if (shop) {
    // 更新店铺信息
    if (shop.name) shopName.value = shop.name
    if (shop.businessHours) businessHours.value = shop.businessHours
    if (shop.notice) notice.value = shop.notice

    // 更新轮播图
    if (shop.bannerImages?.length) {
      banners.value = shop.bannerImages.map((url) => {
        if (url.startsWith('http')) return url
        return baseUrl + url
      })
    }
  }
  bannerLoading.value = false
}

function handleStartOrder() {
  if (store.currentTable)
    goPage('menu')
  else push('table')
}

function openFeaturedProduct(index: number) {
  const product = featuredProducts.value[index]
  if (!product)
    return
  goMenuProduct(product.id)
}

function getActivityTypeLabel(type: string) {
  const typeMap: Record<string, string> = {
    DISCOUNT: '折扣',
    COUPON: '优惠券',
    POINTS: '积分',
    SPECIAL: '特惠',
  }
  return typeMap[type] || type
}

function getActivityTypeColor(type: string) {
  const colorMap: Record<string, string> = {
    DISCOUNT: '#d2a85f',
    COUPON: '#ff6b6b',
    POINTS: '#4ecdc4',
    SPECIAL: '#ff9f43',
  }
  return colorMap[type] || '#d2a85f'
}
</script>

<template>
  <view class="view view-scroll">
    <view class="brand-head">
      <view class="brand-title">
        {{ shopName }}
      </view>
      <view class="muted small">
        营业时间 {{ businessHours }}
      </view>
    </view>

    <!-- 首页轮播图 -->
    <view v-if="banners.length > 0" class="banner-wrap">
      <swiper
        class="banner-swiper"
        :indicator-dots="true"
        :autoplay="true"
        :interval="4000"
        :duration="500"
        indicator-color="rgba(255,255,255,0.3)"
        indicator-active-color="#d2a85f"
        circular
      >
        <swiper-item v-for="(url, idx) in banners" :key="idx">
          <image class="banner-img" mode="aspectFill" lazy-load :src="url" />
        </swiper-item>
      </swiper>
    </view>
    <view v-else-if="bannerLoading" class="banner-skeleton" />

    <view class="notice-pill">
      <uv-icon name="volume-fill" color="#d2a85f" size="18" />
      <text class="notice-text">{{ notice }}</text>
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
      <uv-button
        text="去选桌"
        icon="arrow-right"
        color="linear-gradient(135deg, #d2a85f, #bc8945)"
        shape="circle"
        size="small"
        custom-style="height: 34px; padding: 0 16px; color: #111318; font-weight: 800;"
        @click="push('table')"
      />
    </view>

    <view class="action-grid">
      <view class="action-card" @tap="handleStartOrder">
        <view class="action-icon gold-bg">
          <uv-icon name="grid-fill" color="#d2a85f" size="24" />
        </view>
        <view>我要喝</view>
      </view>
      <view class="action-card" @tap="push('table')">
        <view class="action-icon blue-bg">
          <uv-icon name="scan" color="#6fa8ff" size="24" />
        </view>
        <view>扫码点单</view>
      </view>
    </view>

    <!-- 活动专区 -->
    <view v-if="activities.length > 0" class="section">
      <view class="section-title">
        <uv-icon name="gift-fill" color="#d2a85f" size="18" /> 活动专区
      </view>
      <view v-if="activitiesLoading" class="recommend-loading">
        <uv-loading-icon mode="circle" color="#d2a85f" text="加载中" text-color="#8d929d" />
      </view>
      <view v-else class="activities-list">
        <view v-for="activity in activities" :key="activity.id" class="activity-card">
          <view class="activity-header">
            <view class="activity-type-badge" :style="{ background: getActivityTypeColor(activity.type) + '20', color: getActivityTypeColor(activity.type) }">
              {{ getActivityTypeLabel(activity.type) }}
            </view>
            <view class="activity-status">
              进行中
            </view>
          </view>
          <view class="activity-title">
            {{ activity.title }}
          </view>
          <view v-if="activity.description" class="activity-desc">
            {{ activity.description }}
          </view>
          <view class="activity-rule">
            <uv-icon name="tags-fill" :color="getActivityTypeColor(activity.type)" size="14" />
            <text>{{ activity.settingSummary }}</text>
          </view>
          <view class="activity-time">
            <uv-icon name="clock" color="#8d929d" size="13" />
            <text>{{ activity.startDate.slice(5, 16) }} ~ {{ activity.endDate.slice(5, 16) }}</text>
          </view>
        </view>
      </view>
    </view>

    <view class="section">
      <view class="section-title">
        <uv-icon name="star-fill" color="#d2a85f" size="18" /> 店长推荐
      </view>
      <view v-if="loading" class="recommend-loading">
        <uv-loading-icon mode="circle" color="#d2a85f" text="加载中" text-color="#8d929d" />
      </view>
      <view v-else class="recommend-grid">
        <view v-if="featuredProducts[0]" class="recommend-card" @tap="openFeaturedProduct(0)">
          <image class="recommend-img" mode="aspectFill" lazy-load :src="featuredProducts[0].image" />
          <view class="recommend-info">
            <view class="product-name truncate">
              {{ featuredProducts[0].name }}
            </view>
            <text class="price">¥{{ featuredProducts[0].price.toFixed(2) }}</text>
          </view>
        </view>
        <view v-if="featuredProducts[1]" class="recommend-card" @tap="openFeaturedProduct(1)">
          <image class="recommend-img" mode="aspectFill" lazy-load :src="featuredProducts[1].image" />
          <view class="recommend-info">
            <view class="product-name truncate">
              {{ featuredProducts[1].name }}
            </view>
            <text class="price">¥{{ featuredProducts[1].price.toFixed(2) }}</text>
          </view>
        </view>
        <view v-if="featuredProducts[2]" class="recommend-card" @tap="openFeaturedProduct(2)">
          <image class="recommend-img" mode="aspectFill" lazy-load :src="featuredProducts[2].image" />
          <view class="recommend-info">
            <view class="product-name truncate">
              {{ featuredProducts[2].name }}
            </view>
            <text class="price">¥{{ featuredProducts[2].price.toFixed(2) }}</text>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<style scoped>
.brand-head {
  padding: calc(var(--xunye-safe-top, 44px) + 12px) 24px 16px;
}
.brand-title {
  color: var(--xunye-gold);
  font-size: 28px;
  font-weight: 800;
  letter-spacing: 2px;
}

/* ========== Banner Swiper ========== */
.banner-wrap {
  margin: 4px 16px;
  border-radius: 14px;
  overflow: hidden;
}
.banner-swiper {
  width: 100%;
  height: 160px;
  border-radius: 14px;
}
.banner-img {
  width: 100%;
  height: 100%;
  background: var(--xunye-surface-2);
}
.banner-skeleton {
  margin: 4px 16px;
  height: 160px;
  border-radius: 14px;
  background: var(--xunye-surface);
}

.notice-pill {
  display: flex;
  align-items: center;
  gap: 10px;
  margin: 8px 16px;
  padding: 9px 16px;
  background: rgba(21, 23, 27, 0.9);
  border: 1px solid rgba(210, 168, 95, 0.18);
  border-radius: 999px;
}
.notice-text {
  flex: 1;
  overflow: hidden;
  font-size: 13px;
  white-space: nowrap;
  text-overflow: ellipsis;
}
.table-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin: 4px 16px;
  padding: 14px 18px;
  background: rgba(21, 23, 27, 0.94);
  border: 1px solid var(--xunye-line);
  border-radius: 16px;
}
.table-code {
  margin-top: 2px;
  font-size: 20px;
  font-weight: 700;
}
.action-grid {
  display: flex;
  gap: 12px;
  margin: 16px;
}
.action-card {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 20px 0;
  background: rgba(21, 23, 27, 0.94);
  border: 1px solid var(--xunye-line);
  border-radius: 16px;
  font-size: 14px;
  font-weight: 600;
}
.action-icon {
  width: 44px;
  height: 44px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 999px;
  font-size: 20px;
}
.gold-bg {
  background: rgba(210, 168, 95, 0.14);
}
.blue-bg {
  background: rgba(64, 128, 255, 0.15);
}
.section {
  margin: 8px 16px;
}
.section-title {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 10px;
  font-size: 18px;
  font-weight: 700;
}
.recommend-loading {
  display: flex;
  justify-content: center;
  padding: 40px 0;
}
.recommend-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
  padding-bottom: 8px;
}
.recommend-card {
  background: rgba(21, 23, 27, 0.94);
  border: 1px solid var(--xunye-line);
  border-radius: 16px;
  overflow: hidden;
}
.recommend-img {
  width: 100%;
  height: 112px;
  background: var(--xunye-surface-2);
}
.recommend-info {
  padding: 10px 12px 12px;
}
.product-name {
  margin-bottom: 6px;
  font-size: 14px;
  font-weight: 600;
}
.truncate {
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
}

/* ========== Activities Section ========== */
.activities-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.activity-card {
  padding: 16px;
  background: rgba(21, 23, 27, 0.94);
  border: 1px solid var(--xunye-line);
  border-radius: 16px;
}
.activity-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 10px;
}
.activity-type-badge {
  padding: 4px 10px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 600;
}
.activity-status {
  padding: 3px 8px;
  background: rgba(76, 217, 100, 0.15);
  border-radius: 999px;
  color: #4cd964;
  font-size: 11px;
  font-weight: 600;
}
.activity-title {
  margin-bottom: 6px;
  font-size: 16px;
  font-weight: 700;
  line-height: 1.4;
}
.activity-desc {
  margin-bottom: 10px;
  color: #8d929d;
  font-size: 13px;
  line-height: 1.5;
}
.activity-rule {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 8px;
  padding: 8px 12px;
  background: rgba(210, 168, 95, 0.08);
  border-radius: 8px;
  font-size: 13px;
  font-weight: 600;
}
.activity-time {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #8d929d;
  font-size: 12px;
}
</style>
