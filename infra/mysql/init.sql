-- 서비스별 database와 전용 계정 (ADR-006)
-- MySQL은 크로스 database 조인이 가능하므로, 계정 권한으로 접근을 격리한다.

CREATE DATABASE IF NOT EXISTS restaurant_db  CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
CREATE DATABASE IF NOT EXISTS reservation_db CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
CREATE DATABASE IF NOT EXISTS payment_db     CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;

CREATE USER IF NOT EXISTS 'restaurant'@'%'  IDENTIFIED BY 'restaurant';
CREATE USER IF NOT EXISTS 'reservation'@'%' IDENTIFIED BY 'reservation';
CREATE USER IF NOT EXISTS 'payment'@'%'     IDENTIFIED BY 'payment';

GRANT ALL PRIVILEGES ON restaurant_db.*  TO 'restaurant'@'%';
GRANT ALL PRIVILEGES ON reservation_db.* TO 'reservation'@'%';
GRANT ALL PRIVILEGES ON payment_db.*     TO 'payment'@'%';

FLUSH PRIVILEGES;
