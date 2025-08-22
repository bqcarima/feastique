package com.qinet.feastique.repository.customer

import com.qinet.feastique.model.entity.phoneNumber.CustomerPhoneNumber
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface CustomerPhoneNumberRepository : JpaRepository<CustomerPhoneNumber, Long> {

    fun existsByPhoneNumber(phoneNumber: String): Boolean
}