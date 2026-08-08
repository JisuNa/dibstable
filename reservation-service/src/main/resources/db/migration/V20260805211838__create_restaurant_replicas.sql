CREATE TABLE restaurant_replicas
(
    restaurant_id BIGINT UNSIGNED NOT NULL COMMENT 'restaurant-service가 채번한 ID — 여기서 채번하지 않는다',
    name          VARCHAR(100)    NOT NULL COMMENT '식당명',
    address       VARCHAR(255)    NOT NULL COMMENT '주소',
    PRIMARY KEY (restaurant_id)
) ENGINE = InnoDB COMMENT = '식당 레플리카 — 소유자는 restaurant-service, 여기서는 읽기 전용 사본';
