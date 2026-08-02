package com.dibstable.restaurant.application.port.output

import com.dibstable.restaurant.domain.Restaurant

interface LoadRestaurantPort {

    fun findById(restaurantId: Long): Restaurant?
}
