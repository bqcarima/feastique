package com.qinet.feastique.service.consumables

import com.qinet.feastique.common.mapper.toResponse
import com.qinet.feastique.exception.DuplicateFoundException
import com.qinet.feastique.exception.PermissionDeniedException
import com.qinet.feastique.exception.RequestedEntityNotFoundException
import com.qinet.feastique.exception.UserNotFoundException
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
import com.qinet.feastique.model.enums.Availability
import com.qinet.feastique.model.enums.BeverageGroup
import com.qinet.feastique.model.enums.OrderType
import com.qinet.feastique.model.enums.Size
import com.qinet.feastique.repository.MenuRepository
import com.qinet.feastique.repository.consumables.beverage.BeverageRepository
import com.qinet.feastique.repository.discount.DiscountRepository
import com.qinet.feastique.repository.user.VendorRepository
import com.qinet.feastique.response.consumables.beverage.BeverageResponse
import com.qinet.feastique.security.UserSecurity
import com.qinet.feastique.utility.DuplicateUtility
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
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
    private val discountRepository: DiscountRepository
) {

    @Transactional(readOnly = true)
    fun getBeverage(id: UUID, vendorDetails: UserSecurity): Beverage {
        val beverage = beverageRepository.findById(id)
            .orElseThrow { RequestedEntityNotFoundException("No beverage found for id: $id") }
            .also {
                if (it.vendor.id != vendorDetails.id) {
                    throw PermissionDeniedException("You do not have the permission to access beverage.")
                }
            }
        return beverage
    }

    @Transactional(readOnly = true)
    fun getAllBeverages(vendorDetails: UserSecurity, page: Int, size: Int): Page<BeverageResponse> {
        val pageable = PageRequest.of(page, size, Sort.by("name").ascending())
        val page = beverageRepository.findAllOrdered(vendorDetails.id, pageable).map { it.toResponse() }
        return page
    }

    @Transactional
    fun deleteBeverage(id: UUID, vendorDetails: UserSecurity) {
        val beverage = getBeverage(id, vendorDetails)
        beverageRepository.delete(beverage)
    }

    /**
     * Saves the beverage entity to the database and flushes the changes immediately.
     * This ensures that the entity is persisted and any generated values (like IDs)
     * are available for further processing.
     */
    @Transactional
    fun saveBeverage(beverage: Beverage): Beverage {
        return beverageRepository.saveAndFlush(beverage)
    }

    /**
     * Adds a new beverage or updates an existing one based on the provided BeverageDto.
     * It performs input validation, checks for duplicate beverage names, and manages
     * related entities such as images, order types, flavours, and discounts.
     * @param beverageDto The data transfer object containing beverage details.
     * @param vendorDetails The security details of the vendor performing the operation.
     * @return [Beverage] The saved or updated Beverage entity.
     * @throws UserNotFoundException If the vendor is not found.
     * @throws DuplicateFoundException If a duplicate beverage name is found for the vendor.
     */
    @Transactional
    fun addOrUpdateBeverage(beverageDto: BeverageDto, vendorDetails: UserSecurity): Beverage {
        val vendor = vendorRepository.findById(vendorDetails.id)
            .orElseThrow { UserNotFoundException("Vendor not found.") }

        val beverage: Beverage = if (beverageDto.id != null) {
            getBeverage(beverageDto.id!!, vendorDetails)

        } else {
            Beverage().apply {
                this.vendor = vendor
            }
        }

        // Input validation
        val beverageName = requireNotNull(beverageDto.beverageName) { "Please enter a name." }
        val alcoholicStatus = requireNotNull(beverageDto.alcoholic)
        val alcoholPercentage = requireNotNull(beverageDto.percentage) { "Please enter a percentage value." }
        val beverageGroup = BeverageGroup.fromString(beverageDto.beverageGroup)
        val deliverable = requireNotNull(beverageDto.deliverable) { "Please specify if beverage is deliverable."}
        val deliveryFee = beverageDto.deliveryFee
        val dailyDeliveryQuantity = beverageDto.dailyDeliveryQuantity
        val availability = Availability.fromString(beverageDto.availability)
        val readyAsFrom = requireNotNull(beverageDto.readyAsFrom) { "Please specify the time a beverage becomes available during the day."}
        val preparationTime = beverageDto.preparationTime ?: 0
        val quickDelivery = beverageDto.quickDelivery

        // Prevents duplicate beverage names for the same vendor
        // beverageDto.id == null = true  -> a new beverage is being created.
        // beverage.name != beverageName = false -> the name of an existing vendor has been changed.
        if (beverageDto.id == null || beverage.name != beverageName) {
            if (!duplicateUtility.isDuplicateBeverageFound(beverageName, vendorDetails.id)) {
                beverage.name = beverageName
            } else {
                throw DuplicateFoundException("A beverage with the name $beverageName already exist. Cannot add duplicate.")
            }
        }

        beverage.apply {
            name = beverageName
            alcoholic = alcoholicStatus
            percentage = alcoholPercentage
            this.beverageGroup = beverageGroup
            this.deliverable = deliverable
            this.deliveryFee = deliveryFee
            this.quickDelivery = quickDelivery
            this.dailyDeliveryQuantity = dailyDeliveryQuantity
            this.availability = availability
            this.readyAsFrom = readyAsFrom
            this.preparationTime = preparationTime
        }

        // Persist beverage with basic info to make it managed by JPA
        val managedBeverage = saveBeverage(beverage)

        managedBeverage.beverageImages = prepareBeverageImages(beverageDto, managedBeverage)
        managedBeverage.orderTypes = prepareBeverageOrderTypes(beverageDto, managedBeverage)
        managedBeverage.beverageFlavours = prepareBeverageFlavours(beverageDto, managedBeverage)

        beverageDto.discounts?.let { prepareBeverageDiscounts(beverageDto, managedBeverage, vendor) } ?: managedBeverage.beverageDiscounts.clear()

        val menu = prepareMenu(beverageDto, managedBeverage)
        menuRepository.save(menu)

        return saveBeverage(managedBeverage)
    }

    private fun prepareMenu(beverageDto: BeverageDto, beverage: Beverage): Menu {
        val menu = if (beverageDto.id != null) {
            menuRepository.findById(beverage.menu!!.id)
                .getOrElse { throw RequestedEntityNotFoundException("Menu item not found.") }

        } else {
            Menu().apply {
                this.beverage = beverage
            }
        }

        beverage.orderTypes.forEach { orderType ->
            when (orderType) {
                OrderType.DELIVERY -> menu.delivery =
                    true // null -> sold out, false -> option not offered, true -> option available
                OrderType.DINE_IN -> menu.dineIn =
                    true   // null -> sold out, false -> option not offered, true -> option available
                OrderType.PICKUP -> menu.pickup =
                    true    // null -> sold out, false -> option not offered, true -> option available

                else -> {
                    throw IllegalArgumentException("Unknown order type selected.")
                }
            }
        }
        return menu
    }

    private fun prepareBeverageFlavours(beverageDto: BeverageDto, beverage: Beverage): MutableSet<BeverageFlavour> {
        val existingFlavours = beverage.beverageFlavours.associateBy { it.id }

        val updatedFlavours = beverageDto.beverageFlavours.map { flavourDto ->
            val flavour = existingFlavours[flavourDto.id] ?: BeverageFlavour().apply {
                this.beverage = beverage
            }

            flavour.apply {
                name = requireNotNull(flavourDto.flavourName) { "Please enter a flavour name." }
                description = flavourDto.description
                beverageFlavourSizes = prepareBeverageFlavourSizes(flavourDto.flavourSizes, flavour)
                availability = requireNotNull(Availability.fromString(flavourDto.availability)) { "Please enter an availability status." }
            }
        }

        beverage.beverageFlavours.removeIf { existingFlavour ->
            updatedFlavours.none { it.id == existingFlavour.id }
        }

        updatedFlavours.forEach { updatedFlavour ->
            if (beverage.beverageFlavours.none { it.id == updatedFlavour.id }) {
                beverage.beverageFlavours.add(updatedFlavour)
            }
        }

        return beverage.beverageFlavours
    }

    private fun prepareBeverageDiscounts(
        beverageDto: BeverageDto,
        beverage: Beverage,
        vendor: Vendor
    ) {
        val existingDiscounts = beverage.beverageDiscounts.associateBy { it.discount.id }
        val incomingDiscounts = beverageDto.discounts!!

        val updatedBeverageDiscounts = incomingDiscounts.map { dto ->
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

            val beverageDiscount =
                dto.id?.let { existingDiscounts[it] } ?: BeverageDiscount().apply { this.beverage = beverage }
            beverageDiscount.discount = discount
            beverageDiscount
        }

        // remove old entities not present in the incoming dto (this triggers orphan removal)
        beverage.beverageDiscounts.removeIf { existingDiscount ->
            updatedBeverageDiscounts.none { it.discount.id == existingDiscount.id }
        }

        updatedBeverageDiscounts.forEach { updateDessertDiscount ->
            if (beverage.beverageDiscounts.none { it.discount.id == updateDessertDiscount.discount.id }) {
                beverage.beverageDiscounts.add(updateDessertDiscount)
            }
        }
       // return beverage.beverageDiscounts
    }

    private fun prepareBeverageFlavourSizes(
        flavourSizeDtos: Set<BeverageFlavourSizeDto>,
        beverageFlavour: BeverageFlavour
    ): MutableSet<BeverageFlavourSize> {

        val existingFlavourSizes = beverageFlavour.beverageFlavourSizes.associateBy { it.id }
        if (flavourSizeDtos.isEmpty()) {
            throw IllegalArgumentException("Please select at least one beverage flavour size.")
        }

        val updatedFlavourSizes = flavourSizeDtos.map { optionSizeDto ->
            val flavourSize = existingFlavourSizes[optionSizeDto.id] ?: BeverageFlavourSize().apply {
                this.beverageFlavour = beverageFlavour
            }

            flavourSize.apply {
                size = Size.fromString(optionSizeDto.size)
                name = (optionSizeDto.sizeName) ?: this.size!!.type
                price = requireNotNull(optionSizeDto.price) { "Please provide a price." }
                availability =
                    requireNotNull(Availability.fromString(optionSizeDto.availability)) { "Please enter an availability status." }
            }

        }

        beverageFlavour.beverageFlavourSizes.removeIf { existingSize ->
            updatedFlavourSizes.none { it.id == existingSize.id }
        }

        updatedFlavourSizes.forEach { updatedSize ->
            if (beverageFlavour.beverageFlavourSizes.none { it.id == updatedSize.id }) {
                beverageFlavour.beverageFlavourSizes.add(updatedSize)
            }
        }

        return beverageFlavour.beverageFlavourSizes
    }

    private fun prepareBeverageImages(beverageDto: BeverageDto, beverage: Beverage): MutableSet<BeverageImage> {
        val existingBeverageImages = beverage.beverageImages.associateBy { it.id }
        val incomingImages = beverageDto.beverageImages

        if (incomingImages.isEmpty() || incomingImages.size < 2) {
            throw IllegalArgumentException("Please add at least 2 images of the food")
        }

        val updateBeverageImages = incomingImages.map { dto ->
            val image = existingBeverageImages[dto.id] ?: BeverageImage().apply { this.beverage = beverage }
            image.imageUrl = dto.imageUrl
            image
        }

        beverage.beverageImages.removeIf { existingImage ->
            updateBeverageImages.none { it.id == existingImage.id }
        }

        updateBeverageImages.forEach { updatedImage ->
            if (beverage.beverageImages.none { it.id == updatedImage.id }) {
                beverage.beverageImages.add(updatedImage)
            }
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

        // Add any new types that don't already exist
        incomingOrderTypes.forEach { orderType ->
            if (orderType !in existingOrderTypes) {
                existingOrderTypes.add(orderType)
            }
        }

        return existingOrderTypes
    }
}

