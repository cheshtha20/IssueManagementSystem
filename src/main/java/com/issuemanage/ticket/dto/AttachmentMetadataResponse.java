package com.issuemanage.ticket.dto;

import java.time.LocalDateTime;

public class AttachmentMetadataResponse {

    private final Long id;
    private final String fileName;
    private final String storagePath;
    private final String contentType;
    private final Long fileSize;
    private final LocalDateTime createdAt;

    public AttachmentMetadataResponse(Long id,
                                      String fileName,
                                      String storagePath,
                                      String contentType,
                                      Long fileSize,
                                      LocalDateTime createdAt) {
        this.id = id;
        this.fileName = fileName;
        this.storagePath = storagePath;
        this.contentType = contentType;
        this.fileSize = fileSize;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public String getFileName() {
        return fileName;
    }

    public String getStoragePath() {
        return storagePath;
    }

    public String getContentType() {
        return contentType;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
