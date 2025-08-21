package com.qinet.feastique.repository.phoneNumber

import com.qinet.feastique.model.entity.phoneNumber.VendorPhoneNumber
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface VendorPhoneNumberRepository : JpaRepository<VendorPhoneNumber, String> {

    fun findAllByVendorId(vendorId: Long): List<VendorPhoneNumber>
    fun findFirstByPhoneNumber(phoneNumber: String): VendorPhoneNumber?
}