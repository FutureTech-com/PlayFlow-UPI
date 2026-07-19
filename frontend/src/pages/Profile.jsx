import { useEffect, useState } from 'react'
import { useDispatch } from 'react-redux'
import { userApi } from '../api/user'
import { pushToast } from '../slices/toastSlice'
import { User, ShieldCheck, Lock, Settings as SettingsIcon } from 'lucide-react'
import { Link } from 'react-router-dom'

export default function Profile() {
  const [profile, setProfile] = useState(null)
  const [fullName, setFullName] = useState('')
  const [saving, setSaving] = useState(false)
  const dispatch = useDispatch()

  useEffect(() => {
    userApi.getProfile().then((p) => {
      setProfile(p)
      setFullName(p.fullName)
    })
  }, [])

  const handleSave = async (e) => {
    e.preventDefault()
    setSaving(true)
    try {
      const updated = await userApi.updateProfile({ fullName })
      setProfile(updated)
      dispatch(pushToast('Profile updated', 'success'))
    } catch (err) {
      dispatch(pushToast(err?.response?.data?.message || 'Update failed', 'error'))
    } finally {
      setSaving(false)
    }
  }

  if (!profile) return <p className="text-sm text-[var(--color-muted)]">Loading…</p>

  return (
    <div className="max-w-sm space-y-6">
      <div className="flex items-center gap-4">
        <div className="w-16 h-16 rounded-full bg-[var(--color-primary)]/10 flex items-center justify-center text-[var(--color-primary)]">
          <User size={28} />
        </div>
        <div>
          <h1 className="font-display text-lg font-semibold">{profile.fullName}</h1>
          <p className="text-sm text-[var(--color-muted)]">{profile.email}</p>
        </div>
      </div>

      <div className="bg-[var(--color-surface)] border border-[var(--color-line)] rounded-2xl p-5 space-y-3">
        <div className="flex items-center justify-between text-sm">
          <span className="text-[var(--color-muted)]">Phone</span>
          <span className="font-medium">{profile.phone}</span>
        </div>
        <div className="flex items-center justify-between text-sm">
          <span className="text-[var(--color-muted)]">Member since</span>
          <span className="font-medium">{new Date(profile.memberSince).toLocaleDateString('en-IN', { dateStyle: 'medium' })}</span>
        </div>
        <div className="flex items-center justify-between text-sm">
          <span className="text-[var(--color-muted)]">Transaction PIN</span>
          <span className={`flex items-center gap-1 font-medium ${profile.pinSet ? 'text-[var(--color-success)]' : 'text-[var(--color-accent)]'}`}>
            <ShieldCheck size={14} /> {profile.pinSet ? 'Set' : 'Not set'}
          </span>
        </div>
      </div>

      <form onSubmit={handleSave} className="bg-[var(--color-surface)] border border-[var(--color-line)] rounded-2xl p-5 space-y-3">
        <label className="block text-xs font-medium text-[var(--color-muted)]">Full name</label>
        <input
          value={fullName}
          onChange={(e) => setFullName(e.target.value)}
          className="w-full rounded-lg border border-[var(--color-line)] px-3 py-2 text-sm"
        />
        <button type="submit" disabled={saving} className="rounded-lg bg-[var(--color-primary)] text-white text-sm font-medium px-4 py-2 disabled:opacity-60">
          {saving ? 'Saving…' : 'Save changes'}
        </button>
      </form>

      <Link to="/settings" className="flex items-center gap-2 text-sm font-medium text-[var(--color-primary)]">
        <SettingsIcon size={16} /> Settings & security
      </Link>
    </div>
  )
}
