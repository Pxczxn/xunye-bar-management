import request from './request';
import type {
  TableArea,
  TableAreaSaveParams,
  BarTable,
  BarTablePageResult,
  BarTableQueryParams,
  BarTableSaveParams,
  BarTableStatusParams,
} from '@/types/api';

export const getTableAreas = (): Promise<TableArea[]> =>
  request.get('/api/admin/table-areas');

export const createTableArea = (data: TableAreaSaveParams): Promise<void> =>
  request.post('/api/admin/table-areas', data);

export const updateTableArea = (id: number, data: TableAreaSaveParams): Promise<void> =>
  request.put(`/api/admin/table-areas/${id}`, data);

export const deleteTableArea = (id: number): Promise<void> =>
  request.delete(`/api/admin/table-areas/${id}`);

export const getTablePage = (params: BarTableQueryParams): Promise<BarTablePageResult> =>
  request.get('/api/admin/tables', { params });

export const createTable = (data: BarTableSaveParams): Promise<void> =>
  request.post('/api/admin/tables', data);

export const updateTable = (id: number, data: BarTableSaveParams): Promise<void> =>
  request.put(`/api/admin/tables/${id}`, data);

export const updateTableStatus = (id: number, data: BarTableStatusParams): Promise<void> =>
  request.patch(`/api/admin/tables/${id}/status`, data);

export const clearTable = (id: number): Promise<void> =>
  request.patch(`/api/admin/tables/${id}/clear`);

export const deleteTable = (id: number): Promise<void> =>
  request.delete(`/api/admin/tables/${id}`);
