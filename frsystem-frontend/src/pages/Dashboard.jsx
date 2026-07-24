import { useState, useContext } from 'react';
import { AuthContext } from '../context/AuthContext';
import Airports from './Airports';

const Dashboard = () => {
    const [activeTab, setActiveTab] = useState('Airports');
    
    
    const { role, firstName, lastName, logout } = useContext(AuthContext);
    
    
    const isAdmin = role === 'ADMIN';


    return (
        <div className="min-h-screen bg-gray-50">
            
            <header className="bg-white shadow-sm px-8 py-4 flex justify-between items-center">
                <h1 className="text-2xl font-bold text-gray-800">Dashboard</h1>
                
                
                <div className="flex items-center gap-6">
                    <div className="text-right">
                        
                        <div className="text-lg font-bold text-gray-800">
                            {firstName} {lastName}
                        </div>
                        <div className="text-sm mt-1 flex justify-end">
                            <span className="font-semibold text-blue-600 bg-blue-50 px-2 py-0.5 rounded border border-blue-100">
                                {role}
                            </span> 
                        </div>
                    </div>
                    
                    
                    <button 
                        onClick={logout}
                        className="bg-red-50 text-red-600 px-4 py-2 rounded font-medium hover:bg-red-100 transition border border-red-100"
                    >
                        Logout
                    </button>
                </div>
            </header>

            <div className="px-8 mt-6">
                <div className="flex space-x-4 border-b border-gray-200">
                    {['Airports', 'Airplanes', 'Flights', 'Reservations'].map((tab) => (
                        <button
                            key={tab}
                            onClick={() => setActiveTab(tab)}
                            className={`py-2 px-4 font-medium text-sm transition-colors duration-200 border-b-2 mb-[-2px] ${
                                activeTab === tab
                                    ? 'border-blue-600 text-blue-600'
                                    : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
                            }`}
                        >
                            {tab}
                        </button>
                    ))}
                </div>
            </div>

            <main className="p-8">
                {activeTab === 'Airports' && (
                    <Airports 
                    isAdmin={isAdmin}/>
                )}
                
                {activeTab === 'Airplanes' && (
                    <div className="bg-white p-8 rounded-lg shadow-sm text-center text-gray-500">
                        PLACEHOLDER
                    </div>
                )}
                
                {activeTab === 'Flights' && (
                    <div className="bg-white p-8 rounded-lg shadow-sm text-center text-gray-500">
                        PLACEHOLDER
                    </div>
                )}
                
                {activeTab === 'Reservations' && (
                    <div className="bg-white p-8 rounded-lg shadow-sm text-center text-gray-500">
                        PLACEHOLDER
                    </div>
                )}
            </main>

        </div>
    );
};

export default Dashboard;