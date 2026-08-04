package com.dibstable.restaurant.application.service

import com.dibstable.restaurant.TestInfra
import com.dibstable.restaurant.adapter.output.messaging.OutboxEventPublisher
import com.dibstable.restaurant.adapter.output.messaging.OutboxJpaRepository
import com.dibstable.restaurant.adapter.output.persistence.RestaurantJpaRepository
import com.dibstable.restaurant.application.port.input.RegisterRestaurantUseCase
import com.dibstable.restaurant.application.port.output.PublishEventPort
import com.dibstable.restaurant.domain.RestaurantRegistered
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.extensions.ApplyExtension
import io.kotest.core.spec.style.FunSpec
import io.kotest.extensions.spring.SpringExtension
import io.kotest.matchers.shouldBe
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.Primary
import java.io.IOException

@SpringBootTest
@Import(TestInfra::class, PublishThenFail::class)
@ApplyExtension(SpringExtension::class)
class RegisterRestaurantAtomicityTest(
    private val registerRestaurant: RegisterRestaurantUseCase,
    private val restaurants: RestaurantJpaRepository,
    private val outbox: OutboxJpaRepository,
) : FunSpec({

    // 실패를 아웃박스 INSERT '뒤'에 일으킨다. 앞에서 막으면 outbox가 비어 있는 게 당연해져
    // 그 단언이 @Transactional 유무와 무관하게 항상 통과한다.
    test("아웃박스에 기록한 뒤 실패하면 식당과 메시지가 함께 롤백된다") {
        shouldThrow<IOException> {
            registerRestaurant.register("딥스식당 서초점", "서울 서초구 서초대로 4")
        }

        restaurants.count() shouldBe 0L
        outbox.count() shouldBe 0L
    }
})

// 실제 어댑터로 아웃박스 행을 넣은 뒤 checked 예외를 던진다.
// rollbackFor가 빠지면 스프링이 커밋해버리는 것을 이 조합이 잡아낸다.
@TestConfiguration(proxyBeanMethods = false)
class PublishThenFail {

    @Bean
    @Primary
    fun publishThenFail(outboxEventPublisher: OutboxEventPublisher): PublishEventPort =
        object : PublishEventPort {
            override fun publish(event: RestaurantRegistered) {
                outboxEventPublisher.publish(event)
                throw IOException("발행 실패")
            }
        }
}
