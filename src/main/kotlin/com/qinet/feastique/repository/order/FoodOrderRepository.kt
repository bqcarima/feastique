package com.qinet.feastique.repository.order

import com.qinet.feastique.model.entity.order.FoodOrder
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface FoodOrderRepository : JpaRepository<FoodOrder, Long> {
    fun findAllByCustomerDeletedStatusAndCustomerId(customerDeletedStatus: Boolean, customerId: Long): List<FoodOrder>
    fun findAllByVendorDeletedStatusAndVendorId(vendorDeletedStatus: Boolean, vendorId: Long): List<FoodOrder>

}