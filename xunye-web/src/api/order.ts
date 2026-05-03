import request from './request';
export const getRecentOrders = () => request.get('/api/admin/orders/recent');
