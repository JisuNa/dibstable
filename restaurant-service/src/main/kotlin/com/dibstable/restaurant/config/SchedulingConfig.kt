package com.dibstable.restaurant.config

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableScheduling

// 테스트에서는 폴링이 돌면 다른 스펙이 방금 넣은 아웃박스 행을 지워 간섭한다.
// src/test/resources/application.yml이 이 값을 false로 내리고, 릴레이 테스트는 relay()를 직접 부른다.
@Configuration
@EnableScheduling
@ConditionalOnProperty(name = ["dibstable.outbox.relay.enabled"], matchIfMissing = true)
class SchedulingConfig
