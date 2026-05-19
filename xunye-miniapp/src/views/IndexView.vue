<script lang="ts" setup>
import { computed } from 'vue'
import { products, useXunyeStore } from '@/store'
import { useShellState } from '@/composables/useShellState'

const store = useXunyeStore()
const { push, goPage, goMenuProduct } = useShellState()

const tableText = computed(() => store.currentTable?.code || '未选桌')
const featuredProducts = computed(() => [products[0], products[4], products[5]].filter(Boolean))

function handleStartOrder() {
  if (store.currentTable) goPage('menu')
  else push('table')
}

function openFeaturedProduct(index: number) {
  const product = featuredProducts.value[index]
  if (!product) return
  goMenuProduct(product.id)
}
</script>

<template>
  <view class="view view-scroll">
    <view class="brand-head">
      <view class="brand-title">寻野 XUNYE</view>
      <view class="muted small">营业时间 18:00 - 02:00</view>
    </view>

    <view class="notice-pill">
      <uv-icon name="volume-fill" color="#d2a85f" size="18" />
      <text class="notice-text">公告：今晚 21:00 爵士现场即将开始，敬请期待！特调买二送一。</text>
    </view>

    <view class="table-card">
      <view>
        <view class="muted small">当前桌台</view>
        <view class="table-code">{{ tableText }}</view>
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

    <view class="section">
      <view class="section-title"><uv-icon name="star-fill" color="#d2a85f" size="18" /> 店长推荐</view>
      <view class="recommend-grid">
        <view v-if="featuredProducts[0]" class="recommend-card" @tap="openFeaturedProduct(0)">
          <image class="recommend-img" mode="aspectFill" lazy-load :src="featuredProducts[0].image" />
          <view class="recommend-info">
            <view class="product-name truncate">{{ featuredProducts[0].name }}</view>
            <text class="price">¥{{ featuredProducts[0].price.toFixed(2) }}</text>
          </view>
        </view>
        <view v-if="featuredProducts[1]" class="recommend-card" @tap="openFeaturedProduct(1)">
          <image class="recommend-img" mode="aspectFill" lazy-load :src="featuredProducts[1].image" />
          <view class="recommend-info">
            <view class="product-name truncate">{{ featuredProducts[1].name }}</view>
            <text class="price">¥{{ featuredProducts[1].price.toFixed(2) }}</text>
          </view>
        </view>
        <view v-if="featuredProducts[2]" class="recommend-card" @tap="openFeaturedProduct(2)">
          <image class="recommend-img" mode="aspectFill" lazy-load :src="featuredProducts[2].image" />
          <view class="recommend-info">
            <view class="product-name truncate">{{ featuredProducts[2].name }}</view>
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
.gold-bg { background: rgba(210, 168, 95, 0.14); }
.blue-bg { background: rgba(64, 128, 255, 0.15); }
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
</style>
