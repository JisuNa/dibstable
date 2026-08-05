package com.dibstable.restaurant.adapter.output.messaging

import com.dibstable.restaurant.TestInfra
import com.dibstable.restaurant.application.port.input.RegisterRestaurantUseCase
import com.jayway.jsonpath.JsonPath
import io.kotest.core.extensions.ApplyExtension
import io.kotest.core.spec.style.FunSpec
import io.kotest.extensions.spring.SpringExtension
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.matchers.shouldBe
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringDeserializer
import org.springframework.boot.autoconfigure.kafka.KafkaConnectionDetails
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.Primary
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.core.ProducerFactory
import org.springframework.kafka.support.SendResult
import java.time.Duration
import java.util.Properties
import java.util.UUID
import java.util.concurrent.CompletableFuture

private const val POISON_KEY = "poison"

@SpringBootTest
@Import(TestInfra::class, PoisonKafkaTemplate::class)
@ApplyExtension(SpringExtension::class)
class OutboxRelayTest(
    private val registerRestaurant: RegisterRestaurantUseCase,
    private val outbox: OutboxJpaRepository,
    private val relay: OutboxRelay,
    // @ServiceConnection은 이 빈으로 주소를 알린다. spring.kafka.bootstrap-servers 프로퍼티는
    // application.yml의 localhost:9092 그대로라 KafkaProperties를 보면 엉뚱한 브로커에 붙는다.
    private val kafkaConnection: KafkaConnectionDetails,
) : FunSpec({

    test("아웃박스 행을 Kafka로 발행하고 지운다") {
        val restaurant = registerRestaurant.register("딥스식당 잠실점", "서울 송파구 올림픽로 7")
        val key = restaurant.id.toString()
        val recorded = outbox.findTop100ByOrderByIdAsc().single { it.aggregateId == key }

        relay.relay()

        outbox.findTop100ByOrderByIdAsc().any { it.aggregateId == key } shouldBe false

        val message = consume(kafkaConnection.consumerBootstrapServers, key)
        // 헤더명은 service-apis.md의 계약이라 프로덕션 상수를 빌리지 않고 리터럴로 못박는다.
        // message-id는 아웃박스에 적혀 있던 값 그대로여야 재발행돼도 컨슈머가 같은 키로 걸러낸다.
        message.header("message-id") shouldBe recorded.messageId
        message.header("event-type") shouldBe "RestaurantRegistered"
        JsonPath.read<Int>(message.value(), "$.restaurantId") shouldBe restaurant.id.toInt()
        JsonPath.read<String>(message.value(), "$.name") shouldBe "딥스식당 잠실점"
    }

    test("발행에 실패한 행은 남고 뒤의 행은 그대로 나간다") {
        // poison을 먼저 넣어 id가 작다 — 맨 앞에서 걸려도 뒤가 막히지 않아야 한다.
        val poison = outbox.save(outboxRow(POISON_KEY))
        val healthy = outbox.save(outboxRow("9001"))

        relay.relay()

        val remaining = outbox.findTop100ByOrderByIdAsc().map { it.id }
        remaining shouldContain poison.id
        remaining shouldNotContain healthy.id
    }
})

// 특정 키만 실패시킨다. 정상 행과 섞어 두고 poison 행이 뒤를 막지 않는지 본다.
@TestConfiguration(proxyBeanMethods = false)
class PoisonKafkaTemplate {

    @Bean
    @Primary
    fun poisonKafkaTemplate(
        producerFactory: ProducerFactory<String, String>,
    ): KafkaTemplate<String, String> = object : KafkaTemplate<String, String>(producerFactory) {
        override fun send(
            record: ProducerRecord<String, String>,
        ): CompletableFuture<SendResult<String, String>> =
            if (record.key() == POISON_KEY) error("발행 실패") else super.send(record)
    }
}

private fun outboxRow(aggregateId: String) = OutboxJpaEntity(
    messageId = UUID.randomUUID().toString(),
    aggregateType = "Restaurant",
    aggregateId = aggregateId,
    eventType = "RestaurantRegistered",
    payload = """{"restaurantId":0,"name":"이름","address":"주소"}""",
)

private fun ConsumerRecord<String, String>.header(name: String): String =
    String(requireNotNull(headers().lastHeader(name)) { "헤더 $name 가 없다" }.value())

private fun consume(bootstrapServers: List<String>, key: String): ConsumerRecord<String, String> {
    val props = Properties().apply {
        put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers.joinToString(","))
        put(ConsumerConfig.GROUP_ID_CONFIG, "outbox-relay-test-$key")
        put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
        put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer::class.java)
        put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer::class.java)
    }

    KafkaConsumer<String, String>(props).use { consumer ->
        consumer.subscribe(listOf("restaurant"))
        repeat(15) {
            consumer.poll(Duration.ofSeconds(1)).firstOrNull { it.key() == key }?.let { return it }
        }
    }
    throw AssertionError("토픽 restaurant에서 key=$key 메시지를 받지 못했다")
}
