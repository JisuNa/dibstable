package com.dibstable.restaurant.adapter.output.persistence

import com.dibstable.restaurant.domain.Restaurant
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "restaurants")
class RestaurantJpaEntity(

    val name: String,

    val address: String,

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
) {
    fun toDomain(): Restaurant = Restaurant(name, address, id)

    companion object {
        fun from(restaurant: Restaurant): RestaurantJpaEntity =
            RestaurantJpaEntity(restaurant.name, restaurant.address, restaurant.id)
    }
}
