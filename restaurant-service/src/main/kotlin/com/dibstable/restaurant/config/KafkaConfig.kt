package com.dibstable.restaurant.config

import org.apache.kafka.clients.admin.NewTopic
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.config.TopicBuilder

@Configuration
class KafkaConfig {

    // 토픽은 애플리케이션이 명시 선언한다 — 브로커의 KAFKA_NUM_PARTITIONS는 보조 안전망일 뿐이다.
    // 파티션 키(restaurantId)의 순서 보장을 검증하려면 파티션이 2개 이상이어야 한다.
    @Bean
    fun restaurantTopic(): NewTopic =
        TopicBuilder.name("restaurant").partitions(3).replicas(1).build()
}
