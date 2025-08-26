package com.qinet.feastique.model.entity

import com.fasterxml.jackson.annotation.JsonBackReference
import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull

@Entity
@Table(name = "beverage")
class Beverage {

    @Id
    @GeneratedValue
    var id: Long? = null

    @Column(name = "name")
    @NotBlank(message = "Beverage name cannot be null.")
    @NotEmpty(message = "Beverage name cannot be empty.")
    var beverageName: String? = ""

    @NotNull(message = "Type status cannot be null.")
    var alcoholic: Boolean? = null

    var percentage: Int? = 0

    @Column(name = "beverage_group")
    @NotBlank(message = "Beverage group cannot be null.")
    @NotEmpty(message = "Beverage group cannot be empty.")
    var beverageGroup: String? = ""

    var price: Long? = 0

    @NotNull(message = "Delivery availability cannot be null.")
    var delivery: Boolean? = null

    @JsonBackReference // prevent infinite recursion for extra protection
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vendor_id", nullable = false)
    @JsonIgnore
    lateinit var vendor: com.qinet.feastique.model.entity.user.Vendor
}

