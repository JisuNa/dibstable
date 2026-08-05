plugins {
    alias(libs.plugins.kotlin.jpa)
    alias(libs.plugins.kotlin.allopen)
}

// kotlin-jpa는 no-arg 생성자만 만든다. @Entity를 열어주지 않으면 클래스가 final로 남아
// Hibernate가 프록시를 못 만들고 지연 로딩이 조용히 즉시 로딩으로 떨어진다.
allOpen {
    annotation("jakarta.persistence.Entity")
    annotation("jakarta.persistence.MappedSuperclass")
    annotation("jakarta.persistence.Embeddable")
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.kafka:spring-kafka")
    runtimeOnly("org.flywaydb:flyway-mysql")
    runtimeOnly("com.mysql:mysql-connector-j")

    testImplementation("org.springframework.boot:spring-boot-testcontainers")
    testImplementation("org.testcontainers:mysql")
    // spring-boot-testcontainers는 연동 글루일 뿐이라 기술별 모듈이 따로 필요하다.
    testImplementation("org.testcontainers:kafka")
}

tasks.test {
    // 폴링이 돌면 다른 스펙이 방금 넣은 아웃박스 행을 지워 간섭한다.
    // src/test/resources/application.yml로 두면 안 된다 — 같은 이름이 클래스패스에서 먼저 잡혀
    // main의 application.yml을 통째로 가린다(datasource·jpa·kafka 설정이 전부 사라진다).
    systemProperty("dibstable.outbox.relay.enabled", "false")
}
