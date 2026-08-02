package com.dibstable.restaurant.application.port.input

import com.dibstable.restaurant.domain.Restaurant

interface FindRestaurantUseCase {

    fun find(restaurantId: Long): Restaurant?
}
