import { useEffect, useState } from 'react';
import { Plus } from 'lucide-react';
import Modal from '../../components/Modal';
import FormField from '../../components/FormField';
import Badge from '../../components/Badge';
import LoadingState from '../../components/LoadingState';
import ErrorState from '../../components/ErrorState';
import EmptyState from '../../components/EmptyState';
import { leaveService } from '../../services/leaveService';
import { useToast } from '../../context/ToastContext';

const statusTone = { PENDING: 'warning', APPROVED: 'success', REJECTED: 'danger' };
const emptyForm = { fromDate: '', toDate: '', reason: '' };

export default function LeaveApplication() {
  const [requests, setRequests] = useState([]);
  const [status, setStatus] = useState('loading');
  const [modalOpen, setModalOpen] = useState(false);
  const [form, setForm] = useState(emptyForm);
  const [saving, setSaving] = useState(false);
  const { showToast } = useToast();

  const load = () => {
    setStatus('loading');
    leaveService.myRequests().then((r) => { setRequests(r); setStatus('ready'); }).catch(() => setStatus('error'));
  };
  useEffect(load, []);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setSaving(true);
    try {
      await leaveService.apply(form);
      showToast('Leave request submitted', 'success');
      setModalOpen(false);
      setForm(emptyForm);
      load();
    } catch (err) {
      showToast(err.response?.data?.message || 'Could not submit request', 'error');
    } finally {
      setSaving(false);
    }
  };

  if (status === 'loading') return <LoadingState label="Loading leave requests" />;
  if (status === 'error') return <ErrorState onRetry={load} />;

  return (
    <div>
      <div className="flex items-center justify-between mb-6">
        <div>
          <p className="ledger-index uppercase text-ink-muted">Student · Leave</p>
          <h1 className="font-display text-2xl text-ink dark:text-white">Leave applications</h1>
        </div>
        <button onClick={() => setModalOpen(true)} className="flex items-center gap-2 rounded-lg bg-navy-900 text-white px-4 py-2 text-sm font-medium hover:bg-navy-800">
          <Plus size={16} /> Apply for leave
        </button>
      </div>

      {requests.length === 0 ? (
        <div className="rounded-xl border border-black/8 dark:border-white/10 bg-white dark:bg-navy-800">
          <EmptyState message="No leave requests yet" />
        </div>
      ) : (
        <div className="space-y-3">
          {requests.map((r) => (
            <div key={r.id} className="rounded-xl border border-black/8 dark:border-white/10 bg-white dark:bg-navy-800 p-4">
              <div className="flex items-start justify-between">
                <div>
                  <p className="text-sm font-medium text-ink dark:text-white">{r.fromDate} → {r.toDate}</p>
                  <p className="text-sm text-ink-muted mt-1">{r.reason}</p>
                  {r.reviewComment && <p className="text-xs text-ink-muted mt-1">Reviewer note: {r.reviewComment}</p>}
                </div>
                <Badge tone={statusTone[r.status]}>{r.status}</Badge>
              </div>
            </div>
          ))}
        </div>
      )}

      <Modal
        open={modalOpen}
        onClose={() => setModalOpen(false)}
        title="Apply for leave"
        footer={
          <>
            <button onClick={() => setModalOpen(false)} className="px-4 py-2 text-sm rounded-lg hover:bg-navy-900/5">Cancel</button>
            <button form="leave-form" type="submit" disabled={saving} className="px-4 py-2 text-sm rounded-lg bg-navy-900 text-white hover:bg-navy-800 disabled:opacity-60">
              {saving ? 'Submitting…' : 'Submit'}
            </button>
          </>
        }
      >
        <form id="leave-form" onSubmit={handleSubmit}>
          <div className="grid grid-cols-2 gap-3">
            <FormField label="From">
              <input required type="date" value={form.fromDate} onChange={(e) => setForm({ ...form, fromDate: e.target.value })} className="w-full rounded-lg border border-black/10 px-3 py-2 text-sm outline-none focus:border-navy-700" />
            </FormField>
            <FormField label="To">
              <input required type="date" value={form.toDate} onChange={(e) => setForm({ ...form, toDate: e.target.value })} className="w-full rounded-lg border border-black/10 px-3 py-2 text-sm outline-none focus:border-navy-700" />
            </FormField>
          </div>
          <FormField label="Reason">
            <textarea required rows={3} value={form.reason} onChange={(e) => setForm({ ...form, reason: e.target.value })} className="w-full rounded-lg border border-black/10 px-3 py-2 text-sm outline-none focus:border-navy-700" />
          </FormField>
        </form>
      </Modal>
    </div>
  );
}
