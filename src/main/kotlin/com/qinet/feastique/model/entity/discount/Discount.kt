package com.qinet.feastique.model.entity.discount

import com.fasterxml.jackson.annotation.*
import com.github.f4b6a3.uuid.UuidCreator
import com.qinet.feastique.model.entity.order.item.FoodCartItem
import com.qinet.feastique.model.entity.order.item.FoodOrderItem
import com.qinet.feastique.model.entity.consumables.beverage.Beverage
import com.qinet.feastique.model.entity.consumables.dessert.Dessert
import com.qinet.feastique.model.entity.consumables.food.Food
import com.qinet.feastique.model.entity.order.item.*
import com.qinet.feastique.model.entity.user.Vendor
import jakarta.persistence.*
import jakarta.validation.constraints.*
import java.util.*

@Entity
@Table(name = "discounts")
class Discount {

    @Id
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "uuid")
    var id: UUID = UuidCreator.getTimeOrdered()

    @Column(name = "discount_name")
    @NotBlank
    @NotEmpty(message = "Discount name be empty.")
    var discountName: String? = ""

    @NotNull(message = "Percentage cannot be null.")
    var percentage: Int? = 1

    @Column(name = "start_date")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    var startDate: Date? = null

    @Column(name = "end_date")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    var endDate: Date? = null

    @JsonBackReference // prevent infinite recursion for extra protection
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vendor_id", nullable = false)
    @JsonIgnore
    lateinit var vendor: Vendor
}

@Entity
@Table(name = "applied_discounts")
class AppliedDiscount {

    @Id
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "uuid")
    var id: UUID = UuidCreator.getTimeOrdered()

    @ManyToOne
    @JoinColumn(name = "discount_id")
    lateinit var discount: Discount

    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY, cascade = [])
    @JoinColumn(name = "beverage_cart_item_id")
    var beverageCartItem: BeverageCartItem? = null

    @ManyToOne(cascade = [])
    @JoinColumn(name = "beverage_order_item_id")
    var beverageOrderItem: BeverageOrderItem? = null

    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY, cascade = [])
    @JoinColumn(name = "dessert_cart_item_id")
    var dessertCartItem: DessertCartItem? = null

    @ManyToOne(cascade = [])
    @JoinColumn(name = "dessert_order_item_id")
    var dessertOrderItem: DessertOrderItem? = null

    @ManyToOne(cascade = [])
    @JoinColumn(name = "food_order_item_id")
    var foodOrderItem: FoodOrderItem? = null

    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY, cascade = [])
    @JoinColumn(name = "food_cart_item_id")
    @JsonIgnore
    var foodCartItem: FoodCartItem? = null
}

@Entity
@Table(name = "beverage_discounts")
class BeverageDiscount {

    @Id
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "uuid")
    var id: UUID = UuidCreator.getTimeOrdered()

    @JsonBackReference // prevent infinite recursion for extra protection
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "discount_id", nullable = false)
    @JsonIgnore
    lateinit var discount: Discount

    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "beverage_id", nullable = false)
    @JsonIgnore
    lateinit var beverage: Beverage
}

@Entity
@Table(name = "dessert_discounts")
class DessertDiscount {

    @Id
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "uuid")
    var id: UUID = UuidCreator.getTimeOrdered()

    @JsonBackReference // prevent infinite recursion for extra protection
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "discount_id", nullable = false)
    @JsonIgnore
    lateinit var discount: Discount

    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dessert_id", nullable = false)
    @JsonIgnore
   var dessert: Dessert? = null
}

@Entity
@Table(name = "food_discounts")
class FoodDiscount {

    @Id
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "uuid")
    var id: UUID = UuidCreator.getTimeOrdered()

    @JsonBackReference // prevent infinite recursion for extra protection
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "discount_id", nullable = false)
    @JsonIgnore
    lateinit var discount: Discount

    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "food_id", nullable = false)
    @JsonIgnore
    lateinit var food: Food

}

