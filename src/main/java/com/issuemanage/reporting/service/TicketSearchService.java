package com.issuemanage.reporting.service;

import com.issuemanage.auth.model.User;
import com.issuemanage.auth.repository.UserRepository;
import com.issuemanage.reporting.dto.TicketSearchCriteria;
import com.issuemanage.reporting.specification.TicketSpecificationBuilder;
import com.issuemanage.ticket.dto.TicketResponse;
import com.issuemanage.ticket.model.Ticket;
import com.issuemanage.ticket.repository.TicketRepository;
import java.util.Locale;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TicketSearchService {

    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final TicketSpecificationBuilder ticketSpecificationBuilder;

    public TicketSearchService(TicketRepository ticketRepository,
                               UserRepository userRepository,
                               TicketSpecificationBuilder ticketSpecificationBuilder) {
        this.ticketRepository = ticketRepository;
        this.userRepository = userRepository;
        this.ticketSpecificationBuilder = ticketSpecificationBuilder;
    }

    @Transactional(readOnly = true)
    public Page<TicketResponse> searchTickets(String actorEmail, TicketSearchCriteria criteria, Pageable pageable) {
        User actor = findUser(actorEmail);
        return ticketRepository.findAll(ticketSpecificationBuilder.build(criteria, actor), pageable)
                .map(this::toResponse);
    }

    private User findUser(String actorEmail) {
        return userRepository.findByEmail(actorEmail.toLowerCase(Locale.ROOT))
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    private TicketResponse toResponse(Ticket ticket) {
        long waitingTime = 0L;
        if (ticket.getAssignedAt() != null) {
            java.time.LocalDateTime end = ticket.getWorkStartedAt() != null ? ticket.getWorkStartedAt() : java.time.LocalDateTime.now();
            waitingTime = java.time.Duration.between(ticket.getAssignedAt(), end).toMinutes();
        }
        
        long totalHold = ticket.getTotalHoldDuration() != null ? ticket.getTotalHoldDuration() : 0L;
        if (ticket.getHoldStartedAt() != null) {
            totalHold += java.time.Duration.between(ticket.getHoldStartedAt(), java.time.LocalDateTime.now()).toMinutes();
        }
        
        long activeWork = 0L;
        if (ticket.getWorkStartedAt() != null) {
            java.time.LocalDateTime end = ticket.getResolvedAt() != null ? ticket.getResolvedAt() : java.time.LocalDateTime.now();
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
