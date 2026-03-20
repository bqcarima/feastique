package com.qinet.feastique.model.entity.message

import com.fasterxml.jackson.annotation.JsonIgnore
import com.github.f4b6a3.uuid.UuidCreator
import com.qinet.feastique.model.entity.user.Customer
import com.qinet.feastique.model.entity.user.Vendor
import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "conversations")
class Conversation {

    @Id
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "uuid")
    var id: UUID = UuidCreator.getTimeOrdered()

    @Column(name = "started_at", updatable = false)
    var startedAt: LocalDateTime = LocalDateTime.now()

    @Column(name = "customer_read", nullable = false)
    var customerRead: Boolean = false

    @Column(name = "vendor_read", nullable = false)
    var vendorRead: Boolean = false

    @Column(name = "customer_deleted", nullable = false)
    var customerDeleted: Boolean = false

    @Column(name = "vendor_deleted", nullable = false)
    var vendorDeleted: Boolean = false

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vendor_id", nullable = false)
    @JsonIgnore
    lateinit var vendor: Vendor

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    @JsonIgnore
    lateinit var customer: Customer
}

