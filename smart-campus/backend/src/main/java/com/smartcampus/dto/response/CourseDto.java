package com.smartcampus.dto.response;

public class CourseDto {
    private Long id;
    private String name;
    private String code;
    private Long departmentId;
    private String departmentName;
    private Integer durationSemesters;
    private String description;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public Long getDepartmentId() { return departmentId; }
    public void setDepartmentId(Long departmentId) { this.departmentId = departmentId; }
    public String getDepartmentName() { return departmentName; }
    public void setDepartmentName(String departmentName) { this.departmentName = departmentName; }
    public Integer getDurationSemesters() { return durationSemesters; }
    public void setDurationSemesters(Integer durationSemesters) { this.durationSemesters = durationSemesters; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
