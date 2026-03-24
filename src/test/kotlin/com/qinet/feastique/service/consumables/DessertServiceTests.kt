package com.qinet.feastique.service.consumables

import com.qinet.feastique.exception.DuplicateFoundException
import com.qinet.feastique.exception.RequestedEntityNotFoundException
import com.qinet.feastique.exception.UserNotFoundException
import com.qinet.feastique.model.dto.DessertAvailabilityDto
import com.qinet.feastique.model.dto.FlavourAvailabilityDto
import com.qinet.feastique.model.dto.ImageDto
import com.qinet.feastique.model.dto.SizeAvailabilityDto
import com.qinet.feastique.model.dto.consumables.DessertDto
import com.qinet.feastique.model.dto.consumables.DessertFlavourDto
import com.qinet.feastique.model.dto.consumables.DessertFlavourSizeDto
import com.qinet.feastique.model.entity.consumables.dessert.Dessert
import com.qinet.feastique.model.entity.consumables.flavour.DessertFlavour
import com.qinet.feastique.model.entity.menu.Menu
import com.qinet.feastique.model.entity.size.DessertFlavourSize
import com.qinet.feastique.model.entity.user.Vendor
import com.qinet.feastique.model.enums.Availability
import com.qinet.feastique.model.enums.DessertType
import com.qinet.feastique.model.enums.OrderType
import com.qinet.feastique.model.enums.Size
import com.qinet.feastique.repository.bookmark.DessertBookmarkRepository
import com.qinet.feastique.repository.consumables.dessert.DessertRepository
import com.qinet.feastique.repository.discount.DiscountRepository
import com.qinet.feastique.repository.like.DessertLikeRepository
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

class DessertServiceTest {

    private val dessertRepository: DessertRepository = mock()
    private val vendorRepository: VendorRepository = mock()
    private val duplicateUtility: DuplicateUtility = mock()
    private val menuRepository: MenuRepository = mock()
    private val discountRepository: DiscountRepository = mock()
    private val dessertLikeRepository: DessertLikeRepository = mock()
    private val cursorEncoder: CursorEncoder = mock()
    private val securityUtility: SecurityUtility = mock()
    private val dessertBookmarkRepository: DessertBookmarkRepository = mock()

    private val dessertService = DessertService(
        dessertRepository,
        vendorRepository,
        duplicateUtility,
        menuRepository,
        discountRepository,
        dessertLikeRepository,
        cursorEncoder,
        securityUtility,
        dessertBookmarkRepository
    )

    private val vendorId: UUID = UUID.randomUUID()
    private val dessertId: UUID = UUID.randomUUID()
    private val customerId: UUID = UUID.randomUUID()

    // Initialized at class level so makeDessert can reference it before @BeforeEach
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

    // getDessert (vendor role)

    @Test
    fun `getDessert returns response for vendor when dessert belongs to them`() {
        val dessert = makeDessert(dessertId, "Chocolate Cake")
        whenever(securityUtility.getSingleRole(vendorDetails)).thenReturn("VENDOR")
        whenever(dessertRepository.findByIdAndVendorIdAndIsActiveTrue(dessertId, vendorId))
            .thenReturn(dessert)

        val result = dessertService.getDessert(dessertId, vendorDetails)

        assertThat(result.id).isEqualTo(dessertId)
        assertThat(result.name).isEqualTo("Chocolate Cake")
    }

    @Test
    fun `getDessert throws RequestedEntityNotFoundException for vendor when dessert not found`() {
        whenever(securityUtility.getSingleRole(vendorDetails)).thenReturn("VENDOR")
        whenever(dessertRepository.findByIdAndVendorIdAndIsActiveTrue(dessertId, vendorId))
            .thenReturn(null)

        assertThatThrownBy { dessertService.getDessert(dessertId, vendorDetails) }
            .isInstanceOf(RequestedEntityNotFoundException::class.java)
    }

    // getDessert (customer role)

    @Test
    fun `getDessert returns liked and bookmarked true for customer when both exist`() {
        val dessert = makeDessert(dessertId, "Cheesecake")
        whenever(securityUtility.getSingleRole(customerDetails)).thenReturn("CUSTOMER")
        whenever(dessertLikeRepository.existsByDessertIdAndCustomerId(dessertId, customerId)).thenReturn(true)
        whenever(dessertBookmarkRepository.existsByDessertIdAndCustomerId(dessertId, customerId)).thenReturn(true)
        whenever(dessertRepository.findById(dessertId)).thenReturn(Optional.of(dessert))

        val result = dessertService.getDessert(dessertId, customerDetails)

        assertThat(result.likedByCurrentUser).isTrue()
        assertThat(result.bookmarkedByCurrentUser).isTrue()
    }

    @Test
    fun `getDessert returns liked false and bookmarked false for customer when neither exist`() {
        val dessert = makeDessert(dessertId, "Cheesecake")
        whenever(securityUtility.getSingleRole(customerDetails)).thenReturn("CUSTOMER")
        whenever(dessertLikeRepository.existsByDessertIdAndCustomerId(dessertId, customerId)).thenReturn(false)
        whenever(dessertBookmarkRepository.existsByDessertIdAndCustomerId(dessertId, customerId)).thenReturn(false)
        whenever(dessertRepository.findById(dessertId)).thenReturn(Optional.of(dessert))

        val result = dessertService.getDessert(dessertId, customerDetails)

        assertThat(result.likedByCurrentUser).isFalse()
        assertThat(result.bookmarkedByCurrentUser).isFalse()
    }

    @Test
    fun `getDessert throws RequestedEntityNotFoundException for customer when dessert not found`() {
        whenever(securityUtility.getSingleRole(customerDetails)).thenReturn("CUSTOMER")
        whenever(dessertLikeRepository.existsByDessertIdAndCustomerId(dessertId, customerId)).thenReturn(false)
        whenever(dessertBookmarkRepository.existsByDessertIdAndCustomerId(dessertId, customerId)).thenReturn(false)
        whenever(dessertRepository.findById(dessertId)).thenReturn(Optional.empty())

        assertThatThrownBy { dessertService.getDessert(dessertId, customerDetails) }
            .isInstanceOf(RequestedEntityNotFoundException::class.java)
    }

    // getDessertById

    @Test
    fun `getDessertById returns dessert when found`() {
        val dessert = makeDessert(dessertId, "Brownie")
        whenever(dessertRepository.findByIdAndVendorIdAndIsActiveTrue(dessertId, vendorId))
            .thenReturn(dessert)

        val result = dessertService.getDessertById(dessertId, vendorDetails)

        assertThat(result.id).isEqualTo(dessertId)
        assertThat(result.name).isEqualTo("Brownie")
    }

    @Test
    fun `getDessertById throws RequestedEntityNotFoundException when not found`() {
        whenever(dessertRepository.findByIdAndVendorIdAndIsActiveTrue(dessertId, vendorId))
            .thenReturn(null)

        assertThatThrownBy { dessertService.getDessertById(dessertId, vendorDetails) }
            .isInstanceOf(RequestedEntityNotFoundException::class.java)
    }

    // getAllDesserts

    @Test
    fun `getAllDesserts returns paged responses`() {
        val dessert = makeDessert(dessertId, "Tiramisu")
        whenever(dessertRepository.findAllByVendorIdAndIsActiveTrue(eq(vendorId), any<Pageable>()))
            .thenReturn(PageImpl(listOf(dessert)))

        val result = dessertService.getAllDesserts(vendorDetails, 0, 10)

        assertThat(result.content).hasSize(1)
        assertThat(result.content[0].name).isEqualTo("Tiramisu")
    }

    @Test
    fun `getAllDesserts returns empty page when vendor has no desserts`() {
        whenever(dessertRepository.findAllByVendorIdAndIsActiveTrue(eq(vendorId), any<Pageable>()))
            .thenReturn(PageImpl(emptyList()))

        val result = dessertService.getAllDesserts(vendorDetails, 0, 10)

        assertThat(result.content).isEmpty()
    }

    // scrollDesserts

    @Test
    fun `scrollDesserts returns empty window when no desserts exist`() {
        val emptyWindow = Window.from(emptyList<Dessert>()) { ScrollPosition.offset(it.toLong()) }
        whenever(securityUtility.getSingleRole(vendorDetails)).thenReturn("VENDOR")
        whenever(dessertRepository.findAllByVendorIdAndIsActiveTrue(
            eq(vendorId), any<ScrollPosition>(), any<Sort>(), any<Limit>()
        )).thenReturn(emptyWindow)

        val result = dessertService.scrollDesserts(vendorId, null, 10, vendorDetails)

        assertThat(result.content).isEmpty()
        assertThat(result.hasNext).isFalse()
        assertThat(result.nextCursor).isNull()
    }

    @Test
    fun `scrollDesserts returns mapped dessert responses for vendor`() {
        val dessert = makeDessert(dessertId, "Muffin")
        val window = Window.from(listOf(dessert)) { ScrollPosition.offset(it.toLong()) }
        whenever(securityUtility.getSingleRole(vendorDetails)).thenReturn("VENDOR")
        whenever(dessertRepository.findAllByVendorIdAndIsActiveTrue(
            eq(vendorId), any<ScrollPosition>(), any<Sort>(), any<Limit>()
        )).thenReturn(window)
        whenever(cursorEncoder.encodeOffset(any())).thenReturn("dummyCursor")

        val result = dessertService.scrollDesserts(vendorId, null, 10, vendorDetails)

        assertThat(result.content).hasSize(1)
        assertThat(result.content[0].name).isEqualTo("Muffin")
    }

    @Test
    fun `scrollDesserts resolves like and bookmark status for customer`() {
        val dessert = makeDessert(dessertId, "Cupcake")
        val window = Window.from(listOf(dessert)) { ScrollPosition.offset(it.toLong()) }
        whenever(securityUtility.getSingleRole(customerDetails)).thenReturn("CUSTOMER")
        whenever(dessertRepository.findAllByVendorIdAndIsActiveTrue(
            eq(vendorId), any<ScrollPosition>(), any<Sort>(), any<Limit>()
        )).thenReturn(window)
        whenever(dessertLikeRepository.findAllByCustomerIdAndDessertIdIn(eq(customerId), any()))
            .thenReturn(emptyList())
        whenever(dessertBookmarkRepository.findAllByCustomerIdAndDessertIdIn(eq(customerId), any()))
            .thenReturn(emptyList())
        whenever(cursorEncoder.encodeOffset(any())).thenReturn("dummyCursor")

        val result = dessertService.scrollDesserts(vendorId, null, 10, customerDetails)

        assertThat(result.content).hasSize(1)
        assertThat(result.content[0].likedByCurrentUser).isFalse()
        assertThat(result.content[0].bookmarkedByCurrentUser).isFalse()
    }

    @Test
    fun `scrollDesserts with numeric cursor calls repository`() {
        val window = Window.from(emptyList<Dessert>()) { ScrollPosition.offset(it.toLong()) }
        whenever(securityUtility.getSingleRole(vendorDetails)).thenReturn("VENDOR")
        whenever(dessertRepository.findAllByVendorIdAndIsActiveTrue(
            eq(vendorId), any<ScrollPosition>(), any<Sort>(), any<Limit>()
        )).thenReturn(window)

        dessertService.scrollDesserts(vendorId, "5", 10, vendorDetails)

        verify(dessertRepository).findAllByVendorIdAndIsActiveTrue(
            eq(vendorId), any<ScrollPosition>(), any<Sort>(), any<Limit>()
        )
    }

    // deleteDessert

    @Test
    fun `deleteDessert soft-deletes by setting isActive to false`() {
        val dessert = makeDessert(dessertId, "Donut")
        whenever(dessertRepository.findByIdAndVendorIdAndIsActiveTrue(dessertId, vendorId))
            .thenReturn(dessert)
        whenever(dessertRepository.saveAndFlush(dessert)).thenReturn(dessert)

        dessertService.deleteDessert(dessertId, vendorDetails)

        assertThat(dessert.isActive).isFalse()
        verify(dessertRepository).saveAndFlush(dessert)
    }

    @Test
    fun `deleteDessert throws when dessert not found`() {
        whenever(dessertRepository.findByIdAndVendorIdAndIsActiveTrue(dessertId, vendorId))
            .thenReturn(null)

        assertThatThrownBy { dessertService.deleteDessert(dessertId, vendorDetails) }
            .isInstanceOf(RequestedEntityNotFoundException::class.java)

        verify(dessertRepository, never()).saveAndFlush(any())
    }

    // changeDessertAvailability

    @Test
    fun `changeDessertAvailability updates dessert availability`() {
        val dessert = makeDessert(dessertId, "Pudding")
        dessert.availability = Availability.UNAVAILABLE
        val dto = DessertAvailabilityDto(dessertId = dessertId, availability = "AVAILABLE", dessertFlavours = null)
        whenever(dessertRepository.findByIdAndVendorIdAndIsActiveTrue(dessertId, vendorId))
            .thenReturn(dessert)
        whenever(dessertRepository.saveAndFlush(dessert)).thenReturn(dessert)

        dessertService.changeDessertAvailability(dto, dessertId, vendorDetails)

        assertThat(dessert.availability).isEqualTo(Availability.AVAILABLE)
    }

    @Test
    fun `changeDessertAvailability does not change availability when value is unchanged`() {
        val dessert = makeDessert(dessertId, "Pie")
        dessert.availability = Availability.AVAILABLE
        val dto = DessertAvailabilityDto(dessertId = dessertId, availability = "AVAILABLE", dessertFlavours = null)
        whenever(dessertRepository.findByIdAndVendorIdAndIsActiveTrue(dessertId, vendorId))
            .thenReturn(dessert)
        whenever(dessertRepository.saveAndFlush(dessert)).thenReturn(dessert)

        dessertService.changeDessertAvailability(dto, dessertId, vendorDetails)

        assertThat(dessert.availability).isEqualTo(Availability.AVAILABLE)
    }

    @Test
    fun `changeDessertAvailability updates flavour and size availability`() {
        val flavourId = UUID.randomUUID()
        val sizeId = UUID.randomUUID()

        val flavourSize = DessertFlavourSize().apply {
            id = sizeId
            size = Size.MEDIUM
            name = "Medium"
            price = 500L
            availability = Availability.AVAILABLE
        }

        val flavour = DessertFlavour().apply {
            id = flavourId
            name = "Vanilla"
            availability = Availability.AVAILABLE
            dessertFlavourSizes = mutableSetOf(flavourSize)
        }

        val dessert = makeDessert(dessertId, "Ice Cream")
        dessert.dessertFlavours = mutableListOf(flavour)
        flavour.dessert = dessert

        val sizeDto = SizeAvailabilityDto(sizeId = sizeId, availability = "UNAVAILABLE")
        val flavourDto = FlavourAvailabilityDto(
            flavourId = flavourId,
            availability = "UNAVAILABLE",
            flavourSizes = setOf(sizeDto)
        )
        val dto = DessertAvailabilityDto(
            dessertId = dessertId,
            availability = "AVAILABLE",
            dessertFlavours = setOf(flavourDto)
        )

        whenever(dessertRepository.findByIdAndVendorIdAndIsActiveTrue(dessertId, vendorId))
            .thenReturn(dessert)
        whenever(dessertRepository.saveAndFlush(dessert)).thenReturn(dessert)

        dessertService.changeDessertAvailability(dto, dessertId, vendorDetails)

        assertThat(flavour.availability).isEqualTo(Availability.UNAVAILABLE)
        assertThat(flavourSize.availability).isEqualTo(Availability.UNAVAILABLE)
    }

    @Test
    fun `changeDessertAvailability throws when dessert not found`() {
        val dto = DessertAvailabilityDto(dessertId = dessertId, availability = "AVAILABLE", dessertFlavours = null)
        whenever(dessertRepository.findByIdAndVendorIdAndIsActiveTrue(dessertId, vendorId))
            .thenReturn(null)

        assertThatThrownBy { dessertService.changeDessertAvailability(dto, dessertId, vendorDetails) }
            .isInstanceOf(RequestedEntityNotFoundException::class.java)
    }

    // addOrUpdateDessert (create path)

    @Test
    fun `addOrUpdateDessert creates new dessert successfully`() {
        val dto = makeCreateDto("Lava Cake")
        whenever(vendorRepository.findById(vendorId)).thenReturn(Optional.of(vendor))
        whenever(duplicateUtility.isDuplicateDessertFound("Lava Cake", vendorId)).thenReturn(false)
        whenever(dessertRepository.saveAndFlush(any())).thenAnswer { it.arguments[0] }
        whenever(dessertRepository.save(any())).thenAnswer { it.arguments[0] }
        whenever(menuRepository.save(any())).thenAnswer { it.arguments[0] }
        whenever(menuRepository.saveAndFlush(any())).thenAnswer { it.arguments[0] }

        val result = dessertService.addOrUpdateDessert(dto, vendorDetails)

        assertThat(result.name).isEqualTo("Lava Cake")
        verify(dessertRepository, atLeastOnce()).saveAndFlush(any())
    }

    @Test
    fun `addOrUpdateDessert throws DuplicateFoundException when name already exists`() {
        val dto = makeCreateDto("Lava Cake")
        whenever(vendorRepository.findById(vendorId)).thenReturn(Optional.of(vendor))
        whenever(duplicateUtility.isDuplicateDessertFound("Lava Cake", vendorId)).thenReturn(true)

        assertThatThrownBy { dessertService.addOrUpdateDessert(dto, vendorDetails) }
            .isInstanceOf(DuplicateFoundException::class.java)

        verify(dessertRepository, never()).saveAndFlush(any())
    }

    @Test
    fun `addOrUpdateDessert throws UserNotFoundException when vendor not found`() {
        val dto = makeCreateDto("Lava Cake")
        whenever(vendorRepository.findById(vendorId)).thenReturn(Optional.empty())

        assertThatThrownBy { dessertService.addOrUpdateDessert(dto, vendorDetails) }
            .isInstanceOf(UserNotFoundException::class.java)
    }

    @Test
    fun `addOrUpdateDessert throws IllegalArgumentException when no order types provided`() {
        val dto = makeCreateDto("Lava Cake").copy(orderTypes = emptySet())
        whenever(vendorRepository.findById(vendorId)).thenReturn(Optional.of(vendor))
        whenever(duplicateUtility.isDuplicateDessertFound("Lava Cake", vendorId)).thenReturn(false)
        whenever(dessertRepository.saveAndFlush(any())).thenAnswer { it.arguments[0] }

        assertThatThrownBy { dessertService.addOrUpdateDessert(dto, vendorDetails) }
            .isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun `addOrUpdateDessert throws IllegalArgumentException when no available days provided`() {
        val dto = makeCreateDto("Lava Cake").copy(availableDays = emptySet())
        whenever(vendorRepository.findById(vendorId)).thenReturn(Optional.of(vendor))
        whenever(duplicateUtility.isDuplicateDessertFound("Lava Cake", vendorId)).thenReturn(false)
        whenever(dessertRepository.saveAndFlush(any())).thenAnswer { it.arguments[0] }

        assertThatThrownBy { dessertService.addOrUpdateDessert(dto, vendorDetails) }
            .isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun `addOrUpdateDessert throws IllegalArgumentException when fewer than two images provided`() {
        val dto = makeCreateDto("Lava Cake").copy(
            dessertImages = setOf(ImageDto(imageUrl = "img1.jpg"))
        )
        whenever(vendorRepository.findById(vendorId)).thenReturn(Optional.of(vendor))
        whenever(duplicateUtility.isDuplicateDessertFound("Lava Cake", vendorId)).thenReturn(false)
        whenever(dessertRepository.saveAndFlush(any())).thenAnswer { it.arguments[0] }

        assertThatThrownBy { dessertService.addOrUpdateDessert(dto, vendorDetails) }
            .isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun `addOrUpdateDessert throws IllegalArgumentException when no images provided`() {
        val dto = makeCreateDto("Lava Cake").copy(dessertImages = emptySet())
        whenever(vendorRepository.findById(vendorId)).thenReturn(Optional.of(vendor))
        whenever(duplicateUtility.isDuplicateDessertFound("Lava Cake", vendorId)).thenReturn(false)
        whenever(dessertRepository.saveAndFlush(any())).thenAnswer { it.arguments[0] }

        assertThatThrownBy { dessertService.addOrUpdateDessert(dto, vendorDetails) }
            .isInstanceOf(IllegalArgumentException::class.java)
    }

    // addOrUpdateDessert (update path)

    @Test
    fun `addOrUpdateDessert updates existing dessert when id provided`() {
        val existingDessert = makeDessert(dessertId, "Old Name")
        existingDessert.menu = Menu().apply { this.dessert = existingDessert }

        val dto = makeCreateDto("New Name").copy(id = dessertId)
        whenever(vendorRepository.findById(vendorId)).thenReturn(Optional.of(vendor))
        whenever(dessertRepository.findByIdAndVendorIdAndIsActiveTrue(dessertId, vendorId))
            .thenReturn(existingDessert)
        whenever(dessertRepository.saveAndFlush(any())).thenAnswer { it.arguments[0] }
        whenever(dessertRepository.save(any())).thenAnswer { it.arguments[0] }
        whenever(menuRepository.findById(any())).thenReturn(Optional.of(existingDessert.menu!!))
        whenever(menuRepository.save(any())).thenAnswer { it.arguments[0] }
        whenever(menuRepository.saveAndFlush(any())).thenAnswer { it.arguments[0] }

        val result = dessertService.addOrUpdateDessert(dto, vendorDetails)

        assertThat(result.name).isEqualTo("New Name")
    }

    @Test
    fun `addOrUpdateDessert throws RequestedEntityNotFoundException when updating non-existent dessert`() {
        val dto = makeCreateDto("Cookie").copy(id = dessertId)
        whenever(vendorRepository.findById(vendorId)).thenReturn(Optional.of(vendor))
        whenever(dessertRepository.findByIdAndVendorIdAndIsActiveTrue(dessertId, vendorId))
            .thenReturn(null)

        assertThatThrownBy { dessertService.addOrUpdateDessert(dto, vendorDetails) }
            .isInstanceOf(RequestedEntityNotFoundException::class.java)
    }

    // helpers

    private fun makeDessert(id: UUID, name: String): Dessert {
        return Dessert().apply {
            this.id = id
            this.name = name
            this.vendor = this@DessertServiceTest.vendor
            this.isActive = true
            this.availability = Availability.AVAILABLE
            this.description = "A delicious dessert"
            this.dessertType = DessertType.CAKE
            this.deliverable = false
            this.deliveryFee = 0L
            this.preparationTime = 10
            this.dessertOrderTypes = mutableSetOf(OrderType.DINE_IN)
            this.availableDays = mutableSetOf()
            this.dessertFlavours = mutableListOf()
            this.dessertImages = mutableSetOf()
            this.dessertDiscounts = mutableSetOf()
        }
    }

    private fun makeCreateDto(name: String): DessertDto {
        val sizeDto = DessertFlavourSizeDto(
            id = null,
            size = "MEDIUM",
            sizeName = "Regular",
            availability = "AVAILABLE",
            price = 600L
        )
        val flavourDto = DessertFlavourDto(
            id = null,
            flavourName = "Chocolate",
            description = null,
            availability = "AVAILABLE",
            flavourSizes = listOf(sizeDto),
            availableDays = setOf("MONDAY")
        )
        return DessertDto(
            id = null,
            dessertName = name,
            description = "A delicious dessert",
            dessertType = "CAKE",
            availability = "AVAILABLE",
            readyAsFrom = null,
            preparationTime = 10,
            deliverable = false,
            dailyDeliveryQuantity = null,
            deliveryFee = 0L,
            dessertFlavours = listOf(flavourDto),
            orderTypes = setOf("DINE_IN"),
            availableDays = setOf("MONDAY"),
            dessertImages = setOf(
                ImageDto(imageUrl = "img1.jpg"),
                ImageDto(imageUrl = "img2.jpg")
            ),
            discounts = emptySet()
        )
    }
}

