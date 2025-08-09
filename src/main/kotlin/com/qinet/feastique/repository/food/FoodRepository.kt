package com.qinet.feastique.repository.food

import com.qinet.feastique.model.entity.food.Food
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface FoodRepository : JpaRepository<Food, Long> {
    fun deleteByIdAndVendorId(foodId: Long, vendorId: Long)
    fun findAllByVendorId(vendorId: Long): List<Food>
    fun findByFoodNameIgnoreCaseAndVendorId(foodName: String, vendorId: Long): Food?

}