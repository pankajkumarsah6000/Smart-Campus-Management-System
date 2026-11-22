package com.smartcampus.service;

import com.smartcampus.dto.request.SubjectRequest;
import com.smartcampus.dto.response.SubjectDto;

import java.util.List;

public interface SubjectService {
    SubjectDto createSubject(SubjectRequest request);
    List<SubjectDto> getAllSubjects();
    SubjectDto updateSubject(Long id, SubjectRequest request);
    void deleteSubject(Long id);
}
