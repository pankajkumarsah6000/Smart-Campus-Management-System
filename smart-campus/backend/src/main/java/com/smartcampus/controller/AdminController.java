package com.smartcampus.controller;

import com.smartcampus.dto.ApiResponse;
import com.smartcampus.dto.request.*;
import com.smartcampus.dto.response.*;
import com.smartcampus.security.CustomUserDetails;
import com.smartcampus.service.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Admin-only endpoints. Route-level protection is enforced centrally in
 * SecurityConfig (/api/admin/** requires ROLE_ADMIN); @PreAuthorize is added
 * as defense-in-depth on individual mutating actions.
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final StudentService studentService;
    private final FacultyService facultyService;
    private final DepartmentService departmentService;
    private final CourseService courseService;
    private final SubjectService subjectService;
    private final NoticeService noticeService;
    private final AdminDashboardService adminDashboardService;

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<AdminDashboardDto>> dashboard() {
        return ResponseEntity.ok(ApiResponse.ok(adminDashboardService.getDashboard()));
    }

    // ---------- Students ----------
    @PostMapping("/students")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<StudentDto>> createStudent(@Valid @RequestBody CreateStudentRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Student created", studentService.createStudent(request)));
    }

    @GetMapping("/students")
    public ResponseEntity<ApiResponse<List<StudentDto>>> listStudents(
            @RequestParam(required = false) String department,
            @RequestParam(required = false) String semester,
            @RequestParam(required = false) String section,
            @RequestParam(required = false) String query) {
        return ResponseEntity.ok(ApiResponse.ok(studentService.searchStudents(department, semester, section, query)));
    }

    @GetMapping("/students/{id}")
    public ResponseEntity<ApiResponse<StudentDto>> getStudent(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(studentService.getStudentById(id)));
    }

    @PutMapping("/students/{id}")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<StudentDto>> updateStudent(@PathVariable Long id, @RequestBody UpdateStudentRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Student updated", studentService.updateStudent(id, request)));
    }

    @DeleteMapping("/students/{id}")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteStudent(@PathVariable Long id) {
        studentService.deleteStudent(id);
        return ResponseEntity.ok(ApiResponse.ok("Student deleted", null));
    }

    // ---------- Faculty ----------
    @PostMapping("/faculty")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<FacultyDto>> createFaculty(@Valid @RequestBody CreateFacultyRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Faculty created", facultyService.createFaculty(request)));
    }

    @GetMapping("/faculty")
    public ResponseEntity<ApiResponse<List<FacultyDto>>> listFaculty() {
        return ResponseEntity.ok(ApiResponse.ok(facultyService.getAllFaculty()));
    }

    @GetMapping("/faculty/{id}")
    public ResponseEntity<ApiResponse<FacultyDto>> getFaculty(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(facultyService.getFacultyById(id)));
    }

    @DeleteMapping("/faculty/{id}")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteFaculty(@PathVariable Long id) {
        facultyService.deleteFaculty(id);
        return ResponseEntity.ok(ApiResponse.ok("Faculty deleted", null));
    }

    // ---------- Departments ----------
    @PostMapping("/departments")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<DepartmentDto>> createDepartment(@Valid @RequestBody DepartmentRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Department created", departmentService.createDepartment(request)));
    }

    @GetMapping("/departments")
    public ResponseEntity<ApiResponse<List<DepartmentDto>>> listDepartments() {
        return ResponseEntity.ok(ApiResponse.ok(departmentService.getAllDepartments()));
    }

    @PutMapping("/departments/{id}")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<DepartmentDto>> updateDepartment(@PathVariable Long id, @Valid @RequestBody DepartmentRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Department updated", departmentService.updateDepartment(id, request)));
    }

    @DeleteMapping("/departments/{id}")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteDepartment(@PathVariable Long id) {
        departmentService.deleteDepartment(id);
        return ResponseEntity.ok(ApiResponse.ok("Department deleted", null));
    }

    // ---------- Courses ----------
    @PostMapping("/courses")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CourseDto>> createCourse(@Valid @RequestBody CourseRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Course created", courseService.createCourse(request)));
    }

    @GetMapping("/courses")
    public ResponseEntity<ApiResponse<List<CourseDto>>> listCourses() {
        return ResponseEntity.ok(ApiResponse.ok(courseService.getAllCourses()));
    }

    @DeleteMapping("/courses/{id}")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteCourse(@PathVariable Long id) {
        courseService.deleteCourse(id);
        return ResponseEntity.ok(ApiResponse.ok("Course deleted", null));
    }

    // ---------- Subjects ----------
    @PostMapping("/subjects")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<SubjectDto>> createSubject(@Valid @RequestBody SubjectRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Subject created", subjectService.createSubject(request)));
    }

    @GetMapping("/subjects")
    public ResponseEntity<ApiResponse<List<SubjectDto>>> listSubjects() {
        return ResponseEntity.ok(ApiResponse.ok(subjectService.getAllSubjects()));
    }

    @DeleteMapping("/subjects/{id}")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteSubject(@PathVariable Long id) {
        subjectService.deleteSubject(id);
        return ResponseEntity.ok(ApiResponse.ok("Subject deleted", null));
    }

    // ---------- Notices ----------
    @PostMapping("/notices")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<NoticeDto>> createNotice(@Valid @RequestBody NoticeRequest request,
                                                                @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(ApiResponse.ok("Notice posted", noticeService.createNotice(request, principal.getId())));
    }

    @GetMapping("/notices")
    public ResponseEntity<ApiResponse<List<NoticeDto>>> listNotices() {
        return ResponseEntity.ok(ApiResponse.ok(noticeService.getAllNotices()));
    }

    @DeleteMapping("/notices/{id}")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteNotice(@PathVariable Long id) {
        noticeService.deleteNotice(id);
        return ResponseEntity.ok(ApiResponse.ok("Notice deleted", null));
    }
}
