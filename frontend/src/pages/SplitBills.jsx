import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useDispatch } from 'react-redux'
import { splitBillApi } from '../api/splitBills'
import { pushToast } from '../slices/toastSlice'
import { ArrowLeft, Plus, Trash2, X } from 'lucide-react'

export default function SplitBills() {
  const [tab, setTab] = useState('organized')
  const [organized, setOrganized] = useState([])
  const [myShares, setMyShares] = useState([])
  const [showForm, setShowForm] = useState(false)
  const [title, setTitle] = useState('')
  const [totalAmount, setTotalAmount] = useState('')
  const [participants, setParticipants] = useState([{ vpa: '', amount: '' }])
  const [error, setError] = useState('')
  const [settlingId, setSettlingId] = useState(null)
  const [pin, setPin] = useState('')
  const dispatch = useDispatch()
  const navigate = useNavigate()

  const load = () => {
    splitBillApi.organized().then(setOrganized)
    splitBillApi.myShares().then(setMyShares)
  }
  useEffect(() => { load() }, [])

  const updateParticipant = (i, field, value) => {
    setParticipants((prev) => prev.map((p, idx) => (idx === i ? { ...p, [field]: value } : p)))
  }
  const addParticipantRow = () => setParticipants((prev) => [...prev, { vpa: '', amount: '' }])
  const removeParticipantRow = (i) => setParticipants((prev) => prev.filter((_, idx) => idx !== i))

  const handleCreate = async (e) => {
    e.preventDefault()
    setError('')
    try {
      await splitBillApi.create({
        title,
        totalAmount: Number(totalAmount),
        participants: participants.map((p) => ({ vpa: p.vpa, amount: Number(p.amount) })),
      })
      setShowForm(false)
      setTitle('')
      setTotalAmount('')
      setParticipants([{ vpa: '', amount: '' }])
      load()
      dispatch(pushToast('Split bill created', 'success'))
    } catch (err) {
      setError(err?.response?.data?.message || 'Could not create split bill')
    }
  }

  const handleSettle = async (shareId) => {
    try {
      await splitBillApi.settle(shareId, pin)
      setSettlingId(null)
      setPin('')
      load()
      dispatch(pushToast('Share settled', 'success'))
    } catch (err) {
      dispatch(pushToast(err?.response?.data?.message || 'Could not settle share', 'error'))
    }
  }

  return (
    <div className="max-w-sm mx-auto">
      <button onClick={() => navigate(-1)} className="flex items-center gap-1 text-sm text-[var(--color-muted)] mb-4">
        <ArrowLeft size={16} /> Back
      </button>
      <div className="flex items-center justify-between mb-4">
        <h1 className="font-display text-xl font-semibold">Split a bill</h1>
        <button onClick={() => setShowForm((v) => !v)} className="text-sm font-medium text-[var(--color-primary)] flex items-center gap-1">
          {showForm ? <X size={16} /> : <Plus size={16} />} {showForm ? 'Cancel' : 'New'}
        </button>
      </div>

      {showForm && (
        <form onSubmit={handleCreate} className="bg-[var(--color-surface)] border border-[var(--color-line)] rounded-2xl p-4 space-y-3 mb-4">
          <input required placeholder="Title (e.g. Dinner)" value={title} onChange={(e) => setTitle(e.target.value)}
            className="w-full rounded-lg border border-[var(--color-line)] px-3 py-2 text-sm" />
          <input required type="number" min="1" step="0.01" placeholder="Total amount" value={totalAmount} onChange={(e) => setTotalAmount(e.target.value)}
            className="w-full rounded-lg border border-[var(--color-line)] px-3 py-2 text-sm font-mono-amount" />
          <div className="space-y-2">
            {participants.map((p, i) => (
              <div key={i} className="flex gap-2">
                <input required placeholder="name@payflow" value={p.vpa} onChange={(e) => updateParticipant(i, 'vpa', e.target.value)}
                  className="flex-1 rounded-lg border border-[var(--color-line)] px-2 py-1.5 text-xs" />
                <input required type="number" min="0.01" step="0.01" placeholder="Amount" value={p.amount} onChange={(e) => updateParticipant(i, 'amount', e.target.value)}
                  className="w-24 rounded-lg border border-[var(--color-line)] px-2 py-1.5 text-xs font-mono-amount" />
                {participants.length > 1 && (
                  <button type="button" onClick={() => removeParticipantRow(i)} className="text-[var(--color-muted)]"><Trash2 size={14} /></button>
                )}
              </div>
            ))}
          </div>
          <button type="button" onClick={addParticipantRow} className="text-xs font-medium text-[var(--color-primary)]">+ Add participant</button>
          {error && <p className="text-xs text-[var(--color-danger)]">{error}</p>}
          <button type="submit" className="w-full rounded-lg bg-[var(--color-primary)] text-white text-sm font-medium py-2">Create split</button>
        </form>
      )}

      <div className="flex gap-4 border-b border-[var(--color-line)] mb-4">
        {['organized', 'shares'].map((t) => (
          <button key={t} onClick={() => setTab(t)}
            className={`pb-2 text-sm font-medium capitalize border-b-2 -mb-px ${tab === t ? 'border-[var(--color-primary)] text-[var(--color-primary)]' : 'border-transparent text-[var(--color-muted)]'}`}>
            {t === 'organized' ? 'Created by me' : 'My shares'}
          </button>
        ))}
      </div>

      {tab === 'organized' ? (
        <div className="space-y-3">
          {organized.length === 0 && <p className="text-sm text-[var(--color-muted)] text-center py-6">No split bills yet.</p>}
          {organized.map((b) => (
            <div key={b.id} className="bg-[var(--color-surface)] border border-[var(--color-line)] rounded-xl p-4">
              <div className="flex items-center justify-between">
                <p className="text-sm font-medium">{b.title}</p>
                <p className="font-mono-amount text-sm font-semibold">₹{Number(b.totalAmount).toLocaleString('en-IN')}</p>
              </div>
              <ul className="mt-2 space-y-1">
                {b.shares.map((s) => (
                  <li key={s.id} className="flex items-center justify-between text-xs">
                    <span className="text-[var(--color-muted)]">{s.participantName}</span>
                    <span className={s.settled ? 'text-[var(--color-success)]' : 'text-[var(--color-muted)]'}>
                      ₹{Number(s.amountOwed).toLocaleString('en-IN')} {s.settled ? '· paid' : '· pending'}
                    </span>
                  </li>
                ))}
              </ul>
            </div>
          ))}
        </div>
      ) : (
        <div className="space-y-3">
          {myShares.length === 0 && <p className="text-sm text-[var(--color-muted)] text-center py-6">You have no shares to pay.</p>}
          {myShares.map((s) => (
            <div key={s.shareId} className="bg-[var(--color-surface)] border border-[var(--color-line)] rounded-xl p-4">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-sm font-medium">{s.billTitle}</p>
                  <p className="text-xs text-[var(--color-muted)]">Organized by {s.organizerName}</p>
                </div>
                <p className="font-mono-amount text-sm font-semibold">₹{Number(s.amountOwed).toLocaleString('en-IN')}</p>
              </div>
              {s.settled ? (
                <p className="text-xs text-[var(--color-success)] mt-2">Paid</p>
              ) : settlingId === s.shareId ? (
                <div className="flex gap-2 mt-2">
                  <input autoFocus type="password" inputMode="numeric" placeholder="PIN" value={pin}
                    onChange={(e) => setPin(e.target.value.replace(/\D/g, ''))}
                    className="flex-1 rounded-lg border border-[var(--color-line)] px-2 py-1.5 text-xs tracking-widest" />
                  <button onClick={() => handleSettle(s.shareId)} className="rounded-lg bg-[var(--color-accent)] text-white text-xs font-medium px-3">Pay</button>
                </div>
              ) : (
                <button onClick={() => setSettlingId(s.shareId)} className="text-xs font-medium text-[var(--color-primary)] mt-2">Settle now</button>
              )}
            </div>
          ))}
        </div>
      )}
    </div>
  )
}
