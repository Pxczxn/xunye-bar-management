import request from './request';

export interface CouponTemplate {
  id: number;
  name: string;
  title: string;
  description?: string;
  type: 'AMOUNT' | 'DISCOUNT';
  discountAmount?: number;
  discountRate?: number;
  minAmount: number;
  scopeType: 'ALL' | 'PRODUCT' | 'CATEGORY';
  scopeConfig?: string;
  issueType: 'MANUAL' | 'AUTO_NEW_USER' | 'AUTO_POINTS';
  issueConfig?: string;
  validDays: number;
  maxUseCount?: number;
  totalCount?: number;
  issuedCount: number;
  usedCount: number;
  memberLevelLimit?: string;
  status: number;
  sort: number;
  createdAt: string;
  updatedAt: string;
}

export interface CouponTemplateFormData {
  name: string;
  title: string;
  description?: string;
  type: 'AMOUNT' | 'DISCOUNT';
  discountAmount?: number;
  discountRate?: number;
  minAmount: number;
  scopeType: 'ALL' | 'PRODUCT' | 'CATEGORY';
  scopeConfig?: string;
  issueType: 'MANUAL' | 'AUTO_NEW_USER' | 'AUTO_POINTS';
  issueConfig?: string;
  validDays: number;
  maxUseCount?: number;
  totalCount?: number;
  memberLevelLimit?: string;
  status?: number;
  sort?: number;
}

export interface CouponTemplatePageResult {
  records: CouponTemplate[];
  total: number;
  pageNum: number;
  pageSize: number;
}

export function getCouponTemplatePage(params: Record<string, any>): Promise<CouponTemplatePageResult> {
  return request.get('/api/admin/coupon-templates', { params }) as any;
}

export function getCouponTemplateDetail(id: number): Promise<CouponTemplate> {
  return request.get(`/api/admin/coupon-templates/${id}`) as any;
}

export function createCouponTemplate(data: CouponTemplateFormData): Promise<number> {
  return request.post('/api/admin/coupon-templates', data) as any;
}

export function updateCouponTemplate(id: number, data: CouponTemplateFormData): Promise<any> {
  return request.put(`/api/admin/coupon-templates/${id}`, data) as any;
}

export function deleteCouponTemplate(id: number): Promise<any> {
  return request.delete(`/api/admin/coupon-templates/${id}`) as any;
}

export function updateCouponTemplateStatus(id: number, status: number): Promise<any> {
  return request.patch(`/api/admin/coupon-templates/${id}/status`, null, { params: { status } }) as any;
}
