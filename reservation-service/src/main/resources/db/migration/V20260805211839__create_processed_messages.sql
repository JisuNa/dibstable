CREATE TABLE processed_messages
(
    message_id   CHAR(36) NOT NULL COMMENT '처리 완료한 메시지 ID — 발행 측 아웃박스의 message_id',
    processed_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '진단용',
    PRIMARY KEY (message_id)
) ENGINE = InnoDB COMMENT = '중복 검출 — 브로커가 같은 메시지를 다시 줘도 부수 효과는 한 번만';
