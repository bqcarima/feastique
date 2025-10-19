package com.qinet.feastique.model.entity.address

import com.fasterxml.jackson.annotation.JsonBackReference
import com.fasterxml.jackson.annotation.JsonIgnore
import com.qinet.feastique.model.entity.user.Customer
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table

@Entity
@Table(name = "customer_address")
class CustomerAddress : Address() {

    @Column(name = "is_default")
    var default: Boolean? = false

    @JsonBackReference // prevent infinite recursion for extra protection
    @ManyToOne
    @JoinColumn(name = "customer_id", nullable = false)
    @JsonIgnore
    lateinit var customer: Customer
}

