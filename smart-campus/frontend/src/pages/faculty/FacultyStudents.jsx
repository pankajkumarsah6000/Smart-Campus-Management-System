import { useEffect, useState } from 'react';
import { BookOpen } from 'lucide-react';
import LoadingState from '../../components/LoadingState';
import ErrorState from '../../components/ErrorState';
import EmptyState from '../../components/EmptyState';
import { facultyService } from '../../services/facultyService';

export default function FacultyStudents() {
  const [subjects, setSubjects] = useState([]);
  const [selected, setSelected] = useState('');
  const [students, setStudents] = useState([]);
  const [status, setStatus] = useState('loading');
  const [listStatus, setListStatus] = useState('idle');

  const load = () => {
    setStatus('loading');
    facultyService.subjects().then((s) => { setSubjects(s || []); setStatus('ready'); }).catch(() => setStatus('error'));
  };
  useEffect(load, []);

  const selectSubject = async (subjectId) => {
    setSelected(subjectId);
    setListStatus('loading');
    try {
      setStudents(await facultyService.studentsForSubject(subjectId));
      setListStatus('ready');
    } catch {
      setListStatus('error');
    }
  };

  if (status === 'loading') return <LoadingState label="Loading subjects" />;
  if (status === 'error') return <ErrorState onRetry={load} />;

  return (
    <div>
      <div className="mb-6">
        <p className="ledger-index uppercase text-ink-muted">Faculty · Students</p>
        <h1 className="font-display text-2xl text-ink dark:text-white">Students by subject</h1>
      </div>

      {subjects.length === 0 ? (
        <div className="rounded-xl border border-black/8 dark:border-white/10 bg-white dark:bg-navy-800">
          <EmptyState message="No subjects assigned to you" />
        </div>
      ) : (
        <>
          <div className="flex flex-wrap gap-2 mb-6">
            {subjects.map((s) => (
              <button
                key={s.id}
                onClick={() => selectSubject(s.id)}
                className={`flex items-center gap-2 rounded-lg border px-3 py-2 text-sm font-medium transition-colors ${
                  selected === s.id
                    ? 'border-navy-900 bg-navy-900 text-white'
                    : 'border-black/10 bg-white dark:bg-navy-800 text-ink dark:text-white hover:border-navy-700'
                }`}
              >
                <BookOpen size={14} /> {s.name}
              </button>
            ))}
          </div>

          {listStatus === 'loading' && <LoadingState label="Loading students" />}
          {listStatus === 'error' && <ErrorState onRetry={() => selectSubject(selected)} />}
          {listStatus === 'ready' && (
            students.length === 0 ? (
              <div className="rounded-xl border border-black/8 dark:border-white/10 bg-white dark:bg-navy-800">
                <EmptyState message="No students enrolled in this subject" />
              </div>
            ) : (
              <section className="rounded-xl border border-black/8 dark:border-white/10 bg-white dark:bg-navy-800">
                <h2 className="font-display text-base px-5 py-4 border-b border-black/8 dark:border-white/10">
                  {subjects.find((s) => s.id === selected)?.name} · {students.length} student{students.length === 1 ? '' : 's'}
                </h2>
                <ul className="divide-y divide-black/5 dark:divide-white/5">
                  {students.map((st) => (
                    <li key={st.id} className="px-5 py-3 flex items-center justify-between text-sm">
                      <div>
                        <p className="font-medium text-ink dark:text-white">{st.firstName} {st.lastName}</p>
                        <p className="text-xs text-ink-muted">{st.email}</p>
                      </div>
                      <div className="text-right shrink-0">
                        <p className="font-mono text-xs text-ink-muted">{st.rollNumber}</p>
                        <p className="text-xs text-ink-muted">Sem {st.semester} · {st.section}</p>
                      </div>
                    </li>
                  ))}
                </ul>
              </section>
            )
          )}
        </>
      )}
    </div>
  );
}