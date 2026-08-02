# DibsTable

캐치테이블을 오마주한 **식당 예약 & 예약금 결제 플랫폼**.
《마이크로서비스 패턴》(크리스 리처드슨)의 1~13장을 **그린필드 MSA**로 장별 실습하는 학습 프로젝트다.

> "dibs!" — 찜! 타임슬롯을 선점(hold)하고 예약금을 결제하면 좌석이 확정된다.

## 핵심 시나리오

고객이 식당을 검색하고 타임슬롯을 골라 **예약금을 결제하면 예약이 확정**된다.
인기 식당은 예약 오픈 순간 트래픽이 폭주하며(좌석 초과 예약 0건이 최우선 불변 값), 취소 시 정책에 따라 환불된다.

## 서비스 지도 (예정)

경계를 그은 근거는 [ADR-004](docs/adr/ADR-004-service-boundaries.md), 작업 배정은 [system-operations](docs/architecture/system-operations.md)에 있다.

| 서비스 | 책임 | 등장 장 | FTGO 대응 |
|---|---|---|---|
| reservation-service | 예약 생성·취소, 사가 오케스트레이션 | 2장 | 주문 서비스 |
| restaurant-service | 식당·타임슬롯 재고, 선점/해제 | 2장 | 음식점·주방 서비스 |
| payment-service | 예약금 결제·환불 (이벤트 소싱), PG 연동 | 2장 | 회계 서비스 |
| search-service | 식당 검색·인기 랭킹 (CQRS 뷰) | 7장 | 주문 이력 서비스 |
| api-gateway | 라우팅·API 조합·인증 | 8장 | API 게이트웨이 |
| waiting-service | 웨이팅 (신규 기능을 서비스로) | 13장 | — |

## 장별 진행

| 장 | 주제 | 상태 | 산출물 |
|---|---|---|---|
| 1 | 기반 구축 — 요구사항·ADR·확장 큐브·빌드 뼈대 | ✅ | [requirements](docs/requirements.md) · [ADR-001](docs/adr/ADR-001-microservice-architecture.md) · [ADR-002](docs/adr/ADR-002-monorepo-gradle-multimodule.md) · [ADR-003](docs/adr/ADR-003-tech-baseline.md) · [scale-cube](docs/architecture/scale-cube.md) |
| 2 | 분해 전략 — 시스템 작업·서비스 경계·모듈 3개 | ✅ | [system-operations](docs/architecture/system-operations.md) · [ADR-004](docs/adr/ADR-004-service-boundaries.md) |
| 3 | IPC — REST·Kafka·트랜잭셔널 아웃박스·멱등 컨슈머 | 🚧 | [ADR-005](docs/adr/ADR-005-ipc-style.md) · [ADR-006](docs/adr/ADR-006-database-per-service.md) · [service-apis](docs/architecture/service-apis.md) |
| 4 | 사가 — 예약 생성·취소 사가, 보상 트랜잭션 | ⬜ | |
| 5 | 애그리거트 — 불변 값·낙관적 잠금·도메인 이벤트 | ⬜ | |
| 6 | 이벤트 소싱 — payment-service | ⬜ | |
| 7 | CQRS — search-service·내 예약 뷰 | ⬜ | |
| 8 | API 게이트웨이 — 라우팅·조합·BFF | ⬜ | |
| 9 | 테스트 1부 — 단위·계약 테스트 | ⬜ | |
| 10 | 테스트 2부 — 통합·컴포넌트·E2E | ⬜ | |
| 11 | 프로덕션 레디 — JWT·구성·관측성 | ⬜ | |
| 12 | 배포 — 컨테이너·docker-compose | ⬜ | |
| 13 | 신규 기능 — waiting-service·ACL·피처 토글 | ⬜ | |

각 장의 완료 시점·리드 타임은 이 표와 git 이력이 대신한다 (1장 전달 4지표의 경량 대체).

## 기술 기준선 ([ADR-003](docs/adr/ADR-003-tech-baseline.md))

- JDK 21
- Kotlin 2.1.21
- Spring Boot 3.5.16
- Gradle 8.14.3 (wrapper)

## 실행

인프라(MySQL·Kafka)를 먼저 띄운다. 3장부터 서비스가 여기에 의존한다.

```bash
docker compose up -d
```

```bash
./gradlew build
```

서비스 포트는 `SERVER_PORT` 환경 변수로 주입하며 기본값은 8080이다(컨테이너마다 자기 8080을 쓴다). 호스트에서 여러 서비스를 동시에 띄울 때는 `SERVER_PORT=8081 ./gradlew :restaurant-service:bootRun`처럼 겹치지 않게 지정한다.

## 브랜치 규칙

`main` + 장별 브랜치(`chNN-<주제>`), 장 완료 리뷰 후 머지. 커밋 메시지는 `chNN: 요약`.
