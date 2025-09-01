package com.qinet.feastique.model.entity.order

import com.fasterxml.jackson.annotation.JsonBackReference
import com.qinet.feastique.model.entity.addOn.OrderAddOn
import com.qinet.feastique.model.entity.beverage.OrderBeverage
import jakarta.persistence.CascadeType
import jakarta.persistence.Entity
import jakarta.persistence.OneToMany
import jakarta.persistence.Table


@Entity
@Table(name = "foodCart")
class FoodCart : OrderEntity() {

    @JsonBackReference
    @OneToMany(
        mappedBy = "foodCart",
        cascade = [CascadeType.ALL],
        orphanRemoval = false
    )
    var orderAddon: MutableList<OrderAddOn> = mutableListOf()

    @JsonBackReference
    @OneToMany(
        mappedBy = "foodCart",
        cascade = [CascadeType.ALL],
        orphanRemoval = false
    )
    var orderBeverage: MutableList<OrderBeverage> = mutableListOf()
}

