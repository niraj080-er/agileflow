package com.swiggy.agileflow.realtime.application;

import com.swiggy.agileflow.realtime.domain.RealtimeEventType;

/**
 * Port for broadcasting real-time board changes to connected clients. The
 * transport (WebSocket/STOMP) is hidden behind this abstraction so it can
 * evolve independently. Foundation skeleton — full broadcasting, presence, and
 * replay behavior are delivered by later specs.
 */
public interface RealtimeBroadcaster {

    /** Persists the event to the outbox and (later) pushes it to subscribers. */
    void broadcast(Long projectId, RealtimeEventType eventType, String payload);
}
