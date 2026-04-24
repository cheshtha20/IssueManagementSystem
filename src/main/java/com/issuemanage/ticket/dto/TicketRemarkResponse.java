package com.issuemanage.ticket.dto;

import java.time.LocalDateTime;
import java.util.List;

public class TicketRemarkResponse {

    private final Long id;
    private final String ticketId;
    private final Long authorId;
    private final String authorUsername;
    private final String content;
    private final String type;
    private final LocalDateTime createdAt;
    private final List<AttachmentMetadataResponse> attachments;

    public TicketRemarkResponse(Long id,
                                String ticketId,
                                Long authorId,
                                String authorUsername,
                                String content,
                                String type,
                                LocalDateTime createdAt,
                                List<AttachmentMetadataResponse> attachments) {
        this.id = id;
        this.ticketId = ticketId;
        this.authorId = authorId;
        this.authorUsername = authorUsername;
        this.content = content;
        this.type = type;
        this.createdAt = createdAt;
        this.attachments = attachments;
    }

    public Long getId() {
        return id;
    }

    public String getTicketId() {
        return ticketId;
    }

    public Long getAuthorId() {
        return authorId;
    }

    public String getAuthorUsername() {
        return authorUsername;
    }

    public String getContent() {
        return content;
    }

    public String getType() {
        return type;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public List<AttachmentMetadataResponse> getAttachments() {
        return attachments;
    }
}
