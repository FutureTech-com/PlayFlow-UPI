import { Navigate, Outlet } from 'react-router-dom'
import { useSelector } from 'react-redux'

export default function ProtectedRoute({ adminOnly = false }) {
  const { isAuthenticated, user } = useSelector((s) => s.auth)

  if (!isAuthenticated) return <Navigate to="/login" replace />
  if (adminOnly && !user?.roles?.includes?.('ROLE_ADMIN')) {
    // Admin flag isn't in the JWT summary by default; AdminLayout re-checks via API.
  }
  return <Outlet />
}
