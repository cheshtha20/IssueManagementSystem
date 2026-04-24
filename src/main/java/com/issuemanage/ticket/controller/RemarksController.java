package com.issuemanage.ticket.controller;

import com.issuemanage.ticket.dto.CreateRemarkRequest;
import com.issuemanage.ticket.dto.TicketRemarkResponse;
import com.issuemanage.ticket.service.RemarksService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tickets/{ticketId}/remarks")
public class RemarksController {

    private final RemarksService remarksService;

    public RemarksController(RemarksService remarksService) {
        this.remarksService = remarksService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE', 'SUPPORT_ENGINEER', 'TEAM_LEAD')")
    public ResponseEntity<TicketRemarkResponse> createRemark(@PathVariable String ticketId,
                                                             @Valid @RequestBody CreateRemarkRequest request,
                                                             Authentication authentication) {
        TicketRemarkResponse response = remarksService.createRemark(ticketId, authentication.getName(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE', 'SUPPORT_ENGINEER', 'TEAM_LEAD')")
    public ResponseEntity<List<TicketRemarkResponse>> getRemarks(@PathVariable String ticketId,
                                                                 Authentication authentication) {
        return ResponseEntity.ok(remarksService.getRemarks(ticketId, authentication.getName()));
    }
}
