package com.qinet.feastique.model.enums

enum class OrderType(val type: String) {
    DELIVERY("Delivery"),
    DINE_IN("Dine-in"),
    PICKUP("Pickup"),
    UNKNOWN("Unknown");

    companion object {
        private val lookup = OrderType.entries.associateBy { it.name.uppercase() }
        fun fromString(orderType: String?): OrderType {
            val key = orderType ?: throw IllegalArgumentException("null is not a valid entry.")
            return lookup[key.uppercase()] ?: throw IllegalArgumentException("$orderType is not a valid entry.")
        }
    }
}

