package com.qinet.feastique.model.entity.discount

import com.fasterxml.jackson.annotation.JsonBackReference
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonManagedReference
import com.qinet.feastique.model.entity.Vendor
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import java.util.Date

@Entity
@Table(name = "discount")
class Discount {

    @Id
    @GeneratedValue
    var id: Long? = null

    @Column(name = "discount_name")
    @NotBlank(message = "Discount name cannot be null.")
    @NotEmpty(message = "Discount name be empty.")
    var discountName: String? = ""

    @NotBlank(message = "Percentage cannot be null.")
    @NotEmpty(message = "Percentage cannot be empty.")
    var percentage: String? = ""

    var startDate: Date? = null
    var endDate: Date? = null

    @JsonBackReference // prevent infinite recursion for extra protection
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vendor_id", nullable = false)
    @JsonIgnore
    lateinit var vendor: Vendor

    @JsonManagedReference
    @OneToMany(
        mappedBy = "discount",
        cascade = [CascadeType.ALL],
        orphanRemoval = true
    )
    var foodDiscount: MutableSet<FoodDiscount> = mutableSetOf()
}