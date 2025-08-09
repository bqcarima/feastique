package com.qinet.feastique.repository.addOn

import com.qinet.feastique.model.entity.addOn.FoodAddOn
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface FoodAddOnRepository : JpaRepository<FoodAddOn, Long> {
    fun findAllByFoodId(foodId: Long): List<FoodAddOn>
    fun deleteByAddOnIdAndFoodId(addOnId: Long, foodId: Long)
    fun deleteAllByFoodId(foodId: Long)
    fun deleteAllByAddOnId(addOnId: Long)
}