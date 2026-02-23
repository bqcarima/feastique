package com.qinet.feastique.repository.user

import com.qinet.feastique.model.entity.user.Customer
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface CustomerRepository : JpaRepository<Customer, UUID> {

    fun findFirstByUsername(username: String): Customer?
    fun existsByUsernameIgnoreCase(username: String): Boolean

    @EntityGraph("Customer.withPhoneNumberAndAddress")
    @Query("SELECT c FROM Customer c WHERE c.id = :id")
    fun findByCustomerByIdWithPhoneNumberAndAddress(id: UUID): Customer?
}