package com.dibstable.restaurant.adapter.input.web

import com.dibstable.restaurant.domain.Restaurant

data class RestaurantResponse(
    val restaurantId: Long,
    val name: String,
    val address: String,
) {
    companion object {
        fun of(restaurant: Restaurant) =
            RestaurantResponse(restaurant.id, restaurant.name, restaurant.address)
    }
}
