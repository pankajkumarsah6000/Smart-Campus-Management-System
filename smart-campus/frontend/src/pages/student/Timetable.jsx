import { useEffect, useState } from 'react';
import { CalendarClock } from 'lucide-react';
import LoadingState from '../../components/LoadingState';
import ErrorState from '../../components/ErrorState';
import EmptyState from '../../components/EmptyState';
import { timetableService } from '../../services/timetableService';

const DAYS = ['MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY', 'SUNDAY'];

export default function Timetable() {
  const [slots, setSlots] = useState([]);
  const [status, setStatus] = useState('loading');

  const load = () => {
    setStatus('loading');
    timetableService.my().then((d) => { setSlots(d || []); setStatus('ready'); }).catch(() => setStatus('error'));
  };
  useEffect(load, []);

  if (status === 'loading') return <LoadingState label="Loading timetable" />;
  if (status === 'error') return <ErrorState onRetry={load} />;

  return (
    <div>
      <div className="mb-6">
        <p className="ledger-index uppercase text-ink-muted">Student · Timetable</p>
        <h1 className="font-display text-2xl text-ink dark:text-white">Weekly timetable</h1>
      </div>

      {slots.length === 0 ? (
        <div className="rounded-xl border border-black/8 dark:border-white/10 bg-white dark:bg-navy-800">
          <EmptyState message="No classes scheduled" />
        </div>
      ) : (
        <div className="grid lg:grid-cols-2 gap-6">
          {DAYS.filter((d) => slots.some((s) => s.dayOfWeek === d)).map((day) => (
            <section key={day} className="rounded-xl border border-black/8 dark:border-white/10 bg-white dark:bg-navy-800">
              <h2 className="font-display text-base px-5 py-4 border-b border-black/8 dark:border-white/10 flex items-center gap-2 capitalize">
                <CalendarClock size={16} /> {day.toLowerCase()}
              </h2>
              <ul className="divide-y divide-black/5 dark:divide-white/5">
                {slots
                  .filter((s) => s.dayOfWeek === day)
                  .sort((a, b) => a.startTime.localeCompare(b.startTime))
                  .map((s) => (
                    <li key={s.id} className="px-5 py-3 flex items-center justify-between text-sm">
                      <div>
                        <p className="font-medium text-ink dark:text-white">{s.subjectName}</p>
                        <p className="text-xs text-ink-muted">{s.facultyName || '—'} · {s.room || 'Room TBA'}</p>
                      </div>
                      <span className="font-mono text-xs text-ink-muted">{s.startTime}–{s.endTime}</span>
                    </li>
                  ))}
              </ul>
            </section>
          ))}
        </div>
      )}
    </div>
  );
}