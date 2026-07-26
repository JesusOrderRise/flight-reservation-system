import React, { useState, type ChangeEvent } from 'react';
import { airplaneService } from '../services/AirplaneService';

interface UpdateAirplaneProps {
    isOpen: boolean;
    onClose: () => void;
    onSuccess: () => void;
    airplane: any | null;
}

interface AirplaneFormData {
    tailNumber: string;
    airline: string;
    model: string;
    capacity: string;
}

const UpdateAirplane: React.FC<UpdateAirplaneProps> = ({ isOpen, onClose, onSuccess, airplane }) => {
    const [formData, setFormData] = useState<AirplaneFormData>({
        tailNumber: airplane?.tailNumber || '',
        airline: airplane?.airline || '',
        model: airplane?.model || '',
        capacity: airplane?.capacity ? airplane.capacity.toString() : ''
    });
    const [loading, setLoading] = useState<boolean>(false);
    const [error, setError] = useState<string>('');


    const [prevAirplaneId, setPrevAirplaneId] = useState<string | null>(null);

    
    if (airplane && airplane.id !== prevAirplaneId) {
        setPrevAirplaneId(airplane.id); 
        setFormData({
            tailNumber: airplane.tailNumber || '',
            airline: airplane.airline || '',
            model: airplane.model || '',
            capacity: airplane.capacity ? airplane.capacity.toString() : ''
        });
    }

    if (!isOpen) return null;

    const handleChange = (e: ChangeEvent<HTMLInputElement>) => {
        setFormData({ ...formData, [e.target.name]: e.target.value });
    };

    const handleSubmit = async (e: any) => {
        e.preventDefault();
        if (!airplane) return;
        
        setLoading(true);
        setError('');

        try {
            await airplaneService.updateAirplane(airplane.id, formData);
            onSuccess(); // On success reloads list
        } catch (err: any) {
            console.error("Update Error:", err);
            setError(err.response?.data?.message || err.response?.data || "Update Unsuccessfull.");
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50">
            <div className="bg-white p-6 rounded-lg shadow-xl w-full max-w-md">
                <h2 className="text-xl font-bold mb-4 text-gray-800">Update Airplane</h2>
                
                {error && (
                    <div className="mb-4 p-2 bg-red-50 text-red-600 border border-red-200 rounded text-sm">
                        {error}
                    </div>
                )}

                <form onSubmit={handleSubmit} className="space-y-4">
                    <div>
                        <label className="block text-sm font-medium text-gray-700">Tail Number</label>
                        <input type="text" name="tailNumber" value={formData.tailNumber} onChange={handleChange} required className="mt-1 block w-full border p-2 rounded focus:ring-blue-500 focus:border-blue-500" />
                    </div>
                    <div>
                        <label className="block text-sm font-medium text-gray-700">Airline</label>
                        <input type="text" name="airline" value={formData.airline} onChange={handleChange} required className="mt-1 block w-full border p-2 rounded focus:ring-blue-500 focus:border-blue-500" />
                    </div>
                    <div>
                        <label className="block text-sm font-medium text-gray-700">Model</label>
                        <input type="text" name="model" value={formData.model} onChange={handleChange} required className="mt-1 block w-full border p-2 rounded focus:ring-blue-500 focus:border-blue-500" />
                    </div>
                    <div>
                        <label className="block text-sm font-medium text-gray-700">Capacity</label>
                        <input type="text" name="capacity" value={formData.capacity} onChange={handleChange} required className="mt-1 block w-full border p-2 rounded focus:ring-blue-500 focus:border-blue-500" />
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

export default UpdateAirplane;