plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.10.0"
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}

rootProject.name = "dibstable"

// 서비스 모듈은 2장(분해 전략)의 산출물로 추가된다.
// 예정: reservation-service, restaurant-service, payment-service,
//       search-service, waiting-service, api-gateway
