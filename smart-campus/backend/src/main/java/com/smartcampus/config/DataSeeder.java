package com.smartcampus.config;

import com.smartcampus.entity.*;
import com.smartcampus.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Seeds the four roles, one demo admin, and a realistic set of demo data on
 * first boot so the system is usable immediately across all roles.
 *
 * Demo accounts (CHANGE THESE IN PRODUCTION):
 *   admin@smartcampus.edu   / Admin@123
 *   faculty1@smartcampus.edu / Faculty@123
 *   faculty2@smartcampus.edu / Faculty@123
 *   student1@smartcampus.edu / Student@123
 *   student2@smartcampus.edu / Student@123
 *   student3@smartcampus.edu / Student@123
 *   parent@smartcampus.edu  / Parent@123   (linked to student1)
 */
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final DepartmentRepository departmentRepository;
    private final CourseRepository courseRepository;
    private final FacultyRepository facultyRepository;
    private final StudentRepository studentRepository;
    private final ParentRepository parentRepository;
    private final SubjectRepository subjectRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final TimetableRepository timetableRepository;
    private final AttendanceRepository attendanceRepository;
    private final AssignmentRepository assignmentRepository;
    private final ExaminationRepository examinationRepository;
    private final ResultRepository resultRepository;
    private final FeeRepository feeRepository;
    private final NoticeRepository noticeRepository;
    private final ComplaintRepository complaintRepository;

    @Override
    @Transactional
    public void run(String... args) {
        seedRoles();
        seedAdmin();
        seedDemoData();
    }

    private void seedRoles() {
        for (RoleName roleName : RoleName.values()) {
            if (roleRepository.findByName(roleName).isEmpty()) {
                Role role = new Role();
                role.setName(roleName);
                roleRepository.save(role);
            }
        }
    }

    private void seedAdmin() {
        if (!userRepository.existsByEmail("admin@smartcampus.edu")) {
            Role adminRole = roleRepository.findByName(RoleName.ADMIN).orElseThrow();

            User admin = new User();
            admin.setEmail("admin@smartcampus.edu");
            admin.setPasswordHash(passwordEncoder.encode("Admin@123"));
            admin.setFirstName("Campus");
            admin.setLastName("Administrator");
            admin.setEnabled(true);
            Set<Role> roles = new HashSet<>();
            roles.add(adminRole);
            admin.setRoles(roles);
            userRepository.save(admin);
        }
    }

    private void seedDemoData() {
        // One-time guard: never re-run demo seeding once the first demo student exists.
        if (userRepository.existsByEmail("student1@smartcampus.edu")) {
            return;
        }

        // ---- Departments ----
        Department cse = department("CSE", "Computer Science & Engineering");
        Department ece = department("ECE", "Electronics & Communication Engineering");
        department("ME", "Mechanical Engineering");

        // ---- Courses ----
        Course cseCourse = course("B.Tech Computer Science", "BSC-CSE", cse, 8);
        course("B.Tech Electronics", "BSC-ECE", ece, 8);

        // ---- Faculty ----
        User f1u = user("faculty1@smartcampus.edu", "Faculty@123", "Anil", "Sharma", "9898011121", RoleName.FACULTY);
        User f2u = user("faculty2@smartcampus.edu", "Faculty@123", "Priya", "Verma", "9898011122", RoleName.FACULTY);
        Faculty f1 = faculty(f1u, "EMP001", cse, "Professor", "PhD Computer Science");
        Faculty f2 = faculty(f2u, "EMP002", cse, "Assistant Professor", "MTech CSE");

        // ---- Students ----
        User s1u = user("student1@smartcampus.edu", "Student@123", "Rahul", "Kumar", "9898011131", RoleName.STUDENT);
        User s2u = user("student2@smartcampus.edu", "Student@123", "Sneha", "Gupta", "9898011132", RoleName.STUDENT);
        User s3u = user("student3@smartcampus.edu", "Student@123", "Arjun", "Mehta", "9898011133", RoleName.STUDENT);
        Student s1 = student(s1u, "ROLL-2024-001", cse, "3", "A", LocalDate.of(2004, 5, 14));
        Student s2 = student(s2u, "ROLL-2024-002", cse, "3", "A", LocalDate.of(2004, 8, 2));
        Student s3 = student(s3u, "ROLL-2024-003", cse, "3", "A", LocalDate.of(2004, 11, 23));

        // ---- Parent (linked to student1 only) ----
        User p1u = user("parent@smartcampus.edu", "Parent@123", "Rajesh", "Kumar", "9898011141", RoleName.PARENT);
        Parent parent = new Parent();
        parent.setUser(p1u);
        parent.setRelationship("Father");
        Set<Student> children = new HashSet<>();
        children.add(s1);
        parent.setChildren(children);
        parentRepository.save(parent);

        // ---- Subjects ----
        Subject ds = subject("Data Structures", "CS301", cseCourse, f1, 4, "3");
        Subject dbms = subject("Database Management Systems", "CS302", cseCourse, f2, 3, "3");
        Subject os = subject("Operating Systems", "CS303", cseCourse, f1, 4, "3");

        // ---- Enrollments ----
        enroll(s1, ds);
        enroll(s1, dbms);
        enroll(s1, os);
        enroll(s2, ds);
        enroll(s2, dbms);
        enroll(s2, os);
        enroll(s3, ds);
        enroll(s3, dbms);
        enroll(s3, os);

        // ---- Timetable ----
        slot(ds, f1, "A", "3", DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(10, 0), "LT-101");
        slot(dbms, f2, "A", "3", DayOfWeek.MONDAY, LocalTime.of(10, 0), LocalTime.of(11, 0), "LT-102");
        slot(os, f1, "A", "3", DayOfWeek.WEDNESDAY, LocalTime.of(9, 0), LocalTime.of(10, 0), "LT-103");
        slot(ds, f1, "A", "3", DayOfWeek.FRIDAY, LocalTime.of(14, 0), LocalTime.of(15, 0), "LT-101");

        // ---- Attendance (last 5 working days, Data Structures) ----
        LocalDate today = LocalDate.now();
        for (int i = 5; i >= 1; i--) {
            LocalDate day = today.minusDays(i);
            if (day.getDayOfWeek() == DayOfWeek.SUNDAY) continue;
            attendance(s1, ds, f1, day, "PRESENT");
            attendance(s2, ds, f1, day, "PRESENT");
            attendance(s3, ds, f1, day, i == 3 ? "ABSENT" : "PRESENT");
        }

        // ---- Assignments ----
        assignment("Binary Trees Implementation", "Implement insert, delete and traversal for a binary search tree.",
                ds, f1, LocalDateTime.now().plusDays(5), 20);
        assignment("ER Diagram for Library System", "Design a normalized ER diagram and write schema DDL.",
                dbms, f2, LocalDateTime.now().plusDays(7), 15);

        // ---- Examinations ----
        Examination midTerm = exam("Mid-Term Examination", ds, LocalDateTime.now().plusDays(10), 120, 100, "Exam Hall 1");
        exam("Unit Test 1", dbms, LocalDateTime.now().plusDays(3), 60, 50, "Exam Hall 2");

        // ---- Result (student1 mid-term) ----
        Result result = new Result();
        result.setExamination(midTerm);
        result.setStudent(s1);
        result.setMarksObtained(82.0);
        result.setGrade("A");
        result.setPassed(true);
        result.setRemarks("Good understanding of core concepts.");
        resultRepository.save(result);

        // ---- Fees ----
        fee(s1, "Tuition", new BigDecimal("25000.00"), LocalDate.now().plusMonths(3), "3");
        fee(s2, "Tuition", new BigDecimal("25000.00"), LocalDate.now().plusMonths(3), "3");
        fee(s1, "Hostel", new BigDecimal("45000.00"), LocalDate.now().plusMonths(5), "3");

        // ---- Notices ----
        notice("Mid-term exam schedule announced", "Mid-term examinations start in 10 days. See the exams section for details.",
                s1u, Notice.TargetAudience.STUDENTS, Notice.Priority.HIGH);
        notice("Annual Tech Fest registrations open", "Register your teams for the annual tech fest before the deadline.",
                s1u, Notice.TargetAudience.ALL, Notice.Priority.NORMAL);

        // ---- Complaint ----
        Complaint complaint = new Complaint();
        complaint.setRaisedBy(s1u);
        complaint.setSubject("Library timings on weekends");
        complaint.setDescription("The library closes too early on Saturdays. Can weekend hours be extended?");
        complaint.setCategory(Complaint.ComplaintCategory.INFRASTRUCTURE);
        complaint.setStatus(Complaint.ComplaintStatus.OPEN);
        complaintRepository.save(complaint);
    }

    // ---------- helpers ----------

    private Department department(String code, String name) {
        return departmentRepository.findByCode(code).orElseGet(() -> {
            Department d = new Department();
            d.setCode(code);
            d.setName(name);
            return departmentRepository.save(d);
        });
    }

    private Course course(String name, String code, Department dept, int duration) {
        return courseRepository.findByCode(code).orElseGet(() -> {
            Course c = new Course();
            c.setName(name);
            c.setCode(code);
            c.setDepartment(dept);
            c.setDurationSemesters(duration);
            return courseRepository.save(c);
        });
    }

    private User user(String email, String rawPassword, String firstName, String lastName, String phone, RoleName roleName) {
        Role role = roleRepository.findByName(roleName).orElseThrow();
        User u = new User();
        u.setEmail(email);
        u.setPasswordHash(passwordEncoder.encode(rawPassword));
        u.setFirstName(firstName);
        u.setLastName(lastName);
        u.setPhone(phone);
        u.setEnabled(true);
        Set<Role> roles = new HashSet<>();
        roles.add(role);
        u.setRoles(roles);
        return userRepository.save(u);
    }

    private Faculty faculty(User u, String employeeId, Department dept, String designation, String qualification) {
        Faculty f = new Faculty();
        f.setUser(u);
        f.setEmployeeId(employeeId);
        f.setDepartment(dept);
        f.setDesignation(designation);
        f.setQualification(qualification);
        return facultyRepository.save(f);
    }

    private Student student(User u, String roll, Department dept, String semester, String section, LocalDate dob) {
        Student st = new Student();
        st.setUser(u);
        st.setRollNumber(roll);
        st.setDepartment(dept);
        st.setSemester(semester);
        st.setSection(section);
        st.setDateOfBirth(dob);
        st.setAdmissionDate(LocalDate.of(2024, 7, 15));
        st.setStatus(Student.StudentStatus.ACTIVE);
        return studentRepository.save(st);
    }

    private Subject subject(String name, String code, Course course, Faculty f, int credits, String semester) {
        Subject s = new Subject();
        s.setName(name);
        s.setCode(code);
        s.setCourse(course);
        s.setFaculty(f);
        s.setCredits(credits);
        s.setSemester(semester);
        return subjectRepository.save(s);
    }

    private void enroll(Student student, Subject subject) {
        Enrollment e = new Enrollment();
        e.setStudent(student);
        e.setSubject(subject);
        e.setStatus(Enrollment.EnrollmentStatus.ACTIVE);
        enrollmentRepository.save(e);
    }

    private void slot(Subject subject, Faculty faculty, String section, String semester, DayOfWeek day,
                      LocalTime start, LocalTime end, String room) {
        Timetable t = new Timetable();
        t.setSubject(subject);
        t.setFaculty(faculty);
        t.setSection(section);
        t.setSemester(semester);
        t.setDayOfWeek(day);
        t.setStartTime(start);
        t.setEndTime(end);
        t.setRoom(room);
        timetableRepository.save(t);
    }

    private void attendance(Student student, Subject subject, Faculty markedBy, LocalDate date, String status) {
        if (attendanceRepository.findBySubjectIdAndAttendanceDate(subject.getId(), date).stream()
                .anyMatch(a -> a.getStudent().getId().equals(student.getId()))) {
            return;
        }
        Attendance a = new Attendance();
        a.setStudent(student);
        a.setSubject(subject);
        a.setMarkedBy(markedBy);
        a.setAttendanceDate(date);
        a.setStatus(Attendance.AttendanceStatus.valueOf(status));
        attendanceRepository.save(a);
    }

    private void assignment(String title, String description, Subject subject, Faculty createdBy,
                            LocalDateTime dueDate, int maxMarks) {
        Assignment a = new Assignment();
        a.setTitle(title);
        a.setDescription(description);
        a.setSubject(subject);
        a.setCreatedBy(createdBy);
        a.setDueDate(dueDate);
        a.setMaxMarks(maxMarks);
        assignmentRepository.save(a);
    }

    private Examination exam(String name, Subject subject, LocalDateTime examDate, int durationMinutes,
                             int maxMarks, String venue) {
        Examination e = new Examination();
        e.setName(name);
        e.setSubject(subject);
        e.setExamDate(examDate);
        e.setDurationMinutes(durationMinutes);
        e.setMaxMarks(maxMarks);
        e.setVenue(venue);
        return examinationRepository.save(e);
    }

    private void fee(Student student, String feeType, BigDecimal amount, LocalDate dueDate, String semester) {
        Fee f = new Fee();
        f.setStudent(student);
        f.setFeeType(feeType);
        f.setAmount(amount);
        f.setAmountPaid(feeType.equals("Hostel") ? new BigDecimal("15000.00") : BigDecimal.ZERO);
        f.setDueDate(dueDate);
        f.setSemester(semester);
        BigDecimal paid = f.getAmountPaid();
        if (paid.compareTo(amount) >= 0) {
            f.setStatus(Fee.FeeStatus.PAID);
        } else if (paid.compareTo(BigDecimal.ZERO) > 0) {
            f.setStatus(Fee.FeeStatus.PARTIAL);
        } else if (dueDate != null && dueDate.isBefore(LocalDate.now())) {
            f.setStatus(Fee.FeeStatus.OVERDUE);
        } else {
            f.setStatus(Fee.FeeStatus.PENDING);
        }
        feeRepository.save(f);
    }

    private void notice(String title, String content, User postedBy, Notice.TargetAudience audience, Notice.Priority priority) {
        Notice n = new Notice();
        n.setTitle(title);
        n.setContent(content);
        n.setPostedBy(postedBy);
        n.setTargetAudience(audience);
        n.setPriority(priority);
        noticeRepository.save(n);
    }
}