package com.qinet.feastique.service.consumables

import com.qinet.feastique.exception.DuplicateFoundException
import com.qinet.feastique.exception.RequestedEntityNotFoundException
import com.qinet.feastique.exception.UserNotFoundException
import com.qinet.feastique.model.dto.HandheldAvailabilityDto
import com.qinet.feastique.model.dto.ImageDto
import com.qinet.feastique.model.dto.SizeAvailabilityDto
import com.qinet.feastique.model.dto.consumables.FillingDto
import com.qinet.feastique.model.dto.consumables.HandheldDto
import com.qinet.feastique.model.dto.consumables.HandheldSizeDto
import com.qinet.feastique.model.entity.consumables.handheld.Handheld
import com.qinet.feastique.model.entity.menu.Menu
import com.qinet.feastique.model.entity.size.HandheldSize
import com.qinet.feastique.model.entity.user.Vendor
import com.qinet.feastique.model.enums.Availability
import com.qinet.feastique.model.enums.HandHeldType
import com.qinet.feastique.model.enums.OrderType
import com.qinet.feastique.model.enums.Size
import com.qinet.feastique.repository.bookmark.HandheldBookmarkRepository
import com.qinet.feastique.repository.consumables.filling.FillingRepository
import com.qinet.feastique.repository.consumables.handheld.HandheldRepository
import com.qinet.feastique.repository.discount.DiscountRepository
import com.qinet.feastique.repository.like.HandheldLikeRepository
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

class HandheldServiceTest {

    private val handheldRepository: HandheldRepository = mock()
    private val vendorRepository: VendorRepository = mock()
    private val securityUtility: SecurityUtility = mock()
    private val duplicateUtility: DuplicateUtility = mock()
    private val menuRepository: MenuRepository = mock()
    private val fillingRepository: FillingRepository = mock()
    private val discountRepository: DiscountRepository = mock()
    private val handheldLikeRepository: HandheldLikeRepository = mock()
    private val cursorEncoder: CursorEncoder = mock()
    private val handheldBookmarkRepository: HandheldBookmarkRepository = mock()

    private val handheldService = HandheldService(
        handheldRepository,
        vendorRepository,
        securityUtility,
        duplicateUtility,
        menuRepository,
        fillingRepository,
        discountRepository,
        handheldLikeRepository,
        cursorEncoder,
        handheldBookmarkRepository
    )

    private val vendorId: UUID = UUID.randomUUID()
    private val handheldId: UUID = UUID.randomUUID()
    private val customerId: UUID = UUID.randomUUID()

    // Initialized at class level so makeHandheld can reference it before @BeforeEach
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

    // --- getHandheld (vendor role) ---

    @Test
    fun `getHandheld returns response for vendor when handheld belongs to them`() {
        val handheld = makeHandheld(handheldId, "Shawarma")
        whenever(securityUtility.getRole(vendorDetails)).thenReturn("VENDOR")
        whenever(handheldRepository.findByIdAndVendorIdAndIsActiveTrue(handheldId, vendorId))
            .thenReturn(handheld)

        val result = handheldService.getHandheld(handheldId, vendorDetails)

        assertThat(result.id).isEqualTo(handheldId)
        assertThat(result.name).isEqualTo("Shawarma")
    }

    @Test
    fun `getHandheld throws RequestedEntityNotFoundException for vendor when handheld not found`() {
        whenever(securityUtility.getRole(vendorDetails)).thenReturn("VENDOR")
        whenever(handheldRepository.findByIdAndVendorIdAndIsActiveTrue(handheldId, vendorId))
            .thenReturn(null)

        assertThatThrownBy { handheldService.getHandheld(handheldId, vendorDetails) }
            .isInstanceOf(RequestedEntityNotFoundException::class.java)
    }

    // --- getHandheld (customer role) ---

    @Test
    fun `getHandheld returns liked and bookmarked true for customer when both exist`() {
        val handheld = makeHandheld(handheldId, "Other")
        whenever(securityUtility.getRole(customerDetails)).thenReturn("CUSTOMER")
        whenever(handheldLikeRepository.existsByHandheldIdAndCustomerId(handheldId, customerId)).thenReturn(true)
        whenever(handheldBookmarkRepository.existsByHandheldIdAndCustomerId(handheldId, customerId)).thenReturn(true)
        whenever(handheldRepository.findById(handheldId)).thenReturn(Optional.of(handheld))

        val result = handheldService.getHandheld(handheldId, customerDetails)

        assertThat(result.likedByCurrentUser).isTrue()
        assertThat(result.bookmarkedByCurrentUser).isTrue()
    }

    @Test
    fun `getHandheld returns liked false and bookmarked false for customer when neither exist`() {
        val handheld = makeHandheld(handheldId, "Wrap")
        whenever(securityUtility.getRole(customerDetails)).thenReturn("CUSTOMER")
        whenever(handheldLikeRepository.existsByHandheldIdAndCustomerId(handheldId, customerId)).thenReturn(false)
        whenever(handheldBookmarkRepository.existsByHandheldIdAndCustomerId(handheldId, customerId)).thenReturn(false)
        whenever(handheldRepository.findById(handheldId)).thenReturn(Optional.of(handheld))

        val result = handheldService.getHandheld(handheldId, customerDetails)

        assertThat(result.likedByCurrentUser).isFalse()
        assertThat(result.bookmarkedByCurrentUser).isFalse()
    }

    @Test
    fun `getHandheld throws RequestedEntityNotFoundException for customer when handheld not found`() {
        whenever(securityUtility.getRole(customerDetails)).thenReturn("CUSTOMER")
        whenever(handheldLikeRepository.existsByHandheldIdAndCustomerId(handheldId, customerId)).thenReturn(false)
        whenever(handheldBookmarkRepository.existsByHandheldIdAndCustomerId(handheldId, customerId)).thenReturn(false)
        whenever(handheldRepository.findById(handheldId)).thenReturn(Optional.empty())

        assertThatThrownBy { handheldService.getHandheld(handheldId, customerDetails) }
            .isInstanceOf(RequestedEntityNotFoundException::class.java)
    }

    // --- getHandheldById ---

    @Test
    fun `getHandheldById returns handheld when found`() {
        val handheld = makeHandheld(handheldId, "Hotdog")
        whenever(handheldRepository.findByIdAndVendorIdAndIsActiveTrue(handheldId, vendorId))
            .thenReturn(handheld)

        val result = handheldService.getHandheldById(handheldId, vendorDetails)

        assertThat(result.id).isEqualTo(handheldId)
        assertThat(result.name).isEqualTo("Hotdog")
    }

    @Test
    fun `getHandheldById throws RequestedEntityNotFoundException when not found`() {
        whenever(handheldRepository.findByIdAndVendorIdAndIsActiveTrue(handheldId, vendorId))
            .thenReturn(null)

        assertThatThrownBy { handheldService.getHandheldById(handheldId, vendorDetails) }
            .isInstanceOf(RequestedEntityNotFoundException::class.java)
    }

    // --- getAllHandhelds ---

    @Test
    fun `getAllHandhelds returns paged responses`() {
        val handheld = makeHandheld(handheldId, "Burrito")
        whenever(handheldRepository.findAllByVendorIdAndIsActiveTrue(eq(vendorId), any<Pageable>()))
            .thenReturn(PageImpl(listOf(handheld)))

        val result = handheldService.getAllHandhelds(vendorDetails, 0, 10)

        assertThat(result.content).hasSize(1)
        assertThat(result.content[0].name).isEqualTo("Burrito")
    }

    @Test
    fun `getAllHandhelds returns empty page when vendor has no handhelds`() {
        whenever(handheldRepository.findAllByVendorIdAndIsActiveTrue(eq(vendorId), any<Pageable>()))
            .thenReturn(PageImpl(emptyList()))

        val result = handheldService.getAllHandhelds(vendorDetails, 0, 10)

        assertThat(result.content).isEmpty()
    }

    // --- scrollHandhelds ---

    @Test
    fun `scrollHandhelds returns empty window when no handhelds exist`() {
        val emptyWindow = Window.from(emptyList<Handheld>()) { ScrollPosition.offset(it.toLong()) }
        whenever(securityUtility.getSingleRole(vendorDetails)).thenReturn("VENDOR")
        whenever(handheldRepository.findAllByVendorIdAndIsActiveTrue(
            eq(vendorId), any<ScrollPosition>(), any<Sort>(), any<Limit>()
        )).thenReturn(emptyWindow)

        val result = handheldService.scrollHandhelds(vendorId, null, 10, vendorDetails)

        assertThat(result.content).isEmpty()
        assertThat(result.hasNext).isFalse()
        assertThat(result.nextCursor).isNull()
    }

    @Test
    fun `scrollHandhelds returns mapped handheld responses for vendor`() {
        val handheld = makeHandheld(handheldId, "Taco")
        val window = Window.from(listOf(handheld)) { ScrollPosition.offset(it.toLong()) }
        whenever(securityUtility.getSingleRole(vendorDetails)).thenReturn("VENDOR")
        whenever(handheldRepository.findAllByVendorIdAndIsActiveTrue(
            eq(vendorId), any<ScrollPosition>(), any<Sort>(), any<Limit>()
        )).thenReturn(window)
        whenever(cursorEncoder.encodeOffset(any())).thenReturn("dummyCursor")

        val result = handheldService.scrollHandhelds(vendorId, null, 10, vendorDetails)

        assertThat(result.content).hasSize(1)
        assertThat(result.content[0].name).isEqualTo("Taco")
    }

    @Test
    fun `scrollHandhelds resolves like and bookmark status for customer`() {
        val handheld = makeHandheld(handheldId, "Gyro")
        val window = Window.from(listOf(handheld)) { ScrollPosition.offset(it.toLong()) }
        whenever(securityUtility.getSingleRole(customerDetails)).thenReturn("CUSTOMER")
        whenever(handheldRepository.findAllByVendorIdAndIsActiveTrue(
            eq(vendorId), any<ScrollPosition>(), any<Sort>(), any<Limit>()
        )).thenReturn(window)
        whenever(handheldLikeRepository.findAllByCustomerIdAndHandheldIdIn(eq(customerId), any()))
            .thenReturn(emptyList())
        whenever(handheldBookmarkRepository.findAllByCustomerIdAndHandheldIdIn(eq(customerId), any()))
            .thenReturn(emptyList())
        whenever(cursorEncoder.encodeOffset(any())).thenReturn("dummyCursor")

        val result = handheldService.scrollHandhelds(vendorId, null, 10, customerDetails)

        assertThat(result.content).hasSize(1)
        assertThat(result.content[0].likedByCurrentUser).isFalse()
        assertThat(result.content[0].bookmarkedByCurrentUser).isFalse()
    }

    @Test
    fun `scrollHandhelds with numeric cursor calls repository`() {
        val window = Window.from(emptyList<Handheld>()) { ScrollPosition.offset(it.toLong()) }
        whenever(securityUtility.getSingleRole(vendorDetails)).thenReturn("VENDOR")
        whenever(handheldRepository.findAllByVendorIdAndIsActiveTrue(
            eq(vendorId), any<ScrollPosition>(), any<Sort>(), any<Limit>()
        )).thenReturn(window)

        handheldService.scrollHandhelds(vendorId, "5", 10, vendorDetails)

        verify(handheldRepository).findAllByVendorIdAndIsActiveTrue(
            eq(vendorId), any<ScrollPosition>(), any<Sort>(), any<Limit>()
        )
    }

    // --- deleteHandheld ---

    @Test
    fun `deleteHandheld soft-deletes by setting isActive to false`() {
        val handheld = makeHandheld(handheldId, "Kebab")
        whenever(handheldRepository.findByIdAndVendorIdAndIsActiveTrue(handheldId, vendorId))
            .thenReturn(handheld)
        whenever(handheldRepository.saveAndFlush(handheld)).thenReturn(handheld)

        handheldService.deleteHandheld(handheldId, vendorDetails)

        assertThat(handheld.isActive).isFalse()
        verify(handheldRepository).saveAndFlush(handheld)
    }

    @Test
    fun `deleteHandheld throws when handheld not found`() {
        whenever(handheldRepository.findByIdAndVendorIdAndIsActiveTrue(handheldId, vendorId))
            .thenReturn(null)

        assertThatThrownBy { handheldService.deleteHandheld(handheldId, vendorDetails) }
            .isInstanceOf(RequestedEntityNotFoundException::class.java)

        verify(handheldRepository, never()).saveAndFlush(any())
    }

    // --- changeHandheldAvailability ---

    @Test
    fun `changeHandheldAvailability updates handheld availability`() {
        val sizeId = UUID.randomUUID()
        val handheldSize = HandheldSize().apply {
            id = sizeId
            size = Size.MEDIUM
            name = "Medium"
            price = 1500L
            availability = Availability.AVAILABLE
        }
        val handheld = makeHandheld(handheldId, "Wrap")
        handheld.handheldSizes = mutableSetOf(handheldSize)
        handheldSize.handheld = handheld
        handheld.availability = Availability.UNAVAILABLE

        val sizeDto = SizeAvailabilityDto(sizeId = sizeId, availability = "AVAILABLE")
        val dto = HandheldAvailabilityDto(
            id = handheldId,
            availability = "AVAILABLE",
            handheldSizes = setOf(sizeDto)
        )
        whenever(handheldRepository.findByIdAndVendorIdAndIsActiveTrue(handheldId, vendorId))
            .thenReturn(handheld)
        whenever(handheldRepository.saveAndFlush(handheld)).thenReturn(handheld)

        handheldService.changeHandheldAvailability(dto, handheldId, vendorDetails)

        assertThat(handheld.availability).isEqualTo(Availability.AVAILABLE)
    }

    @Test
    fun `changeHandheldAvailability does not change availability when value is unchanged`() {
        val sizeId = UUID.randomUUID()
        val handheldSize = HandheldSize().apply {
            id = sizeId
            size = Size.MEDIUM
            name = "Medium"
            price = 1500L
            availability = Availability.AVAILABLE
        }
        val handheld = makeHandheld(handheldId, "Wrap")
        handheld.handheldSizes = mutableSetOf(handheldSize)
        handheldSize.handheld = handheld
        handheld.availability = Availability.AVAILABLE

        val sizeDto = SizeAvailabilityDto(sizeId = sizeId, availability = "AVAILABLE")
        val dto = HandheldAvailabilityDto(
            id = handheldId,
            availability = "AVAILABLE",
            handheldSizes = setOf(sizeDto)
        )
        whenever(handheldRepository.findByIdAndVendorIdAndIsActiveTrue(handheldId, vendorId))
            .thenReturn(handheld)
        whenever(handheldRepository.saveAndFlush(handheld)).thenReturn(handheld)

        handheldService.changeHandheldAvailability(dto, handheldId, vendorDetails)

        assertThat(handheld.availability).isEqualTo(Availability.AVAILABLE)
    }

    @Test
    fun `changeHandheldAvailability updates handheld size availability`() {
        val sizeId = UUID.randomUUID()
        val handheldSize = HandheldSize().apply {
            id = sizeId
            size = Size.LARGE
            name = "Large"
            price = 2000L
            availability = Availability.AVAILABLE
        }
        val handheld = makeHandheld(handheldId, "Sandwich")
        handheld.handheldSizes = mutableSetOf(handheldSize)
        handheldSize.handheld = handheld

        val sizeDto = SizeAvailabilityDto(sizeId = sizeId, availability = "UNAVAILABLE")
        val dto = HandheldAvailabilityDto(
            id = handheldId,
            availability = "AVAILABLE",
            handheldSizes = setOf(sizeDto)
        )
        whenever(handheldRepository.findByIdAndVendorIdAndIsActiveTrue(handheldId, vendorId))
            .thenReturn(handheld)
        whenever(handheldRepository.saveAndFlush(handheld)).thenReturn(handheld)

        handheldService.changeHandheldAvailability(dto, handheldId, vendorDetails)

        assertThat(handheldSize.availability).isEqualTo(Availability.UNAVAILABLE)
    }

    @Test
    fun `changeHandheldAvailability throws IllegalArgumentException when handheld sizes are null`() {
        val handheld = makeHandheld(handheldId, "Wrap")
        val dto = HandheldAvailabilityDto(
            id = handheldId,
            availability = "AVAILABLE",
            handheldSizes = null
        )
        whenever(handheldRepository.findByIdAndVendorIdAndIsActiveTrue(handheldId, vendorId))
            .thenReturn(handheld)

        assertThatThrownBy { handheldService.changeHandheldAvailability(dto, handheldId, vendorDetails) }
            .isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun `changeHandheldAvailability throws IllegalArgumentException when handheld sizes are empty`() {
        val handheld = makeHandheld(handheldId, "Wrap")
        val dto = HandheldAvailabilityDto(
            id = handheldId,
            availability = "AVAILABLE",
            handheldSizes = emptySet()
        )
        whenever(handheldRepository.findByIdAndVendorIdAndIsActiveTrue(handheldId, vendorId))
            .thenReturn(handheld)

        assertThatThrownBy { handheldService.changeHandheldAvailability(dto, handheldId, vendorDetails) }
            .isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun `changeHandheldAvailability throws when handheld not found`() {
        val dto = HandheldAvailabilityDto(
            id = handheldId,
            availability = "AVAILABLE",
            handheldSizes = setOf(SizeAvailabilityDto(sizeId = UUID.randomUUID(), availability = "AVAILABLE"))
        )
        whenever(handheldRepository.findByIdAndVendorIdAndIsActiveTrue(handheldId, vendorId))
            .thenReturn(null)

        assertThatThrownBy { handheldService.changeHandheldAvailability(dto, handheldId, vendorDetails) }
            .isInstanceOf(RequestedEntityNotFoundException::class.java)
    }

    // --- addOrUpdateHandheld (create path) ---

    @Test
    fun `addOrUpdateHandheld creates new handheld successfully`() {
        val dto = makeCreateDto("Shawarma Roll")
        whenever(vendorRepository.findById(vendorId)).thenReturn(Optional.of(vendor))
        whenever(duplicateUtility.isDuplicateHandheldFound("Shawarma Roll", vendorId)).thenReturn(false)
        whenever(handheldRepository.findTopOrderByFoodNumberDescWithLock()).thenReturn(emptyList())
        whenever(handheldRepository.saveAndFlush(any())).thenAnswer { it.arguments[0] }
        whenever(fillingRepository.saveAndFlush(any())).thenAnswer { it.arguments[0] }
        whenever(menuRepository.saveAndFlush(any())).thenAnswer { it.arguments[0] }

        val result = handheldService.addOrUpdateHandheld(dto, vendorDetails)

        assertThat(result.name).isEqualTo("Shawarma Roll")
        verify(handheldRepository, atLeastOnce()).saveAndFlush(any())
    }

    @Test
    fun `addOrUpdateHandheld throws DuplicateFoundException when name already exists`() {
        val dto = makeCreateDto("Shawarma Roll")
        whenever(vendorRepository.findById(vendorId)).thenReturn(Optional.of(vendor))
        whenever(duplicateUtility.isDuplicateHandheldFound("Shawarma Roll", vendorId)).thenReturn(true)

        assertThatThrownBy { handheldService.addOrUpdateHandheld(dto, vendorDetails) }
            .isInstanceOf(DuplicateFoundException::class.java)

        verify(handheldRepository, never()).saveAndFlush(any())
    }

    @Test
    fun `addOrUpdateHandheld throws UserNotFoundException when vendor not found`() {
        val dto = makeCreateDto("Shawarma Roll")
        whenever(vendorRepository.findById(vendorId)).thenReturn(Optional.empty())

        assertThatThrownBy { handheldService.addOrUpdateHandheld(dto, vendorDetails) }
            .isInstanceOf(UserNotFoundException::class.java)
    }

    @Test
    fun `addOrUpdateHandheld throws IllegalArgumentException when no order types provided`() {
        val dto = makeCreateDto("Shawarma Roll").copy(orderTypes = emptySet())
        whenever(vendorRepository.findById(vendorId)).thenReturn(Optional.of(vendor))
        whenever(duplicateUtility.isDuplicateHandheldFound("Shawarma Roll", vendorId)).thenReturn(false)
        whenever(handheldRepository.findTopOrderByFoodNumberDescWithLock()).thenReturn(emptyList())
        whenever(handheldRepository.saveAndFlush(any())).thenAnswer { it.arguments[0] }
        whenever(fillingRepository.saveAndFlush(any())).thenAnswer { it.arguments[0] }

        assertThatThrownBy { handheldService.addOrUpdateHandheld(dto, vendorDetails) }
            .isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun `addOrUpdateHandheld throws IllegalArgumentException when no available days provided`() {
        val dto = makeCreateDto("Shawarma Roll").copy(availableDays = emptySet())
        whenever(vendorRepository.findById(vendorId)).thenReturn(Optional.of(vendor))
        whenever(duplicateUtility.isDuplicateHandheldFound("Shawarma Roll", vendorId)).thenReturn(false)
        whenever(handheldRepository.findTopOrderByFoodNumberDescWithLock()).thenReturn(emptyList())
        whenever(handheldRepository.saveAndFlush(any())).thenAnswer { it.arguments[0] }
        whenever(fillingRepository.saveAndFlush(any())).thenAnswer { it.arguments[0] }

        assertThatThrownBy { handheldService.addOrUpdateHandheld(dto, vendorDetails) }
            .isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun `addOrUpdateHandheld throws IllegalArgumentException when no sizes provided`() {
        val dto = makeCreateDto("Shawarma Roll").copy(handheldSizes = emptySet())
        whenever(vendorRepository.findById(vendorId)).thenReturn(Optional.of(vendor))
        whenever(duplicateUtility.isDuplicateHandheldFound("Shawarma Roll", vendorId)).thenReturn(false)
        whenever(handheldRepository.findTopOrderByFoodNumberDescWithLock()).thenReturn(emptyList())
        whenever(handheldRepository.saveAndFlush(any())).thenAnswer { it.arguments[0] }
        whenever(fillingRepository.saveAndFlush(any())).thenAnswer { it.arguments[0] }

        assertThatThrownBy { handheldService.addOrUpdateHandheld(dto, vendorDetails) }
            .isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun `addOrUpdateHandheld throws IllegalArgumentException when fewer than two images provided`() {
        val dto = makeCreateDto("Shawarma Roll").copy(
            handheldImages = setOf(ImageDto(imageUrl = "img1.jpg"))
        )
        whenever(vendorRepository.findById(vendorId)).thenReturn(Optional.of(vendor))
        whenever(duplicateUtility.isDuplicateHandheldFound("Shawarma Roll", vendorId)).thenReturn(false)
        whenever(handheldRepository.findTopOrderByFoodNumberDescWithLock()).thenReturn(emptyList())
        whenever(handheldRepository.saveAndFlush(any())).thenAnswer { it.arguments[0] }
        whenever(fillingRepository.saveAndFlush(any())).thenAnswer { it.arguments[0] }

        assertThatThrownBy { handheldService.addOrUpdateHandheld(dto, vendorDetails) }
            .isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun `addOrUpdateHandheld throws IllegalArgumentException when no images provided`() {
        val dto = makeCreateDto("Shawarma Roll").copy(handheldImages = emptySet())
        whenever(vendorRepository.findById(vendorId)).thenReturn(Optional.of(vendor))
        whenever(duplicateUtility.isDuplicateHandheldFound("Shawarma Roll", vendorId)).thenReturn(false)
        whenever(handheldRepository.findTopOrderByFoodNumberDescWithLock()).thenReturn(emptyList())
        whenever(handheldRepository.saveAndFlush(any())).thenAnswer { it.arguments[0] }
        whenever(fillingRepository.saveAndFlush(any())).thenAnswer { it.arguments[0] }

        assertThatThrownBy { handheldService.addOrUpdateHandheld(dto, vendorDetails) }
            .isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun `addOrUpdateHandheld generates sequential handheld number on create`() {
        val dto = makeCreateDto("Club Sandwich")
        val lastHandheld = makeHandheld(UUID.randomUUID(), "Previous").apply { handheldNumber = "HD-00001" }

        whenever(vendorRepository.findById(vendorId)).thenReturn(Optional.of(vendor))
        whenever(duplicateUtility.isDuplicateHandheldFound("Club Sandwich", vendorId)).thenReturn(false)
        whenever(handheldRepository.findTopOrderByFoodNumberDescWithLock()).thenReturn(listOf(lastHandheld))
        whenever(handheldRepository.saveAndFlush(any())).thenAnswer { it.arguments[0] }
        whenever(fillingRepository.saveAndFlush(any())).thenAnswer { it.arguments[0] }
        whenever(menuRepository.saveAndFlush(any())).thenAnswer { it.arguments[0] }

        handheldService.addOrUpdateHandheld(dto, vendorDetails)

        val captor = argumentCaptor<Handheld>()
        verify(handheldRepository, atLeastOnce()).saveAndFlush(captor.capture())
        val firstSaved = captor.allValues.first()
        assertThat(firstSaved.handheldNumber).isEqualTo("HD-00002")
    }

    // --- addOrUpdateHandheld (update path) ---

    @Test
    fun `addOrUpdateHandheld updates existing handheld when id provided`() {
        val existingHandheld = makeHandheld(handheldId, "Old Name")
        // Wire menu separately to avoid nested apply receiver confusion
        val menu = Menu().also { it.handheld = existingHandheld }
        existingHandheld.menu = menu

        val dto = makeCreateDto("New Name").copy(id = handheldId)
        whenever(vendorRepository.findById(vendorId)).thenReturn(Optional.of(vendor))
        whenever(handheldRepository.findByIdAndVendorIdAndIsActiveTrue(handheldId, vendorId))
            .thenReturn(existingHandheld)
        whenever(handheldRepository.saveAndFlush(any())).thenAnswer { it.arguments[0] }
        whenever(fillingRepository.saveAndFlush(any())).thenAnswer { it.arguments[0] }
        whenever(menuRepository.findById(any())).thenReturn(Optional.of(menu))
        whenever(menuRepository.saveAndFlush(any())).thenAnswer { it.arguments[0] }

        val result = handheldService.addOrUpdateHandheld(dto, vendorDetails)

        assertThat(result.name).isEqualTo("New Name")
    }

    @Test
    fun `addOrUpdateHandheld throws RequestedEntityNotFoundException when updating non-existent handheld`() {
        val dto = makeCreateDto("Ghost Handheld").copy(id = handheldId)
        whenever(vendorRepository.findById(vendorId)).thenReturn(Optional.of(vendor))
        whenever(handheldRepository.findByIdAndVendorIdAndIsActiveTrue(handheldId, vendorId))
            .thenReturn(null)

        assertThatThrownBy { handheldService.addOrUpdateHandheld(dto, vendorDetails) }
            .isInstanceOf(RequestedEntityNotFoundException::class.java)
    }

    @Test
    fun `addOrUpdateHandheld throws DuplicateFoundException when updated name already exists for vendor`() {
        val existingHandheld = makeHandheld(handheldId, "Old Name")
        val menu = Menu().also { it.handheld = existingHandheld }
        existingHandheld.menu = menu

        val dto = makeCreateDto("Taken Name").copy(id = handheldId)
        whenever(vendorRepository.findById(vendorId)).thenReturn(Optional.of(vendor))
        whenever(handheldRepository.findByIdAndVendorIdAndIsActiveTrue(handheldId, vendorId))
            .thenReturn(existingHandheld)
        whenever(duplicateUtility.isDuplicateHandheldFound("Taken Name", vendorId)).thenReturn(true)

        assertThatThrownBy { handheldService.addOrUpdateHandheld(dto, vendorDetails) }
            .isInstanceOf(DuplicateFoundException::class.java)
    }

    // --- helpers ---

    private fun makeHandheld(id: UUID, name: String): Handheld {
        return Handheld().apply {
            this.id = id
            this.name = name
            this.vendor = this@HandheldServiceTest.vendor
            this.isActive = true
            this.availability = Availability.AVAILABLE
            this.handHeldType = HandHeldType.SANDWICH
            this.description = "A delicious handheld"
            this.deliverable = false
            this.deliveryFee = 0L
            this.preparationTime = 10
            this.quickDelivery = false
            this.orderTypes = mutableSetOf(OrderType.DINE_IN)
            this.availableDays = mutableSetOf()
            this.handheldFillings = mutableSetOf()
            this.handheldImages = mutableSetOf()
            this.handheldDiscounts = mutableSetOf()
            this.handheldSizes = mutableSetOf()
        }
    }

    private fun makeCreateDto(name: String): HandheldDto {
        val sizeDto = HandheldSizeDto(
            id = null,
            size = "MEDIUM",
            sizeName = "Regular",
            price = 1500L,
            availability = "AVAILABLE"
        )
        val fillingDto = FillingDto(
            id = null,
            name = "Chicken",
            description = "Grilled chicken"
        )
        return HandheldDto(
            id = null,
            handheldName = name,
            handheldType = "Other",
            description = "A delicious handheld",
            availability = "AVAILABLE",
            deliverable = false,
            readyAsFrom = null,
            dailyDeliveryQuantity = null,
            preparationTime = 10,
            quickDelivery = false,
            deliveryFee = 0L,
            fillings = setOf(fillingDto),
            handheldSizes = setOf(sizeDto),
            handheldImages = setOf(
                ImageDto(imageUrl = "img1.jpg"),
                ImageDto(imageUrl = "img2.jpg")
            ),
            orderTypes = setOf("DINE_IN"),
            availableDays = setOf("MONDAY"),
            discounts = null
        )
    }
}

