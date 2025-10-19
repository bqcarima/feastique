package com.qinet.feastique.repository.order

import com.qinet.feastique.model.entity.order.food.FoodOrderItem
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface FoodOrderItemRepository : JpaRepository<FoodOrderItem, UUID> {
    @EntityGraph("FoodOrderItem.withAllRelations")
    @Query("SELECT f FROM FoodOrderItem f WHERE f.id = :id")
    fun findByIdWithAllRelations(@Param("id") id: UUID): Optional<FoodOrderItem>
}