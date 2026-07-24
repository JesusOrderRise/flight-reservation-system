import axios from 'axios';

const api = axios.create({
  baseURL: 'http://localhost:8080/api/v1', 
  timeout: 5000,
  headers: {
    'Content-Type': 'application/json'
  }
});

//Token adder interceptor
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});


//interceptor when 401 or 403
api.interceptors.response.use(
  (response) => response, 
  (error) => {

    //get status 
    const status = error.response?.status;

    if (status === 401 || status === 403) {
    
      //clear auth context.
      const authKeys = ['token', 'role', 'firstName', 'lastName'];
      authKeys.forEach(key => localStorage.removeItem(key));

      //when not login, routes login
      if (window.location.pathname !== '/login') {
        window.location.replace('/login');
      }
    }
    
    
    return Promise.reject(error);
  }
);


export default api;api.interceptors.response.use(
  (response) => response, // Başarılı yanıtlara doğrudan geçiş izni
  (error) => {
    // Optional chaining ile status kodunu güvenle al
    const status = error.response?.status;

    if (status === 401 || status === 403) {
      // Sadece geliştirme (development) ortamında log göster, canlıda konsolu temiz tut
      if (import.meta.env.DEV) {
        console.warn(`[Auth Warning] Yetkisiz erişim (Status: ${status}). Oturum temizleniyor.`);
      }

      // AuthContext içindeki tüm verileri kökten temizle
      const authKeys = ['token', 'role', 'firstName', 'lastName'];
      authKeys.forEach(key => localStorage.removeItem(key));

      // Eğer zaten login sayfasında değilsek yönlendirme yap (Sonsuz döngüyü engeller)
      if (window.location.pathname !== '/login') {
        window.location.replace('/login'); // .href yerine .replace kullanmak tarayıcı geçmişini kirletmez
      }
    }
    
    // Hatayı formların catch bloklarına fırlatmaya devam et
    return Promise.reject(error);
  }
);