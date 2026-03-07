package com.qinet.feastique.model.entity.sales

import com.fasterxml.jackson.annotation.JsonFormat
import com.github.f4b6a3.uuid.UuidCreator
import com.qinet.feastique.model.entity.order.item.BeverageOrderItem
import com.qinet.feastique.model.entity.order.item.FoodOrderItem
import com.qinet.feastique.model.entity.consumables.addOn.AddOn
import com.qinet.feastique.model.entity.consumables.beverage.Beverage
import com.qinet.feastique.model.entity.consumables.complement.Complement
import com.qinet.feastique.model.entity.consumables.dessert.Dessert
import com.qinet.feastique.model.entity.consumables.food.Food
import com.qinet.feastique.model.entity.consumables.handheld.Handheld
import com.qinet.feastique.model.entity.order.item.DessertOrderItem
import com.qinet.feastique.model.entity.order.item.HandheldOrderItem
import com.qinet.feastique.model.entity.user.Vendor
import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.*

@MappedSuperclass
abstract class ItemRecord {

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

@Entity
@Table(name = "add_on_sales")
class AddOnSale : ItemRecord() {

    @ManyToOne
    @JoinColumn
    lateinit var addOn: AddOn

    @ManyToOne
    @JoinColumn(name = "food_order_item_id")
    lateinit var foodOrderItem: FoodOrderItem
}


@Entity
@Table(name = "beverage_sales")
class BeverageSale : ItemRecord() {

    @ManyToOne
    @JoinColumn(name = "beverage_id", nullable = false)
    lateinit var beverage: Beverage

    @ManyToOne
    @JoinColumn(name = "beverage_order_item_id")
    lateinit var beverageOrderItem: BeverageOrderItem
}


@Entity
@Table(name = "complement_sales")
class ComplementSale : ItemRecord() {

    @ManyToOne
    @JoinColumn(name = "complement_id", nullable = false)
    lateinit var complement: Complement

    @ManyToOne
    @JoinColumn(name = "food_order_item_id")
    lateinit var foodOrderItem: FoodOrderItem
}

@Entity
@Table(name = "dessert_sales")
class DessertSale : ItemRecord() {
    @ManyToOne
    @JoinColumn(name = "dessert_id", nullable = false)
    lateinit var dessert: Dessert

    @ManyToOne
    @JoinColumn(name = "dessert_order_item_id")
    lateinit var dessertOrderItem: DessertOrderItem
}

@Entity
@Table(name = "food_sales")
class FoodSale : ItemRecord() {

    @ManyToOne
    @JoinColumn(name = "food_id", nullable = false)
    lateinit var food: Food

    @ManyToOne
    @JoinColumn(name = "food_order_item_id", nullable = false)
    lateinit var foodOrderItem: FoodOrderItem
}

@Entity
@Table(name = "handheld_sales")
class HandheldSale : ItemRecord() {

    @ManyToOne
    @JoinColumn(name = "handheld_id", nullable = false)
    lateinit var handheld: Handheld

    @ManyToOne
    @JoinColumn(name = "handheld_order_item_id", nullable = false)
    lateinit var handheldOrderItem: HandheldOrderItem
}

