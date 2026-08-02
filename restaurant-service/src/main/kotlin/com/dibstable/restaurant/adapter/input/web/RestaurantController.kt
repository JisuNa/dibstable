package com.dibstable.restaurant.adapter.input.web

import com.dibstable.restaurant.application.port.input.FindRestaurantUseCase
import com.dibstable.restaurant.application.port.input.RegisterRestaurantUseCase
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import java.net.URI

@RestController
@RequestMapping("/restaurants")
class RestaurantController(
    private val registerRestaurant: RegisterRestaurantUseCase,
    private val findRestaurant: FindRestaurantUseCase,
) {
    @PostMapping(name = "식당 등록")
    fun register(@Valid @RequestBody request: RegisterRestaurantRequest): ResponseEntity<RestaurantResponse> {
        val registered = registerRestaurant.register(request.name, request.address)
        return ResponseEntity
            .created(URI.create("/restaurants/${registered.id}"))
            .body(RestaurantResponse.of(registered))
    }

    @GetMapping("/{restaurantId}", name = "식당 조회")
    fun find(@PathVariable restaurantId: Long): RestaurantResponse =
        findRestaurant.find(restaurantId)?.let(RestaurantResponse::of)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
}
