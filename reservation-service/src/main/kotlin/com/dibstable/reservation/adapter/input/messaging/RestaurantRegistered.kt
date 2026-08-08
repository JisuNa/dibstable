package com.dibstable.reservation.adapter.input.messaging

// restaurant-service의 클래스를 공유하지 않고 이 서비스가 자기 사본을 갖는다.
// 코드를 공유하면 서비스가 함께 배포돼야 하는 결합이 생긴다(ADR-004).
data class RestaurantRegistered(
    val restaurantId: Long,
    val name: String,
    val address: String,
)
