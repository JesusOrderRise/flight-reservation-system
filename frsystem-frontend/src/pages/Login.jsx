import { useState, useContext } from 'react';
import { useNavigate } from 'react-router-dom';
import { AuthContext } from '../context/AuthContext';
import api from '../services/api'; 

const Login = () => {
  const [isLogin, setIsLogin] = useState(true); 

  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [firstName, setFirstName] = useState(''); 
  const [lastName, setLastName] = useState('');   

  const { login } = useContext(AuthContext);
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      if (isLogin) {
        //Login
        const response = await api.post('/auth/login', { email, password });
        const { token, role, firstName: userFName, lastName: userLName } = response.data; 
        
        //Context updates
        login(token, role, userFName, userLName);
        
        navigate('/dashboard');
      } else {
        //Register passenger
        await api.post('/auth/register/passenger', { firstName, lastName, email, password });
        
        alert("Register is successfull! You can login!");
        setIsLogin(true); 
        setFirstName('');
        setLastName('');
        setPassword(''); 
      }
    } catch (error) {
      console.error("Auth Error:", error);
      if (error.response && error.response.data) {
        
        const errorMsg = error.response.data.message || error.response.data;
        alert(errorMsg); 
      } else {
        alert("Unexpected Error!");
      }
    }
  };

  return (
    <div className="flex h-screen items-center justify-center bg-gray-100">
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
        
        <button type="submit" className="w-full bg-blue-500 text-white p-2 rounded hover:bg-blue-600 transition mb-4">
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