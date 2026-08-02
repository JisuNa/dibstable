package com.dibstable.restaurant.adapter.input.web

import com.dibstable.restaurant.TestInfra
import com.jayway.jsonpath.JsonPath
import io.kotest.core.extensions.ApplyExtension
import io.kotest.core.spec.style.FunSpec
import io.kotest.extensions.spring.SpringExtension
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldMatch
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestInfra::class)
@ApplyExtension(SpringExtension::class)
class RestaurantApiTest(private val mockMvc: MockMvc) : FunSpec({

    fun post(body: String): MockHttpServletResponse =
        mockMvc.post("/restaurants") {
            contentType = MediaType.APPLICATION_JSON
            content = body
        }.andReturn().response

    fun register(name: String, address: String): MockHttpServletResponse =
        post("""{"name":"$name","address":"$address"}""")

    test("식당을 등록하면 201과 Location을 돌려준다") {
        val response = register("딥스식당 강남점", "서울 강남구 테헤란로 1")

        response.status shouldBe 201
        response.getHeader("Location")!! shouldMatch Regex("""/restaurants/\d+""")
        JsonPath.read<String>(response.contentAsString, "$.name") shouldBe "딥스식당 강남점"
        JsonPath.read<String>(response.contentAsString, "$.address") shouldBe "서울 강남구 테헤란로 1"
    }

    test("이름이 공백이면 400을 돌려준다") {
        register("   ", "서울 강남구 테헤란로 1").status shouldBe 400
    }

    test("주소가 공백이면 400을 돌려준다") {
        register("딥스식당 강남점", "").status shouldBe 400
    }

    test("이름 필드가 아예 없으면 400을 돌려준다") {
        post("""{"address":"서울 강남구 테헤란로 1"}""").status shouldBe 400
    }

    test("주소 필드가 아예 없으면 400을 돌려준다") {
        post("""{"name":"딥스식당 강남점"}""").status shouldBe 400
    }

    test("등록한 식당을 조회한다") {
        val restaurantId = JsonPath.read<Int>(
            register("딥스식당 판교점", "경기 성남시 분당구 판교로 2").contentAsString,
            "$.restaurantId",
        )

        val response = mockMvc.get("/restaurants/$restaurantId").andReturn().response

        response.status shouldBe 200
        JsonPath.read<Int>(response.contentAsString, "$.restaurantId") shouldBe restaurantId
        JsonPath.read<String>(response.contentAsString, "$.name") shouldBe "딥스식당 판교점"
    }

    test("없는 식당을 조회하면 404를 돌려준다") {
        mockMvc.get("/restaurants/999999").andReturn().response.status shouldBe 404
    }
})
