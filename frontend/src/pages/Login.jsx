import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useDispatch, useSelector } from 'react-redux'
import { loginUser, loginWithMobileOtp, clearAuthError } from '../slices/authSlice'
import { authApi } from '../api/auth'
import { pushToast } from '../slices/toastSlice'
import Logo from '../components/Logo'
import { Eye, EyeOff, Send } from 'lucide-react'

export default function Login() {
  const [mode, setMode] = useState('password') // password | mobile
  const [form, setForm] = useState({ email: '', password: '' })
  const [showPassword, setShowPassword] = useState(false)

  const [phone, setPhone] = useState('')
  const [otp, setOtp] = useState('')
  const [otpStep, setOtpStep] = useState('enter-phone') // enter-phone | enter-otp
  const [otpSending, setOtpSending] = useState(false)
  const [otpError, setOtpError] = useState('')

  const dispatch = useDispatch()
  const navigate = useNavigate()
  const { status, error } = useSelector((s) => s.auth)

  const handleChange = (e) => setForm((f) => ({ ...f, [e.target.name]: e.target.value }))

  const switchMode = (next) => {
    setMode(next)
    dispatch(clearAuthError())
    setOtpError('')
  }

  const handlePasswordSubmit = async (e) => {
    e.preventDefault()
    dispatch(clearAuthError())
    const result = await dispatch(loginUser(form))
    if (loginUser.fulfilled.match(result)) {
      dispatch(pushToast(`Welcome back, ${result.payload.user.fullName.split(' ')[0]}!`, 'success'))
      navigate('/home')
    }
  }

  const sendLoginOtp = async (e) => {
    e.preventDefault()
    setOtpError('')
    setOtpSending(true)
    try {
      await authApi.sendMobileLoginOtp(phone.trim())
      setOtpStep('enter-otp')
      dispatch(pushToast('OTP sent to your registered mobile number', 'success'))
    } catch (err) {
      setOtpError(err?.response?.data?.message || 'Could not send OTP')
    } finally {
      setOtpSending(false)
    }
  }

  const verifyLoginOtp = async (e) => {
    e.preventDefault()
    setOtpError('')
    const result = await dispatch(loginWithMobileOtp({ phone: phone.trim(), otp }))
    if (loginWithMobileOtp.fulfilled.match(result)) {
      dispatch(pushToast(`Welcome back, ${result.payload.user.fullName.split(' ')[0]}!`, 'success'))
      navigate('/home')
    } else {
      setOtpError(result.payload || 'Invalid or expired OTP')
    }
  }

  return (
    <div className="min-h-screen flex items-center justify-center bg-[var(--color-paper)] px-4">
      <div className="w-full max-w-sm">
        <div className="flex justify-center mb-8">
          <Logo className="text-[var(--color-ink)] text-xl" />
        </div>

        <div className="bg-[var(--color-surface)] rounded-2xl shadow-sm border border-[var(--color-line)] p-6">
          <h1 className="font-display text-xl font-semibold mb-1">Welcome back</h1>
          <p className="text-sm text-[var(--color-muted)] mb-5">Log in to send and receive money.</p>

          <div className="flex gap-2 bg-[var(--color-paper)] p-1 rounded-lg border border-[var(--color-line)] mb-5">
            <button
              type="button"
              onClick={() => switchMode('password')}
              className={`flex-1 rounded-md py-1.5 text-xs font-medium transition-colors ${
                mode === 'password' ? 'bg-[var(--color-surface)] text-[var(--color-primary)] shadow-sm' : 'text-[var(--color-muted)]'
              }`}
            >
              Password
            </button>
            <button
              type="button"
              onClick={() => switchMode('mobile')}
              className={`flex-1 rounded-md py-1.5 text-xs font-medium transition-colors ${
                mode === 'mobile' ? 'bg-[var(--color-surface)] text-[var(--color-primary)] shadow-sm' : 'text-[var(--color-muted)]'
              }`}
            >
              Mobile OTP
            </button>
          </div>

          {mode === 'password' ? (
            <form onSubmit={handlePasswordSubmit} className="space-y-4">
              <div>
                <label className="block text-xs font-medium text-[var(--color-muted)] mb-1">Email</label>
                <input
                  type="email"
                  name="email"
                  required
                  value={form.email}
                  onChange={handleChange}
                  placeholder="you@example.com"
                  className="w-full rounded-lg border border-[var(--color-line)] px-3 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-[var(--color-primary)]/30 focus:border-[var(--color-primary)]"
                />
              </div>
              <div>
                <label className="block text-xs font-medium text-[var(--color-muted)] mb-1">Password</label>
                <div className="relative">
                  <input
                    type={showPassword ? 'text' : 'password'}
                    name="password"
                    required
                    value={form.password}
                    onChange={handleChange}
                    placeholder="••••••••"
                    className="w-full rounded-lg border border-[var(--color-line)] px-3 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-[var(--color-primary)]/30 focus:border-[var(--color-primary)]"
                  />
                  <button
                    type="button"
                    onClick={() => setShowPassword((v) => !v)}
                    className="absolute right-3 top-1/2 -translate-y-1/2 text-[var(--color-muted)]"
                  >
                    {showPassword ? <EyeOff size={16} /> : <Eye size={16} />}
                  </button>
                </div>
              </div>

              {error && <p className="text-sm text-[var(--color-danger)]">{error}</p>}

              <button
                type="submit"
                disabled={status === 'loading'}
                className="w-full rounded-lg bg-[var(--color-primary)] hover:bg-[var(--color-primary-dark)] text-white font-medium py-2.5 text-sm transition-colors disabled:opacity-60"
              >
                {status === 'loading' ? 'Logging in…' : 'Log in'}
              </button>
            </form>
          ) : otpStep === 'enter-phone' ? (
            <form onSubmit={sendLoginOtp} className="space-y-4">
              <div>
                <label className="block text-xs font-medium text-[var(--color-muted)] mb-1">Registered mobile number</label>
                <input
                  autoFocus
                  required
                  pattern="[6-9][0-9]{9}"
                  value={phone}
                  onChange={(e) => setPhone(e.target.value.replace(/\D/g, ''))}
                  placeholder="9876543210"
                  className="w-full rounded-lg border border-[var(--color-line)] px-3 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-[var(--color-primary)]/30 focus:border-[var(--color-primary)]"
                />
              </div>
              {otpError && <p className="text-sm text-[var(--color-danger)]">{otpError}</p>}
              <button
                type="submit"
                disabled={otpSending}
                className="w-full flex items-center justify-center gap-2 rounded-lg bg-[var(--color-primary)] hover:bg-[var(--color-primary-dark)] text-white font-medium py-2.5 text-sm transition-colors disabled:opacity-60"
              >
                <Send size={15} />
                {otpSending ? 'Sending OTP…' : 'Send OTP'}
              </button>
            </form>
          ) : (
            <form onSubmit={verifyLoginOtp} className="space-y-4">
              <div className="rounded-lg bg-[var(--color-paper)] border border-[var(--color-line)] px-4 py-3 text-sm">
                OTP sent to <span className="font-medium">{phone}</span>.{' '}
                <button type="button" onClick={() => setOtpStep('enter-phone')} className="text-[var(--color-primary)] font-medium">
                  Change
                </button>
              </div>
              <div>
                <label className="block text-xs font-medium text-[var(--color-muted)] mb-1">Enter OTP</label>
                <input
                  autoFocus
                  required
                  inputMode="numeric"
                  value={otp}
                  onChange={(e) => setOtp(e.target.value.replace(/\D/g, ''))}
                  placeholder="6-digit code"
                  className="w-full rounded-lg border border-[var(--color-line)] px-3 py-2.5 text-center tracking-[0.5em] text-lg focus:outline-none focus:ring-2 focus:ring-[var(--color-primary)]/30 focus:border-[var(--color-primary)]"
                />
              </div>
              {otpError && <p className="text-sm text-[var(--color-danger)]">{otpError}</p>}
              <button
                type="submit"
                disabled={status === 'loading'}
                className="w-full rounded-lg bg-[var(--color-primary)] hover:bg-[var(--color-primary-dark)] text-white font-medium py-2.5 text-sm transition-colors disabled:opacity-60"
              >
                {status === 'loading' ? 'Verifying…' : 'Verify & log in'}
              </button>
              <button
                type="button"
                onClick={sendLoginOtp}
                className="w-full text-center text-xs text-[var(--color-muted)] hover:text-[var(--color-primary)]"
              >
                Resend OTP
              </button>
            </form>
          )}
        </div>

        <p className="text-center text-sm text-[var(--color-muted)] mt-6">
          New to PayFlow?{' '}
          <Link to="/register" className="text-[var(--color-primary)] font-medium">
            Create an account
          </Link>
        </p>
      </div>
    </div>
  )
}
