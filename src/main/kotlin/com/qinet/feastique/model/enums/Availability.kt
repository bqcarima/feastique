package com.qinet.feastique.model.enums

enum class Availability(val type: String) {
    AVAILABLE("Available"), // true
    UNAVAILABLE("Unavailable"), // false
    SOLD_OUT("Sold Out"); // null

    companion object {
        private val lookup = Availability.entries.associateBy { it.name.uppercase() }
        fun fromString(availability: String?): Availability {
            val key = availability ?: throw IllegalArgumentException(" null is not a valid entry.")
            return lookup[key.uppercase()]
                ?: throw IllegalArgumentException("$availability is not a valid entry.")
        }
    }
}

