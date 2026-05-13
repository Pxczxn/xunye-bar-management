import axios from 'axios';
import { message } from 'antd';

let isRedirecting = false;

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
      const cleaned: Record<string, unknown> = {};
      for (const [k, v] of Object.entries(config.params)) {
        if (v !== null && v !== undefined && v !== '') {
          cleaned[k] = v;
        }
      }
      config.params = cleaned;
    }
    return config;
  },
  (error) => Promise.reject(error)
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
    const msg = error.response?.data?.message || '网络请求失败';
    return Promise.reject(new Error(msg));
  }
);

export default request;
