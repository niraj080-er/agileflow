package com.swiggy.agileflow.notification.infrastructure;

import com.swiggy.agileflow.notification.domain.Notification;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByRecipientIdOrderByIdDesc(Long recipientId);
}
