package com.issuemanage.reporting.service;

import com.issuemanage.auth.model.Role;
import com.issuemanage.auth.model.User;
import com.issuemanage.auth.repository.UserRepository;
import com.issuemanage.reporting.dto.DashboardSummaryDTO;
import com.issuemanage.reporting.dto.TicketProgressDTO;
import com.issuemanage.reporting.dto.TicketSearchCriteria;
import com.issuemanage.reporting.dto.WorkStatusDTO;
import com.issuemanage.reporting.specification.TicketSpecificationBuilder;
import com.issuemanage.ticket.model.Ticket;
import com.issuemanage.ticket.model.TicketStatus;
import com.issuemanage.ticket.repository.TicketRepository;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DashboardService {

    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final TicketSpecificationBuilder ticketSpecificationBuilder;

    public DashboardService(TicketRepository ticketRepository,
                            UserRepository userRepository,
                            TicketSpecificationBuilder ticketSpecificationBuilder) {
        this.ticketRepository = ticketRepository;
        this.userRepository = userRepository;
        this.ticketSpecificationBuilder = ticketSpecificationBuilder;
    }

    @Transactional(readOnly = true)
    public DashboardSummaryDTO getSummary(String actorEmail) {
        User actor = userRepository.findByEmail(actorEmail.toLowerCase(Locale.ROOT))
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        List<Ticket> visibleTickets = ticketRepository.findAll(ticketSpecificationBuilder.build(TicketSearchCriteria.from(null, null, null, null, null, null, null), actor));

        int total = 0;
        int open = 0;
        int inProgress = 0;
        int resolved = 0;
        int closed = 0;
        int cancelled = 0;
        
        long totalProgressSum = 0;
        int nonCancelledCount = 0;

        int notStarted = 0;
        
        long waitingSum = 0L;
        int waitingCount = 0;
        
        long activeSum = 0L;
        int activeCount = 0;

        List<TicketProgressDTO> progressList = new ArrayList<>();

        for (Ticket ticket : visibleTickets) {
            TicketStatus status = ticket.getStatus();
            if (status != TicketStatus.CANCELLED) {
                total++;
                nonCancelledCount++;
                totalProgressSum += (ticket.getProgress() != null ? ticket.getProgress() : 0);
            } else {
                cancelled++;
            }

            if (status == TicketStatus.OPEN || status == TicketStatus.REOPENED) {
                open++;
            } else if (status == TicketStatus.IN_PROGRESS) {
                inProgress++;
            } else if (status == TicketStatus.RESOLVED) {
                resolved++;
            } else if (status == TicketStatus.CLOSED) {
                closed++;
            }

            // progress DTO mapping
            progressList.add(new TicketProgressDTO(
                    ticket.getTicketId(),
                    ticket.getTitle(),
                    ticket.getStatus(),
                    ticket.getProgress() != null ? ticket.getProgress() : 0,
                    ticket.getAssignedTo() != null ? ticket.getAssignedTo().getUsername() : null
            ));

            // SLA timing aggregations
            if (status == TicketStatus.ASSIGNED) {
                if (ticket.getWorkStartedAt() == null) {
                    notStarted++;
                }
            }

            if (ticket.getAssignedAt() != null) {
                LocalDateTime waitEnd = ticket.getWorkStartedAt() != null ? ticket.getWorkStartedAt() : LocalDateTime.now();
                long waitTime = Duration.between(ticket.getAssignedAt(), waitEnd).toMinutes();
                waitingSum += waitTime;
                waitingCount++;
            }

            if (ticket.getWorkStartedAt() != null) {
                long totalHold = ticket.getTotalHoldDuration() != null ? ticket.getTotalHoldDuration() : 0L;
                if (ticket.getHoldStartedAt() != null) {
                    totalHold += Duration.between(ticket.getHoldStartedAt(), LocalDateTime.now()).toMinutes();
                }
                
                LocalDateTime workEnd = ticket.getResolvedAt() != null ? ticket.getResolvedAt() : LocalDateTime.now();
                long activeTime = Duration.between(ticket.getWorkStartedAt(), workEnd).toMinutes() - totalHold;
                if (activeTime < 0) activeTime = 0L;
                
                activeSum += activeTime;
                activeCount++;
            }
        }

        int overallCompletion = 0;
        if (nonCancelledCount > 0) {
            overallCompletion = (int) (totalProgressSum / nonCancelledCount);
        }

        long avgWaiting = waitingCount > 0 ? (waitingSum / waitingCount) : 0L;
        long avgActive = activeCount > 0 ? (activeSum / activeCount) : 0L;

        return new DashboardSummaryDTO(
                total,
                open,
                inProgress,
                resolved,
                closed,
                cancelled,
                overallCompletion,
                progressList,
                notStarted,
                avgWaiting,
                avgActive
        );
    }

    @Transactional(readOnly = true)
    public List<WorkStatusDTO> getWorkStatus(String actorEmail) {
        User actor = userRepository.findByEmail(actorEmail.toLowerCase(Locale.ROOT))
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        if (actor.getRole() != Role.ROLE_ADMIN && actor.getRole() != Role.ROLE_TEAM_LEAD) {
            throw new com.issuemanage.common.exception.AccessDeniedException("Access denied: Only Admins and Team Leads can view work status detail table.");
        }

        List<Ticket> visibleTickets = ticketRepository.findAll(ticketSpecificationBuilder.build(TicketSearchCriteria.from(null, null, null, null, null, null, null), actor));

        return visibleTickets.stream()
                .filter(t -> t.getAssignedTo() != null)
                .map(t -> {
                    long waitingTime = 0L;
                    if (t.getAssignedAt() != null) {
                        LocalDateTime end = t.getWorkStartedAt() != null ? t.getWorkStartedAt() : LocalDateTime.now();
                        waitingTime = Duration.between(t.getAssignedAt(), end).toMinutes();
                    }

                    long totalHold = t.getTotalHoldDuration() != null ? t.getTotalHoldDuration() : 0L;
                    if (t.getHoldStartedAt() != null) {
                        totalHold += Duration.between(t.getHoldStartedAt(), LocalDateTime.now()).toMinutes();
                    }

                    long activeWork = 0L;
                    if (t.getWorkStartedAt() != null) {
                        LocalDateTime end = t.getResolvedAt() != null ? t.getResolvedAt() : LocalDateTime.now();
                        long elapsed = Duration.between(t.getWorkStartedAt(), end).toMinutes();
                        activeWork = elapsed - totalHold;
                        if (activeWork < 0) activeWork = 0L;
                    }

                    return new WorkStatusDTO(
                            t.getTicketId(),
                            t.getTitle(),
                            t.getAssignedTo().getUsername(),
                            t.getAssignedAt(),
                            t.getWorkStartedAt(),
                            waitingTime,
                            activeWork,
                            totalHold,
                            t.getStatus(),
                            t.getNotStartedAlert() != null ? t.getNotStartedAlert() : false
                    );
                })
                .collect(Collectors.toList());
    }
}
