package com.qinet.feastique.service.consumables

import com.qinet.feastique.common.mapper.toResponse
import com.qinet.feastique.exception.DuplicateFoundException
import com.qinet.feastique.exception.PermissionDeniedException
import com.qinet.feastique.exception.RequestedEntityNotFoundException
import com.qinet.feastique.exception.UserNotFoundException
import com.qinet.feastique.model.dto.DessertAvailabilityDto
import com.qinet.feastique.model.dto.FlavourAvailabilityDto
import com.qinet.feastique.model.dto.consumables.DessertDto
import com.qinet.feastique.model.dto.consumables.DessertFlavourSizeDto
import com.qinet.feastique.model.entity.menu.Menu
import com.qinet.feastique.model.entity.consumables.dessert.Dessert
import com.qinet.feastique.model.entity.consumables.flavour.DessertFlavour
import com.qinet.feastique.model.entity.discount.DessertDiscount
import com.qinet.feastique.model.entity.discount.Discount
import com.qinet.feastique.model.entity.image.DessertImage
import com.qinet.feastique.model.entity.size.DessertFlavourSize
import com.qinet.feastique.model.entity.user.Vendor
import com.qinet.feastique.model.enums.*
import com.qinet.feastique.repository.bookmark.DessertBookmarkRepository
import com.qinet.feastique.repository.menu.MenuRepository
import com.qinet.feastique.repository.consumables.dessert.DessertRepository
import com.qinet.feastique.repository.discount.DiscountRepository
import com.qinet.feastique.repository.like.DessertLikeRepository
import com.qinet.feastique.repository.user.VendorRepository
import com.qinet.feastique.response.consumables.dessert.DessertResponse
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
class DessertService(
    private val dessertRepository: DessertRepository,
    private val vendorRepository: VendorRepository,
    private val duplicateUtility: DuplicateUtility,
    private val menuRepository: MenuRepository,
    private val discountRepository: DiscountRepository,
    private val dessertLikeRepository: DessertLikeRepository,
    private val cursorEncoder: CursorEncoder,
    private val securityUtility: SecurityUtility,
    private val dessertBookmarkRepository: DessertBookmarkRepository
) {

    @Suppress("unused")
    private val logger = LoggerFactory.getLogger(javaClass)

    @Transactional(readOnly = true)
    fun getDessert(dessertId: UUID, userDetails: UserSecurity): DessertResponse {
        val role = securityUtility.getSingleRole(userDetails)
        var liked = false
        var bookmarked = false

        val dessert = when (role) {
            "CUSTOMER" -> {
                liked = dessertLikeRepository.existsByDessertIdAndCustomerId(dessertId, userDetails.id)
                bookmarked = dessertBookmarkRepository.existsByDessertIdAndCustomerId(dessertId, userDetails.id)
                dessertRepository.findById(dessertId)
                    .getOrElse { throw RequestedEntityNotFoundException() }
            }
            "VENDOR" -> dessertRepository.findByIdAndVendorIdAndIsActiveTrue(dessertId, userDetails.id)
                ?: throw RequestedEntityNotFoundException()

            else -> throw IllegalArgumentException("Unsupported role: $role")
        }

        return dessert.toResponse(liked, bookmarked)
    }

    @Transactional(readOnly = true)
    fun getDessertById(dessertId: UUID, vendorDetails: UserSecurity): Dessert {
        return dessertRepository.findByIdAndVendorIdAndIsActiveTrue(dessertId, vendorDetails.id)
            ?: throw RequestedEntityNotFoundException("No dessert found for id: $dessertId")
    }

    @Transactional(readOnly = true)
    fun getAllDesserts(vendorDetails: UserSecurity, page: Int, size: Int): Page<DessertResponse> {
        val pageable = PageRequest.of(page, size, Sort.by("name").ascending())
        return dessertRepository.findAllByVendorIdAndIsActiveTrue(vendorDetails.id, pageable).map { it.toResponse() }
    }

    @Transactional(readOnly = true)
    fun scrollDesserts(
        vendorId: UUID,
        cursor: String?,
        size: Int = Constants.DEFAULT_PAGE_SIZE.type,
        userDetails: UserSecurity
    ): WindowResponse<DessertResponse> {

        val currentOffset: Long = cursor?.toLongOrNull() ?: 0L
        val sort = Sort.by("name").ascending()
        val scrollPosition = if (currentOffset == 0L) ScrollPosition.offset() else ScrollPosition.offset(currentOffset)
        val window = dessertRepository.findAllByVendorIdAndIsActiveTrue(vendorId, scrollPosition, sort, Limit.of(size))

        val isCustomer = securityUtility.getSingleRole(userDetails) == "CUSTOMER"
        val dessertIds = if (isCustomer) window.toList().map { it.id } else emptyList()

        val likedDessertIds: Set<UUID> = if (isCustomer) {
            dessertLikeRepository.findAllByCustomerIdAndDessertIdIn(userDetails.id, dessertIds)
                .map { it.dessert.id }
                .toHashSet()
        } else emptySet()

        val bookmarkedDessertIds: Set<UUID> = if (isCustomer) {
            dessertBookmarkRepository.findAllByCustomerIdAndDessertIdIn(userDetails.id, dessertIds)
                .map { it.dessert.id }
                .toHashSet()
        } else emptySet()

        return window.map { it.toResponse(it.id in likedDessertIds, it.id in bookmarkedDessertIds) }
            .toResponse(currentOffset) { cursorEncoder.encodeOffset(it) }
    }

    @Transactional
    fun deleteDessert(dessertId: UUID, vendorDetails: UserSecurity) {
        val dessert = getDessertById(dessertId, vendorDetails)
        dessert.isActive = false
        saveDessert(dessert)
    }

    @Transactional
    fun saveDessert(dessert: Dessert): Dessert {
        return dessertRepository.saveAndFlush(dessert)
    }

    @Transactional
    fun changeDessertAvailability(
        dessertAvailabilityDto: DessertAvailabilityDto,
        id: UUID,
        vendorDetails: UserSecurity
    ): Dessert {
        val dessert = getDessertById(id, vendorDetails)
        if (dessert.availability != Availability.fromString(dessertAvailabilityDto.availability)) {
            dessert.availability = Availability.fromString(dessertAvailabilityDto.availability)
        }
        dessertAvailabilityDto.dessertFlavours?.let { changeDessertFlavourAvailability(dessertAvailabilityDto, dessert) }
        return saveDessert(dessert)
    }

    /**
     * Creates or updates a dessert for the authenticated vendor.
     *
     * - Create: when [dessertDto].id is null.
     * - Update: when [dessertDto].id is provided — ownership is verified before updating.
     *
     * Enforces unique dessert names per vendor. Nested collections (orderTypes, flavours,
     * images, discounts) are reconciled: existing children are reused or updated, and
     * children absent from the DTO are removed.
     *
     * @throws UserNotFoundException when the vendor cannot be found
     * @throws RequestedEntityNotFoundException when updating a non-existent dessert id
     * @throws PermissionDeniedException when the dessert belongs to another vendor
     * @throws DuplicateFoundException when the name already exists for this vendor
     * @throws IllegalArgumentException when required fields are missing or invalid
     */
    @Transactional
    fun addOrUpdateDessert(dessertDto: DessertDto, vendorDetails: UserSecurity): Dessert {
        val vendor = vendorRepository.findById(vendorDetails.id)
            .orElseThrow { UserNotFoundException("Vendor not found.") }

        val dessert: Dessert = if (dessertDto.id != null) {
            getDessertById(dessertDto.id!!, vendorDetails)
        } else {
            Dessert().apply { this.vendor = vendor }
        }

        val dessertName = requireNotNull(dessertDto.dessertName) { "Please enter a dessert name." }
        val dessertDescription = requireNotNull(dessertDto.description) { "Please enter a description." }
        val dessertType = DessertType.fromString(dessertDto.dessertType)
        val deliveryFee = dessertDto.deliveryFee
        val deliverable = requireNotNull(dessertDto.deliverable) { "Please specify if dessert is deliverable." }
        val dailyDeliveryQuantity = dessertDto.dailyDeliveryQuantity
        val availability = requireNotNull(dessertDto.availability) { "Please select availability status." }
        val readyAsFrom = dessertDto.readyAsFrom ?: vendor.openingTime
        val preparationTime = dessertDto.preparationTime

        // Check for duplicate name only on create, or if the name has changed
        if (dessertDto.id == null || dessertDto.dessertName != dessert.name) {
            if (duplicateUtility.isDuplicateDessertFound(dessertName, vendor.id)) {
                throw DuplicateFoundException("A dessert with the name $dessertName already exists.")
            }
            dessert.name = dessertName
        }

        dessert.apply {
            description = dessertDescription
            this.deliveryFee = deliveryFee
            this.vendor = vendor
            this.dessertType = dessertType
            this.deliverable = deliverable
            this.dailyDeliveryQuantity = dailyDeliveryQuantity
            this.preparationTime = preparationTime
            this.readyAsFrom = readyAsFrom
        }

        val managedDessert = saveDessert(dessert)

        managedDessert.availability = Availability.fromString(availability)
        managedDessert.dessertOrderTypes = prepareDessertOrderTypes(dessertDto, managedDessert)
        managedDessert.dessertFlavours = prepareDessertFlavours(dessertDto, managedDessert)
        managedDessert.availableDays = prepareDessertAvailableDays(dessertDto, managedDessert)
        managedDessert.dessertImages = prepareDessertImages(dessertDto, managedDessert)

        val menu = prepareMenu(dessert)
        managedDessert.menu = menuRepository.save(menu)

        dessertDto.discounts?.let { prepareDessertDiscounts(dessertDto, managedDessert, vendor) }
            ?: managedDessert.dessertDiscounts.clear()

        return dessertRepository.save(managedDessert)
    }

    private fun prepareMenu(dessert: Dessert): Menu {
        val menu = if (dessert.menu != null) {
            menuRepository.findById(dessert.menu!!.id)
                .getOrElse { throw RequestedEntityNotFoundException("Menu item not found.") }
        } else {
            Menu().apply { this.dessert = dessert }
        }

        dessert.dessertOrderTypes.forEach { orderType ->
            when (orderType) {
                // null -> sold out, false -> not offered, true -> available
                OrderType.DELIVERY -> menu.delivery = true
                OrderType.DINE_IN  -> menu.dineIn   = true
                OrderType.PICKUP   -> menu.pickup   = true
                else -> throw IllegalArgumentException("Unknown order type selected.")
            }
        }
        return menu
    }

    private fun prepareDessertAvailableDays(dessertDto: DessertDto, dessert: Dessert): MutableSet<Day> {
        val existingAvailableDays = dessert.availableDays
        val incomingAvailableDays = if (dessertDto.availableDays.isEmpty()) {
            throw IllegalArgumentException("At least one day must be selected.")
        } else {
            dessertDto.availableDays.map { Day.fromString(it) }.toSet()
        }

        existingAvailableDays.removeIf { it !in incomingAvailableDays }
        incomingAvailableDays.forEach { if (it !in existingAvailableDays) existingAvailableDays.add(it) }

        return existingAvailableDays
    }

    private fun prepareDessertDiscounts(dessertDto: DessertDto, dessert: Dessert, vendor: Vendor) {
        val existingDiscounts = dessert.dessertDiscounts.associateBy { it.discount.id }
        val incomingDiscounts = dessertDto.discounts!!

        val updatedDessertDiscounts = incomingDiscounts.map { dto ->
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

            val dessertDiscount = dto.id?.let { existingDiscounts[it] }
                ?: DessertDiscount().apply { this.dessert = dessert }

            dessertDiscount.discount = discount
            dessertDiscount
        }

        dessert.dessertDiscounts.removeIf { existing ->
            updatedDessertDiscounts.none { it.discount.id == existing.discount.id }
        }
        updatedDessertDiscounts.forEach { updated ->
            if (dessert.dessertDiscounts.none { it.discount.id == updated.discount.id }) {
                dessert.dessertDiscounts.add(updated)
            }
        }
    }

    private fun prepareDessertOrderTypes(dessertDto: DessertDto, dessert: Dessert): MutableSet<OrderType> {
        val existingOrderTypes = dessert.dessertOrderTypes
        val incomingOrderTypes = if (dessertDto.orderTypes.isEmpty()) {
            throw IllegalArgumentException("At least one order type must be selected.")
        } else {
            dessertDto.orderTypes.map { OrderType.fromString(it) }.toSet()
        }

        existingOrderTypes.removeIf { it !in incomingOrderTypes }
        incomingOrderTypes.forEach { if (it !in existingOrderTypes) existingOrderTypes.add(it) }

        return existingOrderTypes
    }

    private fun prepareDessertFlavours(dessertDto: DessertDto, dessert: Dessert): MutableSet<DessertFlavour> {
        val existingFlavours = dessert.dessertFlavours.associateBy { it.id }

        val updatedFlavours = dessertDto.dessertFlavours.map { flavourDto ->
            val flavour = existingFlavours[flavourDto.id] ?: DessertFlavour().apply { this.dessert = dessert }
            flavour.apply {
                name = requireNotNull(flavourDto.flavourName) { "Please enter a flavour name." }
                description = flavourDto.description
                dessertFlavourSizes = prepareDessertFlavourSizes(flavourDto.flavourSizes, flavour)
                isActive = true
            }
        }

        dessert.dessertFlavours.forEach { existing ->
            if (updatedFlavours.none { it.id == existing.id }) {
                existing.isActive = false
                existing.dessertFlavourSizes.forEach { it.isActive = false }
            }
        }

        updatedFlavours.forEach { updated ->
            if (dessert.dessertFlavours.none { it.id == updated.id }) dessert.dessertFlavours.add(updated)
        }

        return dessert.dessertFlavours
    }

    private fun prepareDessertFlavourSizes(
        flavourSizeDtos: List<DessertFlavourSizeDto>,
        dessertFlavour: DessertFlavour
    ): MutableSet<DessertFlavourSize> {
        if (flavourSizeDtos.isEmpty()) throw IllegalArgumentException("Please select at least one dessert flavour size.")

        val existingFlavourSizes = dessertFlavour.dessertFlavourSizes.associateBy { it.id }

        val updatedFlavourSizes = flavourSizeDtos.map { dto ->
            val flavourSize = existingFlavourSizes[dto.id] ?: DessertFlavourSize().apply { this.dessertFlavour = dessertFlavour }
            flavourSize.apply {
                this.size = Size.fromString(requireNotNull(dto.size))
                name = dto.sizeName ?: flavourSize.size!!.name
                price = requireNotNull(dto.price) { "Please provide a price." }
                availability = requireNotNull(Availability.fromString(dto.availability!!))
                isActive = true
            }
        }

        dessertFlavour.dessertFlavourSizes.forEach { existing ->
            if (updatedFlavourSizes.none { it.id == existing.id } ) existing.isActive = false
        }
        updatedFlavourSizes.forEach { updated ->
            if (dessertFlavour.dessertFlavourSizes.none { it.id == updated.id })
                dessertFlavour.dessertFlavourSizes.add(updated)
        }

        return dessertFlavour.dessertFlavourSizes
    }

    private fun prepareDessertImages(dessertDto: DessertDto, dessert: Dessert): MutableSet<DessertImage> {
        val incomingImages = dessertDto.dessertImages
        if (incomingImages.isEmpty() || incomingImages.size < 2) {
            throw IllegalArgumentException("Please add at least 2 images of the food")
        }

        val existingImages = dessert.dessertImages.associateBy { it.id }

        val updatedImages = incomingImages.map { dto ->
            val image = existingImages[dto.id] ?: DessertImage().apply { this.dessert = dessert }
            image.imageUrl = dto.imageUrl
            image
        }

        dessert.dessertImages.removeIf { existing -> updatedImages.none { it.id == existing.id } }
        updatedImages.forEach { updated ->
            if (dessert.dessertImages.none { it.id == updated.id }) dessert.dessertImages.add(updated)
        }

        return dessert.dessertImages
    }

    private fun changeDessertFlavourAvailability(dessertAvailabilityDto: DessertAvailabilityDto, dessert: Dessert) {
        val flavours = dessert.dessertFlavours.associateBy { it.id }
        val incomingFlavours = dessertAvailabilityDto.dessertFlavours

        if (incomingFlavours.isNullOrEmpty()) throw IllegalArgumentException("At least one flavour must be selected.")

        incomingFlavours.forEach { dto ->
            val flavourId = dto.flavourId ?: throw IllegalArgumentException("Flavour ID cannot be null.")
            val flavour = flavours[dto.flavourId]
                ?: throw RequestedEntityNotFoundException("Dessert flavour with id $flavourId not found.")

            if (flavour.availability != Availability.fromString(dto.availability)) {
                flavour.availability = Availability.fromString(dto.availability)
            }
            dto.flavourSizes?.let { changeDessertFlavourSizeAvailability(dto, flavour) }
        }
    }

    private fun changeDessertFlavourSizeAvailability(
        flavourAvailabilityDto: FlavourAvailabilityDto,
        dessertFlavour: DessertFlavour
    ) {
        val flavourSizes = dessertFlavour.dessertFlavourSizes.associateBy { it.id }
        val incomingSizes = flavourAvailabilityDto.flavourSizes

        if (incomingSizes.isNullOrEmpty()) throw IllegalArgumentException("At least one flavour size must be selected.")

        incomingSizes.forEach { dto ->
            val sizeId = dto.sizeId ?: throw IllegalArgumentException("Dessert flavour size ID cannot be null.")
            val flavourSize = flavourSizes[dto.sizeId]
                ?: throw RequestedEntityNotFoundException("Dessert flavour size with id $sizeId not found.")

            if (flavourSize.availability != Availability.fromString(dto.availability)) {
                flavourSize.availability = Availability.fromString(dto.availability)
            }
        }
    }
}

