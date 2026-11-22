import { useEffect, useState } from 'react';
import { Plus, Trash2 } from 'lucide-react';
import DataTable from '../../components/DataTable';
import Modal from '../../components/Modal';
import FormField from '../../components/FormField';
import LoadingState from '../../components/LoadingState';
import ErrorState from '../../components/ErrorState';
import { adminService } from '../../services/adminService';
import { useToast } from '../../context/ToastContext';

const emptyForm = { email: '', firstName: '', lastName: '', employeeId: '', designation: '' };

export default function ManageFaculty() {
  const [faculty, setFaculty] = useState([]);
  const [departments, setDepartments] = useState([]);
  const [status, setStatus] = useState('loading');
  const [modalOpen, setModalOpen] = useState(false);
  const [form, setForm] = useState(emptyForm);
  const [saving, setSaving] = useState(false);
  const { showToast } = useToast();

  const load = () => {
    setStatus('loading');
    Promise.all([adminService.listFaculty(), adminService.listDepartments()])
      .then(([f, d]) => { setFaculty(f); setDepartments(d); setStatus('ready'); })
      .catch(() => setStatus('error'));
  };
  useEffect(load, []);

  const handleCreate = async (e) => {
    e.preventDefault();
    setSaving(true);
    try {
      await adminService.createFaculty(form);
      showToast('Faculty member added', 'success');
      setModalOpen(false);
      setForm(emptyForm);
      load();
    } catch (err) {
      showToast(err.response?.data?.message || 'Could not add faculty', 'error');
    } finally {
      setSaving(false);
    }
  };

  const handleDelete = async (id) => {
    if (!window.confirm('Remove this faculty member?')) return;
    try {
      await adminService.deleteFaculty(id);
      showToast('Faculty removed', 'success');
      load();
    } catch {
      showToast('Could not remove faculty', 'error');
    }
  };

  if (status === 'loading') return <LoadingState label="Loading faculty" />;
  if (status === 'error') return <ErrorState onRetry={load} />;

  const columns = [
    { key: 'employeeId', header: 'Employee ID' },
    { key: 'name', header: 'Name', render: (r) => `${r.firstName} ${r.lastName}` },
    { key: 'email', header: 'Email' },
    { key: 'departmentName', header: 'Department', render: (r) => r.departmentName || '—' },
    { key: 'designation', header: 'Designation', render: (r) => r.designation || '—' },
  ];

  return (
    <div>
      <div className="flex items-center justify-between mb-6">
        <div>
          <p className="ledger-index uppercase text-ink-muted">Admin · Faculty</p>
          <h1 className="font-display text-2xl text-ink dark:text-white">Manage faculty</h1>
        </div>
        <button onClick={() => setModalOpen(true)} className="flex items-center gap-2 rounded-lg bg-navy-900 text-white px-4 py-2 text-sm font-medium hover:bg-navy-800">
          <Plus size={16} /> Add faculty
        </button>
      </div>

      <DataTable
        columns={columns}
        rows={faculty}
        emptyMessage="No faculty members yet"
        actions={(row) => (
          <button onClick={() => handleDelete(row.id)} aria-label={`Delete ${row.firstName}`} className="p-1.5 rounded hover:bg-[var(--color-danger-soft)] text-[var(--color-danger)]">
            <Trash2 size={15} />
          </button>
        )}
      />

      <Modal
        open={modalOpen}
        onClose={() => setModalOpen(false)}
        title="Add a faculty member"
        footer={
          <>
            <button onClick={() => setModalOpen(false)} className="px-4 py-2 text-sm rounded-lg hover:bg-navy-900/5">Cancel</button>
            <button form="create-faculty-form" type="submit" disabled={saving} className="px-4 py-2 text-sm rounded-lg bg-navy-900 text-white hover:bg-navy-800 disabled:opacity-60">
              {saving ? 'Saving…' : 'Add faculty'}
            </button>
          </>
        }
      >
        <form id="create-faculty-form" onSubmit={handleCreate}>
          <FormField label="First name">
            <input required value={form.firstName} onChange={(e) => setForm({ ...form, firstName: e.target.value })} className="w-full rounded-lg border border-black/10 px-3 py-2 text-sm outline-none focus:border-navy-700" />
          </FormField>
          <FormField label="Last name">
            <input required value={form.lastName} onChange={(e) => setForm({ ...form, lastName: e.target.value })} className="w-full rounded-lg border border-black/10 px-3 py-2 text-sm outline-none focus:border-navy-700" />
          </FormField>
          <FormField label="Email">
            <input required type="email" value={form.email} onChange={(e) => setForm({ ...form, email: e.target.value })} className="w-full rounded-lg border border-black/10 px-3 py-2 text-sm outline-none focus:border-navy-700" />
          </FormField>
          <FormField label="Employee ID">
            <input required value={form.employeeId} onChange={(e) => setForm({ ...form, employeeId: e.target.value })} className="w-full rounded-lg border border-black/10 px-3 py-2 text-sm outline-none focus:border-navy-700" />
          </FormField>
          <FormField label="Department">
            <select value={form.departmentId || ''} onChange={(e) => setForm({ ...form, departmentId: e.target.value || null })} className="w-full rounded-lg border border-black/10 px-3 py-2 text-sm outline-none focus:border-navy-700">
              <option value="">Select department</option>
              {departments.map((d) => <option key={d.id} value={d.id}>{d.name}</option>)}
            </select>
          </FormField>
          <FormField label="Designation">
            <input value={form.designation} onChange={(e) => setForm({ ...form, designation: e.target.value })} placeholder="e.g. Assistant Professor" className="w-full rounded-lg border border-black/10 px-3 py-2 text-sm outline-none focus:border-navy-700" />
          </FormField>
        </form>
      </Modal>
    </div>
  );
}
