package com.qinet.feastique.model.entity.sales

import com.qinet.feastique.model.entity.provisions.beverage.Beverage
import com.qinet.feastique.model.entity.order.beverage.BeverageOrderItem
import jakarta.persistence.Entity
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table

@Entity
@Table(name = "beverage_sales")
class BeverageSale : BaseRecord() {

    @ManyToOne
    @JoinColumn(name = "beverage_id", nullable = false)
    lateinit var beverage: Beverage

    @ManyToOne
    @JoinColumn(name = "beverage_order_item_id")
    lateinit var beverageOrderItem: BeverageOrderItem
}

