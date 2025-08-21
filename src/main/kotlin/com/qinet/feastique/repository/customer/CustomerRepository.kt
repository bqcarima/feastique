package com.qinet.feastique.repository.customer

import com.qinet.feastique.model.entity.Customer
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface CustomerRepository : JpaRepository<Customer, Long> {

    fun findFirstByUsername(username: String): Optional<Customer>

    fun findFirstByDefaultPhoneNumber(defaultPhoneNumber: String) : Optional<Customer>

}