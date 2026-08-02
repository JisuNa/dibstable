plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.10.0"
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}

rootProject.name = "dibstable"

include(
    "reservation-service",
    "restaurant-service",
    "payment-service",
)

// search-service(7장) · api-gateway(8장) · waiting-service(13장)는 해당 장에서 추가한다.
