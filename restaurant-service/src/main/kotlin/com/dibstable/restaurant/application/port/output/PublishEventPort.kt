package com.dibstable.restaurant.application.port.output

import com.dibstable.restaurant.domain.RestaurantRegistered

interface PublishEventPort {

    fun publish(event: RestaurantRegistered)
}
