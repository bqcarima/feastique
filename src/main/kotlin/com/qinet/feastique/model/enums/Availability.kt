package com.qinet.feastique.model.enums

enum class Availability(val type: String) {
    AVAILABLE("Available"), // true
    UNAVAILABLE("Unavailable"), // false
    SOLD_OUT("Sold Out"); // null

    companion object {
        private val lookup = Availability.entries.associateBy { it.name.uppercase() }
        fun fromString(availabilityName: String?): Availability {
            val key = availabilityName ?: throw IllegalArgumentException(" null is not a valid entry.")
            return lookup[key.uppercase()]
                ?: throw IllegalArgumentException("$availabilityName is not a valid entry.")
        }
    }
}

