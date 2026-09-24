package com.smartcampus.service.impl;

import com.smartcampus.dto.request.CourseRequest;
import com.smartcampus.dto.response.CourseDto;
import com.smartcampus.entity.Course;
import com.smartcampus.entity.Department;
import com.smartcampus.exception.DuplicateResourceException;
import com.smartcampus.exception.ResourceNotFoundException;
import com.smartcampus.repository.CourseRepository;
import com.smartcampus.repository.DepartmentRepository;
import com.smartcampus.service.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CourseServiceImpl implements CourseService {

    private final CourseRepository courseRepository;
    private final DepartmentRepository departmentRepository;

    @Override
    @Transactional
    public CourseDto createCourse(CourseRequest request) {
        if (courseRepository.findByCode(request.getCode()).isPresent()) {
            throw new DuplicateResourceException("A course with this code already exists: " + request.getCode());
        }
        Course course = new Course();
        course.setName(request.getName());
        course.setCode(request.getCode());
        course.setDurationSemesters(request.getDurationSemesters());
        course.setDescription(request.getDescription());
        applyDepartment(course, request.getDepartmentId());
        return toDto(courseRepository.save(course));
    }

    @Override
    public List<CourseDto> getAllCourses() {
        return courseRepository.findAll().stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CourseDto updateCourse(Long id, CourseRequest request) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + id));
        course.setName(request.getName());
        course.setDurationSemesters(request.getDurationSemesters());
        course.setDescription(request.getDescription());
        applyDepartment(course, request.getDepartmentId());
        return toDto(courseRepository.save(course));
    }

    @Override
    @Transactional
    public void deleteCourse(Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + id));
        courseRepository.delete(course);
    }

    private void applyDepartment(Course course, Long departmentId) {
        if (departmentId != null) {
            Department dept = departmentRepository.findById(departmentId)
                    .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + departmentId));
            course.setDepartment(dept);
        }
    }

    private CourseDto toDto(Course c) {
        CourseDto dto = new CourseDto();
        dto.setId(c.getId());
        dto.setName(c.getName());
        dto.setCode(c.getCode());
        if (c.getDepartment() != null) {
            dto.setDepartmentId(c.getDepartment().getId());
            dto.setDepartmentName(c.getDepartment().getName());
        }
        dto.setDurationSemesters(c.getDurationSemesters());
        dto.setDescription(c.getDescription());
        return dto;
    }
}
