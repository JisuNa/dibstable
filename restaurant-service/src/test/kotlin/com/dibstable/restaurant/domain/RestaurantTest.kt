package com.dibstable.restaurant.domain

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

class RestaurantTest : FunSpec({

    test("이름이 공백이면 식당을 만들 수 없다") {
        shouldThrow<IllegalArgumentException> { Restaurant("   ", "서울 강남구 테헤란로 1") }
    }

    test("주소가 공백이면 식당을 만들 수 없다") {
        shouldThrow<IllegalArgumentException> { Restaurant("딥스식당 강남점", "") }
    }

    test("식별자가 같으면 같은 식당이다") {
        Restaurant("딥스식당 강남점", "서울 강남구", id = 1) shouldBe
            Restaurant("딥스식당 판교점", "경기 성남시", id = 1)
    }

    test("저장 전 식당끼리는 서로 다르다") {
        Restaurant("딥스식당 강남점", "서울 강남구") shouldNotBe
            Restaurant("딥스식당 강남점", "서울 강남구")
    }

    test("저장 전이라도 자기 자신과는 같다") {
        val restaurant = Restaurant("딥스식당 강남점", "서울 강남구")

        restaurant shouldBe restaurant
    }
})
