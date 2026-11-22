import { useEffect, useState } from 'react';
import { Plus, Trash2 } from 'lucide-react';
import Modal from '../../components/Modal';
import FormField from '../../components/FormField';
import Badge from '../../components/Badge';
import LoadingState from '../../components/LoadingState';
import ErrorState from '../../components/ErrorState';
import EmptyState from '../../components/EmptyState';
import { adminService } from '../../services/adminService';
import { useToast } from '../../context/ToastContext';

const emptyForm = { title: '', content: '', targetAudience: 'ALL', priority: 'NORMAL' };
const priorityTone = { LOW: 'neutral', NORMAL: 'info', HIGH: 'warning', URGENT: 'danger' };

export default function ManageNotices() {
  const [notices, setNotices] = useState([]);
  const [status, setStatus] = useState('loading');
  const [modalOpen, setModalOpen] = useState(false);
  const [form, setForm] = useState(emptyForm);
  const [saving, setSaving] = useState(false);
  const { showToast } = useToast();

  const load = () => {
    setStatus('loading');
    adminService.listNotices().then((n) => { setNotices(n); setStatus('ready'); }).catch(() => setStatus('error'));
  };
  useEffect(load, []);

  const handleCreate = async (e) => {
    e.preventDefault();
    setSaving(true);
    try {
      await adminService.createNotice(form);
      showToast('Notice posted', 'success');
      setModalOpen(false);
      setForm(emptyForm);
      load();
    } catch (err) {
      showToast(err.response?.data?.message || 'Could not post notice', 'error');
    } finally {
      setSaving(false);
    }
  };

  const handleDelete = async (id) => {
    if (!window.confirm('Delete this notice?')) return;
    try {
      await adminService.deleteNotice(id);
      showToast('Notice deleted', 'success');
      load();
    } catch {
      showToast('Could not delete notice', 'error');
    }
  };

  if (status === 'loading') return <LoadingState label="Loading notices" />;
  if (status === 'error') return <ErrorState onRetry={load} />;

  return (
    <div>
      <div className="flex items-center justify-between mb-6">
        <div>
          <p className="ledger-index uppercase text-ink-muted">Admin · Notices</p>
          <h1 className="font-display text-2xl text-ink dark:text-white">Campus notices</h1>
        </div>
        <button onClick={() => setModalOpen(true)} className="flex items-center gap-2 rounded-lg bg-navy-900 text-white px-4 py-2 text-sm font-medium hover:bg-navy-800">
          <Plus size={16} /> Post notice
        </button>
      </div>

      {notices.length === 0 ? (
        <div className="rounded-xl border border-black/8 dark:border-white/10 bg-white dark:bg-navy-800">
          <EmptyState message="No notices posted yet" />
        </div>
      ) : (
        <div className="space-y-3">
          {notices.map((n) => (
            <div key={n.id} className="rounded-xl border border-black/8 dark:border-white/10 bg-white dark:bg-navy-800 p-4">
              <div className="flex items-start justify-between gap-3">
                <div>
                  <div className="flex items-center gap-2 mb-1">
                    <h3 className="font-medium text-ink dark:text-white">{n.title}</h3>
                    <Badge tone={priorityTone[n.priority] || 'neutral'}>{n.priority}</Badge>
                    <Badge>{n.targetAudience}</Badge>
                  </div>
                  <p className="text-sm text-ink-muted">{n.content}</p>
                  <p className="text-xs text-ink-muted mt-2">
                    Posted by {n.postedByName} · {new Date(n.createdAt).toLocaleString()}
                  </p>
                </div>
                <button onClick={() => handleDelete(n.id)} aria-label="Delete notice" className="p-1.5 rounded hover:bg-[var(--color-danger-soft)] text-[var(--color-danger)] shrink-0">
                  <Trash2 size={15} />
                </button>
              </div>
            </div>
          ))}
        </div>
      )}

      <Modal
        open={modalOpen}
        onClose={() => setModalOpen(false)}
        title="Post a notice"
        footer={
          <>
            <button onClick={() => setModalOpen(false)} className="px-4 py-2 text-sm rounded-lg hover:bg-navy-900/5">Cancel</button>
            <button form="create-notice-form" type="submit" disabled={saving} className="px-4 py-2 text-sm rounded-lg bg-navy-900 text-white hover:bg-navy-800 disabled:opacity-60">
              {saving ? 'Posting…' : 'Post notice'}
            </button>
          </>
        }
      >
        <form id="create-notice-form" onSubmit={handleCreate}>
          <FormField label="Title">
            <input required value={form.title} onChange={(e) => setForm({ ...form, title: e.target.value })} className="w-full rounded-lg border border-black/10 px-3 py-2 text-sm outline-none focus:border-navy-700" />
          </FormField>
          <FormField label="Content">
            <textarea required rows={4} value={form.content} onChange={(e) => setForm({ ...form, content: e.target.value })} className="w-full rounded-lg border border-black/10 px-3 py-2 text-sm outline-none focus:border-navy-700" />
          </FormField>
          <div className="grid grid-cols-2 gap-3">
            <FormField label="Audience">
              <select value={form.targetAudience} onChange={(e) => setForm({ ...form, targetAudience: e.target.value })} className="w-full rounded-lg border border-black/10 px-3 py-2 text-sm outline-none focus:border-navy-700">
                <option value="ALL">Everyone</option>
                <option value="STUDENTS">Students</option>
                <option value="FACULTY">Faculty</option>
                <option value="PARENTS">Parents</option>
              </select>
            </FormField>
            <FormField label="Priority">
              <select value={form.priority} onChange={(e) => setForm({ ...form, priority: e.target.value })} className="w-full rounded-lg border border-black/10 px-3 py-2 text-sm outline-none focus:border-navy-700">
                <option value="LOW">Low</option>
                <option value="NORMAL">Normal</option>
                <option value="HIGH">High</option>
                <option value="URGENT">Urgent</option>
              </select>
            </FormField>
          </div>
        </form>
      </Modal>
    </div>
  );
}
