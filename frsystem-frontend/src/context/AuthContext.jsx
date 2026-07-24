import { createContext, useState } from 'react';

export const AuthContext = createContext();

export const AuthProvider = ({ children }) => {
  const [token, setToken] = useState(localStorage.getItem('token') || null);
 
  const [role, setRole] = useState(localStorage.getItem('role') || null); 
  const [firstName, setFirstName] = useState(localStorage.getItem('firstName') || null);
  const [lastName, setLastName] = useState(localStorage.getItem('lastName') || null);


  const login = (newToken, newRole, newFirstName, newLastName) => {
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

  //everything opened for other components
  return (
    <AuthContext.Provider value={{ token, role, firstName, lastName, login, logout }}>
      {children}
    </AuthContext.Provider>
  );
};