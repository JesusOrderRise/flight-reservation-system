import axios, { type InternalAxiosRequestConfig, type AxiosResponse, AxiosError } from 'axios';

const api = axios.create({
  baseURL: 'http://localhost:8080/api/v1', 
  timeout: 5000,
  headers: {
    'Content-Type': 'application/json'
  }
});

// Token adder interceptor
api.interceptors.request.use(
  (config: InternalAxiosRequestConfig): InternalAxiosRequestConfig => {
    const token = localStorage.getItem('token');
    if (token && config.headers) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error: AxiosError) => {
    return Promise.reject(error);
  }
);

// Interceptor when 401 or 403
api.interceptors.response.use(
  (response: AxiosResponse): AxiosResponse => response, 
  (error: AxiosError) => {
    // Get status 
    const status = error.response?.status;

    if (status === 401 || status === 403) {
      // Clear auth context
      const authKeys = ['token', 'role', 'firstName', 'lastName'];
      authKeys.forEach(key => localStorage.removeItem(key));

      // When not login, routes login
      if (window.location.pathname !== '/login') {
        window.location.replace('/login');
      }
    }
    
    return Promise.reject(error);
  }
);

export default api;