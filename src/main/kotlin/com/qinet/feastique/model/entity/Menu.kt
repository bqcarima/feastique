package com.qinet.feastique.model.entity

import com.fasterxml.jackson.annotation.JsonBackReference
import com.fasterxml.jackson.annotation.JsonIgnore
import com.qinet.feastique.model.entity.food.Food
import com.qinet.feastique.model.enums.Availability
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToOne
import jakarta.persistence.PostLoad
import jakarta.persistence.Table

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

