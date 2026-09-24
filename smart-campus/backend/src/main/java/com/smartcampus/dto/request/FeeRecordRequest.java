package com.smartcampus.dto.request;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class FeeRecordRequest {
    @NotNull(message = "Student is required")
    private Long studentId;

    private String feeType;

    @NotNull(message = "Amount is required")
    private BigDecimal amount;

    private java.time.LocalDate dueDate;

    private String semester;

    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }
    public String getFeeType() { return feeType; }
    public void setFeeType(String feeType) { this.feeType = feeType; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public java.time.LocalDate getDueDate() { return dueDate; }
    public void setDueDate(java.time.LocalDate dueDate) { this.dueDate = dueDate; }
    public String getSemester() { return semester; }
    public void setSemester(String semester) { this.semester = semester; }
}