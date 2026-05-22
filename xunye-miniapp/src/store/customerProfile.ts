import type { CustomerInfoVO, CustomerProfileUpdateDTO } from '@/api/membership'
import { defineStore } from 'pinia'
import { reactive, ref } from 'vue'
import { getMemberInfo, updateCustomerProfile } from '@/api/membership'
import { getEnvBaseUrl } from '@/utils'

const defaultAvatar = '/static/images/avatar.jpg'
const storagePhoneKey = 'xunyeCustomerPhone'
const storageLoginKey = 'xunyeCustomerLoggedIn'

const emptyProfile = {
  customerNo: '',
  openid: '',
  phone: '',
  nickname: '未登录',
  avatar: defaultAvatar,
  birthday: '',
  gender: '',
  memberLevelName: '游客',
  levelText: '登录后查看会员权益',
  favoriteTaste: '',
  favoriteTable: '',
  totalOrders: 0,
  points: 0,
  lastVisitAt: '',
}

function cleanString(value: unknown, fallback = '') {
  return typeof value === 'string' && value.trim() ? value.trim() : fallback
}

function cleanNumber(value: unknown, fallback = 0) {
  const number = Number(value)
  return Number.isFinite(number) && number >= 0 ? number : fallback
}

function cleanPhone(value: unknown) {
  const phone = cleanString(value)
  return /^(?:1[3-9]\d{9}|WX\d+)$/.test(phone) ? phone : ''
}

function cleanCustomerNo(value: unknown) {
  const customerNo = cleanString(value)
  return /^XY\d{12,}$/.test(customerNo) ? customerNo : ''
}

function cleanDate(value: unknown) {
  const date = cleanString(value)
  return /^\d{4}-\d{2}-\d{2}$/.test(date) ? date : ''
}

function normalizeAvatar(value: unknown) {
  const avatar = cleanString(value)
  if (!avatar || avatar.includes('/static/images/default-avatar.png')) {
    return defaultAvatar
  }
  if (avatar.startsWith('http')) {
    return avatar
  }
  return `${getEnvBaseUrl()}${avatar}`
}

function hasProfileIdentity(data?: Partial<CustomerInfoVO> | null) {
  return !!cleanCustomerNo(data?.customerNo) || !!cleanPhone(data?.phone)
}

export const useCustomerProfileStore = defineStore(
  'customerProfile',
  () => {
    const profile = reactive({ ...emptyProfile })
    const isLoggedIn = ref(uni.getStorageSync(storageLoginKey) === '1')

    function resetProfile() {
      Object.assign(profile, emptyProfile)
      uni.removeStorageSync(storagePhoneKey)
      uni.removeStorageSync(storageLoginKey)
      isLoggedIn.value = false
    }

    function apply(data: Partial<CustomerInfoVO> | null | undefined) {
      if (!hasProfileIdentity(data)) {
        resetProfile()
        return
      }

      profile.customerNo = cleanCustomerNo(data?.customerNo)
      profile.openid = cleanString(data?.openid)
      profile.phone = cleanPhone(data?.phone)
      profile.nickname = cleanString(data?.nickname, '寻野会员')
      profile.avatar = normalizeAvatar(data?.avatar)
      profile.birthday = cleanDate(data?.birthday)
      profile.gender = cleanString(data?.gender)
      profile.memberLevelName = cleanString(data?.memberLevelName, '普通会员')
      profile.levelText = profile.memberLevelName
      profile.favoriteTaste = cleanString(data?.favoriteTaste)
      profile.favoriteTable = cleanString(data?.favoriteTable)
      profile.totalOrders = cleanNumber(data?.totalOrders)
      profile.points = cleanNumber(data?.points)
      profile.lastVisitAt = data?.lastVisitAt ? String(data.lastVisitAt).slice(0, 10) : ''

      uni.setStorageSync(storagePhoneKey, profile.phone)
      uni.setStorageSync(storageLoginKey, '1')
      isLoggedIn.value = true
    }

    async function fetchProfile(phone = profile.phone) {
      if (!isLoggedIn.value) {
        return null
      }

      const data = await getMemberInfo(cleanPhone(phone))
      if (!hasProfileIdentity(data)) {
        resetProfile()
        return null
      }
      apply(data)
      return data
    }

    async function saveProfile(patch: Partial<CustomerProfileUpdateDTO> = {}) {
      if (!isLoggedIn.value) {
        throw new Error('Customer is not logged in')
      }

      const patchAvatar = patch.avatar
      const normalizedPatchAvatar = patchAvatar === undefined ? undefined : normalizeAvatar(patchAvatar)
      const payload: CustomerProfileUpdateDTO = {
        customerNo: profile.customerNo || patch.customerNo,
        phone: cleanPhone(patch.phone || profile.phone),
        nickname: cleanString(patch.nickname ?? profile.nickname, '寻野会员'),
        avatar: normalizedPatchAvatar ?? normalizeAvatar(profile.avatar),
        birthday: cleanDate(patch.birthday ?? profile.birthday) || null,
        gender: cleanString(patch.gender ?? profile.gender) || null,
        favoriteTaste: cleanString(patch.favoriteTaste ?? profile.favoriteTaste),
        favoriteTable: cleanString(patch.favoriteTable ?? profile.favoriteTable),
        ...patch,
      }
      if (normalizedPatchAvatar !== undefined) {
        payload.avatar = normalizedPatchAvatar
      }
      const data = await updateCustomerProfile(payload)
      apply(data)
      return data
    }

    return {
      profile,
      isLoggedIn,
      apply,
      resetProfile,
      fetchProfile,
      saveProfile,
    }
  },
  {
    persist: true,
  },
)
