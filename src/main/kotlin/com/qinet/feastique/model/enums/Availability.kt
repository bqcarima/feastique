package com.qinet.feastique.model.enums

enum class Availability(val type: String) {
    AVAILABLE("Available"),
    UNAVAILABLE("Unavailable");

    companion object {
        private val lookup = Availability.entries.associateBy { it.name.uppercase() }
        fun fromString(availabilityName: String): Availability =
            lookup[availabilityName.uppercase()] ?: throw IllegalArgumentException("$availabilityName is not a valid entry.")
    }
}

