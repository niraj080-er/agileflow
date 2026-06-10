package com.swiggy.agileflow.realtime.application;

import com.swiggy.agileflow.realtime.domain.RealtimeEvent;
import com.swiggy.agileflow.realtime.domain.RealtimeEventType;
import com.swiggy.agileflow.realtime.infrastructure.RealtimeEventRepository;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

/**
 * Foundation skeleton: persists each event to the {@code realtime_events} outbox
 * (enabling missed-event replay by id cursor) and pushes it to a STOMP topic.
 * Presence tracking and reconnection replay are layered on by later specs.
 */
@Service
public class OutboxRealtimeBroadcaster implements RealtimeBroadcaster {

    private final RealtimeEventRepository eventRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public OutboxRealtimeBroadcaster(RealtimeEventRepository eventRepository,
                                     SimpMessagingTemplate messagingTemplate) {
        this.eventRepository = eventRepository;
        this.messagingTemplate = messagingTemplate;
    }

    @Override
    public void broadcast(Long projectId, RealtimeEventType eventType, String payload) {
        RealtimeEvent event = new RealtimeEvent();
        event.setProjectId(projectId);
        event.setEventType(eventType);
        event.setPayload(payload);
        RealtimeEvent saved = eventRepository.save(event);
        messagingTemplate.convertAndSend("/topic/projects/" + projectId, saved);
    }
}
