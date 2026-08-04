package com.dibstable.restaurant.domain

data class RestaurantRegistered(
    val restaurantId: Long,
    val name: String,
    val address: String,
) {
    companion object {
        fun of(restaurant: Restaurant) =
            RestaurantRegistered(restaurant.id, restaurant.name, restaurant.address)
    }
}
