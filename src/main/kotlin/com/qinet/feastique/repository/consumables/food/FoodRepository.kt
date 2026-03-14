package com.qinet.feastique.repository.consumables.food

import com.qinet.feastique.model.entity.consumables.food.Food
import jakarta.persistence.LockModeType
import org.springframework.data.domain.Limit
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.ScrollPosition
import org.springframework.data.domain.Sort
import org.springframework.data.domain.Window
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface FoodRepository : JpaRepository<Food, UUID> {
    fun findAllByVendorId(vendorId: UUID, pageable: Pageable): Page<Food>
    fun findAllByVendorId(vendorId: UUID, scrollPosition: ScrollPosition, sort: Sort, limit: Limit): Window<Food>

    @EntityGraph("Vendor.withAllRelations")
    @Query("SELECT f FROM Food f WHERE f.id = :id")
    fun findByIdWithAllRelations(@Param("id") id: UUID): Optional<Food>

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT f FROM Food f ORDER BY f.foodNumber DESC ")
    fun findTopOrderByFoodNumberDescWithLock(pageable: Pageable = PageRequest.of(0, 1)): List<Food>
    fun existsByNameIgnoreCaseAndVendorId(name: String, vendorId: UUID): Boolean
}

