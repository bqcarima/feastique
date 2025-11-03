package com.qinet.feastique.repository.order.orderItem

import com.qinet.feastique.model.entity.order.food.FoodCartItem
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface FoodCartRepository : JpaRepository<FoodCartItem, UUID>