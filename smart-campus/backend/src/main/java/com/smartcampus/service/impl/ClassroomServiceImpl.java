package com.smartcampus.service.impl;

import com.smartcampus.dto.request.ClassroomRequest;
import com.smartcampus.dto.response.ClassroomDto;
import com.smartcampus.entity.Classroom;
import com.smartcampus.entity.Department;
import com.smartcampus.exception.DuplicateResourceException;
import com.smartcampus.exception.ResourceNotFoundException;
import com.smartcampus.repository.ClassroomRepository;
import com.smartcampus.repository.DepartmentRepository;
import com.smartcampus.service.ClassroomService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ClassroomServiceImpl implements ClassroomService {

    private final ClassroomRepository classroomRepository;
    private final DepartmentRepository departmentRepository;

    @Override
    @Transactional
    public ClassroomDto createClassroom(ClassroomRequest request) {
        if (classroomRepository.findByRoomNumber(request.getRoomNumber()).isPresent()) {
            throw new DuplicateResourceException("A classroom with this room number already exists: " + request.getRoomNumber());
        }
        Classroom classroom = new Classroom();
        applyFields(classroom, request);
        return toDto(classroomRepository.save(classroom));
    }

    @Override
    public List<ClassroomDto> getAllClassrooms() {
        return classroomRepository.findAll().stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ClassroomDto updateClassroom(Long id, ClassroomRequest request) {
        Classroom classroom = classroomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Classroom not found with id: " + id));
        applyFields(classroom, request);
        return toDto(classroomRepository.save(classroom));
    }

    @Override
    @Transactional
    public void deleteClassroom(Long id) {
        Classroom classroom = classroomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Classroom not found with id: " + id));
        classroomRepository.delete(classroom);
    }

    private void applyFields(Classroom classroom, ClassroomRequest request) {
        classroom.setRoomNumber(request.getRoomNumber());
        classroom.setBuilding(request.getBuilding());
        classroom.setCapacity(request.getCapacity());
        if (request.getType() != null) {
            classroom.setType(Classroom.RoomType.valueOf(request.getType()));
        }
        if (request.getDepartmentId() != null) {
            Department dept = departmentRepository.findById(request.getDepartmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Department not found: " + request.getDepartmentId()));
            classroom.setDepartment(dept);
        } else {
            classroom.setDepartment(null);
        }
    }

    private ClassroomDto toDto(Classroom c) {
        ClassroomDto dto = new ClassroomDto();
        dto.setId(c.getId());
        dto.setRoomNumber(c.getRoomNumber());
        dto.setBuilding(c.getBuilding());
        dto.setCapacity(c.getCapacity());
        dto.setType(c.getType().name());
        if (c.getDepartment() != null) {
            dto.setDepartmentId(c.getDepartment().getId());
            dto.setDepartmentName(c.getDepartment().getName());
        }
        return dto;
    }
}
