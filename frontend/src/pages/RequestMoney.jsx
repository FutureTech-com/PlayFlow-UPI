import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useDispatch } from 'react-redux'
import { paymentApi } from '../api/payment'
import { upiApi } from '../api/upi'
import { pushToast } from '../slices/toastSlice'
import { ArrowLeft } from 'lucide-react'

export default function RequestMoney() {
  const [vpa, setVpa] = useState('')
  const [amount, setAmount] = useState('')
  const [note, setNote] = useState('')
  const [error, setError] = useState('')
  const [submitting, setSubmitting] = useState(false)
  const dispatch = useDispatch()
  const navigate = useNavigate()

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError('')
    setSubmitting(true)
    try {
      await upiApi.details(vpa.trim()) // validate VPA exists first
      await paymentApi.request({ payerVpa: vpa.trim(), amount: Number(amount), note })
      dispatch(pushToast('Payment request sent', 'success'))
      navigate('/transactions')
    } catch (err) {
      setError(err?.response?.data?.message || 'Could not send request')
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <div className="max-w-sm mx-auto">
      <button onClick={() => navigate(-1)} className="flex items-center gap-1 text-sm text-[var(--color-muted)] mb-4">
        <ArrowLeft size={16} /> Back
      </button>
      <h1 className="font-display text-xl font-semibold mb-6">Request money</h1>

      <form onSubmit={handleSubmit} className="space-y-4">
        <div>
          <label className="block text-xs font-medium text-[var(--color-muted)] mb-1">From UPI ID</label>
          <input
            required
            value={vpa}
            onChange={(e) => setVpa(e.target.value)}
            placeholder="name@payflow"
            className="w-full rounded-lg border border-[var(--color-line)] px-3 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-[var(--color-primary)]/30 focus:border-[var(--color-primary)]"
          />
        </div>
        <div>
          <label className="block text-xs font-medium text-[var(--color-muted)] mb-1">Amount (₹)</label>
          <input
            type="number"
            min="1"
            step="0.01"
            required
            value={amount}
            onChange={(e) => setAmount(e.target.value)}
            placeholder="0.00"
            className="w-full rounded-lg border border-[var(--color-line)] px-3 py-2.5 text-sm font-mono-amount focus:outline-none focus:ring-2 focus:ring-[var(--color-primary)]/30 focus:border-[var(--color-primary)]"
          />
        </div>
        <div>
          <label className="block text-xs font-medium text-[var(--color-muted)] mb-1">Note (optional)</label>
          <input
            value={note}
            onChange={(e) => setNote(e.target.value)}
            placeholder="Splitting the bill…"
            className="w-full rounded-lg border border-[var(--color-line)] px-3 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-[var(--color-primary)]/30 focus:border-[var(--color-primary)]"
          />
        </div>
        {error && <p className="text-sm text-[var(--color-danger)]">{error}</p>}
        <button
          type="submit"
          disabled={submitting}
          className="w-full rounded-lg bg-[var(--color-primary)] text-white font-medium py-2.5 text-sm disabled:opacity-60"
        >
          {submitting ? 'Sending…' : 'Send request'}
        </button>
      </form>
    </div>
  )
}
