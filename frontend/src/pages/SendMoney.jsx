import { useEffect, useState } from 'react'
import { useNavigate, useSearchParams } from 'react-router-dom'
import { useDispatch } from 'react-redux'
import { upiApi } from '../api/upi'
import { paymentApi } from '../api/payment'
import { pushToast } from '../slices/toastSlice'
import { ArrowLeft, CheckCircle2 } from 'lucide-react'

const STEPS = { ENTER_VPA: 0, ENTER_AMOUNT: 1, PIN: 2, DONE: 3 }

export default function SendMoney() {
  const [searchParams] = useSearchParams()
  const [step, setStep] = useState(STEPS.ENTER_VPA)
  const [recipientType, setRecipientType] = useState('VPA') // VPA | MOBILE | BANK
  const [vpa, setVpa] = useState('')
  const [mobile, setMobile] = useState('')
  const [accountNumber, setAccountNumber] = useState('')
  const [ifsc, setIfsc] = useState('')
  const [recipient, setRecipient] = useState(null)
  const [amount, setAmount] = useState('')
  const [note, setNote] = useState('')
  const [pin, setPin] = useState('')
  const [error, setError] = useState('')
  const [checking, setChecking] = useState(false)
  const [submitting, setSubmitting] = useState(false)
  const [result, setResult] = useState(null)
  const dispatch = useDispatch()
  const navigate = useNavigate()

  // Coming from Scan & Pay (?vpa=...&amount=...&note=...) - skip straight to
  // the amount step with everything pre-filled, instead of re-typing the VPA.
  useEffect(() => {
    const qVpa = searchParams.get('vpa')
    if (!qVpa) return
    setVpa(qVpa)
    setRecipientType('VPA')
    setChecking(true)
    upiApi
      .details(qVpa)
      .then((details) => {
        setRecipient(details)
        const qAmount = searchParams.get('amount')
        const qNote = searchParams.get('note')
        if (qAmount) setAmount(qAmount)
        if (qNote) setNote(qNote)
        setStep(STEPS.ENTER_AMOUNT)
      })
      .catch((err) => setError(err?.response?.data?.message || 'UPI ID not found'))
      .finally(() => setChecking(false))
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [])

  const verifyVpa = async (e) => {
    e.preventDefault()
    setError('')
    setChecking(true)
    try {
      // Only VPA lookups can be verified ahead of time via /upi/details; mobile/bank-account
      // recipients are resolved server-side at send time instead.
      if (recipientType === 'VPA') {
        const details = await upiApi.details(vpa.trim())
        setRecipient(details)
      } else {
        setRecipient({ vpa: recipientType === 'MOBILE' ? mobile : `${accountNumber} (${ifsc})` })
      }
      setStep(STEPS.ENTER_AMOUNT)
    } catch (err) {
      setError(err?.response?.data?.message || 'UPI ID not found')
    } finally {
      setChecking(false)
    }
  }

  const proceedToPin = (e) => {
    e.preventDefault()
    if (!amount || Number(amount) <= 0) {
      setError('Enter a valid amount')
      return
    }
    setError('')
    setStep(STEPS.PIN)
  }

  const confirmPayment = async (e) => {
    e.preventDefault()
    setError('')
    setSubmitting(true)
    try {
      const payload = {
        amount: Number(amount),
        note,
        transactionPin: pin,
      }
      if (recipientType === 'VPA') payload.receiverVpa = recipient.vpa
      else if (recipientType === 'MOBILE') payload.receiverMobile = mobile.trim()
      else {
        payload.receiverAccountNumber = accountNumber.trim()
        payload.receiverIfsc = ifsc.trim()
      }
      const txn = await paymentApi.send(payload)
      setResult(txn)
      setStep(STEPS.DONE)
      dispatch(pushToast('Payment sent successfully', 'success'))
    } catch (err) {
      setError(err?.response?.data?.message || 'Payment failed')
      setPin('')
    } finally {
      setSubmitting(false)
    }
  }

  if (step === STEPS.DONE && result) {
    return (
      <div className="max-w-sm mx-auto text-center py-16">
        <CheckCircle2 size={56} className="mx-auto text-[var(--color-success)]" />
        <h1 className="font-display text-xl font-semibold mt-4">Payment successful</h1>
        <p className="font-mono-amount text-3xl font-semibold mt-2">₹{Number(amount).toLocaleString('en-IN')}</p>
        <p className="text-sm text-[var(--color-muted)] mt-1">to {recipient.vpa}</p>
        <p className="text-xs text-[var(--color-muted)] mt-4">Ref: {result.referenceId}</p>
        <div className="flex gap-3 mt-8 justify-center">
          <button
            onClick={() => navigate('/transactions')}
            className="rounded-lg border border-[var(--color-line)] px-4 py-2 text-sm font-medium"
          >
            View receipt
          </button>
          <button
            onClick={() => navigate('/home')}
            className="rounded-lg bg-[var(--color-primary)] text-white px-4 py-2 text-sm font-medium"
          >
            Done
          </button>
        </div>
      </div>
    )
  }

  return (
    <div className="max-w-sm mx-auto">
      <button onClick={() => navigate(-1)} className="flex items-center gap-1 text-sm text-[var(--color-muted)] mb-4">
        <ArrowLeft size={16} /> Back
      </button>
      <h1 className="font-display text-xl font-semibold mb-6">Send money</h1>

      {step === STEPS.ENTER_VPA && (
        <form onSubmit={verifyVpa} className="space-y-4">
          <div className="flex gap-2 bg-[var(--color-paper)] p-1 rounded-lg border border-[var(--color-line)]">
            {[
              { key: 'VPA', label: 'UPI ID' },
              { key: 'MOBILE', label: 'Mobile' },
              { key: 'BANK', label: 'Bank a/c' },
            ].map((t) => (
              <button
                key={t.key}
                type="button"
                onClick={() => { setRecipientType(t.key); setError('') }}
                className={`flex-1 rounded-md py-1.5 text-xs font-medium transition-colors ${
                  recipientType === t.key ? 'bg-[var(--color-surface)] text-[var(--color-primary)] shadow-sm' : 'text-[var(--color-muted)]'
                }`}
              >
                {t.label}
              </button>
            ))}
          </div>

          {recipientType === 'VPA' && (
            <div>
              <label className="block text-xs font-medium text-[var(--color-muted)] mb-1">Recipient's UPI ID</label>
              <input
                autoFocus
                required
                value={vpa}
                onChange={(e) => setVpa(e.target.value)}
                placeholder="name@payflow"
                className="w-full rounded-lg border border-[var(--color-line)] px-3 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-[var(--color-primary)]/30 focus:border-[var(--color-primary)]"
              />
            </div>
          )}

          {recipientType === 'MOBILE' && (
            <div>
              <label className="block text-xs font-medium text-[var(--color-muted)] mb-1">Recipient's mobile number</label>
              <input
                autoFocus
                required
                pattern="[6-9][0-9]{9}"
                value={mobile}
                onChange={(e) => setMobile(e.target.value)}
                placeholder="9876543210"
                className="w-full rounded-lg border border-[var(--color-line)] px-3 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-[var(--color-primary)]/30 focus:border-[var(--color-primary)]"
              />
              <p className="text-[10px] text-[var(--color-muted)] mt-1">Must be registered on PayFlow.</p>
            </div>
          )}

          {recipientType === 'BANK' && (
            <div className="space-y-3">
              <div>
                <label className="block text-xs font-medium text-[var(--color-muted)] mb-1">Account number</label>
                <input
                  autoFocus
                  required
                  value={accountNumber}
                  onChange={(e) => setAccountNumber(e.target.value)}
                  className="w-full rounded-lg border border-[var(--color-line)] px-3 py-2.5 text-sm font-mono-amount"
                />
              </div>
              <div>
                <label className="block text-xs font-medium text-[var(--color-muted)] mb-1">IFSC code</label>
                <input
                  required
                  value={ifsc}
                  onChange={(e) => setIfsc(e.target.value.toUpperCase())}
                  className="w-full rounded-lg border border-[var(--color-line)] px-3 py-2.5 text-sm uppercase"
                />
              </div>
            </div>
          )}

          {error && <p className="text-sm text-[var(--color-danger)]">{error}</p>}
          <button
            type="submit"
            disabled={checking}
            className="w-full rounded-lg bg-[var(--color-primary)] text-white font-medium py-2.5 text-sm disabled:opacity-60"
          >
            {checking ? 'Verifying…' : 'Continue'}
          </button>
        </form>
      )}

      {step === STEPS.ENTER_AMOUNT && recipient && (
        <form onSubmit={proceedToPin} className="space-y-4">
          <div className="rounded-lg bg-[var(--color-surface)] border border-[var(--color-line)] px-4 py-3">
            <p className="text-xs text-[var(--color-muted)]">Paying</p>
            <p className="text-sm font-medium">{recipient.vpa}</p>
          </div>
          <div>
            <label className="block text-xs font-medium text-[var(--color-muted)] mb-1">Amount (₹)</label>
            <input
              autoFocus
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
              placeholder="For dinner…"
              className="w-full rounded-lg border border-[var(--color-line)] px-3 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-[var(--color-primary)]/30 focus:border-[var(--color-primary)]"
            />
          </div>
          {error && <p className="text-sm text-[var(--color-danger)]">{error}</p>}
          <button type="submit" className="w-full rounded-lg bg-[var(--color-primary)] text-white font-medium py-2.5 text-sm">
            Continue
          </button>
        </form>
      )}

      {step === STEPS.PIN && (
        <form onSubmit={confirmPayment} className="space-y-4">
          <div className="rounded-lg bg-[var(--color-surface)] border border-[var(--color-line)] px-4 py-3 text-center">
            <p className="text-xs text-[var(--color-muted)]">You're paying</p>
            <p className="font-mono-amount text-2xl font-semibold">₹{Number(amount).toLocaleString('en-IN')}</p>
            <p className="text-sm text-[var(--color-muted)]">to {recipient.vpa}</p>
          </div>
          <div>
            <label className="block text-xs font-medium text-[var(--color-muted)] mb-1 text-center">Enter transaction PIN</label>
            <input
              autoFocus
              type="password"
              inputMode="numeric"
              pattern="\d{4,6}"
              maxLength={6}
              required
              value={pin}
              onChange={(e) => setPin(e.target.value.replace(/\D/g, ''))}
              className="w-full rounded-lg border border-[var(--color-line)] px-3 py-2.5 text-center tracking-[0.5em] text-lg focus:outline-none focus:ring-2 focus:ring-[var(--color-primary)]/30 focus:border-[var(--color-primary)]"
            />
          </div>
          {error && <p className="text-sm text-[var(--color-danger)] text-center">{error}</p>}
          <button
            type="submit"
            disabled={submitting}
            className="w-full rounded-lg bg-[var(--color-accent)] hover:bg-[var(--color-accent-dark)] text-white font-medium py-2.5 text-sm disabled:opacity-60"
          >
            {submitting ? 'Processing…' : `Pay ₹${amount}`}
          </button>
        </form>
      )}
    </div>
  )
}
