import React, { createContext, useState, type ReactNode } from 'react';

interface AuthContextType {
  token: string | null;
  role: string | null;
  firstName: string | null;
  lastName: string | null;
  login: (newToken: string, newRole: string, newFirstName: string, newLastName: string) => void;
  logout: () => void;
}

export const AuthContext = createContext<AuthContextType | undefined>(undefined);

interface AuthProviderProps {
  children: ReactNode;
}

export const AuthProvider: React.FC<AuthProviderProps> = ({ children }) => {
  const [token, setToken] = useState<string | null>(localStorage.getItem('token') || null);
  const [role, setRole] = useState<string | null>(localStorage.getItem('role') || null); 
  const [firstName, setFirstName] = useState<string | null>(localStorage.getItem('firstName') || null);
  const [lastName, setLastName] = useState<string | null>(localStorage.getItem('lastName') || null);

  const login = (newToken: string, newRole: string, newFirstName: string, newLastName: string) => {
    localStorage.setItem('token', newToken);
    localStorage.setItem('role', newRole);
    localStorage.setItem('firstName', newFirstName);
    localStorage.setItem('lastName', newLastName);
    setToken(newToken);
    setRole(newRole);
    setFirstName(newFirstName);
    setLastName(newLastName);
  };

  const logout = () => {
    localStorage.removeItem('token');
    localStorage.removeItem('role');
    localStorage.removeItem('firstName');
    localStorage.removeItem('lastName');
    setToken(null);
    setRole(null);
    setFirstName(null);
    setLastName(null);
  };

  return (
    <AuthContext.Provider value={{ token, role, firstName, lastName, login, logout }}>
      {children}
    </AuthContext.Provider>
  );
};