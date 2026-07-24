package com.issuemanage.ticket.controller;

import com.issuemanage.ticket.dto.AssignTicketRequest;
import com.issuemanage.ticket.dto.CreateTicketRequest;
import com.issuemanage.ticket.dto.RerouteTicketRequest;
import com.issuemanage.ticket.dto.UpdateStatusRequest;
import com.issuemanage.ticket.dto.TicketResponse;
import com.issuemanage.auth.dto.UserResponse;
import com.issuemanage.ticket.service.TicketService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.issuemanage.ticket.dto.ProgressUpdateRequest;

@RestController
@RequestMapping("/api/tickets")
public class TicketController {

    private final TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @PostMapping
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<TicketResponse> createTicket(@Valid @RequestBody CreateTicketRequest request,
                                                       Authentication authentication) {
        TicketResponse response = ticketService.createTicket(authentication.getName(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{ticketId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE', 'SUPPORT_ENGINEER', 'TEAM_LEAD')")
    public ResponseEntity<TicketResponse> getTicket(@PathVariable String ticketId) {
        return ResponseEntity.ok(ticketService.getTicketByTicketId(ticketId));
    }

    @GetMapping("/pending-assignment")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEAM_LEAD', 'SUPPORT_ENGINEER')")
    public ResponseEntity<List<TicketResponse>> getPendingAssignments() {
        return ResponseEntity.ok(ticketService.getPendingAssignments());
    }

    @PostMapping("/{ticketId}/assign")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEAM_LEAD')")
    public ResponseEntity<TicketResponse> assignTicket(@PathVariable String ticketId,
                                                       @Valid @RequestBody AssignTicketRequest request,
                                                       Authentication authentication) {
        return ResponseEntity.ok(ticketService.assignTicket(ticketId, request, authentication.getName()));
    }

    @PostMapping("/{ticketId}/reroute")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPPORT_ENGINEER', 'TEAM_LEAD')")
    public ResponseEntity<TicketResponse> rerouteTicket(@PathVariable String ticketId,
                                                        @Valid @RequestBody RerouteTicketRequest request,
                                                        Authentication authentication) {
        return ResponseEntity.ok(ticketService.rerouteTicket(ticketId, authentication.getName(), request));
    }

    @PostMapping("/{ticketId}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEAM_LEAD', 'SUPPORT_ENGINEER', 'EMPLOYEE')")
    public ResponseEntity<TicketResponse> updateStatus(@PathVariable String ticketId,
                                                       @Valid @RequestBody UpdateStatusRequest request,
                                                       Authentication authentication) {
        return ResponseEntity.ok(ticketService.updateStatus(ticketId, request, authentication.getName()));
    }

    @GetMapping("/{ticketId}/available-assignees")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEAM_LEAD')")
    public ResponseEntity<List<UserResponse>> getAvailableAssignees(@PathVariable String ticketId) {
        return ResponseEntity.ok(ticketService.getAvailableAssignees(ticketId));
    }

    @PatchMapping("/{ticketId}/progress")
    @PreAuthorize("hasRole('SUPPORT_ENGINEER')")
    public ResponseEntity<TicketResponse> updateProgress(@PathVariable String ticketId,
                                                         @Valid @RequestBody ProgressUpdateRequest request,
                                                         Authentication authentication) {
        return ResponseEntity.ok(ticketService.updateProgress(ticketId, request.getProgress(), authentication.getName()));
    }
}
