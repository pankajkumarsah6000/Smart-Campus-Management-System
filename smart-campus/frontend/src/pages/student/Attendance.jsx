import { useEffect, useState } from 'react';
import LoadingState from '../../components/LoadingState';
import ErrorState from '../../components/ErrorState';
import Badge from '../../components/Badge';
import { attendanceService } from '../../services/attendanceService';

const statusTone = { PRESENT: 'success', ABSENT: 'danger', LATE: 'warning', EXCUSED: 'info' };

export default function Attendance() {
  const [data, setData] = useState(null);
  const [status, setStatus] = useState('loading');

  const load = () => {
    setStatus('loading');
    attendanceService.getMySummary().then((d) => { setData(d); setStatus('ready'); }).catch(() => setStatus('error'));
  };
  useEffect(load, []);

  if (status === 'loading') return <LoadingState label="Loading attendance" />;
  if (status === 'error') return <ErrorState onRetry={load} />;

  return (
    <div>
      <div className="mb-6">
        <p className="ledger-index uppercase text-ink-muted">Student · Attendance</p>
        <h1 className="font-display text-2xl text-ink dark:text-white">Your attendance</h1>
      </div>

      <div className="stat-card rounded-xl p-5 mb-6 max-w-xs">
        <p className="ledger-index uppercase text-ink-muted">Overall</p>
        <p className={`font-mono text-4xl mt-2 ${data.overallPercentage >= 75 ? 'text-[var(--color-success)]' : 'text-[var(--color-danger)]'}`}>
          {data.overallPercentage.toFixed(1)}%
        </p>
      </div>

      <div className="grid md:grid-cols-2 gap-6">
        <section className="rounded-xl border border-black/8 dark:border-white/10 bg-white dark:bg-navy-800">
          <h2 className="font-display text-base px-5 py-4 border-b border-black/8 dark:border-white/10">By subject</h2>
          <ul className="divide-y divide-black/5 dark:divide-white/5">
            {(data.bySubject || []).map((s) => (
              <li key={s.subjectName} className="px-5 py-3 flex items-center justify-between text-sm">
                <span className="font-medium text-ink dark:text-white">{s.subjectName}</span>
                <span className="font-mono text-xs text-ink-muted">{s.present}/{s.total} ({s.percentage.toFixed(0)}%)</span>
              </li>
            ))}
          </ul>
        </section>

        <section className="rounded-xl border border-black/8 dark:border-white/10 bg-white dark:bg-navy-800">
          <h2 className="font-display text-base px-5 py-4 border-b border-black/8 dark:border-white/10">Recent records</h2>
          <ul className="divide-y divide-black/5 dark:divide-white/5 max-h-80 overflow-y-auto">
            {(data.recentRecords || []).map((r) => (
              <li key={r.id} className="px-5 py-3 flex items-center justify-between text-sm">
                <div>
                  <p className="text-ink dark:text-white">{r.subjectName}</p>
                  <p className="text-xs text-ink-muted">{r.attendanceDate}</p>
                </div>
                <Badge tone={statusTone[r.status] || 'neutral'}>{r.status}</Badge>
              </li>
            ))}
          </ul>
        </section>
      </div>
    </div>
  );
}
