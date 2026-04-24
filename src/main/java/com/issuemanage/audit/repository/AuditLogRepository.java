package com.issuemanage.audit.repository;

import com.issuemanage.audit.model.AuditLog;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    List<AuditLog> findByTicketTicketIdOrderByCreatedAtAsc(String ticketId);
}
