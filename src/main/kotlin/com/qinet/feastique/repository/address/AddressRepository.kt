package com.qinet.feastique.repository.address

import com.qinet.feastique.model.entity.address.CustomerAddress
import com.qinet.feastique.model.entity.address.VendorAddress
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface CustomerAddressRepository : JpaRepository<CustomerAddress, UUID> {
    fun findByIdAndCustomerIdAndIsActiveTrue(id: UUID, customerId: UUID): CustomerAddress?
    fun findAllByCustomerIdAndIsActiveTrue(customerId: UUID): List<CustomerAddress>
}

@Repository
interface VendorAddressRepository : JpaRepository<VendorAddress, UUID> {
    fun findByIdAndVendorId(id: UUID, vendorId: UUID): VendorAddress?
}
