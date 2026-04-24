package com.issuemanage.ticket.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "routing_rules")
public class RoutingRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private IssueCategory category;

    @Column(nullable = false, length = 100)
    private String keyword;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_team", nullable = false, length = 30)
    private SupportTeam targetTeam;

    @Column(name = "confidence_score", nullable = false)
    private Integer confidenceScore;

    @Column(nullable = false)
    private boolean active = true;

    public RoutingRule() {
    }

    public RoutingRule(IssueCategory category, String keyword, SupportTeam targetTeam, Integer confidenceScore, boolean active) {
        this.category = category;
        this.keyword = keyword;
        this.targetTeam = targetTeam;
        this.confidenceScore = confidenceScore;
        this.active = active;
    }

    public Long getId() {
        return id;
    }

    public IssueCategory getCategory() {
        return category;
    }

    public void setCategory(IssueCategory category) {
        this.category = category;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public SupportTeam getTargetTeam() {
        return targetTeam;
    }

    public void setTargetTeam(SupportTeam targetTeam) {
        this.targetTeam = targetTeam;
    }

    public Integer getConfidenceScore() {
        return confidenceScore;
    }

    public void setConfidenceScore(Integer confidenceScore) {
        this.confidenceScore = confidenceScore;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
