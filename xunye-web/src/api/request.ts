import axios, { AxiosRequestConfig } from 'axios';
import { message } from 'antd';

let isRedirecting = false;
const inflightGetRequests = new Map<string, Promise<unknown>>();

function cleanParams(params: unknown): unknown {
  if (!params || typeof params !== 'object' || Array.isArray(params)) {
    return params;
  }
  const cleaned: Record<string, unknown> = {};
  for (const [key, value] of Object.entries(params as Record<string, unknown>)) {
    if (value !== null && value !== undefined && value !== '') {
      cleaned[key] = value;
    }
  }
  return cleaned;
}

function stableStringify(value: unknown): string {
  if (!value || typeof value !== 'object') {
    return JSON.stringify(value);
  }
  if (Array.isArray(value)) {
    return `[${value.map(stableStringify).join(',')}]`;
  }
  return `{${Object.entries(value as Record<string, unknown>)
    .sort(([a], [b]) => a.localeCompare(b))
    .map(([key, val]) => `${JSON.stringify(key)}:${stableStringify(val)}`)
    .join(',')}}`;
}

function generateGetRequestKey(url: string, config?: AxiosRequestConfig): string {
  return ['get', url, stableStringify(cleanParams(config?.params))].join('&');
}

const request = axios.create({
  baseURL: '',  // 使用 Vite 代理转发到后端
  timeout: 10000,
});

function handleAuthExpired(msg?: string) {
  if (isRedirecting || window.location.pathname === '/login') return;
  isRedirecting = true;
  localStorage.removeItem('token');
  localStorage.removeItem('user');
  message.error(msg || '登录已过期，请重新登录');
  window.location.href = '/login';
}

request.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    if (config.params) {
      config.params = cleanParams(config.params);
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

request.interceptors.response.use(
  (response) => {
    const res = response.data;

    if (res.code === 401) {
      handleAuthExpired(res.message);
      return Promise.reject(new Error(res.message || '登录已过期，请重新登录'));
    }

    if (res.code === 403) {
      message.error('无权限访问');
      return Promise.reject(new Error('无权限访问'));
    }

    if (res.code !== 200) {
      message.error(res.message || '请求失败');
      return Promise.reject(new Error(res.message || '请求失败'));
    }

    return res.data;
  },
  (error) => {
    if (error.response?.status === 401) {
      handleAuthExpired();
      return Promise.reject(new Error('登录已过期'));
    }
    if (error.response?.status === 403) {
      message.error('无权限访问');
      return Promise.reject(new Error('无权限访问'));
    }
    if (error.response?.status === 404) {
      message.error('请求的资源不存在');
      return Promise.reject(new Error('资源不存在'));
    }
    if (error.response?.status >= 500) {
      message.error('服务器错误,请稍后重试');
      return Promise.reject(new Error('服务器错误'));
    }
    if (error.code === 'ECONNABORTED') {
      message.error('请求超时,请检查网络');
      return Promise.reject(new Error('请求超时'));
    }
    if (!error.response) {
      message.error('网络连接失败,请检查网络');
      return Promise.reject(new Error('网络连接失败'));
    }

    const msg = error.response?.data?.message || '请求失败';
    message.error(msg);
    return Promise.reject(new Error(msg));
  }
);

const rawGet = request.get.bind(request);

request.get = ((url: string, config?: AxiosRequestConfig) => {
  const requestConfig = config ? { ...config, params: cleanParams(config.params) } : undefined;
  const requestKey = generateGetRequestKey(url, requestConfig);
  const inflight = inflightGetRequests.get(requestKey);

  if (inflight) {
    return inflight;
  }

  const promise = rawGet(url, requestConfig).finally(() => {
    inflightGetRequests.delete(requestKey);
  });
  inflightGetRequests.set(requestKey, promise);
  return promise;
}) as typeof request.get;

export default request;
