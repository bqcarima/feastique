package com.qinet.feastique.model.enums

enum class OrderStatus(val type: String) {
    CANCELLED("Cancelled"),
    CONFIRMED("Confirmed"),
    DELIVERED("Delivered"),
    EN_ROUTE("En route"),
    PENDING("Pending"),
    READY("Ready"),
    SERVED("Served")
}