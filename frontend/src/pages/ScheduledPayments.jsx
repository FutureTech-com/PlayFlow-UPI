import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useDispatch } from 'react-redux'
import { scheduledPaymentApi } from '../api/scheduledPayments'
import { pushToast } from '../slices/toastSlice'
import { ArrowLeft, Plus, X, Trash2, RefreshCw } from 'lucide-react'

export default function ScheduledPayments() {
  const [list, setList] = useState([])
  const [showForm, setShowForm] = useState(false)
  const [form, setForm] = useState({ receiverVpa: '', amount: '', note: '', recurrence: 'MONTHLY', startAt: '' })
  const [error, setError] = useState('')
  const dispatch = useDispatch()
  const navigate = useNavigate()

  const load = () => scheduledPaymentApi.list().then(setList)
  useEffect(() => { load() }, [])

  const handleChange = (e) => setForm((f) => ({ ...f, [e.target.name]: e.target.value }))

  const handleCreate = async (e) => {
    e.preventDefault()
    setError('')
    try {
      await scheduledPaymentApi.create({ ...form, amount: Number(form.amount), startAt: new Date(form.startAt).toISOString() })
      setShowForm(false)
      setForm({ receiverVpa: '', amount: '', note: '', recurrence: 'MONTHLY', startAt: '' })
      load()
      dispatch(pushToast('Scheduled payment created', 'success'))
    } catch (err) {
      setError(err?.response?.data?.message || 'Could not schedule payment')
    }
  }

  const cancel = (id) => scheduledPaymentApi.cancel(id).then(() => { load(); dispatch(pushToast('Cancelled', 'success')) })

  return (
    <div className="max-w-sm mx-auto">
      <button onClick={() => navigate(-1)} className="flex items-center gap-1 text-sm text-[var(--color-muted)] mb-4">
        <ArrowLeft size={16} /> Back
      </button>
      <div className="flex items-center justify-between mb-4">
        <h1 className="font-display text-xl font-semibold">Scheduled & AutoPay</h1>
        <button onClick={() => setShowForm((v) => !v)} className="text-sm font-medium text-[var(--color-primary)] flex items-center gap-1">
          {showForm ? <X size={16} /> : <Plus size={16} />} {showForm ? 'Cancel' : 'New'}
        </button>
      </div>

      {showForm && (
        <form onSubmit={handleCreate} className="bg-[var(--color-surface)] border border-[var(--color-line)] rounded-2xl p-4 space-y-3 mb-4">
          <input required name="receiverVpa" placeholder="Recipient UPI ID" value={form.receiverVpa} onChange={handleChange}
            className="w-full rounded-lg border border-[var(--color-line)] px-3 py-2 text-sm" />
          <input required type="number" min="1" step="0.01" name="amount" placeholder="Amount" value={form.amount} onChange={handleChange}
            className="w-full rounded-lg border border-[var(--color-line)] px-3 py-2 text-sm font-mono-amount" />
          <input name="note" placeholder="Note (optional)" value={form.note} onChange={handleChange}
            className="w-full rounded-lg border border-[var(--color-line)] px-3 py-2 text-sm" />
          <select name="recurrence" value={form.recurrence} onChange={handleChange} className="w-full rounded-lg border border-[var(--color-line)] px-3 py-2 text-sm">
            <option value="ONCE">Once</option>
            <option value="DAILY">Daily</option>
            <option value="WEEKLY">Weekly</option>
            <option value="MONTHLY">Monthly (AutoPay)</option>
          </select>
          <input required type="datetime-local" name="startAt" value={form.startAt} onChange={handleChange}
            className="w-full rounded-lg border border-[var(--color-line)] px-3 py-2 text-sm" />
          {error && <p className="text-xs text-[var(--color-danger)]">{error}</p>}
          <button type="submit" className="w-full rounded-lg bg-[var(--color-primary)] text-white text-sm font-medium py-2">Schedule</button>
          <p className="text-[10px] text-[var(--color-muted)]">A background job runs these automatically once due — you'll get a notification each time.</p>
        </form>
      )}

      <div className="space-y-3">
        {list.length === 0 && <p className="text-sm text-[var(--color-muted)] text-center py-6">No scheduled payments.</p>}
        {list.map((sp) => (
          <div key={sp.id} className="bg-[var(--color-surface)] border border-[var(--color-line)] rounded-xl p-4">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm font-medium">{sp.receiverVpa}</p>
                <p className="text-xs text-[var(--color-muted)] flex items-center gap-1">
                  <RefreshCw size={11} /> {sp.recurrence} · next {new Date(sp.nextRunAt).toLocaleDateString('en-IN')}
                </p>
              </div>
              <p className="font-mono-amount text-sm font-semibold">₹{Number(sp.amount).toLocaleString('en-IN')}</p>
            </div>
            {sp.lastFailureReason && <p className="text-[10px] text-[var(--color-danger)] mt-1">{sp.lastFailureReason}</p>}
            {sp.active && (
              <button onClick={() => cancel(sp.id)} className="flex items-center gap-1 text-xs text-[var(--color-danger)] mt-2">
                <Trash2 size={12} /> Cancel
              </button>
            )}
          </div>
        ))}
      </div>
    </div>
  )
}
