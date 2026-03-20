package com.qinet.feastique.model.entity.bookmark

import com.fasterxml.jackson.annotation.JsonIgnore
import com.github.f4b6a3.uuid.UuidCreator
import com.qinet.feastique.model.entity.consumables.beverage.Beverage
import com.qinet.feastique.model.entity.consumables.dessert.Dessert
import com.qinet.feastique.model.entity.consumables.food.Food
import com.qinet.feastique.model.entity.consumables.handheld.Handheld
import com.qinet.feastique.model.entity.user.Customer
import com.qinet.feastique.model.entity.user.Vendor
import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import java.time.LocalDateTime
import java.util.*

@MappedSuperclass
abstract class Bookmark {

    @Id
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "uuid")
    var id: UUID = UuidCreator.getTimeOrdered()

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    @JsonIgnore
    lateinit var customer: Customer

    @Column(name = "created_at")
    @CreationTimestamp
    var createdAt: LocalDateTime? = null
}

@Entity
@Table(name = "beverage_bookmarks")
class BeverageBookmark : Bookmark() {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "beverage_id", nullable = false)
    @JsonIgnore
    lateinit var beverage: Beverage
}

@Entity
@Table(name = "dessert_bookmarks")
class DessertBookmark : Bookmark() {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dessert_id", nullable = false)
    @JsonIgnore
    lateinit var dessert: Dessert
}

@Entity
@Table(name = "food_bookmarks")
class FoodBookmark : Bookmark() {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "food_id", nullable = false)
    @JsonIgnore
    lateinit var food: Food
}

@Entity
@Table(name = "handheld_bookmarks")
class HandheldBookmark : Bookmark() {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "handheld_id", nullable = false)
    @JsonIgnore
    lateinit var handheld: Handheld
}

@Entity
@Table(name = "vendor_bookmarks")
class VendorBookmark : Bookmark() {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vendor_id", nullable = false)
    @JsonIgnore
    lateinit var vendor: Vendor
}

