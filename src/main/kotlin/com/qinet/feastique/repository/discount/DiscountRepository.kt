package com.qinet.feastique.repository.discount

import com.qinet.feastique.model.entity.discount.Discount
import org.springframework.data.jpa.repository.JpaRepository

interface DiscountRepository : JpaRepository<Discount, Long> {

    fun findAllByVendorId(vendorId: Long): List<Discount>
    fun findAllByIdInAndVendorId(discountIds: List<Long>, vendorId: Long): List<Discount>
    fun findByDiscountNameIgnoreCaseAndVendorId(discountName: String, vendorId: Long): Discount?
    fun deleteAllByVendorId(vendorId: Long)

}