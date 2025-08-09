package com.qinet.feastique.repository.complement

import com.qinet.feastique.model.entity.complement.FoodComplement
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface FoodComplementRepository : JpaRepository<FoodComplement, Long> {
    fun findAllByFoodId(foodId: Long): List<FoodComplement>
    fun deleteByComplementIdAndFoodId(complementId: Long, foodId: Long)
    fun deleteAllByFoodId(foodId: Long)
}

