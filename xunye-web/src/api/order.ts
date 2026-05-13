import request from './request';
import type { OrderCreateParams, OrderQueryParams, OrderPageResult, OrderPageVO, OrderPayParams } from '@/types/api';

export const getRecentOrders = () => request.get('/api/admin/orders/recent');

export const createOrder = (data: OrderCreateParams): Promise<number> =>
  request.post('/api/admin/orders', data);

export const getOrderPage = (params: OrderQueryParams): Promise<OrderPageResult> =>
  request.get('/api/admin/orders', { params });

export const getOrderDetail = (id: number): Promise<OrderPageVO> =>
  request.get(`/api/admin/orders/${id}`);

export const payOrder = (id: number, data: OrderPayParams): Promise<void> =>
  request.patch(`/api/admin/orders/${id}/pay`, data);

export const cancelOrder = (id: number): Promise<void> =>
  request.patch(`/api/admin/orders/${id}/cancel`);

export const startMaking = (id: number): Promise<void> =>
  request.patch(`/api/admin/orders/${id}/making`);

export const finishOrder = (id: number): Promise<void> =>
  request.patch(`/api/admin/orders/${id}/finish`);
