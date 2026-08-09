package com.dibstable.reservation.adapter.output.persistence

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface ProcessedMessageJpaRepository : JpaRepository<ProcessedMessageJpaEntity, String> {

    // save()를 쓰면 안 된다. message_id가 수동 할당 PK라 Spring Data가 persist 대신 merge를
    // 호출하고, merge는 upsert라 중복을 넣어도 제약 위반이 나지 않는다 — 검출이 조용히 무력화된다.
    // 반환값은 삽입된 행 수다. 1이면 처음 보는 메시지, 0이면 이미 처리한 메시지.
    @Modifying
    @Query(
        value = "INSERT IGNORE INTO processed_messages (message_id) VALUES (:messageId)",
        nativeQuery = true,
    )
    fun insertIfAbsent(@Param("messageId") messageId: String): Int
}
