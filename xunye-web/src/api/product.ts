import request from './request';
import type { ProductCategory, ProductPageResult, ProductQueryParams, ProductFormData } from '@/types/api';

export const getSimpleProducts = () => request.get('/api/admin/products/simple');

export const getCategoryList = (): Promise<ProductCategory[]> => request.get('/api/admin/categories');

export const getProductPage = (params: ProductQueryParams): Promise<ProductPageResult> =>
  request.get('/api/admin/products', { params });

export const createProduct = (data: ProductFormData): Promise<any> =>
  request.post('/api/admin/products', data);

export const updateProduct = (id: number, data: ProductFormData): Promise<any> =>
  request.put(`/api/admin/products/${id}`, data);

export const updateProductStatus = (id: number, status: string): Promise<any> =>
  request.patch(`/api/admin/products/${id}/status`, { status });

export const deleteProduct = (id: number): Promise<any> =>
  request.delete(`/api/admin/products/${id}`);
