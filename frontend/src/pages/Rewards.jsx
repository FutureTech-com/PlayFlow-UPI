import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { rewardApi } from '../api/rewards'
import { ArrowLeft, Gift } from 'lucide-react'

export default function Rewards() {
  const [summary, setSummary] = useState(null)
  const navigate = useNavigate()

  useEffect(() => { rewardApi.summary().then(setSummary) }, [])

  return (
    <div className="max-w-sm mx-auto">
      <button onClick={() => navigate(-1)} className="flex items-center gap-1 text-sm text-[var(--color-muted)] mb-4">
        <ArrowLeft size={16} /> Back
      </button>
      <h1 className="font-display text-xl font-semibold mb-6">Rewards & cashback</h1>

      {summary && (
        <>
          <div className="rounded-2xl bg-gradient-to-br from-[var(--color-ink)] to-[#16324A] text-white p-6 text-center">
            <Gift size={28} className="mx-auto text-[var(--color-accent)]" />
            <p className="font-mono-amount text-3xl font-semibold mt-2">
              ₹{Number(summary.totalCashback).toLocaleString('en-IN', { minimumFractionDigits: 2 })}
            </p>
            <p className="text-white/60 text-xs mt-1">{summary.totalPoints} points earned</p>
          </div>

          <div className="bg-[var(--color-surface)] border border-[var(--color-line)] rounded-2xl mt-4">
            {summary.history.length === 0 ? (
              <p className="px-5 py-8 text-center text-sm text-[var(--color-muted)]">No rewards yet — cashback is earned automatically on qualifying payments.</p>
            ) : (
              <ul className="divide-y divide-[var(--color-line)]">
                {summary.history.map((r) => (
                  <li key={r.id} className="flex items-center justify-between px-4 py-3">
                    <div>
                      <p className="text-sm">{r.reason}</p>
                      <p className="text-xs text-[var(--color-muted)]">{new Date(r.createdAt).toLocaleDateString('en-IN')}</p>
                    </div>
                    <p className="font-mono-amount text-sm font-semibold text-[var(--color-success)]">+₹{Number(r.cashbackAmount).toFixed(2)}</p>
                  </li>
                ))}
              </ul>
            )}
          </div>
        </>
      )}
    </div>
  )
}
