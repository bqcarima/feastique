package com.qinet.feastique.model.entity

import com.fasterxml.jackson.annotation.JsonBackReference
import com.fasterxml.jackson.annotation.JsonIgnore
import com.qinet.feastique.model.entity.food.Food
import jakarta.persistence.*

@Entity
@Table(name = "menu")
class Menu {
    @Id
    @GeneratedValue
    var id: Long? = null

    @JsonBackReference
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "food_id", nullable = false)
    @JsonIgnore
    lateinit var food: Food

    var delivery: Boolean? = false

    @Column(name = "dine_in")
    var dineIn: Boolean? = false

    var takeaway: Boolean? = false

    @PostLoad
    fun resetFlags() {
        delivery = false
        dineIn = false
        takeaway = false
    }

}

