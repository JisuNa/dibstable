package com.dibstable.reservation.application.port.output

interface RecordProcessedMessagePort {

    // 처음 보는 메시지면 true, 이미 처리한 메시지면 false.
    fun recordIfAbsent(messageId: String): Boolean
}
