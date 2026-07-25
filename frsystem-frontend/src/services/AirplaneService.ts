import api from './Api';

export const airplaneService = {

  getAllAirplanes: async () => {
    const response = await api.get('/airplanes');
    return response.data;
  },

  getAirplaneById: async (id: number | string) => {
    const response = await api.get(`/airplanes/${id}`);
    return response.data;
  },

  searchAirplane: async (airplaneData: any) => {
    const response = await api.post('/airplanes/search', airplaneData);
    return response.data;
  },
  
  createAirplane: async (airplaneData: any) => {
    const response = await api.post('/airplanes', airplaneData);
    return response.data;
  },

  updateAirplane: async (id: number | string, airplaneData: any) => {
    const response = await api.put(`/airplanes/${id}`, airplaneData);
    return response.data;
  },

  deleteAirplane: async (id: number | string) => {
    const response = await api.delete(`/airplanes/${id}`);
    return response.data;
  },
};