package com.qinet.feastique.model.entity.consumables.addOn

import com.fasterxml.jackson.annotation.JsonBackReference
import com.fasterxml.jackson.annotation.JsonIgnore
import com.github.f4b6a3.uuid.UuidCreator
import com.qinet.feastique.model.entity.consumables.EdibleEntity
import com.qinet.feastique.model.entity.consumables.food.Food
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(name = "add_ons")
class AddOn : EdibleEntity(){

    @Column(name = "price", nullable = false)
    var price: Long? = 0

    @JsonBackReference
    @OneToMany(
        mappedBy = "addOn",
        cascade = [CascadeType.ALL],
        orphanRemoval = false
    )
    var foodAddOn: MutableList<FoodAddOn> = mutableListOf()
}

@Entity
@Table(name = "food_add_ons")
class FoodAddOn {

    @Id
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "uuid")
    var id: UUID = UuidCreator.getTimeOrdered()

    @JsonBackReference // prevent infinite recursion for extra protection
    @ManyToOne
    @JoinColumn(name = "add_on_id", nullable = false)
    @JsonIgnore
    lateinit var addOn: AddOn

    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "food_id", nullable = false)
    @JsonIgnore
    lateinit var food: Food
}

