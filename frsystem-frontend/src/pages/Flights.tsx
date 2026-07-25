import React, { useState, useEffect, type ChangeEvent } from 'react';
import { flightService } from '../services/FlightService';
import { airportService } from '../services/AirportService';
import AddFlight from '../components/AddFlight';
import Reserve from '../components/Reserve';
import { toast } from 'react-toastify';

interface FlightsProps {
    isAdmin: boolean;
}

interface SearchParams {
    flightNumber: string;
    departureAirportId: string;
    arrivalAirportId: string;
}

const Flights: React.FC<FlightsProps> = ({ isAdmin }) => {
    const [flights, setFlights] = useState<any[]>([]);
    const [airports, setAirports] = useState<any[]>([]);
    const [loading, setLoading] = useState<boolean>(false);
    const [isAddModalOpen, setIsAddModalOpen] = useState<boolean>(false);
    const [isReserveModalOpen, setIsReserveModalOpen] = useState<boolean>(false);
    const [selectedFlightForReserve, setSelectedFlightForReserve] = useState<any | null>(null);
    
    const [searchParams, setSearchParams] = useState<SearchParams>({
        flightNumber: '',
        departureAirportId: '',
        arrivalAirportId: ''
    });

    const fetchAllFlights = async () => {
        setLoading(true);
        try {
            const data = await flightService.getAllFlights();
            setFlights(data);
        } catch (err) {
            console.error("Fetch error:", err);
        } finally {
            setLoading(false);
        }
    };


    useEffect(() => {
        flightService.getAllFlights()
                    .then(data => setFlights(data))
                    .catch(err => console.error("Fetch error:", err));
        airportService.getAllAirports()
                    .then(data => setAirports(data))
                    .catch(err => console.error("Fetch error:", err));
    }, []);

    const handleSearch = async (e: any) => {
        e.preventDefault();
        setLoading(true);
        try {
            const payload = {
                flightNumber: searchParams.flightNumber.trim() !== '' ? searchParams.flightNumber : null,
                departureAirportId: searchParams.departureAirportId ? Number(searchParams.departureAirportId) : null,
                arrivalAirportId: searchParams.arrivalAirportId ? Number(searchParams.arrivalAirportId) : null,
                airplaneId: null,
                departureTime: null,
                arrivalTime: null
            };

            const data = await flightService.searchFlights(payload);
            setFlights(data);
        } catch (error: any) {
            toast.error(error.response?.data);
        } finally {
            setLoading(false);
        }
    };

    const clearSearch = () => {
        setSearchParams({ flightNumber: '', departureAirportId: '', arrivalAirportId: '' });
        fetchAllFlights();
    };

    const handleStatusChange = async (flightId: number | string, newStatus: string) => {
        try {
            await flightService.updateFlightStatus(flightId, newStatus);
            toast.success(`Flight updated as ${newStatus}`);
            
            setFlights(prevFlights => 
                prevFlights.map(flight => 
                    flight.id === flightId ? { ...flight, status: newStatus } : flight
                )
            );
        } catch (error: any) {
            toast.error(error.response?.data || "Couldnt Update");
        }
    };

    const deleteFlight = async (flightId: number | string) => {
        if (!window.confirm("Are you sure you want to delete?")) return;

        try {
            await flightService.deleteFlight(flightId);
            toast.success("Flight has been deleted successfully!");
            fetchAllFlights();
        } catch (error: any) {
            console.error("Deleting Error:", error);
            if (error.response && error.response.data) {
                
                const errorMsg = error.response.data.message || error.response.data;
                toast.error(errorMsg);
            }
            }
    };

    const getStatusStyle = (status: string) => {
        switch (status) {
            case 'ACTIVE': return 'bg-blue-100 text-blue-800 border-blue-200';
            case 'FINISHED': return 'bg-gray-100 text-gray-800 border-gray-200';
            case 'CANCELED': return 'bg-red-100 text-red-800 border-red-200';
            default: return 'bg-gray-100 text-gray-800';
        }
    };

    return (
        <div className="p-4">
            <form onSubmit={handleSearch} className="flex flex-row justify-between items-center gap-4 mb-6 border-b pb-4">
                <div className="flex flex-row flex-wrap justify-start items-center gap-2 flex-1">
                    <input 
                        type="text" 
                        value={searchParams.flightNumber}
                        onChange={(e: ChangeEvent<HTMLInputElement>) => setSearchParams({...searchParams, flightNumber: e.target.value})}
                        className="border p-2 rounded focus:outline-none focus:ring-2 focus:ring-blue-500 w-50"
                        placeholder="Flight Number"
                    />
                    <select 
                        value={searchParams.departureAirportId}
                        onChange={(e: ChangeEvent<HTMLSelectElement>) => setSearchParams({...searchParams, departureAirportId: e.target.value})}
                        className={`border p-2 rounded focus:outline-none focus:ring-2 focus:ring-blue-500 w-50 ${!searchParams.departureAirportId ? 'text-gray-400' : 'text-black'}`}
                    >
                        <option value="">Departure</option>
                        {airports.map(a => (
                            <option key={a.id} value={a.id} className="text-black">{a.iataCode} - {a.city}</option>
                        ))}
                    </select>
                    <select 
                        value={searchParams.arrivalAirportId}
                        onChange={(e: ChangeEvent<HTMLSelectElement>) => setSearchParams({...searchParams, arrivalAirportId: e.target.value})}
                        className={`border p-2 rounded focus:outline-none focus:ring-2 focus:ring-blue-500 w-50 ${!searchParams.arrivalAirportId ? 'text-gray-400' : 'text-black'}`}
                    >
                        <option value="">Arrival</option>
                        {airports.map(a => (
                            <option key={a.id} value={a.id} className="text-black">{a.iataCode} - {a.city}</option>
                        ))}
                    </select>
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
                        onClick={clearSearch} 
                        className="bg-gray-600 text-white px-4 py-2 rounded hover:bg-gray-700 transition"
                    >
                        Clear
                    </button>
                </div>
            </form>

            <div className="flex justify-between items-center mb-4">
                <h2 className="text-xl font-bold text-gray-800">
                    {flights.length} Flights Found
                </h2>
                
                {isAdmin && (
                    <button 
                        onClick={() => setIsAddModalOpen(true)}
                        className="bg-red-600 text-white px-4 py-2 rounded font-semibold hover:bg-red-700 transition shadow-sm"
                    >
                        + Add Flight
                    </button>
                )}
            </div>

            {loading ? (
                <div className="text-center py-10 text-gray-500">Loading...</div>
            ) : flights.length === 0 ? (
                <div className="text-center py-8 text-gray-500 bg-white rounded-lg shadow border">
                    No flights found matching your criteria.
                </div>
            ) : ( 
                <div className="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3 gap-6">
                    {flights.map((flight) => (
                        <div key={flight.id} className="bg-white rounded-lg shadow hover:shadow-md transition-shadow duration-300 border border-gray-200 overflow-hidden flex flex-col">
                            <div className="bg-gray-100 px-4 py-3 border-b-2 border-gray-200 flex justify-between items-center">
                                <div className="flex items-center gap-2">
                                    <span className="bg-red-900 text-white text-xs font-black px-2 py-1 rounded tracking-widest">
                                        {flight.flightNumber}
                                    </span>
                                    {isAdmin ? (<span className="text-xs font-semibold text-gray-400">ID: {flight.id}</span>): (<span></span>)}
                                </div>
                                
                                <div className="flex items-center gap-2">
                                    {isAdmin ? (
                                        <select 
                                            value={flight.status}
                                            onChange={(e: ChangeEvent<HTMLSelectElement>) => handleStatusChange(flight.id, e.target.value)}
                                            className={`text-xs font-bold px-2 py-1 rounded-full border outline-none cursor-pointer ${getStatusStyle(flight.status)}`}
                                        >
                                            <option value="ACTIVE">ACTIVE</option>
                                            <option value="FINISHED">FINISHED</option>
                                            <option value="CANCELED">CANCELED</option>
                                        </select>
                                    ) : (
                                        <span className={`text-xs font-bold px-2 py-1 rounded-full border ${getStatusStyle(flight.status)}`}>
                                            {flight.status}
                                        </span>
                                    )}

                                    {isAdmin && (
                                        <button 
                                            onClick={() => deleteFlight(flight.id)}
                                            className="text-red-600 hover:text-red-900 font-medium transition ml-2"
                                            title="Delete Flight"
                                        >
                                            Delete
                                        </button>
                                    )}
                                </div>
                            </div>

                            <div className="p-5 flex-1">
                                <div className="flex justify-between items-center mb-6">
                                    <div className="text-center w-2/5">
                                        <div className="text-3xl font-black text-gray-800">
                                            {flight.departureAirport?.iataCode || 'N/A'} 
                                        </div>
                                        <div className="text-xs text-gray-500 mt-1 truncate">
                                            {flight.departureAirport?.city || 'N/A'}
                                        </div>
                                        <div className="text-xs font-bold text-red-900 mt-2 bg-red-50 py-1 rounded">
                                            {flight.departureTime || 'N/A'}
                                        </div>
                                    </div>
                                    
                                    <div className="w-1/5 flex flex-col items-center">
                                        <span className="text-gray-300 text-2xl">→</span>
                                    </div>
                                    
                                    <div className="text-center w-2/5">
                                        <div className="text-3xl font-black text-gray-800">
                                            {flight.arrivalAirport?.iataCode || 'N/A'}
                                        </div>
                                        <div className="text-xs text-gray-500 mt-1 truncate">
                                            {flight.arrivalAirport?.city || 'N/A'}
                                        </div>
                                        <div className="text-xs font-bold text-red-900 mt-2 bg-red-50 py-1 rounded">
                                            {flight.arrivalTime || 'N/A'}
                                        </div>
                                    </div>
                                </div>

                                <div className="bg-red-50 rounded-lg p-3 flex justify-between items-center text-sm border border-red-100">
                                    <div className="flex flex-col">
                                        <span className="text-red-400 text-xs font-semibold">Plane Info</span>
                                        <span className="text-red-900 font-bold">
                                            {flight.airplane?.airline || 'Airline'} - {flight.airplane?.tailNumber || 'Tail Number'}
                                        </span>
                                    </div>
                                    <div className="flex flex-col text-right">
                                        <span className="text-red-400 text-xs font-semibold">Capacity</span>
                                        <span className="text-red-900 font-bold">
                                            {flight.airplane?.capacity || '0'} 
                                        </span>
                                    </div>
                                </div>
                            </div>
                            <div className="flex justify-center mb-4">
                                <button 
                                    onClick={() => {
                                        setSelectedFlightForReserve(flight); 
                                        setIsReserveModalOpen(true);
                                    }}
                                    className="bg-green-600 text-white px-6 py-2 rounded font-semibold hover:bg-green-700 transition"
                                >
                                    Reserve
                                </button>
                            </div>
                        </div>
                    ))}
                </div>
            )}

            <AddFlight 
                isOpen={isAddModalOpen} 
                onClose={() => setIsAddModalOpen(false)} 
                onSuccess={() => {
                    setIsAddModalOpen(false); 
                    fetchAllFlights(); 
                }} 
            />

            <Reserve 
                isOpen={isReserveModalOpen} 
                flight={selectedFlightForReserve}
                onClose={() => setIsReserveModalOpen(false)} 
                onSuccess={() => {
                    setIsReserveModalOpen(false);  
                }} 
            />
        </div>
    );
};

export default Flights;