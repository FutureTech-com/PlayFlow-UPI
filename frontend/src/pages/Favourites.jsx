import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useDispatch } from 'react-redux'
import { beneficiaryApi } from '../api/beneficiaries'
import { pushToast } from '../slices/toastSlice'
import { ArrowLeft, Star, Trash2, Plus, User } from 'lucide-react'

export default function Favourites() {
  const [list, setList] = useState([])
  const [showForm, setShowForm] = useState(false)
  const [nickname, setNickname] = useState('')
  const [vpa, setVpa] = useState('')
  const [error, setError] = useState('')
  const dispatch = useDispatch()
  const navigate = useNavigate()

  const load = () => beneficiaryApi.list().then(setList)
  useEffect(() => { load() }, [])

  const handleAdd = async (e) => {
    e.preventDefault()
    setError('')
    try {
      await beneficiaryApi.add({ nickname, vpa })
      setNickname('')
      setVpa('')
      setShowForm(false)
      load()
      dispatch(pushToast('Favourite added', 'success'))
    } catch (err) {
      setError(err?.response?.data?.message || 'Could not add favourite')
    }
  }

  const toggleFav = (id) => beneficiaryApi.toggleFavourite(id).then(load)
  const remove = (id) => beneficiaryApi.remove(id).then(load)

  return (
    <div className="max-w-sm mx-auto">
      <button onClick={() => navigate(-1)} className="flex items-center gap-1 text-sm text-[var(--color-muted)] mb-4">
        <ArrowLeft size={16} /> Back
      </button>
      <div className="flex items-center justify-between mb-4">
        <h1 className="font-display text-xl font-semibold">Favourites</h1>
        <button onClick={() => setShowForm((v) => !v)} className="text-sm font-medium text-[var(--color-primary)] flex items-center gap-1">
          <Plus size={16} /> Add
        </button>
      </div>

      {showForm && (
        <form onSubmit={handleAdd} className="bg-[var(--color-surface)] border border-[var(--color-line)] rounded-2xl p-4 space-y-3 mb-4">
          <input required placeholder="Nickname" value={nickname} onChange={(e) => setNickname(e.target.value)}
            className="w-full rounded-lg border border-[var(--color-line)] px-3 py-2 text-sm" />
          <input required placeholder="name@payflow" value={vpa} onChange={(e) => setVpa(e.target.value)}
            className="w-full rounded-lg border border-[var(--color-line)] px-3 py-2 text-sm" />
          {error && <p className="text-xs text-[var(--color-danger)]">{error}</p>}
          <button type="submit" className="rounded-lg bg-[var(--color-primary)] text-white text-sm font-medium px-4 py-2">Save</button>
        </form>
      )}

      <div className="bg-[var(--color-surface)] border border-[var(--color-line)] rounded-2xl">
        {list.length === 0 ? (
          <p className="px-5 py-8 text-center text-sm text-[var(--color-muted)]">No favourites yet.</p>
        ) : (
          <ul className="divide-y divide-[var(--color-line)]">
            {list.map((b) => (
              <li key={b.id} className="flex items-center gap-3 px-4 py-3">
                <div className="w-9 h-9 rounded-full bg-[var(--color-primary)]/10 flex items-center justify-center text-[var(--color-primary)]">
                  <User size={16} />
                </div>
                <div className="flex-1 min-w-0">
                  <p className="text-sm font-medium truncate">{b.nickname}</p>
                  <p className="text-xs text-[var(--color-muted)] truncate">{b.vpa}</p>
                </div>
                <button onClick={() => toggleFav(b.id)} className={b.favourite ? 'text-[var(--color-accent)]' : 'text-[var(--color-muted)]'}>
                  <Star size={16} fill={b.favourite ? 'currentColor' : 'none'} />
                </button>
                <button onClick={() => remove(b.id)} className="text-[var(--color-muted)] hover:text-[var(--color-danger)]">
                  <Trash2 size={16} />
                </button>
              </li>
            ))}
          </ul>
        )}
      </div>
    </div>
  )
}
