package com.qinet.feastique.model.entity.availability

import com.fasterxml.jackson.annotation.JsonBackReference
import com.fasterxml.jackson.annotation.JsonIgnore
import com.github.f4b6a3.uuid.UuidCreator
import com.qinet.feastique.model.entity.consumables.beverage.Beverage
import com.qinet.feastique.model.entity.consumables.dessert.Dessert
import com.qinet.feastique.model.entity.consumables.food.Food
import com.qinet.feastique.model.enums.Day
import jakarta.persistence.*
import java.util.*

@MappedSuperclass
abstract class ItemAvailability {
    @Id
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "uuid")
    var id: UUID = UuidCreator.getTimeOrdered()

    @Column(name = "available_day")
    @Enumerated(EnumType.STRING)
    var availableDay: Day? = null
}

@Entity
@Table(name = "beverage_availability")
class BeverageAvailability : ItemAvailability() {

    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "beverage_id", nullable = false)
    @JsonIgnore
    lateinit var beverage: Beverage
}


@Entity
@Table(name = "dessert_availability")
class DessertAvailability : ItemAvailability() {

    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dessert_id", nullable = false)
    @JsonIgnore
    lateinit var dessert: Dessert
}

@Entity
@Table(name = "food_availability")
class FoodAvailability : ItemAvailability() {

    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "food_id", nullable = false)
    @JsonIgnore
    lateinit var food: Food
}

