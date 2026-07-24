package com.issuemanage.reporting.dto;

import java.util.List;

public class DashboardSummaryDTO {

    private Integer totalTickets;
    private Integer openTickets;
    private Integer inProgressTickets;
    private Integer resolvedTickets;
    private Integer closedTickets;
    private Integer cancelledTickets;
    private Integer overallCompletionPercent;
    private List<TicketProgressDTO> ticketProgress;

    // SLA tracking aggregations
    private Integer notStartedTickets;
    private Long avgWaitingTimeMinutes;
    private Long avgActiveWorkTimeMinutes;

    public DashboardSummaryDTO() {
    }

    public DashboardSummaryDTO(Integer totalTickets,
                               Integer openTickets,
                               Integer inProgressTickets,
                               Integer resolvedTickets,
                               Integer closedTickets,
                               Integer cancelledTickets,
                               Integer overallCompletionPercent,
                               List<TicketProgressDTO> ticketProgress,
                               Integer notStartedTickets,
                               Long avgWaitingTimeMinutes,
                               Long avgActiveWorkTimeMinutes) {
        this.totalTickets = totalTickets;
        this.openTickets = openTickets;
        this.inProgressTickets = inProgressTickets;
        this.resolvedTickets = resolvedTickets;
        this.closedTickets = closedTickets;
        this.cancelledTickets = cancelledTickets;
        this.overallCompletionPercent = overallCompletionPercent;
        this.ticketProgress = ticketProgress;
        this.notStartedTickets = notStartedTickets;
        this.avgWaitingTimeMinutes = avgWaitingTimeMinutes;
        this.avgActiveWorkTimeMinutes = avgActiveWorkTimeMinutes;
    }

    public Integer getTotalTickets() {
        return totalTickets;
    }

    public void setTotalTickets(Integer totalTickets) {
        this.totalTickets = totalTickets;
    }

    public Integer getOpenTickets() {
        return openTickets;
    }

    public void setOpenTickets(Integer openTickets) {
        this.openTickets = openTickets;
    }

    public Integer getInProgressTickets() {
        return inProgressTickets;
    }

    public void setInProgressTickets(Integer inProgressTickets) {
        this.inProgressTickets = inProgressTickets;
    }

    public Integer getResolvedTickets() {
        return resolvedTickets;
    }

    public void setResolvedTickets(Integer resolvedTickets) {
        this.resolvedTickets = resolvedTickets;
    }

    public Integer getClosedTickets() {
        return closedTickets;
    }

    public void setClosedTickets(Integer closedTickets) {
        this.closedTickets = closedTickets;
    }

    public Integer getCancelledTickets() {
        return cancelledTickets;
    }

    public void setCancelledTickets(Integer cancelledTickets) {
        this.cancelledTickets = cancelledTickets;
    }

    public Integer getOverallCompletionPercent() {
        return overallCompletionPercent;
    }

    public void setOverallCompletionPercent(Integer overallCompletionPercent) {
        this.overallCompletionPercent = overallCompletionPercent;
    }

    public List<TicketProgressDTO> getTicketProgress() {
        return ticketProgress;
    }

    public void setTicketProgress(List<TicketProgressDTO> ticketProgress) {
        this.ticketProgress = ticketProgress;
    }

    public Integer getNotStartedTickets() {
        return notStartedTickets;
    }

    public void setNotStartedTickets(Integer notStartedTickets) {
        this.notStartedTickets = notStartedTickets;
    }

    public Long getAvgWaitingTimeMinutes() {
        return avgWaitingTimeMinutes;
    }

    public void setAvgWaitingTimeMinutes(Long avgWaitingTimeMinutes) {
        this.avgWaitingTimeMinutes = avgWaitingTimeMinutes;
    }

    public Long getAvgActiveWorkTimeMinutes() {
        return avgActiveWorkTimeMinutes;
    }

    public void setAvgActiveWorkTimeMinutes(Long avgActiveWorkTimeMinutes) {
        this.avgActiveWorkTimeMinutes = avgActiveWorkTimeMinutes;
    }
}
