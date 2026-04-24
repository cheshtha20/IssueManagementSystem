package com.issuemanage.reporting.dto;

import com.issuemanage.ticket.model.IssueCategory;
import com.issuemanage.ticket.model.TicketPriority;
import com.issuemanage.ticket.model.TicketStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;

public class TicketSearchCriteria {

    private final String keyword;
    private final IssueCategory category;
    private final TicketStatus status;
    private final TicketPriority priority;
    private final LocalDateTime startDate;
    private final LocalDateTime endDate;
    private final String assignee;
    private final String department;

    private TicketSearchCriteria(String keyword,
                                 IssueCategory category,
                                 TicketStatus status,
                                 TicketPriority priority,
                                 LocalDateTime startDate,
                                 LocalDateTime endDate,
                                 String assignee,
                                 String department) {
        this.keyword = keyword;
        this.category = category;
        this.status = status;
        this.priority = priority;
        this.startDate = startDate;
        this.endDate = endDate;
        this.assignee = assignee;
        this.department = department;
    }

    public static TicketSearchCriteria from(String keyword,
                                            IssueCategory category,
                                            TicketStatus status,
                                            TicketPriority priority,
                                            String dateRange,
                                            String assignee,
                                            String department) {
        LocalDateTime startDate = null;
        LocalDateTime endDate = null;

        if (dateRange != null && !dateRange.isBlank()) {
            try {
                String[] parts = dateRange.split(",");
                if (parts.length > 2) {
                    throw new IllegalArgumentException("dateRange must be in YYYY-MM-DD or YYYY-MM-DD,YYYY-MM-DD format");
                }

                LocalDate start = LocalDate.parse(parts[0].trim());
                LocalDate end = parts.length == 2 ? LocalDate.parse(parts[1].trim()) : start;

                if (end.isBefore(start)) {
                    throw new IllegalArgumentException("dateRange end date cannot be earlier than start date");
                }

                startDate = start.atStartOfDay();
                endDate = end.atTime(LocalTime.MAX);
            } catch (DateTimeParseException ex) {
                throw new IllegalArgumentException("dateRange must use YYYY-MM-DD values");
            }
        }

        return new TicketSearchCriteria(
                normalize(keyword),
                category,
                status,
                priority,
                startDate,
                endDate,
                normalize(assignee),
                normalize(department)
        );
    }

    private static String normalize(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    public String getKeyword() {
        return keyword;
    }

    public IssueCategory getCategory() {
        return category;
    }

    public TicketStatus getStatus() {
        return status;
    }

    public TicketPriority getPriority() {
        return priority;
    }

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public LocalDateTime getEndDate() {
        return endDate;
    }

    public String getAssignee() {
        return assignee;
    }

    public String getDepartment() {
        return department;
    }
}
