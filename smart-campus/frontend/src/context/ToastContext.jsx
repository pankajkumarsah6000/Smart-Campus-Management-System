import { createContext, useCallback, useContext, useState } from 'react';
import { CheckCircle2, XCircle, Info, X } from 'lucide-react';

const ToastContext = createContext(null);

let idCounter = 0;

export function ToastProvider({ children }) {
  const [toasts, setToasts] = useState([]);

  const removeToast = useCallback((id) => {
    setToasts((t) => t.filter((toast) => toast.id !== id));
  }, []);

  const showToast = useCallback((message, type = 'info') => {
    const id = ++idCounter;
    setToasts((t) => [...t, { id, message, type }]);
    setTimeout(() => removeToast(id), 4500);
  }, [removeToast]);

  const icons = {
    success: <CheckCircle2 size={18} className="text-[var(--color-success)]" />,
    error: <XCircle size={18} className="text-[var(--color-danger)]" />,
    info: <Info size={18} className="text-[var(--color-info)]" />,
  };

  return (
    <ToastContext.Provider value={{ showToast }}>
      {children}
      <div className="fixed bottom-4 right-4 z-50 flex flex-col gap-2 w-80">
        {toasts.map((t) => (
          <div
            key={t.id}
            role="status"
            className="flex items-start gap-2 rounded-lg border border-black/5 bg-white dark:bg-navy-800 dark:border-white/10 shadow-lg px-4 py-3 text-sm animate-[fadeIn_0.15s_ease-out]"
          >
            {icons[t.type]}
            <p className="flex-1 text-[var(--color-ink)] dark:text-white">{t.message}</p>
            <button onClick={() => removeToast(t.id)} aria-label="Dismiss notification" className="text-ink-muted hover:text-ink">
              <X size={14} />
            </button>
          </div>
        ))}
      </div>
    </ToastContext.Provider>
  );
}

export function useToast() {
  const ctx = useContext(ToastContext);
  if (!ctx) throw new Error('useToast must be used within ToastProvider');
  return ctx;
}
