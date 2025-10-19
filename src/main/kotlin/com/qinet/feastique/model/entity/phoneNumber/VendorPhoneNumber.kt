package com.qinet.feastique.model.entity.phoneNumber

import com.fasterxml.jackson.annotation.JsonBackReference
import com.fasterxml.jackson.annotation.JsonIgnore
import com.qinet.feastique.model.entity.user.Vendor
import jakarta.persistence.*


@Entity
@Table(name = "vendor_phone_numbers")
class VendorPhoneNumber : PhoneNumber() {

    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vendor_id", nullable = false)
    @JsonIgnore
    lateinit var vendor: Vendor
}