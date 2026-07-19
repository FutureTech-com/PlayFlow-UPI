import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useDispatch, useSelector } from 'react-redux'
import { fetchUpiIds } from '../slices/walletSlice'
import api from '../api/client'
import { ArrowLeft } from 'lucide-react'

export default function MerchantQr() {
  const dispatch = useDispatch()
  const navigate = useNavigate()
  const { upiIds } = useSelector((s) => s.wallet)
  const [vpa, setVpa] = useState('')
  const [amount, setAmount] = useState('')
  const [note, setNote] = useState('')
  const [qr, setQr] = useState(null)
  const [loading, setLoading] = useState(false)

  useEffect(() => { dispatch(fetchUpiIds()) }, [dispatch])
  useEffect(() => { if (upiIds[0] && !vpa) setVpa(upiIds[0].vpa) }, [upiIds])

  const generate = async (e) => {
    e.preventDefault()
    setLoading(true)
    try {
      const res = await api.get('/upi/qr/dynamic', { params: { vpa, amount: amount || undefined, note: note || undefined } })
      setQr(res.data.qrCodeBase64)
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="max-w-sm mx-auto">
      <button onClick={() => navigate(-1)} className="flex items-center gap-1 text-sm text-[var(--color-muted)] mb-4">
        <ArrowLeft size={16} /> Back
      </button>
      <h1 className="font-display text-xl font-semibold mb-6">Merchant QR generator</h1>

      <form onSubmit={generate} className="space-y-4">
        <select required value={vpa} onChange={(e) => setVpa(e.target.value)} className="w-full rounded-lg border border-[var(--color-line)] px-3 py-2.5 text-sm">
          <option value="">Choose your UPI ID</option>
          {upiIds.map((u) => <option key={u.id} value={u.vpa}>{u.vpa}{u.merchant ? ' (merchant)' : ''}</option>)}
        </select>
        <input type="number" min="0" step="0.01" placeholder="Fixed amount (optional — leave blank for any amount)" value={amount} onChange={(e) => setAmount(e.target.value)}
          className="w-full rounded-lg border border-[var(--color-line)] px-3 py-2.5 text-sm font-mono-amount" />
        <input placeholder="Note (optional)" value={note} onChange={(e) => setNote(e.target.value)}
          className="w-full rounded-lg border border-[var(--color-line)] px-3 py-2.5 text-sm" />
        <button type="submit" disabled={loading} className="w-full rounded-lg bg-[var(--color-primary)] text-white font-medium py-2.5 text-sm disabled:opacity-60">
          {loading ? 'Generating…' : 'Generate QR'}
        </button>
      </form>

      {qr && (
        <div className="mt-6 bg-[var(--color-surface)] border border-[var(--color-line)] rounded-2xl p-6 text-center">
          <img src={`data:image/png;base64,${qr}`} alt="Dynamic QR" className="mx-auto w-56 h-56 rounded-lg border border-[var(--color-line)]" />
          <p className="text-xs text-[var(--color-muted)] mt-3">{amount ? `Charges exactly ₹${amount}` : 'Payer chooses the amount'}</p>
        </div>
      )}
    </div>
  )
}
