package com.qinet.feastique.model.entity.order.item

import com.qinet.feastique.model.entity.order.OrderEntity

class UnknownItem : OrderEntity() {
    override fun calculateTotal(): Long {
        return 0
    }
}