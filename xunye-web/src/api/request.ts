import axios from 'axios';

const request = axios.create({
  baseURL: '',  // 使用 Vite 代理转发到后端
  timeout: 10000,
});

request.interceptors.response.use(
  (response) => {
    const res = response.data;
    
    if (res.code !== 200) {
      return Promise.reject(new Error(res.message || 'Error'));
    }
    
    return res.data;
  },
  (error) => {
    return Promise.reject(new Error('网络请求失败'));
  }
);

export default request;
