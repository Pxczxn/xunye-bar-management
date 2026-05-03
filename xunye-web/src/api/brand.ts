import request from './request';
import type { ProductBrand, ProductBrandFormData } from '@/types/api';

export const getBrandList = (): Promise<ProductBrand[]> =>
  request.get('/api/admin/brands');

export const createBrand = (data: ProductBrandFormData): Promise<any> =>
  request.post('/api/admin/brands', data);

export const deleteBrand = (id: number): Promise<any> =>
  request.delete(`/api/admin/brands/${id}`);
