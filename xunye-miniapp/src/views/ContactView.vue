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
      <button class="icon-button" hover-class="none" @tap="() => back()">
        <uv-icon name="arrow-left" color="#f7f1e8" size="20" />
      </button>
      <view class="top-title">
        联系客服
      </view>
    </view>
    <scroll-view scroll-y class="scroll-view" enhanced show-scrollbar="false">
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
          <button class="contact-row" hover-class="none" @tap="() => callPhone('13800000000')">
            <view class="row-main">
              <uv-icon name="phone-fill" color="#d2a85f" size="17" />
              <view class="row-copy">
                <text class="row-title">
                  客服电话
                </text>
                <text class="row-label">
                  138-0000-0000
                </text>
              </view>
            </view>
            <view class="row-action">
              <text>拨打</text>
              <uv-icon name="arrow-right" color="#8d929d" size="14" />
            </view>
          </button>
          <button class="contact-row" hover-class="none" @tap="() => copyText('寻野酒吧')">
            <view class="row-main">
              <uv-icon name="chat-fill" color="#d2a85f" size="17" />
              <view class="row-copy">
                <text class="row-title">
                  微信公众号
                </text>
                <text class="row-label">
                  寻野酒吧
                </text>
              </view>
            </view>
            <view class="row-action">
              <text>复制</text>
              <uv-icon name="arrow-right" color="#8d929d" size="14" />
            </view>
          </button>
          <view class="contact-row last">
            <view class="row-main">
              <uv-icon name="map-fill" color="#d2a85f" size="17" />
              <view class="row-copy">
                <text class="row-title">
                  门店地址
                </text>
                <text class="row-label">
                  示例街道 88 号
                </text>
              </view>
            </view>
          </view>
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
  box-sizing: border-box;
  width: 100%;
  padding: calc(var(--xunye-safe-top, 44px) + 5px) 12px 8px;
  overflow: hidden;
}
.scroll-view {
  box-sizing: border-box;
  flex: 1;
  width: 100%;
  max-width: 100%;
  padding: 0 16px;
  overflow-x: hidden;
}
.section-wrap {
  box-sizing: border-box;
  width: 100%;
  max-width: 100%;
  margin-bottom: 20px;
  overflow: hidden;
}
.section-title {
  display: block;
  margin-bottom: 10px;
  font-size: 16px;
  font-weight: 700;
}
.contact-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  box-sizing: border-box;
  width: 100%;
  padding: 24px;
  background: rgba(21, 23, 27, 0.94);
  border: 1px solid var(--xunye-line);
  border-radius: 18px;
}
.contact-icon-wrap {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 52px;
  height: 52px;
  margin-bottom: 8px;
  background: rgba(210, 168, 95, 0.14);
  border-radius: 999px;
  font-size: 24px;
}
.contact-name {
  font-size: 16px;
  font-weight: 600;
}
.contact-desc {
  margin-top: 2px;
  color: #858585;
  font-size: 12px;
}
.contact-list {
  box-sizing: border-box;
  width: 100%;
  padding: 0 16px;
  overflow: hidden;
  background: rgba(21, 23, 27, 0.94);
  border: 1px solid var(--xunye-line);
  border-radius: 18px;
}
.contact-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  box-sizing: border-box;
  width: 100%;
  min-height: 64px;
  padding: 0;
  color: #f7f1e8;
  background: transparent;
  border-bottom: 1px solid var(--xunye-line);
}
.contact-row.last {
  border-bottom: 0;
}
.row-main {
  display: flex;
  align-items: center;
  gap: 9px;
  min-width: 0;
}
.row-copy {
  display: flex;
  flex-direction: column;
  min-width: 0;
}
.row-title {
  overflow: hidden;
  color: #f7f1e8;
  font-size: 14px;
  font-weight: 800;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.row-label {
  overflow: hidden;
  margin-top: 3px;
  color: var(--xunye-muted);
  font-size: 12px;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.row-action {
  display: flex;
  align-items: center;
  flex-shrink: 0;
  gap: 2px;
  margin-left: 10px;
  color: var(--xunye-muted);
  font-size: 13px;
}
.info-row {
  display: flex;
  gap: 12px;
  justify-content: space-between;
  box-sizing: border-box;
  width: 100%;
  padding: 12px 0;
  border-bottom: 1px solid var(--xunye-line);
  font-size: 14px;
}
.info-row.last {
  border-bottom: none;
}
.info-label {
  flex-shrink: 0;
  color: var(--xunye-muted);
}
.info-value {
  min-width: 0;
  overflow: hidden;
  color: #d9dde6;
  text-align: right;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.muted {
  color: var(--xunye-muted);
}
</style>
