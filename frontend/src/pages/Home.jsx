import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { useDispatch, useSelector } from 'react-redux'
import { fetchBankAccounts, fetchUpiIds } from '../slices/walletSlice'
import { transactionApi } from '../api/transactions'
import { notificationApi } from '../api/notifications'
import AmountText from '../components/AmountText'
import { Send, QrCode, ScanLine, HandCoins, Landmark, Bell, ArrowUpRight, ArrowDownLeft, ArrowLeftRight, Wallet, Users, Split, CalendarClock, Smartphone, Gift, ShieldCheck, MessageSquareWarning, BarChart3, Store,  Settings as SettingsIcon} from 'lucide-react'
 
const QUICK_ACTIONS = [
  { to: '/send', label: 'Send', icon: Send },
  { to: '/scan', label: 'Scan', icon: ScanLine },
  { to: '/receive', label: 'Receive', icon: QrCode },
  { to: '/request', label: 'Request', icon: HandCoins },
  { to: '/bank-accounts', label: 'Accounts', icon: Landmark },
]

const MORE_FEATURES = [
  { to: '/bills', label: 'Recharge & bills', icon: Smartphone },
  { to: '/wallet', label: 'Wallet', icon: Wallet },
  { to: '/self-transfer', label: 'Transfer', icon: ArrowLeftRight },
  { to: '/split-bills', label: 'Split', icon: Split },
  { to: '/merchant-qr', label: 'Merchant QR', icon: Store },
  { to: '/scheduled-payments', label: 'AutoPay', icon: CalendarClock },
  { to: '/rewards', label: 'Rewards', icon: Gift },
  { to: '/analytics', label: 'Insights', icon: BarChart3 },
  { to: '/favourites', label: 'Favourites', icon: Users },
  { to: '/security', label: 'Security', icon: ShieldCheck },
  { to: '/settings', label: 'Settings', icon: SettingsIcon },
  { to: '/complaints', label: 'Help', icon: MessageSquareWarning },
]

export default function Home() {
  const dispatch = useDispatch()
  const user = useSelector((s) => s.auth.user)
  const { bankAccounts, upiIds } = useSelector((s) => s.wallet)
  const [recent, setRecent] = useState([])
  const [unread, setUnread] = useState(0)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    dispatch(fetchBankAccounts())
    dispatch(fetchUpiIds())
    Promise.all([transactionApi.history({}), notificationApi.unreadCount()])
      .then(([txns, count]) => {
        setRecent(txns.slice(0, 5))
        setUnread(count.unread)
      })
      .finally(() => setLoading(false))
  }, [dispatch])

  const primaryAccount = bankAccounts.find((a) => a.primary) || bankAccounts[0]
  const primaryUpi = upiIds[0]

  const getGreeting = () => {
    const hour = new Date().getHours();

    if (hour < 12) return "Good Morning";
    if (hour < 17) return "Good Afternoon";
    return "Good Evening";
  };

  return (
    <div className="space-y-6">
      {/* Greeting + balance card */}
      <div className="rounded-2xl bg-gradient-to-br from-[var(--color-ink)] to-[#16324A] text-white p-6 shadow-sm">
        <div className="flex items-start justify-between">
          <div>
            <p className="text-white/60 text-sm">
              {getGreeting()}, {user?.fullName?.split(" ")[0]} 👋
            </p>
          </div>
          <Link to="/notifications" className="relative">
            <Bell size={20} className="text-white/70" />
            {unread > 0 && (
              <span className="absolute -top-1 -right-1 w-4 h-4 rounded-full bg-[var(--color-accent)] text-[10px] flex items-center justify-center">
                {unread}
              </span>
            )}
          </Link>
        </div>

        <div className="mt-6">
          <p className="text-white/60 text-xs uppercase tracking-wide">
            {primaryAccount ? `${primaryAccount.bankName} · Primary balance` : 'No linked account'}
          </p>
          {primaryAccount ? (
            <p className="font-mono-amount text-3xl font-semibold mt-1">
              ₹{Number(primaryAccount.balance).toLocaleString('en-IN', { minimumFractionDigits: 2 })}
            </p>
          ) : (
            <Link to="/bank-accounts" className="text-[var(--color-accent)] text-sm font-medium">
              Link a bank account →
            </Link>
          )}
          {primaryUpi && <p className="text-white/50 text-xs mt-1">{primaryUpi.vpa}</p>}
        </div>
      </div>

      {/* Quick actions */}
      <div className="grid grid-cols-5 gap-2 sm:gap-3">
        {QUICK_ACTIONS.map(({ to, label, icon: Icon }) => (
          <Link
            key={to}
            to={to}
            className="flex flex-col items-center gap-2 bg-[var(--color-surface)] border border-[var(--color-line)] rounded-xl py-4 hover:border-[var(--color-primary)]/40 hover:shadow-sm transition-all"
          >
            <div className="w-10 h-10 rounded-full bg-[var(--color-primary)]/10 flex items-center justify-center text-[var(--color-primary)]">
              <Icon size={18} />
            </div>
            <span className="text-xs font-medium text-[var(--color-ink)]">{label}</span>
          </Link>
        ))}
      </div>

      
      {/* Explore More */}
        <div className="bg-[var(--color-surface)] border border-[var(--color-line)] rounded-2xl p-5">
          <div className="flex items-center justify-between mb-4">
            <h2 className="font-display font-semibold text-sm">
              Explore Services
            </h2>
              
            <Link
              to="/more"
              className="text-xs font-medium text-[var(--color-primary)]"
            >
              View All
            </Link>
          </div>
              
          <div className="grid grid-cols-3 sm:grid-cols-4 gap-4">
            {MORE_FEATURES.map(({ to, label, icon: Icon }) => (
              <Link
                key={to}
                to={to}
                className="flex flex-col items-center gap-2 rounded-xl border border-[var(--color-line)] p-4 hover:border-[var(--color-primary)]/40 hover:shadow-sm transition"
              >
                <div className="w-10 h-10 rounded-full bg-[var(--color-primary)]/10 flex items-center justify-center text-[var(--color-primary)]">
                  <Icon size={18} />
                </div>
            
                <span className="text-xs text-center font-medium">
                  {label}
                </span>
              </Link>
            ))}
          </div>
        </div>

        {/* Recent activity */}
      <div className="bg-[var(--color-surface)] border border-[var(--color-line)] rounded-2xl">
        <div className="flex items-center justify-between px-5 py-4 border-b border-[var(--color-line)]">
          <h2 className="font-display font-semibold text-sm">Recent activity</h2>
          <Link to="/transactions" className="text-xs text-[var(--color-primary)] font-medium">
            View all
          </Link>
        </div>
        {loading ? (
          <p className="px-5 py-6 text-sm text-[var(--color-muted)]">Loading…</p>
        ) : recent.length === 0 ? (
          <p className="px-5 py-6 text-sm text-[var(--color-muted)]">No transactions yet. Send your first payment!</p>
        ) : (
          <ul className="divide-y divide-[var(--color-line)]">
            {recent.map((t) => (
              <li key={t.id} className="flex items-center gap-3 px-5 py-3">
                <div
                  className={`w-9 h-9 rounded-full flex items-center justify-center shrink-0 ${
                    t.type === 'CREDIT' ? 'bg-[var(--color-success)]/10 text-[var(--color-success)]' : 'bg-[var(--color-accent)]/10 text-[var(--color-accent)]'
                  }`}
                >
                  {t.type === 'CREDIT' ? <ArrowDownLeft size={16} /> : <ArrowUpRight size={16} />}
                </div>
                <div className="flex-1 min-w-0">
                  <p className="text-sm font-medium text-[var(--color-ink)] truncate">{t.counterpartyName}</p>
                  <p className="text-xs text-[var(--color-muted)] truncate">{t.counterpartyVpa}</p>
                </div>
                <AmountText value={t.amount} sign={t.type === 'CREDIT' ? '+' : '-'} className="text-sm font-semibold" />
              </li>
            ))}
          </ul>
        )}
      </div>

    </div>
  )
}
