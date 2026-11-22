import { useEffect, useState } from 'react';
import { Plus, Trash2 } from 'lucide-react';
import DataTable from '../../components/DataTable';
import Modal from '../../components/Modal';
import FormField from '../../components/FormField';
import LoadingState from '../../components/LoadingState';
import ErrorState from '../../components/ErrorState';
import { adminService } from '../../services/adminService';
import { useToast } from '../../context/ToastContext';

const emptyForm = { roomNumber: '', building: '', capacity: '', type: 'LECTURE_HALL' };

export default function ManageClassrooms() {
  const [rooms, setRooms] = useState([]);
  const [status, setStatus] = useState('loading');
  const [modalOpen, setModalOpen] = useState(false);
  const [form, setForm] = useState(emptyForm);
  const [saving, setSaving] = useState(false);
  const { showToast } = useToast();

  const load = () => {
    setStatus('loading');
    adminService.listClassrooms().then((r) => { setRooms(r); setStatus('ready'); }).catch(() => setStatus('error'));
  };
  useEffect(load, []);

  const handleCreate = async (e) => {
    e.preventDefault();
    setSaving(true);
    try {
      await adminService.createClassroom({ ...form, capacity: form.capacity ? Number(form.capacity) : null });
      showToast('Classroom added', 'success');
      setModalOpen(false);
      setForm(emptyForm);
      load();
    } catch (err) {
      showToast(err.response?.data?.message || 'Could not add classroom', 'error');
    } finally {
      setSaving(false);
    }
  };

  const handleDelete = async (id) => {
    if (!window.confirm('Delete this classroom?')) return;
    try {
      await adminService.deleteClassroom(id);
      showToast('Classroom deleted', 'success');
      load();
    } catch {
      showToast('Could not delete classroom', 'error');
    }
  };

  if (status === 'loading') return <LoadingState label="Loading classrooms" />;
  if (status === 'error') return <ErrorState onRetry={load} />;

  const columns = [
    { key: 'roomNumber', header: 'Room' },
    { key: 'building', header: 'Building', render: (r) => r.building || '—' },
    { key: 'type', header: 'Type' },
    { key: 'capacity', header: 'Capacity', render: (r) => r.capacity ?? '—' },
    { key: 'departmentName', header: 'Department', render: (r) => r.departmentName || 'Shared' },
  ];

  return (
    <div>
      <div className="flex items-center justify-between mb-6">
        <div>
          <p className="ledger-index uppercase text-ink-muted">Admin · Classrooms</p>
          <h1 className="font-display text-2xl text-ink dark:text-white">Manage classrooms</h1>
        </div>
        <button onClick={() => setModalOpen(true)} className="flex items-center gap-2 rounded-lg bg-navy-900 text-white px-4 py-2 text-sm font-medium hover:bg-navy-800">
          <Plus size={16} /> Add classroom
        </button>
      </div>

      <DataTable
        columns={columns}
        rows={rooms}
        emptyMessage="No classrooms yet"
        actions={(row) => (
          <button onClick={() => handleDelete(row.id)} aria-label={`Delete ${row.roomNumber}`} className="p-1.5 rounded hover:bg-[var(--color-danger-soft)] text-[var(--color-danger)]">
            <Trash2 size={15} />
          </button>
        )}
      />

      <Modal
        open={modalOpen}
        onClose={() => setModalOpen(false)}
        title="Add a classroom"
        footer={
          <>
            <button onClick={() => setModalOpen(false)} className="px-4 py-2 text-sm rounded-lg hover:bg-navy-900/5">Cancel</button>
            <button form="create-room-form" type="submit" disabled={saving} className="px-4 py-2 text-sm rounded-lg bg-navy-900 text-white hover:bg-navy-800 disabled:opacity-60">
              {saving ? 'Saving…' : 'Create'}
            </button>
          </>
        }
      >
        <form id="create-room-form" onSubmit={handleCreate}>
          <FormField label="Room number">
            <input required value={form.roomNumber} onChange={(e) => setForm({ ...form, roomNumber: e.target.value })} className="w-full rounded-lg border border-black/10 px-3 py-2 text-sm outline-none focus:border-navy-700" />
          </FormField>
          <FormField label="Building">
            <input value={form.building} onChange={(e) => setForm({ ...form, building: e.target.value })} className="w-full rounded-lg border border-black/10 px-3 py-2 text-sm outline-none focus:border-navy-700" />
          </FormField>
          <div className="grid grid-cols-2 gap-3">
            <FormField label="Type">
              <select value={form.type} onChange={(e) => setForm({ ...form, type: e.target.value })} className="w-full rounded-lg border border-black/10 px-3 py-2 text-sm outline-none focus:border-navy-700">
                <option value="LECTURE_HALL">Lecture Hall</option>
                <option value="LAB">Lab</option>
                <option value="SEMINAR_ROOM">Seminar Room</option>
                <option value="AUDITORIUM">Auditorium</option>
              </select>
            </FormField>
            <FormField label="Capacity">
              <input type="number" min="1" value={form.capacity} onChange={(e) => setForm({ ...form, capacity: e.target.value })} className="w-full rounded-lg border border-black/10 px-3 py-2 text-sm outline-none focus:border-navy-700" />
            </FormField>
          </div>
        </form>
      </Modal>
    </div>
  );
}
