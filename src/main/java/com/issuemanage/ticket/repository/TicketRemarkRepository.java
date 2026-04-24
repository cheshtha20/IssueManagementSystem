package com.issuemanage.ticket.repository;

import com.issuemanage.ticket.model.TicketRemark;
import com.issuemanage.ticket.model.Ticket;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TicketRemarkRepository extends JpaRepository<TicketRemark, Long> {
    List<TicketRemark> findByTicketOrderByCreatedAtAsc(Ticket ticket);
}
