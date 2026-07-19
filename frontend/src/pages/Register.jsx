import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useDispatch, useSelector } from 'react-redux'
import { registerUser, clearAuthError } from '../slices/authSlice'
import { pushToast } from '../slices/toastSlice'
import Logo from '../components/Logo'

export default function Register() {
  const [form, setForm] = useState({ fullName: '', email: '', phone: '', password: '' })
  const dispatch = useDispatch()
  const navigate = useNavigate()
  const { status, error } = useSelector((s) => s.auth)

  const handleChange = (e) => setForm((f) => ({ ...f, [e.target.name]: e.target.value }))

  const handleSubmit = async (e) => {
    e.preventDefault()
    dispatch(clearAuthError())
    const result = await dispatch(registerUser(form))
    if (registerUser.fulfilled.match(result)) {
      dispatch(pushToast('Account created. Welcome to PayFlow!', 'success'))
      navigate('/home')
    }
  }

  return (
    <div className="min-h-screen flex items-center justify-center bg-[var(--color-paper)] px-4 py-10">
      <div className="w-full max-w-sm">
        <div className="flex justify-center mb-8">
          <Logo className="text-[var(--color-ink)] text-xl" />
        </div>

        <div className="bg-[var(--color-surface)] rounded-2xl shadow-sm border border-[var(--color-line)] p-6">
          <h1 className="font-display text-xl font-semibold mb-1">Create your account</h1>
          <p className="text-sm text-[var(--color-muted)] mb-6">Takes less than a minute.</p>

          <form onSubmit={handleSubmit} className="space-y-4">
            <div>
              <label className="block text-xs font-medium text-[var(--color-muted)] mb-1">Full name</label>
              <input
                name="fullName"
                required
                value={form.fullName}
                onChange={handleChange}
                placeholder="Rahul Sharma"
                className="w-full rounded-lg border border-[var(--color-line)] px-3 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-[var(--color-primary)]/30 focus:border-[var(--color-primary)]"
              />
            </div>
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
              <label className="block text-xs font-medium text-[var(--color-muted)] mb-1">Mobile number</label>
              <input
                name="phone"
                required
                pattern="[6-9][0-9]{9}"
                title="10-digit Indian mobile number"
                value={form.phone}
                onChange={handleChange}
                placeholder="9876543210"
                className="w-full rounded-lg border border-[var(--color-line)] px-3 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-[var(--color-primary)]/30 focus:border-[var(--color-primary)]"
              />
            </div>
            <div>
              <label className="block text-xs font-medium text-[var(--color-muted)] mb-1">Password</label>
              <input
                type="password"
                name="password"
                required
                minLength={8}
                value={form.password}
                onChange={handleChange}
                placeholder="At least 8 characters"
                className="w-full rounded-lg border border-[var(--color-line)] px-3 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-[var(--color-primary)]/30 focus:border-[var(--color-primary)]"
              />
            </div>

            {error && <p className="text-sm text-[var(--color-danger)]">{error}</p>}

            <button
              type="submit"
              disabled={status === 'loading'}
              className="w-full rounded-lg bg-[var(--color-primary)] hover:bg-[var(--color-primary-dark)] text-white font-medium py-2.5 text-sm transition-colors disabled:opacity-60"
            >
              {status === 'loading' ? 'Creating account…' : 'Create account'}
            </button>
          </form>
        </div>

        <p className="text-center text-sm text-[var(--color-muted)] mt-6">
          Already have an account?{' '}
          <Link to="/login" className="text-[var(--color-primary)] font-medium">
            Log in
          </Link>
        </p>
      </div>
    </div>
  )
}
