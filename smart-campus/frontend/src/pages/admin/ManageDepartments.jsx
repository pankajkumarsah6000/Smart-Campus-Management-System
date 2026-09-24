import { useEffect, useState } from 'react';
import { Plus, Trash2 } from 'lucide-react';
import DataTable from '../../components/DataTable';
import Modal from '../../components/Modal';
import FormField from '../../components/FormField';
import LoadingState from '../../components/LoadingState';
import ErrorState from '../../components/ErrorState';
import { adminService } from '../../services/adminService';
import { useToast } from '../../context/ToastContext';

const emptyForm = { name: '', code: '', description: '' };

export default function ManageDepartments() {
  const [departments, setDepartments] = useState([]);
  const [status, setStatus] = useState('loading');
  const [modalOpen, setModalOpen] = useState(false);
  const [form, setForm] = useState(emptyForm);
  const [saving, setSaving] = useState(false);
  const { showToast } = useToast();

  const load = () => {
    setStatus('loading');
    adminService.listDepartments().then((d) => { setDepartments(d); setStatus('ready'); }).catch(() => setStatus('error'));
  };
  useEffect(load, []);

  const handleCreate = async (e) => {
    e.preventDefault();
    setSaving(true);
    try {
      await adminService.createDepartment(form);
      showToast('Department created', 'success');
      setModalOpen(false);
      setForm(emptyForm);
      load();
    } catch (err) {
      showToast(err.response?.data?.message || 'Could not create department', 'error');
    } finally {
      setSaving(false);
    }
  };

  const handleDelete = async (id) => {
    if (!window.confirm('Delete this department?')) return;
    try {
      await adminService.deleteDepartment(id);
      showToast('Department deleted', 'success');
      load();
    } catch {
      showToast('Could not delete department', 'error');
    }
  };

  if (status === 'loading') return <LoadingState label="Loading departments" />;
  if (status === 'error') return <ErrorState onRetry={load} />;

  const columns = [
    { key: 'code', header: 'Code' },
    { key: 'name', header: 'Name' },
    { key: 'headOfDepartmentName', header: 'HOD', render: (r) => r.headOfDepartmentName || '—' },
    { key: 'studentCount', header: 'Students' },
    { key: 'facultyCount', header: 'Faculty' },
  ];

  return (
    <div>
      <div className="flex items-center justify-between mb-6">
        <div>
          <p className="ledger-index uppercase text-ink-muted">Admin · Departments</p>
          <h1 className="font-display text-2xl text-ink dark:text-white">Manage departments</h1>
        </div>
        <button onClick={() => setModalOpen(true)} className="flex items-center gap-2 rounded-lg bg-navy-900 text-white px-4 py-2 text-sm font-medium hover:bg-navy-800">
          <Plus size={16} /> Add department
        </button>
      </div>

      <DataTable
        columns={columns}
        rows={departments}
        emptyMessage="No departments yet"
        actions={(row) => (
          <button onClick={() => handleDelete(row.id)} aria-label={`Delete ${row.name}`} className="p-1.5 rounded hover:bg-[var(--color-danger-soft)] text-[var(--color-danger)]">
            <Trash2 size={15} />
          </button>
        )}
      />

      <Modal
        open={modalOpen}
        onClose={() => setModalOpen(false)}
        title="Add a department"
        footer={
          <>
            <button onClick={() => setModalOpen(false)} className="px-4 py-2 text-sm rounded-lg hover:bg-navy-900/5">Cancel</button>
            <button form="create-dept-form" type="submit" disabled={saving} className="px-4 py-2 text-sm rounded-lg bg-navy-900 text-white hover:bg-navy-800 disabled:opacity-60">
              {saving ? 'Saving…' : 'Create'}
            </button>
          </>
        }
      >
        <form id="create-dept-form" onSubmit={handleCreate}>
          <FormField label="Name">
            <input required value={form.name} onChange={(e) => setForm({ ...form, name: e.target.value })} className="w-full rounded-lg border border-black/10 px-3 py-2 text-sm outline-none focus:border-navy-700" />
          </FormField>
          <FormField label="Code">
            <input required value={form.code} onChange={(e) => setForm({ ...form, code: e.target.value })} placeholder="e.g. CSE" className="w-full rounded-lg border border-black/10 px-3 py-2 text-sm outline-none focus:border-navy-700" />
          </FormField>
          <FormField label="Description">
            <textarea value={form.description} onChange={(e) => setForm({ ...form, description: e.target.value })} rows={3} className="w-full rounded-lg border border-black/10 px-3 py-2 text-sm outline-none focus:border-navy-700" />
          </FormField>
        </form>
      </Modal>
    </div>
  );
}
