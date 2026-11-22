package com.smartcampus.service;

import com.smartcampus.dto.request.AssignmentRequest;
import com.smartcampus.dto.request.AssignmentSubmitRequest;
import com.smartcampus.dto.request.GradeSubmissionRequest;
import com.smartcampus.dto.response.AssignmentDto;
import com.smartcampus.dto.response.SubmissionDto;

import java.util.List;

public interface AssignmentService {
    AssignmentDto createAssignment(AssignmentRequest request, Long callerUserId, boolean admin);
    AssignmentDto updateAssignment(Long id, AssignmentRequest request, Long callerUserId, boolean admin);
    void deleteAssignment(Long id, Long callerUserId, boolean admin);
    List<AssignmentDto> getMyAssignments(Long callerUserId);
    List<AssignmentDto> getAllAssignments();
    AssignmentDto getAssignmentDetail(Long id, Long callerUserId, boolean admin);
    SubmissionDto submitAssignment(Long assignmentId, AssignmentSubmitRequest request, Long studentUserId);
    List<SubmissionDto> getSubmissionsForAssignment(Long assignmentId, Long callerUserId, boolean admin);
    SubmissionDto gradeSubmission(Long submissionId, GradeSubmissionRequest request, Long callerUserId, boolean admin);
    List<SubmissionDto> getMySubmissions(Long studentUserId);
}