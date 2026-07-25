import React, { useContext } from 'react';
import { Navigate, Outlet } from 'react-router-dom';
import { AuthContext } from '../context/AuthContext';

const ProtectedRoute: React.FC = () => {
  const auth = useContext(AuthContext) as { token: string | null } | undefined;
  const token = auth?.token;

  return token ? <Outlet /> : <Navigate to="/login" replace />;
};

export default ProtectedRoute;