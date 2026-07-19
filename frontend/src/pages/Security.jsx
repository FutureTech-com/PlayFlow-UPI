import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useDispatch } from 'react-redux'
import { deviceApi } from '../api/devices'
import { userApi } from '../api/user'
import { pushToast } from '../slices/toastSlice'
import { ArrowLeft, Smartphone, ShieldCheck, Trash2, Send } from 'lucide-react'

export default function Security() {
  const [devices, setDevices] = useState([])
  const [phoneStep, setPhoneStep] = useState('idle') // idle | sent
  const [otp, setOtp] = useState('')
  const dispatch = useDispatch()
  const navigate = useNavigate()

  const load = () => deviceApi.list().then(setDevices)
  useEffect(() => { load() }, [])

  const sendOtp = async () => {
    try {
      await userApi.sendPhoneOtp()
      setPhoneStep('sent')
      dispatch(pushToast('OTP sent (check server console in this demo)', 'success'))
    } catch (err) {
      dispatch(pushToast(err?.response?.data?.message || 'Could not send OTP', 'error'))
    }
  }

  const verifyOtp = async () => {
    try {
      await userApi.verifyPhoneOtp(otp)
      dispatch(pushToast('Phone number verified', 'success'))
      setPhoneStep('idle')
      setOtp('')
    } catch (err) {
      dispatch(pushToast(err?.response?.data?.message || 'Invalid OTP', 'error'))
    }
  }

  const trustDevice = (id) => deviceApi.trust(id).then(load)
  const removeDevice = (id) => deviceApi.remove(id).then(load)

  return (
    <div className="max-w-sm mx-auto space-y-6">
      <button onClick={() => navigate(-1)} className="flex items-center gap-1 text-sm text-[var(--color-muted)]">
        <ArrowLeft size={16} /> Back
      </button>
      <h1 className="font-display text-xl font-semibold">Security & devices</h1>

      <div className="bg-[var(--color-surface)] border border-[var(--color-line)] rounded-2xl p-5 space-y-3">
        <p className="text-sm font-medium">Mobile number verification</p>
        {phoneStep === 'idle' ? (
          <button onClick={sendOtp} className="flex items-center gap-2 text-sm font-medium text-[var(--color-primary)]">
            <Send size={14} /> Send verification OTP
          </button>
        ) : (
          <div className="flex gap-2">
            <input autoFocus inputMode="numeric" placeholder="Enter OTP" value={otp} onChange={(e) => setOtp(e.target.value.replace(/\D/g, ''))}
              className="flex-1 rounded-lg border border-[var(--color-line)] px-3 py-2 text-sm tracking-widest" />
            <button onClick={verifyOtp} className="rounded-lg bg-[var(--color-primary)] text-white text-sm font-medium px-4">Verify</button>
          </div>
        )}
      </div>

      <div>
        <p className="text-sm font-medium mb-3">Devices that have logged in</p>
        <div className="bg-[var(--color-surface)] border border-[var(--color-line)] rounded-2xl">
          {devices.length === 0 ? (
            <p className="px-5 py-6 text-sm text-[var(--color-muted)]">No device history yet.</p>
          ) : (
            <ul className="divide-y divide-[var(--color-line)]">
              {devices.map((d) => (
                <li key={d.id} className="flex items-center gap-3 px-4 py-3">
                  <div className="w-9 h-9 rounded-full bg-[var(--color-primary)]/10 flex items-center justify-center text-[var(--color-primary)]">
                    <Smartphone size={16} />
                  </div>
                  <div className="flex-1 min-w-0">
                    <p className="text-sm font-medium truncate">{d.deviceLabel}</p>
                    <p className="text-xs text-[var(--color-muted)]">
                      {d.lastIp} · {new Date(d.lastSeenAt).toLocaleString('en-IN', { dateStyle: 'medium', timeStyle: 'short' })}
                    </p>
                  </div>
                  {!d.trusted && (
                    <button onClick={() => trustDevice(d.id)} title="Mark trusted" className="text-[var(--color-muted)] hover:text-[var(--color-success)]">
                      <ShieldCheck size={16} />
                    </button>
                  )}
                  <button onClick={() => removeDevice(d.id)} title="Remove" className="text-[var(--color-muted)] hover:text-[var(--color-danger)]">
                    <Trash2 size={16} />
                  </button>
                </li>
              ))}
            </ul>
          )}
        </div>
      </div>
    </div>
  )
}
