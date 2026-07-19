import { useEffect, useState } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { transactionApi } from '../api/transactions'
import AmountText from '../components/AmountText'
import { ArrowLeft, Download, CheckCircle2, XCircle, Clock, AlertTriangle } from 'lucide-react'

const STATUS_CONFIG = {
  SUCCESS: { icon: CheckCircle2, className: 'text-[var(--color-success)]' },
  FAILED: { icon: XCircle, className: 'text-[var(--color-danger)]' },
  PENDING: { icon: Clock, className: 'text-[var(--color-muted)]' },
}

export default function TransactionDetail() {
  const { id } = useParams()
  const navigate = useNavigate()
  const [txn, setTxn] = useState(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    transactionApi
      .details(id)
      .then(setTxn)
      .finally(() => setLoading(false))
  }, [id])

  const downloadReceipt = async () => {
    const token = localStorage.getItem('payflow_access_token')
    const res = await fetch(transactionApi.receiptUrl(id), { headers: { Authorization: `Bearer ${token}` } })
    const blob = await res.blob()
    const url = window.URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = `receipt-${id}.pdf`
    a.click()
    window.URL.revokeObjectURL(url)
  }

  if (loading) return <p className="text-sm text-[var(--color-muted)]">Loading…</p>
  if (!txn) return <p className="text-sm text-[var(--color-danger)]">Transaction not found.</p>

  const cfg = STATUS_CONFIG[txn.status] || STATUS_CONFIG.PENDING
  const StatusIcon = cfg.icon

  return (
    <div className="max-w-sm mx-auto">
      <button onClick={() => navigate(-1)} className="flex items-center gap-1 text-sm text-[var(--color-muted)] mb-4">
        <ArrowLeft size={16} /> Back
      </button>

      <div className="bg-[var(--color-surface)] border border-[var(--color-line)] rounded-2xl p-6 text-center">
        <StatusIcon size={48} className={`mx-auto ${cfg.className}`} />
        <AmountText value={txn.amount} className="text-3xl font-semibold block mt-3" />
        <p className={`text-sm font-medium mt-1 ${cfg.className}`}>{txn.status}</p>

        {(txn.riskLevel === 'MEDIUM' || txn.riskLevel === 'HIGH') && (
          <div className="mt-4 flex items-center gap-2 rounded-lg bg-[var(--color-accent)]/10 text-[var(--color-accent-dark)] px-3 py-2 text-xs text-left">
            <AlertTriangle size={16} className="shrink-0" />
            <span>
              {txn.riskLevel === 'HIGH' ? 'This payment was flagged as unusual by fraud prediction.' : 'This payment had a few unusual signals.'}
              {' '}If this wasn't you, report it from Security &amp; devices.
            </span>
          </div>
        )}

        <div className="mt-6 text-left space-y-3 border-t border-[var(--color-line)] pt-4">
          <Row label={txn.type === 'CREDIT' ? 'From' : 'To'} value={txn.counterpartyName} />
          <Row label="UPI ID" value={txn.counterpartyVpa} />
          {txn.category && <Row label="Category" value={txn.category} />}
          <Row label="Reference ID" value={txn.referenceId} mono />
          <Row label="Date & time" value={new Date(txn.timestamp).toLocaleString('en-IN', { dateStyle: 'medium', timeStyle: 'medium' })} />
          {txn.note && <Row label="Note" value={txn.note} />}
        </div>

        <button
          onClick={downloadReceipt}
          className="mt-6 w-full flex items-center justify-center gap-2 rounded-lg border border-[var(--color-line)] py-2.5 text-sm font-medium"
        >
          <Download size={16} /> Download receipt (PDF)
        </button>
      </div>
    </div>
  )
}

function Row({ label, value, mono }) {
  return (
    <div className="flex items-center justify-between text-sm">
      <span className="text-[var(--color-muted)]">{label}</span>
      <span className={mono ? 'font-mono-amount' : 'font-medium'}>{value}</span>
    </div>
  )
}
