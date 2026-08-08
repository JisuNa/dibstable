package com.dibstable.reservation.application.port.output

import com.dibstable.reservation.domain.RestaurantReplica

interface SaveRestaurantReplicaPort {

    fun save(replica: RestaurantReplica)
}
