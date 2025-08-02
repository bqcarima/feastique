package com.qinet.feastique.repository

import com.qinet.feastique.model.entity.Customer
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface CustomerRepository : JpaRepository<Customer, Long> {

    fun findByUsername(username: String): Optional<Customer>

    fun findByDefaultPhoneNumber(defaultPhoneNumber: String) : Optional<Customer>

}