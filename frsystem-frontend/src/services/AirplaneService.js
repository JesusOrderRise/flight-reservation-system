import api from './api';

export const airplaneService = {

  getAllairplanes: async () => {
    const response = await api.get('/airplanes');
    return response.data;
  },

  
  getairplaneById: async (id) => {
    const response = await api.get(`/airplanes/${id}`);
    return response.data;
  },

  searchairplanes: async (airplaneData) => {
    const response = await api.put('/airplanes/search', airplaneData);
    return response.data;
  },
  
  createairplane: async (airplaneData) => {
    const response = await api.post('/airplanes', airplaneData);
    return response.data;
  },

  updateairplane: async (id, airplaneData) => {
    const response = await api.put(`/airplanes/${id}`, airplaneData);
    return response.data;
  },

    deleteairplane: async (id) => {
    const response = await api.delete(`/airplanes/${id}`);
    return response.data;
  },
};