import { useEffect, useState } from 'react'
import { adminApi } from '../../api/admin'
import { Users, Landmark, ArrowLeftRight, TrendingUp, ShieldOff, ShieldCheck, Gift, AlertTriangle } from 'lucide-react'

const STAT_CARDS = [
  { key: 'totalUsers', label: 'Total users', icon: Users },
  { key: 'totalBankAccounts', label: 'Bank accounts', icon: Landmark },
  { key: 'totalTransactions', label: 'Transactions', icon: ArrowLeftRight },
  { key: 'successfulTransactions', label: 'Successful', icon: ShieldCheck },
  { key: 'failedTransactions', label: 'Failed', icon: ShieldOff },
]

const TABS = ['overview', 'users', 'transactions', 'fraud', 'complaints', 'cashback']

export default function AdminDashboard() {
  const [stats, setStats] = useState(null)
  const [users, setUsers] = useState([])
  const [transactions, setTransactions] = useState([])
  const [complaints, setComplaints] = useState([])
  const [cashback, setCashback] = useState(null)
  const [fraudAlerts, setFraudAlerts] = useState([])
  const [tab, setTab] = useState('overview')

  useEffect(() => {
    adminApi.dashboard().then(setStats)
    adminApi.users().then(setUsers)
    adminApi.transactions().then(setTransactions)
    adminApi.complaints().then(setComplaints)
    adminApi.cashbackOverview().then(setCashback)
    adminApi.fraudAlerts().then(setFraudAlerts)
  }, [])

  const toggleUser = async (id, enabled) => {
    const updated = await adminApi.setUserEnabled(id, !enabled)
    setUsers((prev) => prev.map((u) => (u.id === id ? updated : u)))
  }

  const updateKyc = async (id, status) => {
    const updated = await adminApi.updateKyc(id, status)
    setUsers((prev) => prev.map((u) => (u.id === id ? updated : u)))
  }

  const resolveComplaint = async (id, status) => {
    const updated = await adminApi.resolveComplaint(id, status, `Marked ${status.toLowerCase()} by admin`)
    setComplaints((prev) => prev.map((c) => (c.id === id ? updated : c)))
  }

  return (
    <div className="space-y-6">
      <div>
        <h1 className="font-display text-xl font-semibold">Admin dashboard</h1>
        <p className="text-sm text-[var(--color-muted)]">Platform monitoring &amp; management</p>
      </div>

      <div className="flex gap-4 border-b border-[var(--color-line)] overflow-x-auto">
        {TABS.map((t) => (
          <button
            key={t}
            onClick={() => setTab(t)}
            className={`pb-2 text-sm font-medium capitalize border-b-2 -mb-px whitespace-nowrap ${
              tab === t ? 'border-[var(--color-primary)] text-[var(--color-primary)]' : 'border-transparent text-[var(--color-muted)]'
            }`}
          >
            {t}
          </button>
        ))}
      </div>

      {tab === 'overview' && stats && (
        <div className="grid sm:grid-cols-3 gap-4">
          {STAT_CARDS.map(({ key, label, icon: Icon }) => (
            <div key={key} className="bg-[var(--color-surface)] border border-[var(--color-line)] rounded-2xl p-5">
              <Icon size={18} className="text-[var(--color-primary)] mb-2" />
              <p className="font-mono-amount text-2xl font-semibold">{stats[key]}</p>
              <p className="text-xs text-[var(--color-muted)]">{label}</p>
            </div>
          ))}
          <div className="bg-[var(--color-surface)] border border-[var(--color-line)] rounded-2xl p-5 sm:col-span-3">
            <TrendingUp size={18} className="text-[var(--color-accent)] mb-2" />
            <p className="font-mono-amount text-2xl font-semibold">
              ₹{Number(stats.totalVolume).toLocaleString('en-IN', { minimumFractionDigits: 2 })}
            </p>
            <p className="text-xs text-[var(--color-muted)]">Total transaction volume</p>
          </div>
        </div>
      )}

      {tab === 'users' && (
        <div className="bg-[var(--color-surface)] border border-[var(--color-line)] rounded-2xl overflow-x-auto">
          <table className="w-full text-sm">
            <thead>
              <tr className="text-left text-xs text-[var(--color-muted)] border-b border-[var(--color-line)]">
                <th className="px-4 py-3 font-medium">Name</th>
                <th className="px-4 py-3 font-medium">Email</th>
                <th className="px-4 py-3 font-medium">Status</th>
                <th className="px-4 py-3 font-medium">KYC</th>
                <th className="px-4 py-3 font-medium"></th>
              </tr>
            </thead>
            <tbody>
              {users.map((u) => (
                <tr key={u.id} className="border-b border-[var(--color-line)] last:border-0">
                  <td className="px-4 py-3">{u.fullName}</td>
                  <td className="px-4 py-3 text-[var(--color-muted)]">{u.email}</td>
                  <td className="px-4 py-3">
                    <span className={u.enabled ? 'text-[var(--color-success)]' : 'text-[var(--color-danger)]'}>
                      {u.enabled ? 'Active' : 'Disabled'}
                    </span>
                  </td>
                  <td className="px-4 py-3">
                    <select
                      value={u.kycStatus}
                      onChange={(e) => updateKyc(u.id, e.target.value)}
                      className="text-xs rounded border border-[var(--color-line)] px-1.5 py-1"
                    >
                      <option value="PENDING">Pending</option>
                      <option value="VERIFIED">Verified</option>
                      <option value="REJECTED">Rejected</option>
                    </select>
                  </td>
                  <td className="px-4 py-3">
                    <button onClick={() => toggleUser(u.id, u.enabled)} className="text-xs font-medium text-[var(--color-primary)]">
                      {u.enabled ? 'Disable' : 'Enable'}
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {tab === 'transactions' && (
        <div className="bg-[var(--color-surface)] border border-[var(--color-line)] rounded-2xl overflow-x-auto">
          <table className="w-full text-sm">
            <thead>
              <tr className="text-left text-xs text-[var(--color-muted)] border-b border-[var(--color-line)]">
                <th className="px-4 py-3 font-medium">Reference</th>
                <th className="px-4 py-3 font-medium">Amount</th>
                <th className="px-4 py-3 font-medium">Status</th>
                <th className="px-4 py-3 font-medium">Date</th>
              </tr>
            </thead>
            <tbody>
              {transactions.map((t) => (
                <tr key={t.id} className="border-b border-[var(--color-line)] last:border-0">
                  <td className="px-4 py-3 font-mono-amount text-xs">{t.referenceId}</td>
                  <td className="px-4 py-3 font-mono-amount">₹{Number(t.amount).toLocaleString('en-IN')}</td>
                  <td className="px-4 py-3">{t.status}</td>
                  <td className="px-4 py-3 text-[var(--color-muted)]">
                    {new Date(t.createdAt).toLocaleDateString('en-IN')}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {tab === 'fraud' && (
        <div className="bg-[var(--color-surface)] border border-[var(--color-line)] rounded-2xl divide-y divide-[var(--color-line)]">
          <div className="px-5 py-3 flex items-center gap-2 text-xs text-[var(--color-muted)]">
            <AlertTriangle size={14} className="text-[var(--color-accent)]" />
            Transactions flagged by rule-based fraud prediction (medium/high risk), most recent first
          </div>
          {fraudAlerts.length === 0 ? (
            <p className="px-5 py-6 text-sm text-[var(--color-muted)]">No flagged transactions. Nothing looks unusual right now.</p>
          ) : (
            fraudAlerts.map((a) => (
              <div key={a.transactionId} className="px-5 py-4">
                <div className="flex items-center justify-between">
                  <p className="text-sm font-medium">
                    {a.userFullName} <span className="text-xs text-[var(--color-muted)]">→ {a.counterpartyVpa}</span>
                  </p>
                  <span
                    className={`text-xs font-semibold px-2 py-0.5 rounded-full ${
                      a.riskLevel === 'HIGH' ? 'bg-[var(--color-danger)]/10 text-[var(--color-danger)]' : 'bg-[var(--color-accent)]/10 text-[var(--color-accent-dark)]'
                    }`}
                  >
                    {a.riskLevel} · {a.riskScore}
                  </span>
                </div>
                <p className="font-mono-amount text-sm mt-1">₹{Number(a.amount).toLocaleString('en-IN')}</p>
                {a.reasons?.length > 0 && (
                  <ul className="mt-1.5 space-y-0.5">
                    {a.reasons.map((r, i) => (
                      <li key={i} className="text-xs text-[var(--color-muted)]">• {r}</li>
                    ))}
                  </ul>
                )}
                <p className="text-[10px] text-[var(--color-muted)] mt-1.5">
                  {new Date(a.timestamp).toLocaleString('en-IN', { dateStyle: 'medium', timeStyle: 'short' })} · Ref {a.referenceId}
                </p>
              </div>
            ))
          )}
        </div>
      )}

      {tab === 'complaints' && (
        <div className="bg-[var(--color-surface)] border border-[var(--color-line)] rounded-2xl divide-y divide-[var(--color-line)]">
          {complaints.length === 0 ? (
            <p className="px-5 py-6 text-sm text-[var(--color-muted)]">No complaints raised.</p>
          ) : (
            complaints.map((c) => (
              <div key={c.id} className="px-5 py-4">
                <div className="flex items-center justify-between">
                  <p className="text-sm font-medium">{c.subject} <span className="text-xs text-[var(--color-muted)]">— {c.userName}</span></p>
                  <span className="text-xs font-medium">{c.status}</span>
                </div>
                <p className="text-xs text-[var(--color-muted)] mt-1">{c.description}</p>
                {c.status === 'OPEN' && (
                  <div className="flex gap-2 mt-2">
                    <button onClick={() => resolveComplaint(c.id, 'IN_REVIEW')} className="text-xs font-medium text-[var(--color-primary)]">Review</button>
                    <button onClick={() => resolveComplaint(c.id, 'RESOLVED')} className="text-xs font-medium text-[var(--color-success)]">Resolve</button>
                    <button onClick={() => resolveComplaint(c.id, 'REJECTED')} className="text-xs font-medium text-[var(--color-danger)]">Reject</button>
                  </div>
                )}
              </div>
            ))
          )}
        </div>
      )}

      {tab === 'cashback' && cashback && (
        <div className="bg-[var(--color-surface)] border border-[var(--color-line)] rounded-2xl p-5">
          <Gift size={18} className="text-[var(--color-accent)] mb-2" />
          <p className="font-mono-amount text-2xl font-semibold">
            ₹{Number(cashback.totalCashbackIssued).toLocaleString('en-IN', { minimumFractionDigits: 2 })}
          </p>
          <p className="text-xs text-[var(--color-muted)]">Total cashback issued across {cashback.totalRewardEntries} entries</p>
        </div>
      )}
    </div>
  )
}
