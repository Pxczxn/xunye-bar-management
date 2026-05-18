import request from './request';
import type {
  ActivityItem,
  ActivityPageResult,
  ActivityFormData,
} from '@/types/api';

export function getActivityPage(params: Record<string, any>): Promise<ActivityPageResult> {
  return request.get('/api/admin/activities', { params }) as any;
}

export function getActivityDetail(id: number): Promise<ActivityItem> {
  return request.get(`/api/admin/activities/${id}`) as any;
}

export function createActivity(data: ActivityFormData): Promise<any> {
  return request.post('/api/admin/activities', data) as any;
}

export function updateActivity(id: number, data: ActivityFormData): Promise<any> {
  return request.put(`/api/admin/activities/${id}`, data) as any;
}

export function deleteActivity(id: number): Promise<any> {
  return request.delete(`/api/admin/activities/${id}`) as any;
}
