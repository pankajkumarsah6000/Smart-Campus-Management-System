import { useEffect, useState } from 'react';
import { Plus } from 'lucide-react';
import Modal from '../../components/Modal';
import FormField from '../../components/FormField';
import Badge from '../../components/Badge';
import LoadingState from '../../components/LoadingState';
import ErrorState from '../../components/ErrorState';
import EmptyState from '../../components/EmptyState';
import { complaintService } from '../../services/complaintService';
import { useToast } from '../../context/ToastContext';

const emptyForm = { subject: '', description: '', category: 'OTHER' };
const statusTone = { OPEN: 'danger', IN_PROGRESS: 'warning', RESOLVED: 'success', CLOSED: 'neutral' };

export default function Complaints() {
  const [complaints, setComplaints] = useState([]);
  const [status, setStatus] = useState('loading');
  const [modalOpen, setModalOpen] = useState(false);
  const [form, setForm] = useState(emptyForm);
  const [saving, setSaving] = useState(false);
  const { showToast } = useToast();

  const load = () => {
    setStatus('loading');
    complaintService.my().then((d) => { setComplaints(d || []); setStatus('ready'); }).catch(() => setStatus('error'));
  };
  useEffect(load, []);

  const handleRaise = async (e) => {
    e.preventDefault();
    setSaving(true);
    try {
      await complaintService.raise(form);
      showToast('Complaint submitted', 'success');
      setModalOpen(false);
      setForm(emptyForm);
      load();
    } catch (err) {
      showToast(err.response?.data?.message || 'Could not submit complaint', 'error');
    } finally {
      setSaving(false);
    }
  };

  if (status === 'loading') return <LoadingState label="Loading complaints" />;
  if (status === 'error') return <ErrorState onRetry={load} />;

  return (
    <div>
      <div className="flex items-center justify-between mb-6">
        <div>
          <p className="ledger-index uppercase text-ink-muted">Student · Complaints</p>
          <h1 className="font-display text-2xl text-ink dark:text-white">My complaints</h1>
        </div>
        <button onClick={() => setModalOpen(true)} className="flex items-center gap-2 rounded-lg bg-navy-900 text-white px-4 py-2 text-sm font-medium hover:bg-navy-800">
          <Plus size={16} /> Raise complaint
        </button>
      </div>

      {complaints.length === 0 ? (
        <div className="rounded-xl border border-black/8 dark:border-white/10 bg-white dark:bg-navy-800">
          <EmptyState message="No complaints raised yet" />
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
                    Raised {new Date(c.createdAt).toLocaleString()}
                    {c.resolutionNote ? ` · Update: ${c.resolutionNote}` : ''}
                  </p>
                </div>
              </div>
            </div>
          ))}
        </div>
      )}

      <Modal
        open={modalOpen}
        onClose={() => setModalOpen(false)}
        title="Raise a complaint"
        footer={
          <>
            <button onClick={() => setModalOpen(false)} className="px-4 py-2 text-sm rounded-lg hover:bg-navy-900/5">Cancel</button>
            <button form="raise-complaint-form" type="submit" disabled={saving} className="px-4 py-2 text-sm rounded-lg bg-navy-900 text-white hover:bg-navy-800 disabled:opacity-60">
              {saving ? 'Submitting…' : 'Submit complaint'}
            </button>
          </>
        }
      >
        <form id="raise-complaint-form" onSubmit={handleRaise}>
          <FormField label="Subject">
            <input required value={form.subject} onChange={(e) => setForm({ ...form, subject: e.target.value })} className="w-full rounded-lg border border-black/10 px-3 py-2 text-sm outline-none focus:border-navy-700" />
          </FormField>
          <FormField label="Description">
            <textarea required rows={4} value={form.description} onChange={(e) => setForm({ ...form, description: e.target.value })} className="w-full rounded-lg border border-black/10 px-3 py-2 text-sm outline-none focus:border-navy-700" />
          </FormField>
          <FormField label="Category">
            <select value={form.category} onChange={(e) => setForm({ ...form, category: e.target.value })} className="w-full rounded-lg border border-black/10 px-3 py-2 text-sm outline-none focus:border-navy-700">
              <option value="ACADEMIC">Academic</option>
              <option value="HOSTEL">Hostel</option>
              <option value="FEE">Fees</option>
              <option value="INFRASTRUCTURE">Infrastructure</option>
              <option value="FACULTY">Faculty</option>
              <option value="OTHER">Other</option>
            </select>
          </FormField>
        </form>
      </Modal>
    </div>
  );
}