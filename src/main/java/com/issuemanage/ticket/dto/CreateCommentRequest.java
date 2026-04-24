package com.issuemanage.ticket.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;

public class CreateCommentRequest {

    @NotBlank
    @Size(max = 1000)
    private String text;

    @Valid
    private List<AttachmentMetadataRequest> attachments = new ArrayList<>();

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public List<AttachmentMetadataRequest> getAttachments() {
        return attachments;
    }

    public void setAttachments(List<AttachmentMetadataRequest> attachments) {
        this.attachments = attachments;
    }
}
