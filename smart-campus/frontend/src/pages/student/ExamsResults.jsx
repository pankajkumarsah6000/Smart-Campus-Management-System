import { useEffect, useState } from 'react';
import { CalendarDays, FileText } from 'lucide-react';
import Badge from '../../components/Badge';
import LoadingState from '../../components/LoadingState';
import ErrorState from '../../components/ErrorState';
import EmptyState from '../../components/EmptyState';
import { examinationService } from '../../services/examinationService';

export default function ExamsResults() {
  const [exams, setExams] = useState([]);
  const [results, setResults] = useState([]);
  const [status, setStatus] = useState('loading');

  const load = () => {
    setStatus('loading');
    Promise.all([examinationService.myExams(), examinationService.myResults()])
      .then(([e, r]) => { setExams(e || []); setResults(r || []); setStatus('ready'); })
      .catch(() => setStatus('error'));
  };
  useEffect(load, []);

  if (status === 'loading') return <LoadingState label="Loading exams and results" />;
  if (status === 'error') return <ErrorState onRetry={load} />;

  return (
    <div>
      <div className="mb-6">
        <p className="ledger-index uppercase text-ink-muted">Student · Exams</p>
        <h1 className="font-display text-2xl text-ink dark:text-white">Exams &amp; results</h1>
      </div>

      <div className="grid lg:grid-cols-2 gap-6">
        <section className="rounded-xl border border-black/8 dark:border-white/10 bg-white dark:bg-navy-800">
          <h2 className="font-display text-base px-5 py-4 border-b border-black/8 dark:border-white/10 flex items-center gap-2">
            <CalendarDays size={16} /> Exam schedule
          </h2>
          {exams.length === 0 ? (
            <EmptyState message="No exams scheduled for your subjects" />
          ) : (
            <ul className="divide-y divide-black/5 dark:divide-white/5">
              {exams.map((ex) => (
                <li key={ex.id} className="px-5 py-3 flex items-center justify-between text-sm">
                  <div>
                    <p className="font-medium text-ink dark:text-white">{ex.name}</p>
                    <p className="text-xs text-ink-muted">{ex.subjectName} · {ex.venue || 'Venue TBA'}</p>
                  </div>
                  <div className="text-right">
                    <p className="font-mono text-xs text-ink-muted">
                      {ex.examDate ? new Date(ex.examDate).toLocaleString() : '—'}
                    </p>
                    <p className="font-mono text-xs text-ink-muted">{ex.durationMinutes} min · {ex.maxMarks} marks</p>
                  </div>
                </li>
              ))}
            </ul>
          )}
        </section>

        <section className="rounded-xl border border-black/8 dark:border-white/10 bg-white dark:bg-navy-800">
          <h2 className="font-display text-base px-5 py-4 border-b border-black/8 dark:border-white/10 flex items-center gap-2">
            <FileText size={16} /> Published results
          </h2>
          {results.length === 0 ? (
            <EmptyState message="No results published yet" />
          ) : (
            <ul className="divide-y divide-black/5 dark:divide-white/5">
              {results.map((r) => (
                <li key={r.id} className="px-5 py-3 flex items-center justify-between text-sm">
                  <div>
                    <p className="font-medium text-ink dark:text-white">{r.examinationName}</p>
                    <p className="text-xs text-ink-muted">{r.subjectName}</p>
                  </div>
                  <div className="text-right">
                    <p className="font-mono text-sm text-ink dark:text-white">
                      {r.marksObtained ?? '—'} / {r.maxMarks ?? '—'}
                      {r.percentage != null && <span className="text-xs text-ink-muted"> ({r.percentage.toFixed(1)}%)</span>}
                    </p>
                    <div className="flex justify-end gap-2 mt-0.5">
                      <Badge tone={r.passed ? 'success' : 'danger'}>{r.passed ? 'Pass' : 'Fail'}</Badge>
                      {r.grade && <Badge>{r.grade}</Badge>}
                    </div>
                  </div>
                </li>
              ))}
            </ul>
          )}
        </section>
      </div>
    </div>
  );
}