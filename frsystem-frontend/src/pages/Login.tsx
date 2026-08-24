import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext'; 
import api from '../services/Api';
import { toast } from 'react-toastify';
import logoImage from '../assets/logo.png';

const Login: React.FC = () => {
  const [isLogin, setIsLogin] = useState<boolean>(true); 

  const [email, setEmail] = useState<string>('');
  const [password, setPassword] = useState<string>('');
  const [firstName, setFirstName] = useState<string>(''); 
  const [lastName, setLastName] = useState<string>('');   

  
  const { login } = useAuth();
  const navigate = useNavigate();

  const handleSubmit = async (e: any) => {
    e.preventDefault();
    try {
      if (isLogin) {
        // Login
        const response = await api.post('/auth/login', { email, password });
        const { token, role, firstName: userFName, lastName: userLName } = response.data; 
        
        // Context updates
        login(token, role, userFName, userLName);
        
        navigate('/dashboard');
      } else {
        // Register passenger
        await api.post('/auth/register/passenger', { firstName, lastName, email, password });
        
        toast.success("Register is successfull! You can login!");
        setIsLogin(true); 
        setFirstName('');
        setLastName('');
        setPassword(''); 
      }
    } catch (error: any) {
      console.error("Auth Error:", error);
      if (error.response && error.response.data) {
        const errorMsg = error.response.data.message || error.response.data;
        toast.error(errorMsg); 
      } else {
        toast.error("Unexpected Error!");
      }
    }
  };

  return (
    <div className="flex flex-col h-screen items-center justify-center bg-gray-100">
        <div className="flex flex-col items-center mb-6">
            <img 
              src={logoImage} 
              alt="Rise and Fly Logo" 
              className="w-50 h-50 object-contain" 
            />
            <h1 className="text-5xl font-black text-gray-800 tracking-wider">rise and fly</h1>
        </div>
        
        <form onSubmit={handleSubmit} className="bg-white p-8 rounded-lg shadow-md w-96">
            <h2 className="text-2xl font-bold mb-6 text-center">
                {isLogin ? 'Login' : 'Passenger Registiration'}
            </h2>
        
        {!isLogin && (
          <div className="flex gap-2 mb-4">
            <input
              type="text"
              placeholder="Name"
              className="w-1/2 p-2 border rounded focus:outline-none focus:ring-2 focus:ring-blue-500"
              value={firstName}
              onChange={(e) => setFirstName(e.target.value)}
              required={!isLogin}
            />
            <input
              type="text"
              placeholder="Surname"
              className="w-1/2 p-2 border rounded focus:outline-none focus:ring-2 focus:ring-blue-500"
              value={lastName}
              onChange={(e) => setLastName(e.target.value)}
              required={!isLogin}
            />
          </div>
        )}

        <input
          type="email"
          placeholder="Email"
          className="w-full p-2 mb-4 border rounded focus:outline-none focus:ring-2 focus:ring-blue-500"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
          required
        />
        <input
          type="password"
          placeholder="Password"
          className="w-full p-2 mb-6 border rounded focus:outline-none focus:ring-2 focus:ring-blue-500"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          required
        />
        
        <button type="submit" className="w-full bg-red-500 text-white p-2 rounded hover:bg-red-600 transition mb-4">
          {isLogin ? 'Login' : 'Register'}
        </button>

        <div className="text-center text-sm text-gray-600">
          {isLogin ? "Dont have an account? " : "Already have an account? "}
          <button 
            type="button" 
            onClick={() => {
              setIsLogin(!isLogin);
              setFirstName('');
              setLastName('');
            }} 
            className="text-blue-500 hover:underline focus:outline-none"
          >
            {isLogin ? 'Register' : 'Login'}
          </button>
        </div>
      </form>
    </div>
  );
};

export default Login;