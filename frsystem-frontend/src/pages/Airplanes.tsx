import React, { useState, useEffect, type ChangeEvent } from 'react';
import { airplaneService } from '../services/AirplaneService';
import AddAirplane from '../components/AddAirplane'; 
import UpdateAirplane from '../components/UpdateAirplane';
import { toast } from 'react-toastify';

interface AirplanesProps {
    isAdmin: boolean;
}

interface SearchQuery {
    tailNumber: string;
    airline: string;
    model: string;
    capacity: string;
}

const Airplanes: React.FC<AirplanesProps> = ({ isAdmin }) => {
    const [airplanes, setAirplanes] = useState<any[]>([]);
    const [isUpdateModalOpen, setIsUpdateModalOpen] = useState<boolean>(false);
    const [selectedAirplane, setSelectedAirplane] = useState<any | null>(null);
    const [searchQuery, setSearchQuery] = useState<SearchQuery>({
        tailNumber: '',
        airline: '',
        model: '',
        capacity: ''
    });
    const [loading, setLoading] = useState<boolean>(false);
    const [isAddModalOpen, setIsAddModalOpen] = useState<boolean>(false);
    const [visibleCount, setVisibleCount] = useState<number>(5);

    // Fetch all airplanes function.
    const fetchAllAirplanes = async () => {
        try {
            const data = await airplaneService.getAllAirplanes();
            setAirplanes(data);
        } catch (err) {
            console.error("Fetch error:", err);
        }
    };

    // Runs once when loaded
    useEffect(() => {
        airplaneService.getAllAirplanes()
            .then(data => setAirplanes(data))
            .catch(err => console.error("Fetch error:", err));
    }, []);

    const searchAirplanes = async () => {
        const isEmpty = Object.values(searchQuery).every(val => val === '');
        
        if (isEmpty) {
            fetchAllAirplanes();
        } else {
            setLoading(true);
            try {
                const data = await airplaneService.searchAirplane(searchQuery);
                setAirplanes(data);
            } catch (err) {
                console.error('Search error:', err);
            } finally {
                setLoading(false);
            }
        }
    };

    const handleInputChange = (e: ChangeEvent<HTMLInputElement>) => {
        const { name, value } = e.target;
        setSearchQuery(prev => ({
            ...prev,
            [name]: value
        }));
    };

    const handleSearch = () => {
        searchAirplanes();
    };

    const handleClear = () => {
        setSearchQuery({
            tailNumber: '',
            airline: '',
            model: '',
            capacity: ''
        });
        fetchAllAirplanes();
    };

    const deleteAirplane = async (airplane: any) => {
        // Confirmation before deleting.
        if (!window.confirm(`Are you sure you want to delete ${airplane.tailNumber}?`)) return;

        try {
            await airplaneService.deleteAirplane(airplane.id);
            toast.success('Successfully deleted!');
            
            const hasSearch = Object.values(searchQuery).some(val => val !== '');
            if (hasSearch) {
                const data = await airplaneService.searchAirplane(searchQuery);
                setAirplanes(data);
            } else {
                fetchAllAirplanes();
            }
        } catch (error: any) {
            console.error("Deleting Error:", error);
            if (error.response && error.response.data) {
                toast.error(error.response.data);
            }
        }
    };

    return (
        <div className="p-4">
            <div className="flex flex-row justify-between items-center gap-4 mb-6 border-b pb-4">
                <div className="flex flex-row justify-start items-center gap-2 flex-1">
                    <input
                        name="tailNumber"
                        placeholder="Tail Number"
                        value={searchQuery.tailNumber}
                        onChange={handleInputChange}
                        className="border p-2 rounded focus:outline-none focus:ring-2 focus:ring-blue-500"
                    />
                    <input
                        name="airline"
                        placeholder="Airline"
                        value={searchQuery.airline}
                        onChange={handleInputChange}
                        className="border p-2 rounded focus:outline-none focus:ring-2 focus:ring-blue-500"
                    />
                    <input
                        name="model"
                        placeholder="Model"
                        value={searchQuery.model}
                        onChange={handleInputChange}
                        className="border p-2 rounded focus:outline-none focus:ring-2 focus:ring-blue-500"
                    />
                    <input
                        name="capacity"
                        placeholder="Capacity"
                        value={searchQuery.capacity}
                        onChange={handleInputChange}
                        className="border p-2 rounded focus:outline-none focus:ring-2 focus:ring-blue-500"
                    />
                </div>
                <div className="flex flex-row justify-end gap-2 shrink-0">
                    <button 
                        className="bg-green-600 text-white px-4 py-2 rounded hover:bg-green-700 transition"
                        onClick={handleSearch}
                        disabled={loading}
                    >
                        {loading ? 'Searching...' : 'Search'}
                    </button>
                    <button 
                        className="bg-gray-600 text-white px-4 py-2 rounded hover:bg-gray-700 transition"
                        onClick={handleClear}
                    >
                        Clear
                    </button>
                </div>
            </div>

            <div className="flex justify-between items-center mb-4">
                <h2 className="text-xl font-bold text-gray-800">
                    {airplanes.length} Airplanes Found
                </h2>
                
                {isAdmin && (
                    <button 
                        onClick={() => setIsAddModalOpen(true)}
                        className="bg-red-600 text-white px-4 py-2 rounded font-semibold hover:bg-red-700 transition shadow-sm"
                    >
                        + Add Airplane
                    </button>
                )}
            </div>

            <div className="overflow-x-auto bg-white rounded-lg shadow">
                <table className="min-w-full border-collapse">
                    <thead className="bg-gray-100 border-b-2 border-gray-200">
                        <tr>
                            <th className="py-3 px-4 text-left text-sm font-semibold text-gray-600 tracking-wider">ID</th>
                            <th className="py-3 px-4 text-left text-sm font-semibold text-gray-600 tracking-wider">Tail Number</th>
                            <th className="py-3 px-4 text-left text-sm font-semibold text-gray-600 tracking-wider">Airline</th>
                            <th className="py-3 px-4 text-left text-sm font-semibold text-gray-600 tracking-wider">Model</th>
                            <th className="py-3 px-4 text-left text-sm font-semibold text-gray-600 tracking-wider">Capacity</th>
                            {isAdmin && (
                                <th className="py-3 px-4 text-right text-sm font-semibold text-gray-600 tracking-wider">Actions</th>
                            )}
                        </tr>
                    </thead>
                    <tbody className="divide-y divide-gray-200">
                        {airplanes.slice(0, visibleCount).map((airplane) => (
                            <tr key={airplane.id} className="hover:bg-gray-50 transition">
                                <td className="py-3 px-4 text-sm text-gray-700">{airplane.id}</td>
                                <td className="py-3 px-4 text-sm font-medium text-gray-900">{airplane.tailNumber}</td>
                                <td className="py-3 px-4 text-sm text-gray-700">{airplane.airline}</td>
                                <td className="py-3 px-4 text-sm text-gray-700">{airplane.model}</td>
                                <td className="py-3 px-4 text-sm text-gray-700">{airplane.capacity}</td>
                                
                                {isAdmin && (
                                    <td className="py-3 px-4 text-right whitespace-nowrap text-sm">
                                        <button 
                                            className="text-blue-600 hover:text-blue-900 font-medium mr-4 transition" 
                                            onClick={() => {
                                                setSelectedAirplane(airplane);
                                                setIsUpdateModalOpen(true);
                                            }}
                                        >
                                            Edit
                                        </button>
                                        <button 
                                            className="text-red-600 hover:text-red-900 font-medium transition" 
                                            onClick={() => deleteAirplane(airplane)}
                                        >
                                            Delete
                                        </button>
                                    </td>
                                )}
                            </tr>
                        ))}
                    </tbody>
                </table>
                {visibleCount < airplanes.length && (
                    <div className="flex justify-center mt-6 mb-4">
                        <button 
                            onClick={() => setVisibleCount(prev => prev + 5)} 
                            className="bg-gray-100 border border-gray-300 text-gray-700 px-6 py-2 rounded-full font-semibold hover:bg-gray-200 transition shadow-sm"
                        >
                            Load More ↓
                        </button>
                    </div>
                )}
                
                {airplanes.length === 0 && !loading && (
                    <div className="text-center py-8 text-gray-500">
                        No airplanes found matching your criteria.
                    </div>
                )}
            </div>
            <AddAirplane 
                isOpen={isAddModalOpen} 
                onClose={() => setIsAddModalOpen(false)} 
                onSuccess={() => {
                    setIsAddModalOpen(false); 
                    fetchAllAirplanes(); 
                }} 
            />
            <UpdateAirplane
                isOpen={isUpdateModalOpen} 
                onClose={() => setIsUpdateModalOpen(false)} 
                airplane={selectedAirplane}
                onSuccess={() => {
                    setIsUpdateModalOpen(false); 
                    const hasSearch = Object.values(searchQuery).some(val => val !== '');
                    if (hasSearch) {
                        searchAirplanes();
                    } else {
                        fetchAllAirplanes(); 
                    }
                }} 
            />
        </div>
    );
};

export default Airplanes;