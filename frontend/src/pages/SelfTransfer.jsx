import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useDispatch, useSelector } from 'react-redux'
import { fetchBankAccounts } from '../slices/walletSlice'
import { paymentApi } from '../api/payment'
import { pushToast } from '../slices/toastSlice'
import { ArrowLeft, ArrowRight } from 'lucide-react'

export default function SelfTransfer() {
  const dispatch = useDispatch()
  const navigate = useNavigate()
  const { bankAccounts } = useSelector((s) => s.wallet)
  const [from, setFrom] = useState('')
  const [to, setTo] = useState('')
  const [amount, setAmount] = useState('')
  const [pin, setPin] = useState('')
  const [error, setError] = useState('')
  const [submitting, setSubmitting] = useState(false)

  useEffect(() => {
    dispatch(fetchBankAccounts())
  }, [dispatch])

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError('')
    if (from === to) {
      setError('Choose two different accounts')
      return
    }
    setSubmitting(true)
    try {
      await paymentApi.selfTransfer({ fromBankAccountId: from, toBankAccountId: to, amount: Number(amount), transactionPin: pin })
      dispatch(pushToast('Self-transfer complete', 'success'))
      dispatch(fetchBankAccounts())
      navigate('/home')
    } catch (err) {
      setError(err?.response?.data?.message || 'Transfer failed')
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <div className="max-w-sm mx-auto">
      <button onClick={() => navigate(-1)} className="flex items-center gap-1 text-sm text-[var(--color-muted)] mb-4">
        <ArrowLeft size={16} /> Back
      </button>
      <h1 className="font-display text-xl font-semibold mb-6">Self transfer</h1>

      <form onSubmit={handleSubmit} className="space-y-4">
        <div className="flex items-center gap-2">
          <select required value={from} onChange={(e) => setFrom(e.target.value)} className="flex-1 rounded-lg border border-[var(--color-line)] px-3 py-2.5 text-sm">
            <option value="">From account</option>
            {bankAccounts.map((a) => (
              <option key={a.id} value={a.id}>{a.bankName} · {a.maskedAccountNumber}</option>
            ))}
          </select>
          <ArrowRight size={16} className="text-[var(--color-muted)] shrink-0" />
          <select required value={to} onChange={(e) => setTo(e.target.value)} className="flex-1 rounded-lg border border-[var(--color-line)] px-3 py-2.5 text-sm">
            <option value="">To account</option>
            {bankAccounts.map((a) => (
              <option key={a.id} value={a.id}>{a.bankName} · {a.maskedAccountNumber}</option>
            ))}
          </select>
        </div>
        <div>
          <label className="block text-xs font-medium text-[var(--color-muted)] mb-1">Amount (\u20B9)</label>
          <input type="number" min="1" step="0.01" required value={amount} onChange={(e) => setAmount(e.target.value)}
            className="w-full rounded-lg border border-[var(--color-line)] px-3 py-2.5 text-sm font-mono-amount" />
        </div>
        <div>
          <label className="block text-xs font-medium text-[var(--color-muted)] mb-1">Transaction PIN</label>
          <input type="password" inputMode="numeric" pattern="\d{4,6}" maxLength={6} required value={pin}
            onChange={(e) => setPin(e.target.value.replace(/\D/g, ''))}
            className="w-full rounded-lg border border-[var(--color-line)] px-3 py-2.5 text-center tracking-[0.5em]" />
        </div>
        {error && <p className="text-sm text-[var(--color-danger)]">{error}</p>}
        <button type="submit" disabled={submitting} className="w-full rounded-lg bg-[var(--color-primary)] text-white font-medium py-2.5 text-sm disabled:opacity-60">
          {submitting ? 'Transferring…' : 'Transfer'}
        </button>
      </form>
    </div>
  )
}
