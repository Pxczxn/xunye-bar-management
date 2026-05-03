import request from './request';
import type { InventoryWarning, InventoryRecordPageResult, InventoryAdjustParams, InventoryRecordsQueryParams } from '@/types/api';

export const getInventoryWarnings = (): Promise<InventoryWarning[]> =>
  request.get('/api/admin/inventory/warnings');

export const getInventoryRecords = (params: InventoryRecordsQueryParams): Promise<InventoryRecordPageResult> =>
  request.get('/api/admin/inventory/records', { params });

export const adjustInventory = (data: InventoryAdjustParams): Promise<void> =>
  request.post('/api/admin/inventory/adjust', data);
