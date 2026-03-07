package com.qinet.feastique.model.entity.consumables.handheld

import com.fasterxml.jackson.annotation.JsonBackReference
import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonManagedReference
import com.qinet.feastique.model.entity.Menu
import com.qinet.feastique.model.entity.consumables.EdibleEntity
import com.qinet.feastique.model.entity.consumables.filling.HandheldFilling
import com.qinet.feastique.model.entity.discount.HandheldDiscount
import com.qinet.feastique.model.entity.image.HandheldImage
import com.qinet.feastique.model.entity.size.HandheldSize
import com.qinet.feastique.model.enums.Day
import com.qinet.feastique.model.enums.HandHeldType
import com.qinet.feastique.model.enums.OrderType
import jakarta.persistence.*
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull
import java.time.LocalTime

@Entity
@Table(name = "handhelds")
class Handheld : EdibleEntity() {

    @Column(name = "handheld_number", nullable = false, unique = true)
    var handheldNumber: String? = null

    @Column(name = "ready_as_from", nullable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "hh:mm a")
    var readyAsFrom: LocalTime? = null

    @ElementCollection(targetClass = Day::class)
    @CollectionTable(
        name = "handheld_available_days",
        joinColumns = [JoinColumn(name = "handheld_id")]
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "day")
    var availableDays: MutableSet<Day> = mutableSetOf()

    @ElementCollection(targetClass = OrderType::class)
    @CollectionTable(
        name = "handheld_order_types",
        joinColumns = [JoinColumn(name = "handheld_id")]
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "order_types")
    var orderTypes: MutableSet<OrderType> = mutableSetOf()

    @NotEmpty(message = "Description cannot be empty.")
    @NotNull(message = "Description cannot be empty.")
    var description: String? = null

    @Column(name = "delivery_fee", nullable = false)
    var deliveryFee : Long? = 0L

    @Column(name = "handheld_type", nullable = false)
    @Enumerated(EnumType.STRING)
    var handHeldType: HandHeldType? = null

    @JsonBackReference
    @OneToMany(
        mappedBy = "handheld",
        cascade = [CascadeType.ALL],
        orphanRemoval = true
    )
    var handheldImages: MutableSet<HandheldImage> = mutableSetOf()

    @JsonBackReference
    @OneToMany(
        mappedBy = "handheld",
        cascade = [CascadeType.ALL],
        orphanRemoval = true
    )
    var handheldFillings: MutableSet<HandheldFilling> = mutableSetOf()

    @JsonBackReference
    @OneToMany(
        mappedBy = "handheld",
        cascade = [CascadeType.ALL],
        orphanRemoval = true
    )
    var handheldDiscounts: MutableSet<HandheldDiscount> = mutableSetOf()

    @JsonBackReference
    @OneToMany(
        mappedBy = "handheld",
        cascade = [CascadeType.ALL],
        orphanRemoval = true
    )
    var handheldSizes: MutableSet<HandheldSize> = mutableSetOf()

    @JsonManagedReference
    @OneToOne(
        mappedBy = "handheld",
        cascade = [CascadeType.ALL],
        orphanRemoval = true
    )
    var menu: Menu? = null
}

