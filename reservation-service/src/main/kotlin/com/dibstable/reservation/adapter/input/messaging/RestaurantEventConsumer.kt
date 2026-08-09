package com.dibstable.reservation.adapter.input.messaging

import com.dibstable.reservation.application.port.input.ReplicateRestaurantUseCase
import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

@Component
class RestaurantEventConsumer(
    private val replicateRestaurant: ReplicateRestaurantUseCase,
    private val objectMapper: ObjectMapper,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    // 헤더는 @Header 바인딩 대신 레코드에서 직접 읽는다. 헤더 변환 설정에 기대지 않고
    // 계약(service-apis.md)에 적힌 이름을 그대로 쓴다.
    @KafkaListener(topics = ["restaurant"])
    fun consume(record: ConsumerRecord<String, String>) {
        val eventType = record.header("event-type")
        if (eventType != RESTAURANT_REGISTERED) {
            log.debug("처리 대상이 아닌 이벤트 — eventType={}", eventType)
            return
        }

        val messageId = record.header("message-id")
        val event = objectMapper.readValue(record.value(), RestaurantRegistered::class.java)

        // 오프셋 커밋은 이 메서드가 끝난 뒤(트랜잭션 밖) 일어난다. 그 사이에 죽으면 같은 메시지를
        // 다시 받지만, replicate()의 중복 검출이 부수 효과를 한 번으로 묶는다.
        if (!replicateRestaurant.replicate(messageId, event.restaurantId, event.name, event.address)) {
            log.debug("이미 처리한 메시지 — messageId={}", messageId)
        }
    }

    private fun ConsumerRecord<String, String>.header(name: String): String =
        String(
            requireNotNull(headers().lastHeader(name)) {
                "헤더 $name 가 없다 — message-id 없이는 멱등 처리가 불가능하다"
            }.value(),
        )

    companion object {
        private const val RESTAURANT_REGISTERED = "RestaurantRegistered"
    }
}
