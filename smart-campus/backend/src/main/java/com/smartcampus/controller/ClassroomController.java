package com.smartcampus.controller;

import com.smartcampus.dto.ApiResponse;
import com.smartcampus.dto.request.ClassroomRequest;
import com.smartcampus.dto.response.ClassroomDto;
import com.smartcampus.service.ClassroomService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/classrooms")
@RequiredArgsConstructor
public class ClassroomController {

    private final ClassroomService classroomService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ClassroomDto>> create(@Valid @RequestBody ClassroomRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Classroom created", classroomService.createClassroom(request)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ClassroomDto>>> list() {
        return ResponseEntity.ok(ApiResponse.ok(classroomService.getAllClassrooms()));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ClassroomDto>> update(@PathVariable Long id, @Valid @RequestBody ClassroomRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Classroom updated", classroomService.updateClassroom(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        classroomService.deleteClassroom(id);
        return ResponseEntity.ok(ApiResponse.ok("Classroom deleted", null));
    }
}
