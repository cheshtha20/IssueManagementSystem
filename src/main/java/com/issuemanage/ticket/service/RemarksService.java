package com.issuemanage.ticket.service;

import com.issuemanage.audit.model.AuditActionType;
import com.issuemanage.audit.service.AuditLogService;
import com.issuemanage.auth.model.Role;
import com.issuemanage.auth.model.User;
import com.issuemanage.auth.repository.UserRepository;
import com.issuemanage.notification.NotificationService;
import com.issuemanage.ticket.dto.AttachmentMetadataRequest;
import com.issuemanage.ticket.dto.AttachmentMetadataResponse;
import com.issuemanage.ticket.dto.CreateRemarkRequest;
import com.issuemanage.ticket.dto.TicketRemarkResponse;
import com.issuemanage.ticket.model.Attachment;
import com.issuemanage.ticket.model.Ticket;
import com.issuemanage.ticket.model.TicketRemark;
import com.issuemanage.ticket.model.TicketRemarkType;
import com.issuemanage.ticket.repository.TicketRemarkRepository;
import com.issuemanage.ticket.repository.TicketRepository;
import java.util.List;
import java.util.Locale;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RemarksService {

    private final TicketRepository ticketRepository;
    private final TicketRemarkRepository ticketRemarkRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final AuditLogService auditLogService;

    public RemarksService(TicketRepository ticketRepository,
                          TicketRemarkRepository ticketRemarkRepository,
                          UserRepository userRepository,
                          NotificationService notificationService,
                          AuditLogService auditLogService) {
        this.ticketRepository = ticketRepository;
        this.ticketRemarkRepository = ticketRemarkRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
        this.auditLogService = auditLogService;
    }

    @Transactional
    public TicketRemarkResponse createRemark(String ticketId, String actorEmail, CreateRemarkRequest request) {
        Ticket ticket = findTicket(ticketId);
        User actor = findUser(actorEmail);
        validateCreationAccess(ticket, actor, request.getType());

        TicketRemark remark = new TicketRemark();
        remark.setTicket(ticket);
        remark.setAuthor(actor);
        remark.setRemarkType(request.getType());
        remark.setMessage(request.getContent().trim());

        if (request.getAttachments() != null) {
            request.getAttachments().forEach(attachmentRequest -> remark.addAttachment(toAttachment(attachmentRequest)));
        }

        if (request.getType() == TicketRemarkType.RESOLUTION) {
            ticket.setResolutionSummary(request.getContent().trim());
            ticketRepository.save(ticket);
        }

        TicketRemark savedRemark = ticketRemarkRepository.save(remark);
        auditLogService.record(
                ticket,
                actor,
                request.getType() == TicketRemarkType.RESOLUTION
                        ? AuditActionType.RESOLUTION_ADDED
                        : AuditActionType.REMARK_ADDED,
                "Remark added with type " + request.getType() + "."
        );
        notificationService.notifyMentionsIfPresent(ticket, savedRemark);
        return toResponse(savedRemark);
    }

    @Transactional(readOnly = true)
    public List<TicketRemarkResponse> getRemarks(String ticketId, String actorEmail) {
        Ticket ticket = findTicket(ticketId);
        User actor = findUser(actorEmail);
        validateViewAccess(ticket, actor);

        return ticketRemarkRepository.findByTicketOrderByCreatedAtAsc(ticket).stream()
                .filter(remark -> isVisibleToUser(remark, actor))
                .map(this::toResponse)
                .toList();
    }

    private Ticket findTicket(String ticketId) {
        return ticketRepository.findByTicketId(ticketId)
                .orElseThrow(() -> new IllegalArgumentException("Ticket not found"));
    }

    private User findUser(String actorEmail) {
        return userRepository.findByEmail(actorEmail.toLowerCase(Locale.ROOT))
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    private void validateCreationAccess(Ticket ticket, User actor, TicketRemarkType type) {
        if (actor.getRole() == Role.ROLE_EMPLOYEE) {
            if (!ticket.getRaisedBy().getId().equals(actor.getId())) {
                throw new AccessDeniedException("You can only comment on your own tickets");
            }
            if (type != TicketRemarkType.COMMENT) {
                throw new AccessDeniedException("Ticket raisers can create only comment remarks");
            }
        }
        if (actor.getRole() == Role.ROLE_SUPPORT_ENGINEER
                && (ticket.getAssignedTo() == null || !ticket.getAssignedTo().getId().equals(actor.getId()))) {
            throw new AccessDeniedException("Resolvers can only add remarks to tickets assigned to them");
        }
    }

    private void validateViewAccess(Ticket ticket, User actor) {
        if (actor.getRole() == Role.ROLE_EMPLOYEE
                && !ticket.getRaisedBy().getId().equals(actor.getId())) {
            throw new AccessDeniedException("You can only view remarks on your own tickets");
        }
        if (actor.getRole() == Role.ROLE_SUPPORT_ENGINEER
                && (ticket.getAssignedTo() == null || !ticket.getAssignedTo().getId().equals(actor.getId()))) {
            throw new AccessDeniedException("Resolvers can only view remarks on tickets assigned to them");
        }
    }

    private boolean isVisibleToUser(TicketRemark remark, User actor) {
        if (actor.getRole() != Role.ROLE_EMPLOYEE) {
            return true;
        }
        return switch (remark.getRemarkType()) {
            case INTERNAL, ROUTING, MANUAL_ASSIGNMENT, REROUTE_JUSTIFICATION -> false;
            default -> true;
        };
    }

    private Attachment toAttachment(AttachmentMetadataRequest request) {
        Attachment attachment = new Attachment();
        attachment.setFileName(request.getFileName().trim());
        attachment.setStoragePath(request.getStoragePath().trim());
        attachment.setContentType(request.getContentType() == null ? null : request.getContentType().trim());
        attachment.setFileSize(request.getFileSize());
        return attachment;
    }

    private TicketRemarkResponse toResponse(TicketRemark remark) {
        return new TicketRemarkResponse(
                remark.getId(),
                remark.getTicket().getTicketId(),
                remark.getAuthor() == null ? null : remark.getAuthor().getId(),
                remark.getAuthor() == null ? "SYSTEM" : remark.getAuthor().getUsername(),
                remark.getMessage(),
                remark.getRemarkType().name(),
                remark.getCreatedAt(),
                remark.getAttachments().stream()
                        .map(attachment -> new AttachmentMetadataResponse(
                                attachment.getId(),
                                attachment.getFileName(),
                                attachment.getStoragePath(),
                                attachment.getContentType(),
                                attachment.getFileSize(),
                                attachment.getCreatedAt()
                        ))
                        .toList()
        );
    }
}
