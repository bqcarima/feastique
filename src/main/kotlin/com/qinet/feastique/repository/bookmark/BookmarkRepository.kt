package com.qinet.feastique.repository.bookmark

import com.qinet.feastique.model.entity.bookmark.*
import org.springframework.data.domain.Limit
import org.springframework.data.domain.ScrollPosition
import org.springframework.data.domain.Sort
import org.springframework.data.domain.Window
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*


@Repository
interface BeverageBookmarkRepository : JpaRepository<BeverageBookmark, UUID> {
    fun findByBeverageIdAndCustomerId(beverageId: UUID, customerId: UUID): BeverageBookmark?
    fun existsByBeverageIdAndCustomerId(beverageId: UUID, customerId: UUID): Boolean
    fun findAllByCustomerIdAndBeverageIsActiveTrue(customerId: UUID, scrollPosition: ScrollPosition, sort: Sort, limit: Limit): Window<BeverageBookmark>
    fun findAllByCustomerIdAndBeverageIdIn(customerId: UUID, beverageIds: List<UUID>): List<BeverageBookmark>
}

@Repository
interface DessertBookmarkRepository : JpaRepository<DessertBookmark, UUID> {
    fun findByDessertIdAndCustomerId(dessertId: UUID, customerId: UUID): DessertBookmark?
    fun existsByDessertIdAndCustomerId(dessertId: UUID, customerId: UUID): Boolean
    fun findAllByCustomerIdAndDessertIsActiveTrue(customerId: UUID, scrollPosition: ScrollPosition, sort: Sort, limit: Limit): Window<DessertBookmark>
    fun findAllByCustomerIdAndDessertIdIn(customerId: UUID, beverageIds: List<UUID>): List<DessertBookmark>
}

@Repository
interface FoodBookmarkRepository : JpaRepository<FoodBookmark, UUID> {
    fun findByFoodIdAndCustomerId(foodId: UUID, customerId: UUID): FoodBookmark?
    fun existsByFoodIdAndCustomerId(foodId: UUID, customerId: UUID): Boolean
    fun findAllByCustomerIdAndFoodIsActiveTrue(customerId: UUID, scrollPosition: ScrollPosition, sort: Sort, limit: Limit): Window<FoodBookmark>
    fun findAllByCustomerIdAndFoodIdIn(customerId: UUID, beverageIds: List<UUID>): List<FoodBookmark>
}

@Repository
interface HandheldBookmarkRepository : JpaRepository<HandheldBookmark, UUID> {
    fun findByHandheldIdAndCustomerId(handheldId: UUID, customerId: UUID): HandheldBookmark?
    fun existsByHandheldIdAndCustomerId(handheldId: UUID, customerId: UUID): Boolean
    fun findAllByCustomerIdAndHandheldIsActiveTrue(customerId: UUID, scrollPosition: ScrollPosition, sort: Sort, limit: Limit): Window<HandheldBookmark>
    fun findAllByCustomerIdAndHandheldIdIn(customerId: UUID, beverageIds: List<UUID>): List<HandheldBookmark>
}

@Repository
interface VendorBookmarkRepository : JpaRepository<VendorBookmark, UUID> {
    fun findByVendorIdAndCustomerId(vendorId: UUID, customerId: UUID): VendorBookmark?
    fun existsByVendorIdAndCustomerId(vendorId: UUID, customerId: UUID): Boolean
    fun findAllByCustomerId(customerId: UUID, scrollPosition: ScrollPosition, sort: Sort, limit: Limit): Window<VendorBookmark>
    fun findAllByCustomerIdAndVendorIdIn(customerId: UUID, beverageIds: List<UUID>): List<VendorBookmark>
}

