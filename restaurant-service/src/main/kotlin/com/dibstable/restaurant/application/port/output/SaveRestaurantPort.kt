package com.dibstable.restaurant.application.port.output

import com.dibstable.restaurant.domain.Restaurant

interface SaveRestaurantPort {

    fun save(restaurant: Restaurant): Restaurant
}
