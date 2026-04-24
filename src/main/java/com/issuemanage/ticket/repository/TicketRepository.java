package com.issuemanage.ticket.repository;

import com.issuemanage.ticket.model.Ticket;
import com.issuemanage.ticket.model.TicketStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface TicketRepository extends JpaRepository<Ticket, Long>, JpaSpecificationExecutor<Ticket> {
    Optional<Ticket> findTopByOrderByIdDesc();
    Optional<Ticket> findByTicketId(String ticketId);
    long countByAssignedToIdAndStatusIn(Long assignedToId, List<TicketStatus> statuses);
    List<Ticket> findByStatusOrderByCreatedAtAsc(TicketStatus status);
}
