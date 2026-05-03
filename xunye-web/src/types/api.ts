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
