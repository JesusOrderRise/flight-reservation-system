import { useState, useEffect } from 'react';
import { airportService } from '../services/airportService';
import AddAirport from '../components/AddAirport'; 
 
const Airports = ({ onUpdate, isAdmin }) => {
    const [airports, setAirports] = useState([]);
    const [searchQuery, setSearchQuery] = useState({
        iataCode: '',
        name: '',
        country: '',
        city: ''
    });
    const [loading, setLoading] = useState(false);
    const [isAddModalOpen, setIsAddModalOpen] = useState(false);

    const [visibleCount, setVisibleCount] = useState(5);

    // Fetch all airports function.
    const fetchAllAirports = async () => {
        try {
            const data = await airportService.getAllAirports();
            setAirports(data);
        } catch (err) {
            console.error("Fetch error:", err);
        }
    };

    // Runs once when loaded
    useEffect(() => {
        fetchAllAirports();
    }, []);

    const searchAirports = async () => {
        const isEmpty = Object.values(searchQuery).every(val => val === '');
        
        if (isEmpty) {
            fetchAllAirports();
        } else {
            setLoading(true);
            try {
                const data = await airportService.searchAirports(searchQuery);
                setAirports(data);
            } catch (err) {
                console.error('Search error:', err);
            } finally {
                setLoading(false);
            }
        }
    };

    const handleInputChange = (e) => {
        const { name, value } = e.target;
        setSearchQuery(prev => ({
            ...prev,
            [name]: value
        }));
    };

    const handleSearch = () => {
        searchAirports();
    };

    const handleClear = () => {
        setSearchQuery({
            iataCode: '',
            name: '',
            country: '',
            city: ''
        });
        
        fetchAllAirports();
    };

    const deleteAirport = async (airport) => {
        //Confirmation before deleting.
        if (!window.confirm(`Are you sure you want to delete ${airport.name}?`)) return;

        try {
            await airportService.deleteAirport(airport.id);
            alert('Successfully deleted!');
            
            const hasSearch = Object.values(searchQuery).some(val => val !== '');
            if (hasSearch) {
                const data = await airportService.searchAirports(searchQuery);
                setAirports(data);
            } else {
                fetchAllAirports();
            }
        } catch (error) {
            console.error("Deleting Error:", error);
            if (error.response && error.response.data) {
                alert(error.response.data);
            }
        }
    };

    return (
        <div className="p-4">
            <div className="flex flex-row justify-between items-center gap-4 mb-6 border-b pb-4">
                <div className="flex flex-row justify-start items-center gap-2 flex-1">
                    <input
                        name="iataCode"
                        placeholder="IATA Code"
                        value={searchQuery.iataCode}
                        onChange={handleInputChange}
                        className="border p-2 rounded focus:outline-none focus:ring-2 focus:ring-blue-500"
                    />
                    <input
                        name="name"
                        placeholder="Name"
                        value={searchQuery.name}
                        onChange={handleInputChange}
                        className="border p-2 rounded focus:outline-none focus:ring-2 focus:ring-blue-500"
                    />
                    <input
                        name="country"
                        placeholder="Country"
                        value={searchQuery.country}
                        onChange={handleInputChange}
                        className="border p-2 rounded focus:outline-none focus:ring-2 focus:ring-blue-500"
                    />
                    <input
                        name="city"
                        placeholder="City"
                        value={searchQuery.city}
                        onChange={handleInputChange}
                        className="border p-2 rounded focus:outline-none focus:ring-2 focus:ring-blue-500"
                    />
                </div>
                <div className='flex flex-row justify-end gap-2 shrink-0'>
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
                    {airports.length} Airports Found
                </h2>
                
                {/* Only visible for admins */}
                {isAdmin && (
                    <button 
                        onClick={() => setIsAddModalOpen(true)}
                        className="bg-blue-600 text-white px-4 py-2 rounded font-semibold hover:bg-blue-700 transition shadow-sm"
                    >
                        + Add Airport
                    </button>
                )}
            </div>

            <div className="overflow-x-auto bg-white rounded-lg shadow">
                <table className="min-w-full border-collapse">
                    <thead className="bg-gray-100 border-b-2 border-gray-200">
                        <tr>
                            <th className="py-3 px-4 text-left text-sm font-semibold text-gray-600 tracking-wider">ID</th>
                            <th className="py-3 px-4 text-left text-sm font-semibold text-gray-600 tracking-wider">IATA</th>
                            <th className="py-3 px-4 text-left text-sm font-semibold text-gray-600 tracking-wider">Name</th>
                            <th className="py-3 px-4 text-left text-sm font-semibold text-gray-600 tracking-wider">Country</th>
                            <th className="py-3 px-4 text-left text-sm font-semibold text-gray-600 tracking-wider">City</th>
                            {/* Actions Column only visible for admin */}
                            {isAdmin && (
                                <th className="py-3 px-4 text-right text-sm font-semibold text-gray-600 tracking-wider">Actions</th>
                            )}
                        </tr>
                    </thead>
                    {/* slicing for pagination */}
                    <tbody className="divide-y divide-gray-200">
                        {airports.slice(0, visibleCount).map((airport) => (
                            <tr key={airport.id} className="hover:bg-gray-50 transition">
                                <td className="py-3 px-4 text-sm text-gray-700">{airport.id}</td>
                                <td className="py-3 px-4 text-sm font-medium text-gray-900">{airport.iataCode}</td>
                                <td className="py-3 px-4 text-sm text-gray-700">{airport.name}</td>
                                <td className="py-3 px-4 text-sm text-gray-700">{airport.country}</td>
                                <td className="py-3 px-4 text-sm text-gray-700">{airport.city}</td>
                                
                                {/* Action Buttons only visible if user is admin */}
                                {isAdmin && (
                                    <td className="py-3 px-4 text-right whitespace-nowrap text-sm">
                                        <button 
                                            className="text-blue-600 hover:text-blue-900 font-medium mr-4 transition" 
                                            onClick={() => onUpdate(airport)}
                                        >
                                            Edit
                                        </button>
                                        <button 
                                            className="text-red-600 hover:text-red-900 font-medium transition" 
                                            onClick={() => deleteAirport(airport)}
                                        >
                                            Delete
                                        </button>
                                    </td>
                                )}
                            </tr>
                        ))}
                    </tbody>
                </table>
                {visibleCount < airports.length && (
                    <div className="flex justify-center mt-6 mb-4">
                        <button 
                            onClick={() => setVisibleCount(prev => prev + 5)} 
                            className="bg-gray-100 border border-gray-300 text-gray-700 px-6 py-2 rounded-full font-semibold hover:bg-gray-200 transition shadow-sm"
                        >
                        Load More ↓
                        </button>
                    </div>
)}
                
                {airports.length === 0 && !loading && (
                    <div className="text-center py-8 text-gray-500">
                        No airports found matching your criteria.
                    </div>
                )}
            </div>
            <AddAirport 
                isOpen={isAddModalOpen} 
                onClose={() => setIsAddModalOpen(false)} 
                onSuccess={() => {
                    setIsAddModalOpen(false); 
                    fetchAllAirports(); 
                }} 
            />
        </div>
    );
};

export default Airports;