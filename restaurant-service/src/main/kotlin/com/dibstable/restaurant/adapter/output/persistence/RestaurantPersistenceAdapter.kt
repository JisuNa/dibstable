package com.dibstable.restaurant.adapter.output.persistence

import com.dibstable.restaurant.application.port.output.LoadRestaurantPort
import com.dibstable.restaurant.application.port.output.SaveRestaurantPort
import com.dibstable.restaurant.domain.Restaurant
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository

@Repository
class RestaurantPersistenceAdapter(
    private val restaurants: RestaurantJpaRepository,
) : SaveRestaurantPort, LoadRestaurantPort {

    override fun save(restaurant: Restaurant): Restaurant =
        restaurants.save(RestaurantJpaEntity.from(restaurant)).toDomain()

    override fun findById(restaurantId: Long): Restaurant? =
        restaurants.findByIdOrNull(restaurantId)?.toDomain()
}
