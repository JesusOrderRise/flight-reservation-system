import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import ProtectedRoute from './components/ProtectedRoute';
import Login from './pages/Login';
import Dashboard from './pages/Dashboard'; 
import { ToastContainer } from 'react-toastify'; 
import 'react-toastify/dist/ReactToastify.css';

const App: React.FC = () => {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Routes>
          {/* all permitted */}
          <Route path="/login" element={<Login />} />

          {/* only with token */}
          <Route element={<ProtectedRoute />}>
            
            {/* dashboard */}
            <Route path="/dashboard" element={<Dashboard />} />
            
            {/* directly navigate to dashboard from root */}
            <Route path="/" element={<Navigate to="/dashboard" replace />} />
            
            
            {/* navigates to dashboard if tried to access non existing path. 
            If has no token, protected route automatically will route to login anyways.*/}
            <Route path="*" element={<Navigate to="/dashboard" replace />} />

          </Route>
        </Routes>
      </BrowserRouter>
      <ToastContainer 
        position="bottom-right" 
        autoClose={3000} 
        theme="colored" 
      />
    </AuthProvider>
  );
}

export default App;