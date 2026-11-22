package com.smartcampus.service.impl;

import com.smartcampus.dto.request.CreateFacultyRequest;
import com.smartcampus.dto.response.AssignmentDto;
import com.smartcampus.dto.response.FacultyDashboardDto;
import com.smartcampus.dto.response.FacultyDto;
import com.smartcampus.dto.response.StudentDto;
import com.smartcampus.dto.response.SubjectDto;
import com.smartcampus.entity.*;
import com.smartcampus.exception.DuplicateResourceException;
import com.smartcampus.exception.ResourceNotFoundException;
import com.smartcampus.exception.UnauthorizedException;
import com.smartcampus.repository.*;
import com.smartcampus.service.FacultyService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FacultyServiceImpl implements FacultyService {

    private final FacultyRepository facultyRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final DepartmentRepository departmentRepository;
    private final SubjectRepository subjectRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final AssignmentRepository assignmentRepository;
    private final LeaveRequestRepository leaveRequestRepository;
    private final ComplaintRepository complaintRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public FacultyDto createFaculty(CreateFacultyRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("A user with this email already exists: " + request.getEmail());
        }
        if (facultyRepository.findByEmployeeId(request.getEmployeeId()).isPresent()) {
            throw new DuplicateResourceException("A faculty member with this employee ID already exists: " + request.getEmployeeId());
        }

        Role facultyRole = roleRepository.findByName(RoleName.FACULTY)
                .orElseThrow(() -> new ResourceNotFoundException("FACULTY role is not configured"));

        String rawPassword = (request.getPassword() != null && !request.getPassword().isBlank())
                ? request.getPassword() : generateTempPassword();

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPhone(request.getPhone());
        Set<Role> roles = new HashSet<>();
        roles.add(facultyRole);
        user.setRoles(roles);
        user = userRepository.save(user);

        Faculty faculty = new Faculty();
        faculty.setUser(user);
        faculty.setEmployeeId(request.getEmployeeId());
        faculty.setDesignation(request.getDesignation());
        faculty.setQualification(request.getQualification());

        if (request.getDepartmentId() != null) {
            Department dept = departmentRepository.findById(request.getDepartmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Department not found: " + request.getDepartmentId()));
            faculty.setDepartment(dept);
        }

        faculty = facultyRepository.save(faculty);
        return toDto(faculty);
    }

    @Override
    public FacultyDto getFacultyById(Long id) {
        return toDto(facultyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Faculty not found with id: " + id)));
    }

    @Override
    public FacultyDto getFacultyByUserId(Long userId) {
        return toDto(facultyRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("No faculty profile linked to this account")));
    }

    @Override
    public List<FacultyDto> getAllFaculty() {
        return facultyRepository.findAll().stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteFaculty(Long id) {
        Faculty faculty = facultyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Faculty not found with id: " + id));
        facultyRepository.delete(faculty);
        userRepository.delete(faculty.getUser());
    }

    @Override
    public FacultyDashboardDto getDashboard(Long userId) {
        Faculty faculty = findFacultyByUserId(userId);
        FacultyDashboardDto dashboard = new FacultyDashboardDto();
        dashboard.setProfile(toDto(faculty));

        List<Subject> subjects = subjectRepository.findByFacultyId(faculty.getId());
        dashboard.setSubjects(subjects.stream().map(this::toSubjectDto).collect(Collectors.toList()));
        dashboard.setSubjectCount(subjects.size());

        dashboard.setTotalStudents(subjects.stream()
                .flatMap(s -> enrollmentRepository.findBySubjectId(s.getId()).stream())
                .map(e -> e.getStudent().getId())
                .distinct()
                .count());

        dashboard.setPendingLeaveRequests(
                leaveRequestRepository.findByStatus(LeaveRequest.LeaveStatus.PENDING).size());
        dashboard.setOpenComplaints(
                complaintRepository.findByStatus(Complaint.ComplaintStatus.OPEN).size());

        List<AssignmentDto> recent = subjects.stream()
                .flatMap(s -> assignmentRepository.findBySubjectId(s.getId()).stream())
                .map(this::toAssignmentDto)
                .sorted(Comparator.comparing(a -> a.getDueDate() == null ? java.time.LocalDateTime.MAX : a.getDueDate()))
                .limit(5)
                .collect(Collectors.toList());
        dashboard.setRecentAssignments(recent);
        return dashboard;
    }

    @Override
    public List<SubjectDto> getMySubjects(Long userId) {
        Faculty faculty = findFacultyByUserId(userId);
        return subjectRepository.findByFacultyId(faculty.getId()).stream()
                .map(this::toSubjectDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<StudentDto> getStudentsForSubject(Long subjectId, Long facultyUserId) {
        Faculty faculty = findFacultyByUserId(facultyUserId);
        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new ResourceNotFoundException("Subject not found with id: " + subjectId));

        if (subject.getFaculty() == null || !subject.getFaculty().getId().equals(faculty.getId())) {
            throw new UnauthorizedException("You are not assigned to teach this subject");
        }

        return enrollmentRepository.findBySubjectId(subjectId).stream()
                .map(Enrollment::getStudent)
                .map(this::toStudentDto)
                .collect(Collectors.toList());
    }

    private Faculty findFacultyByUserId(Long userId) {
        return facultyRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("No faculty profile linked to this account"));
    }

    private String generateTempPassword() {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz23456789!@#";
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 12; i++) sb.append(chars.charAt(random.nextInt(chars.length())));
        return sb.toString();
    }

    private SubjectDto toSubjectDto(Subject s) {
        SubjectDto dto = new SubjectDto();
        dto.setId(s.getId());
        dto.setName(s.getName());
        dto.setCode(s.getCode());
        if (s.getCourse() != null) {
            dto.setCourseId(s.getCourse().getId());
            dto.setCourseName(s.getCourse().getName());
        }
        if (s.getFaculty() != null) {
            dto.setFacultyId(s.getFaculty().getId());
            dto.setFacultyName(s.getFaculty().getUser().getFirstName() + " " + s.getFaculty().getUser().getLastName());
        }
        dto.setCredits(s.getCredits());
        dto.setSemester(s.getSemester());
        return dto;
    }

    private StudentDto toStudentDto(Student s) {
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
        dto.setStatus(s.getStatus().name());
        return dto;
    }

    private AssignmentDto toAssignmentDto(Assignment a) {
        AssignmentDto dto = new AssignmentDto();
        dto.setId(a.getId());
        dto.setTitle(a.getTitle());
        dto.setDescription(a.getDescription());
        dto.setSubjectId(a.getSubject().getId());
        dto.setSubjectName(a.getSubject().getName());
        dto.setSubjectCode(a.getSubject().getCode());
        dto.setCreatedByFacultyId(a.getCreatedBy().getId());
        dto.setCreatedByFacultyName(a.getCreatedBy().getUser().getFirstName() + " " + a.getCreatedBy().getUser().getLastName());
        dto.setDueDate(a.getDueDate());
        dto.setMaxMarks(a.getMaxMarks());
        dto.setAttachmentUrl(a.getAttachmentUrl());
        return dto;
    }

    private FacultyDto toDto(Faculty f) {
        FacultyDto dto = new FacultyDto();
        dto.setId(f.getId());
        dto.setUserId(f.getUser().getId());
        dto.setEmail(f.getUser().getEmail());
        dto.setFirstName(f.getUser().getFirstName());
        dto.setLastName(f.getUser().getLastName());
        dto.setPhone(f.getUser().getPhone());
        dto.setEmployeeId(f.getEmployeeId());
        if (f.getDepartment() != null) {
            dto.setDepartmentId(f.getDepartment().getId());
            dto.setDepartmentName(f.getDepartment().getName());
        }
        dto.setDesignation(f.getDesignation());
        dto.setQualification(f.getQualification());
        return dto;
    }
}