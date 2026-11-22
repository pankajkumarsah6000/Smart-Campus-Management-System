package com.smartcampus.dto.request;

import jakarta.validation.constraints.NotBlank;

public class DepartmentRequest {
    @NotBlank
    private String name;
    @NotBlank
    private String code;
    private String description;
    private Long headOfDepartmentId;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Long getHeadOfDepartmentId() { return headOfDepartmentId; }
    public void setHeadOfDepartmentId(Long headOfDepartmentId) { this.headOfDepartmentId = headOfDepartmentId; }
}
