CREATE TABLE outbox
(
    id             BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '폴링 순서용 — 메시지에 실리지 않는다',
    message_id     CHAR(36)        NOT NULL COMMENT 'message-id 헤더로 나가는 UUID',
    aggregate_type VARCHAR(50)     NOT NULL COMMENT '토픽 결정 (Restaurant → restaurant)',
    aggregate_id   VARCHAR(50)     NOT NULL COMMENT '파티션 키 (restaurantId)',
    event_type     VARCHAR(100)    NOT NULL COMMENT 'event-type 헤더 (RestaurantRegistered)',
    payload        JSON            NOT NULL COMMENT '메시지 본문',
    created_at     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '진단용',
    PRIMARY KEY (id),
    UNIQUE KEY uk_outbox_message_id (message_id)
) ENGINE = InnoDB COMMENT = '발행 대기 메시지 — 발행에 성공한 행은 DELETE한다';
