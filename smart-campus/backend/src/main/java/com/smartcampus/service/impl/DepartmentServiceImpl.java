package com.smartcampus.service.impl;

import com.smartcampus.dto.request.DepartmentRequest;
import com.smartcampus.dto.response.DepartmentDto;
import com.smartcampus.entity.Department;
import com.smartcampus.entity.Faculty;
import com.smartcampus.exception.DuplicateResourceException;
import com.smartcampus.exception.ResourceNotFoundException;
import com.smartcampus.repository.DepartmentRepository;
import com.smartcampus.repository.FacultyRepository;
import com.smartcampus.repository.StudentRepository;
import com.smartcampus.service.DepartmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final FacultyRepository facultyRepository;
    private final StudentRepository studentRepository;

    @Override
    @Transactional
    public DepartmentDto createDepartment(DepartmentRequest request) {
        if (departmentRepository.findByCode(request.getCode()).isPresent()) {
            throw new DuplicateResourceException("A department with this code already exists: " + request.getCode());
        }
        Department dept = new Department();
        dept.setName(request.getName());
        dept.setCode(request.getCode());
        dept.setDescription(request.getDescription());
        applyHod(dept, request.getHeadOfDepartmentId());
        return toDto(departmentRepository.save(dept));
    }

    @Override
    @Transactional
    public DepartmentDto updateDepartment(Long id, DepartmentRequest request) {
        Department dept = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + id));
        dept.setName(request.getName());
        dept.setDescription(request.getDescription());
        applyHod(dept, request.getHeadOfDepartmentId());
        return toDto(departmentRepository.save(dept));
    }

    @Override
    public List<DepartmentDto> getAllDepartments() {
        return departmentRepository.findAll().stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    public DepartmentDto getDepartmentById(Long id) {
        return toDto(departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + id)));
    }

    @Override
    @Transactional
    public void deleteDepartment(Long id) {
        Department dept = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + id));
        departmentRepository.delete(dept);
    }

    private void applyHod(Department dept, Long hodId) {
        if (hodId != null) {
            Faculty hod = facultyRepository.findById(hodId)
                    .orElseThrow(() -> new ResourceNotFoundException("Faculty not found with id: " + hodId));
            dept.setHeadOfDepartment(hod);
        }
    }

    private DepartmentDto toDto(Department d) {
        DepartmentDto dto = new DepartmentDto();
        dto.setId(d.getId());
        dto.setName(d.getName());
        dto.setCode(d.getCode());
        dto.setDescription(d.getDescription());
        if (d.getHeadOfDepartment() != null) {
            dto.setHeadOfDepartmentName(d.getHeadOfDepartment().getUser().getFirstName() + " " + d.getHeadOfDepartment().getUser().getLastName());
        }
        dto.setStudentCount(studentRepository.findByDepartmentId(d.getId()).size());
        dto.setFacultyCount(facultyRepository.findByDepartmentId(d.getId()).size());
        return dto;
    }
}
