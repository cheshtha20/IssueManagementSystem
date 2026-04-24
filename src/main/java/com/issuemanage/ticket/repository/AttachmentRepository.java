package com.issuemanage.ticket.repository;

import com.issuemanage.ticket.model.Attachment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AttachmentRepository extends JpaRepository<Attachment, Long> {
}
