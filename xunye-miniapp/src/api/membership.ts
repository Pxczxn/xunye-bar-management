import { http } from '@/http/http'

export interface CustomerInfoVO {
  id: number
  customerNo: string
  openid: string | null
  phone: string
  nickname: string
  avatar: string | null
  birthday: string | null
  gender: string | null
  favoriteTaste: string | null
  favoriteTable: string | null
  memberLevel: string
  memberLevelName: string
  points: number
  balance: number
  totalOrders: number
  totalAmount: number
  lastVisitAt: string | null
  createdAt: string | null
}

export interface CustomerWxLoginDTO {
  code: string
  customerNo?: string
  phone?: string
  verifyCode?: string
  nickname?: string
  avatar?: string
}

export interface CustomerProfileUpdateDTO {
  customerNo?: string
  phone: string
  nickname?: string
  avatar?: string | null
  birthday?: string | null
  gender?: string | null
  favoriteTaste?: string | null
  favoriteTable?: string | null
}

export interface MemberLevelVO {
  level: string
  name: string
  minAmount: number
  discount: number
  pointsRate: number
  description: string
  sort: number
}

export interface ActivityVO {
  id: number
  title: string
  description: string | null
  type: string
  startDate: string | null
  endDate: string | null
  coverImage: string | null
  status: number
  sort: number
}

export function getMemberInfo(phone?: string) {
  return http.get<CustomerInfoVO>('/api/customer/member/info', { phone: phone || '' })
}

export function updateCustomerProfile(data: CustomerProfileUpdateDTO) {
  return http.put<CustomerInfoVO>('/api/customer/member/profile', data)
}

export function wxLoginCustomer(data: CustomerWxLoginDTO) {
  return http.post<CustomerInfoVO>('/api/customer/member/wx-login', data)
}

export function registerCustomer(data: CustomerWxLoginDTO) {
  return http.post<CustomerInfoVO>('/api/customer/member/register', data)
}

export function sendRegisterCode(phone: string) {
  return http.post<void>('/api/customer/member/register-code', undefined, { phone })
}

export function sendLoginCode(phone: string) {
  return http.post<void>('/api/customer/member/login-code', undefined, { phone })
}

export function phoneLoginByCode(phone: string, verifyCode: string) {
  return http.post<CustomerInfoVO>('/api/customer/member/phone-login-by-code', { phone, verifyCode })
}

export function phoneLoginByPassword(phone: string, password: string) {
  return http.post<CustomerInfoVO>('/api/customer/member/phone-login-by-password', { phone, password })
}

export function setPassword(phone: string, password: string, verifyCode: string) {
  return http.post<void>('/api/customer/member/set-password', { phone, password, verifyCode })
}

export function getMemberLevels() {
  return http.get<MemberLevelVO[]>('/api/customer/member/levels')
}

export function getActiveActivities() {
  return http.get<ActivityVO[]>('/api/customer/activities')
}
