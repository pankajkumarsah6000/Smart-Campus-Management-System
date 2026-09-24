import { X } from 'lucide-react';
import { useEffect } from 'react';

export default function Modal({ open, onClose, title, children, footer }) {
  useEffect(() => {
    if (!open) return;
    const onKey = (e) => e.key === 'Escape' && onClose();
    window.addEventListener('keydown', onKey);
    return () => window.removeEventListener('keydown', onKey);
  }, [open, onClose]);

  if (!open) return null;

  return (
    <div className="fixed inset-0 z-40 flex items-center justify-center p-4">
      <div className="absolute inset-0 bg-navy-950/40 backdrop-blur-sm" onClick={onClose} aria-hidden="true" />
      <div
        role="dialog"
        aria-modal="true"
        aria-label={title}
        className="relative w-full max-w-lg rounded-xl bg-white dark:bg-navy-800 shadow-2xl max-h-[90vh] overflow-y-auto"
      >
        <div className="flex items-center justify-between px-5 py-4 border-b border-black/8 dark:border-white/10">
          <h2 className="font-display text-lg text-ink dark:text-white">{title}</h2>
          <button onClick={onClose} aria-label="Close dialog" className="p-1 rounded hover:bg-navy-900/5 dark:hover:bg-white/5">
            <X size={18} />
          </button>
        </div>
        <div className="px-5 py-4">{children}</div>
        {footer && <div className="px-5 py-4 border-t border-black/8 dark:border-white/10 flex justify-end gap-2">{footer}</div>}
      </div>
    </div>
  );
}
