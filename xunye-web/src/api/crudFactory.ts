import request from './request';

/**
 * 分页查询参数
 */
export interface PageQueryParams {
  pageNum?: number;
  pageSize?: number;
  keyword?: string;
  [key: string]: any;
}

/**
 * 分页结果
 */
export interface PageResult<T> {
  records: T[];
  total: number;
  pageNum?: number;
  pageSize?: number;
}

/**
 * CRUD API 接口
 */
export interface CrudApi<T, CreateDTO = any, UpdateDTO = any, QueryParams = PageQueryParams> {
  getPage: (params: QueryParams) => Promise<PageResult<T>>;
  getById: (id: number) => Promise<T>;
  create: (data: CreateDTO) => Promise<any>;
  update: (id: number, data: UpdateDTO) => Promise<any>;
  delete: (id: number) => Promise<any>;
}

/**
 * 创建 CRUD API 工厂函数
 * @param basePath API 基础路径，如 '/api/admin/products'
 * @returns CRUD API 对象
 *
 * @example
 * const productApi = createCrudApi<ProductItem>('/api/admin/products');
 *
 * // 使用
 * const page = await productApi.getPage({ pageNum: 1, pageSize: 10 });
 * const item = await productApi.getById(1);
 * await productApi.create({ name: 'New Product' });
 * await productApi.update(1, { name: 'Updated Product' });
 * await productApi.delete(1);
 */
export function createCrudApi<T = any, CreateDTO = any, UpdateDTO = any, QueryParams = PageQueryParams>(
  basePath: string
): CrudApi<T, CreateDTO, UpdateDTO, QueryParams> {
  return {
    getPage: (params: QueryParams) => request.get<PageResult<T>>(basePath, { params }),

    getById: (id: number) => request.get<T>(`${basePath}/${id}`),

    create: (data: CreateDTO) => request.post(basePath, data),

    update: (id: number, data: UpdateDTO) => request.put(`${basePath}/${id}`, data),

    delete: (id: number) => request.delete(`${basePath}/${id}`),
  };
}

/**
 * 创建扩展的 CRUD API（支持自定义方法）
 * @param basePath API 基础路径
 * @param extensions 扩展方法
 * @returns 扩展的 CRUD API 对象
 *
 * @example
 * const productApi = createExtendedCrudApi<ProductItem>(
 *   '/api/admin/products',
 *   {
 *     updateStatus: (id: number, status: string) =>
 *       request.put(`/api/admin/products/${id}/status`, { status }),
 *     getSimpleList: () =>
 *       request.get('/api/admin/products/simple'),
 *   }
 * );
 */
export function createExtendedCrudApi<T = any, CreateDTO = any, UpdateDTO = any, QueryParams = PageQueryParams, Extensions = {}>(
  basePath: string,
  extensions: Extensions
): CrudApi<T, CreateDTO, UpdateDTO, QueryParams> & Extensions {
  const baseApi = createCrudApi<T, CreateDTO, UpdateDTO, QueryParams>(basePath);
  return {
    ...baseApi,
    ...extensions,
  };
}
