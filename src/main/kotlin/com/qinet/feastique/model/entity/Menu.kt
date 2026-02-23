package com.qinet.feastique.model.entity

import com.fasterxml.jackson.annotation.JsonBackReference
import com.fasterxml.jackson.annotation.JsonIgnore
import com.github.f4b6a3.uuid.UuidCreator
import com.qinet.feastique.model.entity.consumables.beverage.Beverage
import com.qinet.feastique.model.entity.consumables.dessert.Dessert
import com.qinet.feastique.model.entity.consumables.food.Food
import jakarta.persistence.*
import java.util.UUID

@Entity
@Table(name = "menu")
class Menu {

    @Id
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "uuid")
    var id: UUID = UuidCreator.getTimeOrdered()

    @JsonBackReference
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "food_id", nullable = true)
    @JsonIgnore
    var food: Food? = null

    @JsonBackReference
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dessert_id", nullable = true)
    @JsonIgnore
    var dessert: Dessert? = null

    @JsonBackReference
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "beverage_id", nullable = true)
    @JsonIgnore
    var beverage: Beverage? = null

    var delivery: Boolean? = false

    @Column(name = "delivery_items_left")
    var deliveryItemsLeft: Int? = null

    @Column(name = "dine_in")
    var dineIn: Boolean? = false

    var pickup: Boolean? = false

   /* @PostLoad
    fun resetFlags() {
        delivery = false
        dineIn = false
        pickup = false
    }*/

    @Suppress("unused")
    @Version
    var version: Long = 0

}

