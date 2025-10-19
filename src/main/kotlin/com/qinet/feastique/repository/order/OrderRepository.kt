package com.qinet.feastique.repository.order

import com.qinet.feastique.model.entity.order.Order
import com.qinet.feastique.model.enums.OrderStatus
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface OrderRepository : JpaRepository<Order, UUID> {
    fun findAllByCustomerDeletedStatusAndCustomerId(customerDeletedStatus: Boolean, customerId: UUID): List<Order>
    fun findAllByVendorDeletedStatusAndVendorId(vendorDeletedStatus: Boolean, vendorId: UUID): List<Order>
    fun findByIdAndCustomerIdAndOrderStatus(id: UUID, customerId: UUID, orderStatus: OrderStatus): Order?
    fun findByIdAndCustomerIdAndCustomerDeletedStatus(id: UUID, customerId: UUID, customerDeletedStatus: Boolean): Order?
    fun findByIdAndVendorIdAndCustomerDeletedStatus(id: UUID, vendorId: UUID, customerDeletedStatus: Boolean): Order?

    @EntityGraph("Order.withAllRelations")
    @Query("SELECT o FROM Order o WHERE o.id = :id")
    fun findByIdWithAllRelations(@Param("id") id: UUID): Optional<Order>
}

