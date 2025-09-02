package com.qinet.feastique.repository.order

import com.qinet.feastique.model.entity.order.FoodOrder
import com.qinet.feastique.model.enums.OrderStatus
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface FoodOrderRepository : JpaRepository<FoodOrder, Long> {
    fun findAllByCustomerDeletedStatusAndCustomerId(customerDeletedStatus: Boolean, customerId: Long): List<FoodOrder>
    fun findAllByVendorDeletedStatusAndVendorId(vendorDeletedStatus: Boolean, vendorId: Long): List<FoodOrder>
    fun findByIdAndCustomerIdAndOrderStatus(id: Long, customerId: Long, orderStatus: OrderStatus): FoodOrder?
    fun findByIdAndCustomerIdAndCustomerDeletedStatus(id: Long, customerId: Long, customerDeletedStatus: Boolean): FoodOrder?
    fun findByIdAndVendorIdAndCustomerDeletedStatus(id: Long, vendorId: Long, customerDeletedStatus: Boolean): FoodOrder?

    @EntityGraph("FoodOrder.withAllRelations")
    @Query("SELECT f FROM FoodOrder f WHERE f.id = :id")
    fun findByIdWithAllRelations(@Param("id") id: Long): Optional<FoodOrder>
}