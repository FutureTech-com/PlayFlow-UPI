import { useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { useSelector } from 'react-redux'
import Logo from '../components/Logo'

export default function Splash() {
  const navigate = useNavigate()
  const isAuthenticated = useSelector((s) => s.auth.isAuthenticated)

  useEffect(() => {
    const t = setTimeout(() => {
      navigate(isAuthenticated ? '/home' : '/login', { replace: true })
    }, 1100)
    return () => clearTimeout(t)
  }, [navigate, isAuthenticated])

  return (
    <div className="min-h-screen flex flex-col items-center justify-center bg-[var(--color-ink)] text-white">
      <div className="flex flex-col items-center gap-4 animate-pulse">
        <svg width="64" height="64" viewBox="0 0 28 28" fill="none" xmlns="http://www.w3.org/2000/svg">
          <rect width="28" height="28" rx="8" fill="#0E5F4E" />
          <path d="M8 19V9h5.2a3.5 3.5 0 1 1 0 7H10v3H8Zm2-5h3.1a1.5 1.5 0 0 0 0-3H10v3Z" fill="#FF7A45" />
        </svg>
        <h1 className="font-display text-3xl font-semibold">PayFlow</h1>
        <p className="text-white/60 text-sm tracking-wide">Money, moving simply.</p>
      </div>
    </div>
  )
}
