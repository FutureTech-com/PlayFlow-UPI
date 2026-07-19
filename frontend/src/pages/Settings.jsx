import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useDispatch } from 'react-redux'
import { authApi } from '../api/auth'
import { userApi } from '../api/user'
import { pushToast } from '../slices/toastSlice'
import { logout } from '../slices/authSlice'
import { ArrowLeft, KeyRound, Lock, LogOut, Send } from 'lucide-react'

export default function Settings() {
  const navigate = useNavigate()
  const dispatch = useDispatch()

  const [pwForm, setPwForm] = useState({ oldPassword: '', newPassword: '' })
  const [pinForm, setPinForm] = useState({ pin: '' })
  const [pwStatus, setPwStatus] = useState('')
  const [pinStatus, setPinStatus] = useState('')

  const [resetStep, setResetStep] = useState('idle') // idle | otp-sent
  const [resetOtp, setResetOtp] = useState('')
  const [newPin, setNewPin] = useState('')
  const [resetStatus, setResetStatus] = useState('')
  const [resetSubmitting, setResetSubmitting] = useState(false)

  const changePassword = async (e) => {
    e.preventDefault()
    try {
      await authApi.changePassword(pwForm)
      dispatch(pushToast('Password changed', 'success'))
      setPwForm({ oldPassword: '', newPassword: '' })
      setPwStatus('')
    } catch (err) {
      setPwStatus(err?.response?.data?.message || 'Could not change password')
    }
  }

  const setPin = async (e) => {
    e.preventDefault()
    try {
      await userApi.setTransactionPin(pinForm)
      dispatch(pushToast('Transaction PIN updated', 'success'))
      setPinForm({ pin: '' })
      setPinStatus('')
    } catch (err) {
      setPinStatus(err?.response?.data?.message || 'Could not set PIN')
    }
  }

  const requestPinReset = async () => {
    setResetStatus('')
    try {
      await userApi.forgotPin()
      setResetStep('otp-sent')
      dispatch(pushToast('OTP sent to your registered mobile number', 'success'))
    } catch (err) {
      setResetStatus(err?.response?.data?.message || 'Could not send OTP')
    }
  }

  const confirmPinReset = async (e) => {
    e.preventDefault()
    setResetStatus('')
    setResetSubmitting(true)
    try {
      await userApi.resetPin(resetOtp, newPin)
      dispatch(pushToast('Transaction PIN reset successfully', 'success'))
      setResetStep('idle')
      setResetOtp('')
      setNewPin('')
    } catch (err) {
      setResetStatus(err?.response?.data?.message || 'Invalid or expired OTP')
    } finally {
      setResetSubmitting(false)
    }
  }

  const handleLogout = () => {
    dispatch(logout())
    navigate('/login')
  }

  return (
    <div className="max-w-sm space-y-6">
      <button onClick={() => navigate(-1)} className="flex items-center gap-1 text-sm text-[var(--color-muted)]">
        <ArrowLeft size={16} /> Back
      </button>
      <h1 className="font-display text-xl font-semibold">Settings & security</h1>

      <form onSubmit={changePassword} className="bg-[var(--color-surface)] border border-[var(--color-line)] rounded-2xl p-5 space-y-3">
        <div className="flex items-center gap-2 font-medium text-sm">
          <Lock size={16} /> Change password
        </div>
        <input
          type="password"
          required
          placeholder="Current password"
          value={pwForm.oldPassword}
          onChange={(e) => setPwForm((f) => ({ ...f, oldPassword: e.target.value }))}
          className="w-full rounded-lg border border-[var(--color-line)] px-3 py-2 text-sm"
        />
        <input
          type="password"
          required
          minLength={8}
          placeholder="New password (min 8 characters)"
          value={pwForm.newPassword}
          onChange={(e) => setPwForm((f) => ({ ...f, newPassword: e.target.value }))}
          className="w-full rounded-lg border border-[var(--color-line)] px-3 py-2 text-sm"
        />
        {pwStatus && <p className="text-xs text-[var(--color-danger)]">{pwStatus}</p>}
        <button type="submit" className="rounded-lg bg-[var(--color-primary)] text-white text-sm font-medium px-4 py-2">
          Update password
        </button>
      </form>

      <form onSubmit={setPin} className="bg-[var(--color-surface)] border border-[var(--color-line)] rounded-2xl p-5 space-y-3">
        <div className="flex items-center gap-2 font-medium text-sm">
          <KeyRound size={16} /> Transaction PIN
        </div>
        <input
          type="password"
          inputMode="numeric"
          pattern="\d{4,6}"
          required
          placeholder="4-6 digit PIN"
          value={pinForm.pin}
          onChange={(e) => setPinForm({ pin: e.target.value.replace(/\D/g, '') })}
          className="w-full rounded-lg border border-[var(--color-line)] px-3 py-2 text-sm tracking-widest"
        />
        {pinStatus && <p className="text-xs text-[var(--color-danger)]">{pinStatus}</p>}
        <div className="flex items-center justify-between">
          <button type="submit" className="rounded-lg bg-[var(--color-primary)] text-white text-sm font-medium px-4 py-2">
            Set PIN
          </button>
          {resetStep === 'idle' && (
            <button type="button" onClick={requestPinReset} className="text-xs font-medium text-[var(--color-muted)] hover:text-[var(--color-primary)]">
              Forgot PIN?
            </button>
          )}
        </div>
      </form>

      {resetStep === 'otp-sent' && (
        <form onSubmit={confirmPinReset} className="bg-[var(--color-surface)] border border-[var(--color-line)] rounded-2xl p-5 space-y-3">
          <div className="flex items-center gap-2 font-medium text-sm">
            <Send size={16} /> Reset PIN with OTP
          </div>
          <p className="text-xs text-[var(--color-muted)]">
            Enter the OTP sent to your registered mobile number, then choose a new PIN.
          </p>
          <input
            autoFocus
            inputMode="numeric"
            placeholder="6-digit OTP"
            required
            value={resetOtp}
            onChange={(e) => setResetOtp(e.target.value.replace(/\D/g, ''))}
            className="w-full rounded-lg border border-[var(--color-line)] px-3 py-2 text-sm tracking-widest"
          />
          <input
            type="password"
            inputMode="numeric"
            pattern="\d{4,6}"
            placeholder="New 4-6 digit PIN"
            required
            value={newPin}
            onChange={(e) => setNewPin(e.target.value.replace(/\D/g, ''))}
            className="w-full rounded-lg border border-[var(--color-line)] px-3 py-2 text-sm tracking-widest"
          />
          {resetStatus && <p className="text-xs text-[var(--color-danger)]">{resetStatus}</p>}
          <div className="flex gap-2">
            <button
              type="button"
              onClick={() => { setResetStep('idle'); setResetOtp(''); setNewPin(''); setResetStatus('') }}
              className="rounded-lg border border-[var(--color-line)] text-sm font-medium px-4 py-2"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={resetSubmitting}
              className="flex-1 rounded-lg bg-[var(--color-accent)] hover:bg-[var(--color-accent-dark)] text-white text-sm font-medium px-4 py-2 disabled:opacity-60"
            >
              {resetSubmitting ? 'Resetting…' : 'Confirm new PIN'}
            </button>
          </div>
        </form>
      )}

      <button onClick={handleLogout} className="flex items-center gap-2 text-sm font-medium text-[var(--color-danger)]">
        <LogOut size={16} /> Log out
      </button>
    </div>
  )
}
