import { NavLink, Outlet } from 'react-router-dom';
import { useEffect, useState } from 'react';
import { Menu, Sun, Moon, LogOut, Bell, GraduationCap } from 'lucide-react';
import { useAuth } from '../context/AuthContext';
import { useTheme } from '../context/ThemeContext';
import { notificationService } from '../services/notificationService';

export default function DashboardLayout({ navItems, roleLabel }) {
  const { user, logout } = useAuth();
  const { theme, toggleTheme } = useTheme();
  const [sidebarOpen, setSidebarOpen] = useState(false);
  const [notifOpen, setNotifOpen] = useState(false);
  const [notifications, setNotifications] = useState([]);
  const [unreadCount, setUnreadCount] = useState(0);

  const loadNotifications = () => {
    notificationService.list().then(setNotifications).catch(() => {});
    notificationService.unreadCount().then(setUnreadCount).catch(() => {});
  };

  useEffect(() => {
    loadNotifications();
    const interval = setInterval(loadNotifications, 30000);
    return () => clearInterval(interval);
  }, []);

  const openNotifications = async () => {
    setNotifOpen((o) => !o);
    if (!notifOpen && unreadCount > 0) {
      await notificationService.markAllRead();
      setUnreadCount(0);
      setNotifications((list) => list.map((n) => ({ ...n, read: true })));
    }
  };

  return (
    <div className="min-h-screen flex">
      {/* Sidebar */}
      <aside
        className={`fixed lg:static inset-y-0 left-0 z-30 w-64 bg-navy-900 text-white transform transition-transform lg:translate-x-0 ${
          sidebarOpen ? 'translate-x-0' : '-translate-x-full'
        }`}
      >
        <div className="flex items-center gap-2 px-5 h-16 border-b border-white/10">
          <GraduationCap size={22} className="text-[var(--color-gold)]" />
          <span className="font-display text-lg tracking-wide">Smart Campus</span>
        </div>

        <nav className="px-3 py-4 space-y-1">
          {navItems.map((item, i) => (
            <NavLink
              key={item.to}
              to={item.to}
              end={item.end}
              onClick={() => setSidebarOpen(false)}
              className={({ isActive }) =>
                `flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm transition-colors ${
                  isActive ? 'bg-white/10 text-white' : 'text-white/65 hover:bg-white/5 hover:text-white'
                }`
              }
            >
              <span className="ledger-index text-[var(--color-gold)] w-5">{String(i + 1).padStart(2, '0')}</span>
              <item.icon size={17} />
              {item.label}
            </NavLink>
          ))}
        </nav>

        <div className="absolute bottom-0 left-0 right-0 p-3 border-t border-white/10">
          <button
            onClick={logout}
            className="w-full flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm text-white/65 hover:bg-white/5 hover:text-white"
          >
            <LogOut size={17} /> Sign out
          </button>
        </div>
      </aside>

      {sidebarOpen && (
        <div className="fixed inset-0 bg-black/40 z-20 lg:hidden" onClick={() => setSidebarOpen(false)} aria-hidden="true" />
      )}

      {/* Main column */}
      <div className="flex-1 flex flex-col min-w-0">
        <header className="h-16 flex items-center justify-between px-4 lg:px-8 border-b border-black/8 dark:border-white/10 bg-white dark:bg-navy-900 sticky top-0 z-10">
          <div className="flex items-center gap-3">
            <button className="lg:hidden p-2 rounded hover:bg-navy-900/5" onClick={() => setSidebarOpen(true)} aria-label="Open menu">
              <Menu size={20} />
            </button>
            <div>
              <p className="ledger-index uppercase text-ink-muted dark:text-white/50">{roleLabel}</p>
              <p className="text-sm font-medium text-ink dark:text-white">
                Welcome, {user?.firstName} {user?.lastName}
              </p>
            </div>
          </div>

          <div className="flex items-center gap-1.5 relative">
            <button
              aria-label={`Notifications${unreadCount > 0 ? `, ${unreadCount} unread` : ''}`}
              onClick={openNotifications}
              className="p-2 rounded-lg hover:bg-navy-900/5 dark:hover:bg-white/5 relative"
            >
              <Bell size={18} />
              {unreadCount > 0 && (
                <span className="absolute top-1 right-1 h-2 w-2 rounded-full bg-[var(--color-danger)]" />
              )}
            </button>

            {notifOpen && (
              <>
                <div className="fixed inset-0 z-10" onClick={() => setNotifOpen(false)} aria-hidden="true" />
                <div className="absolute right-0 top-12 z-20 w-80 max-h-96 overflow-y-auto rounded-xl border border-black/8 dark:border-white/10 bg-white dark:bg-navy-800 shadow-xl">
                  <div className="px-4 py-3 border-b border-black/8 dark:border-white/10">
                    <p className="text-sm font-medium text-ink dark:text-white">Notifications</p>
                  </div>
                  {notifications.length === 0 ? (
                    <p className="px-4 py-6 text-sm text-ink-muted text-center">No notifications yet</p>
                  ) : (
                    <ul className="divide-y divide-black/5 dark:divide-white/5">
                      {notifications.slice(0, 15).map((n) => (
                        <li key={n.id} className="px-4 py-3 text-sm">
                          <p className="font-medium text-ink dark:text-white">{n.title}</p>
                          <p className="text-xs text-ink-muted mt-0.5">{n.message}</p>
                        </li>
                      ))}
                    </ul>
                  )}
                </div>
              </>
            )}
            <button
              aria-label="Toggle dark mode"
              onClick={toggleTheme}
              className="p-2 rounded-lg hover:bg-navy-900/5 dark:hover:bg-white/5"
            >
              {theme === 'dark' ? <Sun size={18} /> : <Moon size={18} />}
            </button>
            <div className="ml-2 h-8 w-8 rounded-full bg-[var(--color-gold-soft)] text-[var(--color-warning)] flex items-center justify-center text-sm font-medium">
              {user?.firstName?.[0]}{user?.lastName?.[0]}
            </div>
          </div>
        </header>

        <main className="flex-1 p-4 lg:p-8 max-w-[1400px] w-full mx-auto">
          <Outlet />
        </main>
      </div>
    </div>
  );
}
