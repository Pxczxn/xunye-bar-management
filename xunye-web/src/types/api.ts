export interface DashboardSummary {
  todayRevenue: number;
  todayOrderCount: number;
  averageOrderValue: number;
  inventoryWarningCount: number;
}
export interface SalesTrend {
  date: string;
  revenue: number;
  orderCount: number;
}
export interface PaymentMethod {
  method: string;
  amount: number;
  percent: number;
}
export interface HotProduct {
  id: string;
  productName: string;
  salesCount: number;
  salesAmount: number;
}
export interface ProductCategory {
  id: number;
  name: string;
  sort: number;
  status: number;
}

export interface ProductItem {
  id: number;
  name: string;
  categoryId: number;
  categoryName: string;
  brand: string;
  spec: string;
  price: number;
  costPrice: number;
  stock: number;
  safeStock: number;
  unit: string;
  imageUrl: string | null;
  description: string;
  status: 'ON_SALE' | 'OFF_SALE';
  createdAt: string;
}

export interface ProductPageResult {
  records: ProductItem[];
  total: number;
  pageNum: number;
  pageSize: number;
}

export interface ProductQueryParams {
  pageNum: number;
  pageSize: number;
  keyword?: string;
  categoryId?: number;
  status?: string;
}

export interface ProductFormData {
  categoryId: number;
  name: string;
  brand: string;
  spec: string;
  price: number;
  costPrice: number;
  stock: number;
  safeStock: number;
  unit: string;
  imageUrl: string;
  description: string;
  status: 'ON_SALE' | 'OFF_SALE';
}
export interface Order {
  id: string;
  orderNo: string;
  tableName: string;
  amount: number;
  paymentMethod: string;
  status: 'PENDING' | 'PAID' | 'CANCELLED';
  createdAt: string;
}
export interface InventoryWarning {
  productId: number;
  productName: string;
  currentStock: number;
  safeStock: number;
  unit: string;
  warningLevel: 'HIGH' | 'MEDIUM' | 'LOW';
}

export interface InventoryRecord {
  id: number;
  productId: number;
  productName: string;
  type: 'IN' | 'OUT' | 'LOSS' | 'ADJUST';
  typeText: string;
  changeQuantity: number;
  beforeStock: number;
  afterStock: number;
  reason: string;
  operatorName: string;
  createdAt: string;
}

export interface InventoryRecordPageResult {
  records: InventoryRecord[];
  total: number;
  pageNum: number;
  pageSize: number;
}

export interface InventoryRecordsQueryParams {
  pageNum: number;
  pageSize: number;
  productName?: string;
  type?: string;
}

export interface InventoryAdjustParams {
  productId: number;
  type: 'IN' | 'OUT' | 'LOSS' | 'ADJUST';
  quantity: number;
  reason?: string;
}

export interface CategoryFormData {
  name: string;
  sort: number;
  status: number;
}

export interface ProductBrand {
  id: number;
  name: string;
  sort: number;
}

export interface ProductBrandFormData {
  name: string;
  sort: number;
}

export interface TableArea {
  id: number;
  name: string;
  sort: number;
  status: number;
  createdAt: string;
}

export interface TableAreaSaveParams {
  name: string;
  sort: number;
  status: number;
}

export interface BarTable {
  id: number;
  areaId: number;
  areaName: string;
  name: string;
  capacity: number;
  status: 'EMPTY' | 'USING' | 'CLEANING' | 'DISABLED';
  createdAt: string;
}

export interface BarTablePageResult {
  records: BarTable[];
  total: number;
  pageNum: number;
  pageSize: number;
}

export interface BarTableQueryParams {
  pageNum: number;
  pageSize: number;
  areaId?: number;
  status?: string;
  keyword?: string;
}

export interface BarTableSaveParams {
  areaId: number;
  name: string;
  capacity: number;
  status: string;
}

export interface BarTableStatusParams {
  status: string;
}

export interface OrderItemCreateParams {
  productId: number;
  quantity: number;
}

export interface OrderCreateParams {
  tableId: number;
  items: OrderItemCreateParams[];
  remark?: string;
}

export interface OrderItemVO {
  id: number;
  productId: number;
  productName: string;
  quantity: number;
  price: number;
  amount: number;
}

export interface OrderPageVO {
  id: number;
  orderNo: string;
  tableId: number;
  tableName: string;
  totalAmount: number;
  status: string;
  serveStatus?: string;
  paymentMethod: string | null;
  source: string | null;
  remark: string | null;
  createdAt: string;
  paidAt: string | null;
  cancelledAt: string | null;
  items?: OrderItemVO[];
}

export interface OrderQueryParams {
  pageNum: number;
  pageSize: number;
  orderNo?: string;
  tableName?: string;
  status?: string;
  serveStatus?: string;
  source?: string;
  excludeStatus?: string;
}

export interface OrderPageResult {
  records: OrderPageVO[];
  total: number;
  pageNum: number;
  pageSize: number;
}

export interface OrderPayParams {
  paymentMethod: 'WECHAT' | 'ALIPAY' | 'CASH';
}

export interface UserInfo {
  id: number;
  username: string;
  nickname: string;
  role: 'BOSS' | 'MANAGER' | 'STAFF';
}

export interface LoginParams {
  username: string;
  password: string;
}

export interface LoginResult {
  token: string;
  user: UserInfo;
}

export interface StaffItem {
  id: number;
  username: string;
  nickname: string;
  role: 'BOSS' | 'MANAGER' | 'STAFF';
  status: 0 | 1;
  lastLoginAt: string | null;
  createdAt: string;
}

export interface StaffPageResult {
  records: StaffItem[];
  total: number;
  pageNum: number;
  pageSize: number;
}

export interface StaffQueryParams {
  pageNum: number;
  pageSize: number;
  keyword?: string;
  role?: string;
  status?: string;
}

export interface StaffCreateParams {
  username: string;
  password: string;
  nickname: string;
  role: 'BOSS' | 'MANAGER' | 'STAFF';
  status: 0 | 1;
}

export interface StaffUpdateParams {
  nickname: string;
  role: 'BOSS' | 'MANAGER' | 'STAFF';
  status: 0 | 1;
}

// ===== 会员管理 =====
export interface CustomerMemberItem {
  id: number;
  phone: string;
  nickname: string;
  avatar: string | null;
  memberLevel: string;
  memberLevelName: string;
  points: number;
  balance: number;
  totalOrders: number;
  totalAmount: number;
  nextLevelAmount: number;
  nextLevelName: string;
  lastVisitAt: string | null;
  createdAt: string;
}

export interface CustomerMemberPageResult {
  records: CustomerMemberItem[];
  total: number;
  pageNum: number;
  pageSize: number;
}

export interface CustomerMemberQueryParams {
  pageNum: number;
  pageSize: number;
  keyword?: string;
  memberLevel?: string;
}

export interface MemberLevelItem {
  level: string;
  name: string;
  minAmount: number;
  discount: number;
  pointsRate: number;
  description: string;
  sort: number;
}

// ===== 活动管理 =====
export interface ActivitySettings {
  discountRate?: number;
  minAmount?: number;
  discountAmount?: number;
  pointsMultiplier?: number;
  specialPrice?: number;
  originalPrice?: number;
  stockLimit?: number;
  scopeProductType?: 'ALL' | 'CATEGORY' | 'PRODUCT';
  productIds?: number[];
  categoryIds?: number[];
  scopeTableType?: 'ALL' | 'AREA' | 'TABLE';
  tableIds?: number[];
  areaIds?: number[];
}

export interface ActivityItem {
  id: number;
  title: string;
  description: string | null;
  type: string;
  startDate: string | null;
  endDate: string | null;
  coverImage: string | null;
  settings: ActivitySettings;
  settingSummary: string;
  status: number;
  sort: number;
  createdAt: string;
  updatedAt: string;
}

export interface ActivityPageResult {
  records: ActivityItem[];
  total: number;
  pageNum: number;
  pageSize: number;
}

export interface ActivityQueryParams {
  pageNum: number;
  pageSize: number;
  keyword?: string;
  type?: string;
  status?: number;
}

export interface ActivityFormData {
  title: string;
  description: string;
  type: string;
  startDate: string | null;
  endDate: string | null;
  coverImage: string;
  settings: ActivitySettings;
  status: number;
  sort: number;
}
