package com.smartcampus.service;

import com.smartcampus.dto.request.CreateFacultyRequest;
import com.smartcampus.dto.response.FacultyDashboardDto;
import com.smartcampus.dto.response.FacultyDto;
import com.smartcampus.dto.response.StudentDto;
import com.smartcampus.dto.response.SubjectDto;

import java.util.List;

public interface FacultyService {
    FacultyDto createFaculty(CreateFacultyRequest request);
    FacultyDto getFacultyById(Long id);
    FacultyDto getFacultyByUserId(Long userId);
    List<FacultyDto> getAllFaculty();
    void deleteFaculty(Long id);
    FacultyDashboardDto getDashboard(Long userId);
    List<SubjectDto> getMySubjects(Long userId);
    List<StudentDto> getStudentsForSubject(Long subjectId, Long facultyUserId);
}