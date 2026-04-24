package com.issuemanage.ticket.service;

import com.issuemanage.audit.model.AuditActionType;
import com.issuemanage.audit.service.AuditLogService;
import com.issuemanage.auth.model.Role;
import com.issuemanage.auth.model.User;
import com.issuemanage.auth.repository.UserRepository;
import com.issuemanage.notification.NotificationService;
import com.issuemanage.ticket.dto.AttachmentMetadataRequest;
import com.issuemanage.ticket.dto.AttachmentMetadataResponse;
import com.issuemanage.ticket.dto.CommentResponse;
import com.issuemanage.ticket.dto.CreateCommentRequest;
import com.issuemanage.ticket.model.Attachment;
import com.issuemanage.ticket.model.Ticket;
import com.issuemanage.ticket.model.TicketRemark;
import com.issuemanage.ticket.model.TicketRemarkType;
import com.issuemanage.ticket.repository.CommentRepository;
import com.issuemanage.ticket.repository.TicketRepository;
import java.util.List;
import java.util.Locale;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CommentService {

    private final TicketRepository ticketRepository;
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final AuditLogService auditLogService;

    public CommentService(TicketRepository ticketRepository,
                          CommentRepository commentRepository,
                          UserRepository userRepository,
                          NotificationService notificationService,
                          AuditLogService auditLogService) {
        this.ticketRepository = ticketRepository;
        this.commentRepository = commentRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
        this.auditLogService = auditLogService;
    }

    @Transactional
    public CommentResponse addComment(String ticketId, String actorEmail, CreateCommentRequest request) {
        Ticket ticket = findTicket(ticketId);
        User actor = findUser(actorEmail);
        validateTicketAccess(ticket, actor);

        TicketRemark comment = new TicketRemark();
        comment.setTicket(ticket);
        comment.setAuthor(actor);
        comment.setRemarkType(TicketRemarkType.COMMENT);
        comment.setMessage(request.getText().trim());

        if (request.getAttachments() != null) {
            request.getAttachments().forEach(attachmentRequest -> comment.addAttachment(toAttachment(attachmentRequest)));
        }

        TicketRemark savedComment = commentRepository.save(comment);
        auditLogService.record(ticket, actor, AuditActionType.REMARK_ADDED, "Comment added to ticket discussion.");
        notificationService.notifyMentionsIfPresent(ticket, savedComment);
        return toResponse(savedComment);
    }

    @Transactional(readOnly = true)
    public List<CommentResponse> getComments(String ticketId, String actorEmail) {
        Ticket ticket = findTicket(ticketId);
        User actor = findUser(actorEmail);
        validateTicketAccess(ticket, actor);

        return commentRepository.findByTicketAndRemarkTypeOrderByCreatedAtAsc(ticket, TicketRemarkType.COMMENT).stream()
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

    private void validateTicketAccess(Ticket ticket, User actor) {
        if (actor.getRole() == Role.ROLE_EMPLOYEE
                && !ticket.getRaisedBy().getId().equals(actor.getId())) {
            throw new AccessDeniedException("You can only comment on your own tickets");
        }
        if (actor.getRole() == Role.ROLE_SUPPORT_ENGINEER
                && (ticket.getAssignedTo() == null || !ticket.getAssignedTo().getId().equals(actor.getId()))) {
            throw new AccessDeniedException("Resolvers can only access comments on tickets assigned to them");
        }
    }

    private Attachment toAttachment(AttachmentMetadataRequest request) {
        Attachment attachment = new Attachment();
        attachment.setFileName(request.getFileName().trim());
        attachment.setStoragePath(request.getStoragePath().trim());
        attachment.setContentType(request.getContentType() == null ? null : request.getContentType().trim());
        attachment.setFileSize(request.getFileSize());
        return attachment;
    }

    private CommentResponse toResponse(TicketRemark comment) {
        return new CommentResponse(
                comment.getId(),
                comment.getTicket().getTicketId(),
                comment.getAuthor() == null ? null : comment.getAuthor().getId(),
                comment.getAuthor() == null ? "SYSTEM" : comment.getAuthor().getUsername(),
                comment.getAuthor() == null ? null : comment.getAuthor().getEmail(),
                comment.getMessage(),
                comment.getCreatedAt(),
                comment.getAttachments().stream()
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
