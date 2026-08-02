package com.dibstable.restaurant.application.service

import com.dibstable.restaurant.application.port.input.FindRestaurantUseCase
import com.dibstable.restaurant.application.port.input.RegisterRestaurantUseCase
import com.dibstable.restaurant.application.port.output.LoadRestaurantPort
import com.dibstable.restaurant.application.port.output.SaveRestaurantPort
import com.dibstable.restaurant.domain.Restaurant
import org.springframework.stereotype.Service

@Service
class RestaurantService(
    private val saveRestaurant: SaveRestaurantPort,
    private val loadRestaurant: LoadRestaurantPort,
) : RegisterRestaurantUseCase, FindRestaurantUseCase {

    override fun register(name: String, address: String): Restaurant =
        saveRestaurant.save(Restaurant(name, address))

    override fun find(restaurantId: Long): Restaurant? =
        loadRestaurant.findById(restaurantId)
}
