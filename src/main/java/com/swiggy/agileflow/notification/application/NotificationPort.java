package com.swiggy.agileflow.notification.application;

import com.swiggy.agileflow.notification.domain.NotificationType;

/**
 * Port for generating notifications. Notification creation is kept out of
 * controllers; application services raise notifications through this port.
 * Foundation skeleton — delivery channels are added by later specs.
 */
public interface NotificationPort {

    void notify(Long recipientId, NotificationType type, Long issueId, String payload);
}
