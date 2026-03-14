package com.qinet.feastique.service.review

import com.qinet.feastique.common.mapper.toResponse
import com.qinet.feastique.exception.PermissionDeniedException
import com.qinet.feastique.exception.RequestedEntityNotFoundException
import com.qinet.feastique.exception.UserNotFoundException
import com.qinet.feastique.model.dto.review.ReviewTypeDto
import com.qinet.feastique.model.dto.review.ReviewDto
import com.qinet.feastique.model.entity.order.Order
import com.qinet.feastique.model.entity.review.*
import com.qinet.feastique.model.entity.user.Customer
import com.qinet.feastique.model.enums.Constants
import com.qinet.feastique.model.enums.ReviewType
import com.qinet.feastique.repository.order.OrderRepository
import com.qinet.feastique.repository.review.*
import com.qinet.feastique.repository.user.CustomerRepository
import com.qinet.feastique.repository.user.VendorRepository
import com.qinet.feastique.response.pagination.WindowResponse
import com.qinet.feastique.response.review.BaseReviewResponse
import com.qinet.feastique.security.UserSecurity
import com.qinet.feastique.utility.CursorEncoder
import com.qinet.feastique.utility.DuplicateUtility
import org.slf4j.LoggerFactory
import org.springframework.data.domain.*
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*
import kotlin.jvm.optionals.getOrElse

@Service
class ReviewService(
    private val cursorEncoder: CursorEncoder,
    private val beverageReviewRepository: BeverageReviewRepository,
    private val dessertReviewRepository: DessertReviewRepository,
    private val foodReviewRepository: FoodReviewRepository,
    private val handheldReviewRepository: HandheldReviewRepository,
    private val vendorReviewRepository: VendorReviewRepository,
    private val customerRepository: CustomerRepository,
    private val orderRepository: OrderRepository,
    private val duplicateUtility: DuplicateUtility,
    private val vendorRepository: VendorRepository
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    // Beverage
    @Transactional(readOnly = true)
    fun getBeverageReview(id: UUID, customerDetails: UserSecurity): BeverageReview {
        val beverageReview = beverageReviewRepository.findById(id)
            .getOrElse { throw RequestedEntityNotFoundException("Beverage review with id: $id not found.") }
            .also {
                if (it.customer.id != customerDetails.id) {
                    throw PermissionDeniedException("You do not have permission to access this review.")
                }
            }

        logger.info("Beverage review with id: $id by user: ${beverageReview.customer.id} successfully retrieved.")
        return beverageReview
    }

    @Transactional(readOnly = true)
    fun getAllBeverageReviews(
        beverageId: UUID,
        page: Int,
        size: Int = Constants.DEFAULT_PAGE_SIZE.type
    ): Page<BaseReviewResponse> {
        val pageable = PageRequest.of(page, size, Sort.by("createdAt").ascending())
        val beverageReviews = beverageReviewRepository.findAllByBeverageId(beverageId, pageable).map { it.toResponse() }
        return beverageReviews
    }

    @Transactional(readOnly = true)
    fun scrollBeverageReviews(
        beverageId: UUID,
        cursor: String?,
        size: Int = Constants.DEFAULT_PAGE_SIZE.type,
    ): WindowResponse<BaseReviewResponse> {
        val currentOffset: Long = cursor?.toLongOrNull() ?: 0L
        val scrollPosition = if (currentOffset == 0L) {
            ScrollPosition.offset()
        } else {
            ScrollPosition.offset(currentOffset)
        }

        val sort = Sort.by("createdAt").descending()
        val window = beverageReviewRepository.findAllByBeverageId(beverageId, scrollPosition, sort, Limit.of(size))
            .map { it.toResponse() }

        return window.toResponse(currentOffset) { cursorEncoder.encodeOffset(it) }
    }

    // Dessert
    @Transactional(readOnly = true)
    fun getDessertReview(id: UUID, customerDetails: UserSecurity): DessertReview {
        val dessertReview = dessertReviewRepository.findById(id)
            .getOrElse { throw RequestedEntityNotFoundException("Beverage review with id: $id not found.") }
            .also {
                if (it.customer.id != customerDetails.id) {
                    throw PermissionDeniedException("You do not have permission to access this review.")
                }
            }

        logger.info("Dessert review with id: $id by user: ${dessertReview.customer.id} successfully retrieved.")
        return dessertReview
    }

    @Transactional(readOnly = true)
    fun getAllDessertReviews(
        dessertId: UUID,
        page: Int,
        size: Int = Constants.DEFAULT_PAGE_SIZE.type
    ): Page<BaseReviewResponse> {
        val pageable = PageRequest.of(page, size, Sort.by("createdAt").ascending())
        val dessertReviews = dessertReviewRepository.findAllByDessertId(dessertId, pageable).map { it.toResponse() }
        return dessertReviews
    }

    @Transactional(readOnly = true)
    fun scrollDessertReviews(
        dessertId: UUID,
        cursor: String?,
        size: Int = Constants.DEFAULT_PAGE_SIZE.type,
    ): WindowResponse<BaseReviewResponse> {
        val currentOffset: Long = cursor?.toLongOrNull() ?: 0L
        val scrollPosition = if (currentOffset == 0L) {
            ScrollPosition.offset()
        } else {
            ScrollPosition.offset(currentOffset)
        }

        val sort = Sort.by("createdAt").descending()
        val window = dessertReviewRepository.findAllByDessertId(dessertId, scrollPosition, sort, Limit.of(size))
            .map { it.toResponse() }

        return window.toResponse(currentOffset) { cursorEncoder.encodeOffset(it) }
    }

    // Food
    @Transactional(readOnly = true)
    fun getFoodReview(id: UUID, customerDetails: UserSecurity): FoodReview {
        val foodReview = foodReviewRepository.findById(id)
            .getOrElse { throw RequestedEntityNotFoundException("Beverage review with id: $id not found.") }
            .also {
                if (it.customer.id != customerDetails.id) {
                    throw PermissionDeniedException("You do not have permission to access this review.")
                }
            }

        logger.info("Food review with id: $id by user: ${foodReview.customer.id} successfully retrieved.")
        return foodReview
    }

    @Transactional(readOnly = true)
    fun getAllFoodReviews(
        foodId: UUID,
        page: Int,
        size: Int = Constants.DEFAULT_PAGE_SIZE.type
    ): Page<BaseReviewResponse> {
        val pageable = PageRequest.of(page, size, Sort.by("createdAt").ascending())
        val foodReviews = foodReviewRepository.findAllByFoodId(foodId, pageable).map { it.toResponse() }
        return foodReviews
    }

    @Transactional(readOnly = true)
    fun scrollFoodReviews(
        foodId: UUID,
        cursor: String?,
        size: Int = Constants.DEFAULT_PAGE_SIZE.type,
    ): WindowResponse<BaseReviewResponse> {
        val currentOffset: Long = cursor?.toLongOrNull() ?: 0L
        val scrollPosition = if (currentOffset == 0L) {
            ScrollPosition.offset()
        } else {
            ScrollPosition.offset(currentOffset)
        }

        val sort = Sort.by("createdAt").descending()
        val window =
            foodReviewRepository.findAllByFoodId(foodId, scrollPosition, sort, Limit.of(size)).map { it.toResponse() }

        return window.toResponse(currentOffset) { cursorEncoder.encodeOffset(it) }
    }

    // Handheld
    @Transactional(readOnly = true)
    fun getHandheldReview(id: UUID, customerDetails: UserSecurity): HandheldReview {
        val handheldReview = handheldReviewRepository.findById(id)
            .getOrElse { throw RequestedEntityNotFoundException("Beverage review with id: $id not found.") }
            .also {
                if (it.customer.id != customerDetails.id) {
                    throw PermissionDeniedException("You do not have permission to access this review.")
                }
            }

        logger.info("Handheld review with id: $id by user: ${handheldReview.customer.id} successfully retrieved.")
        return handheldReview
    }

    @Transactional(readOnly = true)
    fun getAllHandheldReviews(
        handheldId: UUID,
        page: Int,
        size: Int = Constants.DEFAULT_PAGE_SIZE.type
    ): Page<BaseReviewResponse> {
        val pageable = PageRequest.of(page, size, Sort.by("createdAt").ascending())
        val handheldReviews = handheldReviewRepository.findAllByHandheldId(handheldId, pageable).map { it.toResponse() }
        return handheldReviews
    }

    @Transactional(readOnly = true)
    fun scrollHandheldReviews(
        handheldId: UUID,
        cursor: String?,
        size: Int = Constants.DEFAULT_PAGE_SIZE.type,
    ): WindowResponse<BaseReviewResponse> {
        val currentOffset: Long = cursor?.toLongOrNull() ?: 0L
        val scrollPosition = if (currentOffset == 0L) {
            ScrollPosition.offset()
        } else {
            ScrollPosition.offset(currentOffset)
        }

        val sort = Sort.by("createdAt").descending()
        val window = handheldReviewRepository.findAllByHandheldId(handheldId, scrollPosition, sort, Limit.of(size))
            .map { it.toResponse() }

        return window.toResponse(currentOffset) { cursorEncoder.encodeOffset(it) }
    }

    // Vendor
    @Transactional(readOnly = true)
    fun getVendorReview(id: UUID, customerDetails: UserSecurity): VendorReview {
        val vendorReview = vendorReviewRepository.findById(id)
            .getOrElse { throw RequestedEntityNotFoundException("Beverage review with id: $id not found.") }
            .also {
                if (it.customer.id != customerDetails.id) {
                    throw PermissionDeniedException("You do not have permission to access this review.")
                }
            }

        logger.info("Vendor review with id: $id by user: ${vendorReview.customer.id} successfully retrieved.")
        return vendorReview
    }

    @Transactional(readOnly = true)
    fun getAllVendorReviews(
        vendorId: UUID,
        page: Int,
        size: Int = Constants.DEFAULT_PAGE_SIZE.type
    ): Page<BaseReviewResponse> {
        val pageable = PageRequest.of(page, size, Sort.by("createdAt").ascending())
        val vendorReviews = vendorReviewRepository.findAllByVendorId(vendorId, pageable).map { it.toResponse() }
        return vendorReviews
    }

    @Transactional(readOnly = true)
    fun scrollVendorReviews(
        vendorId: UUID,
        cursor: String?,
        size: Int = Constants.DEFAULT_PAGE_SIZE.type,
    ): WindowResponse<BaseReviewResponse> {
        val currentOffset: Long = cursor?.toLongOrNull() ?: 0L
        val scrollPosition = if (currentOffset == 0L) {
            ScrollPosition.offset()
        } else {
            ScrollPosition.offset(currentOffset)
        }

        val sort = Sort.by("createdAt").descending()
        val window = vendorReviewRepository.findAllByVendorId(vendorId, scrollPosition, sort, Limit.of(size))
            .map { it.toResponse() }

        return window.toResponse(currentOffset) { cursorEncoder.encodeOffset(it) }
    }

    @Transactional
    fun addOrUpdateReview(reviewDto: ReviewDto, customerDetails: UserSecurity): Review {
        val customer = customerRepository.findById(customerDetails.id)
            .getOrElse { throw UserNotFoundException() }

        val order = orderRepository.findById(requireNotNull(reviewDto.orderId) { "Order ID must not be null." })
            .getOrElse { throw RequestedEntityNotFoundException("Order with id: ${reviewDto.orderId} not found.") }
            .also {
                if (it.customer!!.id != customer.id)
                    throw PermissionDeniedException("You do not have permission to view this order.")
            }

        requireNotNull(reviewDto.rating) { "Please enter a rating." }

        return when {
            reviewDto.beverageReviewDto != null -> prepareBeverageReview(reviewDto, customer, order, customerDetails)
            reviewDto.dessertReviewDto != null -> prepareDessertReview(reviewDto, customer, order, customerDetails)
            reviewDto.foodReviewDto != null -> prepareFoodReview(reviewDto, customer, order, customerDetails)
            reviewDto.handheldReviewDto != null -> prepareHandheldReview(reviewDto, customer, order, customerDetails)
            reviewDto.vendorReviewDto != null -> prepareVendorReview(reviewDto, customer, order, customerDetails)
            else -> throw IllegalArgumentException("No review type specified in request.")
        }
    }

    private fun prepareBeverageReview(
        reviewDto: ReviewDto,
        customer: Customer,
        order: Order,
        customerDetails: UserSecurity
    ): BeverageReview {
        val beverageId = requireNotNull(reviewDto.beverageReviewDto?.beverageId) { "Beverage id cannot be empty." }

        val reviewExists =
            (duplicateUtility.isExistingReviewFound(beverageId, customer.id, order.id, ReviewType.BEVERAGE))

        if (reviewDto.id == null && reviewExists) {
            throw IllegalArgumentException("You have already submitted a review for this beverage in this order.")
        }

        val beverageOrderItem = order.beverageOrderItems.find { it.beverage.id == beverageId }
            ?: throw RequestedEntityNotFoundException("Beverage $beverageId not found in order.")

        val review = if (reviewDto.id != null) {
            beverageReviewRepository.findById(reviewDto.id!!)
                .getOrElse { throw RequestedEntityNotFoundException("Beverage review with id: ${reviewDto.id} not found.") }
                .also {
                    if (it.customer.id != customerDetails.id) {
                        throw PermissionDeniedException("You do not have permission to edit or update this review.")
                    }
                }
        } else {
            BeverageReview().apply {
                this.beverage = beverageOrderItem.beverage
                this.beverageOrderItem = beverageOrderItem
                this.order = order
                this.customer = customer
            }
        }

        review.review = reviewDto.review
        review.rating = reviewDto.rating
        return beverageReviewRepository.saveAndFlush(review)
    }

    private fun prepareDessertReview(
        reviewDto: ReviewDto,
        customer: Customer,
        order: Order,
        customerDetails: UserSecurity
    ): DessertReview {
        val dessertId = requireNotNull(reviewDto.dessertReviewDto?.dessertId) { "Dessert id cannot be empty." }

        val reviewExists =
            (duplicateUtility.isExistingReviewFound(dessertId, customer.id, order.id, ReviewType.DESSERT))

        if (reviewDto.id == null && reviewExists) {
            throw IllegalArgumentException("You have already submitted a review for this dessert in this order.")
        }

        val dessertOrderItem = order.dessertOrderItems.find { it.dessert.id == dessertId }
            ?: throw RequestedEntityNotFoundException("Dessert $dessertId not found in order.")

        val review = if (reviewDto.id != null) {
            dessertReviewRepository.findById(reviewDto.id!!)
                .getOrElse { throw RequestedEntityNotFoundException("Dessert review with id: ${reviewDto.id} not found.") }
                .also {
                    if (it.customer.id != customerDetails.id) {
                        throw PermissionDeniedException("You do not have permission to access this review.")
                    }
                }
        } else {
            DessertReview().apply {
                this.dessert = dessertOrderItem.dessert
                this.dessertOrderItem = dessertOrderItem
                this.order = order
                this.customer = customer
            }
        }

        review.review = reviewDto.review
        review.rating = reviewDto.rating
        return dessertReviewRepository.saveAndFlush(review)
    }

    private fun prepareFoodReview(
        reviewDto: ReviewDto,
        customer: Customer,
        order: Order,
        customerDetails: UserSecurity
    ): FoodReview {
        val foodId = requireNotNull(reviewDto.foodReviewDto?.foodId) { "Food id cannot be empty." }

        val reviewExists = (duplicateUtility.isExistingReviewFound(foodId, customer.id, order.id, ReviewType.FOOD))

        if (reviewDto.id == null && reviewExists) {
            throw IllegalArgumentException("You have already submitted a review for this food in this order.")
        }

        val foodOrderItem = order.foodOrderItems.find { it.food.id == foodId }
            ?: throw RequestedEntityNotFoundException("Food $foodId not found in order.")

        val review = if (reviewDto.id != null) {
            foodReviewRepository.findById(reviewDto.id!!)
                .getOrElse { throw RequestedEntityNotFoundException("Food review with id: ${reviewDto.id} not found.") }
                .also {
                    if (it.customer.id != customerDetails.id) {
                        throw PermissionDeniedException("You do not have permission to access this review.")
                    }
                }
        } else {
            FoodReview().apply {
                this.food = foodOrderItem.food
                this.foodOrderItem = foodOrderItem
                this.order = order
                this.customer = customer
            }
        }

        review.review = reviewDto.review
        review.rating = reviewDto.rating
        return foodReviewRepository.saveAndFlush(review)
    }

    private fun prepareHandheldReview(
        reviewDto: ReviewDto,
        customer: Customer,
        order: Order,
        customerDetails: UserSecurity
    ): HandheldReview {
        val handheldId = requireNotNull(reviewDto.handheldReviewDto?.handheldId) { "Handheld id cannot be empty." }

        val reviewExists =
            (duplicateUtility.isExistingReviewFound(handheldId, customer.id, order.id, ReviewType.HANDHELD))

        if (reviewDto.id == null && reviewExists) {
            throw IllegalArgumentException("You have already submitted a review for this handheld in this order.")
        }

        val handheldOrderItem = order.handheldOrderItems.find { it.handheld.id == handheldId }
            ?: throw RequestedEntityNotFoundException("Handheld $handheldId not found in order.")

        val review = if (reviewDto.id != null) {
            handheldReviewRepository.findById(reviewDto.id!!)
                .getOrElse { throw RequestedEntityNotFoundException("Handheld review with id: ${reviewDto.id} not found.") }
                .also {
                    if (it.customer.id != customerDetails.id) {
                        throw PermissionDeniedException("You do not have permission to access this review.")
                    }
                }

        } else {
            HandheldReview().apply {
                this.handheld = handheldOrderItem.handheld
                this.handheldOrderItem = handheldOrderItem
                this.order = order
                this.customer = customer
            }
        }

        review.review = reviewDto.review
        review.rating = reviewDto.rating
        return handheldReviewRepository.saveAndFlush(review)
    }

    private fun prepareVendorReview(
        reviewDto: ReviewDto,
        customer: Customer,
        order: Order,
        customerDetails: UserSecurity
    ): VendorReview {
        val vendorId = requireNotNull(reviewDto.vendorReviewDto?.vendorId) { "Vendor id cannot be empty." }

        val reviewExists = (duplicateUtility.isExistingReviewFound(vendorId, customer.id, order.id, ReviewType.VENDOR))

        if (reviewDto.id == null && reviewExists) {
            throw IllegalArgumentException("You have already submitted a review for this order concerning the order.")
        }

        if (order.vendor?.id != vendorId)
            throw PermissionDeniedException("This vendor is not associated with this order.")

        val vendor = vendorRepository.findById(vendorId)
            .getOrElse { throw RequestedEntityNotFoundException("Vendor $vendorId not found.") }

        check(vendor.id == order.vendor?.id) { "This vendor is not associated with this order." }

        val review = if (reviewDto.id != null) {
            vendorReviewRepository.findById(reviewDto.id!!)
                .getOrElse { throw RequestedEntityNotFoundException("Handheld review with id: ${reviewDto.id} not found.") }
                .also {
                    if (it.customer.id != customerDetails.id) {
                        throw PermissionDeniedException("You do not have permission to access this review.")
                    }
                }
        } else {
            VendorReview().apply {
                this.vendor = vendor
                this.order = order
                this.customer = customer
            }
        }

        review.review = reviewDto.review
        review.rating = reviewDto.rating
        return vendorReviewRepository.saveAndFlush(review)
    }

    @Transactional(readOnly = true)
    fun getReview(id: UUID, reviewTypeDto: ReviewTypeDto, customerDetails: UserSecurity): Review {
        return when (ReviewType.fromString(reviewTypeDto.reviewType)) {
            ReviewType.BEVERAGE -> getBeverageReview(id, customerDetails)
            ReviewType.DESSERT -> getDessertReview(id, customerDetails)
            ReviewType.FOOD -> getFoodReview(id, customerDetails)
            ReviewType.HANDHELD -> getHandheldReview(id, customerDetails)
            ReviewType.VENDOR -> getVendorReview(id, customerDetails)
        }
    }

    @Transactional(readOnly = true)
    fun getAllItemReviews(
        itemId: UUID,
        reviewTypeDto: ReviewTypeDto,
        page: Int,
        size: Int = Constants.DEFAULT_PAGE_SIZE.type,
    ): Page<BaseReviewResponse> {
        val pageable = PageRequest.of(page, size, Sort.by("createdAt").ascending())
        return when (ReviewType.fromString(reviewTypeDto.reviewType)) {
            ReviewType.BEVERAGE -> beverageReviewRepository.findAllByBeverageId(itemId, pageable)
            ReviewType.DESSERT -> dessertReviewRepository.findAllByDessertId(itemId, pageable)
            ReviewType.FOOD -> foodReviewRepository.findAllByFoodId(itemId, pageable)
            ReviewType.HANDHELD -> handheldReviewRepository.findAllByHandheldId(itemId, pageable)
            ReviewType.VENDOR -> vendorReviewRepository.findAllByVendorId(itemId, pageable)
        }.map { it.toResponse() }
    }

    @Transactional
    fun deleteReview(reviewTypeDto: ReviewTypeDto, id: UUID, customerDetails: UserSecurity) {
        val reviewType = ReviewType.fromString(reviewTypeDto.reviewType)
        when (reviewType) {
            ReviewType.BEVERAGE -> beverageReviewRepository.delete(getBeverageReview(id, customerDetails))
            ReviewType.DESSERT -> dessertReviewRepository.delete(getDessertReview(id, customerDetails))
            ReviewType.FOOD -> foodReviewRepository.delete(getFoodReview(id, customerDetails))
            ReviewType.HANDHELD -> handheldReviewRepository.delete(getHandheldReview(id, customerDetails))
            ReviewType.VENDOR -> vendorReviewRepository.delete(getVendorReview(id, customerDetails))
        }
    }
}

