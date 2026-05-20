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
  nickname?: string
  avatar?: string
}

export interface CustomerProfileUpdateDTO {
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

export function getMemberLevels() {
  return http.get<MemberLevelVO[]>('/api/customer/member/levels')
}

export function getActiveActivities() {
  return http.get<ActivityVO[]>('/api/customer/activities')
}
