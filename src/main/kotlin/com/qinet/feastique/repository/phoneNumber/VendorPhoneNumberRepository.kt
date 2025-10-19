package com.qinet.feastique.repository.phoneNumber

import com.qinet.feastique.model.entity.phoneNumber.VendorPhoneNumber
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface VendorPhoneNumberRepository : JpaRepository<VendorPhoneNumber, UUID> {

    fun findAllByVendorId(vendorId: UUID): List<VendorPhoneNumber>
    fun existsByPhoneNumber(phoneNumber: String): Boolean
}