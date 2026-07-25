import React, { useState, type ChangeEvent } from 'react';
import api from '../services/Api';
import { toast } from 'react-toastify';

interface RegisterAdminProps {
    isOpen: boolean;
    onClose: () => void;
}

interface AdminFormData {
    firstName: string;
    lastName: string;
    email: string;
    password: string;
}

const RegisterAdmin: React.FC<RegisterAdminProps> = ({ isOpen, onClose }) => {
    const [formData, setFormData] = useState<AdminFormData>({
        firstName: '',
        lastName: '',
        email: '',
        password: ''
    });
    const [loading, setLoading] = useState<boolean>(false);

    if (!isOpen) return null;

    const handleChange = (e: ChangeEvent<HTMLInputElement>) => {
        setFormData({ ...formData, [e.target.name]: e.target.value });
    };

    const handleSubmit = async (e: any) => {
        e.preventDefault();
        setLoading(true);

        try {
            // adminregisterendpoint
            await api.post('/auth/register/admin', formData);
            
            toast.success('Admin registered succesfully!');
            
            setFormData({ firstName: '', lastName: '', email: '', password: '' });
            onClose(); 
        } catch (error: any) {
            toast.error(error.response?.data?.message || error.response?.data || "Unexpected error!");
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50">
            <div className="bg-white p-6 rounded-lg shadow-xl w-full max-w-md">
                <h2 className="text-xl font-bold mb-4 text-gray-800 border-b pb-2">Admin Registeration</h2>
                
                <form onSubmit={handleSubmit} className="space-y-4 mt-4">
                    <div className="flex gap-2">
                        <div className="w-1/2">
                            <label className="block text-sm font-medium text-gray-700">Name</label>
                            <input type="text" name="firstName" value={formData.firstName} onChange={handleChange} required className="mt-1 block w-full border p-2 rounded focus:ring-blue-500 focus:border-blue-500" />
                        </div>
                        <div className="w-1/2">
                            <label className="block text-sm font-medium text-gray-700">Surname</label>
                            <input type="text" name="lastName" value={formData.lastName} onChange={handleChange} required className="mt-1 block w-full border p-2 rounded focus:ring-blue-500 focus:border-blue-500" />
                        </div>
                    </div>
                    
                    <div>
                        <label className="block text-sm font-medium text-gray-700">Email</label>
                        <input type="email" name="email" value={formData.email} onChange={handleChange} required className="mt-1 block w-full border p-2 rounded focus:ring-blue-500 focus:border-blue-500" />
                    </div>
                    
                    <div>
                        <label className="block text-sm font-medium text-gray-700">Password</label>
                        <input type="password" name="password" value={formData.password} onChange={handleChange} required className="mt-1 block w-full border p-2 rounded focus:ring-blue-500 focus:border-blue-500" />
                    </div>
                    
                    <div className="flex justify-end space-x-3 mt-6 pt-4 border-t">
                        <button type="button" onClick={onClose} className="px-4 py-2 text-gray-600 bg-gray-100 rounded hover:bg-gray-200 transition">
                            Cancel
                        </button>
                        <button type="submit" disabled={loading} className="px-4 py-2 bg-indigo-600 text-white rounded hover:bg-indigo-700 transition disabled:bg-indigo-300">
                            {loading ? 'Registering...' : 'Register Admin'}
                        </button>
                    </div>
                </form>
            </div>
        </div>
    );
};

export default RegisterAdmin;