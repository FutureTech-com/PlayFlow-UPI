import { useEffect, useState } from 'react'
import { notificationApi } from '../api/notifications'
import { Bell, CheckCheck } from 'lucide-react'

export default function Notifications() {
  const [items, setItems] = useState([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    notificationApi.list().then((data) => {
      setItems(data)
      setLoading(false)
      notificationApi.markAllRead()
    })
  }, [])

  return (
    <div className="max-w-sm space-y-4">
      <div className="flex items-center justify-between">
        <h1 className="font-display text-xl font-semibold">Notifications</h1>
        <CheckCheck size={18} className="text-[var(--color-muted)]" />
      </div>

      <div className="bg-[var(--color-surface)] border border-[var(--color-line)] rounded-2xl">
        {loading ? (
          <p className="px-5 py-6 text-sm text-[var(--color-muted)]">Loading…</p>
        ) : items.length === 0 ? (
          <div className="px-5 py-10 text-center text-sm text-[var(--color-muted)]">
            <Bell size={28} className="mx-auto mb-2" />
            You're all caught up.
          </div>
        ) : (
          <ul className="divide-y divide-[var(--color-line)]">
            {items.map((n) => (
              <li key={n.id} className={`px-5 py-3 ${!n.read ? 'bg-[var(--color-primary)]/5' : ''}`}>
                <p className="text-sm font-medium">{n.title}</p>
                <p className="text-xs text-[var(--color-muted)] mt-0.5">{n.message}</p>
                <p className="text-[10px] text-[var(--color-muted)] mt-1">
                  {new Date(n.createdAt).toLocaleString('en-IN', { dateStyle: 'medium', timeStyle: 'short' })}
                </p>
              </li>
            ))}
          </ul>
        )}
      </div>
    </div>
  )
}
