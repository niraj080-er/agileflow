package com.swiggy.agileflow.watcher.infrastructure;

import com.swiggy.agileflow.watcher.domain.WatcherSubscription;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WatcherRepository extends JpaRepository<WatcherSubscription, Long> {

    List<WatcherSubscription> findByIssueId(Long issueId);

    List<WatcherSubscription> findByUserId(Long userId);
}
