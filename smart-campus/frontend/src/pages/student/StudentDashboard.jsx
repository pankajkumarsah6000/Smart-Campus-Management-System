import { useEffect, useState } from 'react';
import { ClipboardCheck, FileWarning, Award, CalendarClock } from 'lucide-react';
import StatCard from '../../components/StatCard';
import LoadingState from '../../components/LoadingState';
import ErrorState from '../../components/ErrorState';
import EmptyState from '../../components/EmptyState';
import Badge from '../../components/Badge';
import { studentService } from '../../services/studentService';
import { useAuth } from '../../context/AuthContext';

export default function StudentDashboard() {
  const [data, setData] = useState(null);
  const [status, setStatus] = useState('loading');
  const { user } = useAuth();

  const load = () => {
    setStatus('loading');
    studentService.getMyDashboard().then((d) => { setData(d); setStatus('ready'); }).catch(() => setStatus('error'));
  };
  useEffect(load, []);

  if (status === 'loading') return <LoadingState label="Loading your dashboard" />;
  if (status === 'error') return <ErrorState onRetry={load} />;

  return (
    <div>
      <div className="mb-6">
        <p className="ledger-index uppercase text-ink-muted">Student Dashboard</p>
        <h1 className="font-display text-2xl text-ink dark:text-white">Welcome, {user?.firstName}</h1>
      </div>

      <div className="grid grid-cols-2 lg:grid-cols-4 gap-4 mb-8">
        <StatCard
          label="Attendance"
          value={`${data.attendancePercentage.toFixed(0)}%`}
          icon={ClipboardCheck}
          accent={data.attendancePercentage >= 75 ? 'success' : 'danger'}
        />
        <StatCard label="Assignments" value={`${data.pendingAssignments} Pending`} icon={FileWarning} accent={data.pendingAssignments > 0 ? 'warning' : 'success'} />
        <StatCard label="CGPA" value={data.cgpa ?? '—'} icon={Award} />
        <StatCard label="Upcoming Exam" value={data.nextExamInfo || '—'} icon={CalendarClock} />
      </div>

      <div className="grid lg:grid-cols-2 gap-6">
        <section className="rounded-xl border border-black/8 dark:border-white/10 bg-white dark:bg-navy-800">
          <h2 className="font-display text-base px-5 py-4 border-b border-black/8 dark:border-white/10">Today's timetable</h2>
          {data.todaysTimetable?.length ? (
            <ul className="divide-y divide-black/5 dark:divide-white/5">
              {data.todaysTimetable.map((slot) => (
                <li key={slot.id} className="px-5 py-3 flex items-center justify-between text-sm">
                  <div>
                    <p className="font-medium text-ink dark:text-white">{slot.subjectName}</p>
                    <p className="text-xs text-ink-muted">{slot.facultyName} · {slot.room}</p>
                  </div>
                  <span className="font-mono text-xs text-ink-muted">{slot.startTime}–{slot.endTime}</span>
                </li>
              ))}
            </ul>
          ) : <EmptyState message="No classes scheduled today" />}
        </section>

        <section className="rounded-xl border border-black/8 dark:border-white/10 bg-white dark:bg-navy-800">
          <h2 className="font-display text-base px-5 py-4 border-b border-black/8 dark:border-white/10">Recent notices</h2>
          {data.recentNotices?.length ? (
            <ul className="divide-y divide-black/5 dark:divide-white/5">
              {data.recentNotices.map((n) => (
                <li key={n.id} className="px-5 py-3 text-sm">
                  <p className="font-medium text-ink dark:text-white">{n.title}</p>
                  <p className="text-xs text-ink-muted line-clamp-2">{n.content}</p>
                </li>
              ))}
            </ul>
          ) : <EmptyState message="No notices yet" />}
        </section>

        <section className="rounded-xl border border-black/8 dark:border-white/10 bg-white dark:bg-navy-800 lg:col-span-2">
          <h2 className="font-display text-base px-5 py-4 border-b border-black/8 dark:border-white/10">Upcoming assignments</h2>
          {data.upcomingAssignments?.length ? (
            <ul className="divide-y divide-black/5 dark:divide-white/5">
              {data.upcomingAssignments.map((a) => (
                <li key={a.id} className="px-5 py-3 flex items-center justify-between text-sm">
                  <div>
                    <p className="font-medium text-ink dark:text-white">{a.title}</p>
                    <p className="text-xs text-ink-muted">{a.subjectName}</p>
                  </div>
                  <div className="flex items-center gap-3">
                    {a.dueDate && <span className="text-xs text-ink-muted font-mono">{new Date(a.dueDate).toLocaleDateString()}</span>}
                    <Badge tone={a.submitted ? 'success' : 'warning'}>{a.submitted ? 'Submitted' : 'Pending'}</Badge>
                  </div>
                </li>
              ))}
            </ul>
          ) : <EmptyState message="No assignments due" />}
        </section>
      </div>
    </div>
  );
}
