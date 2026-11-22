package com.smartcampus.dto.response;

public class DepartmentDto {
    private Long id;
    private String name;
    private String code;
    private String description;
    private String headOfDepartmentName;
    private long studentCount;
    private long facultyCount;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getHeadOfDepartmentName() { return headOfDepartmentName; }
    public void setHeadOfDepartmentName(String headOfDepartmentName) { this.headOfDepartmentName = headOfDepartmentName; }
    public long getStudentCount() { return studentCount; }
    public void setStudentCount(long studentCount) { this.studentCount = studentCount; }
    public long getFacultyCount() { return facultyCount; }
    public void setFacultyCount(long facultyCount) { this.facultyCount = facultyCount; }
}
