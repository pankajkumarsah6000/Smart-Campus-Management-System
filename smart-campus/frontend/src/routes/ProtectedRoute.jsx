import { Navigate, Outlet } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import LoadingState from '../components/LoadingState';

export default function ProtectedRoute({ allowedRoles }) {
  const { user, loading, hasRole } = useAuth();

  if (loading) return <div className="min-h-screen flex items-center justify-center"><LoadingState label="Checking session" /></div>;

  if (!user) return <Navigate to="/login" replace />;

  if (allowedRoles && !allowedRoles.some((r) => hasRole(r))) {
    return <Navigate to="/login" replace />;
  }

  return <Outlet />;
}
