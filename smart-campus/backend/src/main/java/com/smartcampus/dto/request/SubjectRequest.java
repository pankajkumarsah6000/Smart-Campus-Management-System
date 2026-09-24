package com.smartcampus.dto.request;

import jakarta.validation.constraints.NotBlank;

public class SubjectRequest {
    @NotBlank
    private String name;
    @NotBlank
    private String code;
    private Long courseId;
    private Long facultyId;
    private Integer credits;
    private String semester;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public Long getCourseId() { return courseId; }
    public void setCourseId(Long courseId) { this.courseId = courseId; }
    public Long getFacultyId() { return facultyId; }
    public void setFacultyId(Long facultyId) { this.facultyId = facultyId; }
    public Integer getCredits() { return credits; }
    public void setCredits(Integer credits) { this.credits = credits; }
    public String getSemester() { return semester; }
    public void setSemester(String semester) { this.semester = semester; }
}
