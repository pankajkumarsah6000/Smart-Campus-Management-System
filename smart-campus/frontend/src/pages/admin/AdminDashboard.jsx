import { useEffect, useState } from 'react';
import { Users, GraduationCap, Building2, ClipboardCheck, Wallet, FileClock } from 'lucide-react';
import StatCard from '../../components/StatCard';
import LoadingState from '../../components/LoadingState';
import ErrorState from '../../components/ErrorState';
import { adminService } from '../../services/adminService';

export default function AdminDashboard() {
  const [data, setData] = useState(null);
  const [status, setStatus] = useState('loading');

  const load = () => {
    setStatus('loading');
    adminService.getDashboard()
      .then((d) => { setData(d); setStatus('ready'); })
      .catch(() => setStatus('error'));
  };

  useEffect(load, []);

  if (status === 'loading') return <LoadingState label="Loading dashboard" />;
  if (status === 'error') return <ErrorState onRetry={load} />;

  return (
    <div>
      <div className="mb-6">
        <p className="ledger-index uppercase text-ink-muted">Admin Dashboard</p>
        <h1 className="font-display text-2xl text-ink dark:text-white">Campus overview</h1>
      </div>

      <div className="grid grid-cols-2 lg:grid-cols-3 gap-4">
        <StatCard label="Total Students" value={data.totalStudents} icon={GraduationCap} />
        <StatCard label="Total Faculty" value={data.totalFaculty} icon={Users} />
        <StatCard label="Departments" value={data.totalDepartments} icon={Building2} />
        <StatCard
          label="Attendance"
          value={`${data.overallAttendancePercentage.toFixed(1)}%`}
          icon={ClipboardCheck}
          accent={data.overallAttendancePercentage >= 75 ? 'success' : 'warning'}
        />
        <StatCard label="Pending Requests" value={data.pendingLeaveRequests} icon={FileClock} accent="warning" />
        <StatCard
          label="Fee Collection"
          value={`${data.feeCollectionPercentage.toFixed(1)}%`}
          icon={Wallet}
          accent="success"
        />
      </div>
    </div>
  );
}
