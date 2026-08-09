package com.dibstable.reservation.application.port.input

interface ReplicateRestaurantUseCase {

    // 이미 처리한 메시지면 아무것도 하지 않고 false를 돌려준다.
    fun replicate(messageId: String, restaurantId: Long, name: String, address: String): Boolean
}
