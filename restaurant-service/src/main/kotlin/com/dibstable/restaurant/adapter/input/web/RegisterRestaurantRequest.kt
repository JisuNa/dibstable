package com.dibstable.restaurant.adapter.input.web

import jakarta.validation.constraints.NotBlank

data class RegisterRestaurantRequest(
    @field:NotBlank val name: String,
    @field:NotBlank val address: String,
)
