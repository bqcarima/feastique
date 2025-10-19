package com.qinet.feastique.repository.order

import com.qinet.feastique.model.entity.order.beverage.BeverageOrderItem
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface BeverageOrderItemRepository : JpaRepository<BeverageOrderItem, UUID>