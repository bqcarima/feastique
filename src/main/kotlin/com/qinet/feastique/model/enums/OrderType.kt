package com.qinet.feastique.model.enums

enum class OrderType(val type: String) {
    DELIVERY("Delivery"),
    DINE_IN("Dine-in"),
    PICKUP("Pickup"),
    UNKNOWN("Unknown");

    companion object {
        private val lookup = OrderType.entries.associateBy { it.name.uppercase() }
        fun fromString(orderTypeName: String): OrderType =
            lookup[orderTypeName.uppercase()] ?: throw IllegalArgumentException("$orderTypeName is not a valid entry.")
    }
}

