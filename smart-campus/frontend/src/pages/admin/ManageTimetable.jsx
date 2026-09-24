import { useEffect, useState } from 'react';
import { Plus, Trash2 } from 'lucide-react';
import Modal from '../../components/Modal';
import FormField from '../../components/FormField';
import LoadingState from '../../components/LoadingState';
import ErrorState from '../../components/ErrorState';
import EmptyState from '../../components/EmptyState';
import { timetableService } from '../../services/timetableService';
import { adminService } from '../../services/adminService';
import { useToast } from '../../context/ToastContext';

const DAYS = ['MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY', 'SUNDAY'];
const emptyForm = { subjectId: '', section: 'A', semester: '3', dayOfWeek: 'MONDAY', startTime: '09:00', endTime: '10:00', room: '' };

export default function ManageTimetable() {
  const [slots, setSlots] = useState([]);
  const [subjects, setSubjects] = useState([]);
  const [status, setStatus] = useState('loading');
  const [modalOpen, setModalOpen] = useState(false);
  const [form, setForm] = useState(emptyForm);
  const [saving, setSaving] = useState(false);
  const { showToast } = useToast();

  const load = () => {
    setStatus('loading');
    Promise.all([timetableService.my(), adminService.listSubjects()])
      .then(([s, subs]) => { setSlots(s || []); setSubjects(subs || []); setStatus('ready'); })
      .catch(() => setStatus('error'));
  };
  useEffect(load, []);

  const handleCreate = async (e) => {
    e.preventDefault();
    setSaving(true);
    try {
      await timetableService.create({
        subjectId: Number(form.subjectId),
        section: form.section,
        semester: form.semester,
        dayOfWeek: form.dayOfWeek,
        startTime: form.startTime,
        endTime: form.endTime,
        room: form.room || null,
      });
      showToast('Slot created', 'success');
      setModalOpen(false);
      setForm(emptyForm);
      load();
    } catch (err) {
      showToast(err.response?.data?.message || 'Could not create slot', 'error');
    } finally {
      setSaving(false);
    }
  };

  const handleDelete = async (id) => {
    if (!window.confirm('Delete this timetable slot?')) return;
    try {
      await timetableService.remove(id);
      showToast('Slot deleted', 'success');
      load();
    } catch {
      showToast('Could not delete slot', 'error');
    }
  };

  if (status === 'loading') return <LoadingState label="Loading timetable" />;
  if (status === 'error') return <ErrorState onRetry={load} />;

  return (
    <div>
      <div className="flex items-center justify-between mb-6">
        <div>
          <p className="ledger-index uppercase text-ink-muted">Admin · Timetable</p>
          <h1 className="font-display text-2xl text-ink dark:text-white">Weekly timetable</h1>
        </div>
        <button onClick={() => setModalOpen(true)} className="flex items-center gap-2 rounded-lg bg-navy-900 text-white px-4 py-2 text-sm font-medium hover:bg-navy-800">
          <Plus size={16} /> Add slot
        </button>
      </div>

      {slots.length === 0 ? (
        <div className="rounded-xl border border-black/8 dark:border-white/10 bg-white dark:bg-navy-800">
          <EmptyState message="No timetable slots yet" />
        </div>
      ) : (
        <div className="grid lg:grid-cols-2 gap-6">
          {DAYS.filter((d) => slots.some((s) => s.dayOfWeek === d)).map((day) => (
            <section key={day} className="rounded-xl border border-black/8 dark:border-white/10 bg-white dark:bg-navy-800">
              <h2 className="font-display text-base px-5 py-4 border-b border-black/8 dark:border-white/10 capitalize">{day.toLowerCase()}</h2>
              <ul className="divide-y divide-black/5 dark:divide-white/5">
                {slots.filter((s) => s.dayOfWeek === day).sort((a, b) => a.startTime.localeCompare(b.startTime)).map((s) => (
                  <li key={s.id} className="px-5 py-3 flex items-center justify-between text-sm">
                    <div>
                      <p className="font-medium text-ink dark:text-white">{s.subjectName}</p>
                      <p className="text-xs text-ink-muted">{s.facultyName || 'No faculty'} · {s.room || '—'}</p>
                    </div>
                    <div className="flex items-center gap-2 shrink-0">
                      <span className="font-mono text-xs text-ink-muted">{s.startTime}–{s.endTime}</span>
                      <button onClick={() => handleDelete(s.id)} className="p-1.5 rounded hover:bg-[var(--color-danger-soft)] text-[var(--color-danger)]" aria-label="Delete slot">
                        <Trash2 size={14} />
                      </button>
                    </div>
                  </li>
                ))}
              </ul>
            </section>
          ))}
        </div>
      )}

      <Modal
        open={modalOpen}
        onClose={() => setModalOpen(false)}
        title="Add timetable slot"
        footer={
          <>
            <button onClick={() => setModalOpen(false)} className="px-4 py-2 text-sm rounded-lg hover:bg-navy-900/5">Cancel</button>
            <button form="create-slot-form" type="submit" disabled={saving} className="px-4 py-2 text-sm rounded-lg bg-navy-900 text-white hover:bg-navy-800 disabled:opacity-60">
              {saving ? 'Creating…' : 'Create'}
            </button>
          </>
        }
      >
        <form id="create-slot-form" onSubmit={handleCreate}>
          <FormField label="Subject">
            <select required value={form.subjectId} onChange={(e) => setForm({ ...form, subjectId: e.target.value })} className="w-full rounded-lg border border-black/10 px-3 py-2 text-sm outline-none focus:border-navy-700">
              <option value="" disabled>Select subject</option>
              {subjects.map((s) => <option key={s.id} value={s.id}>{s.name} ({s.code}){s.facultyName ? ` — ${s.facultyName}` : ''}</option>)}
            </select>
          </FormField>
          <div className="grid grid-cols-2 gap-3">
            <FormField label="Section">
              <input required value={form.section} onChange={(e) => setForm({ ...form, section: e.target.value })} className="w-full rounded-lg border border-black/10 px-3 py-2 text-sm outline-none focus:border-navy-700" />
            </FormField>
            <FormField label="Semester">
              <input required value={form.semester} onChange={(e) => setForm({ ...form, semester: e.target.value })} className="w-full rounded-lg border border-black/10 px-3 py-2 text-sm outline-none focus:border-navy-700" />
            </FormField>
          </div>
          <FormField label="Day">
            <select value={form.dayOfWeek} onChange={(e) => setForm({ ...form, dayOfWeek: e.target.value })} className="w-full rounded-lg border border-black/10 px-3 py-2 text-sm outline-none focus:border-navy-700">
              {DAYS.map((d) => <option key={d} value={d}>{d.toLowerCase()}</option>)}
            </select>
          </FormField>
          <div className="grid grid-cols-2 gap-3">
            <FormField label="Start time">
              <input required type="time" value={form.startTime} onChange={(e) => setForm({ ...form, startTime: e.target.value })} className="w-full rounded-lg border border-black/10 px-3 py-2 text-sm outline-none focus:border-navy-700" />
            </FormField>
            <FormField label="End time">
              <input required type="time" value={form.endTime} onChange={(e) => setForm({ ...form, endTime: e.target.value })} className="w-full rounded-lg border border-black/10 px-3 py-2 text-sm outline-none focus:border-navy-700" />
            </FormField>
          </div>
          <FormField label="Room">
            <input value={form.room} onChange={(e) => setForm({ ...form, room: e.target.value })} placeholder="e.g. LT-101" className="w-full rounded-lg border border-black/10 px-3 py-2 text-sm outline-none focus:border-navy-700" />
          </FormField>
        </form>
      </Modal>
    </div>
  );
}