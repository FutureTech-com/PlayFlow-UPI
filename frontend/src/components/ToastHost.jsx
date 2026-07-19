import { useDispatch, useSelector } from 'react-redux'
import { dismissToast } from '../slices/toastSlice'
import { CheckCircle2, XCircle, Info, X } from 'lucide-react'

const VARIANT_STYLES = {
  success: { icon: CheckCircle2, className: 'bg-white border-l-4 border-[var(--color-success)] text-[var(--color-ink)]' },
  error: { icon: XCircle, className: 'bg-white border-l-4 border-[var(--color-danger)] text-[var(--color-ink)]' },
  info: { icon: Info, className: 'bg-white border-l-4 border-[var(--color-primary)] text-[var(--color-ink)]' },
}

export default function ToastHost() {
  const items = useSelector((s) => s.toast.items)
  const dispatch = useDispatch()

  if (!items.length) return null

  return (
    <div className="fixed top-4 right-4 z-50 flex flex-col gap-2 w-[calc(100%-2rem)] max-w-sm">
      {items.map((t) => {
        const cfg = VARIANT_STYLES[t.variant] || VARIANT_STYLES.info
        const Icon = cfg.icon
        return (
          <div key={t.id} className={`flex items-start gap-2 rounded-lg shadow-lg px-4 py-3 ${cfg.className}`}>
            <Icon size={18} className="mt-0.5 shrink-0" />
            <p className="text-sm flex-1">{t.message}</p>
            <button onClick={() => dispatch(dismissToast(t.id))} className="text-slate-400 hover:text-slate-600">
              <X size={16} />
            </button>
          </div>
        )
      })}
    </div>
  )
}
