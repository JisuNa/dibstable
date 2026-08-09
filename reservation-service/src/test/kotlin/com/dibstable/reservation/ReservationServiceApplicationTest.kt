package com.dibstable.reservation

import io.kotest.core.extensions.ApplyExtension
import io.kotest.core.spec.style.FunSpec
import io.kotest.extensions.spring.SpringExtension
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import

@SpringBootTest
@Import(TestInfra::class)
@ApplyExtension(SpringExtension::class)
class ReservationServiceApplicationTest : FunSpec({

    test("스프링 컨텍스트가 로드된다") { }
})
