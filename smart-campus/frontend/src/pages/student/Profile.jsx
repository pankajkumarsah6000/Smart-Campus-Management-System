import { useEffect, useState } from 'react';
import LoadingState from '../../components/LoadingState';
import ErrorState from '../../components/ErrorState';
import Badge from '../../components/Badge';
import { studentService } from '../../services/studentService';

export default function Profile() {
  const [profile, setProfile] = useState(null);
  const [status, setStatus] = useState('loading');

  const load = () => {
    setStatus('loading');
    studentService.getMyProfile().then((p) => { setProfile(p); setStatus('ready'); }).catch(() => setStatus('error'));
  };
  useEffect(load, []);

  if (status === 'loading') return <LoadingState label="Loading profile" />;
  if (status === 'error') return <ErrorState onRetry={load} />;

  const rows = [
    ['Roll Number', profile.rollNumber],
    ['Email', profile.email],
    ['Phone', profile.phone || '—'],
    ['Department', profile.departmentName || '—'],
    ['Semester', profile.semester || '—'],
    ['Section', profile.section || '—'],
    ['Admission Date', profile.admissionDate || '—'],
  ];

  return (
    <div className="max-w-xl">
      <div className="mb-6">
        <p className="ledger-index uppercase text-ink-muted">Student · Profile</p>
        <h1 className="font-display text-2xl text-ink dark:text-white">{profile.firstName} {profile.lastName}</h1>
        <Badge tone={profile.status === 'ACTIVE' ? 'success' : 'neutral'}>{profile.status}</Badge>
      </div>

      <dl className="rounded-xl border border-black/8 dark:border-white/10 bg-white dark:bg-navy-800 divide-y divide-black/5 dark:divide-white/5">
        {rows.map(([label, value]) => (
          <div key={label} className="flex justify-between px-5 py-3 text-sm">
            <dt className="text-ink-muted">{label}</dt>
            <dd className="font-medium text-ink dark:text-white">{value}</dd>
          </div>
        ))}
      </dl>
    </div>
  );
}
