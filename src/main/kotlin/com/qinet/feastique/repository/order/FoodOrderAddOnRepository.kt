package com.qinet.feastique.repository.order

import com.qinet.feastique.model.entity.addOn.OrderAddOn
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface FoodOrderAddOnRepository : JpaRepository<OrderAddOn, Long> {
    fun findAllByFoodOrderId(foodOrderId: Long): List<OrderAddOn>
}