package com.issuemanage.notification;

import com.issuemanage.auth.model.User;
import com.issuemanage.auth.repository.UserRepository;
import com.issuemanage.notification.dto.InAppAlertResponse;
import com.issuemanage.notification.model.InAppAlert;
import com.issuemanage.notification.model.NotificationFailure;
import com.issuemanage.notification.repository.InAppAlertRepository;
import com.issuemanage.notification.repository.NotificationFailureRepository;
import com.issuemanage.ticket.model.Ticket;
import com.issuemanage.ticket.model.TicketRemark;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationService.class);
    private static final Pattern MENTION_PATTERN = Pattern.compile("@([A-Za-z0-9._-]+)");
    private static final int MAX_EMAIL_ATTEMPTS = 3;

    private final UserRepository userRepository;
    private final InAppAlertRepository inAppAlertRepository;
    private final NotificationFailureRepository notificationFailureRepository;
    private final JavaMailSender mailSender;

    @Value("${app.mail.from:noreply@ims.com}")
    private String fromEmail;

    @Value("${app.notifications.ticket-base-url:http://localhost:8080/tickets}")
    private String ticketBaseUrl;

    @Value("${app.notifications.retry-base-delay-ms:1000}")
    private long retryBaseDelayMs;

    public NotificationService(UserRepository userRepository,
                               InAppAlertRepository inAppAlertRepository,
                               NotificationFailureRepository notificationFailureRepository,
                               ObjectProvider<JavaMailSender> mailSenderProvider) {
        this.userRepository = userRepository;
        this.inAppAlertRepository = inAppAlertRepository;
        this.notificationFailureRepository = notificationFailureRepository;
        this.mailSender = mailSenderProvider.getIfAvailable();
    }

    @Async
    public void notifyMentionsIfPresent(Ticket ticket, TicketRemark remark) {
        Matcher matcher = MENTION_PATTERN.matcher(remark.getMessage());
        Set<String> uniqueMentions = new LinkedHashSet<>();
        while (matcher.find()) {
            uniqueMentions.add(matcher.group(1).toLowerCase(Locale.ROOT));
        }

        for (String mentionedUsername : uniqueMentions) {
            userRepository.findByUsername(mentionedUsername).ifPresent(user ->
                    LOGGER.info(
                            "Notification hook triggered for mention. ticketId={}, remarkId={}, mentionedUser={}",
                            ticket.getTicketId(),
                            remark.getId(),
                            user.getUsername()
                    ));
        }
    }

    @Async
    @Transactional
    public void notifyTicketAssignment(Ticket ticket, User resolver) {
        createInAppAlert(ticket, resolver);
        sendAssignmentEmailWithRetry(ticket, resolver);
    }

    @Transactional(readOnly = true)
    public List<InAppAlertResponse> getAlertsForUser(String actorEmail) {
        User actor = userRepository.findByEmail(actorEmail.toLowerCase(Locale.ROOT))
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        return inAppAlertRepository.findByRecipientIdOrderByCreatedAtDesc(actor.getId()).stream()
                .map(alert -> new InAppAlertResponse(
                        alert.getId(),
                        alert.getTicket() == null ? null : alert.getTicket().getTicketId(),
                        alert.getMessage(),
                        alert.getDirectLink(),
                        alert.isRead(),
                        alert.getCreatedAt()
                ))
                .toList();
    }

    private void createInAppAlert(Ticket ticket, User resolver) {
        InAppAlert alert = new InAppAlert();
        alert.setRecipient(resolver);
        alert.setTicket(ticket);
        alert.setMessage("Ticket " + ticket.getTicketId() + " has been assigned to you.");
        alert.setDirectLink(buildTicketLink(ticket));
        alert.setRead(false);
        inAppAlertRepository.save(alert);
    }

    @Async
    @Transactional
    public void notifyStatusChange(Ticket ticket, com.issuemanage.ticket.model.TicketStatus oldStatus) {
        User raiser = ticket.getRaisedBy();
        
        // In-App Alert
        InAppAlert alert = new InAppAlert();
        alert.setRecipient(raiser);
        alert.setTicket(ticket);
        alert.setMessage("Ticket " + ticket.getTicketId() + " status updated from " + oldStatus + " to " + ticket.getStatus() + ".");
        alert.setDirectLink(buildTicketLink(ticket));
        alert.setRead(false);
        inAppAlertRepository.save(alert);

        // Email Notification
        String subject = "Ticket Update: " + ticket.getTicketId() + " - " + ticket.getStatus();
        String body = """
                The status of your ticket has been updated.
                
                Ticket ID: %s
                Title: %s
                Status Change: %s -> %s
                
                Link: %s
                """.formatted(
                ticket.getTicketId(),
                ticket.getTitle(),
                oldStatus.name(),
                ticket.getStatus().name(),
                buildTicketLink(ticket)
        );

        sendEmailWithRetry(raiser.getEmail(), subject, body, "TICKET_STATUS_UPDATE", ticket.getTicketId());
    }

    private void sendAssignmentEmailWithRetry(Ticket ticket, User resolver) {
        String subject = "Ticket Assigned: " + ticket.getTicketId();
        String body = """
                A ticket has been assigned to you.
                
                Ticket ID: %s
                Title: %s
                Priority: %s
                Link: %s
                """.formatted(
                ticket.getTicketId(),
                ticket.getTitle(),
                ticket.getPriority().name(),
                buildTicketLink(ticket)
        );

        sendEmailWithRetry(resolver.getEmail(), subject, body, "TICKET_ASSIGNMENT", ticket.getTicketId());
    }

    private void sendEmailWithRetry(String toEmail, String subject, String body, String eventType, String ticketId) {
        if (mailSender == null) {
            LOGGER.info("JavaMailSender is not configured. {} email skipped for {}", eventType, toEmail);
            logFailure("EMAIL", eventType, toEmail, ticketId, 0, "JavaMailSender is not configured");
            return;
        }

        for (int attempt = 1; attempt <= MAX_EMAIL_ATTEMPTS; attempt++) {
            try {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setFrom(fromEmail);
                message.setTo(toEmail);
                message.setSubject(subject);
                message.setText(body);
                mailSender.send(message);
                LOGGER.info("{} email sent to {} for ticket {}", eventType, toEmail, ticketId);
                return;
            } catch (Exception ex) {
                LOGGER.warn("{} email attempt {} failed for {}: {}", eventType, attempt, toEmail, ex.getMessage());
                if (attempt == MAX_EMAIL_ATTEMPTS) {
                    logFailure("EMAIL", eventType, toEmail, ticketId, attempt, ex.getMessage());
                    return;
                }
                sleepBeforeRetry(attempt);
            }
        }
    }

    private String buildTicketLink(Ticket ticket) {
        return ticketBaseUrl + "/" + ticket.getTicketId();
    }

    private void sleepBeforeRetry(int attempt) {
        long delay = retryBaseDelayMs * (1L << (attempt - 1));
        try {
            Thread.sleep(delay);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Notification retry interrupted", ex);
        }
    }

    private void logFailure(String channelType,
                            String eventType,
                            String recipientEmail,
                            String ticketId,
                            int attempts,
                            String errorMessage) {
        NotificationFailure failure = new NotificationFailure();
        failure.setChannelType(channelType);
        failure.setEventType(eventType);
        failure.setRecipientEmail(recipientEmail);
        failure.setTicketId(ticketId);
        failure.setAttempts(attempts);
        failure.setErrorMessage(errorMessage == null ? "Unknown notification failure" : errorMessage);
        notificationFailureRepository.save(failure);
    }
}
