package com.dibstable.restaurant.application.port.input

import com.dibstable.restaurant.domain.Restaurant

interface RegisterRestaurantUseCase {

    fun register(name: String, address: String): Restaurant
}
