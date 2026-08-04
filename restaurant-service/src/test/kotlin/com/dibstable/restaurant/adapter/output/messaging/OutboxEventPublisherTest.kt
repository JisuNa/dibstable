package com.dibstable.restaurant.adapter.output.messaging

import com.dibstable.restaurant.TestInfra
import com.dibstable.restaurant.application.port.input.RegisterRestaurantUseCase
import com.jayway.jsonpath.JsonPath
import io.kotest.core.extensions.ApplyExtension
import io.kotest.core.spec.style.FunSpec
import io.kotest.extensions.spring.SpringExtension
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldMatch
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import

@SpringBootTest
@Import(TestInfra::class)
@ApplyExtension(SpringExtension::class)
class OutboxEventPublisherTest(
    private val registerRestaurant: RegisterRestaurantUseCase,
    private val outbox: OutboxJpaRepository,
) : FunSpec({

    test("식당을 등록하면 발행할 메시지가 아웃박스에 함께 기록된다") {
        val restaurant = registerRestaurant.register("딥스식당 역삼점", "서울 강남구 역삼로 3")

        val message = outbox.findAll().single { it.aggregateId == restaurant.id.toString() }

        message.aggregateType shouldBe "Restaurant"
        message.eventType shouldBe "RestaurantRegistered"
        message.messageId shouldMatch Regex("[0-9a-f]{8}(-[0-9a-f]{4}){3}-[0-9a-f]{12}")
        JsonPath.read<Int>(message.payload, "$.restaurantId") shouldBe restaurant.id.toInt()
        JsonPath.read<String>(message.payload, "$.name") shouldBe "딥스식당 역삼점"
        JsonPath.read<String>(message.payload, "$.address") shouldBe "서울 강남구 역삼로 3"
    }

    test("등록할 때마다 message_id가 새로 생긴다") {
        val first = registerRestaurant.register("딥스식당 삼성점", "서울 강남구 삼성로 5")
        val second = registerRestaurant.register("딥스식당 논현점", "서울 강남구 논현로 6")

        val messages = outbox.findAll().associateBy { it.aggregateId }

        messages.getValue(first.id.toString()).messageId shouldNotBe
            messages.getValue(second.id.toString()).messageId
    }
})
