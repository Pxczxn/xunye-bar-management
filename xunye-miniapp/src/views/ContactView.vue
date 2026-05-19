<script lang="ts" setup>
import { useShellState } from '@/composables/useShellState'

const { back } = useShellState()

function callPhone(phone: string) {
  uni.makePhoneCall({ phoneNumber: phone })
}
function copyText(text: string) {
  uni.setClipboardData({
    data: text,
    success: () => { uni.showToast({ title: '已复制', icon: 'success', duration: 1500 }) },
  })
}
</script>

<template>
  <view class="view">
    <view class="topbar">
      <button class="icon-button" hover-class="none" @tap="back">
        <uv-icon name="arrow-left" color="#f7f1e8" size="20" />
      </button>
      <view class="top-title">联系客服</view>
    </view>
    <scroll-view scroll-y class="scroll-view">
      <view class="section-wrap">
        <view class="contact-card">
          <view class="contact-icon-wrap">
            <uv-icon name="kefu-ermai" color="#d2a85f" size="30" />
          </view>
          <text class="contact-name">在线客服</text>
          <text class="contact-desc">营业时间内快速响应</text>
        </view>
      </view>

      <view class="section-wrap">
        <view class="contact-list">
          <uv-cell title="客服电话" label="138-0000-0000" icon="phone-fill" value="拨打" is-link clickable :border="true"
            :title-style="{ color: '#f7f1e8', fontWeight: 700 }"
            :label-style="{ color: '#8d929d' }"
            :icon-style="{ color: '#d2a85f' }"
            :cell-style="{ background: 'transparent', padding: '14px 0' }"
            @click="callPhone('13800000000')"
          />
          <uv-cell title="微信公众号" label="寻野酒吧" icon="chat-fill" value="复制" is-link clickable :border="true"
            :title-style="{ color: '#f7f1e8', fontWeight: 700 }"
            :label-style="{ color: '#8d929d' }"
            :icon-style="{ color: '#d2a85f' }"
            :cell-style="{ background: 'transparent', padding: '14px 0' }"
            @click="copyText('寻野酒吧')"
          />
          <uv-cell title="门店地址" label="示例街道 88 号" icon="map-fill" :border="false"
            :title-style="{ color: '#f7f1e8', fontWeight: 700 }"
            :label-style="{ color: '#8d929d' }"
            :icon-style="{ color: '#d2a85f' }"
            :cell-style="{ background: 'transparent', padding: '14px 0' }"
          />
        </view>
      </view>

      <view class="section-wrap">
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
</template>

<style scoped>
.topbar {
  display: flex;
  align-items: center;
  padding: calc(var(--xunye-safe-top, 44px) + 5px) 12px 8px;
}
.scroll-view {
  flex: 1;
  padding: 0 16px;
}
.section-wrap { margin-bottom: 20px; }
.section-title { font-size: 16px; font-weight: 700; display: block; margin-bottom: 10px; }
.contact-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 24px;
  background: rgba(21, 23, 27, 0.94);
  border: 1px solid var(--xunye-line);
  border-radius: 18px;
}
.contact-icon-wrap {
  width: 52px; height: 52px;
  display: flex; align-items: center; justify-content: center;
  background: rgba(210, 168, 95, 0.14);
  border-radius: 999px;
  margin-bottom: 8px;
  font-size: 24px;
}
.contact-name { font-size: 16px; font-weight: 600; }
.contact-desc { font-size: 12px; color: #858585; margin-top: 2px; }
.contact-list {
  padding: 0 16px;
  overflow: hidden;
  background: rgba(21, 23, 27, 0.94);
  border: 1px solid var(--xunye-line);
  border-radius: 18px;
}
.info-row {
  display: flex;
  justify-content: space-between;
  padding: 12px 0;
  border-bottom: 1px solid var(--xunye-line);
  font-size: 14px;
}
.info-row.last { border-bottom: none; }
.info-label { color: var(--xunye-muted); }
.info-value { color: #d9dde6; }
.muted { color: var(--xunye-muted); }
</style>
