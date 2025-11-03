package com.qinet.feastique.model.enums

enum class Region(val type: String) {
    ADAMAWA("ADAMAWA"),
    CENTRE("CENTRE"),
    EAST("EAST"),
    FAR_NORTH("FAR_NORTH"),
    LITTORAL("LITTORAL"),
    NORTH("NORTH"),
    NORTHWEST("NORTHWEST"),
    SOUTH("SOUTH"),
    SOUTHWEST("SOUTHWEST"),
    WEST("WEST"),
    NON_SELECTED("NONE");
    companion object {
        private val lookup = Region.entries.associateBy { it.name.uppercase() }
        fun fromString(regionName: String): Region =
            lookup[regionName.uppercase()] ?: throw IllegalArgumentException("$regionName is not a valid region.")
    }
}

