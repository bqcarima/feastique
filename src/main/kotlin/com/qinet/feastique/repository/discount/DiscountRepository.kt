package com.qinet.feastique.repository.discount

import com.qinet.feastique.model.entity.discount.Discount
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface DiscountRepository : JpaRepository<Discount, UUID> {

    fun findAllByVendorId(vendorId: UUID): List<Discount>
    fun findAllByIdInAndVendorId(discountIds: List<UUID>, vendorId: UUID): List<Discount>
    fun findFirstByDiscountNameIgnoreCaseAndVendorId(discountName: String, vendorId: UUID): Discount?
    fun deleteAllByVendorId(vendorId: UUID)

}