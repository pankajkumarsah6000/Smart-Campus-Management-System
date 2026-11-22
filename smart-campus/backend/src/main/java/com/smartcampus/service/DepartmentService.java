package com.smartcampus.service;

import com.smartcampus.dto.request.DepartmentRequest;
import com.smartcampus.dto.response.DepartmentDto;

import java.util.List;

public interface DepartmentService {
    DepartmentDto createDepartment(DepartmentRequest request);
    DepartmentDto updateDepartment(Long id, DepartmentRequest request);
    List<DepartmentDto> getAllDepartments();
    DepartmentDto getDepartmentById(Long id);
    void deleteDepartment(Long id);
}
