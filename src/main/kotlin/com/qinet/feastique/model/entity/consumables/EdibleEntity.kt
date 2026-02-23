package com.qinet.feastique.model.entity.consumables

import com.fasterxml.jackson.annotation.JsonBackReference
import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonIgnore
import com.github.f4b6a3.uuid.UuidCreator
import com.qinet.feastique.model.entity.user.Vendor
import com.qinet.feastique.model.enums.Availability
import jakarta.persistence.*
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull
import org.hibernate.annotations.CreationTimestamp
import java.time.Instant
import java.time.LocalTime
import java.util.*

@MappedSuperclass
abstract class EdibleEntity {

    @Id
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "uuid")
    var id: UUID = UuidCreator.getTimeOrdered()

    @Column(name = "name")
    @NotEmpty(message = "Name cannot be empty.")
    var name: String? = ""

    @JsonBackReference // prevent infinite recursion for extra protection
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vendor_id", nullable = false)
    @JsonIgnore
    lateinit var vendor: Vendor

    @NotNull(message = "Delivery availability cannot be empty.")
    @Column(name = "deliverable", nullable = false)
    var deliverable: Boolean? = false

    @Column(name = "daily_delivery_quantity", nullable = true)
    var dailyDeliveryQuantity: Int? = null // null -> unlimited

    @Column(name = "availability")
    @Enumerated(EnumType.STRING)
    var availability: Availability? = null

    @Column(name = "quick_delivery", nullable = false)
    var quickDelivery: Boolean? = false

    @Column(name = "preparation_time")
    @NotNull(message = "Preparation time cannot be empty.")
    var preparationTime: Int? = 0

    @Suppress("unused")
    @Column(name = "created_at", nullable = true)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    @CreationTimestamp
    var createdAt: Instant? = null
}

