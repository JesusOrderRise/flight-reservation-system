import { useState } from 'react';
import { airportService } from '../services/airportService';

const AddAirport = ({ isOpen, onClose, onSuccess }) => {
    const [formData, setFormData] = useState({
        iataCode: '',
        name: '',
        country: '',
        city: ''
    });
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);

    //Unless called draws nothing
    if (!isOpen) return null;

    const handleInputChange = (e) => {
        const { name, value } = e.target;
        setFormData(prev => ({
            ...prev,
            [name]: value
        }));
    };

    const handleSubmit = async (e) => {
        e.preventDefault(); 
        setLoading(true);
        setError(null);

        try {
            await airportService.createAirport(formData);
            
            setFormData({ iataCode: '', name: '', country: '', city: '' });
            
            alert('Airport successfully added!');
            
            //after addition refreshes the list
            onSuccess();
            
} catch (err) {
    
    if (err.response && err.response.data) {
        
        if (err.response.data.message) {
            setError(err.response.data.message);
        } 
        
        else {
            setError("Unexpected error occured!");
        }
    }
    
    } finally {
        setLoading(false);
    }
};

    return (
        <div className="fixed inset-0 bg-black/50 flex justify-center items-center z-50">
            
            <div className="bg-white rounded-lg shadow-xl w-full max-w-md p-6">
                <div className="flex justify-between items-center mb-4 border-b pb-2">
                    <h2 className="text-xl font-bold text-gray-800">Add New Airport</h2>
                </div>

                {error && (
                    <div className="bg-red-50 text-red-600 p-3 rounded mb-4 text-sm border border-red-200">
                        {error}
                    </div>
                )}

                <form onSubmit={handleSubmit} className="space-y-4">
                    <div>
                        <label className="block text-sm font-medium text-gray-700 mb-1">IATA Code</label>
                        <input
                            required
                            name="iataCode"
                            value={formData.iataCode}
                            onChange={handleInputChange}
                            className="w-full border p-2 rounded focus:outline-none focus:ring-2 focus:ring-blue-500 uppercase"
                            placeholder="e.g. IST"
                            maxLength="3"
                        />
                    </div>
                    
                    <div>
                        <label className="block text-sm font-medium text-gray-700 mb-1">Name</label>
                        <input
                            required
                            name="name"
                            value={formData.name}
                            onChange={handleInputChange}
                            className="w-full border p-2 rounded focus:outline-none focus:ring-2 focus:ring-blue-500"
                            placeholder="e.g. Istanbul Airport"
                        />
                    </div>

                    <div>
                        <label className="block text-sm font-medium text-gray-700 mb-1">Country</label>
                        <input
                            required
                            name="country"
                            value={formData.country}
                            onChange={handleInputChange}
                            className="w-full border p-2 rounded focus:outline-none focus:ring-2 focus:ring-blue-500"
                            placeholder="e.g. Turkey"
                        />
                    </div>

                    <div>
                        <label className="block text-sm font-medium text-gray-700 mb-1">City</label>
                        <input
                            required
                            name="city"
                            value={formData.city}
                            onChange={handleInputChange}
                            className="w-full border p-2 rounded focus:outline-none focus:ring-2 focus:ring-blue-500"
                            placeholder="e.g. Istanbul"
                        />
                    </div>

                    <div className="flex justify-end gap-3 mt-6 pt-4 border-t">
                        <button
                            type="button"
                            onClick={onClose}
                            className="px-4 py-2 bg-gray-100 text-gray-700 rounded hover:bg-gray-200 transition"
                        >
                            Cancel
                        </button>
                        <button
                            type="submit"
                            disabled={loading}
                            className="px-4 py-2 bg-blue-600 text-white rounded hover:bg-blue-700 transition disabled:bg-blue-400"
                        >
                            {loading ? 'Saving...' : 'Save Airport'}
                        </button>
                    </div>
                </form>
            </div>
        </div>
    );
};

export default AddAirport;