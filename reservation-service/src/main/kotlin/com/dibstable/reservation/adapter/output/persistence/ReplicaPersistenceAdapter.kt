package com.dibstable.reservation.adapter.output.persistence

import com.dibstable.reservation.application.port.output.RecordProcessedMessagePort
import com.dibstable.reservation.application.port.output.SaveRestaurantReplicaPort
import com.dibstable.reservation.domain.RestaurantReplica
import org.springframework.stereotype.Repository

// 두 포트를 한 어댑터가 구현한다. 중복 기록과 레플리카 저장은 같은 트랜잭션에서 함께 커밋돼야
// 의미가 있어서, 영속성 관심사 하나로 묶어 두는 편이 경계를 정직하게 드러낸다.
@Repository
class ReplicaPersistenceAdapter(
    private val replicas: RestaurantReplicaJpaRepository,
    private val processedMessages: ProcessedMessageJpaRepository,
) : SaveRestaurantReplicaPort, RecordProcessedMessagePort {

    override fun save(replica: RestaurantReplica) {
        replicas.save(RestaurantReplicaJpaEntity.from(replica))
    }

    override fun recordIfAbsent(messageId: String): Boolean =
        processedMessages.insertIfAbsent(messageId) == 1
}
