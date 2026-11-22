import { useEffect, useState } from 'react';
import { Plus, Trash2, CreditCard } from 'lucide-react';
import Modal from '../../components/Modal';
import FormField from '../../components/FormField';
import Badge from '../../components/Badge';
import LoadingState from '../../components/LoadingState';
import ErrorState from '../../components/ErrorState';
import EmptyState from '../../components/EmptyState';
import { feeService } from '../../services/feeService';
import { adminService } from '../../services/adminService';
import { useToast } from '../../context/ToastContext';

const emptyForm = { studentId: '', feeType: '', amount: '', dueDate: '', semester: '' };
const statusTone = { PAID: 'success', PARTIAL: 'warning', PENDING: 'info', OVERDUE: 'danger' };
const fmt = (n) => (n == null ? '—' : `₹${Number(n).toLocaleString('en-IN', { maximumFractionDigits: 2 })}`);

export default function ManageFees() {
  const [fees, setFees] = useState([]);
  const [students, setStudents] = useState([]);
  const [status, setStatus] = useState('loading');
  const [modalOpen, setModalOpen] = useState(false);
  const [form, setForm] = useState(emptyForm);
  const [saving, setSaving] = useState(false);
  const { showToast } = useToast();

  const load = () => {
    setStatus('loading');
    Promise.all([feeService.all(), adminService.listStudents()])
      .then(([f, s]) => { setFees(f || []); setStudents(s || []); setStatus('ready'); })
      .catch(() => setStatus('error'));
  };
  useEffect(load, []);

  const handleCreate = async (e) => {
    e.preventDefault();
    setSaving(true);
    try {
      await feeService.create({
        studentId: Number(form.studentId),
        feeType: form.feeType,
        amount: Number(form.amount),
        dueDate: form.dueDate || null,
        semester: form.semester || null,
      });
      showToast('Fee record created', 'success');
      setModalOpen(false);
      setForm(emptyForm);
      load();
    } catch (err) {
      showToast(err.response?.data?.message || 'Could not create fee record', 'error');
    } finally {
      setSaving(false);
    }
  };

  const handlePay = async (fee) => {
    const amount = window.prompt(`Amount to record for ${fee.studentName} (balance ${fmt(fee.balance)}):`, fee.balance);
    if (amount == null) return;
    try {
      await feeService.pay(fee.id, { amountPaid: Number(amount) });
      showToast('Payment recorded', 'success');
      load();
    } catch (err) {
      showToast(err.response?.data?.message || 'Could not record payment', 'error');
    }
  };

  const handleDelete = async (id) => {
    if (!window.confirm('Delete this fee record?')) return;
    try {
      await feeService.remove(id);
      showToast('Fee record deleted', 'success');
      load();
    } catch {
      showToast('Could not delete fee record', 'error');
    }
  };

  if (status === 'loading') return <LoadingState label="Loading fee records" />;
  if (status === 'error') return <ErrorState onRetry={load} />;

  return (
    <div>
      <div className="flex items-center justify-between mb-6">
        <div>
          <p className="ledger-index uppercase text-ink-muted">Admin · Fees</p>
          <h1 className="font-display text-2xl text-ink dark:text-white">Fee records</h1>
        </div>
        <button onClick={() => setModalOpen(true)} className="flex items-center gap-2 rounded-lg bg-navy-900 text-white px-4 py-2 text-sm font-medium hover:bg-navy-800">
          <Plus size={16} /> Add fee record
        </button>
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
                <div className="min-w-0">
                  <div className="flex flex-wrap items-center gap-2 mb-1">
                    <h3 className="font-medium text-ink dark:text-white">{f.studentName}</h3>
                    <Badge tone={statusTone[f.status] || 'neutral'}>{f.status}</Badge>
                  </div>
                  <p className="text-sm text-ink-muted">{f.rollNumber} · {f.feeType || 'Fee'}{f.semester ? ` · Sem ${f.semester}` : ''}</p>
                  <p className="text-xs text-ink-muted mt-1">Due {f.dueDate || '—'}{f.paidDate ? ` · Paid on ${f.paidDate}` : ''}</p>
                </div>
                <div className="text-right font-mono text-sm shrink-0">
                  <p className="text-ink dark:text-white">{fmt(f.amount)}</p>
                  <p className="text-xs text-ink-muted">Paid {fmt(f.amountPaid)}</p>
                  <p className={`text-xs ${Number(f.balance) > 0 ? 'text-[var(--color-danger)]' : 'text-[var(--color-success)]'}`}>Due {fmt(f.balance)}</p>
                </div>
                <div className="flex items-center gap-1 shrink-0">
                  {Number(f.balance) > 0 && (
                    <button onClick={() => handlePay(f)} className="p-2 rounded hover:bg-navy-900/5 dark:hover:bg-white/5 text-ink-muted" aria-label="Record payment" title="Record payment">
                      <CreditCard size={16} />
                    </button>
                  )}
                  <button onClick={() => handleDelete(f.id)} className="p-2 rounded hover:bg-[var(--color-danger-soft)] text-[var(--color-danger)]" aria-label="Delete fee record">
                    <Trash2 size={15} />
                  </button>
                </div>
              </div>
            </div>
          ))}
        </div>
      )}

      <Modal
        open={modalOpen}
        onClose={() => setModalOpen(false)}
        title="Add fee record"
        footer={
          <>
            <button onClick={() => setModalOpen(false)} className="px-4 py-2 text-sm rounded-lg hover:bg-navy-900/5">Cancel</button>
            <button form="create-fee-form" type="submit" disabled={saving} className="px-4 py-2 text-sm rounded-lg bg-navy-900 text-white hover:bg-navy-800 disabled:opacity-60">
              {saving ? 'Creating…' : 'Create'}
            </button>
          </>
        }
      >
        <form id="create-fee-form" onSubmit={handleCreate}>
          <FormField label="Student">
            <select required value={form.studentId} onChange={(e) => setForm({ ...form, studentId: e.target.value })} className="w-full rounded-lg border border-black/10 px-3 py-2 text-sm outline-none focus:border-navy-700">
              <option value="" disabled>Select student</option>
              {students.map((s) => <option key={s.id} value={s.id}>{s.rollNumber} — {s.firstName} {s.lastName}</option>)}
            </select>
          </FormField>
          <div className="grid grid-cols-2 gap-3">
            <FormField label="Fee type">
              <input required value={form.feeType} onChange={(e) => setForm({ ...form, feeType: e.target.value })} placeholder="e.g. Tuition" className="w-full rounded-lg border border-black/10 px-3 py-2 text-sm outline-none focus:border-navy-700" />
            </FormField>
            <FormField label="Amount (₹)">
              <input required type="number" min="1" step="0.01" value={form.amount} onChange={(e) => setForm({ ...form, amount: e.target.value })} className="w-full rounded-lg border border-black/10 px-3 py-2 text-sm outline-none focus:border-navy-700" />
            </FormField>
          </div>
          <div className="grid grid-cols-2 gap-3">
            <FormField label="Due date">
              <input type="date" value={form.dueDate} onChange={(e) => setForm({ ...form, dueDate: e.target.value })} className="w-full rounded-lg border border-black/10 px-3 py-2 text-sm outline-none focus:border-navy-700" />
            </FormField>
            <FormField label="Semester">
              <input value={form.semester} onChange={(e) => setForm({ ...form, semester: e.target.value })} placeholder="e.g. 3" className="w-full rounded-lg border border-black/10 px-3 py-2 text-sm outline-none focus:border-navy-700" />
            </FormField>
          </div>
        </form>
      </Modal>
    </div>
  );
}