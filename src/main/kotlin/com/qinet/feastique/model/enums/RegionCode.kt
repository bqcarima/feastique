package com.qinet.feastique.model.enums

enum class RegionCode(val type: String) {
    ADAMAWA("CMO1"),
    CENTRE("CM02"),
    EAST("CM03"),
    FAR_NORTH("CM04"),
    LITTORAL("CM05"),
    NORTH("CM06"),
    NORTHWEST("CM07"),
    SOUTH("CM08"),
    SOUTHWEST("CM09"),
    WEST("CM10");

    companion object {
        private val lookup = RegionCode.entries.associateBy { it.name.uppercase() }
        fun fromString(regionName: String): RegionCode =
            lookup[regionName.uppercase()] ?: throw IllegalArgumentException("$regionName is not a valid region.")
    }
}

