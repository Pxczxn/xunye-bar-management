import request from './request';
export const getDashboardSummary = () => request.get('/api/admin/dashboard/summary');
export const getSalesTrend = () => request.get('/api/admin/dashboard/sales-trend');
export const getHotProducts = () => request.get('/api/admin/dashboard/hot-products');
export const getPaymentMethods = () => request.get('/api/admin/dashboard/payment-methods');
