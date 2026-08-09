package com.dibstable.reservation.application.service

import com.dibstable.reservation.application.port.input.ReplicateRestaurantUseCase
import com.dibstable.reservation.application.port.output.RecordProcessedMessagePort
import com.dibstable.reservation.application.port.output.SaveRestaurantReplicaPort
import com.dibstable.reservation.domain.RestaurantReplica
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class RestaurantReplicaService(
    private val recordProcessedMessage: RecordProcessedMessagePort,
    private val saveRestaurantReplica: SaveRestaurantReplicaPort,
) : ReplicateRestaurantUseCase {

    // 중복 검출과 레플리카 저장이 한 트랜잭션이어야 한다. 나뉘면 "처리 기록은 남았는데 레플리카는
    // 안 바뀐" 상태가 만들어지고, 그 메시지는 이후 영원히 중복으로 걸러져 복구 경로가 사라진다.
    // rollbackFor가 없으면 checked 예외에서 정확히 그 상태가 커밋된다.
    @Transactional(rollbackFor = [Exception::class])
    override fun replicate(messageId: String, restaurantId: Long, name: String, address: String): Boolean {
        if (!recordProcessedMessage.recordIfAbsent(messageId)) return false

        saveRestaurantReplica.save(RestaurantReplica(restaurantId, name, address))
        return true
    }
}
