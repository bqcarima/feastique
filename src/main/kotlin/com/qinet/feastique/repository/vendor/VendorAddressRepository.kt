package com.qinet.feastique.repository.vendor

import com.qinet.feastique.model.entity.address.VendorAddress
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface VendorAddressRepository : JpaRepository<VendorAddress, Long> {
    fun findAllByVendorId(vendorId: Long): List<VendorAddress>

}