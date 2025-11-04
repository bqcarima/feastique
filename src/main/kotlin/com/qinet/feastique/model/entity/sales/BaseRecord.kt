package com.qinet.feastique.model.entity.sales

import com.fasterxml.jackson.annotation.JsonFormat
import com.github.f4b6a3.uuid.UuidCreator
import com.qinet.feastique.model.entity.user.Vendor
import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.*

@MappedSuperclass
abstract class BaseRecord {

    @Id
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "uuid")
    var id: UUID = UuidCreator.getTimeOrdered()

    var quantity: Int? = null
    var amount: Long? = null

    @ManyToOne
    @JoinColumn(name = "vendor_id", nullable = false)
    lateinit var vendor: Vendor

    @Column(name = "sale_date")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    var saleDate: LocalDateTime? = null
}

