package com.issuemanage.reporting.dto;

import com.issuemanage.ticket.model.TicketStatus;

public class TicketProgressDTO {

    private String ticketId;
    private String title;
    private TicketStatus status;
    private Integer progress;
    private String assignedTo;

    public TicketProgressDTO() {
    }

    public TicketProgressDTO(String ticketId, String title, TicketStatus status, Integer progress, String assignedTo) {
        this.ticketId = ticketId;
        this.title = title;
        this.status = status;
        this.progress = progress;
        this.assignedTo = assignedTo;
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

    public TicketStatus getStatus() {
        return status;
    }

    public void setStatus(TicketStatus status) {
        this.status = status;
    }

    public Integer getProgress() {
        return progress;
    }

    public void setProgress(Integer progress) {
        this.progress = progress;
    }

    public String getAssignedTo() {
        return assignedTo;
    }

    public void setAssignedTo(String assignedTo) {
        this.assignedTo = assignedTo;
    }
}
