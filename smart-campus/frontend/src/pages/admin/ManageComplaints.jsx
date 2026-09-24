import { useEffect, useState } from 'react';
import { CheckCircle2 } from 'lucide-react';
import Modal from '../../components/Modal';
import FormField from '../../components/FormField';
import Badge from '../../components/Badge';
import LoadingState from '../../components/LoadingState';
import ErrorState from '../../components/ErrorState';
import EmptyState from '../../components/EmptyState';
import { complaintService } from '../../services/complaintService';
import { useToast } from '../../context/ToastContext';

const statusTone = { OPEN: 'danger', IN_PROGRESS: 'warning', RESOLVED: 'success', CLOSED: 'neutral' };

export default function ManageComplaints() {
  const [complaints, setComplaints] = useState([]);
  const [status, setStatus] = useState('loading');
  const [resolving, setResolving] = useState(null);
  const [form, setForm] = useState({ status: 'IN_PROGRESS', resolutionNote: '' });
  const [saving, setSaving] = useState(false);
  const { showToast } = useToast();

  const load = () => {
    setStatus('loading');
    complaintService.all().then((d) => { setComplaints(d || []); setStatus('ready'); }).catch(() => setStatus('error'));
  };
  useEffect(load, []);

  const openResolve = (c) => { setResolving(c); setForm({ status: c.status === 'OPEN' ? 'IN_PROGRESS' : c.status, resolutionNote: c.resolutionNote || '' }); };

  const handleResolve = async (e) => {
    e.preventDefault();
    setSaving(true);
    try {
      await complaintService.resolve(resolving.id, form);
      showToast('Complaint updated', 'success');
      setResolving(null);
      load();
    } catch (err) {
      showToast(err.response?.data?.message || 'Could not update complaint', 'error');
    } finally {
      setSaving(false);
    }
  };

  if (status === 'loading') return <LoadingState label="Loading complaints" />;
  if (status === 'error') return <ErrorState onRetry={load} />;

  return (
    <div>
      <div className="mb-6">
        <p className="ledger-index uppercase text-ink-muted">Admin · Complaints</p>
        <h1 className="font-display text-2xl text-ink dark:text-white">All complaints</h1>
      </div>

      {complaints.length === 0 ? (
        <div className="rounded-xl border border-black/8 dark:border-white/10 bg-white dark:bg-navy-800">
          <EmptyState message="No complaints raised" />
        </div>
      ) : (
        <div className="space-y-3">
          {complaints.map((c) => (
            <div key={c.id} className="rounded-xl border border-black/8 dark:border-white/10 bg-white dark:bg-navy-800 p-4">
              <div className="flex items-start justify-between gap-3">
                <div className="min-w-0">
                  <div className="flex flex-wrap items-center gap-2 mb-1">
                    <h3 className="font-medium text-ink dark:text-white">{c.subject}</h3>
                    <Badge tone={statusTone[c.status] || 'neutral'}>{c.status}</Badge>
                    <Badge>{c.category}</Badge>
                  </div>
                  <p className="text-sm text-ink-muted">{c.description}</p>
                  <p className="text-xs text-ink-muted mt-2">
                    {c.raisedByName} ({c.raisedByRole || 'member'}) · {new Date(c.createdAt).toLocaleString()}
                    {c.resolutionNote ? ` · Update: ${c.resolutionNote}` : ''}
                  </p>
                </div>
                {c.status !== 'CLOSED' && (
                  <button onClick={() => openResolve(c)} className="flex shrink-0 items-center gap-2 rounded-lg bg-navy-900 text-white px-3 py-2 text-sm font-medium hover:bg-navy-800">
                    <CheckCircle2 size={14} /> Update
                  </button>
                )}
              </div>
            </div>
          ))}
        </div>
      )}

      <Modal
        open={!!resolving}
        onClose={() => setResolving(null)}
        title={resolving ? `Update — ${resolving.subject}` : 'Update complaint'}
        footer={
          <>
            <button onClick={() => setResolving(null)} className="px-4 py-2 text-sm rounded-lg hover:bg-navy-900/5">Cancel</button>
            <button form="resolve-complaint-form" type="submit" disabled={saving} className="px-4 py-2 text-sm rounded-lg bg-navy-900 text-white hover:bg-navy-800 disabled:opacity-60">
              {saving ? 'Updating…' : 'Update'}
            </button>
          </>
        }
      >
        <form id="resolve-complaint-form" onSubmit={handleResolve}>
          <FormField label="Status">
            <select value={form.status} onChange={(e) => setForm({ ...form, status: e.target.value })} className="w-full rounded-lg border border-black/10 px-3 py-2 text-sm outline-none focus:border-navy-700">
              <option value="IN_PROGRESS">In progress</option>
              <option value="RESOLVED">Resolved</option>
              <option value="CLOSED">Closed</option>
            </select>
          </FormField>
          <FormField label="Resolution note">
            <textarea rows={4} value={form.resolutionNote} onChange={(e) => setForm({ ...form, resolutionNote: e.target.value })} className="w-full rounded-lg border border-black/10 px-3 py-2 text-sm outline-none focus:border-navy-700" />
          </FormField>
        </form>
      </Modal>
    </div>
  );
}