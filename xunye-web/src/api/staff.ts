import request from './request';
import type {
  StaffItem,
  StaffPageResult,
  StaffQueryParams,
  StaffCreateParams,
  StaffUpdateParams,
} from '@/types/api';

export const getStaffPage = (params: StaffQueryParams): Promise<StaffPageResult> =>
  request.get('/api/admin/staff', { params });

export const getStaffById = (id: number): Promise<StaffItem> =>
  request.get(`/api/admin/staff/${id}`);

export const createStaff = (data: StaffCreateParams): Promise<any> =>
  request.post('/api/admin/staff', data);

export const updateStaff = (id: number, data: StaffUpdateParams): Promise<any> =>
  request.put(`/api/admin/staff/${id}`, data);

export const updateStaffStatus = (id: number, status: number): Promise<any> =>
  request.patch(`/api/admin/staff/${id}/status`, { status });

export const resetStaffPassword = (id: number, password: string): Promise<any> =>
  request.patch(`/api/admin/staff/${id}/reset-password`, { password });

export const deleteStaff = (id: number): Promise<any> =>
  request.delete(`/api/admin/staff/${id}`);
