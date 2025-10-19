package com.qinet.feastique.repository.addOn

import com.qinet.feastique.model.entity.addOn.AddOn
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface AddOnRepository : JpaRepository<AddOn, UUID> {
    fun findAllByVendorId(vendorId: UUID): List<AddOn>
    fun findAllByIdInAndVendorId(addOnIds: List<UUID>, vendorId: UUID): List<AddOn>
    fun findFirstByAddOnNameIgnoreCaseAndVendorId(addOnName: String, vendorId: UUID): AddOn?
    fun existsByAddOnNameIgnoreCaseAndVendorId(addOnName: String, vendorId: UUID): Boolean
}