package com.swiggy.agileflow.realtime.domain;

import com.swiggy.agileflow.common.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Persisted real-time event (outbox) used for broadcast and missed-event replay. */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "realtime_events")
public class RealtimeEvent extends BaseEntity {

    @Column(name = "project_id", nullable = false)
    private Long projectId;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false)
    private RealtimeEventType eventType;

    @Column
    private String payload;
}
