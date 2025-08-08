package com.qinet.feastique.repository.food

import com.qinet.feastique.model.entity.food.FoodSize
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface FoodSizeRepository : JpaRepository<FoodSize, Long> {
    fun findAllByFoodId(foodId: Long): List<FoodSize>
    fun findByIdAndFoodId(id: Long, foodId: Long): FoodSize?
    fun findAllByIdInAndFoodId(sizeId: List<Long>, foodId: Long): List<FoodSize>
    fun deleteByIdAndFoodId(id: Long, foodId: Long)
    fun deleteAllByFoodId(foodId: Long)
}