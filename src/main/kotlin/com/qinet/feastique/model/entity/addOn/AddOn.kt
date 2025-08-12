package com.qinet.feastique.model.entity.addOn

import com.fasterxml.jackson.annotation.JsonBackReference
import com.fasterxml.jackson.annotation.JsonIgnore
import com.qinet.feastique.model.entity.Vendor
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
import org.hibernate.annotations.Fetch

@Entity
@Table(name = "add_on")
class AddOn {
    @Id
    @GeneratedValue
    var id: Long? = null

    @Column(name = "add_on_name")
    @NotBlank(message = "Name cannot be null.")
    @NotEmpty(message = "Name cannot be empty.")
    var addOnName: String? = ""

    @NotBlank(message = "Price cannot be null.")
    @NotEmpty(message = "Price cannot be empty.")
    var price: Long? = 0

    @JsonBackReference // prevent infinite recursion for extra protection
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vendor_id", nullable = false)
    @JsonIgnore
    lateinit var vendor: Vendor

}