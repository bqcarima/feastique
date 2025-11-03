package com.qinet.feastique.repository.order.cartItem

import com.qinet.feastique.model.entity.order.food.FoodOrderItem
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface FoodCartItemRepository : JpaRepository<FoodOrderItem, UUID>