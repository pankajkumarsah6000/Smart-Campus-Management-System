import { useEffect, useState } from 'react';
import { useParams, Link } from 'react-router-dom';
import { ArrowLeft, ClipboardCheck, FileText, Wallet, CalendarClock } from 'lucide-react';
import Badge from '../../components/Badge';
import LoadingState from '../../components/LoadingState';
import ErrorState from '../../components/ErrorState';
import EmptyState from '../../components/EmptyState';
import { parentService } from '../../services/parentService';

const TABS = [
  { id: 'attendance', label: 'Attendance', icon: ClipboardCheck },
  { id: 'results', label: 'Results', icon: FileText },
  { id: 'fees', label: 'Fees', icon: Wallet },
  { id: 'timetable', label: 'Timetable', icon: CalendarClock },
];

const DAYS = ['MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY', 'SUNDAY'];
const feeStatusTone = { PAID: 'success', PARTIAL: 'warning', PENDING: 'info', OVERDUE: 'danger' };

export default function ChildDetails() {
  const { studentId } = useParams();
  const [tab, setTab] = useState('attendance');
  const [data, setData] = useState(null);
  const [status, setStatus] = useState('loading');

  const load = () => {
    setStatus('loading');
    parentService.childDashboard(studentId).then((d) => { setData(d); setStatus('ready'); }).catch(() => setStatus('error'));
  };
  useEffect(load, [studentId]);

  if (status === 'loading') return <LoadingState label="Loading child overview" />;
  if (status === 'error') return <ErrorState onRetry={load} />;

  const child = data?.profile;

  const renderTab = () => {
    if (tab === 'attendance') {
      const summary = data?.attendancePercentage;
      return (
        <section className="rounded-xl border border-black/8 dark:border-white/10 bg-white dark:bg-navy-800">
          <h2 className="font-display text-base px-5 py-4 border-b border-black/8 dark:border-white/10">Attendance</h2>
          <div className="px-5 py-4">
            <p className="ledger-index uppercase text-ink-muted text-xs">Overall</p>
            <p className={`font-mono text-3xl ${summary >= 75 ? 'text-[var(--color-success)]' : 'text-[var(--color-danger)]'}`}>
              {summary.toFixed(1)}%
            </p>
          </div>
        </section>
      );
    }
    if (tab === 'results') {
      return <Section title="Results"><ResultList studentId={studentId} /></Section>;
    }
    if (tab === 'fees') {
      return <Section title="Fees"><FeeList studentId={studentId} /></Section>;
    }
    return <Section title="Timetable"><SlotList studentId={studentId} /></Section>;
  };

  return (
    <div>
      <div className="mb-6">
        <Link to="/parent" className="inline-flex items-center gap-1.5 text-sm text-ink-muted hover:text-ink dark:hover:text-white mb-2">
          <ArrowLeft size={15} /> Back to children
        </Link>
        <p className="ledger-index uppercase text-ink-muted">Parent · Child</p>
        <h1 className="font-display text-2xl text-ink dark:text-white">{child?.firstName} {child?.lastName}</h1>
        <p className="text-sm text-ink-muted mt-1">{child?.rollNumber} · Sem {child?.semester} · Section {child?.section}</p>
      </div>

      <div className="flex flex-wrap gap-2 mb-6">
        {TABS.map((t) => (
          <button
            key={t.id}
            onClick={() => setTab(t.id)}
            className={`flex items-center gap-2 rounded-lg px-3 py-2 text-sm font-medium transition-colors ${
              tab === t.id ? 'bg-navy-900 text-white' : 'bg-white dark:bg-navy-800 text-ink dark:text-white border border-black/10 dark:border-white/10 hover:border-navy-700'
            }`}
          >
            <t.icon size={14} /> {t.label}
          </button>
        ))}
      </div>

      {renderTab()}
    </div>
  );
}

function Section({ title, children }) {
  return <section className="rounded-xl border border-black/8 dark:border-white/10 bg-white dark:bg-navy-800"><h2 className="font-display text-base px-5 py-4 border-b border-black/8 dark:border-white/10">{title}</h2>{children}</section>;
}

function ResultList({ studentId }) {
  const [results, setResults] = useState(null);
  const [error, setError] = useState(false);
  const load = () => {
    setResults(null); setError(false);
    parentService.childResults(studentId).then(setResults).catch(() => setError(true));
  };
  useEffect(load, [studentId]);
  if (error) return <ErrorState onRetry={load} />;
  if (!results) return <LoadingState label="Loading results" />;
  if (results.length === 0) return <EmptyState message="No results published" />;
  return (
    <ul className="divide-y divide-black/5 dark:divide-white/5">
      {results.map((r) => (
        <li key={r.id} className="px-5 py-3 flex items-center justify-between text-sm">
          <div>
            <p className="font-medium text-ink dark:text-white">{r.examinationName}</p>
            <p className="text-xs text-ink-muted">{r.subjectName}</p>
          </div>
          <div className="text-right shrink-0">
            <p className="font-mono">{r.marksObtained}/{r.maxMarks} {r.percentage != null && <span className="text-xs text-ink-muted">({r.percentage.toFixed(1)}%)</span>}</p>
            <div className="flex justify-end gap-1.5 mt-0.5">
              <Badge tone={r.passed ? 'success' : 'danger'}>{r.passed ? 'Pass' : 'Fail'}</Badge>
              {r.grade && <Badge>{r.grade}</Badge>}
            </div>
          </div>
        </li>
      ))}
    </ul>
  );
}

function FeeList({ studentId }) {
  const [fees, setFees] = useState(null);
  const [error, setError] = useState(false);
  const load = () => {
    setFees(null); setError(false);
    parentService.childFees(studentId).then(setFees).catch(() => setError(true));
  };
  useEffect(load, [studentId]);
  if (error) return <ErrorState onRetry={load} />;
  if (!fees) return <LoadingState label="Loading fees" />;
  if (fees.length === 0) return <EmptyState message="No fee records" />;
  return (
    <ul className="divide-y divide-black/5 dark:divide-white/5">
      {fees.map((f) => (
        <li key={f.id} className="px-5 py-3 flex items-center justify-between text-sm">
          <div>
            <div className="flex items-center gap-2">
              <p className="font-medium text-ink dark:text-white">{f.feeType || 'Fee'}</p>
              <Badge tone={feeStatusTone[f.status] || 'neutral'}>{f.status}</Badge>
            </div>
            <p className="text-xs text-ink-muted">Due {f.dueDate || '—'}{f.semester ? ` · Sem ${f.semester}` : ''}</p>
          </div>
          <div className="text-right font-mono text-sm shrink-0">
            <p className="text-ink dark:text-white">₹{Number(f.amount).toLocaleString('en-IN')}</p>
            <p className="text-xs text-ink-muted">Paid ₹{Number(f.amountPaid || 0).toLocaleString('en-IN')}</p>
          </div>
        </li>
      ))}
    </ul>
  );
}

function SlotList({ studentId }) {
  const [slots, setSlots] = useState(null);
  const [error, setError] = useState(false);
  const load = () => {
    setSlots(null); setError(false);
    parentService.childTimetable(studentId).then(setSlots).catch(() => setError(true));
  };
  useEffect(load, [studentId]);
  if (error) return <ErrorState onRetry={load} />;
  if (!slots) return <LoadingState label="Loading timetable" />;
  if (slots.length === 0) return <EmptyState message="No classes scheduled" />;
  return (
    <ul className="divide-y divide-black/5 dark:divide-white/5">
      {DAYS.filter((d) => slots.some((s) => s.dayOfWeek === d)).map((day) => (
        <li key={day} className="px-5 py-3">
          <p className="ledger-index uppercase text-ink-muted text-xs mb-2">{day.toLowerCase()}</p>
          {slots.filter((s) => s.dayOfWeek === day).sort((a, b) => a.startTime.localeCompare(b.startTime)).map((s) => (
            <div key={s.id} className="flex items-center justify-between text-sm py-1">
              <span className="text-ink dark:text-white">{s.subjectName} · {s.room || '—'}</span>
              <span className="font-mono text-xs text-ink-muted">{s.startTime}–{s.endTime}</span>
            </div>
          ))}
        </li>
      ))}
    </ul>
  );
}