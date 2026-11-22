import { useState } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { GraduationCap, Eye, EyeOff } from 'lucide-react';
import { useAuth } from '../../context/AuthContext';
import { useToast } from '../../context/ToastContext';

const ROLE_HOME = {
  ADMIN: '/admin',
  FACULTY: '/faculty',
  STUDENT: '/student',
  PARENT: '/parent',
};

export default function Login() {
  const [email, setEmail] = useState('admin@smartcampus.edu');
  const [password, setPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState('');
  const { login } = useAuth();
  const { showToast } = useToast();
  const navigate = useNavigate();
  const location = useLocation();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setSubmitting(true);
    try {
      const profile = await login(email, password);
      showToast('Signed in successfully', 'success');
      const from = location.state?.from;
      const primaryRole = profile.roles?.[0];
      navigate(from || ROLE_HOME[primaryRole] || '/login', { replace: true });
    } catch (err) {
      setError(err.response?.data?.message || 'Invalid email or password');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="min-h-screen flex bg-navy-950">
      {/* Left: brand panel */}
      <div className="hidden lg:flex lg:w-1/2 flex-col justify-between p-12 bg-navy-950 text-white relative overflow-hidden">
        <div className="absolute inset-0 opacity-[0.06] pointer-events-none"
             style={{ backgroundImage: 'repeating-linear-gradient(0deg, #fff 0, #fff 1px, transparent 1px, transparent 48px)' }} />
        <div className="flex items-center gap-2 relative">
          <GraduationCap size={26} className="text-[var(--color-gold)]" />
          <span className="font-display text-xl">Smart Campus</span>
        </div>
        <div className="relative">
          <p className="ledger-index uppercase text-white/40 mb-3">Academic Year 2026–27</p>
          <h1 className="font-display text-4xl leading-tight max-w-md">
            One record, every department — attendance, marks, and notices, kept in one ledger.
          </h1>
        </div>
        <p className="text-xs text-white/30 relative">© 2026 Smart Campus Management System</p>
      </div>

      {/* Right: form panel */}
      <div className="flex-1 flex items-center justify-center p-6 bg-paper dark:bg-navy-950">
        <div className="w-full max-w-sm">
          <div className="lg:hidden flex items-center gap-2 mb-8 justify-center">
            <GraduationCap size={22} className="text-[var(--color-gold)]" />
            <span className="font-display text-lg text-ink">Smart Campus</span>
          </div>

          <h2 className="font-display text-2xl text-ink mb-1">Sign in</h2>
          <p className="text-sm text-ink-muted mb-6">Enter your campus credentials to continue.</p>

          <form onSubmit={handleSubmit} noValidate>
            <label className="block mb-4">
              <span className="block text-sm font-medium text-ink mb-1.5">Email</span>
              <input
                type="email"
                required
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                className="w-full rounded-lg border border-black/10 bg-white px-3.5 py-2.5 text-sm outline-none focus:border-navy-700"
                placeholder="you@smartcampus.edu"
              />
            </label>

            <label className="block mb-2">
              <span className="block text-sm font-medium text-ink mb-1.5">Password</span>
              <div className="relative">
                <input
                  type={showPassword ? 'text' : 'password'}
                  required
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  className="w-full rounded-lg border border-black/10 bg-white px-3.5 py-2.5 pr-10 text-sm outline-none focus:border-navy-700"
                  placeholder="••••••••"
                />
                <button
                  type="button"
                  onClick={() => setShowPassword((s) => !s)}
                  aria-label={showPassword ? 'Hide password' : 'Show password'}
                  className="absolute right-3 top-1/2 -translate-y-1/2 text-ink-muted"
                >
                  {showPassword ? <EyeOff size={16} /> : <Eye size={16} />}
                </button>
              </div>
            </label>

            {error && <p role="alert" className="text-sm text-[var(--color-danger)] mb-3">{error}</p>}

            <button
              type="submit"
              disabled={submitting}
              className="w-full mt-3 rounded-lg bg-navy-900 text-white py-2.5 text-sm font-medium hover:bg-navy-800 disabled:opacity-60 transition-colors"
            >
              {submitting ? 'Signing in…' : 'Sign in'}
            </button>
          </form>

          <p className="text-xs text-ink-muted mt-6 text-center leading-relaxed">
            Demo accounts<br />
            Admin: admin@smartcampus.edu / Admin@123<br />
            Faculty: faculty1@smartcampus.edu / Faculty@123<br />
            Student: student1@smartcampus.edu / Student@123<br />
            Parent: parent@smartcampus.edu / Parent@123
          </p>
        </div>
      </div>
    </div>
  );
}
