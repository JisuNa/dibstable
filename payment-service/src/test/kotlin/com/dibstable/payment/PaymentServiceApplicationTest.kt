package com.dibstable.payment

import io.kotest.core.extensions.ApplyExtension
import io.kotest.core.spec.style.FunSpec
import io.kotest.extensions.spring.SpringExtension
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
@ApplyExtension(SpringExtension::class)
class PaymentServiceApplicationTest : FunSpec({

    test("스프링 컨텍스트가 로드된다") { }
})
