import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useSelector, useDispatch } from 'react-redux'
import { fetchUpiIds } from '../slices/walletSlice'
import { ArrowLeft, Copy, Check } from 'lucide-react'

export default function ReceiveMoney() {
  const dispatch = useDispatch()
  const navigate = useNavigate()
  const { upiIds } = useSelector((s) => s.wallet)
  const [copied, setCopied] = useState(false)

  useEffect(() => {
    dispatch(fetchUpiIds())
  }, [dispatch])

  const primary = upiIds[0]

  const copyVpa = () => {
    if (!primary) return
    navigator.clipboard.writeText(primary.vpa)
    setCopied(true)
    setTimeout(() => setCopied(false), 1500)
  }

  return (
    <div className="max-w-sm mx-auto">
      <button onClick={() => navigate(-1)} className="flex items-center gap-1 text-sm text-[var(--color-muted)] mb-4">
        <ArrowLeft size={16} /> Back
      </button>
      <h1 className="font-display text-xl font-semibold mb-6">Receive money</h1>

      {!primary ? (
        <div className="text-center py-12 text-sm text-[var(--color-muted)]">
          You need a UPI ID before you can receive money.
          <br />
          <button onClick={() => navigate('/bank-accounts')} className="text-[var(--color-primary)] font-medium mt-2">
            Create one →
          </button>
        </div>
      ) : (
        <div className="bg-[var(--color-surface)] border border-[var(--color-line)] rounded-2xl p-6 text-center">
          {primary.qrCodeBase64 ? (
            <img
              src={`data:image/png;base64,${primary.qrCodeBase64}`}
              alt="UPI QR code"
              className="mx-auto w-56 h-56 rounded-lg border border-[var(--color-line)]"
            />
          ) : (
            <div className="mx-auto w-56 h-56 rounded-lg border border-dashed border-[var(--color-line)] flex items-center justify-center text-xs text-[var(--color-muted)]">
              QR unavailable
            </div>
          )}
          <p className="text-xs text-[var(--color-muted)] mt-4">Scan this code to pay you, or share your UPI ID</p>
          <button
            onClick={copyVpa}
            className="mt-3 inline-flex items-center gap-2 rounded-lg border border-[var(--color-line)] px-4 py-2 text-sm font-medium"
          >
            {primary.vpa}
            {copied ? <Check size={14} className="text-[var(--color-success)]" /> : <Copy size={14} />}
          </button>
        </div>
      )}
    </div>
  )
}
