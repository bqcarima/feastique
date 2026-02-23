package com.qinet.feastique.model.enums

enum class Region(val type: String) {
    ADAMAWA("Adamawa"),
    CENTRE("Centre"),
    EAST("East"),
    FAR_NORTH("Far North"),
    LITTORAL("Littoral"),
    NORTH("North"),
    NORTHWEST("Northwest"),
    SOUTH("South"),
    SOUTHWEST("Southwest"),
    WEST("West"),
    NON_SELECTED("None");
    companion object {
        private val lookup = Region.entries.associateBy { it.name.uppercase() }
        fun fromString(region: String?): Region {
            val key = region ?: throw IllegalArgumentException("null is not a valid entry.")
            return lookup[key.uppercase()] ?: throw IllegalArgumentException("$region is not a valid region.")
        }
    }
}

