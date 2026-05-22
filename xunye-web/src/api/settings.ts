import request from './request';

export interface ShopConfig {
  name: string;
  slogan: string;
  address: string;
  phone: string;
  contactWechat: string;
  businessHours: string;
  notice: string;
}

export interface ReceiptConfig {
  enabled: boolean;
  printer: string;
}

export interface OrderConfig {
  paymentMethods: string[];
  receiptBar: ReceiptConfig;
  receiptKitchen: ReceiptConfig;
  cancelTimeout: string;
}

export interface MiniappConfig {
  homepageTitle: string;
  homepageSubtitle: string;
  menuDisplay: string;
  scanToOrder: boolean;
  bannerImages: string[];
}

export const getShopConfig = (): Promise<ShopConfig> =>
  request.get('/api/admin/settings/shop');

export const getOrderConfig = (): Promise<OrderConfig> =>
  request.get('/api/admin/settings/order');

export const getMiniappConfig = (): Promise<MiniappConfig> =>
  request.get('/api/admin/settings/miniapp');

export const updateConfigs = (configs: Record<string, string>): Promise<void> =>
  request.put('/api/admin/settings', { configs });

export const uploadMiniappImage = (file: File): Promise<string> => {
  const formData = new FormData();
  formData.append('file', file);
  return request.post('/api/admin/settings/miniapp/upload-image', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  });
};

export const deleteMiniappImage = (imageUrl: string): Promise<void> =>
  request.delete('/api/admin/settings/miniapp/images', { params: { imageUrl } });
