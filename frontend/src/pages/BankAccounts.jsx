import { useEffect, useState } from 'react'
import { useDispatch, useSelector } from 'react-redux'
import {
  fetchBankAccounts,
  fetchUpiIds,
  linkBankAccount,
  linkBankAccountByMobile,
  setPrimaryAccount,
  createUpiId,
} from '../slices/walletSlice'
import { bankApi } from '../api/bank'
import { pushToast } from '../slices/toastSlice'
import { Landmark, Plus, ShieldCheck, Star, X, Smartphone, Pencil, Search } from 'lucide-react'

export default function BankAccounts() {
  const dispatch = useDispatch()
  const { bankAccounts, upiIds } = useSelector((s) => s.wallet)
  const [showLinkForm, setShowLinkForm] = useState(false)
  const [linkTab, setLinkTab] = useState('mobile') // mobile | manual

  // --- manual entry state ---
  const [form, setForm] = useState({ bankName: '', accountHolderName: '', accountNumber: '', ifscCode: '', accountType: 'SAVINGS' })
  const [submitting, setSubmitting] = useState(false)
  const [error, setError] = useState('')

  // --- fetch-by-mobile state ---
  const [mobile, setMobile] = useState('')
  const [fetching, setFetching] = useState(false)
  const [fetchedAccounts, setFetchedAccounts] = useState(null) // null = not fetched yet, [] = fetched, none shown
  const [fetchError, setFetchError] = useState('')
  const [linkingIndex, setLinkingIndex] = useState(null)

  const [creatingUpi, setCreatingUpi] = useState(null)
  const [handle, setHandle] = useState('')

  useEffect(() => {
    dispatch(fetchBankAccounts())
    dispatch(fetchUpiIds())
  }, [dispatch])

  const resetLinkForm = () => {
    setForm({ bankName: '', accountHolderName: '', accountNumber: '', ifscCode: '', accountType: 'SAVINGS' })
    setMobile('')
    setFetchedAccounts(null)
    setFetchError('')
    setError('')
  }

  const handleChange = (e) => setForm((f) => ({ ...f, [e.target.name]: e.target.value }))

  const handleManualLink = async (e) => {
    e.preventDefault()
    setError('')
    setSubmitting(true)
    const result = await dispatch(linkBankAccount(form))
    setSubmitting(false)
    if (linkBankAccount.fulfilled.match(result)) {
      dispatch(pushToast('Bank account linked', 'success'))
      setShowLinkForm(false)
      resetLinkForm()
    } else {
      setError(result.payload || 'Could not link account')
    }
  }

  const handleFetchByMobile = async (e) => {
    e.preventDefault()
    setFetchError('')
    setFetching(true)
    try {
      const accounts = await bankApi.fetchByMobile(mobile.trim())
      setFetchedAccounts(accounts)
      if (accounts.length === 0) {
        setFetchError('No accounts found linked to this mobile number.')
      }
    } catch (err) {
      setFetchError(err?.response?.data?.message || 'Could not fetch accounts for this number')
      setFetchedAccounts(null)
    } finally {
      setFetching(false)
    }
  }

  const handleLinkFetched = async (accountIndex) => {
    setLinkingIndex(accountIndex)
    const result = await dispatch(linkBankAccountByMobile({ mobileNumber: mobile.trim(), accountIndex }))
    setLinkingIndex(null)
    if (linkBankAccountByMobile.fulfilled.match(result)) {
      dispatch(pushToast('Bank account linked', 'success'))
      setShowLinkForm(false)
      resetLinkForm()
    } else {
      dispatch(pushToast(result.payload || 'Could not link account', 'error'))
    }
  }

  const handleSetPrimary = (id) => dispatch(setPrimaryAccount(id)).then(() => dispatch(pushToast('Primary account updated', 'success')))

  const handleCreateUpi = async (accountId) => {
    const result = await dispatch(createUpiId({ bankAccountId: accountId, preferredHandle: handle || undefined }))
    if (createUpiId.fulfilled.match(result)) {
      dispatch(pushToast(`UPI ID created: ${result.payload.vpa}`, 'success'))
      setCreatingUpi(null)
      setHandle('')
    } else {
      dispatch(pushToast(result.payload || 'Could not create UPI ID', 'error'))
    }
  }

  const upiForAccount = (accountId) => upiIds.find((u) => u.linkedBankAccountId === accountId)

  return (
    <div className="space-y-6 max-w-2xl">
      <div className="flex items-center justify-between">
        <h1 className="font-display text-xl font-semibold">Bank accounts</h1>
        <button
          onClick={() => {
            setShowLinkForm((v) => !v)
            resetLinkForm()
          }}
          className="flex items-center gap-1.5 rounded-lg bg-[var(--color-primary)] text-white text-sm font-medium px-3 py-2"
        >
          {showLinkForm ? <X size={16} /> : <Plus size={16} />}
          {showLinkForm ? 'Cancel' : 'Link account'}
        </button>
      </div>

      {showLinkForm && (
        <div className="bg-[var(--color-surface)] border border-[var(--color-line)] rounded-2xl p-5 space-y-4">
          <div className="flex gap-2 bg-[var(--color-paper)] p-1 rounded-lg border border-[var(--color-line)]">
            <button
              type="button"
              onClick={() => setLinkTab('mobile')}
              className={`flex-1 flex items-center justify-center gap-1.5 rounded-md py-1.5 text-xs font-medium transition-colors ${
                linkTab === 'mobile' ? 'bg-[var(--color-surface)] text-[var(--color-primary)] shadow-sm' : 'text-[var(--color-muted)]'
              }`}
            >
              <Smartphone size={14} /> Fetch via mobile number
            </button>
            <button
              type="button"
              onClick={() => setLinkTab('manual')}
              className={`flex-1 flex items-center justify-center gap-1.5 rounded-md py-1.5 text-xs font-medium transition-colors ${
                linkTab === 'manual' ? 'bg-[var(--color-surface)] text-[var(--color-primary)] shadow-sm' : 'text-[var(--color-muted)]'
              }`}
            >
              <Pencil size={14} /> Enter manually
            </button>
          </div>

          {linkTab === 'mobile' ? (
            <div className="space-y-3">
              <p className="text-xs text-[var(--color-muted)]">
                Enter a mobile number to discover bank accounts linked to it — the same way UPI apps find your
                accounts automatically. (Simulated for this demo: no real bank/NPCI is contacted.)
              </p>
              <form onSubmit={handleFetchByMobile} className="flex gap-2">
                <input
                  required
                  pattern="[6-9][0-9]{9}"
                  value={mobile}
                  onChange={(e) => {
                    setMobile(e.target.value.replace(/\D/g, ''))
                    setFetchedAccounts(null)
                  }}
                  placeholder="9876543210"
                  className="flex-1 rounded-lg border border-[var(--color-line)] px-3 py-2 text-sm"
                />
                <button
                  type="submit"
                  disabled={fetching}
                  className="flex items-center gap-1.5 rounded-lg bg-[var(--color-primary)] text-white text-sm font-medium px-4 py-2 disabled:opacity-60"
                >
                  <Search size={14} />
                  {fetching ? 'Fetching…' : 'Fetch'}
                </button>
              </form>

              {fetchError && <p className="text-sm text-[var(--color-danger)]">{fetchError}</p>}

              {fetchedAccounts && fetchedAccounts.length > 0 && (
                <div className="space-y-2">
                  {fetchedAccounts.map((acc) => (
                    <div
                      key={acc.accountIndex}
                      className="flex items-center justify-between rounded-lg border border-[var(--color-line)] px-3 py-2.5"
                    >
                      <div className="flex items-center gap-2.5">
                        <div className="w-8 h-8 rounded-full bg-[var(--color-primary)]/10 flex items-center justify-center text-[var(--color-primary)]">
                          <Landmark size={14} />
                        </div>
                        <div>
                          <p className="text-sm font-medium">{acc.bankName}</p>
                          <p className="text-xs text-[var(--color-muted)]">{acc.maskedAccountNumber} · {acc.ifscCode}</p>
                        </div>
                      </div>
                      <button
                        onClick={() => handleLinkFetched(acc.accountIndex)}
                        disabled={linkingIndex === acc.accountIndex}
                        className="text-xs font-medium rounded-lg bg-[var(--color-primary)] text-white px-3 py-1.5 disabled:opacity-60"
                      >
                        {linkingIndex === acc.accountIndex ? 'Linking…' : 'Link'}
                      </button>
                    </div>
                  ))}
                </div>
              )}
            </div>
          ) : (
            <form onSubmit={handleManualLink} className="space-y-3">
              <div className="grid sm:grid-cols-2 gap-3">
                <div>
                  <label className="block text-xs font-medium text-[var(--color-muted)] mb-1">Bank name</label>
                  <input name="bankName" required value={form.bankName} onChange={handleChange} placeholder="HDFC Bank"
                    className="w-full rounded-lg border border-[var(--color-line)] px-3 py-2 text-sm" />
                </div>
                <div>
                  <label className="block text-xs font-medium text-[var(--color-muted)] mb-1">Account holder name</label>
                  <input name="accountHolderName" required value={form.accountHolderName} onChange={handleChange} placeholder="Rahul Sharma"
                    className="w-full rounded-lg border border-[var(--color-line)] px-3 py-2 text-sm" />
                </div>
                <div>
                  <label className="block text-xs font-medium text-[var(--color-muted)] mb-1">Account number</label>
                  <input name="accountNumber" required value={form.accountNumber} onChange={handleChange} placeholder="XXXXXXXXXXXX"
                    className="w-full rounded-lg border border-[var(--color-line)] px-3 py-2 text-sm font-mono-amount" />
                </div>
                <div>
                  <label className="block text-xs font-medium text-[var(--color-muted)] mb-1">IFSC code</label>
                  <input name="ifscCode" required value={form.ifscCode} onChange={handleChange} placeholder="HDFC0001234"
                    className="w-full rounded-lg border border-[var(--color-line)] px-3 py-2 text-sm uppercase" />
                </div>
              </div>
              <div>
                <label className="block text-xs font-medium text-[var(--color-muted)] mb-1">Account type</label>
                <select name="accountType" value={form.accountType} onChange={handleChange}
                  className="w-full rounded-lg border border-[var(--color-line)] px-3 py-2 text-sm">
                  <option value="SAVINGS">Savings</option>
                  <option value="CURRENT">Current</option>
                </select>
              </div>
              {error && <p className="text-sm text-[var(--color-danger)]">{error}</p>}
              <p className="text-xs text-[var(--color-muted)]">Verification is simulated instantly for this demo — no real bank is contacted.</p>
              <button type="submit" disabled={submitting}
                className="rounded-lg bg-[var(--color-primary)] text-white text-sm font-medium px-4 py-2 disabled:opacity-60">
                {submitting ? 'Linking…' : 'Link account'}
              </button>
            </form>
          )}
        </div>
      )}

      <div className="space-y-3">
        {bankAccounts.length === 0 && !showLinkForm && (
          <div className="text-center py-12 text-sm text-[var(--color-muted)]">
            <Landmark size={32} className="mx-auto mb-2 text-[var(--color-muted)]" />
            No bank accounts linked yet.
            <div>
              <button
                onClick={() => setShowLinkForm(true)}
                className="mt-4 inline-flex items-center gap-1.5 rounded-lg bg-[var(--color-primary)] text-white text-sm font-medium px-4 py-2"
              >
                <Plus size={16} /> Add your first account
              </button>
            </div>
          </div>
        )}
        {bankAccounts.map((acc) => {
          const upi = upiForAccount(acc.id)
          return (
            <div key={acc.id} className="bg-[var(--color-surface)] border border-[var(--color-line)] rounded-2xl p-5">
              <div className="flex items-start justify-between">
                <div className="flex items-center gap-3">
                  <div className="w-10 h-10 rounded-full bg-[var(--color-primary)]/10 flex items-center justify-center text-[var(--color-primary)]">
                    <Landmark size={18} />
                  </div>
                  <div>
                    <p className="font-medium text-sm">{acc.bankName}</p>
                    <p className="text-xs text-[var(--color-muted)]">{acc.maskedAccountNumber} · {acc.accountType}</p>
                  </div>
                </div>
                {acc.primary && (
                  <span className="flex items-center gap-1 text-xs font-medium text-[var(--color-accent)] bg-[var(--color-accent)]/10 px-2 py-1 rounded-full">
                    <Star size={12} /> Primary
                  </span>
                )}
              </div>

              <div className="mt-4 flex items-center justify-between">
                <p className="font-mono-amount text-lg font-semibold">
                  ₹{Number(acc.balance).toLocaleString('en-IN', { minimumFractionDigits: 2 })}
                </p>
                {acc.verified && (
                  <span className="flex items-center gap-1 text-xs text-[var(--color-success)]">
                    <ShieldCheck size={14} /> Verified
                  </span>
                )}
              </div>

              <div className="mt-4 flex flex-wrap gap-2">
                {!acc.primary && (
                  <button onClick={() => handleSetPrimary(acc.id)} className="text-xs font-medium rounded-lg border border-[var(--color-line)] px-3 py-1.5">
                    Set as primary
                  </button>
                )}
                {upi ? (
                  <span className="text-xs font-medium rounded-lg bg-[var(--color-paper)] px-3 py-1.5">{upi.vpa}</span>
                ) : creatingUpi === acc.id ? (
                  <span className="flex items-center gap-1">
                    <input
                      autoFocus
                      value={handle}
                      onChange={(e) => setHandle(e.target.value)}
                      placeholder="handle (optional)"
                      className="text-xs rounded-lg border border-[var(--color-line)] px-2 py-1.5 w-32"
                    />
                    <button onClick={() => handleCreateUpi(acc.id)} className="text-xs font-medium rounded-lg bg-[var(--color-primary)] text-white px-3 py-1.5">
                      Create
                    </button>
                  </span>
                ) : (
                  <button onClick={() => setCreatingUpi(acc.id)} className="text-xs font-medium rounded-lg border border-[var(--color-line)] px-3 py-1.5">
                    Create UPI ID
                  </button>
                )}
              </div>
            </div>
          )
        })}
      </div>
    </div>
  )
}
