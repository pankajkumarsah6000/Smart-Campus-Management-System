import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { Users, ArrowRight } from 'lucide-react';
import LoadingState from '../../components/LoadingState';
import ErrorState from '../../components/ErrorState';
import EmptyState from '../../components/EmptyState';
import { parentService } from '../../services/parentService';
import { useAuth } from '../../context/AuthContext';

export default function ParentDashboard() {
  const [profile, setProfile] = useState(null);
  const [children, setChildren] = useState([]);
  const [status, setStatus] = useState('loading');
  const { user } = useAuth();

  const load = () => {
    setStatus('loading');
    Promise.all([parentService.me(), parentService.children()])
      .then(([p, c]) => { setProfile(p); setChildren(c || []); setStatus('ready'); })
      .catch(() => setStatus('error'));
  };
  useEffect(load, []);

  if (status === 'loading') return <LoadingState label="Loading parent portal" />;
  if (status === 'error') return <ErrorState onRetry={load} />;

  return (
    <div>
      <div className="mb-6">
        <p className="ledger-index uppercase text-ink-muted">Parent Portal</p>
        <h1 className="font-display text-2xl text-ink dark:text-white">Welcome, {user?.firstName}</h1>
        <p className="text-sm text-ink-muted mt-1">
          {profile?.relationship || 'Guardian'} · {children.length} linked child{children.length === 1 ? '' : 'ren'}
        </p>
      </div>

      {children.length === 0 ? (
        <div className="rounded-xl border border-black/8 dark:border-white/10 bg-white dark:bg-navy-800">
          <EmptyState message="No children are linked to your account yet" />
        </div>
      ) : (
        <>
          <div className="flex items-center gap-2 mb-3 text-sm font-medium text-ink dark:text-white">
            <Users size={16} /> My children
          </div>
          <div className="grid md:grid-cols-2 gap-4">
            {children.map((c) => (
              <Link
                key={c.id}
                to={`/parent/children/${c.id}`}
                className="group rounded-xl border border-black/8 dark:border-white/10 bg-white dark:bg-navy-800 p-5 hover:border-navy-700 transition-colors"
              >
                <div className="flex items-center justify-between">
                  <div>
                    <p className="font-medium text-ink dark:text-white">{c.firstName} {c.lastName}</p>
                    <p className="text-xs text-ink-muted">{c.rollNumber} · Sem {c.semester} · Section {c.section}</p>
                    <p className="text-xs text-ink-muted mt-1">
                      {c.departmentName || ''} · {c.email}
                    </p>
                  </div>
                  <ArrowRight size={18} className="text-ink-muted group-hover:text-[var(--color-gold)] transition-colors shrink-0" />
                </div>
              </Link>
            ))}
          </div>
        </>
      )}
    </div>
  );
}