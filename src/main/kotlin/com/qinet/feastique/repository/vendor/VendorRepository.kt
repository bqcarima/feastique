package com.qinet.feastique.repository.vendor

import com.qinet.feastique.model.entity.Vendor
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface VendorRepository : JpaRepository<Vendor, Long> {

    fun findByUsername(vendorId: String): Vendor?
    fun findByDefaultPhoneNumber(phoneNumber: String): Vendor?
}