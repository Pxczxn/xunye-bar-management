import type { CustomerInfoVO, CustomerProfileUpdateDTO } from '@/api/membership'
import { defineStore } from 'pinia'
import { reactive } from 'vue'
import { getMemberInfo, updateCustomerProfile } from '@/api/membership'

const defaultPhone = '13800000921'
const defaultAvatar = '/static/images/avatar.jpg'
const storagePhoneKey = 'xunyeCustomerPhone'
const defaultProfile = {
  customerNo: '',
  openid: '',
  phone: defaultPhone,
  nickname: '寻野会员',
  avatar: defaultAvatar,
  birthday: '',
  gender: '',
  memberLevelName: '普通会员',
  levelText: '普通会员 Lv.2',
  favoriteTaste: '清爽微酸 / 琴酒基底',
  favoriteTable: 'A08',
  totalOrders: 0,
  points: 0,
  lastVisitAt: '',
}
const validGender = ['男', '女']
const validMemberNames = ['普通会员', '银卡会员', '金卡会员', '铂金会员', '钻石会员', 'VIP会员', 'SVIP会员', '寻野会员']
const validTastes = ['清爽微酸 / 琴酒基底', '果香偏甜 / 低酒精', '烟熏辛口 / 威士忌基底', '无酒精 / 清爽气泡']

function isDirtyTapValue(value?: unknown) {
  if (typeof value !== 'string') {
    return false
  }
  const trimmed = value.trim()
  return /^\d+,\d+(?:,\d+)?$/.test(trimmed) || /^[a-z]\d+(?:,\d+)+$/i.test(trimmed) || /^e\d+$/i.test(trimmed)
}

function cleanString(value: unknown, fallback = '') {
  if (typeof value !== 'string') {
    return fallback
  }
  const trimmed = value.trim()
  if (!trimmed || isDirtyTapValue(trimmed)) {
    return fallback
  }
  return trimmed
}

function cleanNumber(value: unknown, fallback = 0) {
  const number = Number(value)
  return Number.isFinite(number) && number >= 0 ? number : fallback
}

function cleanPhone(value: unknown, fallback = defaultPhone) {
  const phone = cleanString(value, fallback)
  return /^(?:\d{6,20}|WX\d{8,})$/.test(phone) ? phone : fallback
}

function cleanCustomerNo(value: unknown) {
  const customerNo = cleanString(value)
  return /^XY\d{12,}$/.test(customerNo) ? customerNo : ''
}

function cleanDate(value: unknown) {
  const date = cleanString(value)
  return /^\d{4}-\d{2}-\d{2}$/.test(date) ? date : ''
}

function cleanGender(value: unknown) {
  const gender = cleanString(value)
  return validGender.includes(gender) ? gender : ''
}

function cleanMemberName(value: unknown) {
  const name = cleanString(value, defaultProfile.memberLevelName)
  return validMemberNames.includes(name) ? name : defaultProfile.memberLevelName
}

function cleanTaste(value: unknown, fallback = defaultProfile.favoriteTaste) {
  const taste = cleanString(value, fallback)
  return validTastes.includes(taste) ? taste : fallback
}

function cleanTable(value: unknown, fallback = defaultProfile.favoriteTable) {
  const table = cleanString(value, fallback)
  return /^[a-z0-9-]{1,12}$/i.test(table) ? table : fallback
}

function normalizeAvatar(avatar?: string | null) {
  if (!avatar || avatar.includes('/static/images/default-avatar.png')) {
    return defaultAvatar
  }
  return avatar
}

export const useCustomerProfileStore = defineStore(
  'customerProfile',
  () => {
    const profile = reactive({
      ...defaultProfile,
      phone: cleanPhone(uni.getStorageSync(storagePhoneKey)),
    })

    function cleanProfile() {
      profile.phone = cleanPhone(profile.phone)
      profile.customerNo = cleanCustomerNo(profile.customerNo)
      profile.openid = cleanString(profile.openid)
      profile.nickname = cleanString(profile.nickname, defaultProfile.nickname)
      profile.avatar = normalizeAvatar(cleanString(profile.avatar, defaultAvatar))
      profile.birthday = cleanDate(profile.birthday)
      profile.gender = cleanGender(profile.gender)
      profile.memberLevelName = cleanMemberName(profile.memberLevelName)
      profile.levelText = `${profile.memberLevelName} Lv.2`
      profile.favoriteTaste = cleanTaste(profile.favoriteTaste)
      profile.favoriteTable = cleanTable(profile.favoriteTable)
      profile.totalOrders = cleanNumber(profile.totalOrders)
      profile.points = cleanNumber(profile.points)
      profile.lastVisitAt = cleanDate(profile.lastVisitAt)
      uni.setStorageSync(storagePhoneKey, profile.phone)
    }

    function apply(data: Partial<CustomerInfoVO>) {
      profile.phone = cleanPhone(data.phone || profile.phone)
      profile.customerNo = cleanCustomerNo(data.customerNo || profile.customerNo)
      profile.openid = cleanString(data.openid || profile.openid)
      uni.setStorageSync(storagePhoneKey, profile.phone)
      profile.nickname = cleanString(data.nickname || profile.nickname, defaultProfile.nickname)
      profile.avatar = normalizeAvatar(cleanString(data.avatar || profile.avatar, defaultAvatar))
      profile.birthday = cleanDate(data.birthday)
      profile.gender = cleanGender(data.gender)
      profile.memberLevelName = cleanMemberName(data.memberLevelName)
      profile.levelText = `${profile.memberLevelName} Lv.2`
      profile.favoriteTaste = cleanTaste(data.favoriteTaste || profile.favoriteTaste)
      profile.favoriteTable = cleanTable(data.favoriteTable || profile.favoriteTable)
      profile.totalOrders = cleanNumber(data.totalOrders ?? profile.totalOrders)
      profile.points = cleanNumber(data.points ?? profile.points)
      profile.lastVisitAt = data.lastVisitAt ? String(data.lastVisitAt).slice(0, 10) : ''
      cleanProfile()
    }

    async function fetchProfile(phone = profile.phone) {
      cleanProfile()
      profile.phone = cleanPhone(phone || profile.phone)
      const data = await getMemberInfo(phone)
      apply(data)
      return data
    }

    async function saveProfile(patch: Partial<CustomerProfileUpdateDTO> = {}) {
      cleanProfile()
      const payload: CustomerProfileUpdateDTO = {
        phone: cleanPhone(patch.phone || profile.phone),
        nickname: cleanString(patch.nickname ?? profile.nickname, defaultProfile.nickname),
        avatar: normalizeAvatar(cleanString(patch.avatar ?? profile.avatar, defaultAvatar)),
        birthday: cleanDate(patch.birthday ?? profile.birthday) || null,
        gender: cleanGender(patch.gender ?? profile.gender) || null,
        favoriteTaste: cleanTaste(patch.favoriteTaste ?? profile.favoriteTaste),
        favoriteTable: cleanTable(patch.favoriteTable ?? profile.favoriteTable),
        ...patch,
      }
      payload.phone = cleanPhone(payload.phone)
      payload.nickname = cleanString(payload.nickname, defaultProfile.nickname)
      payload.avatar = normalizeAvatar(cleanString(payload.avatar, defaultAvatar))
      payload.birthday = cleanDate(payload.birthday) || null
      payload.gender = cleanGender(payload.gender) || null
      payload.favoriteTaste = cleanTaste(payload.favoriteTaste)
      payload.favoriteTable = cleanTable(payload.favoriteTable)
      profile.phone = payload.phone
      uni.setStorageSync(storagePhoneKey, profile.phone)
      const data = await updateCustomerProfile(payload)
      apply(data)
      return data
    }

    return {
      profile,
      apply,
      cleanProfile,
      fetchProfile,
      saveProfile,
    }
  },
  {
    persist: true,
  },
)
