package com.qinet.feastique.model.entity.provisions.beverage

import com.qinet.feastique.model.entity.provisions.BaseEntity
import com.qinet.feastique.model.enums.BeverageGroup
import jakarta.persistence.*
import jakarta.validation.constraints.NotNull

@Entity
@Table(name = "beverages")
class Beverage : BaseEntity() {

    @NotNull(message = "Type status cannot be null.")
    var alcoholic: Boolean? = null

    var percentage: Int? = 0

    @Column(name = "beverage_group")
    @NotNull(message = "Beverage group cannot be empty.")
    @Enumerated(EnumType.STRING)
    var beverageGroup: BeverageGroup? = null

    @NotNull(message = "Delivery availability cannot be null.")
    var delivery: Boolean? = null
}

