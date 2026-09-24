import { useEffect, useState } from 'react';
import { BookOpen, Users, FileClock, MessageSquareWarning } from 'lucide-react';
import StatCard from '../../components/StatCard';
import LoadingState from '../../components/LoadingState';
import ErrorState from '../../components/ErrorState';
import EmptyState from '../../components/EmptyState';
import { facultyService } from '../../services/facultyService';
import { useAuth } from '../../context/AuthContext';

export default function FacultyDashboard() {
  const [data, setData] = useState(null);
  const [status, setStatus] = useState('loading');
  const { user } = useAuth();

  const load = () => {
    setStatus('loading');
    facultyService.dashboard().then((d) => { setData(d); setStatus('ready'); }).catch(() => setStatus('error'));
  };
  useEffect(load, []);

  if (status === 'loading') return <LoadingState label="Loading your dashboard" />;
  if (status === 'error') return <ErrorState onRetry={load} />;

  return (
    <div>
      <div className="mb-6">
        <p className="ledger-index uppercase text-ink-muted">Faculty Dashboard</p>
        <h1 className="font-display text-2xl text-ink dark:text-white">Welcome, {user?.firstName}</h1>
      </div>

      <div className="grid grid-cols-2 lg:grid-cols-4 gap-4 mb-8">
        <StatCard label="Subjects" value={data.subjectCount} icon={BookOpen} />
        <StatCard label="Total students" value={data.totalStudents} icon={Users} />
        <StatCard label="Pending leave" value={data.pendingLeaveRequests} icon={FileClock} accent={data.pendingLeaveRequests > 0 ? 'warning' : 'success'} />
        <StatCard label="Open complaints" value={data.openComplaints} icon={MessageSquareWarning} accent={data.openComplaints > 0 ? 'danger' : 'success'} />
      </div>

      <div className="grid lg:grid-cols-2 gap-6">
        <section className="rounded-xl border border-black/8 dark:border-white/10 bg-white dark:bg-navy-800">
          <h2 className="font-display text-base px-5 py-4 border-b border-black/8 dark:border-white/10">My subjects</h2>
          {data.subjects?.length ? (
            <ul className="divide-y divide-black/5 dark:divide-white/5">
              {data.subjects.map((s) => (
                <li key={s.id} className="px-5 py-3 flex items-center justify-between text-sm">
                  <div>
                    <p className="font-medium text-ink dark:text-white">{s.name}</p>
                    <p className="text-xs text-ink-muted">{s.code} · Semester {s.semester}</p>
                  </div>
                  <span className="font-mono text-xs text-ink-muted">{s.credits} cr</span>
                </li>
              ))}
            </ul>
          ) : <EmptyState message="No subjects assigned" />}
        </section>

        <section className="rounded-xl border border-black/8 dark:border-white/10 bg-white dark:bg-navy-800">
          <h2 className="font-display text-base px-5 py-4 border-b border-black/8 dark:border-white/10">Recent assignments</h2>
          {data.recentAssignments?.length ? (
            <ul className="divide-y divide-black/5 dark:divide-white/5">
              {data.recentAssignments.map((a) => (
                <li key={a.id} className="px-5 py-3 flex items-center justify-between text-sm">
                  <div>
                    <p className="font-medium text-ink dark:text-white">{a.title}</p>
                    <p className="text-xs text-ink-muted">{a.subjectName}</p>
                  </div>
                  <span className="font-mono text-xs text-ink-muted">
                    {a.dueDate ? new Date(a.dueDate).toLocaleDateString() : '—'}
                  </span>
                </li>
              ))}
            </ul>
          ) : <EmptyState message="No assignments yet" />}
        </section>
      </div>
    </div>
  );
}