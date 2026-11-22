export default function FormField({ label, error, children }) {
  return (
    <label className="block mb-4">
      <span className="block text-sm font-medium text-ink dark:text-white mb-1.5">{label}</span>
      {children}
      {error && <span className="block text-xs text-[var(--color-danger)] mt-1">{error}</span>}
    </label>
  );
}
