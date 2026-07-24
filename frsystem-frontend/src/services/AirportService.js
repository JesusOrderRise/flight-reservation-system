import api from './api';

export const airportService = {

  getAllAirports: async () => {
    const response = await api.get('/airports');
    return response.data;
  },

  
  getAirportById: async (id) => {
    const response = await api.get(`/airports/${id}`);
    return response.data;
  },

  searchAirports: async (airportData) => {
    const response = await api.post('/airports/search', airportData);
    return response.data;
  },
  
  createAirport: async (airportData) => {
    const response = await api.post('/airports', airportData);
    return response.data;
  },

  updateAirport: async (id, airportData) => {
    const response = await api.put(`/airports/${id}`, airportData);
    return response.data;
  },

    deleteAirport: async (id) => {
    const response = await api.delete(`/airports/${id}`);
    return response.data;
  },
};