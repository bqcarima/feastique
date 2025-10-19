package com.qinet.feastique.model.entity.order

import com.github.f4b6a3.uuid.UuidCreator
import com.qinet.feastique.model.enums.OrderType
import jakarta.persistence.*
import java.util.UUID

@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
abstract class OrderEntity {

    @Id
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "uuid")
    var id: UUID = UuidCreator.getTimeOrdered()

    var quantity: Int? = 1

    @Column(name = "total_amount")
    var totalAmount: Long? = 0

    @Column(name = "order_type")
    @Enumerated(EnumType.STRING)
    var orderType: OrderType? = null

    abstract fun calculateTotal(): Long
}

