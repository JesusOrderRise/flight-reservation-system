import React, { useState, useContext } from 'react';
import { AuthContext } from '../context/AuthContext';
import Airports from './Airports';
import Airplanes from './Airplanes';
import Flights from './Flights';
import RegisterAdmin from '../components/RegisterAdmin';
import Reservations from './Reservations';
import logoImage from '../assets/logo.png';

const Dashboard: React.FC = () => {
    const [activeTab, setActiveTab] = useState<string>('Airports');
    const [isAdminModalOpen, setIsAdminModalOpen] = useState<boolean>(false);
    
    
    const { role, firstName, lastName, logout } = useContext(AuthContext) as {
        role: string;
        firstName: string;
        lastName: string;
        logout: () => void;
    };
    
    const isAdmin = role === 'ADMIN';

    return (
        <div className="min-h-screen bg-gray-50">
            <header className="bg-white shadow-sm px-8 py-0 flex justify-between items-center">
                <div className="flex flex-row items-center mb-1">
                    <img 
                        src={logoImage} 
                        alt="Rise and Fly Logo" 
                        className="w-25 h-25 object-contain" 
                    />
                    <h1 className="text-5xl font-black text-gray-800 tracking-wider">Rise and Fly</h1>
                </div>
                
                <div className="flex items-center gap-6">
                    <div className="text-right">
                        <div className="text-lg font-bold text-gray-800">
                            {firstName} {lastName}
                        </div>
                        <div className="text-sm mt-1 flex justify-end">
                            <span className="font-semibold text-red-600 bg-red-50 px-2 py-0.5 rounded border border-red-100">
                                {role}
                            </span> 
                        </div>
                    </div>
                    
                    <div className="flex flex-col gap-2">
                        <button 
                            onClick={logout}
                            className="bg-red-50 text-red-600 px-4 py-2 rounded font-medium hover:bg-red-100 transition border border-red-100"
                        >
                            Logout
                        </button>
        
                        {/* SADECE ADMİNSE BU BUTON GÖRÜNSÜN */}
                        {isAdmin && (
                            <button 
                                onClick={() => setIsAdminModalOpen(true)}
                                className="bg-indigo-50 text-indigo-700 px-4 py-2 rounded font-medium hover:bg-indigo-100 transition border border-indigo-200 text-sm"
                            >
                                + Register Admin
                            </button>
                        )}
                    </div>
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
                                    ? 'border-red-600 text-red-600'
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
                    <Airports isAdmin={isAdmin}/>
                )}
                
                {activeTab === 'Airplanes' && (
                    <Airplanes isAdmin={isAdmin}/>
                )}
                
                {activeTab === 'Flights' && (
                    <Flights isAdmin={isAdmin}/>
                )}
                
                {activeTab === 'Reservations' && (
                    <Reservations isAdmin={isAdmin}/>
                )}
            </main>

            <RegisterAdmin 
                isOpen={isAdminModalOpen} 
                onClose={() => setIsAdminModalOpen(false)} 
            />
        </div>
    );
};

export default Dashboard;