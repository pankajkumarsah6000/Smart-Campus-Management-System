export default function StatCard({ label, value, sublabel, icon: Icon, accent = 'navy' }) {
  const accentMap = {
    navy: 'text-navy-900 dark:text-white',
    success: 'text-[var(--color-success)]',
    warning: 'text-[var(--color-warning)]',
    danger: 'text-[var(--color-danger)]',
  };
  return (
    <div className="stat-card rounded-xl p-5 shadow-sm">
      <div className="flex items-start justify-between">
        <div>
          <p className="ledger-index uppercase text-ink-muted dark:text-white/50">{label}</p>
          <p className={`font-mono text-3xl font-medium mt-2 ${accentMap[accent]}`}>{value}</p>
          {sublabel && <p className="text-xs text-ink-muted dark:text-white/50 mt-1">{sublabel}</p>}
        </div>
        {Icon && (
          <div className="rounded-lg bg-navy-900/5 dark:bg-white/5 p-2">
            <Icon size={18} className="text-navy-900 dark:text-white/70" />
          </div>
        )}
      </div>
    </div>
  );
}
