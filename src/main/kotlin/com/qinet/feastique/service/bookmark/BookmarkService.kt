package com.qinet.feastique.service.bookmark

import com.qinet.feastique.common.mapper.toBookmarkResponse
import com.qinet.feastique.common.mapper.toResponse
import com.qinet.feastique.exception.RequestedEntityNotFoundException
import com.qinet.feastique.model.entity.bookmark.*
import com.qinet.feastique.model.enums.Constants
import com.qinet.feastique.repository.bookmark.*
import com.qinet.feastique.repository.consumables.beverage.BeverageRepository
import com.qinet.feastique.repository.consumables.dessert.DessertRepository
import com.qinet.feastique.repository.consumables.food.FoodRepository
import com.qinet.feastique.repository.consumables.handheld.HandheldRepository
import com.qinet.feastique.repository.like.*
import com.qinet.feastique.repository.user.CustomerRepository
import com.qinet.feastique.repository.user.VendorRepository
import com.qinet.feastique.response.consumables.beverage.BeverageResponse
import com.qinet.feastique.response.consumables.dessert.DessertResponse
import com.qinet.feastique.response.consumables.food.FoodResponse
import com.qinet.feastique.response.consumables.handheld.HandheldResponse
import com.qinet.feastique.response.pagination.WindowResponse
import com.qinet.feastique.response.user.VendorBookmarkResponse
import com.qinet.feastique.security.UserSecurity
import com.qinet.feastique.utility.CursorEncoder
import org.springframework.data.domain.Limit
import org.springframework.data.domain.ScrollPosition
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*
import kotlin.jvm.optionals.getOrElse


@Service
class BookmarkService(
    private val beverageRepository: BeverageRepository,
    private val dessertRepository: DessertRepository,
    private val foodRepository: FoodRepository,
    private val handheldRepository: HandheldRepository,
    private val vendorRepository: VendorRepository,
    private val customerRepository: CustomerRepository,
    private val beverageBookmarkRepository: BeverageBookmarkRepository,
    private val dessertBookmarkRepository: DessertBookmarkRepository,
    private val foodBookmarkRepository: FoodBookmarkRepository,
    private val handheldBookmarkRepository: HandheldBookmarkRepository,
    private val vendorBookmarkRepository: VendorBookmarkRepository,
    private val cursorEncoder: CursorEncoder,
    private val beverageLikeRepository: BeverageLikeRepository,
    private val dessertLikeRepository: DessertLikeRepository,
    private val foodLikeRepository: FoodLikeRepository,
    private val handheldLikeRepository: HandheldLikeRepository,
    private val vendorLikeRepository: VendorLikeRepository,
) {

    @Transactional
    fun bookmarkOrUnbookmarkBeverage(beverageId: UUID, customerDetails: UserSecurity) {
        val existingBookmark = beverageBookmarkRepository.findByBeverageIdAndCustomerId(beverageId, customerDetails.id)
        if (existingBookmark != null) {
            beverageBookmarkRepository.delete(existingBookmark)
        } else {
            val beverage = beverageRepository.findByIdAndIsActiveTrue(beverageId)
                ?: throw RequestedEntityNotFoundException("Beverage not found.")

            val bookmark = BeverageBookmark().apply {
                this.beverage = beverage
                this.customer = customerRepository.getReferenceById(customerDetails.id)
            }
            beverageBookmarkRepository.save(bookmark)
        }
    }

    @Transactional
    fun bookmarkOrUnbookmarkDessert(dessertId: UUID, customerDetails: UserSecurity) {
        val existingBookmark = dessertBookmarkRepository.findByDessertIdAndCustomerId(dessertId, customerDetails.id)
        if (existingBookmark != null) {
            dessertBookmarkRepository.delete(existingBookmark)
        } else {
            val dessert = dessertRepository.findByIdAndIsActiveTrue(dessertId)
                ?: throw RequestedEntityNotFoundException("Dessert not found.")

            val bookmark = DessertBookmark().apply {
                this.dessert = dessert
                this.customer = customerRepository.getReferenceById(customerDetails.id)
            }
            dessertBookmarkRepository.save(bookmark)
        }
    }

    @Transactional
    fun bookmarkOrUnbookmarkFood(foodId: UUID, customerDetails: UserSecurity) {
        val existingBookmark = foodBookmarkRepository.findByFoodIdAndCustomerId(foodId, customerDetails.id)
        if (existingBookmark != null) {
            foodBookmarkRepository.delete(existingBookmark)
        } else {
            val food = foodRepository.findByIdAndIsActiveTrue(foodId)
                ?: throw RequestedEntityNotFoundException("Food not found.")

            val bookmark = FoodBookmark().apply {
                this.food = food
                this.customer = customerRepository.getReferenceById(customerDetails.id)
            }
            foodBookmarkRepository.save(bookmark)
        }
    }

    @Transactional
    fun bookmarkOrUnbookmarkHandheld(handheldId: UUID, customerDetails: UserSecurity) {
        val existingBookmark = handheldBookmarkRepository.findByHandheldIdAndCustomerId(handheldId, customerDetails.id)
        if (existingBookmark != null) {
            handheldBookmarkRepository.delete(existingBookmark)
        } else {
            val handheld = handheldRepository.findByIdAndIsActiveTrue(handheldId)
                ?: throw RequestedEntityNotFoundException("Handheld not found.")

            val bookmark = HandheldBookmark().apply {
                this.handheld = handheld
                this.customer = customerRepository.getReferenceById(customerDetails.id)
            }
            handheldBookmarkRepository.save(bookmark)
        }
    }

    @Transactional
    fun bookmarkOrUnbookmarkVendor(vendorId: UUID, customerDetails: UserSecurity) {
        val existingBookmark = vendorBookmarkRepository.findByVendorIdAndCustomerId(vendorId, customerDetails.id)
        if (existingBookmark != null) {
            vendorBookmarkRepository.delete(existingBookmark)
        } else {
            val vendor = vendorRepository.findById(vendorId)
                .getOrElse { throw RequestedEntityNotFoundException("Vendor not found.") }
            val bookmark = VendorBookmark()
            bookmark.vendor = vendor
            bookmark.customer = customerRepository.getReferenceById(customerDetails.id)
            vendorBookmarkRepository.save(bookmark)
        }
    }

    @Transactional(readOnly = true)
    fun scrollBeverageBookmarks(
        cursor: String?,
        size: Int = Constants.DEFAULT_PAGE_SIZE.type,
        customerDetails: UserSecurity

    ): WindowResponse<BeverageResponse> {
        val currentOffset: Long = cursor?.toLongOrNull() ?: 0L
        val scrollPosition = if (currentOffset == 0L) ScrollPosition.offset() else ScrollPosition.offset(currentOffset)
        val sort = Sort.by("createdAt").descending()

        val window = beverageBookmarkRepository.findAllByCustomerIdAndBeverageIsActiveTrue(customerDetails.id, scrollPosition, sort, Limit.of(size))
        val beverageIds = window.toList().map { it.beverage.id }

        val likeBeverageIds: Set<UUID> =
            beverageLikeRepository.findAllByCustomerIdAndBeverageIdIn(customerDetails.id, beverageIds)
                .map { it.beverage.id }
                .toHashSet()

        return window.map { it.beverage.toResponse(it.beverage.id in likeBeverageIds, true) }
            .toResponse(currentOffset) { cursorEncoder.encodeOffset(it) }
    }

    @Transactional(readOnly = true)
    fun scrollDessertBookmarks(
        cursor: String?,
        size: Int = Constants.DEFAULT_PAGE_SIZE.type,
        customerDetails: UserSecurity

    ): WindowResponse<DessertResponse> {
        val currentOffset: Long = cursor?.toLongOrNull() ?: 0L
        val scrollPosition = if (currentOffset == 0L) ScrollPosition.offset() else ScrollPosition.offset(currentOffset)
        val sort = Sort.by("createdAt").descending()

        val window = dessertBookmarkRepository.findAllByCustomerIdAndDessertIsActiveTrue(customerDetails.id, scrollPosition, sort, Limit.of(size))
        val dessertIds = window.toList().map { it.dessert.id }

        val likeDessertIds: Set<UUID> =
            dessertLikeRepository.findAllByCustomerIdAndDessertIdIn(customerDetails.id, dessertIds)
                .map { it.dessert.id }
                .toHashSet()
        return window.map { it.dessert.toResponse(it.dessert.id in likeDessertIds, true) }
            .toResponse(currentOffset) { cursorEncoder.encodeOffset(it) }
    }

    @Transactional(readOnly = true)
    fun scrollFoodBookmarks(
        cursor: String?,
        size: Int = Constants.DEFAULT_PAGE_SIZE.type,
        customerDetails: UserSecurity

    ): WindowResponse<FoodResponse> {
        val currentOffset: Long = cursor?.toLongOrNull() ?: 0L
        val scrollPosition = if (currentOffset == 0L) ScrollPosition.offset() else ScrollPosition.offset(currentOffset)
        val sort = Sort.by("createdAt").descending()

        val window = foodBookmarkRepository.findAllByCustomerIdAndFoodIsActiveTrue(customerDetails.id, scrollPosition, sort, Limit.of(size))
        val foodIds = window.toList().map { it.food.id }
        val likedFoodIds: Set<UUID> =
            foodLikeRepository.findAllByCustomerIdAndFoodIdIn(customerDetails.id, foodIds)
                .map { it.food.id }
                .toHashSet()

        return window.map { it.food.toResponse(it.food.id in likedFoodIds, true) }
            .toResponse(currentOffset) { cursorEncoder.encodeOffset(it) }
    }

    @Transactional(readOnly = true)
    fun scrollHandheldBookmarks(
        cursor: String?,
        size: Int = Constants.DEFAULT_PAGE_SIZE.type,
        customerDetails: UserSecurity

    ): WindowResponse<HandheldResponse> {
        val currentOffset: Long = cursor?.toLongOrNull() ?: 0L
        val scrollPosition = if (currentOffset == 0L) ScrollPosition.offset() else ScrollPosition.offset(currentOffset)
        val sort = Sort.by("createdAt").descending()

        val window = handheldBookmarkRepository.findAllByCustomerIdAndHandheldIsActiveTrue(customerDetails.id, scrollPosition, sort, Limit.of(size))
        val handheldIds = window.toList().map { it.handheld.id }

        val likeHandheldIds = handheldLikeRepository.findAllByCustomerIdAndHandheldIdIn(customerDetails.id, handheldIds)
            .map { it.handheld.id }
            .toHashSet()

        return window.map { it.handheld.toResponse(it.handheld.id in likeHandheldIds, true) }
            .toResponse(currentOffset) { cursorEncoder.encodeOffset(it) }
    }

    @Transactional(readOnly = true)
    fun scrollVendorBookmarks(
        cursor: String?,
        size: Int = Constants.DEFAULT_PAGE_SIZE.type,
        userDetails: UserSecurity
    ): WindowResponse<VendorBookmarkResponse> {
        val currentOffset: Long = cursor?.toLongOrNull() ?: 0L
        val scrollPosition = if (currentOffset == 0L) ScrollPosition.offset() else ScrollPosition.offset(currentOffset)
        val sort = Sort.by("createdAt").descending()

        val window = vendorBookmarkRepository.findAllByCustomerId(userDetails.id, scrollPosition, sort, Limit.of(size))
        val vendorIds = window.toList().map { it.vendor.id }

        val likedVendorIds: Set<UUID> =
            vendorLikeRepository.findAllByCustomerIdAndVendorIdIn(userDetails.id, vendorIds)
                .map { it.vendor.id }
                .toHashSet()

        return window.map { it.vendor.toBookmarkResponse(it.vendor.id in likedVendorIds, true) }
            .toResponse(currentOffset) { cursorEncoder.encodeOffset(it) }
    }
}

