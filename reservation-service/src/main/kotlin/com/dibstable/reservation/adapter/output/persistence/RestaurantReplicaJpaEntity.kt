package com.dibstable.reservation.adapter.output.persistence

import com.dibstable.reservation.domain.RestaurantReplica
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table

// @GeneratedValue가 없다 — ID는 restaurant-service가 채번한 값을 그대로 받는다.
@Entity
@Table(name = "restaurant_replicas")
class RestaurantReplicaJpaEntity(

    @Id
    val restaurantId: Long,

    val name: String,

    val address: String,
) {
    companion object {
        fun from(replica: RestaurantReplica): RestaurantReplicaJpaEntity =
            RestaurantReplicaJpaEntity(replica.restaurantId, replica.name, replica.address)
    }
}
