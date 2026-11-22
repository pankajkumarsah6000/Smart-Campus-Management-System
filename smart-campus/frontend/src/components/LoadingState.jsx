export default function LoadingState({ label = 'Loading' }) {
  return (
    <div className="flex items-center justify-center py-14 gap-2 text-ink-muted" role="status" aria-live="polite">
      <span className="h-4 w-4 rounded-full border-2 border-navy-900/20 dark:border-white/20 border-t-[var(--color-gold)] animate-spin" />
      <span className="text-sm">{label}...</span>
    </div>
  );
}
