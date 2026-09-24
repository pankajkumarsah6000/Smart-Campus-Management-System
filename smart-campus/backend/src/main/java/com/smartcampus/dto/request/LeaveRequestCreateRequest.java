package com.smartcampus.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public class LeaveRequestCreateRequest {
    @NotNull
    private LocalDate fromDate;
    @NotNull
    private LocalDate toDate;
    @NotBlank
    private String reason;

    public LocalDate getFromDate() { return fromDate; }
    public void setFromDate(LocalDate fromDate) { this.fromDate = fromDate; }
    public LocalDate getToDate() { return toDate; }
    public void setToDate(LocalDate toDate) { this.toDate = toDate; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
