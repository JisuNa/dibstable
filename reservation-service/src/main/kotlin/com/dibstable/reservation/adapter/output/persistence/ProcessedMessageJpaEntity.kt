package com.dibstable.reservation.adapter.output.persistence

import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table

// 리포지터리의 도메인 타입 노릇만 한다. 실제 삽입은 INSERT IGNORE 네이티브 쿼리가 하고,
// processed_at은 DB의 DEFAULT CURRENT_TIMESTAMP에 맡긴다.
@Entity
@Table(name = "processed_messages")
class ProcessedMessageJpaEntity(

    @Id
    val messageId: String,
)
