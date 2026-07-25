import api from './Api';

export const flightService = {

  getAllFlights: async () => {
    const response = await api.get('/flights');
    return response.data;
  },

  getFlightById: async (id: number | string) => {
    const response = await api.get(`/flights/${id}`);
    return response.data;
  },

  searchFlights: async (flightData: any) => {
    const response = await api.post('/flights/search', flightData);
    return response.data;
  },
  
  createFlight: async (flightData: any) => {
    const response = await api.post('/flights', flightData);
    return response.data;
  },

  updateFlightStatus: async (id: number | string, status: string) => {
    const response = await api.put(`/flights/${id}/status?status=${status}`);
    return response.data;
  },

  deleteFlight: async (id: number | string) => {
    const response = await api.delete(`/flights/${id}`);
    return response.data;
  },
};