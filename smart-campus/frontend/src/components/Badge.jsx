const styles = {
  success: 'bg-[var(--color-success-soft)] text-[var(--color-success)]',
  danger: 'bg-[var(--color-danger-soft)] text-[var(--color-danger)]',
  warning: 'bg-[var(--color-warning-soft)] text-[var(--color-warning)]',
  info: 'bg-[var(--color-info-soft)] text-[var(--color-info)]',
  neutral: 'bg-navy-900/5 dark:bg-white/10 text-ink-muted dark:text-white/70',
};

export default function Badge({ children, tone = 'neutral' }) {
  return (
    <span className={`inline-flex items-center px-2 py-0.5 rounded-full text-xs font-medium ${styles[tone]}`}>
      {children}
    </span>
  );
}
