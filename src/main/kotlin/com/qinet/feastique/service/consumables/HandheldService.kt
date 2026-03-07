package com.qinet.feastique.service.consumables

import com.qinet.feastique.common.mapper.toResponse
import com.qinet.feastique.exception.DuplicateFoundException
import com.qinet.feastique.exception.PermissionDeniedException
import com.qinet.feastique.exception.RequestedEntityNotFoundException
import com.qinet.feastique.exception.UserNotFoundException
import com.qinet.feastique.model.dto.HandheldAvailabilityDto
import com.qinet.feastique.model.dto.consumables.HandheldDto
import com.qinet.feastique.model.entity.Menu
import com.qinet.feastique.model.entity.consumables.filling.Filling
import com.qinet.feastique.model.entity.consumables.filling.HandheldFilling
import com.qinet.feastique.model.entity.consumables.handheld.Handheld
import com.qinet.feastique.model.entity.discount.Discount
import com.qinet.feastique.model.entity.discount.HandheldDiscount
import com.qinet.feastique.model.entity.image.HandheldImage
import com.qinet.feastique.model.entity.size.HandheldSize
import com.qinet.feastique.model.entity.user.Vendor
import com.qinet.feastique.model.enums.Availability
import com.qinet.feastique.model.enums.Day
import com.qinet.feastique.model.enums.HandHeldType
import com.qinet.feastique.model.enums.OrderType
import com.qinet.feastique.model.enums.Size
import com.qinet.feastique.repository.MenuRepository
import com.qinet.feastique.repository.consumables.filling.FillingRepository
import com.qinet.feastique.repository.consumables.handheld.HandheldRepository
import com.qinet.feastique.repository.discount.DiscountRepository
import com.qinet.feastique.repository.user.VendorRepository
import com.qinet.feastique.response.consumables.handheld.HandheldResponse
import com.qinet.feastique.security.UserSecurity
import com.qinet.feastique.utility.DuplicateUtility
import com.qinet.feastique.utility.SecurityUtility
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID
import kotlin.jvm.optionals.getOrElse


/**
 * Service class for managing handheld entities, including creation, retrieval, updating, and deletion.
 * Handles business logic related to handhelds, such as availability toggling, size management, and menu association.
 */
@Service
class HandheldService(
    private val handheldRepository: HandheldRepository,
    private val vendorRepository: VendorRepository,
    private val securityUtility: SecurityUtility,
    private val duplicateUtility: DuplicateUtility,
    private val menuRepository: MenuRepository,
    private val fillingRepository: FillingRepository,
    private val discountRepository: DiscountRepository,
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    /**
     * Saves a [Handheld] entity to the database.
     *
     * @param handheld The Handheld entity to be saved.
     * @return The saved Handheld entity with any updates from the database (e.g., generated ID).
     */
    @Transactional
    fun saveHandheld(handheld: Handheld): Handheld {
        return handheldRepository.saveAndFlush(handheld)
    }

    /**
     * Retrieves a single [Handheld] by ID.
     * Vendors may only fetch their own handhelds.
     * Customers may fetch any available handheld.
     *
     * @param id
     * @param userDetails
     * @return [Handheld]
     * @throws RequestedEntityNotFoundException
     * @throws PermissionDeniedException
     */
    @Transactional(readOnly = true)
    fun getHandheldById(id: UUID, userDetails: UserSecurity): Handheld {
        val role = securityUtility.getRole(userDetails)
        val handheld = handheldRepository.findById(id)
            .orElseThrow { RequestedEntityNotFoundException("Handheld not found.") }
            .also {
                if (role == "VENDOR" && it.vendor.id != userDetails.id) {
                    throw PermissionDeniedException("You do not have permission to view this handheld.")
                }
            }

        return handheld
    }

    /**
     * Returns all [Handheld] items.
     * Vendors get only their own; customers get all.
     *
     * @param vendorDetails
     * @return List<[Handheld]>
     */
    @Transactional(readOnly = true)
    fun getAllHandhelds(vendorDetails: UserSecurity, page: Int, size: Int): Page<HandheldResponse> {
        val pageable = PageRequest.of(page, size, Sort.by("name").ascending())
        val handheldResponses = handheldRepository.findAllByVendorId(vendorDetails.id, pageable).map { it.toResponse() }
        return handheldResponses
    }

    /**
     * Deletes a [Handheld] by ID.
     * Only the owning vendor may delete their handheld.
     *
     * @param id
     * @param UserSecurity
     * @throws RequestedEntityNotFoundException
     * @throws PermissionDeniedException
     */
    @Transactional
    fun deleteHandheld(id: UUID, vendorDetails: UserSecurity) {
        val handheld = getHandheldById(id,vendorDetails)

        handheldRepository.delete(handheld)
        logger.info("Handheld '{}' deleted by vendor '{}'", id, vendorDetails.id)
    }

    /**
     * Toggles the availability of a [Handheld].
     * Only the owning vendor may toggle availability.
     *
     * @param id
     * @param vendorDetails
     * @return [Handheld]
     * @throws RequestedEntityNotFoundException
     * @throws PermissionDeniedException
     */
    @Transactional
    fun toggleAvailability(handheldAvailabilityDto: HandheldAvailabilityDto, id: UUID, vendorDetails: UserSecurity): Handheld {
        val handheld = getHandheldById(id, vendorDetails)

        if (handheld.availability != Availability.fromString(handheldAvailabilityDto.availability)) {
            handheld.availability = Availability.fromString(handheldAvailabilityDto.availability!!)
        }

        toggleHandheldSizes(handheldAvailabilityDto, handheld)

        val updatedHandheld = handheldRepository.saveAndFlush(handheld)
        logger.info("Handheld '{}' availability toggled to '{}' by vendor '{}'", id, updatedHandheld.availability, vendorDetails.id)
        return updatedHandheld
    }

    /**
     * Adds a new handheld or updates an existing one based on the presence of an ID in the [HandheldDto].
     * Validates input data, checks for duplicates, and ensures that only the owning vendor can update their handheld.
     *
     * @param handheldDto The DTO containing the handheld data to be added or updated.
     * @param vendorDetails The security details of the vendor performing the operation.
     * @return [Handheld] - The added or updated Handheld entity.
     * @throws UserNotFoundException if the vendor is not found in the database.
     * @throws DuplicateFoundException if a handheld with the same name already exists for the vendor when adding a new handheld or changing the name of an existing one.
     * @throws IllegalArgumentException if any required fields are missing or invalid in the DTO.
     */
    @Transactional
    fun addOrUpdateHandheld(handheldDto: HandheldDto, vendorDetails: UserSecurity): Handheld {
        val vendor = vendorRepository.findById(vendorDetails.id)
            .orElseThrow { UserNotFoundException("Vendor not found.") }

        // Determine whether to create or update handheld
        val newHandheld: Handheld = if (handheldDto.id != null) {
            getHandheldById(handheldDto.id!!, vendorDetails)
        } else {
            Handheld().apply { this.vendor = vendor }
        }

        // Input validation
        val handheldName = requireNotNull(handheldDto.handheldName) { "Please enter a handheld name." }
        val handheldType = HandHeldType.fromString(handheldDto.handheldType)
        val description = handheldDto.description
        val availability = Availability.fromString(handheldDto.availability)
        val deliverable = requireNotNull(handheldDto.deliverable) { "Please select deliverability." }
        val readyAsFrom = handheldDto.readyAsFrom ?: vendor.openingTime

        // null -> almost unlimited (too many to count)
        val dailyDeliveryQuantity = handheldDto.dailyDeliveryQuantity

        val preparationTime = handheldDto.preparationTime ?: 0
        val quickDelivery = handheldDto.quickDelivery
        val deliveryFee = handheldDto.deliveryFee

        if (handheldDto.id == null) {
            // Check if the vendor has already added a handheld with the same name
            if (duplicateUtility.isDuplicateHandheldFound(handheldName, vendorDetails.id )) {
                throw DuplicateFoundException("A $handheldType with the name: $handheldName already exist. Cannot add duplicate.")
            }

            newHandheld.name = handheldName
            val lastHandheld = handheldRepository.findTopOrderByFoodNumberDescWithLock().firstOrNull()
            val nextNumber = lastHandheld
                ?.handheldNumber
                ?.takeLast(5)
                ?.toInt()
                ?.plus(1)
                ?: 1

            newHandheld.handheldNumber = "HD-%05d".format(nextNumber)
        } else {

            // if the vendor changed the name of an existing food
            if (newHandheld.name != handheldName) {
                if (duplicateUtility.isDuplicateHandheldFound(handheldName, vendorDetails.id )) {
                    throw DuplicateFoundException("A $handheldType with the name: $handheldName already exist. Cannot add duplicate.")
                }
            }
        }

        // Basic info
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

        // Persist handheld with basic info to make it managed by JPA
        val managedHandheld = saveHandheld(newHandheld)

        prepareHandheldFillings(handheldDto, managedHandheld, vendor)
        managedHandheld.availableDays = prepareHandheldAvailableDays(handheldDto, managedHandheld)
        handheldDto.discounts?.let { prepareHandheldDiscounts(handheldDto, managedHandheld, vendor) } ?: managedHandheld.handheldDiscounts.clear()
        managedHandheld.handheldImages = prepareHandheldImages(handheldDto, managedHandheld)
        managedHandheld.orderTypes = prepareHandheldOrderTypes(handheldDto, managedHandheld)
        managedHandheld.handheldSizes = prepareHandheldSizes(handheldDto, managedHandheld)

        // Adding handheld item to the menu
        val menu = prepareMenu(handheldDto, managedHandheld)
        menuRepository.saveAndFlush(menu)

        return saveHandheld(managedHandheld)
    }

    /**
     * Prepares the [Menu] entity for a handheld by checking if the handheld already has an associated menu item.
     * If it does, it retrieves and updates that menu item; if not, it creates a new menu item linked to the handheld.
     * It also sets the order types for the menu based on the handheld's order types.
     *
     * @param handheldDto The DTO containing the incoming handheld data.
     * @param handheld The managed Handheld entity for which the menu is being prepared.
     * @return The prepared Menu entity ready for persistence.
     * @throws RequestedEntityNotFoundException if an existing menu item is expected but not found.
     * @throws IllegalArgumentException if an unknown order type is encountered in the handheld's order types.
     */
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

                /**
                 * false -> option not offered
                 * true + deliveryItemsLeft = null -> unlimited stock
                 * true + deliveryItemsLeft > 0 -> available
                 *  true + deliveryItemsLeft = 0 -> sold out
                 */

                OrderType.DELIVERY -> menu.delivery = true
                OrderType.DINE_IN -> menu.dineIn = true    // null -> sold out, false -> option not offered, true -> option available
                OrderType.PICKUP -> menu.pickup = true     // null -> sold out, false -> option not offered, true -> option available
                else -> {
                    throw IllegalArgumentException("Unknown order type selected.")
                }
            }
        }
        return menu
    }

    /**
     * Prepares the handheld fillings for persistence by comparing incoming filling DTOs with existing handheld fillings.
     * Validates that all required fields are present and updates the handheld's filling set accordingly.
     *
     * @param handheldDto The DTO containing the incoming filling data.
     * @param handheld The managed Handheld entity to which the fillings belong.
     * @param vendor The Vendor entity to which the fillings belong.
     * @throws IllegalArgumentException if any required filling fields are missing in the DTO.
     */
    private fun prepareHandheldFillings(handheldDto: HandheldDto, handheld: Handheld, vendor: Vendor) {
        val existingHandheldFillings = handheld.handheldFillings.associateBy { it.filling.id }
        val incomingFillings = handheldDto.fillings

        val updatedHandheldFillings = incomingFillings.map { dto ->
            var filling = Filling().apply {
                dto.id?.let { this.id = it }
                this.vendor = vendor
                this.name = requireNotNull(dto.name)
                this.description = dto.description
            }

            filling = fillingRepository.saveAndFlush(filling)

            val handheldFilling = dto.id?.let { existingHandheldFillings[it] } ?: HandheldFilling().apply { this.handheld = handheld }
            handheldFilling.filling = filling
            handheldFilling
        }

        handheld.handheldFillings.removeIf { existingFilling ->
            updatedHandheldFillings.none { it.filling.id == existingFilling.filling.id }
        }

        updatedHandheldFillings.forEach { updatedHandheldFilling ->
            if (handheld.handheldFillings.none { it.filling.id == updatedHandheldFilling.filling.id}) {
                handheld.handheldFillings.add(updatedHandheldFilling)
            }
        }
    }

    /**
     * Prepares the handheld discounts for persistence by comparing incoming discount DTOs with existing handheld discounts.
     * Validates that all required fields are present and updates the handheld's discount set accordingly.
     *
     * @param handheldDto The DTO containing the incoming discount data.
     * @param handheld The managed Handheld entity to which the discounts belong.
     * @param vendor The Vendor entity to which the discounts belong.
     * @throws IllegalArgumentException if any required discount fields are missing in the DTO.
     */
    private fun prepareHandheldDiscounts(handheldDto: HandheldDto, handheld: Handheld, vendor: Vendor) {
        val existingHandheldDiscounts = handheld.handheldDiscounts.associateBy { it.discount.id }
        val incomingDiscounts = handheldDto.discounts!!

        val updatedHandheldDiscounts = incomingDiscounts.map { dto ->

            var discount = dto.id?.let { id ->
                // Reuse the existing managed discount if it exists
                discountRepository.findById(id)
                    .orElseThrow { RequestedEntityNotFoundException("Discount with id $id not found") }
            } ?: Discount().apply {
                this.vendor = vendor
            }

            discount.apply {
                discountName = requireNotNull(dto.discountName)
                percentage = requireNotNull(dto.percentage)
                startDate = requireNotNull(dto.startDate)
                endDate = requireNotNull(dto.endDate)
            }

            discount = discountRepository.save(discount)

            val handheldDiscounts = dto.id?.let { existingHandheldDiscounts[it] } ?: HandheldDiscount().apply { this.handheld = handheld }
            handheldDiscounts.discount = discount
            handheldDiscounts
        }

        handheld.handheldDiscounts.removeIf { existingHandheldDiscount ->
            updatedHandheldDiscounts.none { it.discount.id == existingHandheldDiscount.id }
        }

        updatedHandheldDiscounts.forEach { updatedHandheldDiscount ->
            if (handheld.handheldDiscounts.none { it.discount.id == updatedHandheldDiscount.discount.id }) {
                handheld.handheldDiscounts.add(updatedHandheldDiscount)
            }
        }
    }

    /**
     * Prepares the handheld sizes for persistence by comparing incoming size DTOs with existing handheld sizes.
     * Validates that at least one size is selected and updates the handheld's size set accordingly.
     *
     * @param handheldDto The DTO containing the incoming handheld size data.
     * @param handheld The managed Handheld entity to which the sizes belong.
     * @return The updated set of HandheldSize entities associated with the handheld.
     * @throws IllegalArgumentException if no sizes are selected in the DTO.
     */
    private fun prepareHandheldSizes(handheldDto: HandheldDto, handheld: Handheld): MutableSet<HandheldSize> {
        val existingHandheldSizes = handheld.handheldSizes.associateBy { it.id }
        val incomingHandheldSizes = handheldDto.handheldSizes

        if (incomingHandheldSizes.isEmpty()) {
            throw IllegalArgumentException("Please select at least one food size.")
        }

        val updatedHandheldSizes = incomingHandheldSizes.map { dto ->
            val handheldSize = existingHandheldSizes[dto.id] ?: HandheldSize().apply { this.handheld = handheld }

            handheldSize.apply {
                this.size = Size.fromString(requireNotNull(dto.size))
                name = (dto.sizeName) ?: this.size!!.name
                price = dto.price
                availability = requireNotNull(Availability.fromString(dto.availability!!))
            }

        }

        handheld.handheldSizes.removeIf { existingSize ->
            updatedHandheldSizes.none { it.id == existingSize.id }
        }

        updatedHandheldSizes.forEach { updatedSize ->
            if (handheld.handheldSizes.none { it.id == updatedSize.id }) {
                handheld.handheldSizes.add(updatedSize)
            }
        }

        return handheld.handheldSizes
    }

    /**
     * Prepares the handheld's available days for persistence by comparing incoming day strings with existing available days.
     * Validates that at least one day is selected and updates the handheld's available day set accordingly.
     *
     * @param handheldDto The DTO containing the incoming available day data.
     * @param handheld The managed Handheld entity to which the available days belong.
     * @return The updated set of Day enums associated with the handheld.
     * @throws IllegalArgumentException if no days are selected in the DTO.
     */
    private fun prepareHandheldAvailableDays(handheldDto: HandheldDto, handheld: Handheld): MutableSet<Day> {
        val existingAvailableDays = handheld.availableDays
        val incomingAvailableDays = if (handheldDto.availableDays.isEmpty()) {
            throw IllegalArgumentException("At least one day must be selected.")

        } else {
            handheldDto.availableDays.map { Day.fromString(it) }.toSet()
        }

        existingAvailableDays.removeIf { it !in incomingAvailableDays }

        incomingAvailableDays.forEach { availableDay ->
            if (availableDay !in existingAvailableDays) {
                existingAvailableDays.add(availableDay)
            }
        }

        return existingAvailableDays
    }

    /**
     * Prepares the handheld's order types for persistence by comparing incoming order type strings with existing order types.
     * Validates that at least one order type is selected and updates the handheld's order type set accordingly.
     *
     * @param handheldDto The DTO containing the incoming order type data.
     * @param handheld The managed Handheld entity to which the order types belong.
     * @return The updated set of OrderType enums associated with the handheld.
     * @throws IllegalArgumentException if no order types are selected in the DTO.
     */
    private fun prepareHandheldOrderTypes(handheldDto: HandheldDto, handheld: Handheld): MutableSet<OrderType> {

        val existingOrderTypes = handheld.orderTypes
        val incomingOrderTypes = if (handheldDto.orderTypes.isEmpty()) {
            throw IllegalArgumentException("At least one order type must be selected.")
        } else {
            handheldDto.orderTypes.map { OrderType.fromString(it) }.toSet()
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
     * Prepares the handheld images for persistence by comparing incoming image DTOs with existing images.
     * Validates that at least 2 images are provided and updates the handheld's image set accordingly.
     *
     * @param handheldDto The DTO containing the incoming image data.
     * @param handheld The managed Handheld entity to which the images belong.
     * @return The updated set of HandheldImage entities associated with the handheld.
     * @throws IllegalArgumentException if fewer than 2 images are provided in the DTO.
     */
    private fun prepareHandheldImages(handheldDto: HandheldDto, handheld: Handheld): MutableSet<HandheldImage> {
        val existingHandheldImages = handheld.handheldImages.associateBy { it.id }
        val incomingImages = handheldDto.handheldImages

        if (incomingImages.isEmpty() || incomingImages.size < 2) {
            throw IllegalArgumentException("Please add at least 2 images of the food")
        }

        val updatedHandheldImages = incomingImages.map { dto ->
            val image = existingHandheldImages[dto.id] ?: HandheldImage().apply { this.handheld = handheld }
            image.imageUrl = dto.imageUrl
            image
        }

        handheld.handheldImages.removeIf { existingImage ->
            updatedHandheldImages.none { it.id == existingImage.id }
        }

        updatedHandheldImages.forEach { updatedImage ->
            if (handheld.handheldImages.none { it.id == updatedImage.id }) {
                handheld.handheldImages.add(updatedImage)
            }
        }

        return handheld.handheldImages
    }

    /**
     * Toggles the availability of handheld sizes based on the provided [HandheldAvailabilityDto].
     * Validates that at least one size is selected and that all size IDs are valid.
     * @param handheldAvailabilityDto
     * @param handheld
     * @throws IllegalArgumentException if no sizes are selected or if any size ID is invalid.
     */
    private fun toggleHandheldSizes(handheldAvailabilityDto: HandheldAvailabilityDto, handheld: Handheld) {
        val handheldSizes = handheld.handheldSizes.associateBy { it.id }
        val handheldSizesToToggle = handheldAvailabilityDto.handheldSizes

        if (handheldSizesToToggle.isNullOrEmpty()) {
            throw IllegalArgumentException("Please select at least one handheld size.")
        }

        handheldSizesToToggle.forEach { dto ->
            val sizeId = dto.sizeId ?: throw IllegalArgumentException("Size ID cannot be null.")
            val handheldSize = handheldSizes[dto.sizeId] ?: throw IllegalArgumentException("Size with $sizeId cannot be found.")

            if (handheldSize.availability != Availability.fromString(dto.availability)) {
                handheldSize.availability = Availability.fromString(dto.availability)
            }
        }
    }
}