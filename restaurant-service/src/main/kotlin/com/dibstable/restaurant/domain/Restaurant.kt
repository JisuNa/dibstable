package com.dibstable.restaurant.domain

class Restaurant(

    val name: String,

    val address: String,

    val id: Long = 0,
) {
    init {
        require(name.isNotBlank()) { "식당명은 공백일 수 없다" }
        require(address.isNotBlank()) { "주소는 공백일 수 없다" }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Restaurant) return false
        return id != UNSAVED_ID && id == other.id
    }

    override fun hashCode(): Int = javaClass.hashCode()

    companion object {
        private const val UNSAVED_ID = 0L
    }
}
