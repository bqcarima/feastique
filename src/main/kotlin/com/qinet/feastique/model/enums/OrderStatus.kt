package com.qinet.feastique.model.enums

enum class OrderStatus(val type: String) {
    CANCELLED("CANCELLED"),
    COLLECTED("COLLECTED"),
    CONFIRMED("CONFIRMED"),
    DECLINED("DECLINED"),
    DELIVERED("DELIVERED"),
    EN_ROUTE("EN_ROUTE"),
    PENDING("PENDING"),
    READY("READY"),
    SERVED("SERVED")
}