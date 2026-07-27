import React, { useState, useEffect, type ChangeEvent } from 'react';
import { toast } from 'react-toastify';
import { reservationService } from '../services/ReservationService';

interface ReservationsProps {
    isAdmin: boolean;
}

interface ReservationSearchParams {
    id: string;
    flightNumber: string;
    firstName: string;
    lastName: string;
}

const Reservations: React.FC<ReservationsProps> = ({ isAdmin }) => {
    const [reservations, setReservations] = useState<any[]>([]);
    
    const [loading, setLoading] = useState<boolean>(true);
    const [activeTab, setActiveTab] = useState<string>('MY_RESERVATIONS');
    
    const [searchParams, setSearchParams] = useState<ReservationSearchParams>({
        id: '',
        flightNumber: '',
        firstName: '',
        lastName: ''
    });

    const fetchAllReservations = React.useCallback(async () => {
        try {
            const data = (isAdmin && activeTab === 'ALL_RESERVATIONS')
                ? await reservationService.getAllReservations()
                : await reservationService.getMyReservations();
            setReservations(data);
        } catch (error) {
            console.error("Fetch Error:", error);
            toast.error("Failed to load reservations!");
        } finally {
            setLoading(false);
        }
    }, [isAdmin, activeTab]);

    useEffect(() => {
        // eslint-disable-next-line
        fetchAllReservations();
    }, [fetchAllReservations]);

    const handleSearch = async (e: any) => {
        e.preventDefault();
        setLoading(true);
        try {
            const payload = {
                id: searchParams.id ? Number(searchParams.id) : null,
                flightNumber: searchParams.flightNumber ? String(searchParams.flightNumber) : null,
                firstName: searchParams.firstName.trim() !== '' ? searchParams.firstName : null,
                lastName: searchParams.lastName.trim() !== '' ? searchParams.lastName : null
            };

            const data = await reservationService.searchReservation(payload);
            setReservations(data);
        } catch (error: any) {
            toast.error(error.response?.data || "Search failed");
        } finally {
            setLoading(false);
        }
    };

    const handleClear = () => {
        setSearchParams({ id: '', flightNumber: '', firstName: '', lastName: '' });
        fetchAllReservations();
    };

    const handleCancel = async (id: number | string) => {
        if (!window.confirm("Are you sure you want to cancel this reservation?")) return;

        setLoading(true);
        try {
            if (isAdmin && activeTab === 'ALL_RESERVATIONS') {
                await reservationService.adminCancelReservation(id);
            } else {
                await reservationService.cancelSelfReservation(id);
            }
            toast.success("Reservation has been cancelled.");
            
            fetchAllReservations();

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
            setLoading(false); 
        }
    };

    const getStatusStyle = (status: string) => {
        switch (status) {
            case 'CONFIRMED': return 'bg-green-100 text-green-800 border-green-200';
            case 'CANCELED': return 'bg-gray-200 text-gray-600 border-gray-300 line-through opacity-70';
            default: return 'bg-blue-100 text-blue-800 border-blue-200';
        }
    };

    return (
        <div className="p-4">
            <div className="flex justify-between items-center mb-6 border-b pb-4">
                <h2 className="text-2xl font-bold text-gray-800">
                    Reservations
                </h2>
            </div>

            {isAdmin && (
                <div className="flex flex-col mb-8 gap-4">
                    <div className="flex gap-4">
                        <button 
                            onClick={() => {
                                setActiveTab('MY_RESERVATIONS');
                                setSearchParams({ id: '', flightNumber: '', firstName: '', lastName: '' });
                                setLoading(true); 
                            }}
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
                                setActiveTab('ALL_RESERVATIONS');
                                setSearchParams({ id: '', flightNumber: '', firstName: '', lastName: '' });
                                setLoading(true);
                            }}
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
                        <form onSubmit={handleSearch} className="flex flex-row justify-between items-center gap-4 mt-2">
                            <div className="flex flex-row flex-wrap justify-start items-center gap-2 flex-1">
                                <input 
                                    type="text" 
                                    value={searchParams.id}
                                    onChange={(e: ChangeEvent<HTMLInputElement>) => setSearchParams({...searchParams, id: e.target.value})}
                                    className="border p-2 rounded focus:outline-none focus:ring-2 focus:ring-blue-500 w-40"
                                    placeholder="ID"
                                />
                                <input 
                                    type="text" 
                                    value={searchParams.flightNumber}
                                    onChange={(e: ChangeEvent<HTMLInputElement>) => setSearchParams({...searchParams, flightNumber: e.target.value})}
                                    className="border p-2 rounded focus:outline-none focus:ring-2 focus:ring-blue-500 w-40"
                                    placeholder="Flight Number"
                                />
                                <input 
                                    type="text" 
                                    value={searchParams.firstName}
                                    onChange={(e: ChangeEvent<HTMLInputElement>) => setSearchParams({...searchParams, firstName: e.target.value})}
                                    className="border p-2 rounded focus:outline-none focus:ring-2 focus:ring-blue-500 w-40"
                                    placeholder="First Name"
                                />
                                <input 
                                    type="text" 
                                    value={searchParams.lastName}
                                    onChange={(e: ChangeEvent<HTMLInputElement>) => setSearchParams({...searchParams, lastName: e.target.value})}
                                    className="border p-2 rounded focus:outline-none focus:ring-2 focus:ring-blue-500 w-40"
                                    placeholder="Last Name"
                                />
                            </div>
                            <div className="flex flex-row justify-end gap-2 shrink-0">
                                <button 
                                    type="submit" 
                                    className="bg-green-600 text-white px-4 py-2 rounded hover:bg-green-700 transition"
                                    disabled={loading}
                                >
                                    {loading ? 'Searching...' : 'Search'}
                                </button>
                                <button 
                                    type="button" 
                                    onClick={handleClear} 
                                    className="bg-gray-600 text-white px-4 py-2 rounded hover:bg-gray-700 transition"
                                >
                                    Clear
                                </button>
                            </div>
                        </form>
                    )}
                </div>
            )}

            {loading ? (
                <div className="text-center py-10 text-gray-500 font-semibold">Loading...</div>
            ) : reservations.length === 0 ? (
                <div className="text-center py-10 text-gray-500 bg-white rounded-lg shadow-sm border border-dashed">
                    No reservations found.
                </div>
            ) : (
                <div className="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3 gap-6">
                    {reservations.map((reservation) => (
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