import { http } from '@/http/http'

export interface OrderCreateDTO {
  tableId: number
  phone?: string
  couponId?: number | null
  items: Array<{
    productId: number
    quantity: number
  }>
  remark?: string
}

export interface CustomerOrderSubmitVO {
  orderNo: string
  totalAmount: number
  originalAmount: number
  discountAmount: number
  status: string
}

export interface OrderItemVO {
  id: number
  productId: number
  productName: string
  quantity: number
  price: number
  amount: number
}

export interface OrderPageVO {
  id: number
  orderNo: string
  tableId: number
  tableName: string
  totalAmount: number
  originalAmount: number
  discountAmount: number
  couponId: number | null
  status: string
  serveStatus: string
  paymentMethod: string | null
  source: string
  remark: string | null
  createdAt: string
  paidAt: string | null
  cancelledAt: string | null
  items: OrderItemVO[]
}

export interface PaymentVO {
  paymentNo: string
  orderNo: string
  amount: number
  provider: string
  status: string
}

export interface CustomerTableVO {
  id: number
  tableCode: string
  name: string
  status: string
  areaName: string
}

export interface CustomerCouponVO {
  id: number
  title: string
  rule: string
  discountAmount: number
  minAmount: number
  used: boolean
  validUntil: string
}

export interface CustomerPointsRecordVO {
  id: number
  title: string
  amount: number
  relatedOrderNo: string | null
  createdAt: string
}

export interface CustomerCategoryVO {
  id: number
  name: string
}

export interface CustomerProductVO {
  id: number
  categoryId: number
  categoryName: string
  name: string
  description: string | null
  price: number
  imageUrl: string | null
  stock: number
}

export function listCustomerCategories() {
  return http.get<CustomerCategoryVO[]>('/api/customer/categories')
}

export function listCustomerProducts(params?: { categoryId?: number, keyword?: string }) {
  return http.get<CustomerProductVO[]>('/api/customer/products', params)
}

export function listCustomerTables() {
  return http.get<CustomerTableVO[]>('/api/customer/tables')
}

export function getCustomerTableByCode(tableCode: string) {
  return http.get<CustomerTableVO>(`/api/customer/tables/${tableCode}`)
}

export function createCustomerOrder(data: OrderCreateDTO) {
  return http.post<CustomerOrderSubmitVO>('/api/customer/orders', data)
}

export function listCustomerOrders(params?: Record<string, any>) {
  return http.get<OrderPageVO[]>('/api/customer/orders', params)
}

export function getCustomerOrderDetail(orderNo: string) {
  return http.get<OrderPageVO>(`/api/customer/orders/${orderNo}`)
}

export function createOrderPayment(orderNo: string) {
  return http.post<PaymentVO>(`/api/customer/orders/${orderNo}/payments`)
}

export function confirmOrderPayment(paymentNo: string) {
  return http.post<void>(`/api/customer/payments/${paymentNo}/confirm`)
}

export function listCustomerCoupons(phone: string) {
  return http.get<CustomerCouponVO[]>('/api/customer/coupons', { phone })
}

export function exchangePointsReward(phone: string, rewardId: number) {
  return http.post<CustomerCouponVO>(`/api/customer/points/rewards/${rewardId}/exchange`, undefined, { phone })
}

export function listCustomerPointsRecords(phone: string) {
  return http.get<CustomerPointsRecordVO[]>('/api/customer/points/records', { phone })
}

export interface CustomerMessageVO {
  id: number
  title: string
  content: string
  type: string
  isRead: number
  relatedOrderId: number | null
  createdAt: string
}

export function listCustomerMessages(phone: string) {
  return http.get<CustomerMessageVO[]>('/api/customer/messages', { phone })
}

export interface CustomerStatsVO {
  points: number
  coupons: number
  totalOrders: number
  totalAmount: number
}

export function getCustomerStats(phone: string) {
  return http.get<CustomerStatsVO>('/api/customer/stats', { phone })
}

export function getPaymentStatus(paymentNo: string) {
  return http.get<PaymentVO>(`/api/customer/payments/${paymentNo}`)
}

export interface ShopInfoVO {
  name: string
  slogan: string
  businessHours: string
  notice: string
  bannerImages: string[]
}

export function getShopInfo() {
  return http.get<ShopInfoVO>('/api/customer/shop/info')
}
