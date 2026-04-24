package com.issuemanage.ticket.dto;

import com.issuemanage.ticket.model.IssueCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class RerouteTicketRequest {

    @NotNull
    private IssueCategory newCategory;

    @NotBlank
    @Size(max = 1000)
    private String justification;

    public IssueCategory getNewCategory() {
        return newCategory;
    }

    public void setNewCategory(IssueCategory newCategory) {
        this.newCategory = newCategory;
    }

    public String getJustification() {
        return justification;
    }

    public void setJustification(String justification) {
        this.justification = justification;
    }
}
