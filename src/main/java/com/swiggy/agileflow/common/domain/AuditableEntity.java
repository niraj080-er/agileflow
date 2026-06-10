package com.swiggy.agileflow.common.domain;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import java.time.Instant;
import lombok.Getter;
import org.springframework.data.annotation.LastModifiedDate;

/**
 * Base for entities that are mutated over time and carry an {@code updated_at}
 * column in addition to {@code created_at}.
 */
@Getter
@MappedSuperclass
public abstract class AuditableEntity extends BaseEntity {

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
