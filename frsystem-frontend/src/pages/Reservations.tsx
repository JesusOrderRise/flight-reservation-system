import React, { useState, useEffect, type ChangeEvent } from 'react';
import { toast } from 'react-toastify';
import { reservationService } from '../services/ReservationService';

interface ReservationsProps {
    isAdmin: boolean;
}

const Reservations: React.FC<ReservationsProps> = ({ isAdmin }) => {
    const [reservations, setReservations] = useState<any[]>([]);
    const [loading, setLoading] = useState<boolean>(false);
    const [activeTab, setActiveTab] = useState<string>('MY_RESERVATIONS');
    const [searchTerm, setSearchTerm] = useState<string>('');

    const fetchReservations = async () => {
        setLoading(true);
        try {
            let data: any[] = [];
            if (isAdmin && activeTab === 'ALL_RESERVATIONS') {
                data = await reservationService.getAllReservations();
            } else {
                data = await reservationService.getMyReservations();
            }
            setReservations(data);
        } catch (error) {
            console.error("Fetch Error:", error);
            toast.error("Failed to load reservations!");
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        // eslint-disable-next-line react-hooks/exhaustive-deps
        fetchReservations();
    }, [isAdmin, activeTab]);

    const handleCancel = async (id: number | string) => {
        if (!window.confirm("Are you sure you want to cancel this reservation?")) return;

        try {
            if (isAdmin && activeTab === 'ALL_RESERVATIONS') {
                await reservationService.adminCancelReservation(id);
            } else {
                await reservationService.cancelSelfReservation(id);
            }
            toast.success("Reservation has been cancelled.");
            fetchReservations(); 
        } catch (error: any) {
            console.error("Cancel Error:", error);
            const errorData = error.response?.data;
            let errorMessage = "Cancellation failed.";
            
            if (typeof errorData === 'string') {
                errorMessage = errorData;
            } else if (errorData && errorData.message) {
                errorMessage = errorData.message;
            }
            toast.error(errorMessage);
        }
    };

    const getStatusStyle = (status: string) => {
        switch (status) {
            case 'CONFIRMED': return 'bg-green-100 text-green-800 border-green-200';
            case 'CANCELED': return 'bg-gray-200 text-gray-600 border-gray-300 line-through opacity-70';
            default: return 'bg-blue-100 text-blue-800 border-blue-200';
        }
    };

    const filteredReservations = reservations.filter(res => {
        if (activeTab === 'MY_RESERVATIONS') return true;
        if (!searchTerm) return true;
        
        const term = searchTerm.toLowerCase();
        const fullName = `${res.user?.firstName || ''} ${res.user?.lastName || ''}`.toLowerCase();
        const flightNumber = (res.flight?.flightNumber || '').toLowerCase();
        const resId = res.id?.toString() || '';
        const userId = res.user?.id?.toString() || '';
        
        return fullName.includes(term) || flightNumber.includes(term) || resId.includes(term) || userId.includes(term);
    });

    return (
        <div className="p-4">
            <div className="flex justify-between items-center mb-6 border-b pb-4">
                <h2 className="text-2xl font-bold text-gray-800">
                    Reservations
                </h2>
            </div>

            {isAdmin && (
                <div className="flex flex-col md:flex-row justify-between items-start md:items-center mb-8 gap-4">
                    <div className="flex gap-4">
                        <button 
                            onClick={() => {
                                setActiveTab('MY_RESERVATIONS');
                                setSearchTerm('');
                            }
                            }
                            className={`px-6 py-2.5 rounded-full font-bold transition shadow-sm border-2 
                                ${activeTab === 'MY_RESERVATIONS' 
                                    ? 'bg-red-900 text-white border-red-900' 
                                    : 'bg-white text-gray-500 border-gray-200 hover:border-red-300 hover:text-red-900'
                                }`}
                        >
                            My Reservations
                        </button>
                        <button 
                            onClick={() => {
                                setActiveTab('ALL_RESERVATIONS')
                                setSearchTerm('');
                            }
                            }
                            className={`px-6 py-2.5 rounded-full font-bold transition shadow-sm border-2 
                                ${activeTab === 'ALL_RESERVATIONS' 
                                    ? 'bg-red-900 text-white border-red-900' 
                                    : 'bg-white text-gray-500 border-gray-200 hover:border-red-300 hover:text-red-900'
                                }`}
                        >
                            All Reservations
                        </button>
                    </div>
                    
                    {activeTab === 'ALL_RESERVATIONS' && (
                        <input 
                            type="text"
                            placeholder="Search Name, ID or Flight..."
                            value={searchTerm}
                            onChange={(e: ChangeEvent<HTMLInputElement>) => setSearchTerm(e.target.value)}
                            className="w-full md:w-72 border-2 border-gray-200 p-2.5 rounded-full focus:border-red-900 outline-none text-sm font-semibold text-gray-700 transition"
                        />
                    )}
                </div>
            )}

            {loading ? (
                <div className="text-center py-10 text-gray-500 font-semibold">Loading...</div>
            ) : filteredReservations.length === 0 ? (
                <div className="text-center py-10 text-gray-500 bg-white rounded-lg shadow-sm border border-dashed">
                    No reservations found.
                </div>
            ) : (
                <div className="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3 gap-6">
                    {filteredReservations.map((reservation) => (
                        <div key={reservation.id} className="bg-white rounded-xl shadow-sm hover:shadow-md transition-shadow duration-300 border border-gray-100 overflow-hidden flex flex-col">
                            <div className="bg-gray-50 px-5 py-3 border-b border-gray-100 flex justify-between items-center">
                                <span className="bg-red-900 text-white text-xs font-black px-2 py-1 rounded tracking-widest shadow-sm">
                                    RESERVATION ID: {reservation.id}
                                </span>
                                <span className={`text-xs font-bold px-2 py-1 rounded-full border ${getStatusStyle(reservation.status)}`}>
                                    {reservation.status}
                                </span>
                            </div>

                            {isAdmin && activeTab === 'ALL_RESERVATIONS' && (
                                <div className="px-5 py-3 border-b border-gray-100 bg-red-50 flex justify-between items-center">
                                    <div className="flex flex-col">
                                        <span className="text-red-400 text-[10px] font-bold uppercase tracking-wider">Passenger</span>
                                        <span className="text-red-900 font-bold text-sm">
                                            {reservation.user?.firstName || 'Unknown'} {reservation.user?.lastName || ''}
                                        </span>
                                    </div>
                                    <div className="flex flex-col text-right">
                                        <span className="text-red-400 text-[10px] font-bold uppercase tracking-wider">User ID</span>
                                        <span className="text-red-900 font-bold text-sm">
                                            #{reservation.user?.id || 'N/A'}
                                        </span>
                                    </div>
                                </div>
                            )}

                            <div className="p-5 flex-1">
                                <div className="flex justify-between items-center mb-6">
                                    <div className="text-center w-2/5">
                                        <div className="text-xl font-black text-gray-800">
                                            {reservation.flight?.departureAirport?.iataCode || 'N/A'} 
                                        </div>
                                    </div>
                                    
                                    <div className="w-1/5 flex flex-col items-center">
                                        <span className="text-gray-300 text-xl">➔</span>
                                    </div>
                                    
                                    <div className="text-center w-2/5">
                                        <div className="text-xl font-black text-gray-800">
                                            {reservation.flight?.arrivalAirport?.iataCode || 'N/A'}
                                        </div>
                                    </div>
                                </div>

                                <div className="bg-gray-50 rounded-lg p-4 flex justify-between items-center text-sm border border-gray-200">
                                    <div className="flex flex-col">
                                        <span className="text-gray-400 text-xs font-semibold">Flight</span>
                                        <span className="text-gray-900 font-bold">
                                            {reservation.flight?.flightNumber || 'Unknown'}
                                        </span>
                                    </div>
                                    <div className="flex flex-col text-right">
                                        <span className="text-gray-400 text-xs font-semibold">Seat</span>
                                        <span className="text-red-700 text-lg font-black">
                                            {reservation.seatNumber}
                                        </span>
                                    </div>
                                </div>
                            </div>

                            <div className="p-4 border-t border-gray-100 bg-white flex justify-end">
                                <button 
                                    onClick={() => handleCancel(reservation.id)}
                                    disabled={reservation.status === 'CANCELED'}
                                    className={`px-4 py-2 rounded-lg font-bold text-sm transition 
                                        ${reservation.status === 'CANCELED' 
                                            ? 'bg-gray-100 text-gray-400' 
                                            : 'bg-red-50 text-red-600 hover:bg-red-600 hover:text-white border border-red-200 hover:border-red-600'
                                        }`}
                                >
                                    {reservation.status === 'CANCELED' ? 'Cancelled' : 'Cancel'}
                                </button>
                            </div>
                        </div>
                    ))}
                </div>
            )}
        </div>
    );
};

export default Reservations;