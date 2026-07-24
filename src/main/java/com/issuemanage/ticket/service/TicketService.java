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
import java.time.LocalDateTime;
import java.time.Year;
import java.util.List;
import com.issuemanage.common.exception.AccessDeniedException;
import com.issuemanage.common.exception.WorkNotStartedException;
import com.issuemanage.common.exception.InvalidProgressException;
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
        ticket.setAssignedAt(java.time.LocalDateTime.now());
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

        // Delegate to helper transitions
        if (newStatus == TicketStatus.IN_PROGRESS) {
            if (oldStatus == TicketStatus.ASSIGNED) {
                return startTicket(ticketId, actor);
            } else if (oldStatus == TicketStatus.ON_HOLD) {
                return resumeTicket(ticketId, actor);
            }
        } else if (newStatus == TicketStatus.ON_HOLD) {
            return holdTicket(ticketId, actor);
        } else if (newStatus == TicketStatus.RESOLVED) {
            return resolveTicket(ticketId, actor);
        } else if (newStatus == TicketStatus.REOPENED) {
            return reopenTicket(ticketId, actor);
        } else if (newStatus == TicketStatus.OPEN && oldStatus == TicketStatus.CLOSED) {
            return reopenTicket(ticketId, actor);
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

    @Transactional
    public TicketResponse startTicket(String ticketId, User currentUser) {
        Ticket ticket = ticketRepository.findByTicketId(ticketId)
                .orElseThrow(() -> new IllegalArgumentException("Ticket not found"));
        
        if (ticket.getStatus() != TicketStatus.ASSIGNED) {
            throw new IllegalArgumentException("Ticket must be in ASSIGNED status to start work.");
        }
        
        if (ticket.getAssignedTo() == null || !ticket.getAssignedTo().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Only the assigned resolver can start work on this ticket.");
        }
        
        TicketStatus oldStatus = ticket.getStatus();
        ticket.setStatus(TicketStatus.IN_PROGRESS);
        ticket.setWorkStartedAt(LocalDateTime.now());
        ticket.setWorkStartedBy(currentUser);
        ticket.setNotStartedAlert(false);
        
        Ticket savedTicket = ticketRepository.save(ticket);
        
        String auditMessage = "Status changed from ASSIGNED to IN_PROGRESS. Work started.";
        logRemark(savedTicket, currentUser, TicketRemarkType.STATUS_CHANGE, auditMessage);
        auditLogService.record(
                savedTicket,
                currentUser,
                AuditActionType.STATUS_CHANGED,
                auditMessage
        );
        notificationService.notifyStatusChange(savedTicket, oldStatus);
        
        return toResponse(savedTicket);
    }

    @Transactional
    public TicketResponse holdTicket(String ticketId, User currentUser) {
        Ticket ticket = ticketRepository.findByTicketId(ticketId)
                .orElseThrow(() -> new IllegalArgumentException("Ticket not found"));
        
        TicketStatus oldStatus = ticket.getStatus();
        if (oldStatus != TicketStatus.IN_PROGRESS) {
            throw new IllegalArgumentException("Ticket must be in IN_PROGRESS status to be put on hold.");
        }
        
        validateWorkflowPermission(ticket, currentUser);
        
        ticket.setStatus(TicketStatus.ON_HOLD);
        ticket.setHoldStartedAt(LocalDateTime.now());
        
        Ticket savedTicket = ticketRepository.save(ticket);
        
        String auditMessage = "Status changed from IN_PROGRESS to ON_HOLD.";
        logRemark(savedTicket, currentUser, TicketRemarkType.STATUS_CHANGE, auditMessage);
        auditLogService.record(
                savedTicket,
                currentUser,
                AuditActionType.STATUS_CHANGED,
                auditMessage
        );
        notificationService.notifyStatusChange(savedTicket, oldStatus);
        
        return toResponse(savedTicket);
    }

    @Transactional
    public TicketResponse resumeTicket(String ticketId, User currentUser) {
        Ticket ticket = ticketRepository.findByTicketId(ticketId)
                .orElseThrow(() -> new IllegalArgumentException("Ticket not found"));
        
        TicketStatus oldStatus = ticket.getStatus();
        if (oldStatus != TicketStatus.ON_HOLD) {
            throw new IllegalArgumentException("Ticket must be in ON_HOLD status to resume.");
        }
        
        validateWorkflowPermission(ticket, currentUser);
        
        long holdMinutes = 0;
        if (ticket.getHoldStartedAt() != null) {
            holdMinutes = java.time.Duration.between(ticket.getHoldStartedAt(), LocalDateTime.now()).toMinutes();
        }
        ticket.setTotalHoldDuration(ticket.getTotalHoldDuration() + holdMinutes);
        ticket.setHoldStartedAt(null);
        ticket.setStatus(TicketStatus.IN_PROGRESS);
        
        Ticket savedTicket = ticketRepository.save(ticket);
        
        String auditMessage = "Status changed from ON_HOLD to IN_PROGRESS.";
        logRemark(savedTicket, currentUser, TicketRemarkType.STATUS_CHANGE, auditMessage);
        auditLogService.record(
                savedTicket,
                currentUser,
                AuditActionType.STATUS_CHANGED,
                auditMessage
        );
        notificationService.notifyStatusChange(savedTicket, oldStatus);
        
        return toResponse(savedTicket);
    }

    @Transactional
    public TicketResponse resolveTicket(String ticketId, User currentUser) {
        Ticket ticket = ticketRepository.findByTicketId(ticketId)
                .orElseThrow(() -> new IllegalArgumentException("Ticket not found"));
        
        TicketStatus oldStatus = ticket.getStatus();
        
        validateWorkflowPermission(ticket, currentUser);
        
        long holdMinutes = 0;
        if (ticket.getHoldStartedAt() != null) {
            holdMinutes = java.time.Duration.between(ticket.getHoldStartedAt(), LocalDateTime.now()).toMinutes();
            ticket.setTotalHoldDuration(ticket.getTotalHoldDuration() + holdMinutes);
            ticket.setHoldStartedAt(null);
        }
        ticket.setStatus(TicketStatus.RESOLVED);
        ticket.setProgress(100);
        ticket.setResolvedAt(LocalDateTime.now());
        
        Ticket savedTicket = ticketRepository.save(ticket);
        
        String auditMessage = "Status changed from " + oldStatus + " to RESOLVED.";
        logRemark(savedTicket, currentUser, TicketRemarkType.STATUS_CHANGE, auditMessage);
        auditLogService.record(
                savedTicket,
                currentUser,
                AuditActionType.STATUS_CHANGED,
                auditMessage
        );
        notificationService.notifyStatusChange(savedTicket, oldStatus);
        
        return toResponse(savedTicket);
    }

    @Transactional
    public TicketResponse reopenTicket(String ticketId, User currentUser) {
        Ticket ticket = ticketRepository.findByTicketId(ticketId)
                .orElseThrow(() -> new IllegalArgumentException("Ticket not found"));
        
        TicketStatus oldStatus = ticket.getStatus();
        
        boolean isAdmin = currentUser.getRole() == Role.ROLE_ADMIN;
        boolean isEmployee = currentUser.getRole() == Role.ROLE_EMPLOYEE;
        boolean isRaiser = ticket.getRaisedBy().getId().equals(currentUser.getId());
        if (!isAdmin && !(isEmployee && isRaiser)) {
            throw new IllegalArgumentException("Only the ticket raiser or an Admin can reopen the ticket.");
        }
        
        ticket.setStatus(TicketStatus.REOPENED);
        ticket.setProgress(0);
        ticket.setWorkStartedAt(null);
        ticket.setWorkStartedBy(null);
        ticket.setHoldStartedAt(null);
        ticket.setTotalHoldDuration(0L);
        ticket.setNotStartedAlert(false);
        ticket.setResolvedAt(null);
        ticket.setAssignedTo(null);
        ticket.setAssignedTeam(null);
        
        Ticket savedTicket = ticketRepository.save(ticket);
        
        String auditMessage = "Status changed from " + oldStatus + " to REOPENED.";
        logRemark(savedTicket, currentUser, TicketRemarkType.STATUS_CHANGE, auditMessage);
        auditLogService.record(
                savedTicket,
                currentUser,
                AuditActionType.STATUS_CHANGED,
                auditMessage
        );
        notificationService.notifyStatusChange(savedTicket, oldStatus);
        
        return toResponse(savedTicket);
    }

    private void validateWorkflowPermission(Ticket ticket, User currentUser) {
        boolean isAdmin = currentUser.getRole() == Role.ROLE_ADMIN;
        boolean isTeamLead = currentUser.getRole() == Role.ROLE_TEAM_LEAD;
        boolean isSupport = currentUser.getRole() == Role.ROLE_SUPPORT_ENGINEER;
        boolean isAssignedToActor = ticket.getAssignedTo() != null && ticket.getAssignedTo().getId().equals(currentUser.getId());
        
        if (isTeamLead) {
            // Allowed
        } else if (isSupport) {
            if (!isAssignedToActor) {
                throw new IllegalArgumentException("You can only update the status of tickets assigned to you.");
            }
        } else if (!isAdmin) {
            throw new IllegalArgumentException("You do not have permission to perform this workflow action.");
        }
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

    @Transactional
    public TicketResponse updateProgress(String ticketId, int progress, String actorEmail) {
        Ticket ticket = ticketRepository.findByTicketId(ticketId)
                .orElseThrow(() -> new com.issuemanage.common.exception.TicketNotFoundException("Ticket not found with ID: " + ticketId));
        User currentUser = userRepository.findByEmail(actorEmail.toLowerCase())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        if (ticket.getStatus() == TicketStatus.CLOSED || ticket.getStatus() == TicketStatus.CANCELLED) {
            throw new com.issuemanage.common.exception.InvalidOperationException("Cannot update progress of a closed or cancelled ticket.");
        }

        // Auto-repair legacy tickets
        if (ticket.getWorkStartedAt() == null) {
            if (ticket.getStatus() == TicketStatus.IN_PROGRESS || ticket.getStatus() == TicketStatus.ON_HOLD || ticket.getStatus() == TicketStatus.RESOLVED) {
                ticket.setWorkStartedAt(ticket.getAssignedAt() != null ? ticket.getAssignedAt() : ticket.getCreatedAt());
                if (ticket.getWorkStartedBy() == null) {
                    ticket.setWorkStartedBy(ticket.getAssignedTo());
                }
            } else {
                throw new WorkNotStartedException("Work has not been started on this ticket yet.");
            }
        }

        // Only the assigned resolver (SUPPORT_ENGINEER) can update the progress slider
        boolean isAllowed = false;
        if (currentUser.getRole() == Role.ROLE_SUPPORT_ENGINEER) {
            if (ticket.getAssignedTo() != null && ticket.getAssignedTo().getId().equals(currentUser.getId())) {
                isAllowed = true;
            }
        }

        if (!isAllowed) {
            throw new AccessDeniedException("You do not have permission to update progress on this ticket.");
        }

        if (progress < 0 || progress > 100) {
            throw new InvalidProgressException("Progress must be between 0 and 100.");
        }

        if (progress < ticket.getProgress()) {
            throw new InvalidProgressException("Progress cannot be moved backwards.");
        }

        ticket.setProgress(progress);
        Ticket savedTicket = ticketRepository.save(ticket);

        logRemark(savedTicket, currentUser, TicketRemarkType.STATUS_CHANGE, "Progress updated to " + progress + "%.");
        auditLogService.record(
                savedTicket,
                currentUser,
                AuditActionType.TICKET_UPDATED,
                "Progress updated to " + progress + "%."
        );

        return toResponse(savedTicket);
    }

    private TicketResponse toResponse(Ticket ticket) {
        long waitingTime = 0L;
        if (ticket.getAssignedAt() != null) {
            LocalDateTime end = ticket.getWorkStartedAt() != null ? ticket.getWorkStartedAt() : LocalDateTime.now();
            waitingTime = java.time.Duration.between(ticket.getAssignedAt(), end).toMinutes();
        }
        
        long totalHold = ticket.getTotalHoldDuration() != null ? ticket.getTotalHoldDuration() : 0L;
        if (ticket.getHoldStartedAt() != null) {
            totalHold += java.time.Duration.between(ticket.getHoldStartedAt(), LocalDateTime.now()).toMinutes();
        }
        
        long activeWork = 0L;
        if (ticket.getWorkStartedAt() != null) {
            LocalDateTime end = ticket.getResolvedAt() != null ? ticket.getResolvedAt() : LocalDateTime.now();
            long elapsed = java.time.Duration.between(ticket.getWorkStartedAt(), end).toMinutes();
            activeWork = elapsed - totalHold;
            if (activeWork < 0) {
                activeWork = 0L;
            }
        }

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
                ticket.getCreatedAt(),
                ticket.getProgress(),
                ticket.getAssignedAt(),
                ticket.getWorkStartedAt(),
                ticket.getWorkStartedBy() == null ? null : ticket.getWorkStartedBy().getUsername(),
                waitingTime,
                activeWork,
                totalHold
        );
    }
}
