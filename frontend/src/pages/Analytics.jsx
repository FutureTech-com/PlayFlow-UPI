import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { BarChart, Bar, XAxis, YAxis, Tooltip, ResponsiveContainer, CartesianGrid, PieChart, Pie, Cell } from 'recharts'
import { analyticsApi } from '../api/analytics'
import { aiApi } from '../api/ai'
import { ArrowLeft, Sparkles, PiggyBank } from 'lucide-react'

const PIE_COLORS = ['#0E5F4E', '#FF7A45', '#4A86E8', '#F2C960', '#8E63CE', '#E66550', '#44B984', '#994A64', '#0D3472', '#7A2E0B']

export default function Analytics() {
  const [data, setData] = useState(null)
  const [insights, setInsights] = useState(null)
  const [budget, setBudget] = useState(null)
  const navigate = useNavigate()

  useEffect(() => {
    analyticsApi.spending().then(setData)
    aiApi.insights().then(setInsights)
    aiApi.budgetSuggestions().then(setBudget)
  }, [])

  return (
    <div className="max-w-2xl mx-auto">
      <button onClick={() => navigate(-1)} className="flex items-center gap-1 text-sm text-[var(--color-muted)] mb-4">
        <ArrowLeft size={16} /> Back
      </button>
      <h1 className="font-display text-xl font-semibold mb-1">Spending insights</h1>
      <p className="text-xs text-[var(--color-muted)] mb-6 flex items-center gap-1">
        <Sparkles size={12} /> AI-powered categorization &amp; suggestions, generated from your own transaction history
      </p>

      <div className="space-y-6">
        {/* AI narrative insights */}
        {insights && insights.insights?.length > 0 && (
          <div className="bg-gradient-to-br from-[var(--color-primary)]/10 to-transparent border border-[var(--color-primary)]/20 rounded-2xl p-5">
            <div className="flex items-center gap-2 mb-3">
              <Sparkles size={16} className="text-[var(--color-primary)]" />
              <p className="text-sm font-semibold">Insights</p>
            </div>
            <ul className="space-y-2">
              {insights.insights.map((line, i) => (
                <li key={i} className="text-sm text-[var(--color-ink)] leading-relaxed">{line}</li>
              ))}
            </ul>
          </div>
        )}

        {data && (
          <div className="grid grid-cols-2 gap-4">
            <div className="bg-[var(--color-surface)] border border-[var(--color-line)] rounded-2xl p-5">
              <p className="text-xs text-[var(--color-muted)]">Spent this month</p>
              <p className="font-mono-amount text-xl font-semibold mt-1">₹{Number(data.totalSpentThisMonth).toLocaleString('en-IN')}</p>
            </div>
            <div className="bg-[var(--color-surface)] border border-[var(--color-line)] rounded-2xl p-5">
              <p className="text-xs text-[var(--color-muted)]">Received this month</p>
              <p className="font-mono-amount text-xl font-semibold mt-1 text-[var(--color-success)]">₹{Number(data.totalReceivedThisMonth).toLocaleString('en-IN')}</p>
            </div>
          </div>
        )}

        {/* Category breakdown pie */}
        {insights && insights.categoryBreakdown?.length > 0 && (
          <div className="bg-[var(--color-surface)] border border-[var(--color-line)] rounded-2xl p-5">
            <p className="text-sm font-medium mb-4">Spending by category (this month)</p>
            <div className="flex flex-col sm:flex-row items-center gap-4">
              <div style={{ width: 180, height: 180 }}>
                <ResponsiveContainer>
                  <PieChart>
                    <Pie data={insights.categoryBreakdown} dataKey="totalSpent" nameKey="category" innerRadius={45} outerRadius={80} paddingAngle={2}>
                      {insights.categoryBreakdown.map((entry, i) => (
                        <Cell key={entry.category} fill={PIE_COLORS[i % PIE_COLORS.length]} />
                      ))}
                    </Pie>
                    <Tooltip formatter={(v) => `₹${Number(v).toLocaleString('en-IN')}`} />
                  </PieChart>
                </ResponsiveContainer>
              </div>
              <ul className="flex-1 w-full space-y-1.5">
                {insights.categoryBreakdown.map((c, i) => (
                  <li key={c.category} className="flex items-center justify-between text-xs">
                    <span className="flex items-center gap-1.5">
                      <span className="w-2.5 h-2.5 rounded-full shrink-0" style={{ background: PIE_COLORS[i % PIE_COLORS.length] }} />
                      {c.category}
                    </span>
                    <span className="font-mono-amount text-[var(--color-muted)]">
                      ₹{Number(c.totalSpent).toLocaleString('en-IN')} · {c.percentOfTotal}%
                    </span>
                  </li>
                ))}
              </ul>
            </div>
          </div>
        )}

        {data && (
          <div className="bg-[var(--color-surface)] border border-[var(--color-line)] rounded-2xl p-5">
            <p className="text-sm font-medium mb-4">Last 6 months</p>
            <div style={{ width: '100%', height: 240 }}>
              <ResponsiveContainer>
                <BarChart data={data.last6Months}>
                  <CartesianGrid strokeDasharray="3 3" stroke="#E3E7ED" />
                  <XAxis dataKey="month" tick={{ fontSize: 11 }} />
                  <YAxis tick={{ fontSize: 11 }} />
                  <Tooltip formatter={(v) => `₹${Number(v).toLocaleString('en-IN')}`} />
                  <Bar dataKey="spent" fill="#FF7A45" radius={[4, 4, 0, 0]} />
                  <Bar dataKey="received" fill="#0E5F4E" radius={[4, 4, 0, 0]} />
                </BarChart>
              </ResponsiveContainer>
            </div>
          </div>
        )}

        {/* Smart budget suggestions */}
        {budget && budget.suggestions?.length > 0 && (
          <div className="bg-[var(--color-surface)] border border-[var(--color-line)] rounded-2xl p-5">
            <div className="flex items-center gap-2 mb-1">
              <PiggyBank size={16} className="text-[var(--color-accent)]" />
              <p className="text-sm font-semibold">Smart budget suggestions</p>
            </div>
            <p className="text-xs text-[var(--color-muted)] mb-4">{budget.basis}</p>
            <ul className="space-y-3">
              {budget.suggestions.map((s) => (
                <li key={s.category}>
                  <div className="flex items-center justify-between text-sm mb-1">
                    <span>{s.category}</span>
                    <span className="font-mono-amount font-medium">₹{Number(s.suggestedMonthlyBudget).toLocaleString('en-IN')}/mo</span>
                  </div>
                  <div className="h-1.5 rounded-full bg-[var(--color-paper)] overflow-hidden">
                    <div
                      className="h-full bg-[var(--color-accent)] rounded-full"
                      style={{
                        width: `${Math.min(100, (Number(s.last3MonthAverage) / Number(s.suggestedMonthlyBudget)) * 100)}%`,
                      }}
                    />
                  </div>
                </li>
              ))}
            </ul>
            <div className="flex items-center justify-between text-sm font-semibold mt-4 pt-3 border-t border-[var(--color-line)]">
              <span>Suggested overall budget</span>
              <span className="font-mono-amount">₹{Number(budget.suggestedOverallMonthlyBudget).toLocaleString('en-IN')}/mo</span>
            </div>
          </div>
        )}

        {data && (
          <div className="bg-[var(--color-surface)] border border-[var(--color-line)] rounded-2xl p-5">
            <p className="text-sm font-medium mb-3">Spending by transaction type</p>
            {Object.keys(data.byType).length === 0 ? (
              <p className="text-sm text-[var(--color-muted)]">No spending yet.</p>
            ) : (
              <ul className="space-y-2">
                {Object.entries(data.byType).map(([type, amount]) => (
                  <li key={type} className="flex items-center justify-between text-sm">
                    <span className="text-[var(--color-muted)] capitalize">{type.replace('_', ' ').toLowerCase()}</span>
                    <span className="font-mono-amount font-medium">₹{Number(amount).toLocaleString('en-IN')}</span>
                  </li>
                ))}
              </ul>
            )}
          </div>
        )}
      </div>
    </div>
  )
}
