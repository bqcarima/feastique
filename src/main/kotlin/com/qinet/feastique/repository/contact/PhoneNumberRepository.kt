package com.qinet.feastique.repository.contact

import com.qinet.feastique.model.entity.contact.CustomerPhoneNumber
import com.qinet.feastique.model.entity.contact.VendorPhoneNumber
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface CustomerPhoneNumberRepository : JpaRepository<CustomerPhoneNumber, UUID> {

    fun existsByPhoneNumber(phoneNumber: String): Boolean
    fun findAllByCustomerId(customerId: UUID): List<CustomerPhoneNumber>
}

@Repository
interface VendorPhoneNumberRepository : JpaRepository<VendorPhoneNumber, UUID> {

    fun findAllByVendorId(vendorId: UUID): List<VendorPhoneNumber>
    fun existsByPhoneNumber(phoneNumber: String): Boolean
}

