import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import ProtectedRoute from './components/ProtectedRoute';
import Login from './pages/Login';
import Dashboard from './pages/Dashboard'; // Yeni krallığımızı import ettik

function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Routes>
          {/* all permitted */}
          <Route path="/login" element={<Login />} />

          {/* only with token*/}
          <Route element={<ProtectedRoute />}>
            
            {/* dashboard*/}
            <Route path="/dashboard" element={<Dashboard />} />
            
            {/* directly navigate to dashboard from root */}
            <Route path="/" element={<Navigate to="/dashboard" replace />} />
            
          </Route>
        </Routes>
      </BrowserRouter>
    </AuthProvider>
  );
}

export default App;