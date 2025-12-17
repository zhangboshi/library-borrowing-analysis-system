import axios from 'axios';
import { ElMessage } from 'element-plus';

const instance = axios.create({
  baseURL: import.meta.env.VITE_API_BASEURL || 'http://localhost:8080',
  timeout: 10000,
});

instance.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

instance.interceptors.response.use(
  (response) => {
    const res = response.data;
    if (res && res.success === false) {
      ElMessage.error(res.message || '请求失败');
      return Promise.reject(res);
    }
    return res;
  },
  (error) => {
    ElMessage.error(error.response?.data?.message || error.message || '网络错误');
    return Promise.reject(error);
  }
);

export default instance;
