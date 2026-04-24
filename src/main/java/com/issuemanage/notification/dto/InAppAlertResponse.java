package com.issuemanage.notification.dto;

import java.time.LocalDateTime;

public class InAppAlertResponse {

    private final Long id;
    private final String ticketId;
    private final String message;
    private final String directLink;
    private final boolean read;
    private final LocalDateTime createdAt;

    public InAppAlertResponse(Long id,
                              String ticketId,
                              String message,
                              String directLink,
                              boolean read,
                              LocalDateTime createdAt) {
        this.id = id;
        this.ticketId = ticketId;
        this.message = message;
        this.directLink = directLink;
        this.read = read;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public String getTicketId() {
        return ticketId;
    }

    public String getMessage() {
        return message;
    }

    public String getDirectLink() {
        return directLink;
    }

    public boolean isRead() {
        return read;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
