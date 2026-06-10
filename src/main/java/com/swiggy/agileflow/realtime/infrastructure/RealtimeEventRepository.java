package com.swiggy.agileflow.realtime.infrastructure;

import com.swiggy.agileflow.realtime.domain.RealtimeEvent;
import java.util.List;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RealtimeEventRepository extends JpaRepository<RealtimeEvent, Long> {

    /** Replay events for a project after a known cursor position (ascending id). */
    List<RealtimeEvent> findByProjectIdAndIdGreaterThanOrderByIdAsc(Long projectId, Long id, Limit limit);
}
