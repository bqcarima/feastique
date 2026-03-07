package com.qinet.feastique.model.entity.consumables.dessert

import com.fasterxml.jackson.annotation.JsonBackReference
import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonManagedReference
import com.qinet.feastique.model.entity.Menu
import com.qinet.feastique.model.entity.consumables.EdibleEntity
import com.qinet.feastique.model.entity.consumables.flavour.DessertFlavour
import com.qinet.feastique.model.entity.discount.DessertDiscount
import com.qinet.feastique.model.entity.image.DessertImage
import com.qinet.feastique.model.enums.Day
import com.qinet.feastique.model.enums.DessertType
import com.qinet.feastique.model.enums.OrderType
import jakarta.persistence.*
import jakarta.validation.constraints.NotBlank
import java.time.LocalTime

@Entity
@Table(name = "desserts")
class Dessert : EdibleEntity() {

    @Column(name = "ready_as_from", nullable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "hh:mm a")
    var readyAsFrom: LocalTime? = null

    @ElementCollection(targetClass = Day::class)
    @CollectionTable(
        name = "dessert_available_days",
        joinColumns = [JoinColumn(name = "dessert_id")]
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "day")
    var availableDays: MutableSet<Day> = mutableSetOf()

    @NotBlank(message = "Name cannot be empty.")
    var description: String? = ""

    @Column(name = "delivery_fee", nullable = false)
    var deliveryFee : Long? = 0L
    @Column(name = "dessert_type", nullable = false)
    @Enumerated(EnumType.STRING)
    var dessertType: DessertType? = null

    @JsonBackReference
    @OneToMany(
        mappedBy = "dessert",
        cascade = [CascadeType.ALL],
        orphanRemoval = true
    )
    var dessertImages: MutableSet<DessertImage> = mutableSetOf()

    @JsonBackReference
    @OneToMany(
        mappedBy = "dessert",
        cascade = [CascadeType.ALL],
        orphanRemoval = true
    )
    var dessertFlavours: MutableList<DessertFlavour> = mutableListOf()

    @ElementCollection(targetClass = OrderType::class)
    @CollectionTable(
        name = "dessert_order_types",
        joinColumns = [JoinColumn(name = "dessert_id")]
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "order_type")
    var dessertOrderTypes: MutableSet<OrderType> = mutableSetOf()

    @JsonBackReference
    @OneToMany(
        mappedBy = "dessert",
        cascade = [CascadeType.ALL],
        orphanRemoval = true
    )
    var dessertDiscounts: MutableSet<DessertDiscount> = mutableSetOf()

    @JsonManagedReference
    @OneToOne(
        mappedBy = "dessert",
        cascade = [CascadeType.ALL],
        orphanRemoval = true
    )
    var menu: Menu? = null
}

