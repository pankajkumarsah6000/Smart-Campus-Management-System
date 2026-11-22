import api from './api';

export const adminService = {
  getDashboard: () => api.get('/admin/dashboard').then((r) => r.data.data),

  // Students
  listStudents: (params) => api.get('/admin/students', { params }).then((r) => r.data.data),
  createStudent: (payload) => api.post('/admin/students', payload).then((r) => r.data.data),
  updateStudent: (id, payload) => api.put(`/admin/students/${id}`, payload).then((r) => r.data.data),
  deleteStudent: (id) => api.delete(`/admin/students/${id}`).then((r) => r.data),

  // Faculty
  listFaculty: () => api.get('/admin/faculty').then((r) => r.data.data),
  createFaculty: (payload) => api.post('/admin/faculty', payload).then((r) => r.data.data),
  deleteFaculty: (id) => api.delete(`/admin/faculty/${id}`).then((r) => r.data),

  // Departments
  listDepartments: () => api.get('/admin/departments').then((r) => r.data.data),
  createDepartment: (payload) => api.post('/admin/departments', payload).then((r) => r.data.data),
  updateDepartment: (id, payload) => api.put(`/admin/departments/${id}`, payload).then((r) => r.data.data),
  deleteDepartment: (id) => api.delete(`/admin/departments/${id}`).then((r) => r.data),

  // Courses
  listCourses: () => api.get('/admin/courses').then((r) => r.data.data),
  createCourse: (payload) => api.post('/admin/courses', payload).then((r) => r.data.data),
  deleteCourse: (id) => api.delete(`/admin/courses/${id}`).then((r) => r.data),

  // Subjects
  listSubjects: () => api.get('/admin/subjects').then((r) => r.data.data),
  createSubject: (payload) => api.post('/admin/subjects', payload).then((r) => r.data.data),
  deleteSubject: (id) => api.delete(`/admin/subjects/${id}`).then((r) => r.data),

  // Notices
  listNotices: () => api.get('/admin/notices').then((r) => r.data.data),
  createNotice: (payload) => api.post('/admin/notices', payload).then((r) => r.data.data),
  deleteNotice: (id) => api.delete(`/admin/notices/${id}`).then((r) => r.data),
};

adminService.listClassrooms = () => api.get('/admin/classrooms').then((r) => r.data.data);
adminService.createClassroom = (payload) => api.post('/admin/classrooms', payload).then((r) => r.data.data);
adminService.deleteClassroom = (id) => api.delete(`/admin/classrooms/${id}`).then((r) => r.data);
