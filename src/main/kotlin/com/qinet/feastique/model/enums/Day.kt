package com.qinet.feastique.model.enums

enum class Day(val type: String) {
    MONDAY("Monday"),
    TUESDAY("Tuesday"),
    WEDNESDAY("Wednesday"),
    THURSDAY("Thursday"),
    FRIDAY("Friday"),
    SATURDAY("Saturday"),
    SUNDAY("Sunday"),
    EVERYDAY("Everyday"),
    NONE("None");

    companion object {
        private val lookup = Day.entries.associateBy { it.name.uppercase() }
        fun fromString(dayName: String): Day =
            lookup[dayName.uppercase()] ?: throw IllegalArgumentException("$dayName is not a valid entry.")
    }
}

