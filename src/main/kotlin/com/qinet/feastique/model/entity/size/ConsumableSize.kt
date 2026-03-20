package com.qinet.feastique.model.entity.size

import com.fasterxml.jackson.annotation.JsonBackReference
import com.fasterxml.jackson.annotation.JsonIgnore
import com.github.f4b6a3.uuid.UuidCreator
import com.qinet.feastique.model.entity.consumables.flavour.BeverageFlavour
import com.qinet.feastique.model.entity.consumables.flavour.DessertFlavour
import com.qinet.feastique.model.entity.consumables.food.Food
import com.qinet.feastique.model.entity.consumables.handheld.Handheld
import com.qinet.feastique.model.enums.Availability
import com.qinet.feastique.model.enums.Size
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
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull
import java.util.UUID

@MappedSuperclass
abstract class ConsumableSize {

    @Id
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "uuid")
    var id: UUID = UuidCreator.getTimeOrdered()

    @NotNull(message = "Please select at least one consumableSize.")
    var size: Size? = null

    @NotBlank
    @NotEmpty(message = "Food name cannot be empty.")
    var name: String = ""

    @Column(name = "availability")
    @Enumerated(EnumType.STRING)
    var availability: Availability? = null

    @Column
    var isActive: Boolean = true
}

@Entity
@Table(name = "beverage_flavour_sizes")
class BeverageFlavourSize : ConsumableSize() {

    @NotNull(message = "Price cannot be empty.")
    @Column(name = "price", nullable = false)
    var price: Long? = 0

    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "beverage_flavour_id", nullable = false)
    @JsonIgnore
    lateinit var beverageFlavour: BeverageFlavour
}

@Entity
@Table(name = "dessert_flavour_sizes")
class DessertFlavourSize : ConsumableSize() {

    @NotNull(message = "Price cannot be empty.")
    var price: Long? = 0

    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dessert_flavour_id", nullable = false)
    @JsonIgnore
    lateinit var dessertFlavour: DessertFlavour
}


@Entity
@Table(name = "food_sizes")
class FoodSize : ConsumableSize() {

    @Column(name = "price_increase")
    @NotNull(message = "Please enter price increase.")
    var priceIncrease: Long? = 0

    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "food_id", nullable = false)
    @JsonIgnore
    lateinit var food: Food
}

@Entity
@Table(name = "handheld_sizes")
class HandheldSize : ConsumableSize() {

    @Column(name = "number_of_fillings")
    @NotNull(message = "Please enter number of contents.")
    var numberOfFillings: Long? = 0

    @Column(name = "price")
    @NotNull(message = "Price cannot be empty.")
    var price: Long? = 0

    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "handheld_id", nullable = false)
    @JsonIgnore
    lateinit var handheld: Handheld
}

