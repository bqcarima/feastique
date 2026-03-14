package com.qinet.feastique.model.entity.review

import com.fasterxml.jackson.annotation.JsonBackReference
import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonIgnore
import com.github.f4b6a3.uuid.UuidCreator
import com.qinet.feastique.model.entity.consumables.beverage.Beverage
import com.qinet.feastique.model.entity.consumables.dessert.Dessert
import com.qinet.feastique.model.entity.consumables.food.Food
import com.qinet.feastique.model.entity.consumables.handheld.Handheld
import com.qinet.feastique.model.entity.order.Order
import com.qinet.feastique.model.entity.order.item.BeverageOrderItem
import com.qinet.feastique.model.entity.order.item.DessertOrderItem
import com.qinet.feastique.model.entity.order.item.FoodOrderItem
import com.qinet.feastique.model.entity.order.item.HandheldOrderItem
import com.qinet.feastique.model.entity.user.Customer
import com.qinet.feastique.model.entity.user.Vendor
import jakarta.persistence.*
import jakarta.validation.constraints.NotNull
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.LocalDateTime
import java.util.*

@MappedSuperclass
abstract class Review {

    @Id
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "uuid")
    var id: UUID = UuidCreator.getTimeOrdered()

    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    @JsonIgnore
    lateinit var order: Order

    @Column(nullable = true)
    var review: String? = ""

    @NotNull(message = "Review must not be empty")
    var rating: Float? = 0.0F

    @Column(name = "created_at", nullable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    @CreationTimestamp
    var createdAt: LocalDateTime? = null

    @Column(name = "updated_at", nullable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    @UpdateTimestamp
    var updatedAt: LocalDateTime? = null

    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    @JsonIgnore
    lateinit var customer: Customer
}

@Entity
@Table(name = "beverage_reviews")
class BeverageReview : Review() {

    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "beverage_id", nullable = false)
    @JsonIgnore
    lateinit var beverage: Beverage

    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "beverage_order_item_id", nullable = false)
    @JsonIgnore
    lateinit var beverageOrderItem: BeverageOrderItem
}

@Entity
@Table(name = "dessert_reviews")
class DessertReview : Review() {

    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dessert_id", nullable = false)
    @JsonIgnore
    lateinit var dessert: Dessert

    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dessert_order_item_id", nullable = false)
    @JsonIgnore
    lateinit var dessertOrderItem: DessertOrderItem
}

@Entity
@Table(name = "food_reviews")
class FoodReview : Review() {

    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "food_id", nullable = false)
    @JsonIgnore
    lateinit var food: Food

    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "food_order_item_id", nullable = false)
    @JsonIgnore
    lateinit var foodOrderItem: FoodOrderItem
}

@Entity
@Table(name = "handheld_reviews")
class HandheldReview : Review() {

    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "handheld_id", nullable = false)
    @JsonIgnore
    lateinit var handheld: Handheld

    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "handheld_order_item_id", nullable = false)
    @JsonIgnore
    lateinit var handheldOrderItem: HandheldOrderItem
}

@Entity
@Table(name = "vendor_reviews")
class VendorReview : Review() {

    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vendor_id", nullable = false)
    @JsonIgnore
    lateinit var vendor: Vendor
}

