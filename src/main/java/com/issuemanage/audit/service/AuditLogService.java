package com.issuemanage.audit.service;

import com.issuemanage.audit.model.AuditActionType;
import com.issuemanage.audit.model.AuditLog;
import com.issuemanage.audit.repository.AuditLogRepository;
import com.issuemanage.auth.model.User;
import com.issuemanage.ticket.model.Ticket;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public AuditLogService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional
    public void record(Ticket ticket, User actor, AuditActionType actionType, String details) {
        AuditLog log = new AuditLog();
        log.setTicket(ticket);
        log.setActor(actor);
        log.setActionType(actionType);
        log.setDetails(details);
        auditLogRepository.save(log);
    }

    @Transactional(readOnly = true)
    public List<AuditLog> getAuditTrail(String ticketId) {
        return auditLogRepository.findByTicketTicketIdOrderByCreatedAtAsc(ticketId);
    }
}
