package com.qinet.feastique.service.consumables

import com.qinet.feastique.common.mapper.toResponse
import com.qinet.feastique.exception.*
import com.qinet.feastique.model.dto.DessertAvailabilityDto
import com.qinet.feastique.model.dto.FlavourAvailabilityDto
import com.qinet.feastique.model.dto.consumables.DessertDto
import com.qinet.feastique.model.dto.consumables.DessertFlavourSizeDto
import com.qinet.feastique.model.entity.Menu
import com.qinet.feastique.model.entity.consumables.dessert.Dessert
import com.qinet.feastique.model.entity.consumables.flavour.DessertFlavour
import com.qinet.feastique.model.entity.discount.DessertDiscount
import com.qinet.feastique.model.entity.discount.Discount
import com.qinet.feastique.model.entity.image.DessertImage
import com.qinet.feastique.model.entity.size.DessertFlavourSize
import com.qinet.feastique.model.entity.user.Vendor
import com.qinet.feastique.model.enums.*
import com.qinet.feastique.repository.MenuRepository
import com.qinet.feastique.repository.consumables.dessert.DessertRepository
import com.qinet.feastique.repository.discount.DiscountRepository
import com.qinet.feastique.repository.user.VendorRepository
import com.qinet.feastique.response.consumables.dessert.DessertResponse
import com.qinet.feastique.security.UserSecurity
import com.qinet.feastique.utility.DuplicateUtility
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*
import kotlin.jvm.optionals.getOrElse

/**
 * Service class for managing desserts, including CRUD operations and related business logic.
 * Handles creation and updating of desserts with validation, uniqueness checks, and management
 * of nested collections (order types, flavours, sizes, images, discounts) while ensuring proper
 * ownership and permissions for vendors.
 */
@Service
class DessertService(
    private val dessertRepository: DessertRepository,
    private val vendorRepository: VendorRepository,
    private val duplicateUtility: DuplicateUtility,
    private val menuRepository: MenuRepository,
    private val discountRepository: DiscountRepository
) {

    /**
     * Retrieves a dessert by its ID for a given vendor.
     * @param [UUID] dessertId
     * @param [UserSecurity] vendorDetails
     *
     * @return [Dessert]
     *
     * @throws RequestedEntityNotFoundException
     * @throws PermissionDeniedException
     */
    @Transactional(readOnly = true)
    fun getDessert(dessertId: UUID, vendorDetails: UserSecurity): Dessert {
        val dessert = dessertRepository.findById(dessertId)
            .orElseThrow { RequestedEntityNotFoundException("No dessert found for id: $dessertId") }
            .also {
                if (it.vendor.id != vendorDetails.id) {
                    throw PermissionDeniedException("You do not have permission to access this dessert.")
                }
            }
        return dessert
    }

    /**
     * Retrieves all desserts for a given vendor.
     * @param [UserSecurity] vendorDetails
     *
     * @return [List<Dessert>]
     *
     * @throws RequestedEntityNotFoundException
     * @throws PermissionDeniedException
     */
    @Transactional(readOnly = true)
    fun getAllDesserts(vendorDetails: UserSecurity, page: Int, size: Int): Page<DessertResponse> {
        val pageable = PageRequest.of(page, size, Sort.by("name").ascending())
        val dessertResponses = dessertRepository.findAllByVendorId(vendorDetails.id, pageable).map { it.toResponse() }
        return dessertResponses
    }

    /**
     * Deletes a dessert and all its relationships.
     * @param [UUID] dessertId
     * @param [UserSecurity] vendorDetails
     *
     * @throws RequestedEntityNotFoundException
     * @throws PermissionDeniedException
     */
    @Transactional
    fun deleteDessert(dessertId: UUID, vendorDetails: UserSecurity) {
        val dessert = getDessert(dessertId, vendorDetails)
        dessertRepository.delete(dessert)
    }

    /**
     * Saves a dessert entity to the database. This is a helper method used to persist the dessert
     * before managing its nested collections, ensuring it becomes a managed entity in the current
     * transaction context.
     * @param [Dessert] dessert
     *
     * @return [Dessert] the persisted dessert entity
     */
    @Transactional
    fun saveDessert(dessert: Dessert): Dessert {
        return dessertRepository.saveAndFlush(dessert)
    }

    /**
     * Changes the availability of a dessert and its flavours based on the incoming DTO.
     * Validates the provided availability status, updates the dessert's availability, and
     * delegates to changeDessertFlavourAvailability to update the availability of nested flavours.
     * @param [DessertAvailabilityDto] containing the new availability status and flavour availabilities
     * @param [UUID] dessertId
     * @param [UserSecurity] vendorDetails for ownership verification
     *
     * @return [Dessert] the updated dessert entity with new availability statuses
     *
     * @throws IllegalArgumentException when the provided availability status is invalid
     * @throws RequestedEntityNotFoundException when the dessert or any specified flavour is not found
     * @throws PermissionDeniedException when the vendor does not own the dessert
     */
    @Transactional
    fun changeDessertAvailability(dessertAvailabilityDto: DessertAvailabilityDto, id: UUID, vendorDetails: UserSecurity): Dessert {
        val dessert = getDessert(id, vendorDetails)
        if (dessert.availability != Availability.fromString(dessertAvailabilityDto.availability)) {
            dessert.availability = Availability.fromString(dessertAvailabilityDto.availability)
        }

        dessertAvailabilityDto.dessertFlavours?.let {
            changeDessertFlavourAvailability(dessertAvailabilityDto, dessert)
        }
        return saveDessert(dessert)
    }

    /**
     * Create or update a Dessert for the authenticated vendor.
     *
     * Summary:
     * - Create: when `dessertDto.id` is null — a new Dessert is created and linked to the vendor.
     * - Update: when `dessertDto.id` is provided — the existing Dessert is loaded, ownership
     *   is verified, and the entity is updated.
     *
     * Required fields (in DTO):
     * - `dessertName`, `description`, and `dessertType` must be provided.
     *
     * Behavior & side effects:
     * - Enforces uniqueness of dessert names per vendor; creating or renaming to a
     *   name that already exists for the same vendor will throw [DuplicateFoundException].
     * - Nested collections (`orderType`, `dessertFlavours`) are reconciled by helper
     *   methods (`prepareDessertOrderTypes`, `prepareDessertFlavours`) which reuse existing
     *   child entities, create new ones, and remove children missing from the DTO.
     * - Runs inside a transaction; the returned entity is the persisted state.
     *
     * @param [dessertDto] DTO containing dessert fields and nested collections
     * @param [vendorDetails] authenticated vendor security context used to load/verify the vendor
     * @return [Dessert] the persisted Dessert entity (new or updated)
     *
     * @throws UserNotFoundException when the vendor cannot be found
     * @throws RequestedEntityNotFoundException when updating a non-existent Dessert id
     * @throws PermissionDeniedException when attempting to update a Dessert owned by another vendor
     * @throws IllegalArgumentException when required fields are missing or invalid
     * @throws DuplicateFoundException when a dessert with the same name already exists for the vendor
     */
    @Transactional
    fun addOrUpdateDessert(dessertDto: DessertDto, vendorDetails: UserSecurity): Dessert {
        val vendor = vendorRepository.findById(vendorDetails.id)
            .orElseThrow { UserNotFoundException("Vendor not found.") }

        val dessert: Dessert = if (dessertDto.id != null) {
            getDessert(dessertDto.id!!, vendorDetails)

        } else {
            Dessert().apply {
                this.vendor = vendor
            }
        }

        // Input validation
        val dessertName = requireNotNull(dessertDto.dessertName) { "Please enter a dessert name." }
        val dessertDescription = requireNotNull(dessertDto.description) { "Please enter a description." }
        val dessertType = DessertType.fromString(dessertDto.dessertType)
        val deliveryFee = dessertDto.deliveryFee
        val deliverable = requireNotNull(dessertDto.deliverable) { "Please specify if dessert is deliverable." }
        val dailyDeliveryQuantity = dessertDto.dailyDeliveryQuantity
        val availability = requireNotNull(dessertDto.availability) { "Please select availability status."}
        val readyAsFrom = dessertDto.readyAsFrom ?: vendor.openingTime
        val preparationTime = dessertDto.preparationTime

        // Prevent duplicate dessert names for the same vendor
        // dessertDto.id == null = true -> when adding a new dessert
        // dessertDto.dessertName != dessert.name == false -> when the name of an existing dessert has been changed.
        if (dessertDto.id == null || dessertDto.dessertName != dessert.name) {
            if (duplicateUtility.isDuplicateDessertFound(dessertName, vendor.id)) {
                throw DuplicateFoundException("A dessert with the name $dessertName already exists.")
            }
            dessert.name = dessertName
        }

        // Basic info
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

        // Save dessert to make it managed by JPA
        val managedDessert = saveDessert(dessert)

        managedDessert.availability = Availability.fromString(availability)
        managedDessert.dessertOrderTypes = prepareDessertOrderTypes(dessertDto, managedDessert)
        managedDessert.dessertFlavours = prepareDessertFlavours(dessertDto, managedDessert)
        managedDessert.availableDays = prepareDessertAvailableDays(dessertDto, managedDessert)
        managedDessert.dessertImages = prepareDessertImages(dessertDto, managedDessert)

        val menu = prepareMenu(dessert)
        managedDessert.menu = menuRepository.save(menu)

        dessertDto.discounts?.let { prepareDessertDiscounts(dessertDto, managedDessert, vendor) } ?: managedDessert.dessertDiscounts.clear()

        return dessertRepository.save(managedDessert)
    }

    private fun prepareMenu(dessert: Dessert): Menu {
        val menu = if (dessert.menu != null) {
            menuRepository.findById(dessert.menu!!.id)
                .getOrElse { throw RequestedEntityNotFoundException("Menu item not found.") }

        } else {
            Menu().apply {
                this.dessert = dessert
            }
        }

        dessert.dessertOrderTypes.forEach { orderType ->
            when(orderType) {
                OrderType.DELIVERY -> menu.delivery = true  // null -> sold out, false -> option not offered, true -> option available
                OrderType.DINE_IN -> menu.dineIn = true     // null -> sold out, false -> option not offered, true -> option available
                OrderType.PICKUP -> menu.pickup = true     // null -> sold out, false -> option not offered, true -> option available
                else -> { throw IllegalArgumentException("Unknown order type selected.") }
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

        incomingAvailableDays.forEach { availableDay ->
            if(availableDay !in existingAvailableDays) {
                existingAvailableDays.add(availableDay)
            }
        }

        return existingAvailableDays
    }
    private fun prepareDessertDiscounts(dessertDto: DessertDto, dessert: Dessert, vendor: Vendor) {

        val existingDiscounts = dessert.dessertDiscounts.associateBy { it.discount.id }
        val incomingDiscounts = dessertDto.discounts!!

        val updatedDessertDiscounts = incomingDiscounts.map { dto ->

            // Use existing discount or create new one
            var discount = dto.id?.let { id ->
                // Reuse the existing managed discount if it exists
                discountRepository.findById(id)
                    .orElseThrow { RequestedEntityNotFoundException ("Discount with id $id not found") }
            } ?: Discount().apply {
                this.vendor = vendor
            }

            // Update fields whether existing or new
            discount.apply {
                discountName = requireNotNull(dto.discountName)
                percentage = requireNotNull(dto.percentage)
                startDate = requireNotNull(dto.startDate)
                endDate = requireNotNull(dto.endDate)
            }

            discount = discountRepository.save(discount)

            // Reuse the existing DessertDiscount or create a new bridge entity
            val dessertDiscount = dto.id?.let { existingDiscounts[it] }
                ?: DessertDiscount().apply { this.dessert = dessert }

            dessertDiscount.discount = discount
            dessertDiscount
        }

        // remove old entities not present in incoming DTO
        dessert.dessertDiscounts.removeIf { existingDessertDiscount ->
            updatedDessertDiscounts.none { it.discount.id == existingDessertDiscount.discount.id }
        }

        // add updated/new dessertDiscounts
        updatedDessertDiscounts.forEach { updatedDessertDiscount ->
            if (dessert.dessertDiscounts.none { it.discount.id == updatedDessertDiscount.discount.id }) {
                dessert.dessertDiscounts.add(updatedDessertDiscount)
            }
        }
    }

    /**
     * Builds or updates the set of order types for a dessert from the incoming DTO.
     * Reuses existing DessertOrderCategory entities when an id is present, updates their
     * orderType field, creates new entities for DTO entries without an id, and
     * removes any persisted order types that are no longer present in the DTO.
     * @param [DessertDto]
     * @param [Dessert]
     *
     * @return `MutableSet<DessertOrderCategory>`
     *
     * @throws IllegalArgumentException when an order type string is missing or invalid
     */
    private fun prepareDessertOrderTypes(dessertDto: DessertDto, dessert: Dessert): MutableSet<OrderType> {
        val existingOrderTypes = dessert.dessertOrderTypes

        val incomingOrderTypes = if (dessertDto.orderTypes.isEmpty()) {
            throw IllegalArgumentException("At least one order type must be selected.")
        } else {
            dessertDto.orderTypes.map { OrderType.fromString(it) }.toSet()
        }


        existingOrderTypes.removeIf { it !in incomingOrderTypes }

        // Add any new types that don't already exist
        incomingOrderTypes.forEach { orderType ->
            if (orderType !in existingOrderTypes) {
                existingOrderTypes.add(orderType)
            }
        }

        return existingOrderTypes
    }

    /**
     * Builds or updates the list of dessert flavours from the incoming DTO.
     * This will reuse existing flavour entities when an id is present, update their
     * fields, remove flavours that are no longer present in the DTO, and create
     * new flavour entities for DTO entries without an id.
     * @param [DessertDto]
     * @param [Dessert]
     *
     * @return `List<DessertFlavour>`
     *
     * @throws IllegalArgumentException when a flavour name is missing or invalid
     */
    private fun prepareDessertFlavours(dessertDto: DessertDto, dessert: Dessert): MutableList<DessertFlavour> {
        val existingFlavours = dessert.dessertFlavours.associateBy { it.id }

        val updatedFlavours = dessertDto.dessertFlavours.map { flavourDto ->
            val flavour = existingFlavours[flavourDto.id] ?: DessertFlavour().apply {
                this.dessert = dessert
            }

            flavour.apply {
                name = requireNotNull(flavourDto.flavourName) { "Please enter a flavour name." }
                description = flavourDto.description
                dessertFlavourSizes = prepareDessertFlavourSizes(flavourDto.flavourSizes, flavour)
            }
        }

        dessert.dessertFlavours.removeIf { existingFlavour ->
            updatedFlavours.none { it.id == existingFlavour.id }
        }

        updatedFlavours.forEach { updatedFlavour ->
            if (dessert.dessertFlavours.none { it.id == updatedFlavour.id }) {
                dessert.dessertFlavours.add(updatedFlavour)
            }
        }

        return dessert.dessertFlavours
    }

    /**
     * Builds or updates the set of sizes for a flavour option.
     * Reuses existing consumableSize entities when ids are provided, removes sizes not
     * present in the incoming DTO list, and creates new consumableSize entities as needed.
     * Validates the provided consumableSize, name and price fields.
     * @param List<DessertFlavourSizeDto>
     * @param [DessertFlavour]
     *
     * @return [MutableSet<DessertFlavourSize>]
     *
     * @throws IllegalArgumentException when consumableSize, name or price are missing/invalid
     */
    private fun prepareDessertFlavourSizes(flavourSizeDtos: List<DessertFlavourSizeDto>, dessertFlavour: DessertFlavour): MutableSet<DessertFlavourSize> {

        val existingFlavourSizes = dessertFlavour.dessertFlavourSizes.associateBy { it.id }
        if (flavourSizeDtos.isEmpty()) {
            throw IllegalArgumentException("Please select at least one dessert flavour size.")
        }

        val updatedFlavourSizes = flavourSizeDtos.map { optionSizeDto ->
            val flavourSize = existingFlavourSizes[optionSizeDto.id] ?: DessertFlavourSize().apply {
                this.dessertFlavour = dessertFlavour
            }

            flavourSize.apply {
                this.size = Size.fromString(requireNotNull(optionSizeDto.size))
                name = (optionSizeDto.sizeName) ?: flavourSize.size!!.name
                price = requireNotNull(optionSizeDto.price) { "Please provide a price." }
                availability = requireNotNull(Availability.fromString(optionSizeDto.availability!!))
            }

        }

        dessertFlavour.dessertFlavourSizes.removeIf { existingSize ->
            updatedFlavourSizes.none { it.id == existingSize.id }
        }

        updatedFlavourSizes.forEach { updatedSize ->
            if (dessertFlavour.dessertFlavourSizes.none { it.id == updatedSize.id }) {
                dessertFlavour.dessertFlavourSizes.add(updatedSize)
            }
        }

        return dessertFlavour.dessertFlavourSizes
    }

    private fun prepareDessertImages(dessertDto: DessertDto, dessert: Dessert): MutableSet<DessertImage> {
        val existingDessertImages = dessert.dessertImages.associateBy { it.id }
        val incomingImages = dessertDto.dessertImages

        if (incomingImages.isEmpty() || incomingImages.size < 2) {
            throw IllegalArgumentException("Please add at least 2 images of the food")
        }

        val updatedFoodImages = incomingImages.map { dto ->
            val image = existingDessertImages[dto.id] ?: DessertImage().apply { this.dessert = dessert }
            image.imageUrl = dto.imageUrl
            image
        }

        dessert.dessertImages.removeIf { existingImage ->
            updatedFoodImages.none { it.id == existingImage.id }
        }

        updatedFoodImages.forEach { updatedImage ->
            if (dessert.dessertImages.none { it.id == updatedImage.id }) {
                dessert.dessertImages.add(updatedImage)
            }
        }

        return dessert.dessertImages
    }

    /**
     * Changes the availability of dessert flavours based on the incoming DTO.
     * Validates that at least one flavour is provided, that each flavour has a valid ID and availability status,
     * and that the corresponding flavour entities exist. Updates the availability of each flavour accordingly,
     * and delegates to toggleDessertFlavourSizeAvailability to update the availability of nested sizes.
     * @param [DessertAvailabilityDto] containing the list of flavours with their new availability statuses
     * @param [Dessert] the parent dessert entity whose flavours are being updated
     *
     * @throws IllegalArgumentException when no flavours are provided, when a flavour ID is null, or when an availability status is invalid
     * @throws RequestedEntityNotFoundException when a dessert flavour with a provided ID does not exist
     */
    private fun changeDessertFlavourAvailability(dessertAvailabilityDto: DessertAvailabilityDto, dessert: Dessert) {
        val flavours = dessert.dessertFlavours.associateBy { it.id }
        val incomingFlavours = dessertAvailabilityDto.dessertFlavours

        if (incomingFlavours.isNullOrEmpty()) {
            throw IllegalArgumentException("At least one flavour must be selected.")
        }
        incomingFlavours.forEach { dto ->
            val flavourId = dto.flavourId ?: throw IllegalArgumentException("Flavour ID cannot be null.")
            val flavour = flavours[dto.flavourId] ?: throw RequestedEntityNotFoundException("Dessert flavour with id $flavourId not found.")

            if (flavour.availability != Availability.fromString(dto.availability)) {
                flavour.availability = Availability.fromString(dto.availability)
            }

            dto.flavourSizes?.let { changeDessertFlavourSizeAvailability(dto, flavour) }
        }
    }

    /**
     * Changes the availability of dessert flavour sizes based on the incoming DTO.
     * Validates that at least one size is provided, that each size has a valid ID and availability status,
     * and that the corresponding flavour size entities exist. Updates the availability of each flavour size accordingly.
     * @param [DessertAvailabilityDto] containing the list of flavour sizes with their new availability statuses
     * @param [DessertFlavour] the parent flavour entity whose sizes are being updated
     *
     * @throws IllegalArgumentException when no sizes are provided, when a size ID is null, or when an availability status is invalid
     * @throws RequestedEntityNotFoundException when a flavour size with a provided ID does not exist
     */
    private fun changeDessertFlavourSizeAvailability(flavourAvailabilityDto: FlavourAvailabilityDto, dessertFlavour: DessertFlavour) {
        val flavourSizes = dessertFlavour.dessertFlavourSizes.associateBy { it.id }
        val incomingSizes = flavourAvailabilityDto.flavourSizes

        if (incomingSizes.isNullOrEmpty()) {
            throw IllegalArgumentException("At least one flavour size must be selected.")
        }
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

