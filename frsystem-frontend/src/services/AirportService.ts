import api from './Api';

export const airportService = {

  getAllAirports: async () => {
    const response = await api.get('/airports');
    return response.data;
  },

  getAirportById: async (id: number | string) => {
    const response = await api.get(`/airports/${id}`);
    return response.data;
  },

  searchAirports: async (airportData: any) => {
    const response = await api.post('/airports/search', airportData);
    return response.data;
  },
  
  createAirport: async (airportData: any) => {
    const response = await api.post('/airports', airportData);
    return response.data;
  },

  updateAirport: async (id: number | string, airportData: any) => {
    const response = await api.put(`/airports/${id}`, airportData);
    return response.data;
  },

  deleteAirport: async (id: number | string) => {
    const response = await api.delete(`/airports/${id}`);
    return response.data;
  },
};