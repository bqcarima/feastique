package com.qinet.feastique.repository.food

import com.qinet.feastique.model.entity.food.Food
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface FoodRepository : JpaRepository<Food, Long> {
    fun findAllByVendorId(vendorId: Long): List<Food>
    fun findFirstByFoodNameIgnoreCaseAndVendorId(foodName: String, vendorId: Long): Food?

    /*@Query("""
        SELECT DISTINCT f 
        FROM Food f
        LEFT JOIN FETCH f.foodImage fi
        LEFT JOIN FETCH f.foodSize fs
        LEFT JOIN FETCH f.foodComplement fc
        LEFT JOIN FETCH fc.complement c
        LEFT JOIN FETCH f.foodAddOn fa
        LEFT JOIN FETCH fa.addOn a
        LEFT JOIN FETCH f.foodOrderType fo
        LEFT JOIN FETCH f.foodAvailability fv
        WHERE f.id = :id
    """)*/

    @EntityGraph("Vendor.withAllRelations")
    @Query("SELECT f FROM Food f WHERE f.id = :id")
    fun findByIdWithAllRelations(@Param("id") id: Long): Optional<Food>
}

