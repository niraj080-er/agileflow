package com.swiggy.agileflow.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Enables JPA auditing so {@code @CreatedDate} / {@code @LastModifiedDate}
 * on {@link com.swiggy.agileflow.common.domain.BaseEntity} are populated.
 */
@Configuration
@EnableJpaAuditing
public class JpaAuditingConfig {
}
