import request from './request';
import type { LoginParams, LoginResult, UserInfo } from '@/types/api';

export const login = (data: LoginParams): Promise<LoginResult> =>
  request.post('/api/admin/auth/login', data);

export const getProfile = (): Promise<UserInfo> =>
  request.get('/api/admin/auth/profile');
