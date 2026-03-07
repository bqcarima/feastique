package com.qinet.feastique.model.entity.order.orderCategory

import com.fasterxml.jackson.annotation.JsonBackReference
import com.fasterxml.jackson.annotation.JsonIgnore
import com.github.f4b6a3.uuid.UuidCreator
import com.qinet.feastique.model.entity.consumables.beverage.Beverage
import com.qinet.feastique.model.entity.consumables.dessert.Dessert
import com.qinet.feastique.model.entity.consumables.food.Food
import com.qinet.feastique.model.entity.consumables.handheld.Handheld
import com.qinet.feastique.model.enums.OrderType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.MappedSuperclass
import jakarta.persistence.Table
import jakarta.validation.constraints.NotNull
import java.util.UUID

@MappedSuperclass
abstract class OrderCategory {

    @Id
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "uuid")
    var id: UUID = UuidCreator.getTimeOrdered()

    @Column(name = "order_category", nullable = false)
    @NotNull(message = "OrderType name cannot be empty.")
    @Enumerated(EnumType.STRING)
    var orderType: OrderType? = null

}

@Entity
@Table(name = "beverage_order_category")
class BeverageOrderCategory : OrderCategory() {

    @JsonBackReference // prevent infinite recursion for extra protection
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "beverage_id", nullable = false)
    @JsonIgnore
    lateinit var beverage: Beverage
}

@Entity
@Table(name = "dessert_order_category")
class DessertOrderCategory : OrderCategory() {

    @JsonBackReference // prevent infinite recursion for extra protection
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dessert_id", nullable = false)
    @JsonIgnore
    lateinit var dessert: Dessert
}

@Entity
@Table(name = "food_order_category")
class FoodOrderCategory : OrderCategory() {
    @JsonBackReference // prevent infinite recursion for extra protection
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "food_id", nullable = false)
    @JsonIgnore
    lateinit var food: Food
}

@Entity
@Table(name = "handheld_order_category")
class HandheldOrderCategory : OrderCategory() {
    @JsonBackReference // prevent infinite recursion for extra protection
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "handheld_id", nullable = false)
    @JsonIgnore
    lateinit var handheld: Handheld
}