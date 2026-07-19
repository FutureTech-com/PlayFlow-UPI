import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useDispatch } from 'react-redux'
import { walletApi } from '../api/wallet'
import { pushToast } from '../slices/toastSlice'
import { ArrowLeft, Wallet as WalletIcon, Plus, Landmark } from 'lucide-react'

export default function WalletPage() {
  const [wallet, setWallet] = useState(null)
  const [amount, setAmount] = useState('')
  const [mode, setMode] = useState(null) // 'add' | 'withdraw'
  const [submitting, setSubmitting] = useState(false)
  const [error, setError] = useState('')
  const dispatch = useDispatch()
  const navigate = useNavigate()

  const load = () => walletApi.get().then(setWallet)

  useEffect(() => { load() }, [])

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError('')
    setSubmitting(true)
    try {
      if (mode === 'add') await walletApi.addMoney({ amount: Number(amount) })
      else await walletApi.toBank({ amount: Number(amount) })
      dispatch(pushToast(mode === 'add' ? 'Money added to wallet' : 'Transferred to bank', 'success'))
      setAmount('')
      setMode(null)
      load()
    } catch (err) {
      setError(err?.response?.data?.message || 'Action failed')
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <div className="max-w-sm mx-auto">
      <button onClick={() => navigate(-1)} className="flex items-center gap-1 text-sm text-[var(--color-muted)] mb-4">
        <ArrowLeft size={16} /> Back
      </button>

      <div className="rounded-2xl bg-gradient-to-br from-[var(--color-ink)] to-[#16324A] text-white p-6 text-center">
        <WalletIcon size={28} className="mx-auto text-[var(--color-accent)]" />
        <p className="text-white/60 text-xs uppercase tracking-wide mt-2">PayFlow Wallet balance</p>
        <p className="font-mono-amount text-3xl font-semibold mt-1">
          {wallet ? `₹${Number(wallet.balance).toLocaleString('en-IN', { minimumFractionDigits: 2 })}` : '…'}
        </p>
      </div>

      <div className="flex gap-3 mt-4">
        <button onClick={() => setMode('add')} className={`flex-1 flex items-center justify-center gap-2 rounded-lg border py-2.5 text-sm font-medium ${mode === 'add' ? 'border-[var(--color-primary)] text-[var(--color-primary)]' : 'border-[var(--color-line)]'}`}>
          <Plus size={16} /> Add money
        </button>
        <button onClick={() => setMode('withdraw')} className={`flex-1 flex items-center justify-center gap-2 rounded-lg border py-2.5 text-sm font-medium ${mode === 'withdraw' ? 'border-[var(--color-primary)] text-[var(--color-primary)]' : 'border-[var(--color-line)]'}`}>
          <Landmark size={16} /> To bank
        </button>
      </div>

      {mode && (
        <form onSubmit={handleSubmit} className="mt-4 space-y-3 bg-[var(--color-surface)] border border-[var(--color-line)] rounded-2xl p-5">
          <label className="block text-xs font-medium text-[var(--color-muted)]">Amount (\u20B9)</label>
          <input autoFocus type="number" min="1" step="0.01" required value={amount} onChange={(e) => setAmount(e.target.value)}
            className="w-full rounded-lg border border-[var(--color-line)] px-3 py-2 text-sm font-mono-amount" />
          {error && <p className="text-sm text-[var(--color-danger)]">{error}</p>}
          <button type="submit" disabled={submitting} className="w-full rounded-lg bg-[var(--color-primary)] text-white text-sm font-medium py-2.5 disabled:opacity-60">
            {submitting ? 'Processing…' : mode === 'add' ? 'Add money' : 'Transfer to bank'}
          </button>
        </form>
      )}
    </div>
  )
}
