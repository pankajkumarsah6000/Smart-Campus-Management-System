package com.smartcampus.service.impl;

import com.smartcampus.dto.request.CreateStudentRequest;
import com.smartcampus.dto.request.UpdateStudentRequest;
import com.smartcampus.dto.response.*;
import com.smartcampus.entity.*;
import com.smartcampus.exception.DuplicateResourceException;
import com.smartcampus.exception.ResourceNotFoundException;
import com.smartcampus.repository.*;
import com.smartcampus.service.StudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StudentServiceImpl implements StudentService {

    private final StudentRepository studentRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final DepartmentRepository departmentRepository;
    private final AttendanceRepository attendanceRepository;
    private final AssignmentRepository assignmentRepository;
    private final SubmissionRepository submissionRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final TimetableRepository timetableRepository;
    private final NoticeRepository noticeRepository;
    private final ResultRepository resultRepository;
    private final ExaminationRepository examinationRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public StudentDto createStudent(CreateStudentRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("A user with this email already exists: " + request.getEmail());
        }
        if (studentRepository.findByRollNumber(request.getRollNumber()).isPresent()) {
            throw new DuplicateResourceException("A student with this roll number already exists: " + request.getRollNumber());
        }

        Role studentRole = roleRepository.findByName(RoleName.STUDENT)
                .orElseThrow(() -> new ResourceNotFoundException("STUDENT role is not configured"));

        String rawPassword = (request.getPassword() != null && !request.getPassword().isBlank())
                ? request.getPassword()
                : generateTempPassword();

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPhone(request.getPhone());
        Set<Role> roles = new HashSet<>();
        roles.add(studentRole);
        user.setRoles(roles);
        user = userRepository.save(user);

        Student student = new Student();
        student.setUser(user);
        student.setRollNumber(request.getRollNumber());
        student.setSemester(request.getSemester());
        student.setSection(request.getSection());
        student.setDateOfBirth(request.getDateOfBirth());
        student.setAdmissionDate(request.getAdmissionDate());

        if (request.getDepartmentId() != null) {
            Department dept = departmentRepository.findById(request.getDepartmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Department not found: " + request.getDepartmentId()));
            student.setDepartment(dept);
        }

        student = studentRepository.save(student);
        return toDto(student);
    }

    @Override
    public StudentDto getStudentById(Long id) {
        return toDto(findStudentOrThrow(id));
    }

    @Override
    public StudentDto getStudentByUserId(Long userId) {
        Student student = studentRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("No student profile linked to this account"));
        return toDto(student);
    }

    @Override
    public List<StudentDto> getAllStudents() {
        return studentRepository.findAll().stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    public List<StudentDto> searchStudents(String departmentCode, String semester, String section, String query) {
        return studentRepository.findAll().stream()
                .filter(s -> departmentCode == null || departmentCode.isBlank()
                        || (s.getDepartment() != null && departmentCode.equalsIgnoreCase(s.getDepartment().getCode())))
                .filter(s -> semester == null || semester.isBlank() || semester.equalsIgnoreCase(s.getSemester()))
                .filter(s -> section == null || section.isBlank() || section.equalsIgnoreCase(s.getSection()))
                .filter(s -> query == null || query.isBlank()
                        || s.getRollNumber().toLowerCase().contains(query.toLowerCase())
                        || s.getUser().getFirstName().toLowerCase().contains(query.toLowerCase())
                        || s.getUser().getLastName().toLowerCase().contains(query.toLowerCase())
                        || s.getUser().getEmail().toLowerCase().contains(query.toLowerCase()))
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public StudentDto updateStudent(Long id, UpdateStudentRequest request) {
        Student student = findStudentOrThrow(id);
        User user = student.getUser();

        if (request.getFirstName() != null) user.setFirstName(request.getFirstName());
        if (request.getLastName() != null) user.setLastName(request.getLastName());
        if (request.getPhone() != null) user.setPhone(request.getPhone());
        if (request.getSemester() != null) student.setSemester(request.getSemester());
        if (request.getSection() != null) student.setSection(request.getSection());
        if (request.getStatus() != null) student.setStatus(Student.StudentStatus.valueOf(request.getStatus()));
        if (request.getDepartmentId() != null) {
            Department dept = departmentRepository.findById(request.getDepartmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Department not found: " + request.getDepartmentId()));
            student.setDepartment(dept);
        }

        userRepository.save(user);
        student = studentRepository.save(student);
        return toDto(student);
    }

    @Override
    @Transactional
    public void deleteStudent(Long id) {
        Student student = findStudentOrThrow(id);
        studentRepository.delete(student);
        userRepository.delete(student.getUser());
    }

    @Override
    public StudentDashboardDto getDashboard(Long studentUserId) {
        Student student = studentRepository.findByUserId(studentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("No student profile linked to this account"));

        StudentDashboardDto dashboard = new StudentDashboardDto();
        dashboard.setProfile(toDto(student));

        long total = attendanceRepository.countTotalByStudent(student.getId());
        long present = attendanceRepository.countPresentByStudent(student.getId());
        dashboard.setAttendancePercentage(total == 0 ? 0.0 : (present * 100.0) / total);

        List<Enrollment> enrollments = enrollmentRepository.findByStudentId(student.getId());
        List<Long> subjectIds = enrollments.stream().map(e -> e.getSubject().getId()).collect(Collectors.toList());

        List<Assignment> allAssignments = subjectIds.stream()
                .flatMap(sid -> assignmentRepository.findBySubjectId(sid).stream())
                .collect(Collectors.toList());

        List<AssignmentSummaryDto> upcoming = allAssignments.stream()
                .map(a -> {
                    AssignmentSummaryDto dto = new AssignmentSummaryDto();
                    dto.setId(a.getId());
                    dto.setTitle(a.getTitle());
                    dto.setSubjectName(a.getSubject().getName());
                    dto.setDueDate(a.getDueDate());
                    boolean submitted = submissionRepository.findByAssignmentIdAndStudentId(a.getId(), student.getId()).isPresent();
                    dto.setSubmitted(submitted);
                    return dto;
                })
                .sorted((a, b) -> {
                    if (a.getDueDate() == null) return 1;
                    if (b.getDueDate() == null) return -1;
                    return a.getDueDate().compareTo(b.getDueDate());
                })
                .collect(Collectors.toList());

        dashboard.setUpcomingAssignments(upcoming.stream().limit(5).collect(Collectors.toList()));
        dashboard.setPendingAssignments(upcoming.stream().filter(a -> !a.isSubmitted()).count());

        List<Examination> futureExams = examinationRepository.findAll().stream()
                .filter(e -> subjectIds.contains(e.getSubject().getId()))
                .filter(e -> e.getExamDate() != null && e.getExamDate().isAfter(LocalDateTime.now()))
                .sorted(java.util.Comparator.comparing(Examination::getExamDate))
                .collect(Collectors.toList());
        if (!futureExams.isEmpty()) {
            Examination next = futureExams.get(0);
            dashboard.setNextExamInfo(next.getName() + " — " + next.getSubject().getName()
                    + " on " + next.getExamDate().toLocalDate()
                    + (next.getVenue() != null ? " at " + next.getVenue() : ""));
        }

        List<Result> results = resultRepository.findByStudentId(student.getId());
        if (!results.isEmpty()) {
            double avg = results.stream()
                    .filter(r -> r.getMarksObtained() != null)
                    .mapToDouble(Result::getMarksObtained)
                    .average().orElse(0.0);
            dashboard.setCgpa(Math.round((avg / 10.0) * 100.0) / 100.0);
        }

        DayOfWeek today = LocalDateTime.now().getDayOfWeek();
        List<Timetable> todaysSlots = timetableRepository.findBySectionAndSemester(student.getSection(), student.getSemester())
                .stream()
                .filter(t -> t.getDayOfWeek() == today)
                .sorted((a, b) -> a.getStartTime().compareTo(b.getStartTime()))
                .collect(Collectors.toList());

        dashboard.setTodaysTimetable(todaysSlots.stream().map(t -> {
            TimetableSlotDto dto = new TimetableSlotDto();
            dto.setId(t.getId());
            dto.setSubjectName(t.getSubject().getName());
            dto.setFacultyName(t.getFaculty() != null ? t.getFaculty().getUser().getFirstName() + " " + t.getFaculty().getUser().getLastName() : null);
            dto.setDayOfWeek(t.getDayOfWeek().name());
            dto.setStartTime(t.getStartTime().toString());
            dto.setEndTime(t.getEndTime().toString());
            dto.setRoom(t.getRoom());
            return dto;
        }).collect(Collectors.toList()));

        List<Notice> notices = noticeRepository.findByTargetAudienceOrderByCreatedAtDesc(Notice.TargetAudience.STUDENTS);
        List<Notice> allNotices = noticeRepository.findByTargetAudienceOrderByCreatedAtDesc(Notice.TargetAudience.ALL);
        notices = new java.util.ArrayList<>(notices);
        notices.addAll(allNotices);
        notices.sort((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()));

        dashboard.setRecentNotices(notices.stream().limit(5).map(n -> {
            NoticeDto dto = new NoticeDto();
            dto.setId(n.getId());
            dto.setTitle(n.getTitle());
            dto.setContent(n.getContent());
            dto.setPostedByName(n.getPostedBy().getFirstName() + " " + n.getPostedBy().getLastName());
            dto.setTargetAudience(n.getTargetAudience().name());
            dto.setPriority(n.getPriority().name());
            dto.setCreatedAt(n.getCreatedAt());
            return dto;
        }).collect(Collectors.toList()));

        return dashboard;
    }

    private Student findStudentOrThrow(Long id) {
        return studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + id));
    }

    private String generateTempPassword() {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz23456789!@#";
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 12; i++) sb.append(chars.charAt(random.nextInt(chars.length())));
        return sb.toString();
    }

    private StudentDto toDto(Student s) {
        StudentDto dto = new StudentDto();
        dto.setId(s.getId());
        dto.setUserId(s.getUser().getId());
        dto.setEmail(s.getUser().getEmail());
        dto.setFirstName(s.getUser().getFirstName());
        dto.setLastName(s.getUser().getLastName());
        dto.setPhone(s.getUser().getPhone());
        dto.setRollNumber(s.getRollNumber());
        if (s.getDepartment() != null) {
            dto.setDepartmentId(s.getDepartment().getId());
            dto.setDepartmentName(s.getDepartment().getName());
        }
        dto.setSemester(s.getSemester());
        dto.setSection(s.getSection());
        dto.setDateOfBirth(s.getDateOfBirth());
        dto.setAdmissionDate(s.getAdmissionDate());
        dto.setStatus(s.getStatus().name());
        return dto;
    }
}
