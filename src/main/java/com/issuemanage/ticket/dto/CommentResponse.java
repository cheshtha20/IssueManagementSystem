package com.issuemanage.ticket.dto;

import java.time.LocalDateTime;
import java.util.List;

public class CommentResponse {

    private final Long id;
    private final String ticketId;
    private final Long userId;
    private final String username;
    private final String email;
    private final String text;
    private final LocalDateTime createdAt;
    private final List<AttachmentMetadataResponse> attachments;

    public CommentResponse(Long id,
                           String ticketId,
                           Long userId,
                           String username,
                           String email,
                           String text,
                           LocalDateTime createdAt,
                           List<AttachmentMetadataResponse> attachments) {
        this.id = id;
        this.ticketId = ticketId;
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.text = text;
        this.createdAt = createdAt;
        this.attachments = attachments;
    }

    public Long getId() {
        return id;
    }

    public String getTicketId() {
        return ticketId;
    }

    public Long getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getText() {
        return text;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public List<AttachmentMetadataResponse> getAttachments() {
        return attachments;
    }
}
