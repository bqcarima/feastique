package com.qinet.feastique.model.entity.food

import com.fasterxml.jackson.annotation.JsonBackReference
import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.*
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty

@Entity
@Table(name = "food_image")
class FoodImage {

    @Id
    @GeneratedValue
    var id: Long? = null

    @Column(name = "image_url", nullable = false)
    @NotBlank(message = "Image cannot be null.")
    @NotEmpty(message = "Image cannot be empty.")
    var imageUrl: String? = ""

    @JsonBackReference // prevent infinite recursion for extra protection
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "food_id", nullable = false)
    @JsonIgnore
    lateinit var food: Food

}