package com.dibstable.reservation.application.service

import com.dibstable.reservation.TestInfra
import com.dibstable.reservation.adapter.output.persistence.ProcessedMessageJpaRepository
import com.dibstable.reservation.adapter.output.persistence.RestaurantReplicaJpaRepository
import com.dibstable.reservation.application.port.input.ReplicateRestaurantUseCase
import com.dibstable.reservation.application.port.output.SaveRestaurantReplicaPort
import com.dibstable.reservation.domain.RestaurantReplica
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
import java.util.UUID

@SpringBootTest
@Import(TestInfra::class, FailingReplicaSave::class)
@ApplyExtension(SpringExtension::class)
class ReplicateAtomicityTest(
    private val replicateRestaurant: ReplicateRestaurantUseCase,
    private val processedMessages: ProcessedMessageJpaRepository,
    private val replicas: RestaurantReplicaJpaRepository,
) : FunSpec({

    // 처리 기록만 남고 레플리카가 안 바뀌면, 그 메시지는 이후 영원히 중복으로 걸러져
    // 복구 경로가 사라진다. 두 쓰기가 한 트랜잭션이라는 것이 T3의 핵심이다.
    test("레플리카 저장이 실패하면 처리 기록도 남지 않는다") {
        val messageId = UUID.randomUUID().toString()

        shouldThrow<IOException> {
            replicateRestaurant.replicate(messageId, 21, "딥스식당 연남점", "서울 마포구 연남로 21")
        }

        processedMessages.findById(messageId).isPresent shouldBe false
        replicas.findById(21L).isPresent shouldBe false
    }
})

// checked 예외로 던진다 — rollbackFor가 빠지면 스프링이 커밋해버리는 것까지 이 조합이 잡는다.
@TestConfiguration(proxyBeanMethods = false)
class FailingReplicaSave {

    @Bean
    @Primary
    fun failingSaveRestaurantReplicaPort(): SaveRestaurantReplicaPort =
        object : SaveRestaurantReplicaPort {
            override fun save(replica: RestaurantReplica) = throw IOException("저장 실패")
        }
}
