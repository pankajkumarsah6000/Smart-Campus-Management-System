package com.smartcampus.service.impl;

import com.smartcampus.dto.request.SubjectRequest;
import com.smartcampus.dto.response.SubjectDto;
import com.smartcampus.entity.Course;
import com.smartcampus.entity.Faculty;
import com.smartcampus.entity.Subject;
import com.smartcampus.exception.DuplicateResourceException;
import com.smartcampus.exception.ResourceNotFoundException;
import com.smartcampus.repository.CourseRepository;
import com.smartcampus.repository.FacultyRepository;
import com.smartcampus.repository.SubjectRepository;
import com.smartcampus.service.SubjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SubjectServiceImpl implements SubjectService {

    private final SubjectRepository subjectRepository;
    private final CourseRepository courseRepository;
    private final FacultyRepository facultyRepository;

    @Override
    @Transactional
    public SubjectDto createSubject(SubjectRequest request) {
        if (subjectRepository.findAll().stream().anyMatch(s -> s.getCode().equalsIgnoreCase(request.getCode()))) {
            throw new DuplicateResourceException("A subject with this code already exists: " + request.getCode());
        }
        Subject subject = new Subject();
        applyFields(subject, request);
        return toDto(subjectRepository.save(subject));
    }

    @Override
    public List<SubjectDto> getAllSubjects() {
        return subjectRepository.findAll().stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public SubjectDto updateSubject(Long id, SubjectRequest request) {
        Subject subject = subjectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subject not found with id: " + id));
        applyFields(subject, request);
        return toDto(subjectRepository.save(subject));
    }

    @Override
    @Transactional
    public void deleteSubject(Long id) {
        Subject subject = subjectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subject not found with id: " + id));
        subjectRepository.delete(subject);
    }

    private void applyFields(Subject subject, SubjectRequest request) {
        subject.setName(request.getName());
        subject.setCode(request.getCode());
        subject.setCredits(request.getCredits());
        subject.setSemester(request.getSemester());
        if (request.getCourseId() != null) {
            Course course = courseRepository.findById(request.getCourseId())
                    .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + request.getCourseId()));
            subject.setCourse(course);
        }
        if (request.getFacultyId() != null) {
            Faculty faculty = facultyRepository.findById(request.getFacultyId())
                    .orElseThrow(() -> new ResourceNotFoundException("Faculty not found with id: " + request.getFacultyId()));
            subject.setFaculty(faculty);
        }
    }

    private SubjectDto toDto(Subject s) {
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
}
