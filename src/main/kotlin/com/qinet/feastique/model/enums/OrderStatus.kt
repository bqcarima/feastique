package com.qinet.feastique.model.enums

enum class OrderStatus(val type: String) {
    CANCELLED("Cancelled"),
    COLLECTED("Collected"),
    CONFIRMED("Confirmed"),
    DECLINED("Declined"),
    DELIVERED("Delivered"),
    EN_ROUTE("En route"),
    PENDING("Pending"),
    READY("Ready"),
    SERVED("Served");

    companion object {
        private val lookup = OrderStatus.entries.associateBy { it.name.uppercase() }
        fun fromString(orderStatusName: String): OrderStatus =
            lookup[orderStatusName.uppercase()] ?: throw IllegalArgumentException("$orderStatusName is not a valid entry.")
    }
}

