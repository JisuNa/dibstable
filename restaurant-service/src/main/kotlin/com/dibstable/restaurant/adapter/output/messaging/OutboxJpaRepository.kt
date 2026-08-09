package com.dibstable.restaurant.adapter.output.messaging

import org.springframework.data.jpa.repository.JpaRepository

interface OutboxJpaRepository : JpaRepository<OutboxJpaEntity, Long> {

    // id 오름차순이 곧 채번 순서다. 한 회차에 옮길 행 수를 100으로 묶어 폴링이 길어지지 않게 한다.
    fun findTop100ByOrderByIdAsc(): List<OutboxJpaEntity>
}
