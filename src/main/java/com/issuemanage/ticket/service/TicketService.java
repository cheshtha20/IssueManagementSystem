package com.issuemanage.ticket.service;

import com.issuemanage.audit.model.AuditActionType;
import com.issuemanage.audit.service.AuditLogService;
import com.issuemanage.auth.dto.UserResponse;
import com.issuemanage.auth.model.User;
import com.issuemanage.auth.model.Role;
import com.issuemanage.auth.repository.UserRepository;
import com.issuemanage.notification.NotificationService;
import com.issuemanage.ticket.dto.AssignTicketRequest;
import com.issuemanage.ticket.dto.CreateTicketRequest;
import com.issuemanage.ticket.dto.RerouteTicketRequest;
import com.issuemanage.ticket.dto.UpdateStatusRequest;
import com.issuemanage.ticket.dto.TicketResponse;
import com.issuemanage.ticket.model.Ticket;
import com.issuemanage.ticket.model.TicketRemark;
import com.issuemanage.ticket.model.TicketRemarkType;
import com.issuemanage.ticket.model.TicketStatus;
import com.issuemanage.ticket.repository.TicketRemarkRepository;
import com.issuemanage.ticket.repository.TicketRepository;
import java.time.Year;
import java.util.List;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TicketService {

    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final TicketRemarkRepository ticketRemarkRepository;
    private final RoutingService routingService;
    private final AuditLogService auditLogService;
    private final NotificationService notificationService;

    public TicketService(TicketRepository ticketRepository,
                         UserRepository userRepository,
                         TicketRemarkRepository ticketRemarkRepository,
                         RoutingService routingService,
                         AuditLogService auditLogService,
                         NotificationService notificationService) {
        this.ticketRepository = ticketRepository;
        this.userRepository = userRepository;
        this.ticketRemarkRepository = ticketRemarkRepository;
        this.routingService = routingService;
        this.auditLogService = auditLogService;
        this.notificationService = notificationService;
    }

    @Transactional
    public TicketResponse createTicket(String username, CreateTicketRequest request) {
        User raisedBy = userRepository.findByEmail(username.toLowerCase())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        Ticket ticket = new Ticket();
        ticket.setTicketId(generateTicketId());
        ticket.setTitle(request.getTitle().trim());
        ticket.setCategory(request.getCategory());
        ticket.setPriority(request.getPriority());
        ticket.setDepartment(request.getDepartment() == null ? null : request.getDepartment().trim());
        ticket.setDescription(request.getDescription() == null ? null : request.getDescription().trim());
        ticket.setIssueSummary(null);
        ticket.setStatus(TicketStatus.OPEN);
        ticket.setRaisedBy(raisedBy);
        ticket.setAssignedTo(null);
        ticket.setAssignedTeam(null);
        ticket.setResolutionSummary(null);

        Ticket savedTicket = ticketRepository.save(ticket);
        auditLogService.record(
                savedTicket,
                raisedBy,
                AuditActionType.TICKET_CREATED,
                "Ticket created with category " + savedTicket.getCategory()
                        + " and priority " + savedTicket.getPriority() + "."
        );
        Ticket routedTicket = routingService.route(savedTicket);
        return toResponse(routedTicket);
    }

    @Transactional(readOnly = true)
    public TicketResponse getTicketByTicketId(String ticketId) {
        return ticketRepository.findByTicketId(ticketId)
                .map(this::toResponse)
                .orElseThrow(() -> new IllegalArgumentException("Ticket not found"));
    }

    @Transactional
    public TicketResponse rerouteTicket(String ticketId, String actorEmail, RerouteTicketRequest request) {
        return toResponse(routingService.reroute(ticketId, actorEmail, request));
    }

    @Transactional
    public TicketResponse assignTicket(String ticketId, AssignTicketRequest request, String actorEmail) {
        Ticket ticket = ticketRepository.findByTicketId(ticketId)
                .orElseThrow(() -> new IllegalArgumentException("Ticket not found"));
        User actor = userRepository.findByEmail(actorEmail.toLowerCase())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        User assignee = userRepository.findByEmail(request.getAssigneeEmail().trim().toLowerCase())
                .orElseThrow(() -> new IllegalArgumentException("Resolver not found"));

        if (assignee.getRole() != Role.ROLE_SUPPORT_ENGINEER) {
            throw new IllegalArgumentException("Selected user is not a resolver");
        }
        if (!assignee.isEnabled() || !assignee.isEmailVerified()) {
            throw new IllegalArgumentException("Selected resolver account is not active");
        }
        if (assignee.getTeam() == null) {
            throw new IllegalArgumentException("Selected resolver is not mapped to a support team");
        }

        ticket.setAssignedTo(assignee);
        ticket.setAssignedTeam(assignee.getTeam());
        ticket.setStatus(TicketStatus.ASSIGNED);
        Ticket savedTicket = ticketRepository.save(ticket);

        TicketRemark remark = new TicketRemark();
        remark.setTicket(savedTicket);
        remark.setAuthor(actor);
        remark.setRemarkType(TicketRemarkType.MANUAL_ASSIGNMENT);
        remark.setMessage("Ticket manually assigned to " + assignee.getUsername()
                + " (" + assignee.getEmail() + ") in team " + assignee.getTeam() + ".");
        ticketRemarkRepository.save(remark);
        auditLogService.record(
                savedTicket,
                actor,
                AuditActionType.TICKET_ASSIGNED,
                "Ticket assigned to " + assignee.getUsername()
                        + " (" + assignee.getEmail() + ") in team " + assignee.getTeam() + "."
        );
        notificationService.notifyTicketAssignment(savedTicket, assignee);

        return toResponse(savedTicket);
    }

    @Transactional
    public TicketResponse updateStatus(String ticketId, UpdateStatusRequest request, String actorEmail) {
        Ticket ticket = ticketRepository.findByTicketId(ticketId)
                .orElseThrow(() -> new IllegalArgumentException("Ticket not found"));
        User actor = userRepository.findByEmail(actorEmail.toLowerCase())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        TicketStatus oldStatus = ticket.getStatus();
        TicketStatus newStatus = request.getStatus();

        if (oldStatus == newStatus) {
            return toResponse(ticket);
        }

        // --- Role-Based Permission Logic ---
        
        boolean isAdmin = actor.getRole() == Role.ROLE_ADMIN;
        boolean isTeamLead = actor.getRole() == Role.ROLE_TEAM_LEAD;
        boolean isSupport = actor.getRole() == Role.ROLE_SUPPORT_ENGINEER;
        boolean isEmployee = actor.getRole() == Role.ROLE_EMPLOYEE;
        boolean isAssignedToActor = ticket.getAssignedTo() != null && ticket.getAssignedTo().getId().equals(actor.getId());
        boolean isRaiser = ticket.getRaisedBy().getId().equals(actor.getId());

        // 1. Reopen Action (CLOSED -> OPEN)
        if (newStatus == TicketStatus.OPEN && oldStatus == TicketStatus.CLOSED) {
            if (!isAdmin && !(isEmployee && isRaiser)) {
                throw new IllegalArgumentException("Only the ticket raiser or an Admin can reopen a closed ticket.");
            }
        }
        // 2. Close Action (RESOLVED -> CLOSED)
        else if (newStatus == TicketStatus.CLOSED && oldStatus == TicketStatus.RESOLVED) {
            if (!isAdmin && !(isEmployee && isRaiser)) {
                throw new IllegalArgumentException("Only the ticket raiser or an Admin can close a resolved ticket.");
            }
        }
        // 3. Workflow Actions (OPEN/ASSIGNED -> IN_PROGRESS -> RESOLVED)
        else if (newStatus == TicketStatus.IN_PROGRESS || newStatus == TicketStatus.RESOLVED) {
            // Team Leads can override in exceptional scenarios
            if (isTeamLead) {
                // Allowed but should be noted in audit logs (handled below)
            } else if (isSupport) {
                if (!isAssignedToActor) {
                    throw new IllegalArgumentException("You can only update the status of tickets assigned to you.");
                }
            } else if (!isAdmin) {
                throw new IllegalArgumentException("You do not have permission to perform this workflow action.");
            }
        }
        // 4. Invalid Transitions
        else {
            if (!isAdmin) {
                validateStrictTransition(oldStatus, newStatus);
            }
        }

        // --- Final Execution ---
        
        ticket.setStatus(newStatus);
        
        // Clear assignment if reopening
        if (newStatus == TicketStatus.OPEN && oldStatus == TicketStatus.CLOSED) {
            ticket.setAssignedTo(null);
            ticket.setAssignedTeam(null);
        }

        Ticket savedTicket = ticketRepository.save(ticket);

        // Audit Logging & Remarks
        String auditMessage = String.format("Status changed from %s to %s.", oldStatus, newStatus);
        if (isTeamLead && !isAdmin && !isRaiser && !isAssignedToActor) {
            auditMessage += " (Team Lead Override)";
        }

        logRemark(savedTicket, actor, TicketRemarkType.STATUS_CHANGE, auditMessage);
        auditLogService.record(
                savedTicket,
                actor,
                AuditActionType.STATUS_CHANGED,
                auditMessage
        );

        notificationService.notifyStatusChange(savedTicket, oldStatus);

        return toResponse(savedTicket);
    }

    private void validateStrictTransition(TicketStatus current, TicketStatus next) {
        // Prevent skipping steps
        if (current == TicketStatus.OPEN && next == TicketStatus.RESOLVED) {
            throw new IllegalArgumentException("Ticket must be 'In Progress' before it can be 'Resolved'.");
        }
        if (current == TicketStatus.OPEN && next == TicketStatus.CLOSED) {
            throw new IllegalArgumentException("Ticket must be 'Resolved' before it can be 'Closed'.");
        }
        if (current == TicketStatus.IN_PROGRESS && next == TicketStatus.CLOSED) {
            throw new IllegalArgumentException("Ticket must be 'Resolved' before it can be 'Closed'.");
        }
    }

    private void logRemark(Ticket ticket, User author, TicketRemarkType type, String message) {
        TicketRemark remark = new TicketRemark();
        remark.setTicket(ticket);
        remark.setAuthor(author);
        remark.setRemarkType(type);
        remark.setMessage(message);
        ticketRemarkRepository.save(remark);
    }

    @Transactional(readOnly = true)
    public List<TicketResponse> getPendingAssignments() {
        return routingService.getPendingAssignments().stream()
                .map(this::toResponse)
                .toList();
    }

    private String generateTicketId() {
        long nextSequence = ticketRepository.findTopByOrderByIdDesc()
                .map(ticket -> ticket.getId() + 1)
                .orElse(1L);
        return "IMS-" + Year.now().getValue() + "-" + String.format("%05d", nextSequence);
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getAvailableAssignees(String ticketId) {
        Ticket ticket = ticketRepository.findByTicketId(ticketId)
                .orElseThrow(() -> new IllegalArgumentException("Ticket not found"));
        
        if (ticket.getAssignedTeam() == null) {
            return List.of();
        }

        return userRepository.findByRoleAndTeamAndEnabledTrue(Role.ROLE_SUPPORT_ENGINEER, ticket.getAssignedTeam())
                .stream()
                .map(user -> new UserResponse(
                        user.getId(),
                        user.getUsername(),
                        user.getEmail(),
                        user.getRole().name(),
                        user.getTeam() == null ? null : user.getTeam().name(),
                        user.isEnabled(),
                        user.isEmailVerified()
                ))
                .toList();
    }

    private TicketResponse toResponse(Ticket ticket) {
        return new TicketResponse(
                ticket.getTicketId(),
                ticket.getTitle(),
                ticket.getCategory().name(),
                ticket.getPriority().name(),
                ticket.getDepartment(),
                ticket.getDescription(),
                ticket.getStatus().name(),
                ticket.getRaisedBy().getUsername(),
                ticket.getRaisedBy().getEmail(),
                ticket.getAssignedTeam() == null ? null : ticket.getAssignedTeam().name(),
                ticket.getAssignedTo() == null ? null : ticket.getAssignedTo().getUsername(),
                ticket.getCreatedAt()
        );
    }
}
