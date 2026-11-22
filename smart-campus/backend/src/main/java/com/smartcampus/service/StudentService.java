package com.smartcampus.service;

import com.smartcampus.dto.request.CreateStudentRequest;
import com.smartcampus.dto.request.UpdateStudentRequest;
import com.smartcampus.dto.response.StudentDashboardDto;
import com.smartcampus.dto.response.StudentDto;

import java.util.List;

public interface StudentService {
    StudentDto createStudent(CreateStudentRequest request);
    StudentDto getStudentById(Long id);
    StudentDto getStudentByUserId(Long userId);
    List<StudentDto> getAllStudents();
    List<StudentDto> searchStudents(String departmentCode, String semester, String section, String query);
    StudentDto updateStudent(Long id, UpdateStudentRequest request);
    void deleteStudent(Long id);
    StudentDashboardDto getDashboard(Long studentUserId);
}
