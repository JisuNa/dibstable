package com.dibstable.restaurant.application.service

import com.dibstable.restaurant.application.port.input.FindRestaurantUseCase
import com.dibstable.restaurant.application.port.input.RegisterRestaurantUseCase
import com.dibstable.restaurant.application.port.output.LoadRestaurantPort
import com.dibstable.restaurant.application.port.output.PublishEventPort
import com.dibstable.restaurant.application.port.output.SaveRestaurantPort
import com.dibstable.restaurant.domain.Restaurant
import com.dibstable.restaurant.domain.RestaurantRegistered
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class RestaurantService(
    private val saveRestaurant: SaveRestaurantPort,
    private val loadRestaurant: LoadRestaurantPort,
    private val publishEvent: PublishEventPort,
) : RegisterRestaurantUseCase, FindRestaurantUseCase {

    // 식당 저장과 이벤트 기록이 한 트랜잭션이어야 한다 — 나뉘면 "저장은 됐는데 이벤트가 없다"가 생긴다.
    // 저장이 먼저인 이유는 채번된 restaurantId가 페이로드와 파티션 키에 모두 필요해서다.
    //
    // rollbackFor가 필요한 이유: 스프링 기본 롤백 규칙은 unchecked 예외만 걸러낸다.
    // 직렬화가 던지는 JsonProcessingException은 IOException 하위(checked)라 기본값이면
    // 롤백되지 않고 식당만 커밋된다 — 아웃박스가 막으려던 바로 그 상태가 된다.
    @Transactional(rollbackFor = [Exception::class])
    override fun register(name: String, address: String): Restaurant {
        val restaurant = saveRestaurant.save(Restaurant(name, address))
        publishEvent.publish(RestaurantRegistered.of(restaurant))
        return restaurant
    }

    override fun find(restaurantId: Long): Restaurant? =
        loadRestaurant.findById(restaurantId)
}
