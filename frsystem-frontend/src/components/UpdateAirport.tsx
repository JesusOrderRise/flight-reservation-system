import React, { useState, type ChangeEvent } from 'react';
import { airportService } from '../services/AirportService';

interface UpdateAirportProps {
    isOpen: boolean;
    onClose: () => void;
    onSuccess: () => void;
    airport: any | null;
}

interface AirportFormData {
    iataCode: string;
    name: string;
    country: string;
    city: string;
}

const UpdateAirport: React.FC<UpdateAirportProps> = ({ isOpen, onClose, onSuccess, airport }) => {
    const [formData, setFormData] = useState<AirportFormData>({
    iataCode: airport?.iataCode || '',
    name: airport?.name || '',
    country: airport?.country || '',
    city: airport?.city || ''
});
    const [loading, setLoading] = useState<boolean>(false);
    const [error, setError] = useState<string>('');

    const [prevAirportId, setPrevAirportId] = useState<string | null>(null);

   if (airport && airport.id !== prevAirportId) {
        setPrevAirportId(airport.id); 
        setFormData({
            iataCode: airport?.iataCode || '',
            name: airport?.name || '',
            country: airport?.country || '',
            city: airport?.city || ''
        });
    }

    if (!isOpen) return null;

    const handleChange = (e: ChangeEvent<HTMLInputElement>) => {
        setFormData({ ...formData, [e.target.name]: e.target.value });
    };

    const handleSubmit = async (e: any) => {
        e.preventDefault();
        if (!airport) return;

        setLoading(true);
        setError('');

        try {
            await airportService.updateAirport(airport.id, formData);
            onSuccess(); // On success reloads list
        } catch (err: any) {
            console.error("Update Error:", err);
            setError(err.response?.data?.message || err.response?.data || "Update unsuccessfull.");
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50">
            <div className="bg-white p-6 rounded-lg shadow-xl w-full max-w-md">
                <h2 className="text-xl font-bold mb-4 text-gray-800">Update Airport</h2>
                
                {error && (
                    <div className="mb-4 p-2 bg-red-50 text-red-600 border border-red-200 rounded text-sm">
                        {error}
                    </div>
                )}

                <form onSubmit={handleSubmit} className="space-y-4">
                    <div>
                        <label className="block text-sm font-medium text-gray-700">IATA Code</label>
                        <input type="text" name="iataCode" value={formData.iataCode} onChange={handleChange} required className="mt-1 block w-full border p-2 rounded focus:ring-blue-500 focus:border-blue-500" />
                    </div>
                    <div>
                        <label className="block text-sm font-medium text-gray-700">Name</label>
                        <input type="text" name="name" value={formData.name} onChange={handleChange} required className="mt-1 block w-full border p-2 rounded focus:ring-blue-500 focus:border-blue-500" />
                    </div>
                    <div>
                        <label className="block text-sm font-medium text-gray-700">Country</label>
                        <input type="text" name="country" value={formData.country} onChange={handleChange} required className="mt-1 block w-full border p-2 rounded focus:ring-blue-500 focus:border-blue-500" />
                    </div>
                    <div>
                        <label className="block text-sm font-medium text-gray-700">City</label>
                        <input type="text" name="city" value={formData.city} onChange={handleChange} required className="mt-1 block w-full border p-2 rounded focus:ring-blue-500 focus:border-blue-500" />
                    </div>
                    
                    <div className="flex justify-end space-x-3 mt-6">
                        <button type="button" onClick={onClose} className="px-4 py-2 text-gray-600 bg-gray-100 rounded hover:bg-gray-200 transition">
                            Cancel
                        </button>
                        <button type="submit" disabled={loading} className="px-4 py-2 bg-red-600 text-white rounded hover:bg-red-700 transition disabled:bg-red-300">
                            {loading ? 'Updating...' : 'Update'}
                        </button>
                    </div>
                </form>
            </div>
        </div>
    );
};

export default UpdateAirport;