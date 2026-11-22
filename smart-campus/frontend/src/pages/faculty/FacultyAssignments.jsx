import { useEffect, useState } from 'react';
import { Plus, Trash2, Users } from 'lucide-react';
import Modal from '../../components/Modal';
import FormField from '../../components/FormField';
import Badge from '../../components/Badge';
import LoadingState from '../../components/LoadingState';
import ErrorState from '../../components/ErrorState';
import EmptyState from '../../components/EmptyState';
import { assignmentService } from '../../services/assignmentService';
import { facultyService } from '../../services/facultyService';
import { useToast } from '../../context/ToastContext';

const emptyForm = { title: '', description: '', subjectId: '', dueDate: '', maxMarks: '', attachmentUrl: '' };

export default function FacultyAssignments() {
  const [assignments, setAssignments] = useState([]);
  const [subjects, setSubjects] = useState([]);
  const [status, setStatus] = useState('loading');
  const [modalOpen, setModalOpen] = useState(false);
  const [form, setForm] = useState(emptyForm);
  const [saving, setSaving] = useState(false);
  const [viewing, setViewing] = useState(null);
  const [submissions, setSubmissions] = useState([]);
  const { showToast } = useToast();

  const load = () => {
    setStatus('loading');
    Promise.all([assignmentService.my(), facultyService.subjects()])
      .then(([a, s]) => { setAssignments(a || []); setSubjects(s || []); setStatus('ready'); })
      .catch(() => setStatus('error'));
  };
  useEffect(load, []);

  const handleCreate = async (e) => {
    e.preventDefault();
    setSaving(true);
    try {
      await assignmentService.create({
        title: form.title,
        description: form.description,
        subjectId: Number(form.subjectId),
        dueDate: form.dueDate ? new Date(form.dueDate).toISOString() : null,
        maxMarks: form.maxMarks ? Number(form.maxMarks) : null,
        attachmentUrl: form.attachmentUrl || null,
      });
      showToast('Assignment created', 'success');
      setModalOpen(false);
      setForm(emptyForm);
      load();
    } catch (err) {
      showToast(err.response?.data?.message || 'Could not create assignment', 'error');
    } finally {
      setSaving(false);
    }
  };

  const handleDelete = async (id) => {
    if (!window.confirm('Delete this assignment?')) return;
    try {
      await assignmentService.remove(id);
      showToast('Assignment deleted', 'success');
      load();
    } catch {
      showToast('Could not delete assignment', 'error');
    }
  };

  const viewSubmissions = async (a) => {
    setViewing(a);
    try {
      setSubmissions(await assignmentService.submissions(a.id));
    } catch {
      setSubmissions([]);
    }
  };

  const grade = async (sub) => {
    const marks = window.prompt(`Marks awarded (max ${sub.maxMarks ?? ''}):`, sub.marksAwarded ?? '');
    if (marks == null) return;
    const feedback = window.prompt('Feedback:', sub.feedback ?? '') ?? '';
    try {
      await assignmentService.grade(sub.id, { marksAwarded: marks === '' ? null : Number(marks), feedback });
      showToast('Submission graded', 'success');
      viewSubmissions(viewing);
    } catch (err) {
      showToast(err.response?.data?.message || 'Could not grade submission', 'error');
    }
  };

  if (status === 'loading') return <LoadingState label="Loading assignments" />;
  if (status === 'error') return <ErrorState onRetry={load} />;

  const activeSubmissions = viewing
    ? submissions.filter((s) => s.status !== 'EVALUATED')
    : [];

  return (
    <div>
      <div className="flex items-center justify-between mb-6">
        <div>
          <p className="ledger-index uppercase text-ink-muted">Faculty · Assignments</p>
          <h1 className="font-display text-2xl text-ink dark:text-white">Assignments</h1>
        </div>
        <button onClick={() => setModalOpen(true)} className="flex items-center gap-2 rounded-lg bg-navy-900 text-white px-4 py-2 text-sm font-medium hover:bg-navy-800">
          <Plus size={16} /> New assignment
        </button>
      </div>

      {assignments.length === 0 ? (
        <div className="rounded-xl border border-black/8 dark:border-white/10 bg-white dark:bg-navy-800">
          <EmptyState message="No assignments created yet" />
        </div>
      ) : (
        <div className="space-y-3">
          {assignments.map((a) => (
            <div key={a.id} className="rounded-xl border border-black/8 dark:border-white/10 bg-white dark:bg-navy-800 p-4">
              <div className="flex items-start justify-between gap-3">
                <div className="min-w-0">
                  <h3 className="font-medium text-ink dark:text-white">{a.title}</h3>
                  <p className="text-sm text-ink-muted">{a.subjectName}</p>
                  {a.description && <p className="text-sm text-ink-muted mt-1 line-clamp-2">{a.description}</p>}
                  <p className="text-xs text-ink-muted mt-2">
                    Due {a.dueDate ? new Date(a.dueDate).toLocaleString() : '—'}
                    {a.maxMarks ? ` · ${a.maxMarks} marks` : ''} · {a.submissionCount ?? 0} submissions
                  </p>
                </div>
                <div className="flex items-center gap-1 shrink-0">
                  <button onClick={() => viewSubmissions(a)} className="p-2 rounded hover:bg-navy-900/5 dark:hover:bg-white/5 text-ink-muted" aria-label="View submissions">
                    <Users size={16} />
                  </button>
                  <button onClick={() => handleDelete(a.id)} className="p-2 rounded hover:bg-[var(--color-danger-soft)] text-[var(--color-danger)]" aria-label="Delete assignment">
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
        title="Create assignment"
        footer={
          <>
            <button onClick={() => setModalOpen(false)} className="px-4 py-2 text-sm rounded-lg hover:bg-navy-900/5">Cancel</button>
            <button form="create-assignment-form" type="submit" disabled={saving} className="px-4 py-2 text-sm rounded-lg bg-navy-900 text-white hover:bg-navy-800 disabled:opacity-60">
              {saving ? 'Creating…' : 'Create'}
            </button>
          </>
        }
      >
        <form id="create-assignment-form" onSubmit={handleCreate}>
          <FormField label="Title">
            <input required value={form.title} onChange={(e) => setForm({ ...form, title: e.target.value })} className="w-full rounded-lg border border-black/10 px-3 py-2 text-sm outline-none focus:border-navy-700" />
          </FormField>
          <FormField label="Subject">
            <select required value={form.subjectId} onChange={(e) => setForm({ ...form, subjectId: e.target.value })} className="w-full rounded-lg border border-black/10 px-3 py-2 text-sm outline-none focus:border-navy-700">
              <option value="" disabled>Select subject</option>
              {subjects.map((s) => <option key={s.id} value={s.id}>{s.name} ({s.code})</option>)}
            </select>
          </FormField>
          <FormField label="Description">
            <textarea rows={3} value={form.description} onChange={(e) => setForm({ ...form, description: e.target.value })} className="w-full rounded-lg border border-black/10 px-3 py-2 text-sm outline-none focus:border-navy-700" />
          </FormField>
          <div className="grid grid-cols-2 gap-3">
            <FormField label="Due date">
              <input type="datetime-local" value={form.dueDate} onChange={(e) => setForm({ ...form, dueDate: e.target.value })} className="w-full rounded-lg border border-black/10 px-3 py-2 text-sm outline-none focus:border-navy-700" />
            </FormField>
            <FormField label="Max marks">
              <input type="number" min="1" value={form.maxMarks} onChange={(e) => setForm({ ...form, maxMarks: e.target.value })} className="w-full rounded-lg border border-black/10 px-3 py-2 text-sm outline-none focus:border-navy-700" />
            </FormField>
          </div>
          <FormField label="Attachment URL (optional)">
            <input value={form.attachmentUrl} onChange={(e) => setForm({ ...form, attachmentUrl: e.target.value })} className="w-full rounded-lg border border-black/10 px-3 py-2 text-sm outline-none focus:border-navy-700" />
          </FormField>
        </form>
      </Modal>

      <Modal
        open={!!viewing}
        onClose={() => setViewing(null)}
        title={viewing ? `Submissions — ${viewing.title}` : 'Submissions'}
        footer={
          <button onClick={() => setViewing(null)} className="px-4 py-2 text-sm rounded-lg bg-navy-900 text-white hover:bg-navy-800">Close</button>
        }
      >
        {submissions.length === 0 ? (
          <p className="text-sm text-ink-muted py-4 text-center">No submissions yet</p>
        ) : (
          <ul className="divide-y divide-black/5 dark:divide-white/5">
            {(activeSubmissions.length ? activeSubmissions : submissions).map((s) => (
              <li key={s.id} className="py-3 text-sm">
                <div className="flex items-center justify-between gap-2">
                  <div className="min-w-0">
                    <p className="font-medium text-ink dark:text-white">{s.studentName} <span className="text-xs text-ink-muted">({s.rollNumber})</span></p>
                    <p className="text-xs text-ink-muted">{new Date(s.submittedAt).toLocaleString()}</p>
                    {s.textAnswer && <p className="text-xs text-ink-muted mt-1 line-clamp-3">{s.textAnswer}</p>}
                    {s.fileUrl && <a href={s.fileUrl} target="_blank" rel="noreferrer" className="text-xs text-[var(--color-gold)] underline">{s.fileUrl}</a>}
                    {s.feedback && <p className="text-xs text-ink-muted mt-1">Feedback: {s.feedback}</p>}
                  </div>
                  <div className="flex items-center gap-2 shrink-0">
                    <Badge tone={s.status === 'EVALUATED' ? 'success' : 'warning'}>{s.status}</Badge>
                    {s.marksAwarded != null && <span className="font-mono text-xs">{s.marksAwarded}/{s.maxMarks}</span>}
                    {s.status !== 'EVALUATED' && (
                      <button onClick={() => grade(s)} className="rounded-lg bg-navy-900 text-white px-2.5 py-1.5 text-xs font-medium hover:bg-navy-800">Grade</button>
                    )}
                  </div>
                </div>
              </li>
            ))}
          </ul>
        )}
      </Modal>
    </div>
  );
}