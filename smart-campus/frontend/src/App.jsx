import { BrowserRouter, Routes, Route, Navigate, Link } from 'react-router-dom';
import { AuthProvider, useAuth } from './context/AuthContext';
import { ThemeProvider } from './context/ThemeContext';
import { ToastProvider } from './context/ToastContext';
import ProtectedRoute from './routes/ProtectedRoute';
import DashboardLayout from './layouts/DashboardLayout';
import Login from './pages/auth/Login';

import AdminDashboard from './pages/admin/AdminDashboard';
import ManageStudents from './pages/admin/ManageStudents';
import ManageFaculty from './pages/admin/ManageFaculty';
import ManageDepartments from './pages/admin/ManageDepartments';
import ManageCourses from './pages/admin/ManageCourses';
import ManageNotices from './pages/admin/ManageNotices';
import ManageClassrooms from './pages/admin/ManageClassrooms';
import ManageFees from './pages/admin/ManageFees';
import ManageComplaints from './pages/admin/ManageComplaints';
import ManageTimetable from './pages/admin/ManageTimetable';
import ManageExams from './pages/admin/ManageExams';
import LeaveApprovals from './pages/admin/LeaveApprovals';

import StudentDashboard from './pages/student/StudentDashboard';
import Profile from './pages/student/Profile';
import Attendance from './pages/student/Attendance';
import LeaveApplication from './pages/student/LeaveApplication';
import AIAssistant from './pages/student/AIAssistant';
import Assignments from './pages/student/Assignments';
import ExamsResults from './pages/student/ExamsResults';
import Fees from './pages/student/Fees';
import Timetable from './pages/student/Timetable';
import Complaints from './pages/student/Complaints';

import FacultyDashboard from './pages/faculty/FacultyDashboard';
import FacultyAssignments from './pages/faculty/FacultyAssignments';
import FacultyExams from './pages/faculty/FacultyExams';
import FacultyTimetable from './pages/faculty/FacultyTimetable';
import FacultyStudents from './pages/faculty/FacultyStudents';

import ParentDashboard from './pages/parent/ParentDashboard';
import ChildDetails from './pages/parent/ChildDetails';

import {
  LayoutDashboard, GraduationCap, Users, Building2, BookOpen, Megaphone, UserCircle,
  DoorOpen, FileClock, ClipboardCheck, Sparkles, Wallet, MessageSquareWarning, CalendarClock,
  CalendarDays, FileText,
} from 'lucide-react';

const adminNav = [
  { to: '/admin', label: 'Dashboard', icon: LayoutDashboard, end: true },
  { to: '/admin/students', label: 'Students', icon: GraduationCap },
  { to: '/admin/faculty', label: 'Faculty', icon: Users },
  { to: '/admin/departments', label: 'Departments', icon: Building2 },
  { to: '/admin/courses', label: 'Courses', icon: BookOpen },
  { to: '/admin/classrooms', label: 'Classrooms', icon: DoorOpen },
  { to: '/admin/fees', label: 'Fees', icon: Wallet },
  { to: '/admin/timetable', label: 'Timetable', icon: CalendarClock },
  { to: '/admin/exams', label: 'Exams', icon: CalendarDays },
  { to: '/admin/complaints', label: 'Complaints', icon: MessageSquareWarning },
  { to: '/admin/leave-requests', label: 'Leave Requests', icon: FileClock },
  { to: '/admin/notices', label: 'Notices', icon: Megaphone },
];

const studentNav = [
  { to: '/student', label: 'Dashboard', icon: LayoutDashboard, end: true },
  { to: '/student/attendance', label: 'Attendance', icon: ClipboardCheck },
  { to: '/student/assignments', label: 'Assignments', icon: FileText },
  { to: '/student/exams', label: 'Exams & Results', icon: CalendarDays },
  { to: '/student/fees', label: 'Fees', icon: Wallet },
  { to: '/student/timetable', label: 'Timetable', icon: CalendarClock },
  { to: '/student/complaints', label: 'Complaints', icon: MessageSquareWarning },
  { to: '/student/leave', label: 'Leave', icon: FileClock },
  { to: '/student/ai-assistant', label: 'AI Assistant', icon: Sparkles },
  { to: '/student/profile', label: 'Profile', icon: UserCircle },
];

const facultyNav = [
  { to: '/faculty', label: 'Dashboard', icon: LayoutDashboard, end: true },
  { to: '/faculty/assignments', label: 'Assignments', icon: FileText },
  { to: '/faculty/exams', label: 'Exams & Grades', icon: CalendarDays },
  { to: '/faculty/timetable', label: 'Timetable', icon: CalendarClock },
  { to: '/faculty/students', label: 'Students', icon: Users },
];

const parentNav = [
  { to: '/parent', label: 'Dashboard', icon: LayoutDashboard, end: true },
  { to: '/parent/children', label: 'Children', icon: Users },
];

function RoleHomeRedirect() {
  const { user, hasRole } = useAuth();
  if (!user) return <Navigate to="/login" replace />;
  if (hasRole('ADMIN')) return <Navigate to="/admin" replace />;
  if (hasRole('FACULTY')) return <Navigate to="/faculty" replace />;
  if (hasRole('STUDENT')) return <Navigate to="/student" replace />;
  if (hasRole('PARENT')) return <Navigate to="/parent" replace />;
  return <Navigate to="/login" replace />;
}

function NotFound() {
  return (
    <div className="min-h-screen flex flex-col items-center justify-center bg-paper dark:bg-navy-950 text-center px-6">
      <p className="ledger-index text-[var(--color-gold)] mb-2">Error 404</p>
      <h1 className="font-display text-3xl text-ink dark:text-white mb-2">Page not found</h1>
      <p className="text-sm text-ink-muted mb-6">The page you’re looking for doesn’t exist or has moved.</p>
      <Link to="/" className="rounded-lg bg-navy-900 text-white px-4 py-2 text-sm font-medium hover:bg-navy-800">
        Go home
      </Link>
    </div>
  );
}

export default function App() {
  return (
    <ThemeProvider>
      <ToastProvider>
        <AuthProvider>
          <BrowserRouter>
            <Routes>
              <Route path="/login" element={<Login />} />
              <Route path="/" element={<RoleHomeRedirect />} />

              <Route element={<ProtectedRoute allowedRoles={['ADMIN']} />}>
                <Route element={<DashboardLayout navItems={adminNav} roleLabel="Administrator" />}>
                  <Route path="/admin" element={<AdminDashboard />} />
                  <Route path="/admin/students" element={<ManageStudents />} />
                  <Route path="/admin/faculty" element={<ManageFaculty />} />
                  <Route path="/admin/departments" element={<ManageDepartments />} />
                  <Route path="/admin/courses" element={<ManageCourses />} />
                  <Route path="/admin/classrooms" element={<ManageClassrooms />} />
                  <Route path="/admin/fees" element={<ManageFees />} />
                  <Route path="/admin/timetable" element={<ManageTimetable />} />
                  <Route path="/admin/exams" element={<ManageExams />} />
                  <Route path="/admin/complaints" element={<ManageComplaints />} />
                  <Route path="/admin/leave-requests" element={<LeaveApprovals />} />
                  <Route path="/admin/notices" element={<ManageNotices />} />
                </Route>
              </Route>

              <Route element={<ProtectedRoute allowedRoles={['STUDENT']} />}>
                <Route element={<DashboardLayout navItems={studentNav} roleLabel="Student" />}>
                  <Route path="/student" element={<StudentDashboard />} />
                  <Route path="/student/attendance" element={<Attendance />} />
                  <Route path="/student/assignments" element={<Assignments />} />
                  <Route path="/student/exams" element={<ExamsResults />} />
                  <Route path="/student/fees" element={<Fees />} />
                  <Route path="/student/timetable" element={<Timetable />} />
                  <Route path="/student/complaints" element={<Complaints />} />
                  <Route path="/student/leave" element={<LeaveApplication />} />
                  <Route path="/student/ai-assistant" element={<AIAssistant />} />
                  <Route path="/student/profile" element={<Profile />} />
                </Route>
              </Route>

              <Route element={<ProtectedRoute allowedRoles={['FACULTY']} />}>
                <Route element={<DashboardLayout navItems={facultyNav} roleLabel="Faculty" />}>
                  <Route path="/faculty" element={<FacultyDashboard />} />
                  <Route path="/faculty/assignments" element={<FacultyAssignments />} />
                  <Route path="/faculty/exams" element={<FacultyExams />} />
                  <Route path="/faculty/timetable" element={<FacultyTimetable />} />
                  <Route path="/faculty/students" element={<FacultyStudents />} />
                </Route>
              </Route>

              <Route element={<ProtectedRoute allowedRoles={['PARENT']} />}>
                <Route element={<DashboardLayout navItems={parentNav} roleLabel="Parent" />}>
                  <Route path="/parent" element={<ParentDashboard />} />
                  <Route path="/parent/children" element={<ParentDashboard />} />
                  <Route path="/parent/children/:studentId" element={<ChildDetails />} />
                </Route>
              </Route>

              <Route path="*" element={<NotFound />} />
            </Routes>
          </BrowserRouter>
        </AuthProvider>
      </ToastProvider>
    </ThemeProvider>
  );
}