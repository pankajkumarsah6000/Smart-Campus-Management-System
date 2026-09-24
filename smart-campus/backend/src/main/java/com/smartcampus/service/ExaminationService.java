package com.smartcampus.service;

import com.smartcampus.dto.request.ExaminationRequest;
import com.smartcampus.dto.request.ResultRequest;
import com.smartcampus.dto.response.ExaminationDto;
import com.smartcampus.dto.response.ResultDto;

import java.util.List;

public interface ExaminationService {
    ExaminationDto createExamination(ExaminationRequest request, Long callerUserId, boolean admin);
    ExaminationDto updateExamination(Long id, ExaminationRequest request, Long callerUserId, boolean admin);
    void deleteExamination(Long id, Long callerUserId, boolean admin);
    List<ExaminationDto> getAllExaminations(Long callerUserId, boolean admin);
    List<ExaminationDto> getExaminationsForStudent(Long studentUserId);
    ExaminationDto getNextExamForStudent(Long studentUserId);
    ResultDto recordResult(ResultRequest request, Long callerUserId, boolean admin);
    ResultDto updateResult(Long resultId, ResultRequest request, Long callerUserId, boolean admin);
    List<ResultDto> getResultsByExamination(Long examinationId, Long callerUserId, boolean admin);
    List<ResultDto> getResultsForStudent(Long studentUserId);
    List<ResultDto> getResultsForStudentById(Long studentId);
}