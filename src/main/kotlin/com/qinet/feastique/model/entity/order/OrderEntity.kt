package com.qinet.feastique.model.entity.order

import com.fasterxml.jackson.annotation.JsonFormat
import com.github.f4b6a3.uuid.UuidCreator
import com.qinet.feastique.model.entity.user.Vendor
import com.qinet.feastique.model.enums.OrderType
import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import java.time.LocalDateTime
import java.util.*

@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
abstract class OrderEntity {

    @Id
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "uuid")
    var id: UUID = UuidCreator.getTimeOrdered()

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vendor_id", nullable = false)
    var vendor: Vendor? = null

    var quantity: Int = 1

    @Column(name = "total_amount")
    var totalAmount: Long? = 0

    @Column(name = "order_type")
    @Enumerated(EnumType.STRING)
    var orderType: OrderType? = null

    @Column(name = "added_at")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH-mm-ss-dd-MM-yyyy")
    @CreationTimestamp
    var addedAt: LocalDateTime? = null

    abstract fun calculateTotal(): Long
}

