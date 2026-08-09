package com.dibstable.reservation.domain

// restaurant-service가 소유한 데이터의 읽기 전용 사본. 여기서 값을 바꾸거나 ID를 채번하지 않는다.
data class RestaurantReplica(
    val restaurantId: Long,
    val name: String,
    val address: String,
)
