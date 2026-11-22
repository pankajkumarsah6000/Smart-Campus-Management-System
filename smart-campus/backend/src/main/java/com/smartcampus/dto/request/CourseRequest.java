package com.smartcampus.dto.request;

import jakarta.validation.constraints.NotBlank;

public class CourseRequest {
    @NotBlank
    private String name;
    @NotBlank
    private String code;
    private Long departmentId;
    private Integer durationSemesters;
    private String description;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public Long getDepartmentId() { return departmentId; }
    public void setDepartmentId(Long departmentId) { this.departmentId = departmentId; }
    public Integer getDurationSemesters() { return durationSemesters; }
    public void setDurationSemesters(Integer durationSemesters) { this.durationSemesters = durationSemesters; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
