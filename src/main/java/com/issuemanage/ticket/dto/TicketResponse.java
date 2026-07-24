package com.issuemanage.ticket.dto;

import java.time.LocalDateTime;

public class TicketResponse {

    private final String ticketId;
    private final String title;
    private final String category;
    private final String priority;
    private final String department;
    private final String description;
    private final String status;
    private final String raisedBy;
    private final String raisedByEmail;
    private final String assignedTeam;
    private final String assignedTo;
    private final LocalDateTime createdAt;
    private final Integer progress;

    // Work tracking fields
    private final LocalDateTime assignedAt;
    private final LocalDateTime workStartedAt;
    private final String workStartedBy;
    private final Long waitingTimeMinutes;
    private final Long activeWorkTimeMinutes;
    private final Long totalHoldDurationMinutes;

    public TicketResponse(String ticketId,
                          String title,
                          String category,
                          String priority,
                          String department,
                          String description,
                          String status,
                          String raisedBy,
                          String raisedByEmail,
                          String assignedTeam,
                          String assignedTo,
                          LocalDateTime createdAt,
                          Integer progress,
                          LocalDateTime assignedAt,
                          LocalDateTime workStartedAt,
                          String workStartedBy,
                          Long waitingTimeMinutes,
                          Long activeWorkTimeMinutes,
                          Long totalHoldDurationMinutes) {
        this.ticketId = ticketId;
        this.title = title;
        this.category = category;
        this.priority = priority;
        this.department = department;
        this.description = description;
        this.status = status;
        this.raisedBy = raisedBy;
        this.raisedByEmail = raisedByEmail;
        this.assignedTeam = assignedTeam;
        this.assignedTo = assignedTo;
        this.createdAt = createdAt;
        this.progress = progress;
        this.assignedAt = assignedAt;
        this.workStartedAt = workStartedAt;
        this.workStartedBy = workStartedBy;
        this.waitingTimeMinutes = waitingTimeMinutes;
        this.activeWorkTimeMinutes = activeWorkTimeMinutes;
        this.totalHoldDurationMinutes = totalHoldDurationMinutes;
    }

    public String getTicketId() {
        return ticketId;
    }

    public String getTitle() {
        return title;
    }

    public String getCategory() {
        return category;
    }

    public String getPriority() {
        return priority;
    }

    public String getDepartment() {
        return department;
    }

    public String getDescription() {
        return description;
    }

    public String getStatus() {
        return status;
    }

    public String getRaisedBy() {
        return raisedBy;
    }

    public String getRaisedByEmail() {
        return raisedByEmail;
    }

    public String getAssignedTeam() {
        return assignedTeam;
    }

    public String getAssignedTo() {
        return assignedTo;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public Integer getProgress() {
        return progress;
    }

    public LocalDateTime getAssignedAt() {
        return assignedAt;
    }

    public LocalDateTime getWorkStartedAt() {
        return workStartedAt;
    }

    public String getWorkStartedBy() {
        return workStartedBy;
    }

    public Long getWaitingTimeMinutes() {
        return waitingTimeMinutes;
    }

    public Long getActiveWorkTimeMinutes() {
        return activeWorkTimeMinutes;
    }

    public Long getTotalHoldDurationMinutes() {
        return totalHoldDurationMinutes;
    }
}
