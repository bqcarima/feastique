package com.qinet.feastique.repository.food

import com.qinet.feastique.model.entity.food.FoodOrderType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface FoodOrderTypeRepository : JpaRepository<FoodOrderType, Long> {
    fun findAllByFoodId(foodId: Long): List<FoodOrderType>
    fun findByIdAndFoodId(id: Long, foodId: Long): FoodOrderType?
    fun deleteByIdAndFoodId(id: Long, foodId: Long)
    fun deleteAllByFoodId(foodId: Long)
}