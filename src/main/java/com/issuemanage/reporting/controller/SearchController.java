package com.issuemanage.reporting.controller;

import com.issuemanage.reporting.dto.TicketSearchCriteria;
import com.issuemanage.reporting.service.TicketSearchService;
import com.issuemanage.ticket.dto.TicketResponse;
import com.issuemanage.ticket.model.IssueCategory;
import com.issuemanage.ticket.model.TicketPriority;
import com.issuemanage.ticket.model.TicketStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/search")
public class SearchController {

    private final TicketSearchService ticketSearchService;

    public SearchController(TicketSearchService ticketSearchService) {
        this.ticketSearchService = ticketSearchService;
    }

    @GetMapping("/tickets")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE', 'SUPPORT_ENGINEER', 'TEAM_LEAD')")
    public ResponseEntity<Page<TicketResponse>> searchTickets(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) IssueCategory category,
            @RequestParam(required = false) TicketStatus status,
            @RequestParam(required = false) TicketPriority priority,
            @RequestParam(required = false) String dateRange,
            @RequestParam(required = false) String assignee,
            @RequestParam(required = false) String department,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            Authentication authentication) {
        TicketSearchCriteria criteria = TicketSearchCriteria.from(
                keyword, category, status, priority, dateRange, assignee, department
        );
        return ResponseEntity.ok(ticketSearchService.searchTickets(authentication.getName(), criteria, pageable));
    }
}
