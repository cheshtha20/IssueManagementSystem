package com.issuemanage.ticket.repository;

import com.issuemanage.ticket.model.Ticket;
import com.issuemanage.ticket.model.TicketRemark;
import com.issuemanage.ticket.model.TicketRemarkType;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<TicketRemark, Long> {
    List<TicketRemark> findByTicketAndRemarkTypeOrderByCreatedAtAsc(Ticket ticket, TicketRemarkType remarkType);
}
