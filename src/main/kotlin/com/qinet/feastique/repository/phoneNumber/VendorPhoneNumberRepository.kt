package com.qinet.feastique.repository.phoneNumber

import com.qinet.feastique.model.entity.phoneNumber.VendorPhoneNumber
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface VendorPhoneNumberRepository : JpaRepository<VendorPhoneNumber, Long> {

    fun findAllByVendorId(vendorId: Long): List<VendorPhoneNumber>
    fun existsByPhoneNumber(phoneNumber: String): Boolean
}