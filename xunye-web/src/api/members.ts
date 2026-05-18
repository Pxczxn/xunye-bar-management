import request from './request';
import type {
  CustomerMemberItem,
  CustomerMemberPageResult,
  MemberLevelItem,
} from '@/types/api';

export function getCustomerMemberPage(params: Record<string, any>): Promise<CustomerMemberPageResult> {
  return request.get('/api/admin/members', { params }) as any;
}

export function getCustomerMemberDetail(id: number): Promise<CustomerMemberItem> {
  return request.get(`/api/admin/members/${id}`) as any;
}

export function getMemberLevels(): Promise<MemberLevelItem[]> {
  return request.get('/api/admin/members/levels') as any;
}

export function updateMemberLevel(id: number, memberLevel: string): Promise<any> {
  return request.patch(`/api/admin/members/${id}/level`, { memberLevel }) as any;
}
