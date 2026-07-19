import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useDispatch } from 'react-redux'
import { billApi } from '../api/bills'
import { pushToast } from '../slices/toastSlice'
import { ArrowLeft, Smartphone, Tv, Zap, Droplet, Flame, Wifi, Car, CreditCard } from 'lucide-react'

const CATEGORIES = [
  { key: 'MOBILE_RECHARGE', label: 'Mobile', icon: Smartphone },
  { key: 'DTH', label: 'DTH', icon: Tv },
  { key: 'ELECTRICITY', label: 'Electricity', icon: Zap },
  { key: 'WATER', label: 'Water', icon: Droplet },
  { key: 'GAS', label: 'Gas', icon: Flame },
  { key: 'BROADBAND', label: 'Broadband', icon: Wifi },
  { key: 'FASTAG', label: 'FASTag', icon: Car },
  { key: 'CREDIT_CARD', label: 'Credit card', icon: CreditCard },
]

export default function Bills() {
  const [category, setCategory] = useState(null)
  const [billers, setBillers] = useState([])
  const [billerId, setBillerId] = useState('')
  const [consumerIdentifier, setConsumerIdentifier] = useState('')
  const [amount, setAmount] = useState('')
  const [pin, setPin] = useState('')
  const [error, setError] = useState('')
  const [submitting, setSubmitting] = useState(false)
  const dispatch = useDispatch()
  const navigate = useNavigate()

  useEffect(() => {
    if (category) billApi.billers(category).then(setBillers)
  }, [category])

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError('')
    setSubmitting(true)
    try {
      await billApi.pay({ billerId, consumerIdentifier, amount: Number(amount), transactionPin: pin })
      dispatch(pushToast('Payment successful', 'success'))
      navigate('/transactions')
    } catch (err) {
      setError(err?.response?.data?.message || 'Payment failed')
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <div className="max-w-sm mx-auto">
      <button onClick={() => (category ? setCategory(null) : navigate(-1))}
        className="flex items-center gap-1 text-sm text-[var(--color-muted)] mb-4">
        <ArrowLeft size={16} /> Back
      </button>
      <h1 className="font-display text-xl font-semibold mb-6">Recharge & bills</h1>

      {!category ? (
        <div className="grid grid-cols-4 gap-3">
          {CATEGORIES.map(({ key, label, icon: Icon }) => (
            <button key={key} onClick={() => setCategory(key)} className="flex flex-col items-center gap-2 bg-[var(--color-surface)] border border-[var(--color-line)] rounded-xl py-4">
              <div className="w-10 h-10 rounded-full bg-[var(--color-primary)]/10 flex items-center justify-center text-[var(--color-primary)]">
                <Icon size={18} />
              </div>
              <span className="text-xs font-medium">{label}</span>
            </button>
          ))}
        </div>
      ) : (
        <form onSubmit={handleSubmit} className="space-y-4">
          <select required value={billerId} onChange={(e) => setBillerId(e.target.value)} className="w-full rounded-lg border border-[var(--color-line)] px-3 py-2.5 text-sm">
            <option value="">Select biller</option>
            {billers.map((b) => <option key={b.id} value={b.id}>{b.name}</option>)}
          </select>
          <input required placeholder="Consumer / mobile / account number" value={consumerIdentifier} onChange={(e) => setConsumerIdentifier(e.target.value)}
            className="w-full rounded-lg border border-[var(--color-line)] px-3 py-2.5 text-sm" />
          <input required type="number" min="1" step="0.01" placeholder="Amount" value={amount} onChange={(e) => setAmount(e.target.value)}
            className="w-full rounded-lg border border-[var(--color-line)] px-3 py-2.5 text-sm font-mono-amount" />
          <input required type="password" inputMode="numeric" placeholder="Transaction PIN" value={pin}
            onChange={(e) => setPin(e.target.value.replace(/\D/g, ''))}
            className="w-full rounded-lg border border-[var(--color-line)] px-3 py-2.5 text-sm text-center tracking-widest" />
          {error && <p className="text-sm text-[var(--color-danger)]">{error}</p>}
          <button type="submit" disabled={submitting} className="w-full rounded-lg bg-[var(--color-primary)] text-white font-medium py-2.5 text-sm disabled:opacity-60">
            {submitting ? 'Paying…' : 'Pay bill'}
          </button>
        </form>
      )}
    </div>
  )
}
