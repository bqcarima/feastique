package com.qinet.feastique.repository.addOn

import com.qinet.feastique.model.entity.addOn.AddOn
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface AddOnRepository : JpaRepository<AddOn, Long> {
    fun findAllByVendorId(vendorId: Long): List<AddOn>
    fun deleteByIdAndVendorId(id: Long, vendorId: Long)
    fun findAllByIdInAndVendorId(addOnIds: List<Long>, vendorId: Long): List<AddOn>
    fun findByAddOnNameIgnoreCaseAndVendorId(addOnName: String, vendorId: Long): AddOn?
}