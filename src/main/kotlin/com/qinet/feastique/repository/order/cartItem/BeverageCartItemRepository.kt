package com.qinet.feastique.repository.order.cartItem

import com.qinet.feastique.model.entity.order.beverage.BeverageCartItem
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface BeverageCartItemRepository : JpaRepository<BeverageCartItem, UUID>