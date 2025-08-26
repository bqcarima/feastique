package com.qinet.feastique.model.entity.phoneNumber

import com.fasterxml.jackson.annotation.JsonBackReference
import com.fasterxml.jackson.annotation.JsonIgnore
import com.qinet.feastique.model.entity.user.Customer
import jakarta.persistence.*

@Entity
@Table(name = "customer_phone_number")
class CustomerPhoneNumber : PhoneNumber() {

    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    @JsonIgnore
    lateinit var customer: Customer
}