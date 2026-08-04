package com.dibstable.restaurant.adapter.output.messaging

import org.springframework.data.jpa.repository.JpaRepository

interface OutboxJpaRepository : JpaRepository<OutboxJpaEntity, Long>
