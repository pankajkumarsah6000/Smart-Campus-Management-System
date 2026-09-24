import { useEffect, useState } from 'react';
import { Wallet } from 'lucide-react';
import Badge from '../../components/Badge';
import LoadingState from '../../components/LoadingState';
import ErrorState from '../../components/ErrorState';
import EmptyState from '../../components/EmptyState';
import { feeService } from '../../services/feeService';

const statusTone = { PAID: 'success', PARTIAL: 'warning', PENDING: 'info', OVERDUE: 'danger' };
const fmt = (n) => (n == null ? '—' : `₹${Number(n).toLocaleString('en-IN', { maximumFractionDigits: 2 })}`);

export default function Fees() {
  const [fees, setFees] = useState([]);
  const [status, setStatus] = useState('loading');

  const load = () => {
    setStatus('loading');
    feeService.myFees().then((d) => { setFees(d || []); setStatus('ready'); }).catch(() => setStatus('error'));
  };
  useEffect(load, []);

  if (status === 'loading') return <LoadingState label="Loading fee records" />;
  if (status === 'error') return <ErrorState onRetry={load} />;

  const due = fees.reduce((sum, f) => sum + Number(f.balance || 0), 0);

  return (
    <div>
      <div className="mb-6">
        <p className="ledger-index uppercase text-ink-muted">Student · Fees</p>
        <h1 className="font-display text-2xl text-ink dark:text-white">Fee records</h1>
      </div>

      <div className="stat-card rounded-xl p-5 mb-6 max-w-xs">
        <p className="ledger-index uppercase text-ink-muted">Total outstanding</p>
        <p className="font-mono text-3xl mt-2 text-[var(--color-danger)]">{fmt(due)}</p>
      </div>

      {fees.length === 0 ? (
        <div className="rounded-xl border border-black/8 dark:border-white/10 bg-white dark:bg-navy-800">
          <EmptyState message="No fee records yet" />
        </div>
      ) : (
        <div className="space-y-3">
          {fees.map((f) => (
            <div key={f.id} className="rounded-xl border border-black/8 dark:border-white/10 bg-white dark:bg-navy-800 p-4">
              <div className="flex items-start justify-between gap-3">
                <div>
                  <div className="flex items-center gap-2 mb-1">
                    <Wallet size={15} className="text-ink-muted" />
                    <h3 className="font-medium text-ink dark:text-white">{f.feeType || 'Fee'}</h3>
                    <Badge tone={statusTone[f.status] || 'neutral'}>{f.status}</Badge>
                  </div>
                  <p className="text-xs text-ink-muted">
                    Semester {f.semester || '—'} · Due {f.dueDate || '—'}
                    {f.paidDate ? ` · Paid ${f.paidDate}` : ''}
                  </p>
                </div>
                <div className="text-right font-mono text-sm shrink-0">
                  <p className="text-ink dark:text-white">{fmt(f.amount)}</p>
                  <p className="text-xs text-ink-muted">Paid {fmt(f.amountPaid)}</p>
                  <p className={`text-xs ${Number(f.balance) > 0 ? 'text-[var(--color-danger)]' : 'text-[var(--color-success)]'}`}>
                    Balance {fmt(f.balance)}
                  </p>
                </div>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}