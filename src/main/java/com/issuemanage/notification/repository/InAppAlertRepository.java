package com.issuemanage.notification.repository;

import com.issuemanage.notification.model.InAppAlert;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InAppAlertRepository extends JpaRepository<InAppAlert, Long> {
    List<InAppAlert> findByRecipientIdOrderByCreatedAtDesc(Long recipientId);
}
