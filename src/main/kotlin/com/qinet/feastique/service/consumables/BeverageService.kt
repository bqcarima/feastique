package com.qinet.feastique.service.consumables

import com.qinet.feastique.common.mapper.toResponse
import com.qinet.feastique.exception.DuplicateFoundException
import com.qinet.feastique.exception.PermissionDeniedException
import com.qinet.feastique.exception.RequestedEntityNotFoundException
import com.qinet.feastique.exception.UserNotFoundException
import com.qinet.feastique.model.dto.BeverageAvailabilityDto
import com.qinet.feastique.model.dto.FlavourAvailabilityDto
import com.qinet.feastique.model.dto.consumables.BeverageDto
import com.qinet.feastique.model.dto.consumables.BeverageFlavourSizeDto
import com.qinet.feastique.model.entity.Menu
import com.qinet.feastique.model.entity.consumables.beverage.Beverage
import com.qinet.feastique.model.entity.consumables.flavour.BeverageFlavour
import com.qinet.feastique.model.entity.discount.BeverageDiscount
import com.qinet.feastique.model.entity.discount.Discount
import com.qinet.feastique.model.entity.image.BeverageImage
import com.qinet.feastique.model.entity.size.BeverageFlavourSize
import com.qinet.feastique.model.entity.user.Vendor
import com.qinet.feastique.model.enums.*
import com.qinet.feastique.repository.menu.MenuRepository
import com.qinet.feastique.repository.consumables.beverage.BeverageRepository
import com.qinet.feastique.repository.discount.DiscountRepository
import com.qinet.feastique.repository.user.VendorRepository
import com.qinet.feastique.response.consumables.beverage.BeverageResponse
import com.qinet.feastique.response.pagination.WindowResponse
import com.qinet.feastique.security.UserSecurity
import com.qinet.feastique.utility.CursorEncoder
import com.qinet.feastique.utility.DuplicateUtility
import com.qinet.feastique.utility.SecurityUtility
import org.springframework.data.domain.*
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*
import kotlin.jvm.optionals.getOrElse

@Service
class BeverageService(
    private val beverageRepository: BeverageRepository,
    private val vendorRepository: VendorRepository,
    private val duplicateUtility: DuplicateUtility,
    private val menuRepository: MenuRepository,
    private val discountRepository: DiscountRepository,
    private val securityUtility: SecurityUtility,
    private val cursorEncoder: CursorEncoder
) {

    @Transactional(readOnly = true)
    fun getBeverage(id: UUID, vendorDetails: UserSecurity): Beverage {
        return beverageRepository.findById(id)
            .orElseThrow { RequestedEntityNotFoundException("No beverage found for id: $id") }
            .also {
                if (it.vendor.id != vendorDetails.id) {
                    throw PermissionDeniedException("You do not have the permission to access beverage.")
                }
            }
    }

    @Transactional(readOnly = true)
    fun getAllBeverages(vendorDetails: UserSecurity, page: Int, size: Int): Page<BeverageResponse> {
        val pageable = PageRequest.of(page, size, Sort.by("name").ascending())
        return beverageRepository.findAllOrdered(vendorDetails.id, pageable).map { it.toResponse() }
    }

    @Transactional(readOnly = true)
    fun scrollBeverages(
        userDetails: UserSecurity,
        cursor: String?,
        size: Int = Constants.DEFAULT_PAGE_SIZE.type
    ): WindowResponse<BeverageResponse> {
        val role = securityUtility.getSingleRole(userDetails)
        val currentOffset: Long = cursor?.toLongOrNull() ?: 0L
        val scrollPosition = if (currentOffset == 0L) ScrollPosition.offset() else ScrollPosition.offset(currentOffset)
        val sort = Sort.by("name").ascending()

        val window = when (role) {
            "CUSTOMER" -> beverageRepository.findAllBy(scrollPosition, sort, Limit.of(size))
            "VENDOR" -> beverageRepository.findAllByVendorId(userDetails.id, scrollPosition, sort, Limit.of(size))
            else -> throw PermissionDeniedException("Unrecognized role: $role")
        }.map { it.toResponse() }

        return window.toResponse(currentOffset) { cursorEncoder.encodeOffset(it) }
    }

    @Transactional
    fun deleteBeverage(id: UUID, vendorDetails: UserSecurity) {
        beverageRepository.delete(getBeverage(id, vendorDetails))
    }

    @Transactional
    fun saveBeverage(beverage: Beverage): Beverage {
        return beverageRepository.saveAndFlush(beverage)
    }

    @Transactional
    fun changeBeverageAvailability(
        beverageAvailabilityDto: BeverageAvailabilityDto,
        id: UUID,
        vendorDetails: UserSecurity
    ): Beverage {
        val beverage = getBeverage(id, vendorDetails)

        if (beverage.availability != Availability.fromString(beverageAvailabilityDto.availability)) {
            beverage.availability = Availability.fromString(beverageAvailabilityDto.availability)
        }

        beverageAvailabilityDto.beverageFlavours?.let { changeBeverageFlavourAvailability(beverageAvailabilityDto, beverage) }
        return saveBeverage(beverage)
    }

    /**
     * Creates or updates a beverage for the authenticated vendor.
     *
     * - Create: when [beverageDto].id is null — checks for duplicate names.
     * - Update: when [beverageDto].id is provided — ownership is verified; duplicate check runs only if the name changed.
     *
     * Nested collections (images, orderTypes, flavours, discounts) are reconciled:
     * existing children are reused or updated, and children absent from the DTO are removed.
     *
     * @throws UserNotFoundException when the vendor cannot be found
     * @throws RequestedEntityNotFoundException when updating a non-existent beverage id
     * @throws PermissionDeniedException when the beverage belongs to another vendor
     * @throws DuplicateFoundException when the name already exists for this vendor
     * @throws IllegalArgumentException when required fields are missing or invalid
     */
    @Transactional
    fun addOrUpdateBeverage(beverageDto: BeverageDto, vendorDetails: UserSecurity): Beverage {
        val vendor = vendorRepository.findById(vendorDetails.id)
            .orElseThrow { UserNotFoundException("Vendor not found.") }

        val beverage: Beverage = if (beverageDto.id != null) {
            getBeverage(beverageDto.id!!, vendorDetails)
        } else {
            Beverage().apply { this.vendor = vendor }
        }

        val beverageName          = requireNotNull(beverageDto.beverageName) { "Please enter a name." }
        val alcoholicStatus       = requireNotNull(beverageDto.alcoholic)
        val alcoholPercentage     = requireNotNull(beverageDto.percentage) { "Please enter a percentage value." }
        val beverageGroup         = BeverageGroup.fromString(beverageDto.beverageGroup)
        val deliverable           = requireNotNull(beverageDto.deliverable) { "Please specify if beverage is deliverable." }
        val deliveryFee           = beverageDto.deliveryFee
        val dailyDeliveryQuantity = beverageDto.dailyDeliveryQuantity
        val availability          = Availability.fromString(beverageDto.availability)
        val readyAsFrom           = beverageDto.readyAsFrom ?: vendor.openingTime
        val preparationTime       = beverageDto.preparationTime ?: 0
        val quickDelivery         = beverageDto.quickDelivery

        // Check for duplicate name only on create, or if the name has changed
        if (beverageDto.id == null || beverage.name != beverageName) {
            if (duplicateUtility.isDuplicateBeverageFound(beverageName, vendorDetails.id)) {
                throw DuplicateFoundException("A beverage with the name $beverageName already exist. Cannot add duplicate.")
            }
            beverage.name = beverageName
        }

        beverage.apply {
            name                      = beverageName
            alcoholic                 = alcoholicStatus
            percentage                = alcoholPercentage
            this.beverageGroup        = beverageGroup
            this.deliverable          = deliverable
            this.deliveryFee          = deliveryFee
            this.quickDelivery        = quickDelivery
            this.dailyDeliveryQuantity = dailyDeliveryQuantity
            this.availability         = availability
            this.readyAsFrom          = readyAsFrom
            this.preparationTime      = preparationTime
        }

        val managedBeverage = saveBeverage(beverage)

        managedBeverage.beverageImages   = prepareBeverageImages(beverageDto, managedBeverage)
        managedBeverage.orderTypes       = prepareBeverageOrderTypes(beverageDto, managedBeverage)
        managedBeverage.beverageFlavours = prepareBeverageFlavours(beverageDto, managedBeverage)
        beverageDto.discounts?.let { prepareBeverageDiscounts(beverageDto, managedBeverage, vendor) }
            ?: managedBeverage.beverageDiscounts.clear()

        menuRepository.save(prepareMenu(beverageDto, managedBeverage))

        return saveBeverage(managedBeverage)
    }

    private fun prepareMenu(beverageDto: BeverageDto, beverage: Beverage): Menu {
        val menu = if (beverageDto.id != null) {
            menuRepository.findById(beverage.menu!!.id)
                .getOrElse { throw RequestedEntityNotFoundException("Menu item not found.") }
        } else {
            Menu().apply { this.beverage = beverage }
        }

        beverage.orderTypes.forEach { orderType ->
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

    private fun prepareBeverageFlavours(beverageDto: BeverageDto, beverage: Beverage): MutableSet<BeverageFlavour> {
        val existingFlavours = beverage.beverageFlavours.associateBy { it.id }

        val updatedFlavours = beverageDto.beverageFlavours.map { flavourDto ->
            val flavour = existingFlavours[flavourDto.id] ?: BeverageFlavour().apply { this.beverage = beverage }
            flavour.apply {
                name                 = requireNotNull(flavourDto.flavourName) { "Please enter a flavour name." }
                description          = flavourDto.description
                beverageFlavourSizes = prepareBeverageFlavourSizes(flavourDto.flavourSizes, flavour)
                availability         = requireNotNull(Availability.fromString(flavourDto.availability)) { "Please enter an availability status." }
            }
        }

        beverage.beverageFlavours.removeIf { existing -> updatedFlavours.none { it.id == existing.id } }
        updatedFlavours.forEach { updated ->
            if (beverage.beverageFlavours.none { it.id == updated.id }) beverage.beverageFlavours.add(updated)
        }

        return beverage.beverageFlavours
    }

    private fun prepareBeverageDiscounts(beverageDto: BeverageDto, beverage: Beverage, vendor: Vendor) {
        val existingDiscounts = beverage.beverageDiscounts.associateBy { it.discount.id }

        val updatedDiscounts = beverageDto.discounts!!.map { dto ->
            var discount = dto.id?.let { id ->
                discountRepository.findById(id)
                    .orElseThrow { RequestedEntityNotFoundException("Discount with id $id not found") }
            } ?: Discount().apply { this.vendor = vendor }

            discount.apply {
                discountName = requireNotNull(dto.discountName)
                percentage   = requireNotNull(dto.percentage)
                startDate    = requireNotNull(dto.startDate)
                endDate      = requireNotNull(dto.endDate)
            }
            discount = discountRepository.save(discount)

            val beverageDiscount = dto.id?.let { existingDiscounts[it] } ?: BeverageDiscount().apply { this.beverage = beverage }
            beverageDiscount.discount = discount
            beverageDiscount
        }

        beverage.beverageDiscounts.removeIf { existing -> updatedDiscounts.none { it.discount.id == existing.id } }
        updatedDiscounts.forEach { updated ->
            if (beverage.beverageDiscounts.none { it.discount.id == updated.discount.id }) beverage.beverageDiscounts.add(updated)
        }
    }

    private fun prepareBeverageFlavourSizes(
        flavourSizeDtos: Set<BeverageFlavourSizeDto>,
        beverageFlavour: BeverageFlavour
    ): MutableSet<BeverageFlavourSize> {
        if (flavourSizeDtos.isEmpty()) throw IllegalArgumentException("Please select at least one beverage flavour size.")

        val existingFlavourSizes = beverageFlavour.beverageFlavourSizes.associateBy { it.id }

        val updatedFlavourSizes = flavourSizeDtos.map { dto ->
            val flavourSize = existingFlavourSizes[dto.id] ?: BeverageFlavourSize().apply { this.beverageFlavour = beverageFlavour }
            flavourSize.apply {
                this.size    = Size.fromString(dto.size)
                name         = dto.sizeName ?: this.size!!.type
                price        = requireNotNull(dto.price) { "Please provide a price." }
                availability = requireNotNull(Availability.fromString(dto.availability)) { "Please enter an availability status." }
            }
        }

        beverageFlavour.beverageFlavourSizes.removeIf { existing -> updatedFlavourSizes.none { it.id == existing.id } }
        updatedFlavourSizes.forEach { updated ->
            if (beverageFlavour.beverageFlavourSizes.none { it.id == updated.id }) beverageFlavour.beverageFlavourSizes.add(updated)
        }

        return beverageFlavour.beverageFlavourSizes
    }

    private fun prepareBeverageImages(beverageDto: BeverageDto, beverage: Beverage): MutableSet<BeverageImage> {
        if (beverageDto.beverageImages.isEmpty() || beverageDto.beverageImages.size < 2) {
            throw IllegalArgumentException("Please add at least 2 images of the food")
        }

        val existingImages = beverage.beverageImages.associateBy { it.id }

        val updatedImages = beverageDto.beverageImages.map { dto ->
            val image = existingImages[dto.id] ?: BeverageImage().apply { this.beverage = beverage }
            image.imageUrl = dto.imageUrl
            image
        }

        beverage.beverageImages.removeIf { existing -> updatedImages.none { it.id == existing.id } }
        updatedImages.forEach { updated ->
            if (beverage.beverageImages.none { it.id == updated.id }) beverage.beverageImages.add(updated)
        }

        return beverage.beverageImages
    }

    private fun prepareBeverageOrderTypes(beverageDto: BeverageDto, beverage: Beverage): MutableSet<OrderType> {
        val existingOrderTypes = beverage.orderTypes
        val incomingOrderTypes = if (beverageDto.orderTypes.isEmpty()) {
            throw IllegalArgumentException("At least one order type must be selected.")
        } else {
            beverageDto.orderTypes.map { OrderType.fromString(it) }.toSet()
        }

        existingOrderTypes.removeIf { it !in incomingOrderTypes }
        incomingOrderTypes.forEach { if (it !in existingOrderTypes) existingOrderTypes.add(it) }

        return existingOrderTypes
    }

    private fun changeBeverageFlavourAvailability(beverageAvailabilityDto: BeverageAvailabilityDto, beverage: Beverage) {
        val incomingFlavours = beverageAvailabilityDto.beverageFlavours
        if (incomingFlavours.isNullOrEmpty()) throw IllegalArgumentException("Please provide at least one beverage flavour to toggle availability.")

        val flavours = beverage.beverageFlavours.associateBy { it.id }
        incomingFlavours.forEach { dto ->
            val flavourId = dto.flavourId ?: throw IllegalArgumentException("Beverage flavour ID cannot be null.")
            val flavour = flavours[dto.flavourId]
                ?: throw RequestedEntityNotFoundException("Beverage flavour with id $flavourId not found.")

            if (flavour.availability != Availability.fromString(dto.availability)) {
                flavour.availability = Availability.fromString(dto.availability)
            }
            dto.flavourSizes?.let { changeBeverageFlavourSizeAvailability(dto, flavour) }
        }
    }

    private fun changeBeverageFlavourSizeAvailability(flavourAvailabilityDto: FlavourAvailabilityDto, beverageFlavour: BeverageFlavour) {
        val incomingSizes = flavourAvailabilityDto.flavourSizes
        if (incomingSizes.isNullOrEmpty()) throw IllegalArgumentException("Please provide at least one beverage flavour size to toggle availability.")

        val flavourSizes = beverageFlavour.beverageFlavourSizes.associateBy { it.id }
        incomingSizes.forEach { dto ->
            val sizeId = dto.sizeId ?: throw IllegalArgumentException("Beverage flavour size ID cannot be null.")
            val flavourSize = flavourSizes[sizeId]
                ?: throw RequestedEntityNotFoundException("Beverage flavour size with id $sizeId not found.")

            if (flavourSize.availability != Availability.fromString(dto.availability)) {
                flavourSize.availability = Availability.fromString(dto.availability)
            }
        }
    }
}

