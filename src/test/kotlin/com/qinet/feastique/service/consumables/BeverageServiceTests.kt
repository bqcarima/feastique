package com.qinet.feastique.service.consumables

import com.qinet.feastique.exception.DuplicateFoundException
import com.qinet.feastique.exception.RequestedEntityNotFoundException
import com.qinet.feastique.exception.UserNotFoundException
import com.qinet.feastique.model.dto.BeverageAvailabilityDto
import com.qinet.feastique.model.dto.FlavourAvailabilityDto
import com.qinet.feastique.model.dto.ImageDto
import com.qinet.feastique.model.dto.SizeAvailabilityDto
import com.qinet.feastique.model.dto.consumables.BeverageDto
import com.qinet.feastique.model.dto.consumables.BeverageFlavourDto
import com.qinet.feastique.model.dto.consumables.BeverageFlavourSizeDto
import com.qinet.feastique.model.entity.consumables.beverage.Beverage
import com.qinet.feastique.model.entity.consumables.flavour.BeverageFlavour
import com.qinet.feastique.model.entity.menu.Menu
import com.qinet.feastique.model.entity.size.BeverageFlavourSize
import com.qinet.feastique.model.entity.user.Vendor
import com.qinet.feastique.model.enums.Availability
import com.qinet.feastique.model.enums.BeverageGroup
import com.qinet.feastique.model.enums.OrderType
import com.qinet.feastique.model.enums.Size
import com.qinet.feastique.repository.bookmark.BeverageBookmarkRepository
import com.qinet.feastique.repository.consumables.beverage.BeverageRepository
import com.qinet.feastique.repository.discount.DiscountRepository
import com.qinet.feastique.repository.like.BeverageLikeRepository
import com.qinet.feastique.repository.menu.MenuRepository
import com.qinet.feastique.repository.user.VendorRepository
import com.qinet.feastique.security.UserSecurity
import com.qinet.feastique.utility.CursorEncoder
import com.qinet.feastique.utility.DuplicateUtility
import com.qinet.feastique.utility.SecurityUtility
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*
import org.springframework.data.domain.*
import org.springframework.security.core.authority.SimpleGrantedAuthority
import java.time.LocalTime
import java.util.*

class BeverageServiceTest {

    private val beverageRepository: BeverageRepository = mock()
    private val vendorRepository: VendorRepository = mock()
    private val duplicateUtility: DuplicateUtility = mock()
    private val menuRepository: MenuRepository = mock()
    private val discountRepository: DiscountRepository = mock()
    private val beverageLikeRepository: BeverageLikeRepository = mock()
    private val beverageBookmarkRepository: BeverageBookmarkRepository = mock()
    private val cursorEncoder: CursorEncoder = mock()
    private val securityUtility: SecurityUtility = mock()

    private val beverageService = BeverageService(
        beverageRepository,
        vendorRepository,
        duplicateUtility,
        menuRepository,
        discountRepository,
        beverageLikeRepository,
        beverageBookmarkRepository,
        cursorEncoder,
        securityUtility
    )

    private val vendorId: UUID = UUID.randomUUID()
    private val beverageId: UUID = UUID.randomUUID()
    private val customerId: UUID = UUID.randomUUID()

    // Initialized at class level so makeBeverage can reference it before @BeforeEach
    private val vendor: Vendor = Vendor().apply {
        id = vendorId
        username = "testvendor"
        firstName = "John"
        lastName = "Doe"
        chefName = "Chef John"
        password = "encoded"
        openingTime = LocalTime.of(8, 0)
        closingTime = LocalTime.of(22, 0)
    }

    private lateinit var vendorDetails: UserSecurity
    private lateinit var customerDetails: UserSecurity

    @BeforeEach
    fun setUp() {
        vendorDetails = UserSecurity(
            id = vendorId,
            username = "testvendor",
            password = "",
            mutableListOf(SimpleGrantedAuthority("ROLE_VENDOR"))
        )

        customerDetails = UserSecurity(
            id = customerId,
            username = "testcustomer",
            password = "",
            mutableListOf(SimpleGrantedAuthority("ROLE_CUSTOMER"))
        )
    }

    // --- getBeverage (vendor role) ---

    @Test
    fun `getBeverage returns response for vendor when beverage belongs to them`() {
        val beverage = makeBeverage(beverageId, "Sprite")
        whenever(securityUtility.getSingleRole(vendorDetails)).thenReturn("VENDOR")
        whenever(beverageRepository.findByIdAndVendorIdAndIsActiveTrue(beverageId, vendorId))
            .thenReturn(beverage)

        val result = beverageService.getBeverage(beverageId, vendorDetails)

        assertThat(result.id).isEqualTo(beverageId)
        assertThat(result.name).isEqualTo("Sprite")
    }

    @Test
    fun `getBeverage throws RequestedEntityNotFoundException for vendor when beverage not found`() {
        whenever(securityUtility.getSingleRole(vendorDetails)).thenReturn("VENDOR")
        whenever(beverageRepository.findByIdAndVendorIdAndIsActiveTrue(beverageId, vendorId))
            .thenReturn(null)

        assertThatThrownBy { beverageService.getBeverage(beverageId, vendorDetails) }
            .isInstanceOf(RequestedEntityNotFoundException::class.java)
    }

    // --- getBeverage (customer role) ---

    @Test
    fun `getBeverage returns liked and bookmarked true for customer when both exist`() {
        val beverage = makeBeverage(beverageId, "Fanta")
        whenever(securityUtility.getSingleRole(customerDetails)).thenReturn("CUSTOMER")
        whenever(beverageLikeRepository.existsByBeverageIdAndCustomerId(beverageId, customerId)).thenReturn(true)
        whenever(beverageBookmarkRepository.existsByBeverageIdAndCustomerId(beverageId, customerId)).thenReturn(true)
        whenever(beverageRepository.findById(beverageId)).thenReturn(Optional.of(beverage))

        val result = beverageService.getBeverage(beverageId, customerDetails)

        assertThat(result.likedByCurrentUser).isTrue()
        assertThat(result.bookmarkedByCurrentUser).isTrue()
    }

    @Test
    fun `getBeverage returns liked false and bookmarked false for customer when neither exist`() {
        val beverage = makeBeverage(beverageId, "Fanta")
        whenever(securityUtility.getSingleRole(customerDetails)).thenReturn("CUSTOMER")
        whenever(beverageLikeRepository.existsByBeverageIdAndCustomerId(beverageId, customerId)).thenReturn(false)
        whenever(beverageBookmarkRepository.existsByBeverageIdAndCustomerId(beverageId, customerId)).thenReturn(false)
        whenever(beverageRepository.findById(beverageId)).thenReturn(Optional.of(beverage))

        val result = beverageService.getBeverage(beverageId, customerDetails)

        assertThat(result.likedByCurrentUser).isFalse()
        assertThat(result.bookmarkedByCurrentUser).isFalse()
    }

    @Test
    fun `getBeverage throws RequestedEntityNotFoundException for customer when beverage not found`() {
        whenever(securityUtility.getSingleRole(customerDetails)).thenReturn("CUSTOMER")
        whenever(beverageLikeRepository.existsByBeverageIdAndCustomerId(beverageId, customerId)).thenReturn(false)
        whenever(beverageBookmarkRepository.existsByBeverageIdAndCustomerId(beverageId, customerId)).thenReturn(false)
        whenever(beverageRepository.findById(beverageId)).thenReturn(Optional.empty())

        assertThatThrownBy { beverageService.getBeverage(beverageId, customerDetails) }
            .isInstanceOf(RequestedEntityNotFoundException::class.java)
    }

    // --- getBeverageById ---

    @Test
    fun `getBeverageById returns beverage when found`() {
        val beverage = makeBeverage(beverageId, "Cola")
        whenever(beverageRepository.findByIdAndVendorIdAndIsActiveTrue(beverageId, vendorId))
            .thenReturn(beverage)

        val result = beverageService.getBeverageById(beverageId, vendorDetails)

        assertThat(result.id).isEqualTo(beverageId)
        assertThat(result.name).isEqualTo("Cola")
    }

    @Test
    fun `getBeverageById throws RequestedEntityNotFoundException when not found`() {
        whenever(beverageRepository.findByIdAndVendorIdAndIsActiveTrue(beverageId, vendorId))
            .thenReturn(null)

        assertThatThrownBy { beverageService.getBeverageById(beverageId, vendorDetails) }
            .isInstanceOf(RequestedEntityNotFoundException::class.java)
    }

    // --- getAllBeverages ---

    @Test
    fun `getAllBeverages returns paged responses`() {
        val beverage = makeBeverage(beverageId, "Pepsi")
        whenever(beverageRepository.findAllByVendorIdAndIsActiveTrue(eq(vendorId), any<Pageable>()))
            .thenReturn(PageImpl(listOf(beverage)))

        val result = beverageService.getAllBeverages(vendorDetails, 0, 10)

        assertThat(result.content).hasSize(1)
        assertThat(result.content[0].name).isEqualTo("Pepsi")
    }

    @Test
    fun `getAllBeverages returns empty page when vendor has no beverages`() {
        whenever(beverageRepository.findAllByVendorIdAndIsActiveTrue(eq(vendorId), any<Pageable>()))
            .thenReturn(PageImpl(emptyList()))

        val result = beverageService.getAllBeverages(vendorDetails, 0, 10)

        assertThat(result.content).isEmpty()
    }

    // --- scrollBeverages ---

    @Test
    fun `scrollBeverages returns empty window when no beverages exist`() {
        val emptyWindow = Window.from(emptyList<Beverage>()) { ScrollPosition.offset(it.toLong()) }
        whenever(securityUtility.getSingleRole(vendorDetails)).thenReturn("VENDOR")
        whenever(beverageRepository.findAllByVendorIdAndIsActiveTrue(
            eq(vendorId), any<ScrollPosition>(), any<Sort>(), any<Limit>()
        )).thenReturn(emptyWindow)

        val result = beverageService.scrollBeverages(vendorId, null, 10, vendorDetails)

        assertThat(result.content).isEmpty()
        assertThat(result.hasNext).isFalse()
        assertThat(result.nextCursor).isNull()
    }

    @Test
    fun `scrollBeverages returns mapped beverage responses for vendor`() {
        val beverage = makeBeverage(beverageId, "Tonic Water")
        val window = Window.from(listOf(beverage)) { ScrollPosition.offset(it.toLong()) }
        whenever(securityUtility.getSingleRole(vendorDetails)).thenReturn("VENDOR")
        whenever(beverageRepository.findAllByVendorIdAndIsActiveTrue(
            eq(vendorId), any<ScrollPosition>(), any<Sort>(), any<Limit>()
        )).thenReturn(window)
        whenever(cursorEncoder.encodeOffset(any())).thenReturn("dummyCursor")

        val result = beverageService.scrollBeverages(vendorId, null, 10, vendorDetails)

        assertThat(result.content).hasSize(1)
        assertThat(result.content[0].name).isEqualTo("Tonic Water")
    }

    @Test
    fun `scrollBeverages resolves like and bookmark status for customer`() {
        val beverage = makeBeverage(beverageId, "Lemonade")
        val window = Window.from(listOf(beverage)) { ScrollPosition.offset(it.toLong()) }
        whenever(securityUtility.getSingleRole(customerDetails)).thenReturn("CUSTOMER")
        whenever(beverageRepository.findAllByVendorIdAndIsActiveTrue(
            eq(vendorId), any<ScrollPosition>(), any<Sort>(), any<Limit>()
        )).thenReturn(window)
        whenever(beverageLikeRepository.findAllByCustomerIdAndBeverageIdIn(eq(customerId), any()))
            .thenReturn(emptyList())
        whenever(beverageBookmarkRepository.findAllByCustomerIdAndBeverageIdIn(eq(customerId), any()))
            .thenReturn(emptyList())
        whenever(cursorEncoder.encodeOffset(any())).thenReturn("dummyCursor")

        val result = beverageService.scrollBeverages(vendorId, null, 10, customerDetails)

        assertThat(result.content).hasSize(1)
        assertThat(result.content[0].likedByCurrentUser).isFalse()
        assertThat(result.content[0].bookmarkedByCurrentUser).isFalse()
    }

    @Test
    fun `scrollBeverages with numeric cursor calls repository`() {
        val window = Window.from(emptyList<Beverage>()) { ScrollPosition.offset(it.toLong()) }
        whenever(securityUtility.getSingleRole(vendorDetails)).thenReturn("VENDOR")
        whenever(beverageRepository.findAllByVendorIdAndIsActiveTrue(
            eq(vendorId), any<ScrollPosition>(), any<Sort>(), any<Limit>()
        )).thenReturn(window)

        beverageService.scrollBeverages(vendorId, "5", 10, vendorDetails)

        verify(beverageRepository).findAllByVendorIdAndIsActiveTrue(
            eq(vendorId), any<ScrollPosition>(), any<Sort>(), any<Limit>()
        )
    }

    // --- deleteBeverage ---

    @Test
    fun `deleteBeverage soft-deletes by setting isActive to false`() {
        val beverage = makeBeverage(beverageId, "Ginger Ale")
        whenever(beverageRepository.findByIdAndVendorIdAndIsActiveTrue(beverageId, vendorId))
            .thenReturn(beverage)
        whenever(beverageRepository.saveAndFlush(beverage)).thenReturn(beverage)

        beverageService.deleteBeverage(beverageId, vendorDetails)

        assertThat(beverage.isActive).isFalse()
        verify(beverageRepository).saveAndFlush(beverage)
    }

    @Test
    fun `deleteBeverage throws when beverage not found`() {
        whenever(beverageRepository.findByIdAndVendorIdAndIsActiveTrue(beverageId, vendorId))
            .thenReturn(null)

        assertThatThrownBy { beverageService.deleteBeverage(beverageId, vendorDetails) }
            .isInstanceOf(RequestedEntityNotFoundException::class.java)

        verify(beverageRepository, never()).saveAndFlush(any())
    }

    // --- changeBeverageAvailability ---

    @Test
    fun `changeBeverageAvailability updates beverage availability`() {
        val beverage = makeBeverage(beverageId, "Sparkling Water")
        beverage.availability = Availability.UNAVAILABLE
        val dto = BeverageAvailabilityDto(beverageId = beverageId, availability = "AVAILABLE", beverageFlavours = null)
        whenever(beverageRepository.findByIdAndVendorIdAndIsActiveTrue(beverageId, vendorId))
            .thenReturn(beverage)
        whenever(beverageRepository.saveAndFlush(beverage)).thenReturn(beverage)

        beverageService.changeBeverageAvailability(dto, beverageId, vendorDetails)

        assertThat(beverage.availability).isEqualTo(Availability.AVAILABLE)
    }

    @Test
    fun `changeBeverageAvailability does not change availability when value is unchanged`() {
        val beverage = makeBeverage(beverageId, "Still Water")
        beverage.availability = Availability.AVAILABLE
        val dto = BeverageAvailabilityDto(beverageId = beverageId, availability = "AVAILABLE", beverageFlavours = null)
        whenever(beverageRepository.findByIdAndVendorIdAndIsActiveTrue(beverageId, vendorId))
            .thenReturn(beverage)
        whenever(beverageRepository.saveAndFlush(beverage)).thenReturn(beverage)

        beverageService.changeBeverageAvailability(dto, beverageId, vendorDetails)

        assertThat(beverage.availability).isEqualTo(Availability.AVAILABLE)
    }

    @Test
    fun `changeBeverageAvailability updates flavour and size availability`() {
        val flavourId = UUID.randomUUID()
        val sizeId = UUID.randomUUID()

        val flavourSize = BeverageFlavourSize().apply {
            id = sizeId
            size = Size.MEDIUM
            name = "Medium"
            price = 500L
            availability = Availability.AVAILABLE
        }

        val flavour = BeverageFlavour().apply {
            id = flavourId
            name = "Lemon"
            availability = Availability.AVAILABLE
            beverageFlavourSizes = mutableSetOf(flavourSize)
        }

        val beverage = makeBeverage(beverageId, "Iced Tea")
        beverage.beverageFlavours = mutableSetOf(flavour)
        flavour.beverage = beverage

        val sizeDto = SizeAvailabilityDto(sizeId = sizeId, availability = "UNAVAILABLE")
        val flavourDto = FlavourAvailabilityDto(
            flavourId = flavourId,
            availability = "UNAVAILABLE",
            flavourSizes = setOf(sizeDto)
        )
        val dto = BeverageAvailabilityDto(
            beverageId = beverageId,
            availability = "AVAILABLE",
            beverageFlavours = setOf(flavourDto)
        )

        whenever(beverageRepository.findByIdAndVendorIdAndIsActiveTrue(beverageId, vendorId))
            .thenReturn(beverage)
        whenever(beverageRepository.saveAndFlush(beverage)).thenReturn(beverage)

        beverageService.changeBeverageAvailability(dto, beverageId, vendorDetails)

        assertThat(flavour.availability).isEqualTo(Availability.UNAVAILABLE)
        assertThat(flavourSize.availability).isEqualTo(Availability.UNAVAILABLE)
    }

    @Test
    fun `changeBeverageAvailability throws when beverage not found`() {
        val dto = BeverageAvailabilityDto(beverageId = beverageId, availability = "AVAILABLE", beverageFlavours = null)
        whenever(beverageRepository.findByIdAndVendorIdAndIsActiveTrue(beverageId, vendorId))
            .thenReturn(null)

        assertThatThrownBy { beverageService.changeBeverageAvailability(dto, beverageId, vendorDetails) }
            .isInstanceOf(RequestedEntityNotFoundException::class.java)
    }

    // --- addOrUpdateBeverage (create path) ---

    @Test
    fun `addOrUpdateBeverage creates new beverage successfully`() {
        val dto = makeCreateDto("Mango Juice")
        whenever(vendorRepository.findById(vendorId)).thenReturn(Optional.of(vendor))
        whenever(duplicateUtility.isDuplicateBeverageFound("Mango Juice", vendorId)).thenReturn(false)
        whenever(beverageRepository.saveAndFlush(any())).thenAnswer { it.arguments[0] }
        whenever(menuRepository.save(any())).thenAnswer { it.arguments[0] }

        val result = beverageService.addOrUpdateBeverage(dto, vendorDetails)

        assertThat(result.name).isEqualTo("Mango Juice")
        verify(beverageRepository, atLeastOnce()).saveAndFlush(any())
    }

    @Test
    fun `addOrUpdateBeverage throws DuplicateFoundException when name already exists`() {
        val dto = makeCreateDto("Mango Juice")
        whenever(vendorRepository.findById(vendorId)).thenReturn(Optional.of(vendor))
        whenever(duplicateUtility.isDuplicateBeverageFound("Mango Juice", vendorId)).thenReturn(true)

        assertThatThrownBy { beverageService.addOrUpdateBeverage(dto, vendorDetails) }
            .isInstanceOf(DuplicateFoundException::class.java)

        verify(beverageRepository, never()).saveAndFlush(any())
    }

    @Test
    fun `addOrUpdateBeverage throws UserNotFoundException when vendor not found`() {
        val dto = makeCreateDto("Mango Juice")
        whenever(vendorRepository.findById(vendorId)).thenReturn(Optional.empty())

        assertThatThrownBy { beverageService.addOrUpdateBeverage(dto, vendorDetails) }
            .isInstanceOf(UserNotFoundException::class.java)
    }

    @Test
    fun `addOrUpdateBeverage throws IllegalArgumentException when no order types provided`() {
        val dto = makeCreateDto("Mango Juice").copy(orderTypes = emptySet())
        whenever(vendorRepository.findById(vendorId)).thenReturn(Optional.of(vendor))
        whenever(duplicateUtility.isDuplicateBeverageFound("Mango Juice", vendorId)).thenReturn(false)
        whenever(beverageRepository.saveAndFlush(any())).thenAnswer { it.arguments[0] }

        assertThatThrownBy { beverageService.addOrUpdateBeverage(dto, vendorDetails) }
            .isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun `addOrUpdateBeverage throws IllegalArgumentException when no available days provided`() {
        val dto = makeCreateDto("Mango Juice").copy(availableDays = emptySet())
        whenever(vendorRepository.findById(vendorId)).thenReturn(Optional.of(vendor))
        whenever(duplicateUtility.isDuplicateBeverageFound("Mango Juice", vendorId)).thenReturn(false)
        whenever(beverageRepository.saveAndFlush(any())).thenAnswer { it.arguments[0] }

        assertThatThrownBy { beverageService.addOrUpdateBeverage(dto, vendorDetails) }
            .isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun `addOrUpdateBeverage throws IllegalArgumentException when fewer than two images provided`() {
        val dto = makeCreateDto("Mango Juice").copy(
            beverageImages = setOf(ImageDto(imageUrl = "img1.jpg"))
        )
        whenever(vendorRepository.findById(vendorId)).thenReturn(Optional.of(vendor))
        whenever(duplicateUtility.isDuplicateBeverageFound("Mango Juice", vendorId)).thenReturn(false)
        whenever(beverageRepository.saveAndFlush(any())).thenAnswer { it.arguments[0] }

        assertThatThrownBy { beverageService.addOrUpdateBeverage(dto, vendorDetails) }
            .isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun `addOrUpdateBeverage throws IllegalArgumentException when no images provided`() {
        val dto = makeCreateDto("Mango Juice").copy(beverageImages = emptySet())
        whenever(vendorRepository.findById(vendorId)).thenReturn(Optional.of(vendor))
        whenever(duplicateUtility.isDuplicateBeverageFound("Mango Juice", vendorId)).thenReturn(false)
        whenever(beverageRepository.saveAndFlush(any())).thenAnswer { it.arguments[0] }

        assertThatThrownBy { beverageService.addOrUpdateBeverage(dto, vendorDetails) }
            .isInstanceOf(IllegalArgumentException::class.java)
    }

    // --- addOrUpdateBeverage (update path) ---

    @Test
    fun `addOrUpdateBeverage updates existing beverage when id provided`() {
        val existingBeverage = makeBeverage(beverageId, "Old Name")
        existingBeverage.menu = Menu().apply { this.beverage = existingBeverage }

        val dto = makeCreateDto("New Name").copy(id = beverageId)
        whenever(vendorRepository.findById(vendorId)).thenReturn(Optional.of(vendor))
        whenever(beverageRepository.findByIdAndVendorIdAndIsActiveTrue(beverageId, vendorId))
            .thenReturn(existingBeverage)
        whenever(beverageRepository.saveAndFlush(any())).thenAnswer { it.arguments[0] }
        whenever(menuRepository.findById(any())).thenReturn(Optional.of(existingBeverage.menu!!))
        whenever(menuRepository.save(any())).thenAnswer { it.arguments[0] }

        val result = beverageService.addOrUpdateBeverage(dto, vendorDetails)

        assertThat(result.name).isEqualTo("New Name")
    }

    @Test
    fun `addOrUpdateBeverage throws RequestedEntityNotFoundException when updating non-existent beverage`() {
        val dto = makeCreateDto("Juice").copy(id = beverageId)
        whenever(vendorRepository.findById(vendorId)).thenReturn(Optional.of(vendor))
        whenever(beverageRepository.findByIdAndVendorIdAndIsActiveTrue(beverageId, vendorId))
            .thenReturn(null)

        assertThatThrownBy { beverageService.addOrUpdateBeverage(dto, vendorDetails) }
            .isInstanceOf(RequestedEntityNotFoundException::class.java)
    }

    // --- helpers ---

    private fun makeBeverage(id: UUID, name: String): Beverage {
        return Beverage().apply {
            this.id = id
            this.name = name
            this.vendor = this@BeverageServiceTest.vendor
            this.isActive = true
            this.availability = Availability.AVAILABLE
            this.alcoholic = false
            this.percentage = 0
            this.beverageGroup = BeverageGroup.JUICE
            this.deliverable = false
            this.deliveryFee = 0L
            this.preparationTime = 5
            this.orderTypes = mutableSetOf(OrderType.DINE_IN)
            this.availableDays = mutableSetOf()
            this.beverageFlavours = mutableSetOf()
            this.beverageImages = mutableSetOf()
            this.beverageDiscounts = mutableSetOf()
        }
    }

    private fun makeCreateDto(name: String): BeverageDto {
        val sizeDto = BeverageFlavourSizeDto(
            id = null,
            size = "MEDIUM",
            sizeName = "Regular",
            price = 500L,
            availability = "AVAILABLE"
        )
        val flavourDto = BeverageFlavourDto(
            id = null,
            flavourName = "Original",
            description = null,
            flavourSizes = setOf(sizeDto),
            availability = "AVAILABLE"
        )
        return BeverageDto(
            id = null,
            beverageName = name,
            beverageGroup = "JUICE",
            alcoholic = false,
            percentage = 0,
            orderTypes = setOf("DINE_IN"),
            availableDays = setOf("MONDAY"),
            deliveryFee = 0L,
            deliverable = false,
            dailyDeliveryQuantity = null,
            availability = "AVAILABLE",
            readyAsFrom = null,
            preparationTime = 5,
            quickDelivery = false,
            beverageFlavours = setOf(flavourDto),
            beverageImages = setOf(
                ImageDto(imageUrl = "img1.jpg"),
                ImageDto(imageUrl = "img2.jpg")
            ),
            discounts = emptySet()
        )
    }
}

