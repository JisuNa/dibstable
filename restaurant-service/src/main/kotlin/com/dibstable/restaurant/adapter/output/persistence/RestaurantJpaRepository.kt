package com.dibstable.restaurant.adapter.output.persistence

import org.springframework.data.jpa.repository.JpaRepository

interface RestaurantJpaRepository : JpaRepository<RestaurantJpaEntity, Long>
