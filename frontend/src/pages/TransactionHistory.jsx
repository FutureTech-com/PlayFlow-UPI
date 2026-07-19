import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { transactionApi } from '../api/transactions'
import AmountText from '../components/AmountText'
import { ArrowUpRight, ArrowDownLeft, Search, AlertTriangle } from 'lucide-react'

export default function TransactionHistory() {
  const [transactions, setTransactions] = useState([])
  const [loading, setLoading] = useState(true)
  const [search, setSearch] = useState('')
  const [status, setStatus] = useState('')
  const [type, setType] = useState('')

  const load = () => {
    setLoading(true)
    transactionApi
      .history({ search: search || undefined, status: status || undefined, type: type || undefined })
      .then(setTransactions)
      .finally(() => setLoading(false))
  }

  useEffect(() => {
    load()
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [])

  const handleFilter = (e) => {
    e.preventDefault()
    load()
  }

  return (
    <div className="max-w-2xl space-y-4">
      <h1 className="font-display text-xl font-semibold">Transaction history</h1>

      <form onSubmit={handleFilter} className="flex flex-wrap gap-2">
        <div className="relative flex-1 min-w-[160px]">
          <Search size={14} className="absolute left-3 top-1/2 -translate-y-1/2 text-[var(--color-muted)]" />
          <input
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            placeholder="Search by name or VPA"
            className="w-full rounded-lg border border-[var(--color-line)] pl-8 pr-3 py-2 text-sm"
          />
        </div>
        <select value={type} onChange={(e) => setType(e.target.value)} className="rounded-lg border border-[var(--color-line)] px-3 py-2 text-sm">
          <option value="">All types</option>
          <option value="SEND">Sent</option>
          <option value="RECEIVE">Received</option>
          <option value="REQUEST">Requests</option>
        </select>
        <select value={status} onChange={(e) => setStatus(e.target.value)} className="rounded-lg border border-[var(--color-line)] px-3 py-2 text-sm">
          <option value="">All statuses</option>
          <option value="SUCCESS">Success</option>
          <option value="FAILED">Failed</option>
          <option value="PENDING">Pending</option>
        </select>
        <button type="submit" className="rounded-lg bg-[var(--color-primary)] text-white text-sm font-medium px-4 py-2">
          Filter
        </button>
      </form>

      <div className="bg-[var(--color-surface)] border border-[var(--color-line)] rounded-2xl">
        {loading ? (
          <p className="px-5 py-6 text-sm text-[var(--color-muted)]">Loading…</p>
        ) : transactions.length === 0 ? (
          <p className="px-5 py-6 text-sm text-[var(--color-muted)]">No transactions match your filters.</p>
        ) : (
          <ul className="divide-y divide-[var(--color-line)]">
            {transactions.map((t) => (
              <li key={t.id}>
                <Link to={`/transactions/${t.id}`} className="flex items-center gap-3 px-5 py-3 hover:bg-[var(--color-paper)] transition-colors">
                  <div
                    className={`w-9 h-9 rounded-full flex items-center justify-center shrink-0 ${
                      t.type === 'CREDIT' ? 'bg-[var(--color-success)]/10 text-[var(--color-success)]' : 'bg-[var(--color-accent)]/10 text-[var(--color-accent)]'
                    }`}
                  >
                    {t.type === 'CREDIT' ? <ArrowDownLeft size={16} /> : <ArrowUpRight size={16} />}
                  </div>
                  <div className="flex-1 min-w-0">
                    <p className="text-sm font-medium truncate">{t.counterpartyName}</p>
                    <p className="text-xs text-[var(--color-muted)] truncate">
                      {new Date(t.timestamp).toLocaleString('en-IN', { dateStyle: 'medium', timeStyle: 'short' })}
                      {t.category && <span className="ml-1.5 text-[var(--color-primary)]">· {t.category}</span>}
                    </p>
                  </div>
                  <div className="text-right">
                    <AmountText value={t.amount} sign={t.type === 'CREDIT' ? '+' : '-'} className="text-sm font-semibold block" />
                    <span
                      className={`text-[10px] font-medium inline-flex items-center gap-1 ${
                        t.status === 'SUCCESS' ? 'text-[var(--color-success)]' : t.status === 'FAILED' ? 'text-[var(--color-danger)]' : 'text-[var(--color-muted)]'
                      }`}
                    >
                      {(t.riskLevel === 'MEDIUM' || t.riskLevel === 'HIGH') && (
                        <AlertTriangle size={10} className="text-[var(--color-accent)]" />
                      )}
                      {t.status}
                    </span>
                  </div>
                </Link>
              </li>
            ))}
          </ul>
        )}
      </div>
    </div>
  )
}
