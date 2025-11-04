package com.qinet.feastique.repository.addOn

import com.qinet.feastique.model.entity.provisions.addOn.FoodAddOn
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface FoodAddOnRepository : JpaRepository<FoodAddOn, UUID> {
    fun findAllByFoodId(foodId: UUID): List<FoodAddOn>
}