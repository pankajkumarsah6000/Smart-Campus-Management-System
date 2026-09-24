package com.smartcampus.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;

public class FeeDto {
    private Long id;
    private Long studentId;
    private String studentName;
    private String rollNumber;
    private String feeType;
    private BigDecimal amount;
    private BigDecimal amountPaid;
    private BigDecimal balance;
    private LocalDate dueDate;
    private LocalDate paidDate;
    private String status;
    private String semester;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }
    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }
    public String getRollNumber() { return rollNumber; }
    public void setRollNumber(String rollNumber) { this.rollNumber = rollNumber; }
    public String getFeeType() { return feeType; }
    public void setFeeType(String feeType) { this.feeType = feeType; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public BigDecimal getAmountPaid() { return amountPaid; }
    public void setAmountPaid(BigDecimal amountPaid) { this.amountPaid = amountPaid; }
    public BigDecimal getBalance() { return balance; }
    public void setBalance(BigDecimal balance) { this.balance = balance; }
    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }
    public LocalDate getPaidDate() { return paidDate; }
    public void setPaidDate(LocalDate paidDate) { this.paidDate = paidDate; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getSemester() { return semester; }
    public void setSemester(String semester) { this.semester = semester; }
}