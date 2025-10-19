package com.qinet.feastique.model.entity.order.beverage

import com.fasterxml.jackson.annotation.JsonBackReference
import com.fasterxml.jackson.annotation.JsonIgnore
import com.qinet.feastique.model.entity.beverage.Beverage
import com.qinet.feastique.model.entity.order.OrderEntity
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.MappedSuperclass

@MappedSuperclass
abstract class BeverageEntity : OrderEntity() {

    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "beverage_id", nullable = false)
    @JsonIgnore
    lateinit var beverage: Beverage

}

