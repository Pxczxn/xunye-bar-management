<script lang="ts" setup>
import { computed, onMounted, onUnmounted, reactive, ref, watch } from 'vue'
import { getCustomerStats } from '@/api/customer'
import { phoneLoginByCode, phoneLoginByPassword, registerCustomer, sendLoginCode, sendRegisterCode, wxLoginCustomer } from '@/api/membership'
import { useShellState } from '@/composables/useShellState'
import { useCustomerProfileStore } from '@/store/customerProfile'
import { getEnvBaseUrl } from '@/utils'

const { goPage, push } = useShellState()
const customerProfileStore = useCustomerProfileStore()
const profile = customerProfileStore.profile
const isLoggedIn = computed(() => customerProfileStore.isLoggedIn)
const registerOpen = ref(false)
const loginOpen = ref(false)
const phoneLoginOpen = ref(false)
const loginTab = ref<'code' | 'password'>('code')
const codeCountdown = ref(0)
const phoneLoginCountdown = ref(0)
let codeTimer: ReturnType<typeof setInterval> | null = null
let phoneLoginCodeTimer: ReturnType<typeof setInterval> | null = null
const registerForm = reactive({
  nickname: '',
  phone: '',
  verifyCode: '',
  avatar: '',
})
const phoneLoginForm = reactive({
  phone: '',
  verifyCode: '',
  password: '',
})
const avatarUploadUrl = `${getEnvBaseUrl()}/api/customer/member/avatar`
const customerStats = ref({ points: 0, coupons: 0, totalOrders: 0, totalAmount: 0 })
const statsLoading = ref(false)
const { activeView } = useShellState()

// 切换到该 tab 时自动刷新，30 秒冷却
let lastTabFetchTime = 0
watch(activeView, (val) => {
  if (val === 'mine') {
    const now = Date.now()
    if (now - lastTabFetchTime > 30000) {
      lastTabFetchTime = now
      if (isLoggedIn.value)
        fetchStats()
    }
  }
})

onMounted(() => {
  lastTabFetchTime = Date.now()
  if (isLoggedIn.value) {
    customerProfileStore.fetchProfile().catch(() => {})
    fetchStats()
  }
})

// 清理验证码倒计时定时器（v-show 保活后组件不会销毁）
onUnmounted(() => {
  clearTimers()
})

function clearTimers() {
  if (codeTimer) {
    clearInterval(codeTimer)
    codeTimer = null
  }
  if (phoneLoginCodeTimer) {
    clearInterval(phoneLoginCodeTimer)
    phoneLoginCodeTimer = null
  }
}

async function fetchStats() {
  const phone = customerProfileStore.profile.phone
  if (!phone)
    return
  statsLoading.value = true
  try {
    customerStats.value = await getCustomerStats(phone)
  }
  catch {}
  finally {
    statsLoading.value = false
  }
}

function requireLogin(action: () => void) {
  if (isLoggedIn.value) {
    action()
    return
  }
  uni.showToast({
    title: '请先登录',
    icon: 'none',
  })
}

function openProfile() {
  if (isLoggedIn.value) {
    push('profile')
  }
  else {
    handleLogin()
  }
}

function openPoints() {
  requireLogin(() => push('points'))
}

function openCoupons() {
  requireLogin(() => push('coupons'))
}

function openMembership() {
  requireLogin(() => push('membership'))
}

function openOrders() {
  requireLogin(() => goPage('orders'))
}

function openContact() {
  push('contact')
}

function getWxLoginCode() {
  return new Promise<string>((resolve, reject) => {
    uni.login({
      provider: 'weixin',
      success: (res) => {
        if (res.code) {
          resolve(res.code)
        }
        else {
          reject(new Error('Missing wx login code'))
        }
      },
      fail: reject,
    })
  })
}

function handleLogin() {
  loginOpen.value = true
}

function closeLogin() {
  loginOpen.value = false
}

async function handleWechatLogin() {
  try {
    const code = await getWxLoginCode()
    const data = await wxLoginCustomer({
      code,
      customerNo: profile.customerNo || undefined,
      phone: profile.phone || undefined,
    })
    customerProfileStore.apply(data)
    loginOpen.value = false
    uni.showToast({
      title: '登录成功',
      icon: 'none',
    })
  }
  catch (error) {
    console.error('wechat login failed:', error)
    uni.showToast({
      title: '登录失败，请重试',
      icon: 'none',
    })
  }
}

function openPhoneLogin() {
  loginOpen.value = false
  phoneLoginForm.phone = ''
  phoneLoginForm.verifyCode = ''
  phoneLoginForm.password = ''
  loginTab.value = 'code'
  phoneLoginOpen.value = true
}

function closePhoneLogin() {
  phoneLoginOpen.value = false
}

function switchLoginTab(tab: 'code' | 'password') {
  loginTab.value = tab
}

function isValidPhone(phone: string) {
  return /^1[3-9]\d{9}$/.test(phone)
}

function startPhoneLoginCodeCountdown() {
  phoneLoginCountdown.value = 30
  if (phoneLoginCodeTimer) {
    clearInterval(phoneLoginCodeTimer)
  }
  phoneLoginCodeTimer = setInterval(() => {
    phoneLoginCountdown.value -= 1
    if (phoneLoginCountdown.value <= 0 && phoneLoginCodeTimer) {
      clearInterval(phoneLoginCodeTimer)
      phoneLoginCodeTimer = null
    }
  }, 1000)
}

async function handleSendLoginCode() {
  if (phoneLoginCountdown.value > 0)
    return
  const phone = phoneLoginForm.phone.trim()
  if (!isValidPhone(phone)) {
    uni.showToast({ title: '请输入有效手机号', icon: 'none' })
    return
  }
  try {
    await sendLoginCode(phone)
    startPhoneLoginCodeCountdown()
    uni.showToast({ title: '验证码已发送', icon: 'none' })
  }
  catch {
    uni.showToast({ title: '发送失败，请检查手机号是否已注册', icon: 'none' })
  }
}

async function submitPhoneLoginByCode() {
  const phone = phoneLoginForm.phone.trim()
  const verifyCode = phoneLoginForm.verifyCode.trim()
  if (!isValidPhone(phone)) {
    uni.showToast({ title: '请输入有效手机号', icon: 'none' })
    return
  }
  if (!verifyCode) {
    uni.showToast({ title: '请输入验证码', icon: 'none' })
    return
  }
  try {
    const data = await phoneLoginByCode(phone, verifyCode)
    customerProfileStore.apply(data)
    closePhoneLogin()
    uni.showToast({ title: '登录成功', icon: 'none' })
  }
  catch (error) {
    console.error('phone code login failed:', error)
    uni.showToast({ title: '登录失败，请重试', icon: 'none' })
  }
}

async function submitPhoneLoginByPassword() {
  const phone = phoneLoginForm.phone.trim()
  const password = phoneLoginForm.password.trim()
  if (!isValidPhone(phone)) {
    uni.showToast({ title: '请输入有效手机号', icon: 'none' })
    return
  }
  if (!password) {
    uni.showToast({ title: '请输入密码', icon: 'none' })
    return
  }
  try {
    const data = await phoneLoginByPassword(phone, password)
    customerProfileStore.apply(data)
    closePhoneLogin()
    uni.showToast({ title: '登录成功', icon: 'none' })
  }
  catch (error) {
    console.error('phone password login failed:', error)
    uni.showToast({ title: '登录失败，请检查密码', icon: 'none' })
  }
}

async function handleRegister() {
  registerOpen.value = true
}

function closeRegister() {
  registerOpen.value = false
}

function startCodeCountdown() {
  codeCountdown.value = 30
  if (codeTimer) {
    clearInterval(codeTimer)
  }
  codeTimer = setInterval(() => {
    codeCountdown.value -= 1
    if (codeCountdown.value <= 0 && codeTimer) {
      clearInterval(codeTimer)
      codeTimer = null
    }
  }, 1000)
}

function chooseRegisterAvatar() {
  uni.chooseMedia({
    count: 1,
    mediaType: ['image'],
    sourceType: ['album', 'camera'],
    success: (res) => {
      registerForm.avatar = res.tempFiles[0]?.tempFilePath || ''
    },
  })
}

function normalizeImageUrl(url: string) {
  if (url.startsWith('http'))
    return url
  return `${getEnvBaseUrl()}${url}`
}

function uploadRegisterAvatar(filePath: string, phone: string, customerNo: string) {
  return new Promise<string>((resolve, reject) => {
    uni.uploadFile({
      url: avatarUploadUrl,
      filePath,
      name: 'file',
      formData: {
        phone,
        customerNo,
      },
      success: (res) => {
        try {
          const body = JSON.parse(res.data)
          if (body?.code !== 200 || !body?.data) {
            reject(new Error(body?.message || 'avatar upload failed'))
            return
          }
          resolve(body.data)
        }
        catch (error) {
          reject(error)
        }
      },
      fail: reject,
    })
  })
}

async function handleSendRegisterCode() {
  if (codeCountdown.value > 0)
    return
  const phone = registerForm.phone.trim()
  if (!isValidPhone(phone)) {
    uni.showToast({ title: '请输入有效手机号', icon: 'none' })
    return
  }
  try {
    await sendRegisterCode(phone)
    startCodeCountdown()
    uni.showToast({ title: '验证码已发送', icon: 'none' })
  }
  catch {
    uni.showToast({ title: '手机号已注册或发送太频繁', icon: 'none' })
  }
}

async function submitRegister() {
  const phone = registerForm.phone.trim()
  const verifyCode = registerForm.verifyCode.trim()
  const nickname = registerForm.nickname.trim()
  if (!isValidPhone(phone)) {
    uni.showToast({ title: '请输入有效手机号', icon: 'none' })
    return
  }
  if (!/^\d{4,8}$/.test(verifyCode)) {
    uni.showToast({ title: '请输入验证码', icon: 'none' })
    return
  }
  try {
    const code = await getWxLoginCode()
    const data = await registerCustomer({
      code,
      phone,
      verifyCode,
      nickname: nickname || undefined,
    })
    customerProfileStore.apply(data)
    if (registerForm.avatar) {
      const avatar = await uploadRegisterAvatar(registerForm.avatar, data.phone, data.customerNo)
      customerProfileStore.apply({
        ...data,
        avatar: normalizeImageUrl(avatar),
      })
    }
    closeRegister()
    uni.showToast({
      title: '注册成功',
      icon: 'none',
    })
  }
  catch (error) {
    console.error('customer register failed:', error)
    uni.showToast({
      title: '注册失败，请重试',
      icon: 'none',
    })
  }
}

function handleLogout() {
  uni.showModal({
    title: '退出登录',
    content: '确定要退出当前账号吗？',
    confirmText: '退出',
    confirmColor: '#d2a85f',
    cancelText: '取消',
    success: async ({ confirm }) => {
      if (!confirm)
        return

      uni.removeStorageSync('token')
      uni.removeStorageSync('user')
      uni.removeStorageSync('accessTokenExpireTime')
      uni.removeStorageSync('refreshTokenExpireTime')
      customerProfileStore.resetProfile()
      uni.showToast({
        title: '已退出登录',
        icon: 'none',
      })
    },
  })
}
</script>

<template>
  <view class="view">
    <view class="content">
      <view class="profile-card" @tap="openProfile">
        <image class="avatar" :src="profile.avatar || '/static/images/avatar.jpg'" mode="aspectFill" lazy-load />
        <view class="profile-main">
          <view class="bold profile-name">
            {{ isLoggedIn ? profile.nickname : '未登录' }}
          </view>
          <view class="muted small">
            {{ isLoggedIn ? profile.levelText : '点击登录后查看会员权益' }}
          </view>
          <view class="muted mini">
            {{ isLoggedIn ? `ID ${profile.customerNo || '生成中'}` : '登录后同步订单、积分和优惠券' }}
          </view>
        </view>
        <uv-icon name="arrow-right" color="#8d929d" size="16" />
      </view>
      <view class="stats-card">
        <button class="stat-item" hover-class="none" @tap="openPoints">
          <view class="gold stat-num">
            {{ isLoggedIn ? customerStats.points : '--' }}
          </view>
          <view class="muted mini">
            积分
          </view>
        </button>
        <view class="divider" />
        <button class="stat-item" hover-class="none" @tap="openCoupons">
          <view class="gold stat-num">
            {{ isLoggedIn ? customerStats.coupons : '--' }}
          </view>
          <view class="muted mini">
            优惠券
          </view>
        </button>
        <view class="divider" />
        <button class="stat-item" hover-class="none" @tap="openMembership">
          <view class="gold stat-text">
            {{ isLoggedIn ? (profile.memberLevelName || '普通会员') : '--' }}
          </view>
          <view class="muted mini">
            会员等级
          </view>
        </button>
      </view>
      <view class="mine-cell-panel">
        <button class="mine-row" hover-class="none" @tap="openOrders">
          <view class="mine-row-title">
            <text>历史订单</text>
          </view>
          <text class="row-arrow">›</text>
        </button>
        <button class="mine-row" hover-class="none" @tap="openMembership">
          <view class="mine-row-title">
            <text>会员权益</text>
          </view>
          <text class="row-arrow">›</text>
        </button>
        <button class="mine-row last" hover-class="none" @tap="openContact">
          <view class="mine-row-title">
            <text>联系客服</text>
          </view>
          <text class="row-arrow">›</text>
        </button>
      </view>
      <view v-if="isLoggedIn" class="logout-panel">
        <button class="logout-button" hover-class="none" @tap="handleLogout">
          <uv-icon name="close-circle" color="#f26d6d" size="17" />
          <text>退出登录</text>
        </button>
      </view>
      <view v-else class="login-panel">
        <button class="login-button" hover-class="none" @tap="handleLogin">
          <text>登录</text>
        </button>
        <button class="register-button" hover-class="none" @tap="handleRegister">
          <text>注册会员</text>
        </button>
      </view>
      <view v-if="registerOpen" class="register-mask" @tap="closeRegister">
        <view class="register-dialog" @tap.stop>
          <view class="register-title">
            注册会员
          </view>
          <button class="avatar-picker" hover-class="none" @tap="chooseRegisterAvatar">
            <image class="register-avatar" :src="registerForm.avatar || '/static/images/avatar.jpg'" mode="aspectFill" />
            <text>自定义头像</text>
          </button>
          <input v-model="registerForm.nickname" class="register-input" placeholder="昵称（选填）" placeholder-style="color: rgba(247, 241, 232, 0.42);">
          <input v-model="registerForm.phone" class="register-input" type="number" maxlength="11" placeholder="手机号" placeholder-style="color: rgba(247, 241, 232, 0.42);">
          <view class="code-row">
            <input v-model="registerForm.verifyCode" class="register-input code-input" type="number" placeholder="验证码" placeholder-style="color: rgba(247, 241, 232, 0.42);">
            <view class="code-button" :class="{ disabled: codeCountdown > 0 }" @tap="handleSendRegisterCode">
              <text class="code-button-text">{{ codeCountdown > 0 ? `${codeCountdown}s` : '获取验证码' }}</text>
            </view>
          </view>
          <view class="register-actions">
            <button class="cancel-button" hover-class="none" @tap="closeRegister">
              取消
            </button>
            <button class="submit-button" hover-class="none" @tap="submitRegister">
              提交注册
            </button>
          </view>
        </view>
      </view>
      <!-- Login method dialog -->
      <view v-if="loginOpen" class="register-mask" @tap="closeLogin">
        <view class="register-dialog" @tap.stop>
          <view class="register-title">
            登录
          </view>
          <button class="dialog-big-button" hover-class="none" @tap="handleWechatLogin">
            <uv-icon name="weixin-fill" color="#111318" size="20" />
            <text>微信登录</text>
          </button>
          <button class="dialog-big-button phone-login-btn" hover-class="none" @tap="openPhoneLogin">
            <uv-icon name="phone-fill" color="#f7f1e8" size="20" />
            <text>手机号登录</text>
          </button>
          <view class="register-actions">
            <button class="cancel-button" hover-class="none" @tap="closeLogin">
              取消
            </button>
          </view>
        </view>
      </view>
      <!-- Phone login dialog -->
      <view v-if="phoneLoginOpen" class="register-mask" @tap="closePhoneLogin">
        <view class="register-dialog" @tap.stop>
          <view class="register-title">
            手机号登录
          </view>
          <view class="login-tabs">
            <button
              class="login-tab"
              :class="{ active: loginTab === 'code' }"
              hover-class="none"
              @tap="switchLoginTab('code')"
            >
              验证码登录
            </button>
            <button
              class="login-tab"
              :class="{ active: loginTab === 'password' }"
              hover-class="none"
              @tap="switchLoginTab('password')"
            >
              密码登录
            </button>
          </view>
          <input v-model="phoneLoginForm.phone" class="register-input" type="number" maxlength="11" placeholder="手机号" placeholder-style="color: rgba(247, 241, 232, 0.42);">
          <template v-if="loginTab === 'code'">
            <view class="code-row">
              <input v-model="phoneLoginForm.verifyCode" class="register-input code-input" type="number" placeholder="验证码" placeholder-style="color: rgba(247, 241, 232, 0.42);">
              <view class="code-button" :class="{ disabled: phoneLoginCountdown > 0 }" @tap="handleSendLoginCode">
                <text class="code-button-text">{{ phoneLoginCountdown > 0 ? `${phoneLoginCountdown}s` : '获取验证码' }}</text>
              </view>
            </view>
          </template>
          <template v-else>
            <input v-model="phoneLoginForm.password" class="register-input" type="text" password placeholder="密码" placeholder-style="color: rgba(247, 241, 232, 0.42);">
          </template>
          <view class="register-actions">
            <button class="cancel-button" hover-class="none" @tap="closePhoneLogin">
              取消
            </button>
            <button class="submit-button" hover-class="none" @tap="loginTab === 'code' ? submitPhoneLoginByCode() : submitPhoneLoginByPassword()">
              登录
            </button>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<style scoped>
.content {
  padding-top: calc(var(--xunye-safe-top, 44px) + 18px);
}
.profile-card {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 16px;
  background: rgba(21, 23, 27, 0.94);
  border: 1px solid var(--xunye-line);
  border-radius: 18px;
  margin-bottom: 12px;
}
.avatar {
  width: 52px;
  height: 52px;
  border-radius: 999px;
  background: #222;
}
.profile-main {
  flex: 1;
  min-width: 0;
}
.profile-name {
  font-size: 18px;
}
.stats-card {
  display: grid;
  grid-template-columns: 1fr auto 1fr auto 1fr;
  align-items: center;
  padding: 16px;
  background: linear-gradient(135deg, rgba(28, 30, 35, 0.96) 0%, rgba(21, 23, 27, 0.94) 100%);
  border: 1px solid rgba(210, 168, 95, 0.16);
  border-radius: 18px;
  margin-bottom: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.25);
}
.stat-num {
  font-size: 22px;
  font-weight: 700;
  text-align: center;
  height: 30px;
  display: flex;
  align-items: center;
  justify-content: center;
}
.stat-text {
  font-size: 14px;
  font-weight: 700;
  text-align: center;
  padding: 0 2px;
  word-break: break-all;
  height: 30px;
  display: flex;
  align-items: center;
  justify-content: center;
}
.stat-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-width: 0;
  background: transparent;
  gap: 2px;
}
.divider {
  width: 1px;
  height: 32px;
  background: rgba(210, 168, 95, 0.15);
}
.mine-cell-panel {
  box-sizing: border-box;
  padding: 0 16px;
  overflow: hidden;
  background: rgba(21, 23, 27, 0.94);
  border: 1px solid var(--xunye-line);
  border-radius: 18px;
}
.mine-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  box-sizing: border-box;
  width: 100%;
  min-height: 52px;
  padding: 0;
  color: #fff;
  background: transparent;
  border-bottom: 1px solid var(--xunye-line);
}
.mine-row.last {
  border-bottom: 0;
}
.mine-row-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 15px;
  font-weight: 700;
}
.row-arrow {
  color: #8d929d;
  font-size: 22px;
  line-height: 1;
}
.logout-panel {
  margin-top: 12px;
}
.logout-button {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  width: 100%;
  height: 48px;
  color: #f26d6d;
  font-size: 15px;
  font-weight: 700;
  background: rgba(21, 23, 27, 0.94);
  border: 1px solid rgba(242, 109, 109, 0.3);
  border-radius: 18px;
}
.login-panel {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-top: 14px;
}
.login-button {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  box-sizing: border-box;
  width: 100%;
  min-height: 48px;
  padding: 0 18px;
  color: #111318;
  font-size: 15px;
  font-weight: 800;
  background: var(--xunye-gold);
  border: 0;
  border-radius: 16px;
  line-height: 48px;
}
.register-button {
  box-sizing: border-box;
  width: 100%;
  min-height: 48px;
  padding: 0 18px;
  color: var(--xunye-gold);
  font-size: 15px;
  font-weight: 800;
  background: rgba(21, 23, 27, 0.94);
  border: 1px solid rgba(210, 168, 95, 0.36);
  border-radius: 16px;
  line-height: 46px;
}
.register-mask {
  position: fixed;
  inset: 0;
  z-index: 80;
  display: flex;
  align-items: center;
  justify-content: center;
  box-sizing: border-box;
  padding: calc(var(--xunye-safe-top, 44px) + 16px) 18px calc(env(safe-area-inset-bottom, 8px) + 96px);
  background: rgba(0, 0, 0, 0.64);
}
.register-dialog {
  box-sizing: border-box;
  width: 100%;
  max-height: 100%;
  padding: 18px;
  overflow-y: auto;
  background: #15171b;
  border: 1px solid var(--xunye-line);
  border-radius: 18px;
}
.register-title {
  margin-bottom: 14px;
  color: #fff;
  font-size: 18px;
  font-weight: 800;
}
.avatar-picker {
  display: flex;
  align-items: center;
  gap: 12px;
  width: 100%;
  min-height: 58px;
  margin-bottom: 12px;
  padding: 0;
  color: var(--xunye-gold);
  font-size: 14px;
  font-weight: 700;
  text-align: left;
  background: transparent;
}
.register-avatar {
  width: 52px;
  height: 52px;
  border-radius: 999px;
  background: #222;
}
.register-input {
  box-sizing: border-box;
  width: 100%;
  height: 46px;
  margin-bottom: 10px;
  padding: 0 12px;
  color: #f7f1e8;
  font-size: 14px;
  background: rgba(255, 255, 255, 0.06);
  border: 1px solid rgba(255, 255, 255, 0.12);
  border-radius: 12px;
  line-height: 46px;
  caret-color: var(--xunye-gold);
}
.code-row {
  display: flex;

  align-items: center;

  gap: 10px;

  margin-bottom: 10px;
}

.code-input {
  flex: 1;

  min-width: 0;

  margin-bottom: 0;
}

.code-button {
  display: flex;

  align-items: center;

  justify-content: center;

  box-sizing: border-box;

  flex: 0 0 108px;

  height: 46px;

  padding: 0;

  color: #111318;

  font-size: 12px;

  font-weight: 800;

  background: var(--xunye-gold);

  border-radius: 12px;

  line-height: 46px;

  overflow: hidden;
}

.code-button-text {
  display: inline-block;

  width: 100%;

  color: inherit;

  font-size: 12px;

  font-weight: 800;

  line-height: 46px;

  text-align: center;

  white-space: nowrap;
}

.code-button.disabled {
  color: rgba(247, 241, 232, 0.52);

  background: rgba(255, 255, 255, 0.08);
}

.register-actions {
  display: flex;
  gap: 10px;
  margin-top: 4px;
}
.cancel-button,
.submit-button {
  flex: 1;
  height: 46px;
  padding: 0 12px;
  font-size: 14px;
  font-weight: 800;
  border-radius: 12px;
  line-height: 46px;
}
/* 登录弹窗中单个取消按钮与大按钮对齐 */
.register-actions > .cancel-button:only-child {
  padding: 0 18px;
}
.cancel-button {
  color: #f7f1e8;
  background: rgba(255, 255, 255, 0.08);
}
.submit-button {
  color: #111318;
  background: var(--xunye-gold);
}
.dialog-big-button {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  box-sizing: border-box;
  width: 100%;
  min-height: 50px;
  margin-bottom: 10px;
  padding: 0 18px;
  color: #111318;
  font-size: 15px;
  font-weight: 800;
  background: var(--xunye-gold);
  border: 0;
  border-radius: 14px;
  line-height: 50px;
}
.dialog-big-button.phone-login-btn {
  color: #f7f1e8;
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.16);
}
.login-tabs {
  display: flex;
  gap: 0;
  margin-bottom: 14px;
  background: rgba(255, 255, 255, 0.06);
  border-radius: 12px;
  overflow: hidden;
}
.login-tab {
  flex: 1;
  height: 42px;
  color: var(--xunye-muted);
  font-size: 14px;
  font-weight: 700;
  background: transparent;
  border: 0;
  border-radius: 0;
  line-height: 42px;
}
.login-tab.active {
  color: #111318;
  background: var(--xunye-gold);
}
.gold {
  color: var(--xunye-gold);
}
</style>
