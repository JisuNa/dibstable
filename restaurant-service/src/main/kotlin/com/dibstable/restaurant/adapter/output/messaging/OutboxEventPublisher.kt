package com.dibstable.restaurant.adapter.output.messaging

import com.dibstable.restaurant.application.port.output.PublishEventPort
import com.dibstable.restaurant.domain.RestaurantRegistered
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Component
import java.util.UUID

// 브로커로 보내지 않고 같은 DB에 적는다. 호출자의 트랜잭션에 참여해야 원자성이 성립한다.
// 실제 Kafka 발행은 릴레이가 별도 트랜잭션에서 한다.
@Component
class OutboxEventPublisher(
    private val outbox: OutboxJpaRepository,
    private val objectMapper: ObjectMapper,
) : PublishEventPort {

    override fun publish(event: RestaurantRegistered) {
        outbox.save(
            OutboxJpaEntity(
                messageId = UUID.randomUUID().toString(),
                aggregateType = AGGREGATE_TYPE,
                aggregateId = event.restaurantId.toString(),
                eventType = EVENT_TYPE,
                payload = objectMapper.writeValueAsString(event),
            ),
        )
    }

    companion object {
        // service-apis.md가 정한 계약값. 클래스명에서 유도하면 리팩터링이 계약을 조용히 깬다.
        private const val AGGREGATE_TYPE = "Restaurant"
        private const val EVENT_TYPE = "RestaurantRegistered"
    }
}
