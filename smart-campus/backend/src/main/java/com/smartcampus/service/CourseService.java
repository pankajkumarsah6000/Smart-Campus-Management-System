package com.smartcampus.service;

import com.smartcampus.dto.request.CourseRequest;
import com.smartcampus.dto.response.CourseDto;

import java.util.List;

public interface CourseService {
    CourseDto createCourse(CourseRequest request);
    List<CourseDto> getAllCourses();
    CourseDto updateCourse(Long id, CourseRequest request);
    void deleteCourse(Long id);
}
