package com.smartcampus.service.impl;

import com.smartcampus.dto.request.AssignmentRequest;
import com.smartcampus.dto.request.AssignmentSubmitRequest;
import com.smartcampus.dto.request.GradeSubmissionRequest;
import com.smartcampus.dto.response.AssignmentDto;
import com.smartcampus.dto.response.SubmissionDto;
import com.smartcampus.entity.*;
import com.smartcampus.exception.BadRequestException;
import com.smartcampus.exception.ResourceNotFoundException;
import com.smartcampus.exception.UnauthorizedException;
import com.smartcampus.repository.*;
import com.smartcampus.service.AssignmentService;
import com.smartcampus.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AssignmentServiceImpl implements AssignmentService {

    private final AssignmentRepository assignmentRepository;
    private final SubmissionRepository submissionRepository;
    private final SubjectRepository subjectRepository;
    private final StudentRepository studentRepository;
    private final FacultyRepository facultyRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public AssignmentDto createAssignment(AssignmentRequest request, Long callerUserId, boolean admin) {
        Subject subject = subjectRepository.findById(request.getSubjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Subject not found with id: " + request.getSubjectId()));
        Faculty createdBy = resolveTeachingFaculty(callerUserId, subject, admin);

        Assignment assignment = new Assignment();
        assignment.setTitle(request.getTitle());
        assignment.setDescription(request.getDescription());
        assignment.setSubject(subject);
        assignment.setCreatedBy(createdBy);
        assignment.setDueDate(request.getDueDate());
        assignment.setMaxMarks(request.getMaxMarks());
        assignment.setAttachmentUrl(request.getAttachmentUrl());

        return toFacultyDto(assignmentRepository.save(assignment));
    }

    @Override
    @Transactional
    public AssignmentDto updateAssignment(Long id, AssignmentRequest request, Long callerUserId, boolean admin) {
        Assignment assignment = findAssignmentOrThrow(id);
        if (!admin) {
            requireTeaching(assignment.getSubject(), callerUserId);
        }
        Subject subject = subjectRepository.findById(request.getSubjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Subject not found with id: " + request.getSubjectId()));

        assignment.setTitle(request.getTitle());
        assignment.setDescription(request.getDescription());
        assignment.setSubject(subject);
        assignment.setDueDate(request.getDueDate());
        assignment.setMaxMarks(request.getMaxMarks());
        assignment.setAttachmentUrl(request.getAttachmentUrl());
        return toFacultyDto(assignmentRepository.save(assignment));
    }

    @Override
    @Transactional
    public void deleteAssignment(Long id, Long callerUserId, boolean admin) {
        Assignment assignment = findAssignmentOrThrow(id);
        if (!admin) {
            requireTeaching(assignment.getSubject(), callerUserId);
        }
        assignmentRepository.delete(assignment);
    }

    @Override
    public List<AssignmentDto> getMyAssignments(Long callerUserId) {
        // Role-agnostic: choose view based on which profile exists for the caller.
        if (studentRepository.findByUserId(callerUserId).isPresent()) {
            Student student = studentRepository.findByUserId(callerUserId).orElseThrow();
            List<Long> subjectIds = enrollmentRepository.findByStudentId(student.getId()).stream()
                    .map(e -> e.getSubject().getId())
                    .collect(Collectors.toList());
            return subjectIds.stream()
                    .flatMap(sid -> assignmentRepository.findBySubjectId(sid).stream())
                    .map(a -> toStudentDto(a, student))
                    .sorted((a, b) -> compareDue(a.getDueDate(), b.getDueDate()))
                    .collect(Collectors.toList());
        }
        if (facultyRepository.findByUserId(callerUserId).isPresent()) {
            Faculty faculty = facultyRepository.findByUserId(callerUserId).orElseThrow();
            return assignmentRepository.findByCreatedById(faculty.getId()).stream()
                    .map(this::toFacultyDto)
                    .sorted((a, b) -> compareDue(a.getDueDate(), b.getDueDate()))
                    .collect(Collectors.toList());
        }
        return getAllAssignments();
    }

    @Override
    public List<AssignmentDto> getAllAssignments() {
        return assignmentRepository.findAll().stream()
                .map(this::toFacultyDto)
                .sorted((a, b) -> compareDue(a.getDueDate(), b.getDueDate()))
                .collect(Collectors.toList());
    }

    @Override
    public AssignmentDto getAssignmentDetail(Long id, Long callerUserId, boolean admin) {
        Assignment assignment = findAssignmentOrThrow(id);
        if (admin) {
            return toFacultyDto(assignment);
        }
        if (studentRepository.findByUserId(callerUserId).isPresent()) {
            Student student = studentRepository.findByUserId(callerUserId).orElseThrow();
            requireEnrolled(student, assignment.getSubject());
            return toStudentDto(assignment, student);
        }
        if (facultyRepository.findByUserId(callerUserId).isPresent()) {
            requireTeaching(assignment.getSubject(), callerUserId);
            return toFacultyDto(assignment);
        }
        throw new UnauthorizedException("You do not have access to this assignment");
    }

    @Override
    @Transactional
    public SubmissionDto submitAssignment(Long assignmentId, AssignmentSubmitRequest request, Long studentUserId) {
        Assignment assignment = findAssignmentOrThrow(assignmentId);
        Student student = studentRepository.findByUserId(studentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("No student profile linked to this account"));
        requireEnrolled(student, assignment.getSubject());

        submissionRepository.findByAssignmentIdAndStudentId(assignment.getId(), student.getId())
                .ifPresent(s -> {
                    throw new BadRequestException("You have already submitted this assignment");
                });

        if ((request.getTextAnswer() == null || request.getTextAnswer().isBlank())
                && (request.getFileUrl() == null || request.getFileUrl().isBlank())) {
            throw new BadRequestException("Provide either a text answer or a file");
        }

        Submission submission = new Submission();
        submission.setAssignment(assignment);
        submission.setStudent(student);
        submission.setTextAnswer(request.getTextAnswer());
        submission.setFileUrl(request.getFileUrl());
        submission.setSubmittedAt(LocalDateTime.now());
        submission.setStatus(assignment.getDueDate() != null && LocalDateTime.now().isAfter(assignment.getDueDate())
                ? Submission.SubmissionStatus.LATE : Submission.SubmissionStatus.SUBMITTED);
        submission = submissionRepository.save(submission);

        notificationService.notifyUser(
                assignment.getCreatedBy().getUser().getId(),
                "New submission — " + assignment.getTitle(),
                student.getUser().getFirstName() + " " + student.getUser().getLastName() + " submitted " + assignment.getTitle() + ".",
                "ASSIGNMENT",
                "/faculty/assignments"
        );

        return toDto(submission);
    }

    @Override
    public List<SubmissionDto> getSubmissionsForAssignment(Long assignmentId, Long callerUserId, boolean admin) {
        Assignment assignment = findAssignmentOrThrow(assignmentId);
        if (!admin) {
            requireTeaching(assignment.getSubject(), callerUserId);
        }
        return submissionRepository.findByAssignmentId(assignment.getId()).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public SubmissionDto gradeSubmission(Long submissionId, GradeSubmissionRequest request, Long callerUserId, boolean admin) {
        Submission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Submission not found with id: " + submissionId));
        if (!admin) {
            requireTeaching(submission.getAssignment().getSubject(), callerUserId);
        }
        submission.setMarksAwarded(request.getMarksAwarded());
        submission.setFeedback(request.getFeedback());
        submission.setStatus(Submission.SubmissionStatus.EVALUATED);
        submission = submissionRepository.save(submission);

        notificationService.notifyUser(
                submission.getStudent().getUser().getId(),
                "Assignment graded — " + submission.getAssignment().getTitle(),
                "Your submission for " + submission.getAssignment().getTitle() + " has been graded"
                        + (request.getMarksAwarded() != null ? " (" + request.getMarksAwarded() + " marks)" : "") + ".",
                "ASSIGNMENT",
                "/student/assignments"
        );

        return toDto(submission);
    }

    @Override
    public List<SubmissionDto> getMySubmissions(Long studentUserId) {
        Student student = studentRepository.findByUserId(studentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("No student profile linked to this account"));
        return submissionRepository.findByStudentId(student.getId()).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    private Assignment findAssignmentOrThrow(Long id) {
        return assignmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment not found with id: " + id));
    }

    private void requireEnrolled(Student student, Subject subject) {
        boolean enrolled = enrollmentRepository.findByStudentId(student.getId()).stream()
                .anyMatch(e -> e.getSubject().getId().equals(subject.getId()));
        if (!enrolled) {
            throw new UnauthorizedException("You are not enrolled in this subject");
        }
    }

    private void requireTeaching(Subject subject, Long callerUserId) {
        Faculty faculty = facultyRepository.findByUserId(callerUserId)
                .orElseThrow(() -> new UnauthorizedException("No faculty profile linked to this account"));
        if (subject.getFaculty() == null || !subject.getFaculty().getId().equals(faculty.getId())) {
            throw new UnauthorizedException("You are not assigned to teach this subject");
        }
    }

    private Faculty resolveTeachingFaculty(Long callerUserId, Subject subject, boolean admin) {
        if (admin) {
            if (subject.getFaculty() == null) {
                throw new BadRequestException("This subject has no assigned faculty; assign a faculty member first");
            }
            return subject.getFaculty();
        }
        Faculty faculty = facultyRepository.findByUserId(callerUserId)
                .orElseThrow(() -> new UnauthorizedException("No faculty profile linked to this account"));
        if (subject.getFaculty() == null || !subject.getFaculty().getId().equals(faculty.getId())) {
            throw new UnauthorizedException("You are not assigned to teach this subject");
        }
        return faculty;
    }

    private int compareDue(java.time.LocalDateTime a, java.time.LocalDateTime b) {
        if (a == null) return 1;
        if (b == null) return -1;
        return a.compareTo(b);
    }

    private AssignmentDto toStudentDto(Assignment a, Student student) {
        AssignmentDto dto = toFacultyDto(a);
        submissionRepository.findByAssignmentIdAndStudentId(a.getId(), student.getId())
                .ifPresentOrElse(s -> {
                    dto.setSubmitted(true);
                    dto.setSubmissionStatus(s.getStatus().name());
                    dto.setAwardedMarks(s.getMarksAwarded());
                }, () -> dto.setSubmitted(false));
        return dto;
    }

    private AssignmentDto toFacultyDto(Assignment a) {
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
        dto.setSubmissionCount(submissionRepository.findByAssignmentId(a.getId()).stream().count());
        return dto;
    }

    private SubmissionDto toDto(Submission s) {
        SubmissionDto dto = new SubmissionDto();
        dto.setId(s.getId());
        dto.setAssignmentId(s.getAssignment().getId());
        dto.setAssignmentTitle(s.getAssignment().getTitle());
        dto.setSubjectId(s.getAssignment().getSubject().getId());
        dto.setSubjectName(s.getAssignment().getSubject().getName());
        dto.setStudentId(s.getStudent().getId());
        dto.setStudentName(s.getStudent().getUser().getFirstName() + " " + s.getStudent().getUser().getLastName());
        dto.setRollNumber(s.getStudent().getRollNumber());
        dto.setFileUrl(s.getFileUrl());
        dto.setTextAnswer(s.getTextAnswer());
        dto.setSubmittedAt(s.getSubmittedAt());
        dto.setMarksAwarded(s.getMarksAwarded());
        dto.setFeedback(s.getFeedback());
        dto.setStatus(s.getStatus().name());
        dto.setMaxMarks(s.getAssignment().getMaxMarks());
        return dto;
    }
}