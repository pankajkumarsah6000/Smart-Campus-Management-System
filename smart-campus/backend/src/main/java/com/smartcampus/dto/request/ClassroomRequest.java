package com.smartcampus.dto.request;

import jakarta.validation.constraints.NotBlank;

public class ClassroomRequest {
    @NotBlank
    private String roomNumber;
    private String building;
    private Integer capacity;
    private String type;
    private Long departmentId;

    public String getRoomNumber() { return roomNumber; }
    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }
    public String getBuilding() { return building; }
    public void setBuilding(String building) { this.building = building; }
    public Integer getCapacity() { return capacity; }
    public void setCapacity(Integer capacity) { this.capacity = capacity; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public Long getDepartmentId() { return departmentId; }
    public void setDepartmentId(Long departmentId) { this.departmentId = departmentId; }
}
