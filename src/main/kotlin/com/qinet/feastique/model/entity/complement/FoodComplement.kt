package com.qinet.feastique.model.entity.complement

import com.fasterxml.jackson.annotation.JsonBackReference
import com.fasterxml.jackson.annotation.JsonIgnore
import com.qinet.feastique.model.entity.food.Food
import jakarta.persistence.*

@Entity
@Table(name = "food_complement")
class FoodComplement {

    @Id
    @GeneratedValue
    var id: Long? = null

    @JsonBackReference // prevent infinite recursion for extra protection
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "complement_id", nullable = false)
    @JsonIgnore
    lateinit var complement: Complement

    @JsonBackReference // prevent infinite recursion for extra protection
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "food_id", nullable = false)
    @JsonIgnore
    lateinit var food: Food

}