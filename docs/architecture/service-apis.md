# 서비스 API 계약

서비스 API는 **커맨드·쿼리·이벤트** 세 요소로 구성된다(2.1.3). 이 문서는 그중 **구현된 것**의 구체적 계약을 담는다 — 추상 시스템 작업 목록은 [system-operations.md](system-operations.md)에 있다.

> 3.3.3은 비동기 API에 대해 **채널명·메시지 타입·메시지 포맷**을 반드시 명시하라고 요구한다. REST에는 오픈 API라는 표준이 있지만 비동기에는 없어서, 양쪽을 한 파일에 기술한다.
> **계약을 먼저 쓰고 구현한다** (3.1.2 API 우선 설계).

현재 범위: 3장. 사가 커맨드 채널은 4장에, 나머지 엔드포인트는 해당 장에서 추가한다.

## REST — restaurant-service

공통: 요청·응답 모두 `application/json`, 시각은 ISO-8601, 타임존 `Asia/Seoul` 고정.

### `POST /restaurants` — 식당 등록

요청
```json
{ "name": "딥스식당 강남점", "address": "서울 강남구 테헤란로 1" }
```

응답 `201 Created` · `Location: /restaurants/{restaurantId}`
```json
{ "restaurantId": 1, "name": "딥스식당 강남점", "address": "서울 강남구 테헤란로 1" }
```

| 상태 코드 | 조건 |
|---|---|
| 201 | 등록 성공 |
| 400 | `name`·`address` 누락 또는 공백 |

### `GET /restaurants/{restaurantId}` — 식당 조회

응답 `200 OK`
```json
{ "restaurantId": 1, "name": "딥스식당 강남점", "address": "서울 강남구 테헤란로 1" }
```

| 상태 코드 | 조건 |
|---|---|
| 200 | 조회 성공 |
| 404 | 해당 식당 없음 |

> 타임슬롯 관련 엔드포인트(`POST /restaurants/{id}/time-slots`, `GET .../time-slots`)는 **4장**에서 추가한다. 선점·해제 시맨틱을 사가와 함께 설계해야 하기 때문이다.

## 이벤트 — restaurant-service 발행

| 항목 | 값 |
|---|---|
| **채널(토픽)** | `restaurant` |
| **파티션 키** | `restaurantId` — 같은 식당의 이벤트 순서를 보장한다 (3.3.5) |
| **포맷** | JSON |

### 메시지 헤더

| 헤더 | 값 | 용도 |
|---|---|---|
| `message-id` | 아웃박스의 `message_id` (UUID) | 컨슈머의 중복 검출 키. **발행 시점에 새로 만들지 않는다** |
| `event-type` | 이벤트 타입명 (예: `RestaurantRegistered`) | 컨슈머의 디스패치 키 |

### 아웃박스 → 메시지 매핑

계약이 스키마를 정한다. 구현이 반대로 하지 않도록 여기에 못박는다.

| 아웃박스 컬럼 | 타입 | 메시지에서의 역할 |
|---|---|---|
| `id` | `BIGINT AUTO_INCREMENT` (PK) | **폴링 순서용.** 메시지에 실리지 않는다 |
| `message_id` | `CHAR(36)` UUID | → `message-id` 헤더. 전역 유일해야 여러 서비스가 발행해도 컨슈머가 오인하지 않는다 |
| `aggregate_type` | `VARCHAR` | 토픽 결정 (`Restaurant` → `restaurant`) |
| `aggregate_id` | `VARCHAR` | → **파티션 키** (`restaurantId`) |
| `event_type` | `VARCHAR` | → `event-type` 헤더 |
| `payload` | `JSON` | → 메시지 본문 |
| `created_at` | `DATETIME` | 진단용 |

발행에 성공한 행은 **DELETE**한다. `WHERE id > 마지막_발행_ID` 방식의 워터마크는 금지 — 채번 순서와 커밋 순서가 달라 이벤트가 영구 유실된다([ADR-005](../adr/ADR-005-ipc-style.md)).

### `RestaurantRegistered`

```json
{ "restaurantId": 1, "name": "딥스식당 강남점", "address": "서울 강남구 테헤란로 1" }
```

식당이 등록될 때 발행된다. 컨슈머가 되물어보지 않도록 필요한 데이터를 함께 싣는다(이벤트 강화, 5.3.3).

| 컨슈머 | 용도 |
|---|---|
| reservation-service | 식당 레플리카 유지 (3.4.2 데이터 복제) |

> **3장 시점에 레플리카를 읽는 코드는 없다.** 4장에서 사가를 시작하기 전 `restaurantId` 유효성을 로컬 검증하는 데 쓰이고, 7장 검색 뷰가 본격적으로 소비한다. 지금은 **멱등 컨슈머를 실증하기 위한 최소 부수 효과**라는 점을 명시해 둔다 — 부수 효과가 없는 컨슈머로는 중복 처리 여부를 확인할 수 없다.

## 아직 없는 것

| 항목 | 등장 |
|---|---|
| 사가 커맨드 채널 (`holdCapacity`·`authorizeDeposit` 등)과 응답 메시지 | 4장 |
| 예약·결제 REST 엔드포인트 | 4장 |
| 경로 버저닝(`/v1`) | 8장 — 외부 클라이언트가 생길 때 |
| 계약 위반 자동 검증 | 9장 컨슈머 주도 계약 테스트 |
