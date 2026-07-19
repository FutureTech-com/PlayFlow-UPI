import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useDispatch } from 'react-redux'
import { complaintApi } from '../api/complaints'
import { pushToast } from '../slices/toastSlice'
import { ArrowLeft, Plus, X } from 'lucide-react'

const STATUS_COLOR = {
  OPEN: 'text-[var(--color-accent)]',
  IN_REVIEW: 'text-[var(--color-primary)]',
  RESOLVED: 'text-[var(--color-success)]',
  REJECTED: 'text-[var(--color-danger)]',
}

export default function Complaints() {
  const [list, setList] = useState([])
  const [showForm, setShowForm] = useState(false)
  const [subject, setSubject] = useState('')
  const [description, setDescription] = useState('')
  const dispatch = useDispatch()
  const navigate = useNavigate()

  const load = () => complaintApi.mine().then(setList)
  useEffect(() => { load() }, [])

  const handleSubmit = async (e) => {
    e.preventDefault()
    await complaintApi.create({ subject, description })
    setSubject('')
    setDescription('')
    setShowForm(false)
    load()
    dispatch(pushToast('Complaint submitted', 'success'))
  }

  return (
    <div className="max-w-sm mx-auto">
      <button onClick={() => navigate(-1)} className="flex items-center gap-1 text-sm text-[var(--color-muted)] mb-4">
        <ArrowLeft size={16} /> Back
      </button>
      <div className="flex items-center justify-between mb-4">
        <h1 className="font-display text-xl font-semibold">Help & complaints</h1>
        <button onClick={() => setShowForm((v) => !v)} className="text-sm font-medium text-[var(--color-primary)] flex items-center gap-1">
          {showForm ? <X size={16} /> : <Plus size={16} />} {showForm ? 'Cancel' : 'New'}
        </button>
      </div>

      {showForm && (
        <form onSubmit={handleSubmit} className="bg-[var(--color-surface)] border border-[var(--color-line)] rounded-2xl p-4 space-y-3 mb-4">
          <input required placeholder="Subject" value={subject} onChange={(e) => setSubject(e.target.value)}
            className="w-full rounded-lg border border-[var(--color-line)] px-3 py-2 text-sm" />
          <textarea required placeholder="Describe the issue…" value={description} onChange={(e) => setDescription(e.target.value)}
            rows={4} className="w-full rounded-lg border border-[var(--color-line)] px-3 py-2 text-sm" />
          <button type="submit" className="rounded-lg bg-[var(--color-primary)] text-white text-sm font-medium px-4 py-2">Submit</button>
        </form>
      )}

      <div className="bg-[var(--color-surface)] border border-[var(--color-line)] rounded-2xl">
        {list.length === 0 ? (
          <p className="px-5 py-8 text-center text-sm text-[var(--color-muted)]">No complaints raised.</p>
        ) : (
          <ul className="divide-y divide-[var(--color-line)]">
            {list.map((c) => (
              <li key={c.id} className="px-4 py-3">
                <div className="flex items-center justify-between">
                  <p className="text-sm font-medium">{c.subject}</p>
                  <span className={`text-xs font-medium ${STATUS_COLOR[c.status]}`}>{c.status}</span>
                </div>
                <p className="text-xs text-[var(--color-muted)] mt-1">{c.description}</p>
                {c.resolutionNote && <p className="text-xs text-[var(--color-primary)] mt-1">Resolution: {c.resolutionNote}</p>}
              </li>
            ))}
          </ul>
        )}
      </div>
    </div>
  )
}
