package com.qinet.feastique.model.entity.order

import com.fasterxml.jackson.annotation.JsonBackReference
import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonIgnore
import com.qinet.feastique.model.entity.address.CustomerAddress
import com.qinet.feastique.model.entity.address.VendorAddress
import com.qinet.feastique.model.entity.user.Vendor
import com.qinet.feastique.model.enums.OrderStatus
import com.qinet.feastique.model.enums.OrderType
import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import java.time.LocalDateTime
import java.time.LocalTime

@Entity
@Table(name = "food_order")
class FoodOrder : OrderEntity() {

    @JsonBackReference
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_address_id")
    @JsonIgnore
    lateinit var customerAddress: CustomerAddress

    @JsonBackReference
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vendor_address_id")
    @JsonIgnore
    lateinit var vendorAddress: VendorAddress

    @Column(name = "placement_time", nullable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    @CreationTimestamp
    var placementTime: LocalDateTime? = null

    @Column(name = "response_time", nullable = true)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    var responseTime: LocalDateTime? = null

    @Column(name = "delivery_time", nullable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm a")
    var deliveryTime: LocalTime? = null

    @Column(name = "delivery_fee", nullable = true)
    var deliveryFee: Long? = 0

    @Column(name = "total_amount", nullable = false)
    var totalAmount: Long? = 0

    @Column(name = "order_type", nullable = false)
    @Enumerated(EnumType.STRING)
    var orderType: OrderType? = null

    @Column(name = "order_status", nullable = false)
    var orderStatus: OrderStatus? = null

    @Column(name = "completed_time", nullable = true)
    var completedTime: LocalDateTime? = null

    @Column(name = "customer_deleted_status", nullable = false)
    var customerDeletedStatus: Boolean? = false

    @Column(name = "vendor_deleted_status", nullable = false)
    var vendorDeletedStatus: Boolean? = false
}

