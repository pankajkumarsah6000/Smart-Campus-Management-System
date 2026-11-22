import { Inbox } from 'lucide-react';

export default function EmptyState({ message = 'Nothing here yet', hint }) {
  return (
    <div className="flex flex-col items-center justify-center py-14 text-center px-6">
      <Inbox size={28} className="text-ink-muted mb-3" />
      <p className="text-sm font-medium text-ink dark:text-white">{message}</p>
      {hint && <p className="text-xs text-ink-muted mt-1 max-w-xs">{hint}</p>}
    </div>
  );
}
