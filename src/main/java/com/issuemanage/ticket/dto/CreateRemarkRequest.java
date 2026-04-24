package com.issuemanage.ticket.dto;

import com.issuemanage.ticket.model.TicketRemarkType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;

public class CreateRemarkRequest {

    @NotBlank
    @Size(max = 1000)
    private String content;

    @NotNull
    private TicketRemarkType type;

    @Valid
    private List<AttachmentMetadataRequest> attachments = new ArrayList<>();

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public TicketRemarkType getType() {
        return type;
    }

    public void setType(TicketRemarkType type) {
        this.type = type;
    }

    public List<AttachmentMetadataRequest> getAttachments() {
        return attachments;
    }

    public void setAttachments(List<AttachmentMetadataRequest> attachments) {
        this.attachments = attachments;
    }
}
