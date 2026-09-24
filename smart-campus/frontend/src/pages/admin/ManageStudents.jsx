import { useEffect, useState } from 'react';
import { Plus, Trash2 } from 'lucide-react';
import DataTable from '../../components/DataTable';
import Modal from '../../components/Modal';
import FormField from '../../components/FormField';
import Badge from '../../components/Badge';
import LoadingState from '../../components/LoadingState';
import ErrorState from '../../components/ErrorState';
import { adminService } from '../../services/adminService';
import { useToast } from '../../context/ToastContext';

const emptyForm = { email: '', firstName: '', lastName: '', rollNumber: '', semester: '', section: '' };

export default function ManageStudents() {
  const [students, setStudents] = useState([]);
  const [departments, setDepartments] = useState([]);
  const [status, setStatus] = useState('loading');
  const [modalOpen, setModalOpen] = useState(false);
  const [form, setForm] = useState(emptyForm);
  const [saving, setSaving] = useState(false);
  const { showToast } = useToast();

  const load = () => {
    setStatus('loading');
    Promise.all([adminService.listStudents(), adminService.listDepartments()])
      .then(([s, d]) => { setStudents(s); setDepartments(d); setStatus('ready'); })
      .catch(() => setStatus('error'));
  };

  useEffect(load, []);

  const handleCreate = async (e) => {
    e.preventDefault();
    setSaving(true);
    try {
      await adminService.createStudent(form);
      showToast('Student registered', 'success');
      setModalOpen(false);
      setForm(emptyForm);
      load();
    } catch (err) {
      showToast(err.response?.data?.message || 'Could not create student', 'error');
    } finally {
      setSaving(false);
    }
  };

  const handleDelete = async (id) => {
    if (!window.confirm('Remove this student record? This cannot be undone.')) return;
    try {
      await adminService.deleteStudent(id);
      showToast('Student removed', 'success');
      load();
    } catch {
      showToast('Could not remove student', 'error');
    }
  };

  if (status === 'loading') return <LoadingState label="Loading students" />;
  if (status === 'error') return <ErrorState onRetry={load} />;

  const columns = [
    { key: 'rollNumber', header: 'Roll No.' },
    { key: 'name', header: 'Name', render: (r) => `${r.firstName} ${r.lastName}` },
    { key: 'email', header: 'Email' },
    { key: 'departmentName', header: 'Department', render: (r) => r.departmentName || '—' },
    { key: 'semester', header: 'Sem / Section', render: (r) => `${r.semester || '—'} / ${r.section || '—'}` },
    { key: 'status', header: 'Status', render: (r) => <Badge tone={r.status === 'ACTIVE' ? 'success' : 'neutral'}>{r.status}</Badge> },
  ];

  return (
    <div>
      <div className="flex items-center justify-between mb-6">
        <div>
          <p className="ledger-index uppercase text-ink-muted">Admin · Students</p>
          <h1 className="font-display text-2xl text-ink dark:text-white">Manage students</h1>
        </div>
        <button
          onClick={() => setModalOpen(true)}
          className="flex items-center gap-2 rounded-lg bg-navy-900 text-white px-4 py-2 text-sm font-medium hover:bg-navy-800"
        >
          <Plus size={16} /> Add student
        </button>
      </div>

      <DataTable
        columns={columns}
        rows={students}
        emptyMessage="No students registered yet"
        actions={(row) => (
          <button onClick={() => handleDelete(row.id)} aria-label={`Delete ${row.firstName}`} className="p-1.5 rounded hover:bg-[var(--color-danger-soft)] text-[var(--color-danger)]">
            <Trash2 size={15} />
          </button>
        )}
      />

      <Modal
        open={modalOpen}
        onClose={() => setModalOpen(false)}
        title="Register a new student"
        footer={
          <>
            <button onClick={() => setModalOpen(false)} className="px-4 py-2 text-sm rounded-lg hover:bg-navy-900/5">Cancel</button>
            <button form="create-student-form" type="submit" disabled={saving} className="px-4 py-2 text-sm rounded-lg bg-navy-900 text-white hover:bg-navy-800 disabled:opacity-60">
              {saving ? 'Saving…' : 'Register student'}
            </button>
          </>
        }
      >
        <form id="create-student-form" onSubmit={handleCreate}>
          <FormField label="First name">
            <input required value={form.firstName} onChange={(e) => setForm({ ...form, firstName: e.target.value })} className="w-full rounded-lg border border-black/10 px-3 py-2 text-sm outline-none focus:border-navy-700" />
          </FormField>
          <FormField label="Last name">
            <input required value={form.lastName} onChange={(e) => setForm({ ...form, lastName: e.target.value })} className="w-full rounded-lg border border-black/10 px-3 py-2 text-sm outline-none focus:border-navy-700" />
          </FormField>
          <FormField label="Email">
            <input required type="email" value={form.email} onChange={(e) => setForm({ ...form, email: e.target.value })} className="w-full rounded-lg border border-black/10 px-3 py-2 text-sm outline-none focus:border-navy-700" />
          </FormField>
          <FormField label="Roll number">
            <input required value={form.rollNumber} onChange={(e) => setForm({ ...form, rollNumber: e.target.value })} className="w-full rounded-lg border border-black/10 px-3 py-2 text-sm outline-none focus:border-navy-700" />
          </FormField>
          <FormField label="Department">
            <select value={form.departmentId || ''} onChange={(e) => setForm({ ...form, departmentId: e.target.value || null })} className="w-full rounded-lg border border-black/10 px-3 py-2 text-sm outline-none focus:border-navy-700">
              <option value="">Select department</option>
              {departments.map((d) => <option key={d.id} value={d.id}>{d.name}</option>)}
            </select>
          </FormField>
          <div className="grid grid-cols-2 gap-3">
            <FormField label="Semester">
              <input value={form.semester} onChange={(e) => setForm({ ...form, semester: e.target.value })} className="w-full rounded-lg border border-black/10 px-3 py-2 text-sm outline-none focus:border-navy-700" />
            </FormField>
            <FormField label="Section">
              <input value={form.section} onChange={(e) => setForm({ ...form, section: e.target.value })} className="w-full rounded-lg border border-black/10 px-3 py-2 text-sm outline-none focus:border-navy-700" />
            </FormField>
          </div>
          <p className="text-xs text-ink-muted -mt-2">A temporary password will be generated if left blank (returned once via the API response).</p>
        </form>
      </Modal>
    </div>
  );
}
