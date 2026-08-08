package com.dibstable.restaurant.adapter.output.messaging

import org.apache.kafka.clients.producer.ProducerRecord
import org.slf4j.LoggerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit

// 아웃박스를 폴링해 Kafka로 옮긴다. id 오름차순으로 읽어 채번 순서대로 발행한다.
// 인스턴스가 여럿이면 같은 행을 중복 발행하고 순서도 무너진다 — 3장은 단일 인스턴스를 전제한다(ADR-005).
@Component
class OutboxRelay(
    private val outbox: OutboxJpaRepository,
    private val kafka: KafkaTemplate<String, String>,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    // fixedRate가 아니라 fixedDelay여야 느린 회차가 다음 회차와 겹치지 않는다. 500은 튜닝 노브다.
    @Scheduled(fixedDelay = 500)
    fun relay() {
        for (message in outbox.findTop100ByOrderByIdAsc()) {
            try {
                send(message)
                // 발행 성공(ack)을 확인한 뒤에 지운다. 뒤집으면 발행 실패 시 이벤트가 영구 유실된다.
                // 그 사이에 죽으면 재발행되지만, 그 중복은 컨슈머의 멱등 처리가 흡수한다.
                outbox.deleteById(message.id)
            } catch (e: Exception) {
                // DELETE까지 try 안에 둔다. 밖에 두면 삭제 실패가 루프를 끊어
                // 뒤에 쌓인 행이 발이 묶이고, 그 행은 매 회차 맨 앞에 다시 잡혀 영구 정체가 된다.
                log.warn("아웃박스 발행 실패 — id={}, messageId={}", message.id, message.messageId, e)
            }
        }
    }

    private fun send(message: OutboxJpaEntity) {
        val record = ProducerRecord<String, String>(
            message.aggregateType.lowercase(),
            null,
            message.aggregateId,
            message.payload,
        )
        // 헤더의 message-id는 아웃박스에 적힌 값 그대로다 — 여기서 새로 만들면 재발행 시 중복 검출이 깨진다.
        record.headers().add(MESSAGE_ID_HEADER, message.messageId.toByteArray())
        record.headers().add(EVENT_TYPE_HEADER, message.eventType.toByteArray())

        kafka.send(record).get(SEND_TIMEOUT_SECONDS, TimeUnit.SECONDS)
    }

    companion object {
        private const val SEND_TIMEOUT_SECONDS = 10L

        // service-apis.md가 정한 헤더명
        private const val MESSAGE_ID_HEADER = "message-id"
        private const val EVENT_TYPE_HEADER = "event-type"
    }
}
