package com.dibstable.restaurant

import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.context.annotation.Bean
import org.testcontainers.containers.MySQLContainer
import org.testcontainers.kafka.KafkaContainer

@TestConfiguration(proxyBeanMethods = false)
class TestInfra {

    @Bean
    @ServiceConnection
    fun mysql(): MySQLContainer<*> = MySQLContainer("mysql:8.4")

    // NewTopic 빈이 있으면 KafkaAdmin이 기동 시 브로커에 붙으므로 모든 컨텍스트에 브로커가 필요하다.
    @Bean
    @ServiceConnection
    fun kafka(): KafkaContainer = KafkaContainer("apache/kafka:4.3.1")
}
