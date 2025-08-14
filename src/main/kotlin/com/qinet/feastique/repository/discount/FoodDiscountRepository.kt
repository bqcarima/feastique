package com.qinet.feastique.repository.discount

import com.qinet.feastique.model.entity.discount.FoodDiscount
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface FoodDiscountRepository : JpaRepository<FoodDiscount, Long> {
    fun deleteAllByDiscountIdInAndDiscount_VendorId(discountIds: List<Long>, vendorId: Long)
}