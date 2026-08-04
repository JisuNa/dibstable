package com.dibstable.restaurant.adapter.output.messaging

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

// created_at은 매핑하지 않는다 — DB의 DEFAULT CURRENT_TIMESTAMP에 맡긴다. 진단용이라 앱이 읽을 일이 없다.
@Entity
@Table(name = "outbox")
class OutboxJpaEntity(

    val messageId: String,

    val aggregateType: String,

    val aggregateId: String,

    val eventType: String,

    val payload: String,

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
)
