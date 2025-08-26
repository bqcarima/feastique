package com.qinet.feastique.model.entity.addOn

import com.fasterxml.jackson.annotation.JsonBackReference
import com.fasterxml.jackson.annotation.JsonIgnore
import com.qinet.feastique.model.entity.user.Vendor
import jakarta.persistence.*
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty

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

    @JsonBackReference
    @OneToMany(
        mappedBy = "addOn",
        cascade = [CascadeType.ALL],
        orphanRemoval = true
    )
    @OrderColumn(name = "order_index")
    var foodAddOn: MutableList<FoodAddOn> = mutableListOf()

}

