import request from './request';
import type { ProductCategory, CategoryFormData } from '@/types/api';

export const getCategoryList = (): Promise<ProductCategory[]> =>
  request.get('/api/admin/categories');

export const createCategory = (data: CategoryFormData): Promise<any> =>
  request.post('/api/admin/categories', data);

export const updateCategory = (id: number, data: CategoryFormData): Promise<any> =>
  request.put(`/api/admin/categories/${id}`, data);

export const deleteCategory = (id: number): Promise<any> =>
  request.delete(`/api/admin/categories/${id}`);
