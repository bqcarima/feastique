package com.qinet.feastique.model.entity.address

import com.fasterxml.jackson.annotation.JsonBackReference
import com.fasterxml.jackson.annotation.JsonIgnore
import com.qinet.feastique.model.entity.user.Vendor
import jakarta.persistence.Entity
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToOne
import jakarta.persistence.Table

@Entity
@Table(name = "vendor_address")
class VendorAddress : Address() {

    @JsonBackReference // prevent infinite recursion for extra protection
    @OneToOne
    @JoinColumn(name = "vendor_id", nullable = false)
    @JsonIgnore
    lateinit var vendor: Vendor
}

