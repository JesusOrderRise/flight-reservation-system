import api from './Api';

export const reservationService = {
    
    createReservation: async (reservationData: any) => {
        const response = await api.post('/reservations', reservationData);
        return response.data;
    },

    searchReservation: async (reservationData: any) => {
        const response = await api.post('/reservations/search', reservationData);
        return response.data;
    },

    getMyReservations: async () => {
        const response = await api.get('/reservations/me');
        return response.data;
    },
    
    getAllReservations: async () => {
        const response = await api.get('/reservations');
        return response.data;
    },
    
    cancelSelfReservation: async (reservationId: number | string) => {
        const response = await api.patch(`/reservations/${reservationId}/cancel`);
        return response.data;
    },
    
    adminCancelReservation: async (reservationId: number | string) => {
        const response = await api.patch(`/reservations/${reservationId}/admin-cancel`);
        return response.data;
    },

    getOccupiedSeats: async (flightId: number | string) => {
        const response = await api.get(`/reservations/flight/${flightId}`);
        return response.data;
    }
};