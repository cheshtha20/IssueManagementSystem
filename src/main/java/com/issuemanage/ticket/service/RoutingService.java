package com.issuemanage.ticket.service;

import com.issuemanage.audit.model.AuditActionType;
import com.issuemanage.audit.service.AuditLogService;
import com.issuemanage.auth.model.User;
import com.issuemanage.auth.repository.UserRepository;
import com.issuemanage.ticket.dto.RerouteTicketRequest;
import com.issuemanage.ticket.model.Ticket;
import com.issuemanage.ticket.model.TicketRemark;
import com.issuemanage.ticket.model.TicketRemarkType;
import com.issuemanage.ticket.model.TicketStatus;
import com.issuemanage.ticket.repository.TicketRemarkRepository;
import com.issuemanage.ticket.repository.TicketRepository;
import java.util.List;
import java.util.Locale;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RoutingService {

    private final TicketRepository ticketRepository;
    private final TicketRemarkRepository ticketRemarkRepository;
    private final UserRepository userRepository;
    private final AuditLogService auditLogService;

    public RoutingService(TicketRepository ticketRepository,
                          TicketRemarkRepository ticketRemarkRepository,
                          UserRepository userRepository,
                          AuditLogService auditLogService) {
        this.ticketRepository = ticketRepository;
        this.ticketRemarkRepository = ticketRemarkRepository;
        this.userRepository = userRepository;
        this.auditLogService = auditLogService;
    }

    @Transactional
    public Ticket route(Ticket ticket) {
        ticket.setAssignedTeam(null);
        ticket.setAssignedTo(null);
        ticket.setStatus(TicketStatus.PENDING_ASSIGNMENT);

        Ticket savedTicket = ticketRepository.save(ticket);
        String message = "Ticket moved to pending assignment queue for manual review.";
        logRemark(savedTicket, null, TicketRemarkType.ROUTING, message);
        auditLogService.record(savedTicket, null, AuditActionType.ROUTING_REVIEWED, message);
        return savedTicket;
    }

    @Transactional
    public Ticket reroute(String ticketId, String actorEmail, RerouteTicketRequest request) {
        Ticket ticket = ticketRepository.findByTicketId(ticketId)
                .orElseThrow(() -> new IllegalArgumentException("Ticket not found"));
        User actor = userRepository.findByEmail(actorEmail.toLowerCase(Locale.ROOT))
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        ticket.setCategory(request.getNewCategory());
        ticket.setAssignedTo(null);
        ticket.setAssignedTeam(null);
        ticket.setStatus(TicketStatus.OPEN);
        Ticket savedTicket = ticketRepository.save(ticket);
        auditLogService.record(
                savedTicket,
                actor,
                AuditActionType.TICKET_REROUTED,
                "Ticket rerouted to category " + request.getNewCategory()
                        + " with justification: " + request.getJustification().trim()
        );
        logRemark(savedTicket, actor, TicketRemarkType.REROUTE_JUSTIFICATION,
                "Reroute requested: " + request.getJustification().trim());
        return route(savedTicket);
    }

    public List<Ticket> getPendingAssignments() {
        return ticketRepository.findByStatusOrderByCreatedAtAsc(TicketStatus.PENDING_ASSIGNMENT);
    }

    private void logRemark(Ticket ticket, User author, TicketRemarkType type, String message) {
        TicketRemark remark = new TicketRemark();
        remark.setTicket(ticket);
        remark.setAuthor(author);
        remark.setRemarkType(type);
        remark.setMessage(message);
        ticketRemarkRepository.save(remark);
    }
}
