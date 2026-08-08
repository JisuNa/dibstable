package com.dibstable.reservation.adapter.input.messaging

import com.dibstable.reservation.TestInfra
import com.dibstable.reservation.adapter.output.persistence.RestaurantReplicaJpaRepository
import io.kotest.core.extensions.ApplyExtension
import io.kotest.core.spec.style.FunSpec
import io.kotest.extensions.spring.SpringExtension
import io.kotest.matchers.shouldBe
import org.apache.kafka.clients.producer.ProducerRecord
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.data.repository.findByIdOrNull
import org.springframework.kafka.core.KafkaTemplate
import java.util.UUID

@SpringBootTest
@Import(TestInfra::class)
@ApplyExtension(SpringExtension::class)
class RestaurantEventConsumerTest(
    private val kafka: KafkaTemplate<String, String>,
    private val replicas: RestaurantReplicaJpaRepository,
) : FunSpec({

    test("RestaurantRegistered를 받으면 식당 레플리카를 저장한다") {
        kafka.publish(restaurantId = 7, name = "딥스식당 광화문점", address = "서울 종로구 세종대로 8")

        val replica = eventually { replicas.findByIdOrNull(7L) }

        replica.name shouldBe "딥스식당 광화문점"
        replica.address shouldBe "서울 종로구 세종대로 8"
    }

    test("모르는 이벤트 타입은 무시한다") {
        // 페이로드는 정상이고 event-type만 다르다. 가드를 지우면 8번 레플리카가 생겨 테스트가 깨진다.
        kafka.publish(
            restaurantId = 8,
            name = "딥스식당 서면점",
            address = "부산 진구 중앙대로 9",
            eventType = "RestaurantRenamed",
            partitionKey = SHARED_KEY,
        )
        // 같은 파티션 키라 순서가 보장된다. 뒤엣것이 처리됐다면 앞엣것은 이미 지나간 뒤다 —
        // sleep으로 "충분히 기다렸겠지"를 가정하지 않는다.
        kafka.publish(
            restaurantId = 9,
            name = "딥스식당 해운대점",
            address = "부산 해운대구 해운대로 10",
            partitionKey = SHARED_KEY,
        )

        eventually { replicas.findByIdOrNull(9L) }

        replicas.findByIdOrNull(8L) shouldBe null
    }
})

private const val SHARED_KEY = "8"

private fun KafkaTemplate<String, String>.publish(
    restaurantId: Long,
    name: String,
    address: String,
    eventType: String = "RestaurantRegistered",
    partitionKey: String = restaurantId.toString(),
) {
    val payload = """{"restaurantId":$restaurantId,"name":"$name","address":"$address"}"""
    val record = ProducerRecord("restaurant", partitionKey, payload)
    record.headers().add("message-id", UUID.randomUUID().toString().toByteArray())
    record.headers().add("event-type", eventType.toByteArray())

    send(record).get()
}

// 컨슈머는 별도 스레드라 저장이 즉시 보이지 않는다.
private fun <T : Any> eventually(block: () -> T?): T {
    repeat(30) {
        block()?.let { return it }
        Thread.sleep(500)
    }
    throw AssertionError("15초 안에 결과가 나타나지 않았다")
}
