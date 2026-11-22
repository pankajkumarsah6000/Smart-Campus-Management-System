package com.smartcampus.service;

import com.smartcampus.dto.request.ClassroomRequest;
import com.smartcampus.dto.response.ClassroomDto;
import java.util.List;

public interface ClassroomService {
    ClassroomDto createClassroom(ClassroomRequest request);
    List<ClassroomDto> getAllClassrooms();
    ClassroomDto updateClassroom(Long id, ClassroomRequest request);
    void deleteClassroom(Long id);
}
