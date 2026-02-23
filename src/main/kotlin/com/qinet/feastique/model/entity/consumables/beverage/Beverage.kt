package com.qinet.feastique.model.entity.consumables.beverage

import com.fasterxml.jackson.annotation.JsonBackReference
import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonManagedReference
import com.qinet.feastique.model.entity.Menu
import com.qinet.feastique.model.entity.consumables.EdibleEntity
import com.qinet.feastique.model.entity.consumables.flavour.BeverageFlavour
import com.qinet.feastique.model.entity.discount.BeverageDiscount
import com.qinet.feastique.model.entity.image.BeverageImage
import com.qinet.feastique.model.enums.BeverageGroup
import com.qinet.feastique.model.enums.Day
import com.qinet.feastique.model.enums.OrderType
import jakarta.persistence.*
import jakarta.validation.constraints.NotNull
import java.time.LocalTime

@Entity
@Table(name = "beverages")
class Beverage : EdibleEntity() {

    @Column(name = "ready_as_from", nullable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "hh:mm a")
    var readyAsFrom: LocalTime? = null

    @ElementCollection(targetClass = Day::class)
    @CollectionTable(
        name = "beverage_available_days",
        joinColumns = [JoinColumn(name = "beverage_id")]
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "day")
    var availableDays: MutableSet<Day> = mutableSetOf()

    @ElementCollection(targetClass = OrderType::class)
    @CollectionTable(
        name = "beverage_order_types",
        joinColumns = [JoinColumn(name = "beverage_id")]
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "order_types")
    var orderTypes: MutableSet<OrderType> = mutableSetOf()

    @NotNull(message = "Alcohol status cannot be empty.")
    var alcoholic: Boolean? = null

    var percentage: Int? = 0

    @Column(name = "delivery_fee")
    var deliveryFee: Long? = 0

    @Column(name = "beverage_group")
    @NotNull(message = "Beverage group cannot be empty.")
    @Enumerated(EnumType.STRING)
    var beverageGroup: BeverageGroup? = null

    @JsonBackReference
    @OneToMany(
        mappedBy = "beverage",
        cascade = [CascadeType.ALL],
        orphanRemoval = true
    )
    var beverageImages: MutableSet<BeverageImage> = mutableSetOf()

    @JsonBackReference
    @OneToMany(
        mappedBy = "beverage",
        cascade = [CascadeType.ALL],
        orphanRemoval = true
    )
    var beverageFlavours: MutableSet<BeverageFlavour> = mutableSetOf()

    @JsonBackReference
    @OneToMany(
        mappedBy = "beverage",
        cascade = [CascadeType.ALL],
        orphanRemoval = true
    )
    var beverageDiscounts: MutableSet<BeverageDiscount> = mutableSetOf()

    @JsonManagedReference
    @OneToOne(
        mappedBy = "beverage",
        cascade = [CascadeType.ALL],
        orphanRemoval = true
    )
    var menu: Menu? = null
}

