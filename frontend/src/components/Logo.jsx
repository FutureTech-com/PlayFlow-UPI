export default function Logo({ className = '' }) {
  return (
    <div className={`flex items-center gap-2 font-display font-semibold ${className}`}>
      <svg width="28" height="28" viewBox="0 0 28 28" fill="none" xmlns="http://www.w3.org/2000/svg">
        <rect width="28" height="28" rx="8" fill="#0E5F4E" />
        <path d="M8 19V9h5.2a3.5 3.5 0 1 1 0 7H10v3H8Zm2-5h3.1a1.5 1.5 0 0 0 0-3H10v3Z" fill="#FF7A45" />
      </svg>
      <span>PayFlow</span>
    </div>
  )
}
