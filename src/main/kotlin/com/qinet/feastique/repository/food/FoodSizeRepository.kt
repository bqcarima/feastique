package com.qinet.feastique.repository.food

import com.qinet.feastique.model.entity.food.FoodSize
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface FoodSizeRepository : JpaRepository<FoodSize, Long> {
    fun findAllByFoodId(foodId: Long): List<FoodSize>
}