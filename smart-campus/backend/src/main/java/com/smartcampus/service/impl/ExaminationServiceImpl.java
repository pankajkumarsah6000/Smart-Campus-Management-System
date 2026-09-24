package com.smartcampus.service.impl;

import com.smartcampus.dto.request.ExaminationRequest;
import com.smartcampus.dto.request.ResultRequest;
import com.smartcampus.dto.response.ExaminationDto;
import com.smartcampus.dto.response.ResultDto;
import com.smartcampus.entity.*;
import com.smartcampus.exception.BadRequestException;
import com.smartcampus.exception.ResourceNotFoundException;
import com.smartcampus.exception.UnauthorizedException;
import com.smartcampus.repository.*;
import com.smartcampus.service.ExaminationService;
import com.smartcampus.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ExaminationServiceImpl implements ExaminationService {

    private final ExaminationRepository examinationRepository;
    private final ResultRepository resultRepository;
    private final SubjectRepository subjectRepository;
    private final StudentRepository studentRepository;
    private final FacultyRepository facultyRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public ExaminationDto createExamination(ExaminationRequest request, Long callerUserId, boolean admin) {
        Subject subject = subjectRepository.findById(request.getSubjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Subject not found with id: " + request.getSubjectId()));
        if (!admin) {
            requireTeaching(subject, callerUserId);
        }

        Examination exam = new Examination();
        exam.setName(request.getName());
        exam.setSubject(subject);
        exam.setExamDate(request.getExamDate());
        exam.setDurationMinutes(request.getDurationMinutes());
        exam.setMaxMarks(request.getMaxMarks());
        exam.setVenue(request.getVenue());
        return toDto(examinationRepository.save(exam));
    }

    @Override
    @Transactional
    public ExaminationDto updateExamination(Long id, ExaminationRequest request, Long callerUserId, boolean admin) {
        Examination exam = findExamOrThrow(id);
        if (!admin) {
            requireTeaching(exam.getSubject(), callerUserId);
        }
        Subject subject = subjectRepository.findById(request.getSubjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Subject not found with id: " + request.getSubjectId()));
        exam.setName(request.getName());
        exam.setSubject(subject);
        exam.setExamDate(request.getExamDate());
        exam.setDurationMinutes(request.getDurationMinutes());
        exam.setMaxMarks(request.getMaxMarks());
        exam.setVenue(request.getVenue());
        return toDto(examinationRepository.save(exam));
    }

    @Override
    @Transactional
    public void deleteExamination(Long id, Long callerUserId, boolean admin) {
        Examination exam = findExamOrThrow(id);
        if (!admin) {
            requireTeaching(exam.getSubject(), callerUserId);
        }
        examinationRepository.delete(exam);
    }

    @Override
    public List<ExaminationDto> getAllExaminations(Long callerUserId, boolean admin) {
        if (admin) {
            return examinationRepository.findAll().stream()
                    .sorted((a, b) -> (a.getExamDate() == null ? LocalDateTime.MAX : a.getExamDate())
                            .compareTo(b.getExamDate() == null ? LocalDateTime.MAX : b.getExamDate()))
                    .map(this::toDto).collect(Collectors.toList());
        }
        Faculty faculty = facultyRepository.findByUserId(callerUserId)
                .orElseThrow(() -> new UnauthorizedException("No faculty profile linked to this account"));
        List<Long> subjectIds = subjectRepository.findByFacultyId(faculty.getId()).stream()
                .map(Subject::getId).collect(Collectors.toList());
        return examinationRepository.findAll().stream()
                .filter(e -> subjectIds.contains(e.getSubject().getId()))
                .sorted((a, b) -> (a.getExamDate() == null ? LocalDateTime.MAX : a.getExamDate())
                        .compareTo(b.getExamDate() == null ? LocalDateTime.MAX : b.getExamDate()))
                .map(this::toDto).collect(Collectors.toList());
    }

    @Override
    public List<ExaminationDto> getExaminationsForStudent(Long studentUserId) {
        Student student = studentRepository.findByUserId(studentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("No student profile linked to this account"));
        Set<Long> subjectIds = enrollmentRepository.findByStudentId(student.getId()).stream()
                .map(e -> e.getSubject().getId()).collect(Collectors.toSet());
        return examinationRepository.findAll().stream()
                .filter(e -> subjectIds.contains(e.getSubject().getId()))
                .sorted((a, b) -> (a.getExamDate() == null ? LocalDateTime.MAX : a.getExamDate())
                        .compareTo(b.getExamDate() == null ? LocalDateTime.MAX : b.getExamDate()))
                .map(this::toDto).collect(Collectors.toList());
    }

    @Override
    public ExaminationDto getNextExamForStudent(Long studentUserId) {
        return getExaminationsForStudent(studentUserId).stream()
                .filter(e -> e.getExamDate() != null && e.getExamDate().isAfter(LocalDateTime.now()))
                .findFirst()
                .orElse(null);
    }

    @Override
    @Transactional
    public ResultDto recordResult(ResultRequest request, Long callerUserId, boolean admin) {
        Examination exam = findExamOrThrow(request.getExaminationId());
        if (!admin) {
            requireTeaching(exam.getSubject(), callerUserId);
        }
        Student student = studentRepository.findById(request.getStudentId())
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + request.getStudentId()));

        Result result = resultRepository.findByExaminationIdAndStudentId(exam.getId(), student.getId())
                .orElseGet(Result::new);
        boolean isNew = result.getId() == null;
        result.setExamination(exam);
        result.setStudent(student);
        result.setMarksObtained(request.getMarksObtained());
        result.setRemarks(request.getRemarks());
        if (request.getGrade() != null && !request.getGrade().isBlank()) {
            result.setGrade(request.getGrade());
        } else if (request.getMarksObtained() != null) {
            result.setGrade(computeGrade(request.getMarksObtained(), exam.getMaxMarks()));
        }
        if (request.getPassed() != null) {
            result.setPassed(request.getPassed());
        } else if (request.getMarksObtained() != null) {
            result.setPassed(request.getMarksObtained() >= 0.4 * (exam.getMaxMarks() == null ? 100 : exam.getMaxMarks()));
        }
        result = resultRepository.save(result);

        if (isNew) {
            notificationService.notifyUser(
                    student.getUser().getId(),
                    "Result published — " + exam.getName(),
                    "Your result for " + exam.getName() + " (" + exam.getSubject().getName() + ") has been published.",
                    "EXAMINATION",
                    "/student/results"
            );
        }
        return toDto(result);
    }

    @Override
    @Transactional
    public ResultDto updateResult(Long resultId, ResultRequest request, Long callerUserId, boolean admin) {
        Result result = resultRepository.findById(resultId)
                .orElseThrow(() -> new ResourceNotFoundException("Result not found with id: " + resultId));
        if (!admin) {
            requireTeaching(result.getExamination().getSubject(), callerUserId);
        }
        if (request.getExaminationId() == null) {
            throw new BadRequestException("Examination is required");
        }
        Examination exam = findExamOrThrow(request.getExaminationId());
        Student student = studentRepository.findById(request.getStudentId())
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + request.getStudentId()));

        result.setExamination(exam);
        result.setStudent(student);
        result.setMarksObtained(request.getMarksObtained());
        result.setRemarks(request.getRemarks());
        if (request.getGrade() != null) result.setGrade(request.getGrade());
        if (request.getPassed() != null) result.setPassed(request.getPassed());
        return toDto(resultRepository.save(result));
    }

    @Override
    public List<ResultDto> getResultsByExamination(Long examinationId, Long callerUserId, boolean admin) {
        Examination exam = findExamOrThrow(examinationId);
        if (!admin) {
            requireTeaching(exam.getSubject(), callerUserId);
        }
        return resultRepository.findByExaminationId(exam.getId()).stream()
                .map(this::toDto).collect(Collectors.toList());
    }

    @Override
    public List<ResultDto> getResultsForStudent(Long studentUserId) {
        Student student = studentRepository.findByUserId(studentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("No student profile linked to this account"));
        return getResultsForStudentById(student.getId());
    }

    @Override
    public List<ResultDto> getResultsForStudentById(Long studentId) {
        return resultRepository.findByStudentId(studentId).stream()
                .map(this::toDto).collect(Collectors.toList());
    }

    private Examination findExamOrThrow(Long id) {
        return examinationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Examination not found with id: " + id));
    }

    private void requireTeaching(Subject subject, Long callerUserId) {
        Faculty faculty = facultyRepository.findByUserId(callerUserId)
                .orElseThrow(() -> new UnauthorizedException("No faculty profile linked to this account"));
        if (subject.getFaculty() == null || !subject.getFaculty().getId().equals(faculty.getId())) {
            throw new UnauthorizedException("You are not assigned to teach this subject");
        }
    }

    private String computeGrade(double marks, Integer maxMarks) {
        double pct = marks / (maxMarks == null || maxMarks == 0 ? 100 : maxMarks) * 100.0;
        if (pct >= 90) return "A+";
        if (pct >= 80) return "A";
        if (pct >= 70) return "B+";
        if (pct >= 60) return "B";
        if (pct >= 50) return "C";
        if (pct >= 40) return "D";
        return "F";
    }

    private ExaminationDto toDto(Examination e) {
        ExaminationDto dto = new ExaminationDto();
        dto.setId(e.getId());
        dto.setName(e.getName());
        dto.setSubjectId(e.getSubject().getId());
        dto.setSubjectName(e.getSubject().getName());
        dto.setSubjectCode(e.getSubject().getCode());
        dto.setExamDate(e.getExamDate());
        dto.setDurationMinutes(e.getDurationMinutes());
        dto.setMaxMarks(e.getMaxMarks());
        dto.setVenue(e.getVenue());
        return dto;
    }

    private ResultDto toDto(Result r) {
        ResultDto dto = new ResultDto();
        dto.setId(r.getId());
        dto.setExaminationId(r.getExamination().getId());
        dto.setExaminationName(r.getExamination().getName());
        dto.setSubjectId(r.getExamination().getSubject().getId());
        dto.setSubjectName(r.getExamination().getSubject().getName());
        dto.setStudentId(r.getStudent().getId());
        dto.setStudentName(r.getStudent().getUser().getFirstName() + " " + r.getStudent().getUser().getLastName());
        dto.setRollNumber(r.getStudent().getRollNumber());
        dto.setMarksObtained(r.getMarksObtained());
        Integer maxMarks = r.getExamination().getMaxMarks();
        dto.setMaxMarks(maxMarks);
        dto.setGrade(r.getGrade());
        dto.setPassed(r.getPassed());
        dto.setRemarks(r.getRemarks());
        if (r.getMarksObtained() != null && maxMarks != null && maxMarks > 0) {
            dto.setPercentage(Math.round(r.getMarksObtained() / maxMarks * 10000.0) / 100.0);
        }
        return dto;
    }
}