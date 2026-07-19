import { NavLink, Outlet, useNavigate } from 'react-router-dom'
import { useDispatch, useSelector } from 'react-redux'
import { Home, ArrowLeftRight, Landmark, User, LogOut, Grid2x2 } from 'lucide-react'
import { logout } from '../slices/authSlice'
import Logo from '../components/Logo'
import ToastHost from '../components/ToastHost'
import ChatWidget from '../components/ChatWidget'

const NAV_ITEMS = [
  { to: '/home', label: 'Home', icon: Home },
  { to: '/transactions', label: 'Activity', icon: ArrowLeftRight },
  { to: '/bank-accounts', label: 'Accounts', icon: Landmark },
  { to: '/more', label: 'More', icon: Grid2x2 },
  { to: '/profile', label: 'Profile', icon: User },
]

export default function AppLayout() {
  const user = useSelector((s) => s.auth.user)
  const dispatch = useDispatch()
  const navigate = useNavigate()

  const handleLogout = () => {
    dispatch(logout())
    navigate('/login')
  }

  return (
    <div className="min-h-screen flex flex-col bg-[var(--color-paper)]">
      <ToastHost />
      <ChatWidget />

      {/* Top bar (desktop + mobile) */}
      <header className="sticky top-0 z-30 bg-[var(--color-ink)] text-white">
        <div className="max-w-5xl mx-auto px-4 h-16 flex items-center justify-between">
          <Logo className="text-white" />
          <div className="hidden sm:flex items-center gap-6">
            {NAV_ITEMS.map(({ to, label, icon: Icon }) => (
              <NavLink
                key={to}
                to={to}
                className={({ isActive }) =>
                  `flex items-center gap-1.5 text-sm font-medium transition-colors ${
                    isActive ? 'text-[var(--color-accent)]' : 'text-white/70 hover:text-white'
                  }`
                }
              >
                <Icon size={16} />
                {label}
              </NavLink>
            ))}
          </div>
          <div className="flex items-center gap-3">
            <span className="hidden sm:inline text-sm text-white/70">{user?.fullName?.split(' ')[0]}</span>
            <button
              onClick={handleLogout}
              className="flex items-center gap-1 text-sm text-white/70 hover:text-white transition-colors"
              title="Log out"
            >
              <LogOut size={16} />
              <span className="hidden sm:inline">Log out</span>
            </button>
          </div>
        </div>
      </header>

      <main className="flex-1 max-w-5xl w-full mx-auto px-4 py-6 pb-24 sm:pb-6">
        <Outlet />
      </main>

      {/* Bottom nav (mobile only) */}
      <nav className="sm:hidden fixed bottom-0 inset-x-0 z-30 bg-white border-t border-[var(--color-line)] flex justify-around py-2">
        {NAV_ITEMS.map(({ to, label, icon: Icon }) => (
          <NavLink
            key={to}
            to={to}
            className={({ isActive }) =>
              `flex flex-col items-center gap-0.5 px-3 py-1 text-xs font-medium ${
                isActive ? 'text-[var(--color-primary)]' : 'text-[var(--color-muted)]'
              }`
            }
          >
            <Icon size={20} />
            {label}
          </NavLink>
        ))}
      </nav>
    </div>
  )
}
