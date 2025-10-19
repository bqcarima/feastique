package com.qinet.feastique.repository.food

import com.qinet.feastique.model.entity.food.Food
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.Optional
import java.util.UUID

@Repository
interface FoodRepository : JpaRepository<Food, UUID> {
    fun findAllByVendorId(vendorId: UUID): List<Food>
    fun findFirstByFoodNameIgnoreCaseAndVendorId(foodName: String, vendorId: UUID): Food?

    @EntityGraph("Vendor.withAllRelations")
    @Query("SELECT f FROM Food f WHERE f.id = :id")
    fun findByIdWithAllRelations(@Param("id") id: UUID): Optional<Food>
}

