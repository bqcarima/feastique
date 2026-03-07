package com.qinet.feastique.repository.order

import com.qinet.feastique.model.entity.order.Order
import com.qinet.feastique.model.enums.OrderStatus
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.util.*

@Repository
interface OrderRepository : JpaRepository<Order, UUID> {
    fun findAllByCustomerDeletedAtAndCustomerId(customerDeletedAt: LocalDateTime?, customerId: UUID, pageable: Pageable): Page<Order>
    fun findAllByVendorDeletedAtAndVendorId(vendorDeletedAt: LocalDateTime?, vendorId: UUID, pageable: Pageable): Page<Order>
    fun findByIdAndCustomerIdAndOrderStatus(id: UUID, customerId: UUID, orderStatus: OrderStatus): Order?
    fun findByIdAndCustomerIdAndCustomerDeletedAt(id: UUID, customerId: UUID, customerDeletedAt: LocalDateTime?): Order?
    fun findByIdAndVendorIdAndVendorDeletedAt(id: UUID, vendorId: UUID, vendorDeletedAt: LocalDateTime?): Order?
    fun findByIdAndVendorIdAndOrderStatus(id: UUID, vendorId: UUID, orderStatus: OrderStatus): Order?

    @EntityGraph("Order.withAllRelations")
    @Query("SELECT o FROM Order o WHERE o.id = :id")
    fun findByIdWithAllRelations(@Param("id") id: UUID): Optional<Order>
}

