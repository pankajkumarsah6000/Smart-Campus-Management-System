import { useState } from 'react';
import { Link, Navigate, useNavigate } from 'react-router-dom';
import { GraduationCap } from 'lucide-react';
import { useAuth } from '../../context/AuthContext';
import { useToast } from '../../context/ToastContext';
import LoadingState from '../../components/LoadingState';

export default function Signup() {
  const [form, setForm] = useState({
    firstName: '',
    lastName: '',
    email: '',
    password: '',
    confirmPassword: '',
  });
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState('');
  const { registerStudent, user, loading } = useAuth();
  const { showToast } = useToast();
  const navigate = useNavigate();

  if (loading) return <LoadingState label="Checking session" />;
  if (user) return <Navigate to="/" replace />;

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    if (form.password !== form.confirmPassword) {
      setError('Passwords do not match.');
      return;
    }

    setSubmitting(true);
    try {
      await registerStudent({
        firstName: form.firstName,
        lastName: form.lastName,
        email: form.email,
        password: form.password,
      });
      showToast('Student account created', 'success');
      navigate('/student', { replace: true });
    } catch (err) {
      setError(
        err.response?.data?.message
          || (err.request
            ? 'Cannot connect to the server. Please check your connection and try again.'
            : 'Unable to create your account. Please try again.')
      );
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="min-h-screen flex bg-navy-950">
      <div className="hidden lg:flex lg:w-1/2 flex-col justify-between p-12 bg-navy-950 text-white relative overflow-hidden">
        <div
          className="absolute inset-0 opacity-[0.06] pointer-events-none"
          style={{ backgroundImage: 'repeating-linear-gradient(0deg, #fff 0, #fff 1px, transparent 1px, transparent 48px)' }}
        />
        <div className="flex items-center gap-2 relative">
          <GraduationCap size={26} className="text-[var(--color-gold)]" />
          <span className="font-display text-xl">Smart Campus</span>
        </div>
        <div className="relative">
          <p className="ledger-index uppercase text-white/40 mb-3">Student registration</p>
          <h1 className="font-display text-4xl leading-tight max-w-md">
            Your campus journey, all in one place.
          </h1>
        </div>
        <p className="text-xs text-white/30 relative">© 2026 Smart Campus Management System</p>
      </div>

      <div className="flex-1 flex items-center justify-center p-6 bg-paper dark:bg-navy-950">
        <div className="w-full max-w-sm">
          <div className="lg:hidden flex items-center gap-2 mb-8 justify-center">
            <GraduationCap size={22} className="text-[var(--color-gold)]" />
            <span className="font-display text-lg text-ink">Smart Campus</span>
          </div>

          <h2 className="font-display text-2xl text-ink mb-1">Create student account</h2>
          <p className="text-sm text-ink-muted mb-6">Sign up to access the student portal.</p>

          <form onSubmit={handleSubmit}>
            <div className="grid grid-cols-2 gap-3">
              <label className="block mb-4">
                <span className="block text-sm font-medium text-ink mb-1.5">First name</span>
                <input
                  type="text"
                  required
                  maxLength={100}
                  autoComplete="given-name"
                  value={form.firstName}
                  onChange={(e) => setForm({ ...form, firstName: e.target.value })}
                  className="w-full rounded-lg border border-black/10 bg-white px-3.5 py-2.5 text-sm outline-none focus:border-navy-700"
                />
              </label>
              <label className="block mb-4">
                <span className="block text-sm font-medium text-ink mb-1.5">Last name</span>
                <input
                  type="text"
                  required
                  maxLength={100}
                  autoComplete="family-name"
                  value={form.lastName}
                  onChange={(e) => setForm({ ...form, lastName: e.target.value })}
                  className="w-full rounded-lg border border-black/10 bg-white px-3.5 py-2.5 text-sm outline-none focus:border-navy-700"
                />
              </label>
            </div>

            <label className="block mb-4">
              <span className="block text-sm font-medium text-ink mb-1.5">Email</span>
              <input
                type="email"
                required
                maxLength={150}
                autoComplete="email"
                value={form.email}
                onChange={(e) => setForm({ ...form, email: e.target.value })}
                className="w-full rounded-lg border border-black/10 bg-white px-3.5 py-2.5 text-sm outline-none focus:border-navy-700"
                placeholder="you@smartcampus.edu"
              />
            </label>

            <label className="block mb-4">
              <span className="block text-sm font-medium text-ink mb-1.5">Password</span>
              <input
                type="password"
                required
                minLength={8}
                maxLength={72}
                autoComplete="new-password"
                value={form.password}
                onChange={(e) => setForm({ ...form, password: e.target.value })}
                className="w-full rounded-lg border border-black/10 bg-white px-3.5 py-2.5 text-sm outline-none focus:border-navy-700"
              />
            </label>

            <label className="block mb-2">
              <span className="block text-sm font-medium text-ink mb-1.5">Confirm password</span>
              <input
                type="password"
                required
                minLength={8}
                maxLength={72}
                autoComplete="new-password"
                value={form.confirmPassword}
                onChange={(e) => setForm({ ...form, confirmPassword: e.target.value })}
                className="w-full rounded-lg border border-black/10 bg-white px-3.5 py-2.5 text-sm outline-none focus:border-navy-700"
              />
            </label>

            {error && <p role="alert" className="text-sm text-[var(--color-danger)] mt-3">{error}</p>}

            <p className="text-xs text-ink-muted mt-3">
              Student access is activated immediately without email verification. Your roll number
              is generated automatically; an administrator can complete your academic details.
              Admin, faculty, and parent accounts are created by an administrator.
            </p>

            <button
              type="submit"
              disabled={submitting}
              className="w-full mt-4 rounded-lg bg-navy-900 text-white py-2.5 text-sm font-medium hover:bg-navy-800 disabled:opacity-60 transition-colors"
            >
              {submitting ? 'Creating account…' : 'Sign up'}
            </button>
          </form>

          <p className="text-sm text-ink-muted mt-5">
            Already have an account?{' '}
            <Link to="/login" className="font-medium text-navy-900 hover:underline dark:text-[var(--color-gold)]">
              Sign in
            </Link>
          </p>
        </div>
      </div>
    </div>
  );
}
