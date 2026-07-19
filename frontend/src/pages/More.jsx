import { Link } from 'react-router-dom'
import {
  Wallet, Users, Split, CalendarClock, Smartphone, Gift, ShieldCheck,
  MessageSquareWarning, BarChart3, ArrowLeftRight, Store, ScanLine,
  Bell, Settings as SettingsIcon, Landmark, QrCode, HandCoins,
} from 'lucide-react'

const SECTIONS = [
  {
    title: 'Pay & transfer',
    items: [
      { to: '/scan', label: 'Scan & pay', desc: 'Scan a QR code to pay instantly', icon: ScanLine },
      { to: '/self-transfer', label: 'Self transfer', desc: 'Move money between your own accounts', icon: ArrowLeftRight },
      { to: '/favourites', label: 'Favourites', desc: 'Pay saved contacts in one tap', icon: Users },
      { to: '/request', label: 'Request money', desc: 'Ask someone to pay you', icon: HandCoins },
      { to: '/merchant-qr', label: 'Merchant QR', desc: 'Generate a charge-this-amount code', icon: Store },
      { to: '/split-bills', label: 'Split a bill', desc: 'Split expenses with friends', icon: Split },
    ],
  },
  {
    title: 'Automate',
    items: [
      { to: '/bills', label: 'Recharge & bills', desc: 'Mobile, DTH, electricity, water, gas & more', icon: Smartphone },
      { to: '/scheduled-payments', label: 'Scheduled & AutoPay', desc: 'Recurring and future-dated payments', icon: CalendarClock },
      
    ],
  },
  {
    title: 'Money',
    items: [
      { to: '/wallet', label: 'PayFlow Wallet', desc: 'Add money, pay instantly, top up from bank', icon: Wallet },
      { to: '/bank-accounts', label: 'Bank accounts', desc: 'Linked accounts & balances', icon: Landmark },
      { to: '/rewards', label: 'Rewards & cashback', desc: 'Track what you\u2019ve earned', icon: Gift },
      { to: '/analytics', label: 'Spending insights', desc: 'Monthly analytics & budgets', icon: BarChart3 },
    ],
  },
  {
    title: 'Account',
    items: [
      { to: '/notifications', label: 'Notifications', desc: 'Payment & security alerts', icon: Bell },
      { to: '/security', label: 'Security & devices', desc: 'Trusted devices, OTP verification', icon: ShieldCheck },
      { to: '/settings', label: 'Settings', desc: 'Password, transaction PIN', icon: SettingsIcon },
      { to: '/complaints', label: 'Help & complaints', desc: 'Raise or track an issue', icon: MessageSquareWarning },
    ],
  },
]

export default function More() {
  return (
    <div className="max-w-2xl space-y-8">
      <h1 className="font-display text-xl font-semibold">More</h1>

      {SECTIONS.map((section) => (
        <div key={section.title} className="space-y-3">
          <h2 className="text-xs font-semibold uppercase tracking-wide text-[var(--color-muted)]">{section.title}</h2>
          <div className="grid sm:grid-cols-2 gap-3">
            {section.items.map(({ to, label, desc, icon: Icon }) => (
              <Link
                key={to}
                to={to}
                className="flex items-start gap-3 bg-[var(--color-surface)] border border-[var(--color-line)] rounded-xl p-4 hover:border-[var(--color-primary)]/40 hover:shadow-sm transition-all"
              >
                <div className="w-10 h-10 shrink-0 rounded-full bg-[var(--color-primary)]/10 flex items-center justify-center text-[var(--color-primary)]">
                  <Icon size={18} />
                </div>
                <div>
                  <p className="text-sm font-medium">{label}</p>
                  <p className="text-xs text-[var(--color-muted)]">{desc}</p>
                </div>
              </Link>
            ))}
          </div>
        </div>
      ))}
    </div>
  )
}
