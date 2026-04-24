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
                          LocalDateTime createdAt) {
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
}
