package com.qinet.feastique.model.entity.order

class UnknowItemType : OrderEntity() {
    override fun calculateTotal(): Long {
        return 0
    }
}