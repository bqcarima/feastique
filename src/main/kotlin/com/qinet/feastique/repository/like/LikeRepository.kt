package com.qinet.feastique.repository.like

import com.qinet.feastique.model.entity.like.*
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface PostLikeRepository : JpaRepository<PostLike, UUID> {
    fun existsByPostIdAndCustomerId(postId: UUID, customerId: UUID): Boolean
    fun findByPostIdAndCustomerId(postId: UUID, customerId: UUID): PostLike?
    fun findAllByCustomerIdAndPostIdIn(customerId: UUID, postIds: List<UUID>): List<PostLike>
}

@Repository
interface BeverageLikeRepository : JpaRepository<BeverageLike, UUID> {
    fun existsByBeverageIdAndCustomerId(beverageId: UUID, customerId: UUID): Boolean
    fun findByBeverageIdAndCustomerId(beverageId: UUID, customerId: UUID): BeverageLike?
    fun findAllByCustomerIdAndBeverageIdIn(customerId: UUID, beverageIds: List<UUID>): List<BeverageLike>
}

@Repository
interface DessertLikeRepository : JpaRepository<DessertLike, UUID> {
    fun existsByDessertIdAndCustomerId(dessertId: UUID, customerId: UUID): Boolean
    fun findByDessertIdAndCustomerId(dessertId: UUID, customerId: UUID): DessertLike?
    fun findAllByCustomerIdAndDessertIdIn(customerId: UUID, dessertIds: List<UUID>): List<DessertLike>
}

@Repository
interface FoodLikeRepository : JpaRepository<FoodLike, UUID> {
    fun existsByFoodIdAndCustomerId(foodId: UUID, customerId: UUID): Boolean
    fun findByFoodIdAndCustomerId(foodId: UUID, customerId: UUID): FoodLike?
    fun findAllByCustomerIdAndFoodIdIn(customerId: UUID, foodIds: List<UUID>): List<FoodLike>
}

@Repository
interface HandheldLikeRepository : JpaRepository<HandheldLike, UUID> {
    fun existsByHandheldIdAndCustomerId(handheldId: UUID, customerId: UUID): Boolean
    fun findByHandheldIdAndCustomerId(handheldId: UUID, customerId: UUID): HandheldLike?
    fun findAllByCustomerIdAndHandheldIdIn(customerId: UUID, handheldIds: List<UUID>): List<HandheldLike>
}

@Repository
interface VendorLikeRepository : JpaRepository<VendorLike, UUID> {
    fun existsByVendorIdAndCustomerId(vendorId: UUID, customerId: UUID): Boolean
    fun findByVendorIdAndCustomerId(vendorId: UUID, customerId: UUID): VendorLike?
    fun findAllByCustomerIdAndVendorIdIn(customerId: UUID, vendorIds: List<UUID>): List<VendorLike>
}

