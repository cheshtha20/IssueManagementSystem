package com.issuemanage.reporting.dto;

import com.issuemanage.ticket.model.TicketStatus;
import java.time.LocalDateTime;

public class WorkStatusDTO {

    private String ticketId;
    private String title;
    private String assignedTo; // resolver username
    private LocalDateTime assignedAt;
    private LocalDateTime workStartedAt; // null if not started
    private Long waitingTimeMinutes;
    private Long activeWorkTimeMinutes;
    private Long totalHoldDurationMinutes;
    private TicketStatus status;
    private Boolean notStartedAlert;

    public WorkStatusDTO() {
    }

    public WorkStatusDTO(String ticketId,
                         String title,
                         String assignedTo,
                         LocalDateTime assignedAt,
                         LocalDateTime workStartedAt,
                         Long waitingTimeMinutes,
                         Long activeWorkTimeMinutes,
                         Long totalHoldDurationMinutes,
                         TicketStatus status,
                         Boolean notStartedAlert) {
        this.ticketId = ticketId;
        this.title = title;
        this.assignedTo = assignedTo;
        this.assignedAt = assignedAt;
        this.workStartedAt = workStartedAt;
        this.waitingTimeMinutes = waitingTimeMinutes;
        this.activeWorkTimeMinutes = activeWorkTimeMinutes;
        this.totalHoldDurationMinutes = totalHoldDurationMinutes;
        this.status = status;
        this.notStartedAlert = notStartedAlert;
    }

    public String getTicketId() {
        return ticketId;
    }

    public void setTicketId(String ticketId) {
        this.ticketId = ticketId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAssignedTo() {
        return assignedTo;
    }

    public void setAssignedTo(String assignedTo) {
        this.assignedTo = assignedTo;
    }

    public LocalDateTime getAssignedAt() {
        return assignedAt;
    }

    public void setAssignedAt(LocalDateTime assignedAt) {
        this.assignedAt = assignedAt;
    }

    public LocalDateTime getWorkStartedAt() {
        return workStartedAt;
    }

    public void setWorkStartedAt(LocalDateTime workStartedAt) {
        this.workStartedAt = workStartedAt;
    }

    public Long getWaitingTimeMinutes() {
        return waitingTimeMinutes;
    }

    public void setWaitingTimeMinutes(Long waitingTimeMinutes) {
        this.waitingTimeMinutes = waitingTimeMinutes;
    }

    public Long getActiveWorkTimeMinutes() {
        return activeWorkTimeMinutes;
    }

    public void setActiveWorkTimeMinutes(Long activeWorkTimeMinutes) {
        this.activeWorkTimeMinutes = activeWorkTimeMinutes;
    }

    public Long getTotalHoldDurationMinutes() {
        return totalHoldDurationMinutes;
    }

    public void setTotalHoldDurationMinutes(Long totalHoldDurationMinutes) {
        this.totalHoldDurationMinutes = totalHoldDurationMinutes;
    }

    public TicketStatus getStatus() {
        return status;
    }

    public void setStatus(TicketStatus status) {
        this.status = status;
    }

    public Boolean getNotStartedAlert() {
        return notStartedAlert;
    }

    public void setNotStartedAlert(Boolean notStartedAlert) {
        this.notStartedAlert = notStartedAlert;
    }
}
