package com.qinet.feastique.service.consumables

import com.qinet.feastique.common.mapper.toResponse
import com.qinet.feastique.exception.DuplicateFoundException
import com.qinet.feastique.exception.PermissionDeniedException
import com.qinet.feastique.exception.RequestedEntityNotFoundException
import com.qinet.feastique.exception.UserNotFoundException
import com.qinet.feastique.model.dto.HandheldAvailabilityDto
import com.qinet.feastique.model.dto.consumables.HandheldDto
import com.qinet.feastique.model.entity.consumables.filling.Filling
import com.qinet.feastique.model.entity.consumables.filling.HandheldFilling
import com.qinet.feastique.model.entity.consumables.handheld.Handheld
import com.qinet.feastique.model.entity.discount.Discount
import com.qinet.feastique.model.entity.discount.HandheldDiscount
import com.qinet.feastique.model.entity.image.HandheldImage
import com.qinet.feastique.model.entity.menu.Menu
import com.qinet.feastique.model.entity.size.HandheldSize
import com.qinet.feastique.model.entity.user.Vendor
import com.qinet.feastique.model.enums.*
import com.qinet.feastique.repository.bookmark.HandheldBookmarkRepository
import com.qinet.feastique.repository.consumables.filling.FillingRepository
import com.qinet.feastique.repository.consumables.handheld.HandheldRepository
import com.qinet.feastique.repository.discount.DiscountRepository
import com.qinet.feastique.repository.like.HandheldLikeRepository
import com.qinet.feastique.repository.menu.MenuRepository
import com.qinet.feastique.repository.user.VendorRepository
import com.qinet.feastique.response.consumables.handheld.HandheldResponse
import com.qinet.feastique.response.pagination.WindowResponse
import com.qinet.feastique.security.UserSecurity
import com.qinet.feastique.utility.CursorEncoder
import com.qinet.feastique.utility.DuplicateUtility
import com.qinet.feastique.utility.SecurityUtility
import org.slf4j.LoggerFactory
import org.springframework.data.domain.*
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*
import kotlin.jvm.optionals.getOrElse

@Service
class HandheldService(
    private val handheldRepository: HandheldRepository,
    private val vendorRepository: VendorRepository,
    private val securityUtility: SecurityUtility,
    private val duplicateUtility: DuplicateUtility,
    private val menuRepository: MenuRepository,
    private val fillingRepository: FillingRepository,
    private val discountRepository: DiscountRepository,
    private val handheldLikeRepository: HandheldLikeRepository,
    private val cursorEncoder: CursorEncoder,
    private val handheldBookmarkRepository: HandheldBookmarkRepository,
) {
    @Suppress("unused")
    private val logger = LoggerFactory.getLogger(javaClass)

    @Transactional
    fun saveHandheld(handheld: Handheld): Handheld {
        return handheldRepository.saveAndFlush(handheld)
    }

    @Transactional(readOnly = true)
    fun getHandheld(handheldId: UUID, userDetails: UserSecurity): HandheldResponse {
        val role = securityUtility.getRole(userDetails)
        var liked = false
        var bookmarked = false
        val handheld = when (role) {
            "CUSTOMER" -> {
                liked = handheldLikeRepository.existsByHandheldIdAndCustomerId(handheldId, userDetails.id)
                bookmarked = handheldBookmarkRepository.existsByHandheldIdAndCustomerId(handheldId, userDetails.id)
                handheldRepository.findById(handheldId)
                    .getOrElse { throw RequestedEntityNotFoundException() }
            }
            "VENDOR" -> handheldRepository.findByIdAndVendorIdAndIsActiveTrue(handheldId, userDetails.id)
                ?: throw RequestedEntityNotFoundException()

            else -> throw IllegalArgumentException("Unsupported role: $role")
        }

        return handheld.toResponse(liked, bookmarked)
    }

    @Transactional(readOnly = true)
    fun getHandheldById(handheldId: UUID, vendorDetails: UserSecurity): Handheld {
        return handheldRepository.findByIdAndVendorIdAndIsActiveTrue(handheldId, vendorDetails.id)
            ?: throw RequestedEntityNotFoundException("Handheld not found.")
    }


    @Transactional(readOnly = true)
    fun getAllHandhelds(vendorDetails: UserSecurity, page: Int, size: Int): Page<HandheldResponse> {
        val pageable = PageRequest.of(page, size, Sort.by("name").ascending())
        return handheldRepository.findAllByVendorIdAndIsActiveTrue(vendorDetails.id, pageable).map { it.toResponse() }
    }

    @Transactional(readOnly = true)
    fun scrollHandhelds(
        vendorId: UUID,
        cursor: String?,
        size: Int = Constants.DEFAULT_PAGE_SIZE.type,
        userDetails: UserSecurity
    ): WindowResponse<HandheldResponse> {
        val currentOffset: Long = cursor?.toLongOrNull() ?: 0L
        val scrollPosition = if (currentOffset == 0L) ScrollPosition.offset() else ScrollPosition.offset(currentOffset)
        val sort = Sort.by("name").ascending()

        val window = handheldRepository.findAllByVendorIdAndIsActiveTrue(vendorId, scrollPosition, sort, Limit.of(size))

        val isCustomer = securityUtility.getSingleRole(userDetails) == "CUSTOMER"
        val handheldIds = if (isCustomer) window.toList().map { it.id } else emptyList()

        val likedHandheldIds: Set<UUID> = if (isCustomer) {
            handheldLikeRepository.findAllByCustomerIdAndHandheldIdIn(userDetails.id, handheldIds)
                .map { it.handheld.id }
                .toHashSet()
        } else emptySet()

        val bookmarkedHandheldIds: Set<UUID> = if (isCustomer) {
            handheldBookmarkRepository.findAllByCustomerIdAndHandheldIdIn(userDetails.id, handheldIds)
                .map { it.handheld.id }
                .toHashSet()
        } else emptySet()

        return window.map { it.toResponse(it.id in likedHandheldIds, it.id in bookmarkedHandheldIds) }
            .toResponse(currentOffset) { cursorEncoder.encodeOffset(it) }
    }

    @Transactional
    fun deleteHandheld(id: UUID, vendorDetails: UserSecurity) {
        val handheld = getHandheldById(id, vendorDetails)
        handheld.isActive = false
        saveHandheld(handheld)
    }

    @Transactional
    fun changeHandheldAvailability(
        handheldAvailabilityDto: HandheldAvailabilityDto,
        id: UUID,
        vendorDetails: UserSecurity
    ): Handheld {
        val handheld = getHandheldById(id, vendorDetails)

        if (handheld.availability != Availability.fromString(handheldAvailabilityDto.availability)) {
            handheld.availability = Availability.fromString(handheldAvailabilityDto.availability!!)
        }

        changeHandheldSizeAvailability(handheldAvailabilityDto, handheld)

        val updatedHandheld = handheldRepository.saveAndFlush(handheld)
        return updatedHandheld
    }

    /**
     * Creates or updates a handheld for the authenticated vendor.
     *
     * - Create: when [handheldDto].id is null — generates a sequential handheld number and checks for duplicate names.
     * - Update: when [handheldDto].id is provided — ownership is verified; duplicate check runs only if the name changed.
     *
     * Nested collections (sizes, fillings, images, orderTypes, discounts) are reconciled:
     * existing children are reused or updated, and children absent from the DTO are removed.
     *
     * @throws UserNotFoundException when the vendor cannot be found
     * @throws RequestedEntityNotFoundException when updating a non-existent handheld id
     * @throws PermissionDeniedException when the handheld belongs to another vendor
     * @throws DuplicateFoundException when the name already exists for this vendor
     * @throws IllegalArgumentException when required fields are missing or invalid
     */
    @Transactional
    fun addOrUpdateHandheld(handheldDto: HandheldDto, vendorDetails: UserSecurity): Handheld {
        val vendor = vendorRepository.findById(vendorDetails.id)
            .orElseThrow { UserNotFoundException("Vendor not found.") }

        val newHandheld: Handheld = if (handheldDto.id != null) {
            getHandheldById(handheldDto.id!!, vendorDetails)
        } else {
            Handheld().apply { this.vendor = vendor }
        }

        val handheldName = requireNotNull(handheldDto.handheldName) { "Please enter a handheld name." }
        val handheldType = HandHeldType.fromString(handheldDto.handheldType)
        val description = handheldDto.description
        val availability = Availability.fromString(handheldDto.availability)
        val deliverable = requireNotNull(handheldDto.deliverable) { "Please select deliverability." }
        val readyAsFrom = handheldDto.readyAsFrom ?: vendor.openingTime
        val dailyDeliveryQuantity = handheldDto.dailyDeliveryQuantity // null -> almost unlimited
        val preparationTime = handheldDto.preparationTime ?: 0
        val quickDelivery = handheldDto.quickDelivery
        val deliveryFee = handheldDto.deliveryFee

        if (handheldDto.id == null) {
            if (duplicateUtility.isDuplicateHandheldFound(handheldName, vendorDetails.id)) {
                throw DuplicateFoundException("A $handheldType with the name: $handheldName already exist. Cannot add duplicate.")
            }
            newHandheld.name = handheldName
            val lastHandheld = handheldRepository.findTopOrderByFoodNumberDescWithLock().firstOrNull()
            val nextNumber = lastHandheld?.handheldNumber?.takeLast(5)?.toInt()?.plus(1) ?: 1
            newHandheld.handheldNumber = "HD-%05d".format(nextNumber)
        } else {
            if (newHandheld.name != handheldName) {
                if (duplicateUtility.isDuplicateHandheldFound(handheldName, vendorDetails.id)) {
                    throw DuplicateFoundException("A $handheldType with the name: $handheldName already exist. Cannot add duplicate.")
                }
            }
        }

        newHandheld.apply {
            this.name = handheldName
            this.handHeldType = handheldType
            this.description = description
            this.availability = availability
            this.readyAsFrom = readyAsFrom
            this.deliverable = deliverable
            this.dailyDeliveryQuantity = dailyDeliveryQuantity
            this.preparationTime = preparationTime
            this.quickDelivery = quickDelivery
            this.deliveryFee = deliveryFee
        }

        val managedHandheld = saveHandheld(newHandheld)

        prepareHandheldFillings(handheldDto, managedHandheld, vendor)
        managedHandheld.availableDays = prepareHandheldAvailableDays(handheldDto, managedHandheld)
        handheldDto.discounts?.let { prepareHandheldDiscounts(handheldDto, managedHandheld, vendor) }
            ?: managedHandheld.handheldDiscounts.clear()
        managedHandheld.handheldImages = prepareHandheldImages(handheldDto, managedHandheld)
        managedHandheld.orderTypes = prepareHandheldOrderTypes(handheldDto, managedHandheld)
        managedHandheld.handheldSizes = prepareHandheldSizes(handheldDto, managedHandheld)

        menuRepository.saveAndFlush(prepareMenu(handheldDto, managedHandheld))

        return saveHandheld(managedHandheld)
    }

    private fun prepareMenu(handheldDto: HandheldDto, handheld: Handheld): Menu {
        val menu = if (handheldDto.id != null) {
            menuRepository.findById(handheld.menu!!.id)
                .getOrElse { throw RequestedEntityNotFoundException("Menu item not found.") }
        } else {
            Menu().apply {
                this.handheld = handheld
                deliveryItemsLeft = handheld.dailyDeliveryQuantity // null -> unlimited, 0 -> sold out
            }
        }

        handheld.orderTypes.forEach {
            when (it) {
                // null -> sold out, false -> not offered, true -> available
                // DELIVERY: true + deliveryItemsLeft = null -> unlimited, > 0 -> available, = 0 -> sold out
                OrderType.DELIVERY -> menu.delivery = true
                OrderType.DINE_IN -> menu.dineIn = true
                OrderType.PICKUP -> menu.pickup = true
                else -> throw IllegalArgumentException("Unknown order type selected.")
            }
        }
        return menu
    }

    private fun prepareHandheldFillings(handheldDto: HandheldDto, handheld: Handheld, vendor: Vendor) {
        val existingHandheldFillings = handheld.handheldFillings.associateBy { it.filling.id }

        val updatedFillings = handheldDto.fillings.map { dto ->
            var filling = Filling().apply {
                dto.id?.let { this.id = it }
                this.vendor = vendor
                this.name = requireNotNull(dto.name)
                this.description = dto.description
            }
            filling = fillingRepository.saveAndFlush(filling)

            val handheldFilling =
                dto.id?.let { existingHandheldFillings[it] } ?: HandheldFilling().apply { this.handheld = handheld }
            handheldFilling.filling = filling
            handheldFilling
        }

        handheld.handheldFillings.removeIf { existing -> updatedFillings.none { it.filling.id == existing.filling.id } }
        updatedFillings.forEach { updated ->
            if (handheld.handheldFillings.none { it.filling.id == updated.filling.id }) handheld.handheldFillings.add(
                updated
            )
        }
    }

    private fun prepareHandheldDiscounts(handheldDto: HandheldDto, handheld: Handheld, vendor: Vendor) {
        val existingHandheldDiscounts = handheld.handheldDiscounts.associateBy { it.discount.id }

        val updatedDiscounts = handheldDto.discounts!!.map { dto ->
            var discount = dto.id?.let { id ->
                discountRepository.findById(id)
                    .orElseThrow { RequestedEntityNotFoundException("Discount with id $id not found") }
            } ?: Discount().apply { this.vendor = vendor }

            discount.apply {
                discountName = requireNotNull(dto.discountName)
                percentage = requireNotNull(dto.percentage)
                startDate = requireNotNull(dto.startDate)
                endDate = requireNotNull(dto.endDate)
            }
            discount = discountRepository.save(discount)

            val handheldDiscount =
                dto.id?.let { existingHandheldDiscounts[it] } ?: HandheldDiscount().apply { this.handheld = handheld }
            handheldDiscount.discount = discount
            handheldDiscount
        }

        handheld.handheldDiscounts.removeIf { existing -> updatedDiscounts.none { it.discount.id == existing.id } }
        updatedDiscounts.forEach { updated ->
            if (handheld.handheldDiscounts.none { it.discount.id == updated.discount.id }) handheld.handheldDiscounts.add(
                updated
            )
        }
    }

    private fun prepareHandheldSizes(handheldDto: HandheldDto, handheld: Handheld): MutableSet<HandheldSize> {
        if (handheldDto.handheldSizes.isEmpty()) throw IllegalArgumentException("Please select at least one food size.")

        val existingHandheldSizes = handheld.handheldSizes.associateBy { it.id }

        val updatedSizes = handheldDto.handheldSizes.map { dto ->
            val handheldSize = existingHandheldSizes[dto.id] ?: HandheldSize().apply { this.handheld = handheld }
            handheldSize.apply {
                this.size = Size.fromString(requireNotNull(dto.size))
                name = dto.sizeName ?: this.size!!.name
                price = dto.price
                availability = requireNotNull(Availability.fromString(dto.availability!!))
                isActive = true
            }
        }

        handheld.handheldSizes.forEach { existing ->
            if (updatedSizes.none { it.id == existing.id }) existing.isActive = false
        }
        updatedSizes.forEach { updated ->
            if (handheld.handheldSizes.none { it.id == updated.id })
                handheld.handheldSizes.add(updated)
        }

        return handheld.handheldSizes
    }

    private fun prepareHandheldAvailableDays(handheldDto: HandheldDto, handheld: Handheld): MutableSet<Day> {
        val existingAvailableDays = handheld.availableDays
        val incomingAvailableDays = if (handheldDto.availableDays.isEmpty()) {
            throw IllegalArgumentException("At least one day must be selected.")
        } else {
            handheldDto.availableDays.map { Day.fromString(it) }.toSet()
        }

        existingAvailableDays.removeIf { it !in incomingAvailableDays }
        incomingAvailableDays.forEach { if (it !in existingAvailableDays) existingAvailableDays.add(it) }

        return existingAvailableDays
    }

    private fun prepareHandheldOrderTypes(handheldDto: HandheldDto, handheld: Handheld): MutableSet<OrderType> {
        val existingOrderTypes = handheld.orderTypes
        val incomingOrderTypes = if (handheldDto.orderTypes.isEmpty()) {
            throw IllegalArgumentException("At least one order type must be selected.")
        } else {
            handheldDto.orderTypes.map { OrderType.fromString(it) }.toSet()
        }

        existingOrderTypes.removeIf { it !in incomingOrderTypes }
        incomingOrderTypes.forEach { if (it !in existingOrderTypes) existingOrderTypes.add(it) }

        return existingOrderTypes
    }

    private fun prepareHandheldImages(handheldDto: HandheldDto, handheld: Handheld): MutableSet<HandheldImage> {
        if (handheldDto.handheldImages.isEmpty() || handheldDto.handheldImages.size < 2) {
            throw IllegalArgumentException("Please add at least 2 images of the food")
        }

        val existingImages = handheld.handheldImages.associateBy { it.id }

        val updatedImages = handheldDto.handheldImages.map { dto ->
            val image = existingImages[dto.id] ?: HandheldImage().apply { this.handheld = handheld }
            image.imageUrl = dto.imageUrl
            image
        }

        handheld.handheldImages.removeIf { existing -> updatedImages.none { it.id == existing.id } }
        updatedImages.forEach { updated ->
            if (handheld.handheldImages.none { it.id == updated.id }) handheld.handheldImages.add(updated)
        }

        return handheld.handheldImages
    }

    private fun changeHandheldSizeAvailability(handheldAvailabilityDto: HandheldAvailabilityDto, handheld: Handheld) {
        val handheldSizesToToggle = handheldAvailabilityDto.handheldSizes
        if (handheldSizesToToggle.isNullOrEmpty()) throw IllegalArgumentException("Please select at least one handheld size.")

        val handheldSizes = handheld.handheldSizes.associateBy { it.id }
        handheldSizesToToggle.forEach { dto ->
            val sizeId = dto.sizeId ?: throw IllegalArgumentException("Size ID cannot be null.")
            val handheldSize =
                handheldSizes[dto.sizeId] ?: throw IllegalArgumentException("Size with $sizeId cannot be found.")

            if (handheldSize.availability != Availability.fromString(dto.availability)) {
                handheldSize.availability = Availability.fromString(dto.availability)
            }
        }
    }
}

