package com.smartcampus.dto.request;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

public class CreateStudentRequest {

    @NotBlank @Email
    private String email;

    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;

    private String phone;

    @NotBlank
    private String rollNumber;

    private Long departmentId;

    private String semester;

    private String section;

    private LocalDate dateOfBirth;

    private LocalDate admissionDate;

    // Optional; if omitted, a temporary password is generated and returned once.
    private String password;

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getRollNumber() { return rollNumber; }
    public void setRollNumber(String rollNumber) { this.rollNumber = rollNumber; }
    public Long getDepartmentId() { return departmentId; }
    public void setDepartmentId(Long departmentId) { this.departmentId = departmentId; }
    public String getSemester() { return semester; }
    public void setSemester(String semester) { this.semester = semester; }
    public String getSection() { return section; }
    public void setSection(String section) { this.section = section; }
    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }
    public LocalDate getAdmissionDate() { return admissionDate; }
    public void setAdmissionDate(LocalDate admissionDate) { this.admissionDate = admissionDate; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
