package com.issuemanage.ticket.model;

import com.issuemanage.auth.model.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "tickets")
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ticket_id", nullable = false, unique = true, length = 20)
    private String ticketId;

    @Column(nullable = false, length = 150)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private IssueCategory category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TicketPriority priority;

    @Column(length = 100)
    private String department;

    @Column(length = 500)
    private String description;

    @Column(name = "issue_summary", length = 500)
    private String issueSummary;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TicketStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "raised_by", nullable = false)
    private User raisedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_to")
    private User assignedTo;

    @Enumerated(EnumType.STRING)
    @Column(name = "assigned_team", length = 30)
    private SupportTeam assignedTeam;

    @Column(name = "resolution_summary", length = 1000)
    private String resolutionSummary;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "progress", nullable = false)
    private Integer progress = 0;

    @Column(name = "assigned_at")
    private LocalDateTime assignedAt;

    @Column(name = "work_started_at")
    private LocalDateTime workStartedAt;

    @Column(name = "hold_started_at")
    private LocalDateTime holdStartedAt;

    @Column(name = "total_hold_duration")
    private Long totalHoldDuration = 0L;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "work_started_by")
    private User workStartedBy;

    @Column(name = "not_started_alert")
    private Boolean notStartedAlert = false;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    @PrePersist
    public void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
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

    public IssueCategory getCategory() {
        return category;
    }

    public void setCategory(IssueCategory category) {
        this.category = category;
    }

    public TicketPriority getPriority() {
        return priority;
    }

    public void setPriority(TicketPriority priority) {
        this.priority = priority;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getIssueSummary() {
        return issueSummary;
    }

    public void setIssueSummary(String issueSummary) {
        this.issueSummary = issueSummary;
    }

    public TicketStatus getStatus() {
        return status;
    }

    public void setStatus(TicketStatus status) {
        this.status = status;
    }

    public User getRaisedBy() {
        return raisedBy;
    }

    public void setRaisedBy(User raisedBy) {
        this.raisedBy = raisedBy;
    }

    public User getAssignedTo() {
        return assignedTo;
    }

    public void setAssignedTo(User assignedTo) {
        this.assignedTo = assignedTo;
    }

    public SupportTeam getAssignedTeam() {
        return assignedTeam;
    }

    public void setAssignedTeam(SupportTeam assignedTeam) {
        this.assignedTeam = assignedTeam;
    }

    public String getResolutionSummary() {
        return resolutionSummary;
    }

    public void setResolutionSummary(String resolutionSummary) {
        this.resolutionSummary = resolutionSummary;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public Integer getProgress() {
        return progress;
    }

    public void setProgress(Integer progress) {
        this.progress = progress;
    }

    public LocalDateTime getAssignedAt() {
        return assignedAt;
    }

    public void setAssignedAt(LocalDateTime assignedAt) {
        this.assignedAt = assignedAt;
    }

    public LocalDateTime getWorkStartedAt() {
        return workStartedAt;
    }

    public void setWorkStartedAt(LocalDateTime workStartedAt) {
        this.workStartedAt = workStartedAt;
    }

    public LocalDateTime getHoldStartedAt() {
        return holdStartedAt;
    }

    public void setHoldStartedAt(LocalDateTime holdStartedAt) {
        this.holdStartedAt = holdStartedAt;
    }

    public Long getTotalHoldDuration() {
        return totalHoldDuration == null ? 0L : totalHoldDuration;
    }

    public void setTotalHoldDuration(Long totalHoldDuration) {
        this.totalHoldDuration = totalHoldDuration;
    }

    public User getWorkStartedBy() {
        return workStartedBy;
    }

    public void setWorkStartedBy(User workStartedBy) {
        this.workStartedBy = workStartedBy;
    }

    public Boolean getNotStartedAlert() {
        return notStartedAlert == null ? false : notStartedAlert;
    }

    public void setNotStartedAlert(Boolean notStartedAlert) {
        this.notStartedAlert = notStartedAlert;
    }

    public LocalDateTime getResolvedAt() {
        return resolvedAt;
    }

    public void setResolvedAt(LocalDateTime resolvedAt) {
        this.resolvedAt = resolvedAt;
    }
}
