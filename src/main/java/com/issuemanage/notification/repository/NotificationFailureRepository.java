package com.issuemanage.notification.repository;

import com.issuemanage.notification.model.NotificationFailure;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationFailureRepository extends JpaRepository<NotificationFailure, Long> {
}
