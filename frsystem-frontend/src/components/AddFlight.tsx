import React, { useState, useEffect, type ChangeEvent } from 'react';
import { flightService } from '../services/FlightService';
import { airportService } from '../services/AirportService';
import { airplaneService } from '../services/AirplaneService';
import { toast } from 'react-toastify';

interface AddFlightProps {
    isOpen: boolean;
    onClose: () => void;
    onSuccess: () => void;
}

interface FlightFormData {
    flightNumber: string;
    departureAirportId: string;
    arrivalAirportId: string;
    airplaneId: string;
    departureTime: string;
    arrivalTime: string;
}

const AddFlight: React.FC<AddFlightProps> = ({ isOpen, onClose, onSuccess }) => {
    const [formData, setFormData] = useState<FlightFormData>({
        flightNumber: '',
        departureAirportId: '',
        arrivalAirportId: '',
        airplaneId: '',
        departureTime: '',
        arrivalTime: ''
    });

    const [airports, setAirports] = useState<any[]>([]);
    const [airplanes, setAirplanes] = useState<any[]>([]);
    const [loading, setLoading] = useState<boolean>(false);

    useEffect(() => {
        const fetchDropdownData = async () => {
            try {
                const [airportData, airplaneData] = await Promise.all([
                    airportService.getAllAirports(),
                    airplaneService.getAllAirplanes()
                ]);
                setAirports(airportData);
                setAirplanes(airplaneData);
                setFormData({
                    flightNumber: '',
                    departureAirportId: '',
                    arrivalAirportId: '',
                    airplaneId: '',
                    departureTime: '',
                    arrivalTime: ''
                });
            } catch (error: any) {
                toast.error("Error when fetching data");
            }
        };

        if (isOpen) {
            fetchDropdownData();
        }
    }, [isOpen]);

    const handleChange = (e: ChangeEvent<HTMLInputElement | HTMLSelectElement>) => {
        const { name, value } = e.target;
        setFormData(prev => ({
            ...prev,
            [name]: value
        }));
    };

    const handleSubmit = async (e: any) => {
        e.preventDefault();
        setLoading(true);

        try {
            // turning html datetime to request format which is yyyy-MM-dd HH:mm
            const formattedDeparture = formData.departureTime.replace('T', ' ');
            const formattedArrival = formData.arrivalTime.replace('T', ' ');

            const payload = {
                flightNumber: formData.flightNumber,
                airplaneId: Number(formData.airplaneId),
                departureAirportId: Number(formData.departureAirportId),
                arrivalAirportId: Number(formData.arrivalAirportId),
                departureTime: formattedDeparture,
                arrivalTime: formattedArrival
            };

            await flightService.createFlight(payload);
            toast.success("Flight Has been added successfully!");
            onSuccess(); 
        } catch (error: any) {
            console.error("Flight Create Error:", error);
            
            const errorData = error.response?.data;
            let errorMessage = "Error";
            
            if (typeof errorData === 'string') {
                errorMessage = errorData;
            } else if (errorData && errorData.message) {
                errorMessage = errorData.message;
            }
            
            toast.error(errorMessage);
        } finally {
            setLoading(false);
        }
    };

    if (!isOpen) return null;

    return (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
            <div className="bg-white p-6 rounded-xl shadow-2xl w-full max-w-2xl max-h-[90vh] overflow-y-auto">
                <div className="flex justify-between items-center mb-6 border-b pb-3">
                    <h2 className="text-2xl font-bold text-gray-800">Add New Flight</h2>
                    <button onClick={onClose} className="text-gray-500 hover:text-red-500 text-2xl transition">
                        &times;
                    </button>
                </div>

                <form onSubmit={handleSubmit} className="space-y-5">
                    <div>
                        <label className="block text-sm font-semibold text-gray-700 mb-1">Flight Number</label>
                        <input 
                            type="text" 
                            name="flightNumber" 
                            value={formData.flightNumber} 
                            onChange={handleChange} 
                            required 
                            className="w-full border border-gray-300 p-2.5 rounded-lg focus:ring-2 focus:ring-indigo-500 outline-none uppercase" 
                            placeholder="Flight Number"
                        />
                    </div>

                    <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                        <div>
                            <label className="block text-sm font-semibold text-gray-700 mb-1">Departure Airport</label>
                            <select 
                                name="departureAirportId" 
                                value={formData.departureAirportId} 
                                onChange={handleChange} 
                                required 
                                className="w-full border border-gray-300 p-2.5 rounded-lg focus:ring-2 focus:ring-indigo-500 outline-none"
                            >
                                <option value="">Choose...</option>
                                {airports.map(airport => (
                                    <option key={airport.id} value={airport.id}>
                                        {airport.iataCode} - {airport.city}
                                    </option>
                                ))}
                            </select>
                        </div>
                        
                        <div>
                            <label className="block text-sm font-semibold text-gray-700 mb-1">Arrival Airport</label>
                            <select 
                                name="arrivalAirportId" 
                                value={formData.arrivalAirportId} 
                                onChange={handleChange} 
                                required 
                                className="w-full border border-gray-300 p-2.5 rounded-lg focus:ring-2 focus:ring-indigo-500 outline-none"
                            >
                                <option value="">Choose...</option>
                                {airports.map(airport => (
                                    <option key={airport.id} value={airport.id}>
                                        {airport.iataCode} - {airport.city}
                                    </option>
                                ))}
                            </select>
                        </div>
                    </div>

                    <div>
                        <label className="block text-sm font-semibold text-gray-700 mb-1">Airplane</label>
                        <select 
                            name="airplaneId" 
                            value={formData.airplaneId} 
                            onChange={handleChange} 
                            required 
                            className="w-full border border-gray-300 p-2.5 rounded-lg focus:ring-2 focus:ring-indigo-500 outline-none"
                        >
                            <option value="">Choose...</option>
                            {airplanes.map(plane => (
                                <option key={plane.id} value={plane.id}>
                                    {plane.airline} | {plane.model} (Capacity: {plane.capacity} - {plane.tailNumber})
                                </option>
                            ))}
                        </select>
                    </div>

                    <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                        <div>
                            <label className="block text-sm font-semibold text-gray-700 mb-1">Departure Time</label>
                            <input 
                                type="datetime-local" 
                                name="departureTime" 
                                value={formData.departureTime} 
                                onChange={handleChange} 
                                required 
                                className="w-full border border-gray-300 p-2.5 rounded-lg focus:ring-2 focus:ring-indigo-500 outline-none"
                            />
                        </div>
                        
                        <div>
                            <label className="block text-sm font-semibold text-gray-700 mb-1">Arrival Time</label>
                            <input 
                                type="datetime-local" 
                                name="arrivalTime" 
                                value={formData.arrivalTime} 
                                onChange={handleChange} 
                                required 
                                className="w-full border border-gray-300 p-2.5 rounded-lg focus:ring-2 focus:ring-indigo-500 outline-none"
                            />
                        </div>
                    </div>
                    
                    <div className="flex justify-end space-x-3 pt-4 border-t mt-6">
                        <button 
                            type="button" 
                            onClick={onClose} 
                            className="px-5 py-2.5 text-gray-700 bg-gray-100 font-semibold rounded-lg hover:bg-gray-200 transition"
                        >
                            İptal
                        </button>
                        <button 
                            type="submit" 
                            disabled={loading} 
                            className="px-5 py-2.5 bg-indigo-600 text-white font-semibold rounded-lg hover:bg-indigo-700 transition disabled:bg-indigo-400 flex items-center"
                        >
                            {loading ? 'Adding Flight...' : 'Add Flight'}
                        </button>
                    </div>
                </form>
            </div>
        </div>
    );
};

export default AddFlight;