import { AlertTriangle } from 'lucide-react';

export default function ErrorState({ message = 'Something went wrong. Please try again.', onRetry }) {
  return (
    <div className="flex flex-col items-center justify-center py-14 text-center px-6">
      <AlertTriangle size={26} className="text-[var(--color-danger)] mb-3" />
      <p className="text-sm font-medium text-ink dark:text-white">{message}</p>
      {onRetry && (
        <button onClick={onRetry} className="mt-3 text-sm px-3 py-1.5 rounded-lg bg-navy-900 text-white hover:bg-navy-800">
          Try again
        </button>
      )}
    </div>
  );
}
