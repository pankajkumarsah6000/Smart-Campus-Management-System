import { useEffect, useState } from 'react';
import { Plus, Trash2, ClipboardList, PenLine } from 'lucide-react';
import Modal from '../../components/Modal';
import FormField from '../../components/FormField';
import Badge from '../../components/Badge';
import LoadingState from '../../components/LoadingState';
import ErrorState from '../../components/ErrorState';
import EmptyState from '../../components/EmptyState';
import { examinationService } from '../../services/examinationService';
import { facultyService } from '../../services/facultyService';
import { useToast } from '../../context/ToastContext';

const emptyForm = { name: '', subjectId: '', examDate: '', durationMinutes: '', maxMarks: '', venue: '' };

export default function FacultyExams() {
  const [exams, setExams] = useState([]);
  const [subjects, setSubjects] = useState([]);
  const [status, setStatus] = useState('loading');
  const [modalOpen, setModalOpen] = useState(false);
  const [form, setForm] = useState(emptyForm);
  const [saving, setSaving] = useState(false);
  const [viewing, setViewing] = useState(null);
  const [results, setResults] = useState([]);
  const [recording, setRecording] = useState(null);
  const [recordStudents, setRecordStudents] = useState([]);
  const [recordForm, setRecordForm] = useState({ studentId: '', marksObtained: '' });
  const [recordSaving, setRecordSaving] = useState(false);
  const { showToast } = useToast();

  const load = () => {
    setStatus('loading');
    Promise.all([examinationService.list(), facultyService.subjects()])
      .then(([e, s]) => { setExams(e || []); setSubjects(s || []); setStatus('ready'); })
      .catch(() => setStatus('error'));
  };
  useEffect(load, []);

  const handleCreate = async (e) => {
    e.preventDefault();
    setSaving(true);
    try {
      await examinationService.create({
        name: form.name,
        subjectId: Number(form.subjectId),
        examDate: form.examDate ? new Date(form.examDate).toISOString() : null,
        durationMinutes: form.durationMinutes ? Number(form.durationMinutes) : null,
        maxMarks: form.maxMarks ? Number(form.maxMarks) : null,
        venue: form.venue || null,
      });
      showToast('Exam scheduled', 'success');
      setModalOpen(false);
      setForm(emptyForm);
      load();
    } catch (err) {
      showToast(err.response?.data?.message || 'Could not create exam', 'error');
    } finally {
      setSaving(false);
    }
  };

  const handleDelete = async (id) => {
    if (!window.confirm('Delete this exam?')) return;
    try {
      await examinationService.remove(id);
      showToast('Exam deleted', 'success');
      load();
    } catch {
      showToast('Could not delete exam', 'error');
    }
  };

  const viewResults = async (ex) => {
    setViewing(ex);
    try {
      setResults(await examinationService.resultsForExam(ex.id));
    } catch {
      setResults([]);
    }
  };

  const openRecord = async (ex) => {
    setRecording(ex);
    setRecordForm({ studentId: '', marksObtained: '' });
    try {
      setRecordStudents(await facultyService.studentsForSubject(ex.subjectId));
    } catch {
      setRecordStudents([]);
    }
  };

  const handleRecord = async (e) => {
    e.preventDefault();
    setRecordSaving(true);
    try {
      await examinationService.recordResult(recording.id, {
        studentId: Number(recordForm.studentId),
        marksObtained: Number(recordForm.marksObtained),
      });
      showToast('Result recorded', 'success');
      setRecording(null);
      if (viewing && viewing.id === recording.id) {
        setResults(await examinationService.resultsForExam(recording.id));
      }
    } catch (err) {
      showToast(err.response?.data?.message || 'Could not record result', 'error');
    } finally {
      setRecordSaving(false);
    }
  };

  if (status === 'loading') return <LoadingState label="Loading exams" />;
  if (status === 'error') return <ErrorState onRetry={load} />;

  return (
    <div>
      <div className="flex items-center justify-between mb-6">
        <div>
          <p className="ledger-index uppercase text-ink-muted">Faculty · Exams</p>
          <h1 className="font-display text-2xl text-ink dark:text-white">Exams &amp; results</h1>
        </div>
        <button onClick={() => setModalOpen(true)} className="flex items-center gap-2 rounded-lg bg-navy-900 text-white px-4 py-2 text-sm font-medium hover:bg-navy-800">
          <Plus size={16} /> Schedule exam
        </button>
      </div>

      {exams.length === 0 ? (
        <div className="rounded-xl border border-black/8 dark:border-white/10 bg-white dark:bg-navy-800">
          <EmptyState message="No exams scheduled yet" />
        </div>
      ) : (
        <div className="space-y-3">
          {exams.map((ex) => (
            <div key={ex.id} className="rounded-xl border border-black/8 dark:border-white/10 bg-white dark:bg-navy-800 p-4">
              <div className="flex items-start justify-between gap-3">
                <div className="min-w-0">
                  <h3 className="font-medium text-ink dark:text-white">{ex.name}</h3>
                  <p className="text-sm text-ink-muted">{ex.subjectName} · {ex.venue || 'Venue TBA'}</p>
                  <p className="text-xs text-ink-muted mt-1">
                    {ex.examDate ? new Date(ex.examDate).toLocaleString() : 'Date TBA'}
                    {ex.durationMinutes ? ` · ${ex.durationMinutes} min` : ''}
                    {ex.maxMarks ? ` · ${ex.maxMarks} marks` : ''}
                  </p>
                </div>
                <div className="flex items-center gap-1 shrink-0">
                  <button onClick={() => viewResults(ex)} className="p-2 rounded hover:bg-navy-900/5 dark:hover:bg-white/5 text-ink-muted" aria-label="View results">
                    <ClipboardList size={16} />
                  </button>
                  <button onClick={() => openRecord(ex)} className="p-2 rounded hover:bg-navy-900/5 dark:hover:bg-white/5 text-ink-muted" aria-label="Record result">
                    <PenLine size={16} />
                  </button>
                  <button onClick={() => handleDelete(ex.id)} className="p-2 rounded hover:bg-[var(--color-danger-soft)] text-[var(--color-danger)]" aria-label="Delete exam">
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
        title="Schedule an exam"
        footer={
          <>
            <button onClick={() => setModalOpen(false)} className="px-4 py-2 text-sm rounded-lg hover:bg-navy-900/5">Cancel</button>
            <button form="create-exam-form" type="submit" disabled={saving} className="px-4 py-2 text-sm rounded-lg bg-navy-900 text-white hover:bg-navy-800 disabled:opacity-60">
              {saving ? 'Creating…' : 'Schedule'}
            </button>
          </>
        }
      >
        <form id="create-exam-form" onSubmit={handleCreate}>
          <FormField label="Exam name">
            <input required value={form.name} onChange={(e) => setForm({ ...form, name: e.target.value })} placeholder="e.g. Midterm — Data Structures" className="w-full rounded-lg border border-black/10 px-3 py-2 text-sm outline-none focus:border-navy-700" />
          </FormField>
          <FormField label="Subject">
            <select required value={form.subjectId} onChange={(e) => setForm({ ...form, subjectId: e.target.value })} className="w-full rounded-lg border border-black/10 px-3 py-2 text-sm outline-none focus:border-navy-700">
              <option value="" disabled>Select subject</option>
              {subjects.map((s) => <option key={s.id} value={s.id}>{s.name} ({s.code})</option>)}
            </select>
          </FormField>
          <div className="grid grid-cols-2 gap-3">
            <FormField label="Exam date">
              <input type="datetime-local" value={form.examDate} onChange={(e) => setForm({ ...form, examDate: e.target.value })} className="w-full rounded-lg border border-black/10 px-3 py-2 text-sm outline-none focus:border-navy-700" />
            </FormField>
            <FormField label="Venue">
              <input value={form.venue} onChange={(e) => setForm({ ...form, venue: e.target.value })} className="w-full rounded-lg border border-black/10 px-3 py-2 text-sm outline-none focus:border-navy-700" />
            </FormField>
          </div>
          <div className="grid grid-cols-2 gap-3">
            <FormField label="Duration (minutes)">
              <input type="number" min="1" value={form.durationMinutes} onChange={(e) => setForm({ ...form, durationMinutes: e.target.value })} className="w-full rounded-lg border border-black/10 px-3 py-2 text-sm outline-none focus:border-navy-700" />
            </FormField>
            <FormField label="Max marks">
              <input type="number" min="1" value={form.maxMarks} onChange={(e) => setForm({ ...form, maxMarks: e.target.value })} className="w-full rounded-lg border border-black/10 px-3 py-2 text-sm outline-none focus:border-navy-700" />
            </FormField>
          </div>
        </form>
      </Modal>

      <Modal
        open={!!recording}
        onClose={() => setRecording(null)}
        title={recording ? `Record result — ${recording.name}` : 'Record result'}
        footer={
          <>
            <button onClick={() => setRecording(null)} className="px-4 py-2 text-sm rounded-lg hover:bg-navy-900/5">Cancel</button>
            <button form="record-result-form" type="submit" disabled={recordSaving} className="px-4 py-2 text-sm rounded-lg bg-navy-900 text-white hover:bg-navy-800 disabled:opacity-60">
              {recordSaving ? 'Saving…' : 'Record'}
            </button>
          </>
        }
      >
        {recordStudents.length === 0 ? (
          <p className="text-sm text-ink-muted py-4 text-center">No students enrolled in this subject</p>
        ) : (
          <form id="record-result-form" onSubmit={handleRecord}>
            <FormField label="Student">
              <select required value={recordForm.studentId} onChange={(e) => setRecordForm({ ...recordForm, studentId: e.target.value })} className="w-full rounded-lg border border-black/10 px-3 py-2 text-sm outline-none focus:border-navy-700">
                <option value="" disabled>Select student</option>
                {recordStudents.map((s) => <option key={s.id} value={s.id}>{s.name} ({s.rollNumber})</option>)}
              </select>
            </FormField>
            <FormField label={`Marks obtained (out of ${recording?.maxMarks ?? 'max marks'})`}>
              <input required type="number" min="0" max={recording?.maxMarks || undefined} step="0.5" value={recordForm.marksObtained} onChange={(e) => setRecordForm({ ...recordForm, marksObtained: e.target.value })} className="w-full rounded-lg border border-black/10 px-3 py-2 text-sm outline-none focus:border-navy-700" />
            </FormField>
          </form>
        )}
      </Modal>

      <Modal
        open={!!viewing}
        onClose={() => setViewing(null)}
        title={viewing ? `Results — ${viewing.name}` : 'Results'}
        footer={
          <button onClick={() => setViewing(null)} className="px-4 py-2 text-sm rounded-lg bg-navy-900 text-white hover:bg-navy-800">Close</button>
        }
      >
        {results.length === 0 ? (
          <p className="text-sm text-ink-muted py-4 text-center">No results recorded yet</p>
        ) : (
          <ul className="divide-y divide-black/5 dark:divide-white/5">
            {results.map((r) => (
              <li key={r.id} className="py-3 flex items-center justify-between text-sm">
                <div>
                  <p className="font-medium text-ink dark:text-white">{r.studentName} <span className="text-xs text-ink-muted">({r.rollNumber})</span></p>
                  {r.remarks && <p className="text-xs text-ink-muted">{r.remarks}</p>}
                </div>
                <div className="text-right shrink-0">
                  <p className="font-mono">{r.marksObtained}/{r.maxMarks}</p>
                  <div className="flex justify-end gap-1.5 mt-0.5">
                    <Badge tone={r.passed ? 'success' : 'danger'}>{r.passed ? 'Pass' : 'Fail'}</Badge>
                    {r.grade && <Badge>{r.grade}</Badge>}
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