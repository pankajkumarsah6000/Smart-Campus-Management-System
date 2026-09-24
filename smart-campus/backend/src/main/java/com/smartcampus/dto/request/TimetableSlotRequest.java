package com.smartcampus.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.DayOfWeek;
import java.time.LocalTime;

public class TimetableSlotRequest {
    @NotNull(message = "Subject is required")
    private Long subjectId;

    @NotBlank(message = "Section is required")
    private String section;

    @NotBlank(message = "Semester is required")
    private String semester;

    @NotNull(message = "Day of week is required")
    private DayOfWeek dayOfWeek;

    @NotNull(message = "Start time is required")
    private LocalTime startTime;

    @NotNull(message = "End time is required")
    private LocalTime endTime;

    private String room;

    public Long getSubjectId() { return subjectId; }
    public void setSubjectId(Long subjectId) { this.subjectId = subjectId; }
    public String getSection() { return section; }
    public void setSection(String section) { this.section = section; }
    public String getSemester() { return semester; }
    public void setSemester(String semester) { this.semester = semester; }
    public DayOfWeek getDayOfWeek() { return dayOfWeek; }
    public void setDayOfWeek(DayOfWeek dayOfWeek) { this.dayOfWeek = dayOfWeek; }
    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }
    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }
    public String getRoom() { return room; }
    public void setRoom(String room) { this.room = room; }
}