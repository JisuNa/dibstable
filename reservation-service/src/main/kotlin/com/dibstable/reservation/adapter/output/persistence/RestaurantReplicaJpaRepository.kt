package com.dibstable.reservation.adapter.output.persistence

import org.springframework.data.jpa.repository.JpaRepository

interface RestaurantReplicaJpaRepository : JpaRepository<RestaurantReplicaJpaEntity, Long>
