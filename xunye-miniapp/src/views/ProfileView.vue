<script lang="ts" setup>
import type { CustomerTableVO } from '@/api/customer'
import type { CustomerProfileUpdateDTO } from '@/api/membership'
import { computed, onMounted, reactive, ref } from 'vue'
import { listCustomerTables } from '@/api/customer'
import { useShellState } from '@/composables/useShellState'
import { useCustomerProfileStore } from '@/store/customerProfile'
import { getEnvBaseUrl } from '@/utils'

const { back, showToast } = useShellState()
const customerProfileStore = useCustomerProfileStore()
const profile = customerProfileStore.profile
const editPopupOpen = ref(false)
const profileEditOpen = ref(false)
const editingField = ref<keyof CustomerProfileUpdateDTO | ''>('')
const editingLabel = ref('')
const editingValue = ref('')
const loading = ref(false)
const profileDraft = reactive({
  nickname: '',
  phone: '',
  birthday: '',
  gender: '',
  favoriteTaste: '',
  favoriteTable: '',
})
const genderOptions = ['男', '女']
const tasteOptions = ['清爽微酸 / 琴酒基底', '果香偏甜 / 低酒精', '烟熏辛口 / 威士忌基底', '无酒精 / 清爽气泡']
const tables = ref<CustomerTableVO[]>([])
const tableOptions = computed(() => tables.value.map(item => item.tableCode || item.name))
const birthdayStart = '1900-01-01'
const birthdayEnd = new Date().toISOString().slice(0, 10)
const avatarUploadUrl = `${getEnvBaseUrl()}/api/customer/member/avatar`
const editableFields = ['nickname', 'phone', 'birthday', 'gender', 'favoriteTaste', 'favoriteTable', 'avatar'] as const

const maskedPhone = computed(() => profile.phone.replace(/^(\d{3})\d{4}(\d+)/, '$1 **** $2'))

onMounted(() => {
  fetchProfile()
  fetchTables()
})

async function fetchProfile() {
  try {
    loading.value = true
    await customerProfileStore.fetchProfile()
  }
  catch {
    showToast('资料读取失败，先使用本地资料')
  }
  finally {
    loading.value = false
  }
}

async function fetchTables() {
  tables.value = await listCustomerTables().catch(() => [])
}

function openEditor(field: keyof CustomerProfileUpdateDTO, label: string, value: string | null | undefined) {
  if (!editableFields.includes(field as any) || typeof label !== 'string') {
    return
  }
  editingField.value = field
  editingLabel.value = label
  editingValue.value = typeof value === 'string' ? value : ''
  editPopupOpen.value = true
}

function openNicknameEditor() {
  openEditor('nickname', '昵称', profile.nickname)
}

function openPhoneEditor() {
  openEditor('phone', '手机号', profile.phone)
}

function openBirthdayEditor() {
  editingValue.value = profile.birthday || ''
}

function openGenderEditor() {
  openEditor('gender', '性别', profile.gender)
}

function openTasteEditor() {
  openEditor('favoriteTaste', '口味偏好', profile.favoriteTaste)
}

function openFavoriteTableEditor() {
  openEditor('favoriteTable', '常用桌台', profile.favoriteTable)
}

function closeEditor() {
  editPopupOpen.value = false
}

function openProfileEditor() {
  profileDraft.nickname = profile.nickname
  profileDraft.phone = profile.phone
  profileDraft.birthday = profile.birthday
  profileDraft.gender = profile.gender
  profileDraft.favoriteTaste = profile.favoriteTaste
  profileDraft.favoriteTable = profile.favoriteTable
  profileEditOpen.value = true
}

function closeProfileEditor() {
  profileEditOpen.value = false
}

async function saveProfile(patch: Partial<CustomerProfileUpdateDTO> = {}) {
  const sanitized: Partial<CustomerProfileUpdateDTO> = {}
  editableFields.forEach((field) => {
    const value = patch[field]
    if (typeof value === 'string' || value === null) {
      ;(sanitized as any)[field] = value
    }
  })
  await customerProfileStore.saveProfile(sanitized)
}

async function submitProfileEditor() {
  try {
    await saveProfile({
      nickname: profileDraft.nickname.trim(),
      phone: profileDraft.phone.trim(),
      birthday: profileDraft.birthday.trim() || null,
      gender: profileDraft.gender.trim() || null,
      favoriteTaste: profileDraft.favoriteTaste.trim() || null,
      favoriteTable: profileDraft.favoriteTable.trim() || null,
    })
    showToast('资料已保存')
    closeProfileEditor()
  }
  catch {
    showToast('保存失败，请检查后端服务')
  }
}

async function submitEditor() {
  if (!editingField.value)
    return
  try {
    await saveProfile({ [editingField.value]: editingValue.value } as Partial<CustomerProfileUpdateDTO>)
    showToast(`${editingLabel.value}已保存`)
    closeEditor()
  }
  catch {
    showToast('保存失败，请检查后端服务')
  }
}

async function selectGender(value: string) {
  if (!genderOptions.includes(value))
    return
  editingValue.value = value
  try {
    await saveProfile({ gender: value })
    showToast('性别已保存')
    closeEditor()
  }
  catch {
    showToast('保存失败，请检查后端服务')
  }
}

async function selectBirthday(value: string) {
  if (!/^\d{4}-\d{2}-\d{2}$/.test(value))
    return
  editingValue.value = value
  try {
    await saveProfile({ birthday: value })
    showToast('生日已保存')
    closeEditor()
  }
  catch {
    showToast('保存失败，请检查后端服务')
  }
}

function handleBirthdayChange(event: any) {
  const value = event.detail.value
  selectBirthday(value)
}

function handleDraftBirthdayChange(event: any) {
  const value = event.detail.value
  if (/^\d{4}-\d{2}-\d{2}$/.test(value)) {
    profileDraft.birthday = value
  }
}

async function selectTaste(value: string) {
  if (!tasteOptions.includes(value))
    return
  editingValue.value = value
  try {
    await saveProfile({ favoriteTaste: value })
    showToast('口味偏好已保存')
    closeEditor()
  }
  catch {
    showToast('保存失败，请检查后端服务')
  }
}

async function selectFavoriteTable(value: string) {
  if (!tableOptions.value.includes(value))
    return
  editingValue.value = value
  try {
    await saveProfile({ favoriteTable: value })
    showToast('常用桌台已保存')
    closeEditor()
  }
  catch {
    showToast('保存失败，请检查后端服务')
  }
}

function chooseAvatar() {
  uni.chooseMedia({
    count: 1,
    mediaType: ['image'],
    sourceType: ['album', 'camera'],
    success: (res) => {
      const avatar = res.tempFiles[0]?.tempFilePath
      if (!avatar)
        return
      uploadAvatar(avatar)
    },
  })
}

function normalizeImageUrl(url: string) {
  if (url.startsWith('http'))
    return url
  return `${getEnvBaseUrl()}${url}`
}

function uploadAvatar(filePath: string) {
  uni.uploadFile({
    url: avatarUploadUrl,
    filePath,
    name: 'file',
    formData: {
      phone: profile.phone,
    },
    success: async (res) => {
      try {
        const body = JSON.parse(res.data)
        const avatar = body.data
        profile.avatar = normalizeImageUrl(avatar)
        await saveProfile({ avatar })
        showToast('头像已保存')
      }
      catch {
        showToast('头像上传响应异常')
      }
    },
    fail: () => {
      showToast('头像上传失败')
    },
  })
}
</script>

<template>
  <view class="view profile-view">
    <view class="topbar profile-topbar">
      <button class="icon-button" hover-class="none" @tap="() => back()">
        <uv-icon name="arrow-left" color="#f7f1e8" size="20" />
      </button>
      <view class="top-title">
        个人信息
      </view>
      <button class="icon-button ghost" hover-class="none">
        <uv-icon name="arrow-left" color="#f7f1e8" size="20" />
      </button>
    </view>

    <scroll-view scroll-y class="content view-scroll" enhanced show-scrollbar="false">
      <view class="profile-hero">
        <image class="profile-avatar" :src="profile.avatar" mode="aspectFill" lazy-load @tap="() => chooseAvatar()" />
        <view class="profile-copy">
          <view class="profile-name">
            {{ profile.nickname }}
          </view>
          <view class="muted small">
            {{ profile.levelText }}
          </view>
          <view class="muted mini">
            ID {{ profile.customerNo || '生成中' }}
          </view>
        </view>
        <button class="edit-pill" hover-class="none" @tap.stop="() => openProfileEditor()">
          编辑
        </button>
      </view>

      <view class="profile-stats">
        <view>
          <view class="stat-num">
            {{ profile.totalOrders }}
          </view>
          <view class="muted mini">
            到店次数
          </view>
        </view>
        <view class="divider" />
        <view>
          <view class="stat-num">
            {{ profile.favoriteTable }}
          </view>
          <view class="muted mini">
            常用桌台
          </view>
        </view>
        <view class="divider" />
        <view>
          <view class="stat-num">
            {{ profile.points }}
          </view>
          <view class="muted mini">
            可用积分
          </view>
        </view>
      </view>

      <view class="cell-panel">
        <view class="profile-row" @tap="() => openNicknameEditor()">
          <view class="row-title">
            <uv-icon name="account" color="#d2a85f" size="17" />
            <text>昵称</text>
          </view>
          <view class="row-value">
            <text>{{ profile.nickname }}</text>
            <uv-icon name="arrow-right" color="#8d929d" size="14" />
          </view>
        </view>
        <view class="profile-row" @tap="() => openPhoneEditor()">
          <view class="row-title">
            <uv-icon name="phone-fill" color="#d2a85f" size="17" />
            <text>手机号</text>
          </view>
          <view class="row-value">
            <text>{{ maskedPhone }}</text>
            <uv-icon name="arrow-right" color="#8d929d" size="14" />
          </view>
        </view>
        <picker
          mode="date"
          :value="profile.birthday || birthdayEnd"
          :start="birthdayStart"
          :end="birthdayEnd"
          @change="handleBirthdayChange"
        >
          <view class="profile-row">
            <view class="row-title">
              <uv-icon name="calendar" color="#d2a85f" size="17" />
              <text>生日</text>
            </view>
            <view class="row-value">
              <text>{{ profile.birthday || '未设置' }}</text>
              <uv-icon name="arrow-right" color="#8d929d" size="14" />
            </view>
          </view>
        </picker>
        <view class="profile-row" @tap="() => openGenderEditor()">
          <view class="row-title">
            <uv-icon name="man" color="#d2a85f" size="17" />
            <text>性别</text>
          </view>
          <view class="row-value">
            <text>{{ profile.gender || '未设置' }}</text>
            <uv-icon name="arrow-right" color="#8d929d" size="14" />
          </view>
        </view>
        <view class="profile-row" @tap="() => openTasteEditor()">
          <view class="row-title">
            <uv-icon name="tags-fill" color="#d2a85f" size="17" />
            <text>口味偏好</text>
          </view>
          <view class="row-value">
            <text>{{ profile.favoriteTaste || '未设置' }}</text>
            <uv-icon name="arrow-right" color="#8d929d" size="14" />
          </view>
        </view>
        <view class="profile-row last" @tap="() => openFavoriteTableEditor()">
          <view class="row-title">
            <uv-icon name="map-fill" color="#d2a85f" size="17" />
            <text>常用桌台</text>
          </view>
          <view class="row-value">
            <text>{{ profile.favoriteTable || '未设置' }}</text>
            <uv-icon name="arrow-right" color="#8d929d" size="14" />
          </view>
        </view>
      </view>

      <view class="panel">
        <view class="panel-title">
          顾客偏好
        </view>
        <view class="preference-card">
          <uv-icon name="tags-fill" color="#d2a85f" size="20" />
          <view>
            <view class="bold">
              {{ profile.favoriteTaste }}
            </view>
            <view class="muted small">
              用于推荐特调和默认备注
            </view>
          </view>
        </view>
        <view class="preference-card">
          <uv-icon name="clock-fill" color="#d2a85f" size="20" />
          <view>
            <view class="bold">
              上次到店 {{ profile.lastVisitAt || '暂无记录' }}
            </view>
            <view class="muted small">
              历史消费会逐步沉淀成顾客画像
            </view>
          </view>
        </view>
      </view>
    </scroll-view>

    <view v-if="editPopupOpen" class="sheet-mask" @tap="() => closeEditor()">
      <view class="edit-panel sheet-panel" @tap.stop>
        <view class="edit-title">
          编辑{{ editingLabel }}
        </view>
        <view v-if="editingField === 'gender'" class="gender-segment">
          <button
            v-for="item in genderOptions"
            :key="item"
            class="gender-option"
            :class="{ active: editingValue === item }"
            hover-class="none"
            @tap="() => selectGender(item)"
          >
            {{ item }}
          </button>
        </view>
        <view v-else-if="editingField === 'favoriteTaste'" class="option-list">
          <button
            v-for="item in tasteOptions"
            :key="item"
            class="list-option"
            :class="{ active: editingValue === item }"
            hover-class="none"
            @tap="() => selectTaste(item)"
          >
            {{ item }}
          </button>
        </view>
        <view v-else-if="editingField === 'favoriteTable'" class="option-grid">
          <button
            v-for="item in tableOptions"
            :key="item"
            class="gender-option"
            :class="{ active: editingValue === item }"
            hover-class="none"
            @tap="() => selectFavoriteTable(item)"
          >
            {{ item }}
          </button>
        </view>
        <uv-input
          v-else
          v-model="editingValue"
          border="surround"
          clearable
          :placeholder="`请输入${editingLabel}`"
          color="#f7f1e8"
          placeholder-style="color: #8d929d"
          custom-style="background: rgba(255,255,255,0.06); border-color: rgba(255,255,255,0.12); border-radius: 12px;"
        />
        <view v-if="!['gender', 'favoriteTaste', 'favoriteTable'].includes(editingField)" class="edit-actions">
          <button class="sheet-button muted-button" hover-class="none" @tap="() => closeEditor()">
            取消
          </button>
          <button class="sheet-button save-button" hover-class="none" @tap="() => submitEditor()">
            保存
          </button>
        </view>
      </view>
    </view>

    <view v-if="profileEditOpen" class="sheet-mask" @tap="() => closeProfileEditor()">
      <scroll-view scroll-y class="profile-edit-panel" enhanced show-scrollbar="false" @tap.stop>
        <view class="edit-title">
          编辑资料
        </view>
        <view class="form-stack">
          <view class="form-field">
            <view class="form-label">
              昵称
            </view>
            <uv-input v-model="profileDraft.nickname" border="surround" clearable color="#f7f1e8" placeholder="请输入昵称" placeholder-style="color: #8d929d" custom-style="background: rgba(255,255,255,0.06); border-color: rgba(255,255,255,0.12); border-radius: 12px;" />
          </view>
          <view class="form-field">
            <view class="form-label">
              手机号
            </view>
            <uv-input v-model="profileDraft.phone" border="surround" clearable color="#f7f1e8" type="number" placeholder="请输入手机号" placeholder-style="color: #8d929d" custom-style="background: rgba(255,255,255,0.06); border-color: rgba(255,255,255,0.12); border-radius: 12px;" />
          </view>
          <view class="form-row">
            <view class="form-field">
              <view class="form-label">
                生日
              </view>
              <picker
                mode="date"
                :value="profileDraft.birthday || birthdayEnd"
                :start="birthdayStart"
                :end="birthdayEnd"
                @change="handleDraftBirthdayChange"
              >
                <view class="picker-field compact">
                  {{ profileDraft.birthday || '请选择' }}
                </view>
              </picker>
            </view>
            <view class="form-field">
              <view class="form-label">
                性别
              </view>
              <view class="gender-segment compact">
                <button
                  v-for="item in genderOptions"
                  :key="item"
                  class="gender-option"
                  :class="{ active: profileDraft.gender === item }"
                  hover-class="none"
                  @tap="() => { profileDraft.gender = item }"
                >
                  {{ item }}
                </button>
              </view>
            </view>
          </view>
          <view class="form-field">
            <view class="form-label">
              口味偏好
            </view>
            <picker
              :range="tasteOptions"
              :value="Math.max(0, tasteOptions.indexOf(profileDraft.favoriteTaste))"
              @change="profileDraft.favoriteTaste = tasteOptions[$event.detail.value]"
            >
              <view class="picker-field compact">
                {{ profileDraft.favoriteTaste || '请选择' }}
              </view>
            </picker>
          </view>
          <view class="form-field">
            <view class="form-label">
              常用桌台
            </view>
            <picker
              :range="tableOptions"
              :value="Math.max(0, tableOptions.indexOf(profileDraft.favoriteTable))"
              @change="profileDraft.favoriteTable = tableOptions[$event.detail.value]"
            >
              <view class="picker-field compact">
                {{ profileDraft.favoriteTable || '请选择' }}
              </view>
            </picker>
          </view>
        </view>
        <view class="edit-actions">
          <button class="sheet-button muted-button" hover-class="none" @tap="() => closeProfileEditor()">
            取消
          </button>
          <button class="sheet-button save-button" hover-class="none" @tap="() => submitProfileEditor()">
            保存资料
          </button>
        </view>
      </scroll-view>
    </view>
  </view>
</template>

<style scoped>
.profile-view {
  width: 100%;
  overflow-x: hidden;
}
.profile-topbar {
  box-sizing: border-box;
  width: 100%;
  padding-right: calc(var(--xunye-menu-right, 0px) + var(--xunye-menu-width, 0px) + 12px);
}
.content {
  box-sizing: border-box;
  width: 100%;
  max-width: 100%;
  overflow-x: hidden;
}
.profile-hero,
.profile-stats,
.cell-panel {
  box-sizing: border-box;
  width: 100%;
  margin-bottom: 12px;
  background: rgba(21, 23, 27, 0.94);
  border: 1px solid var(--xunye-line);
  border-radius: 18px;
}
.profile-hero {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 16px;
  overflow: hidden;
}
.profile-avatar {
  width: 58px;
  height: 58px;
  flex-shrink: 0;
  background: var(--xunye-surface-2);
  border-radius: 999px;
}
.profile-copy {
  flex: 1;
  min-width: 0;
}
.profile-name {
  margin-bottom: 4px;
  font-size: 19px;
  font-weight: 800;
}
.profile-stats {
  display: flex;
  align-items: center;
  justify-content: space-around;
  padding: 16px;
  text-align: center;
}
.profile-stats > view {
  flex: 1;
  min-width: 0;
}
.stat-num {
  color: var(--xunye-gold);
  font-size: 20px;
  font-weight: 800;
}
.divider {
  width: 1px;
  height: 34px;
  background: var(--xunye-line);
}
.cell-panel {
  padding: 0 16px;
  overflow: hidden;
}
.profile-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  min-height: 49px;
  border-bottom: 1px solid var(--xunye-line);
}
.profile-row.last {
  border-bottom: 0;
}
.row-title {
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 88px;
  color: #f7f1e8;
  font-size: 15px;
  font-weight: 700;
}
.row-value {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 4px;
  min-width: 0;
  color: var(--xunye-muted);
  font-size: 14px;
}
.row-value text {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.edit-pill {
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  width: 54px;
  height: 32px;
  color: var(--xunye-gold);
  font-size: 13px;
  font-weight: 700;
  background: transparent;
  border: 1px solid rgba(210, 168, 95, 0.45);
  border-radius: 999px;
}
.preference-card {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 0;
  border-bottom: 1px solid var(--xunye-line);
}
.preference-card > view {
  min-width: 0;
}
.preference-card:last-child {
  border-bottom: 0;
}
.edit-panel {
  box-sizing: border-box;
  padding: 18px 18px calc(env(safe-area-inset-bottom, 8px) + 18px);
  background: var(--xunye-surface);
}
.sheet-panel {
  position: fixed;
  right: 0;
  bottom: 0;
  left: 0;
  z-index: 1001;
  border-radius: 18px 18px 0 0;
}
.profile-edit-panel {
  box-sizing: border-box;
  position: fixed;
  right: 0;
  bottom: 0;
  left: 0;
  z-index: 1001;
  max-height: 74vh;
  padding: 18px 18px calc(env(safe-area-inset-bottom, 8px) + 18px);
  background: var(--xunye-surface);
  border-radius: 18px 18px 0 0;
}
.edit-title {
  margin-bottom: 14px;
  font-size: 17px;
  font-weight: 800;
  text-align: center;
}
.form-stack {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.form-row {
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(0, 1fr);
  gap: 10px;
}
.form-field {
  min-width: 0;
}
.form-label {
  margin-bottom: 7px;
  color: var(--xunye-muted);
  font-size: 12px;
}
.gender-segment {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px;
}
.option-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 10px;
}
.option-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.gender-segment.compact {
  height: 38px;
}
.gender-option,
.list-option,
.picker-field {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 40px;
  color: var(--xunye-muted);
  font-size: 14px;
  font-weight: 800;
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.12);
  border-radius: 12px;
}
.list-option {
  justify-content: flex-start;
  padding: 0 14px;
}
.picker-field {
  justify-content: flex-start;
  box-sizing: border-box;
  width: 100%;
  padding: 0 14px;
}
.picker-field.compact {
  height: 40px;
}
.gender-option.active,
.list-option.active {
  color: #111318;
  background: var(--xunye-gold);
  border-color: var(--xunye-gold);
}
.edit-actions {
  display: flex;
  gap: 12px;
  margin-top: 16px;
}
.sheet-mask {
  position: fixed;
  inset: 0;
  z-index: 1000;
  background: rgba(0, 0, 0, 0.56);
}
.sheet-button {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  height: 42px;
  font-size: 14px;
  font-weight: 800;
  border-radius: 999px;
}
.muted-button {
  color: var(--xunye-muted);
  background: transparent;
  border: 1px solid rgba(255, 255, 255, 0.14);
}
.save-button {
  color: #111318;
  background: linear-gradient(135deg, #d2a85f, #bc8945);
}
</style>
