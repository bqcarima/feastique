package com.qinet.feastique.repository.addOn

import com.qinet.feastique.model.entity.addOn.FoodOrderAddOn
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface FoodOrderAddOnRepository : JpaRepository<FoodOrderAddOn, Long> {
    fun findAllByFoodOrderId(foodOrderId: Long): List<FoodOrderAddOn>
}