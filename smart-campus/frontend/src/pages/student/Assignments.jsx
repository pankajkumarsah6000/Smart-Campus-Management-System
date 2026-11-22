import { useEffect, useState } from 'react';
import { Send } from 'lucide-react';
import Modal from '../../components/Modal';
import FormField from '../../components/FormField';
import Badge from '../../components/Badge';
import LoadingState from '../../components/LoadingState';
import ErrorState from '../../components/ErrorState';
import EmptyState from '../../components/EmptyState';
import { assignmentService } from '../../services/assignmentService';
import { useToast } from '../../context/ToastContext';

const statusTone = { SUBMITTED: 'info', LATE: 'warning', EVALUATED: 'success' };

export default function Assignments() {
  const [assignments, setAssignments] = useState([]);
  const [status, setStatus] = useState('loading');
  const [submitFor, setSubmitFor] = useState(null);
  const [form, setForm] = useState({ textAnswer: '', fileUrl: '' });
  const [saving, setSaving] = useState(false);
  const { showToast } = useToast();

  const load = () => {
    setStatus('loading');
    assignmentService.my().then((d) => { setAssignments(d); setStatus('ready'); }).catch(() => setStatus('error'));
  };
  useEffect(load, []);

  const openSubmit = (a) => { setSubmitFor(a); setForm({ textAnswer: '', fileUrl: '' }); };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setSaving(true);
    try {
      await assignmentService.submit(submitFor.id, form);
      showToast('Assignment submitted', 'success');
      setSubmitFor(null);
      load();
    } catch (err) {
      showToast(err.response?.data?.message || 'Could not submit assignment', 'error');
    } finally {
      setSaving(false);
    }
  };

  if (status === 'loading') return <LoadingState label="Loading assignments" />;
  if (status === 'error') return <ErrorState onRetry={load} />;

  return (
    <div>
      <div className="mb-6">
        <p className="ledger-index uppercase text-ink-muted">Student · Assignments</p>
        <h1 className="font-display text-2xl text-ink dark:text-white">Assignments</h1>
      </div>

      {assignments.length === 0 ? (
        <div className="rounded-xl border border-black/8 dark:border-white/10 bg-white dark:bg-navy-800">
          <EmptyState message="No assignments posted for your subjects yet" />
        </div>
      ) : (
        <div className="space-y-3">
          {assignments.map((a) => (
            <div key={a.id} className="rounded-xl border border-black/8 dark:border-white/10 bg-white dark:bg-navy-800 p-4">
              <div className="flex items-start justify-between gap-3">
                <div className="min-w-0">
                  <div className="flex flex-wrap items-center gap-2 mb-1">
                    <h3 className="font-medium text-ink dark:text-white">{a.title}</h3>
                    {a.submitted ? (
                      <Badge tone={statusTone[a.submissionStatus] || 'success'}>
                        {a.submissionStatus === 'EVALUATED' ? 'Graded' : 'Submitted'}
                      </Badge>
                    ) : (
                      <Badge tone="warning">Pending</Badge>
                    )}
                  </div>
                  <p className="text-sm text-ink-muted">{a.subjectName} · {a.createdByFacultyName}</p>
                  {a.description && <p className="text-sm text-ink-muted mt-1.5 line-clamp-2">{a.description}</p>}
                  <p className="text-xs text-ink-muted mt-2">
                    Due {a.dueDate ? new Date(a.dueDate).toLocaleString() : '—'}
                    {a.maxMarks ? ` · ${a.maxMarks} marks` : ''}
                    {a.awardedMarks != null ? ` · scored ${a.awardedMarks}` : ''}
                  </p>
                </div>
                <button
                  onClick={() => openSubmit(a)}
                  disabled={a.submitted}
                  className="flex shrink-0 items-center gap-2 rounded-lg bg-navy-900 text-white px-3 py-2 text-sm font-medium hover:bg-navy-800 disabled:opacity-50"
                >
                  <Send size={14} /> {a.submitted ? 'Submitted' : 'Submit'}
                </button>
              </div>
            </div>
          ))}
        </div>
      )}

      <Modal
        open={!!submitFor}
        onClose={() => setSubmitFor(null)}
        title={submitFor ? `Submit — ${submitFor.title}` : 'Submit assignment'}
        footer={
          <>
            <button onClick={() => setSubmitFor(null)} className="px-4 py-2 text-sm rounded-lg hover:bg-navy-900/5">Cancel</button>
            <button form="submit-assignment-form" type="submit" disabled={saving} className="px-4 py-2 text-sm rounded-lg bg-navy-900 text-white hover:bg-navy-800 disabled:opacity-60">
              {saving ? 'Submitting…' : 'Submit'}
            </button>
          </>
        }
      >
        <form id="submit-assignment-form" onSubmit={handleSubmit}>
          <FormField label="Text answer">
            <textarea required={!form.fileUrl} rows={5} value={form.textAnswer} onChange={(e) => setForm({ ...form, textAnswer: e.target.value })} placeholder="Type your answer here or paste a file URL below" className="w-full rounded-lg border border-black/10 px-3 py-2 text-sm outline-none focus:border-navy-700" />
          </FormField>
          <FormField label="File URL (optional)">
            <input value={form.fileUrl} onChange={(e) => setForm({ ...form, fileUrl: e.target.value })} placeholder="https://…" className="w-full rounded-lg border border-black/10 px-3 py-2 text-sm outline-none focus:border-navy-700" />
          </FormField>
        </form>
      </Modal>
    </div>
  );
}