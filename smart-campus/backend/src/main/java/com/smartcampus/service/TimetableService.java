package com.smartcampus.service;

import com.smartcampus.dto.request.TimetableSlotRequest;
import com.smartcampus.dto.response.TimetableSlotDto;

import java.util.List;

public interface TimetableService {
    TimetableSlotDto createSlot(TimetableSlotRequest request);
    TimetableSlotDto updateSlot(Long id, TimetableSlotRequest request);
    void deleteSlot(Long id);
    List<TimetableSlotDto> getTimetable(String section, String semester);
    List<TimetableSlotDto> getMyTimetable(Long userId);
    List<TimetableSlotDto> getTimetableForStudent(Long studentId);
    List<TimetableSlotDto> getTimetableForFaculty(Long facultyUserId);
}