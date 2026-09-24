import { useEffect, useState } from 'react';
import Badge from '../../components/Badge';
import LoadingState from '../../components/LoadingState';
import ErrorState from '../../components/ErrorState';
import EmptyState from '../../components/EmptyState';
import { leaveService } from '../../services/leaveService';
import { useToast } from '../../context/ToastContext';

export default function LeaveApprovals() {
  const [requests, setRequests] = useState([]);
  const [status, setStatus] = useState('loading');
  const [busyId, setBusyId] = useState(null);
  const { showToast } = useToast();

  const load = () => {
    setStatus('loading');
    leaveService.pending().then((r) => { setRequests(r); setStatus('ready'); }).catch(() => setStatus('error'));
  };
  useEffect(load, []);

  const review = async (id, decision) => {
    setBusyId(id);
    try {
      await leaveService.review(id, { status: decision });
      showToast(`Leave request ${decision.toLowerCase()}`, 'success');
      load();
    } catch {
      showToast('Could not update request', 'error');
    } finally {
      setBusyId(null);
    }
  };

  if (status === 'loading') return <LoadingState label="Loading leave requests" />;
  if (status === 'error') return <ErrorState onRetry={load} />;

  return (
    <div>
      <div className="mb-6">
        <p className="ledger-index uppercase text-ink-muted">Admin · Leave Requests</p>
        <h1 className="font-display text-2xl text-ink dark:text-white">Pending approvals</h1>
      </div>

      {requests.length === 0 ? (
        <div className="rounded-xl border border-black/8 dark:border-white/10 bg-white dark:bg-navy-800">
          <EmptyState message="No pending leave requests" />
        </div>
      ) : (
        <div className="space-y-3">
          {requests.map((r) => (
            <div key={r.id} className="rounded-xl border border-black/8 dark:border-white/10 bg-white dark:bg-navy-800 p-4">
              <div className="flex items-start justify-between gap-3">
                <div>
                  <p className="text-sm font-medium text-ink dark:text-white">{r.requestedByName}</p>
                  <p className="text-xs text-ink-muted">{r.fromDate} → {r.toDate}</p>
                  <p className="text-sm text-ink-muted mt-1">{r.reason}</p>
                </div>
                <Badge tone="warning">PENDING</Badge>
              </div>
              <div className="flex gap-2 mt-3">
                <button
                  disabled={busyId === r.id}
                  onClick={() => review(r.id, 'APPROVED')}
                  className="px-3 py-1.5 rounded-lg bg-[var(--color-success)] text-white text-xs font-medium hover:opacity-90 disabled:opacity-60"
                >
                  Approve
                </button>
                <button
                  disabled={busyId === r.id}
                  onClick={() => review(r.id, 'REJECTED')}
                  className="px-3 py-1.5 rounded-lg bg-[var(--color-danger)] text-white text-xs font-medium hover:opacity-90 disabled:opacity-60"
                >
                  Reject
                </button>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
