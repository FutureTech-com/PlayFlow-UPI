export default function AmountText({ value, sign, className = '' }) {
  const n = Number(value || 0)
  const formatted = n.toLocaleString('en-IN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
  const color =
    sign === '+' ? 'text-[var(--color-success)]' : sign === '-' ? 'text-[var(--color-danger)]' : 'text-[var(--color-ink)]'
  return (
    <span className={`font-mono-amount ${color} ${className}`}>
      {sign ? sign : ''}₹{formatted}
    </span>
  )
}
