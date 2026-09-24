import { useEffect, useState } from 'react';
import { Plus, Trash2 } from 'lucide-react';
import DataTable from '../../components/DataTable';
import Modal from '../../components/Modal';
import FormField from '../../components/FormField';
import LoadingState from '../../components/LoadingState';
import ErrorState from '../../components/ErrorState';
import { adminService } from '../../services/adminService';
import { useToast } from '../../context/ToastContext';

const emptyForm = { name: '', code: '', durationSemesters: '' };

export default function ManageCourses() {
  const [courses, setCourses] = useState([]);
  const [departments, setDepartments] = useState([]);
  const [status, setStatus] = useState('loading');
  const [modalOpen, setModalOpen] = useState(false);
  const [form, setForm] = useState(emptyForm);
  const [saving, setSaving] = useState(false);
  const { showToast } = useToast();

  const load = () => {
    setStatus('loading');
    Promise.all([adminService.listCourses(), adminService.listDepartments()])
      .then(([c, d]) => { setCourses(c); setDepartments(d); setStatus('ready'); })
      .catch(() => setStatus('error'));
  };
  useEffect(load, []);

  const handleCreate = async (e) => {
    e.preventDefault();
    setSaving(true);
    try {
      await adminService.createCourse({ ...form, durationSemesters: form.durationSemesters ? Number(form.durationSemesters) : null });
      showToast('Course created', 'success');
      setModalOpen(false);
      setForm(emptyForm);
      load();
    } catch (err) {
      showToast(err.response?.data?.message || 'Could not create course', 'error');
    } finally {
      setSaving(false);
    }
  };

  const handleDelete = async (id) => {
    if (!window.confirm('Delete this course?')) return;
    try {
      await adminService.deleteCourse(id);
      showToast('Course deleted', 'success');
      load();
    } catch {
      showToast('Could not delete course', 'error');
    }
  };

  if (status === 'loading') return <LoadingState label="Loading courses" />;
  if (status === 'error') return <ErrorState onRetry={load} />;

  const columns = [
    { key: 'code', header: 'Code' },
    { key: 'name', header: 'Name' },
    { key: 'departmentName', header: 'Department', render: (r) => r.departmentName || '—' },
    { key: 'durationSemesters', header: 'Duration', render: (r) => r.durationSemesters ? `${r.durationSemesters} sem` : '—' },
  ];

  return (
    <div>
      <div className="flex items-center justify-between mb-6">
        <div>
          <p className="ledger-index uppercase text-ink-muted">Admin · Courses</p>
          <h1 className="font-display text-2xl text-ink dark:text-white">Manage courses</h1>
        </div>
        <button onClick={() => setModalOpen(true)} className="flex items-center gap-2 rounded-lg bg-navy-900 text-white px-4 py-2 text-sm font-medium hover:bg-navy-800">
          <Plus size={16} /> Add course
        </button>
      </div>

      <DataTable
        columns={columns}
        rows={courses}
        emptyMessage="No courses yet"
        actions={(row) => (
          <button onClick={() => handleDelete(row.id)} aria-label={`Delete ${row.name}`} className="p-1.5 rounded hover:bg-[var(--color-danger-soft)] text-[var(--color-danger)]">
            <Trash2 size={15} />
          </button>
        )}
      />

      <Modal
        open={modalOpen}
        onClose={() => setModalOpen(false)}
        title="Add a course"
        footer={
          <>
            <button onClick={() => setModalOpen(false)} className="px-4 py-2 text-sm rounded-lg hover:bg-navy-900/5">Cancel</button>
            <button form="create-course-form" type="submit" disabled={saving} className="px-4 py-2 text-sm rounded-lg bg-navy-900 text-white hover:bg-navy-800 disabled:opacity-60">
              {saving ? 'Saving…' : 'Create'}
            </button>
          </>
        }
      >
        <form id="create-course-form" onSubmit={handleCreate}>
          <FormField label="Name">
            <input required value={form.name} onChange={(e) => setForm({ ...form, name: e.target.value })} className="w-full rounded-lg border border-black/10 px-3 py-2 text-sm outline-none focus:border-navy-700" />
          </FormField>
          <FormField label="Code">
            <input required value={form.code} onChange={(e) => setForm({ ...form, code: e.target.value })} className="w-full rounded-lg border border-black/10 px-3 py-2 text-sm outline-none focus:border-navy-700" />
          </FormField>
          <FormField label="Department">
            <select value={form.departmentId || ''} onChange={(e) => setForm({ ...form, departmentId: e.target.value || null })} className="w-full rounded-lg border border-black/10 px-3 py-2 text-sm outline-none focus:border-navy-700">
              <option value="">Select department</option>
              {departments.map((d) => <option key={d.id} value={d.id}>{d.name}</option>)}
            </select>
          </FormField>
          <FormField label="Duration (semesters)">
            <input type="number" min="1" value={form.durationSemesters} onChange={(e) => setForm({ ...form, durationSemesters: e.target.value })} className="w-full rounded-lg border border-black/10 px-3 py-2 text-sm outline-none focus:border-navy-700" />
          </FormField>
        </form>
      </Modal>
    </div>
  );
}
