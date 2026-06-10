package com.swiggy.agileflow.notification.application;

import com.swiggy.agileflow.notification.domain.Notification;
import com.swiggy.agileflow.notification.domain.NotificationType;
import com.swiggy.agileflow.notification.infrastructure.NotificationRepository;
import org.springframework.stereotype.Service;

/**
 * Foundation skeleton: persists a notification row. Later specs add the
 * mention/assignment/status-change generation logic and delivery channels.
 */
@Service
public class PersistingNotificationService implements NotificationPort {

    private final NotificationRepository notificationRepository;

    public PersistingNotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Override
    public void notify(Long recipientId, NotificationType type, Long issueId, String payload) {
        Notification notification = new Notification();
        notification.setRecipientId(recipientId);
        notification.setType(type);
        notification.setIssueId(issueId);
        notification.setPayload(payload);
        notificationRepository.save(notification);
    }
}
