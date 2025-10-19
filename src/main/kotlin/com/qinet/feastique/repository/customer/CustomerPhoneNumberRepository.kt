package com.qinet.feastique.repository.customer

import com.qinet.feastique.model.entity.phoneNumber.CustomerPhoneNumber
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface CustomerPhoneNumberRepository : JpaRepository<CustomerPhoneNumber, UUID> {

    fun existsByPhoneNumber(phoneNumber: String): Boolean
    fun findAllByCustomerId(customerId: UUID): List<CustomerPhoneNumber>
}