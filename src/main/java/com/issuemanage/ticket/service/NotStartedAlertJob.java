package com.issuemanage.ticket.service;

import com.issuemanage.notification.NotificationService;
import com.issuemanage.ticket.model.Ticket;
import com.issuemanage.ticket.model.TicketStatus;
import com.issuemanage.ticket.repository.TicketRepository;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class NotStartedAlertJob {

    private static final Logger LOGGER = LoggerFactory.getLogger(NotStartedAlertJob.class);

    private final TicketRepository ticketRepository;
    private final NotificationService notificationService;

    @Value("${app.sla.not-started-hours:4}")
    private int notStartedHoursThreshold;

    public NotStartedAlertJob(TicketRepository ticketRepository, NotificationService notificationService) {
        this.ticketRepository = ticketRepository;
        this.notificationService = notificationService;
    }

    @Scheduled(fixedDelay = 1800000) // Runs every 30 minutes
    @Transactional
    public void checkNotStartedTickets() {
        LOGGER.info("Starting SLA check for assigned tickets not started...");
        try {
            List<Ticket> assignedTickets = ticketRepository.findByStatusOrderByCreatedAtAsc(TicketStatus.ASSIGNED);
            LocalDateTime now = LocalDateTime.now();
            int alertCount = 0;

            for (Ticket ticket : assignedTickets) {
                if (ticket.getAssignedAt() != null && (ticket.getNotStartedAlert() == null || !ticket.getNotStartedAlert())) {
                    long hoursElapsed = Duration.between(ticket.getAssignedAt(), now).toHours();
                    if (hoursElapsed >= notStartedHoursThreshold) {
                        ticket.setNotStartedAlert(true);
                        ticketRepository.save(ticket);
                        notificationService.notifyTeamLeadOfDelay(ticket);
                        alertCount++;
                        LOGGER.info("SLA alert triggered for ticket: {} (assigned at {})", ticket.getTicketId(), ticket.getAssignedAt());
                    }
                }
            }
            LOGGER.info("SLA check completed. Triggered alerts for {} tickets.", alertCount);
        } catch (Exception e) {
            LOGGER.error("Error occurred while executing NotStartedAlertJob: {}", e.getMessage(), e);
        }
    }
}
