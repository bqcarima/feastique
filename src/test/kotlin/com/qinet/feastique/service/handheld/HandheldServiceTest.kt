package com.qinet.feastique.service.handheld

import com.github.f4b6a3.uuid.UuidCreator
import com.qinet.feastique.exception.DuplicateFoundException
import com.qinet.feastique.exception.PermissionDeniedException
import com.qinet.feastique.exception.RequestedEntityNotFoundException
import com.qinet.feastique.exception.UserNotFoundException
import com.qinet.feastique.model.dto.HandheldAvailabilityDto
import com.qinet.feastique.model.dto.SizeAvailabilityDto
import com.qinet.feastique.model.dto.discount.DiscountDto
import com.qinet.feastique.model.dto.consumables.FillingDto
import com.qinet.feastique.model.dto.consumables.HandheldDto
import com.qinet.feastique.model.dto.ImageDto
import com.qinet.feastique.model.dto.consumables.HandheldSizeDto
import com.qinet.feastique.model.entity.Menu
import com.qinet.feastique.model.entity.consumables.filling.Filling
import com.qinet.feastique.model.entity.consumables.handheld.Handheld
import com.qinet.feastique.model.entity.discount.Discount
import com.qinet.feastique.model.entity.size.HandheldSize
import com.qinet.feastique.model.entity.user.Vendor
import com.qinet.feastique.model.enums.Availability
import com.qinet.feastique.model.enums.HandHeldType
import com.qinet.feastique.model.enums.Size
import com.qinet.feastique.repository.menu.MenuRepository
import com.qinet.feastique.repository.consumables.filling.FillingRepository
import com.qinet.feastique.repository.consumables.handheld.HandheldRepository
import com.qinet.feastique.repository.discount.DiscountRepository
import com.qinet.feastique.repository.user.VendorRepository
import com.qinet.feastique.security.UserSecurity
import com.qinet.feastique.service.consumables.HandheldService
import com.qinet.feastique.utility.DuplicateUtility
import com.qinet.feastique.utility.SecurityUtility
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.*
import org.springframework.data.domain.PageImpl
import java.text.SimpleDateFormat
import java.time.LocalTime
import java.util.*
import kotlin.collections.emptyList

// @ExtendWith(MockitoExtension::class)
class HandheldServiceTest {

    private val simpleDateFormat = SimpleDateFormat("dd-MM-yyyy")
    // --- Mocks ---
    private val handheldRepository: HandheldRepository = mock()
    private val vendorRepository: VendorRepository = mock()
    private val securityUtility: SecurityUtility = mock()
    private val duplicateUtility: DuplicateUtility = mock()
    private val menuRepository: MenuRepository = mock()
    private val fillingRepository: FillingRepository = mock()
    private val discountRepository: DiscountRepository = mock()

    private lateinit var service: HandheldService

    // --- Shared fixtures ---
    private val vendorId: UUID = UuidCreator.getTimeOrdered()
    private val handheldId: UUID = UuidCreator.getTimeOrdered()

    private lateinit var vendorDetails: UserSecurity
    private lateinit var vendor: Vendor
    private lateinit var handheld: Handheld

    @BeforeEach
    fun setUp() {
        service = HandheldService(
            handheldRepository,
            vendorRepository,
            securityUtility,
            duplicateUtility,
            menuRepository,
            fillingRepository,
            discountRepository,
            cursorEncoder,
        )

        vendorDetails = mock {
            on { id } doReturn vendorId
        }

        vendor = Vendor().apply {
            id = vendorId
            openingTime = LocalTime.of(8, 0)
        }

        handheld = Handheld().apply {
            id = handheldId
            this.vendor = vendor
            name = "Classic Burger"
            handheldNumber = "HD-00001"
            availability = Availability.AVAILABLE
            handHeldType = HandHeldType.BURGER
            handheldSizes = mutableSetOf()
            handheldFillings = mutableSetOf()
            handheldDiscounts = mutableSetOf()
            handheldImages = mutableSetOf()
            orderTypes = mutableSetOf()
            availableDays = mutableSetOf()
        }
    }

    // saveHandheld
    @Nested
    @DisplayName("saveHandheld")
    inner class SaveHandheld {

        @Test
        fun `saves and returns the handheld`() {
            whenever(handheldRepository.saveAndFlush(handheld)).thenReturn(handheld)

            val result = service.saveHandheld(handheld)

            assertEquals(handheld, result)
            verify(handheldRepository).saveAndFlush(handheld)
        }
    }

    // ---------------------------------------------------------------------------
    // getHandheldById
    // ---------------------------------------------------------------------------
    @Nested
    @DisplayName("getHandheldById")
    inner class GetHandheldById {

        @Test
        fun `returns handheld when vendor owns it`() {
            whenever(securityUtility.getRole(vendorDetails)).thenReturn("VENDOR")
            whenever(handheldRepository.findById(handheldId)).thenReturn(Optional.of(handheld))

            val result = service.getHandheldById(handheldId, vendorDetails)

            assertEquals(handheld, result)
        }

        @Test
        fun `returns handheld for customer regardless of owning vendor`() {
            whenever(securityUtility.getRole(vendorDetails)).thenReturn("CUSTOMER")
            whenever(handheldRepository.findById(handheldId)).thenReturn(Optional.of(handheld))

            val result = service.getHandheldById(handheldId, vendorDetails)

            assertEquals(handheld, result)
        }

        @Test
        fun `throws RequestedEntityNotFoundException when handheld does not exist`() {
            whenever(securityUtility.getRole(vendorDetails)).thenReturn("VENDOR")
            whenever(handheldRepository.findById(handheldId)).thenReturn(Optional.empty())

            assertThrows<RequestedEntityNotFoundException> {
                service.getHandheldById(handheldId, vendorDetails)
            }
        }

        @Test
        fun `throws PermissionDeniedException when vendor does not own the handheld`() {
            val foreignHandheld = Handheld().apply {
                id = handheldId
                vendor = Vendor().apply { id = UUID.randomUUID() }
            }
            whenever(securityUtility.getRole(vendorDetails)).thenReturn("VENDOR")
            whenever(handheldRepository.findById(handheldId)).thenReturn(Optional.of(foreignHandheld))

            assertThrows<PermissionDeniedException> {
                service.getHandheldById(handheldId, vendorDetails)
            }
        }
    }

    // ---------------------------------------------------------------------------
    // getAllHandhelds
    // ---------------------------------------------------------------------------
    @Nested
    @DisplayName("getAllHandhelds")
    inner class GetAllHandhelds {

        @Test
        fun `delegates to repository with correct vendor id and pageable`() {
            whenever(handheldRepository.findAllByVendorId(eq(vendorId), any()))
                .thenReturn(PageImpl(listOf(handheld)))

            service.scrollHandhelds(vendorDetails, 0, 10)

            verify(handheldRepository).findAllByVendorId(eq(vendorId), any())
        }

        @Test
        fun `returns empty page when vendor has no handhelds`() {
            whenever(handheldRepository.findAllByVendorId(eq(vendorId), any()))
                .thenReturn(PageImpl(emptyList()))

            val result = service.scrollHandhelds(vendorDetails, 0, 10)

            assertTrue(result.isEmpty)
        }
    }

    // ---------------------------------------------------------------------------
    // deleteHandheld
    // ---------------------------------------------------------------------------
    @Nested
    @DisplayName("deleteHandheld")
    inner class DeleteHandheld {

        @Test
        fun `deletes handheld when vendor owns it`() {
            whenever(securityUtility.getRole(vendorDetails)).thenReturn("VENDOR")
            whenever(handheldRepository.findById(handheldId)).thenReturn(Optional.of(handheld))

            service.deleteHandheld(handheldId, vendorDetails)

            verify(handheldRepository).delete(handheld)
        }

        @Test
        fun `throws RequestedEntityNotFoundException when handheld does not exist`() {
            whenever(securityUtility.getRole(vendorDetails)).thenReturn("VENDOR")
            whenever(handheldRepository.findById(handheldId)).thenReturn(Optional.empty())

            assertThrows<RequestedEntityNotFoundException> {
                service.deleteHandheld(handheldId, vendorDetails)
            }

            verify(handheldRepository, never()).delete(any<Handheld>())
        }

        @Test
        fun `throws PermissionDeniedException and does not delete when vendor does not own handheld`() {
            val foreignHandheld = Handheld().apply {
                id = handheldId
                vendor = Vendor().apply { id = UUID.randomUUID() }
            }
            whenever(securityUtility.getRole(vendorDetails)).thenReturn("VENDOR")
            whenever(handheldRepository.findById(handheldId)).thenReturn(Optional.of(foreignHandheld))

            assertThrows<PermissionDeniedException> {
                service.deleteHandheld(handheldId, vendorDetails)
            }

            verify(handheldRepository, never()).delete(any<Handheld>())
        }
    }

    // ---------------------------------------------------------------------------
    // toggleAvailability
    // ---------------------------------------------------------------------------
    @Nested
    @DisplayName("toggleAvailability")
    inner class ToggleAvailability {

        @Test
        fun `updates top-level availability when provided`() {
            // HandheldAvailabilityDto(id, availability, handheldSizes)
            val dto = HandheldAvailabilityDto(id = handheldId, availability = "UNAVAILABLE", handheldSizes = null)

            whenever(securityUtility.getRole(vendorDetails)).thenReturn("VENDOR")
            whenever(handheldRepository.findById(handheldId)).thenReturn(Optional.of(handheld))
            whenever(handheldRepository.saveAndFlush(any())).thenAnswer { it.arguments[0] }

            val result = service.toggleAvailability(dto, handheldId, vendorDetails)

            assertEquals(Availability.UNAVAILABLE, result.availability)
            verify(handheldRepository).saveAndFlush(handheld)
        }

        @Test
        fun `updates individual size availability when handheldSizes is provided`() {
            val sizeId = UUID.randomUUID()
            val handheldSize = HandheldSize().apply {
                id = sizeId
                availability = Availability.AVAILABLE
            }
            handheld.handheldSizes = mutableSetOf(handheldSize)

            // SizeAvailabilityDto lives in com.qinet.feastique.model.dto
            val sizeDto = SizeAvailabilityDto(sizeId = sizeId, availability = "UNAVAILABLE")
            val dto = HandheldAvailabilityDto(id = handheldId, availability = null, handheldSizes = setOf(sizeDto))

            whenever(securityUtility.getRole(vendorDetails)).thenReturn("VENDOR")
            whenever(handheldRepository.findById(handheldId)).thenReturn(Optional.of(handheld))
            whenever(handheldRepository.saveAndFlush(any())).thenAnswer { it.arguments[0] }

            service.toggleAvailability(dto, handheldId, vendorDetails)

            assertEquals(Availability.UNAVAILABLE, handheldSize.availability)
        }

        @Test
        fun `throws IllegalArgumentException when handheldSizes set is empty`() {
            val dto = HandheldAvailabilityDto(id = handheldId, availability = null, handheldSizes = emptySet())

            whenever(securityUtility.getRole(vendorDetails)).thenReturn("VENDOR")
            whenever(handheldRepository.findById(handheldId)).thenReturn(Optional.of(handheld))

            assertThrows<IllegalArgumentException> {
                service.toggleAvailability(dto, handheldId, vendorDetails)
            }
        }

        @Test
        fun `throws IllegalArgumentException when sizeId inside dto is null`() {
            val sizeDto = SizeAvailabilityDto(sizeId = null, availability = "UNAVAILABLE")
            val dto = HandheldAvailabilityDto(id = handheldId, availability = null, handheldSizes = setOf(sizeDto))

            whenever(securityUtility.getRole(vendorDetails)).thenReturn("VENDOR")
            whenever(handheldRepository.findById(handheldId)).thenReturn(Optional.of(handheld))

            assertThrows<IllegalArgumentException> {
                service.toggleAvailability(dto, handheldId, vendorDetails)
            }
        }

        @Test
        fun `throws IllegalArgumentException when sizeId does not match any size on the handheld`() {
            handheld.handheldSizes = mutableSetOf()
            val sizeDto = SizeAvailabilityDto(sizeId = UUID.randomUUID(), availability = "UNAVAILABLE")
            val dto = HandheldAvailabilityDto(id = handheldId, availability = null, handheldSizes = setOf(sizeDto))

            whenever(securityUtility.getRole(vendorDetails)).thenReturn("VENDOR")
            whenever(handheldRepository.findById(handheldId)).thenReturn(Optional.of(handheld))

            assertThrows<IllegalArgumentException> {
                service.toggleAvailability(dto, handheldId, vendorDetails)
            }
        }
    }

    // ---------------------------------------------------------------------------
    // addOrUpdateHandheld — CREATE path
    // ---------------------------------------------------------------------------
    @Nested
    @DisplayName("addOrUpdateHandheld — create")
    inner class AddOrUpdateHandheldCreate {

        private fun buildMinimalDto(name: String = "New Burger") = HandheldDto(
            id = null,
            handheldName = name,
            handheldType = "BURGER",
            description = "Tasty",
            availability = "AVAILABLE",
            deliverable = true,
            readyAsFrom = null,
            dailyDeliveryQuantity = null,
            preparationTime = 10,
            quickDelivery = false,
            deliveryFee = 500,
            fillings = setOf(FillingDto(id = null, name = "Chicken", description = null)),
            availableDays = setOf("MONDAY"),
            orderTypes = setOf("DELIVERY"),
            handheldImages = setOf(
                ImageDto(id = null, imageUrl = "http://img1.png"),
                ImageDto(id = null, imageUrl = "http://img2.png"),
            ),
            handheldSizes = setOf(
                HandheldSizeDto(id = null, size = "MEDIUM", sizeName = null, price = 1500, availability = "AVAILABLE")
            ),
            discounts = null,
        )

        @BeforeEach
        fun stubCommonDependencies() {
            whenever(vendorRepository.findById(vendorId)).thenReturn(Optional.of(vendor))
            whenever(duplicateUtility.isDuplicateHandheldFound(any(), eq(vendorId))).thenReturn(false)
            whenever(handheldRepository.findTopOrderByFoodNumberDescWithLock()).thenReturn(emptyList())
            whenever(handheldRepository.saveAndFlush(any())).thenAnswer { it.arguments[0] }
            val filling = Filling().apply { id = UUID.randomUUID(); name = "Chicken" }
            whenever(fillingRepository.saveAndFlush(any())).thenReturn(filling)
            whenever(menuRepository.saveAndFlush(any())).thenAnswer { it.arguments[0] }
        }

        @Test
        fun `creates new handheld and assigns HD-00001 when no prior handhelds exist`() {
            val result = service.addOrUpdateHandheld(buildMinimalDto(), vendorDetails)

            assertEquals("HD-00001", result.handheldNumber)
            assertEquals("New Wrap", result.name)
        }

        @Test
        fun `increments handheld number from the last existing handheld`() {
            val lastHandheld = Handheld().apply { handheldNumber = "HD-00005" }
            whenever(handheldRepository.findTopOrderByFoodNumberDescWithLock())
                .thenReturn(listOf(lastHandheld))

            val result = service.addOrUpdateHandheld(buildMinimalDto(), vendorDetails)

            assertEquals("HD-00006", result.handheldNumber)
        }

        @Test
        fun `throws DuplicateFoundException when handheld name already exists for this vendor`() {
            whenever(duplicateUtility.isDuplicateHandheldFound("New Wrap", vendorId)).thenReturn(true)

            assertThrows<DuplicateFoundException> {
                service.addOrUpdateHandheld(buildMinimalDto("New Wrap"), vendorDetails)
            }
        }

        @Test
        fun `throws UserNotFoundException when vendor does not exist`() {
            whenever(vendorRepository.findById(vendorId)).thenReturn(Optional.empty())

            assertThrows<UserNotFoundException> {
                service.addOrUpdateHandheld(buildMinimalDto(), vendorDetails)
            }
        }

        @Test
        fun `throws IllegalArgumentException when handheld name is null`() {
            assertThrows<IllegalArgumentException> {
                service.addOrUpdateHandheld(buildMinimalDto().copy(handheldName = null), vendorDetails)
            }
        }

        @Test
        fun `throws IllegalArgumentException when deliverable is null`() {
            assertThrows<IllegalArgumentException> {
                service.addOrUpdateHandheld(buildMinimalDto().copy(deliverable = null), vendorDetails)
            }
        }

        @Test
        fun `throws IllegalArgumentException when availableDays is empty`() {
            assertThrows<IllegalArgumentException> {
                service.addOrUpdateHandheld(buildMinimalDto().copy(availableDays = emptySet()), vendorDetails)
            }
        }

        @Test
        fun `throws IllegalArgumentException when orderTypes is empty`() {
            assertThrows<IllegalArgumentException> {
                service.addOrUpdateHandheld(buildMinimalDto().copy(orderTypes = emptySet()), vendorDetails)
            }
        }

        @Test
        fun `throws IllegalArgumentException when fewer than 2 images are provided`() {
            assertThrows<IllegalArgumentException> {
                service.addOrUpdateHandheld(
                    buildMinimalDto().copy(
                        handheldImages = setOf(ImageDto(id = null, imageUrl = "http://img1.png"))
                    ),
                    vendorDetails
                )
            }
        }

        @Test
        fun `throws IllegalArgumentException when no images are provided`() {
            assertThrows<IllegalArgumentException> {
                service.addOrUpdateHandheld(buildMinimalDto().copy(handheldImages = emptySet()), vendorDetails)
            }
        }

        @Test
        fun `throws IllegalArgumentException when handheldSizes is empty`() {
            assertThrows<IllegalArgumentException> {
                service.addOrUpdateHandheld(buildMinimalDto().copy(handheldSizes = emptySet()), vendorDetails)
            }
        }

        @Test
        fun `falls back to vendor opening time when readyAsFrom is not specified`() {
            val result = service.addOrUpdateHandheld(buildMinimalDto(), vendorDetails)

            assertEquals(vendor.openingTime, result.readyAsFrom)
        }

        @Test
        fun `handheld discounts are empty when discounts field is null`() {
            val result = service.addOrUpdateHandheld(buildMinimalDto(), vendorDetails)

            assertTrue(result.handheldDiscounts.isEmpty())
        }
    }

    // ---------------------------------------------------------------------------
    // addOrUpdateHandheld — UPDATE path
    // ---------------------------------------------------------------------------
    @Nested
    @DisplayName("addOrUpdateHandheld — update")
    inner class AddOrUpdateHandheldUpdate {

        private val existingSizeId = UUID.randomUUID()

        private fun existingHandheld() = handheld.apply {
            menu = Menu().apply { id = UUID.randomUUID() }
            handheldSizes = mutableSetOf(
                HandheldSize().apply {
                    id = existingSizeId
                    size = Size.MEDIUM
                    availability = Availability.AVAILABLE
                }
            )
        }

        private fun buildUpdateDto(newName: String = "Classic Burger") = HandheldDto(
            id = handheldId,
            handheldName = newName,
            handheldType = "BURGER",
            description = "Updated",
            availability = "AVAILABLE",
            deliverable = true,
            readyAsFrom = null,
            dailyDeliveryQuantity = null,
            preparationTime = 5,
            quickDelivery = true,
            deliveryFee = 500,
            fillings = setOf(FillingDto(id = null, name = "Cheese", description = null)),
            availableDays = setOf("TUESDAY"),
            orderTypes = setOf("DINE_IN"),
            handheldImages = setOf(
                ImageDto(id = null, imageUrl = "http://img1.png"),
                ImageDto(id = null, imageUrl = "http://img2.png"),
            ),
            handheldSizes = setOf(
                HandheldSizeDto(id = existingSizeId, size = "MEDIUM", sizeName = null, price = 1500, availability = "AVAILABLE")
            ),
            discounts = null,
        )

        @BeforeEach
        fun stubForUpdate() {
            whenever(vendorRepository.findById(vendorId)).thenReturn(Optional.of(vendor))
            whenever(securityUtility.getRole(vendorDetails)).thenReturn("VENDOR")
            whenever(handheldRepository.findById(handheldId)).thenReturn(Optional.of(existingHandheld()))
            whenever(duplicateUtility.isDuplicateHandheldFound(any(), eq(vendorId))).thenReturn(false)
            whenever(handheldRepository.saveAndFlush(any())).thenAnswer { it.arguments[0] }
            val filling = Filling().apply { id = UUID.randomUUID(); name = "Cheese" }
            whenever(fillingRepository.saveAndFlush(any())).thenReturn(filling)
            whenever(menuRepository.findById(any())).thenReturn(Optional.of(Menu()))
            whenever(menuRepository.saveAndFlush(any())).thenAnswer { it.arguments[0] }
        }

        @Test
        fun `updates mutable fields on the existing handheld`() {
            val result = service.addOrUpdateHandheld(buildUpdateDto(newName = "Updated Wrap"), vendorDetails)

            assertEquals("Updated Wrap", result.name)
            assertEquals(true, result.quickDelivery)
        }

        @Test
        fun `does not reassign handheld number on update`() {
            val result = service.addOrUpdateHandheld(buildUpdateDto(), vendorDetails)

            assertEquals("HD-00001", result.handheldNumber)
            verify(handheldRepository, never()).findTopOrderByFoodNumberDescWithLock()
        }

        @Test
        fun `throws DuplicateFoundException when renamed to a name already taken by this vendor`() {
            whenever(duplicateUtility.isDuplicateHandheldFound("Taken Name", vendorId)).thenReturn(true)

            assertThrows<DuplicateFoundException> {
                service.addOrUpdateHandheld(buildUpdateDto(newName = "Taken Name"), vendorDetails)
            }
        }

        @Test
        fun `skips duplicate name check when name is unchanged`() {
            service.addOrUpdateHandheld(buildUpdateDto(newName = "Classic Wrap"), vendorDetails)

            verify(duplicateUtility, never()).isDuplicateHandheldFound(any(), any())
        }

        @Test
        fun `throws RequestedEntityNotFoundException when menu entry is missing`() {
            whenever(menuRepository.findById(any())).thenReturn(Optional.empty())

            assertThrows<RequestedEntityNotFoundException> {
                service.addOrUpdateHandheld(buildUpdateDto(), vendorDetails)
            }
        }
    }

    // ---------------------------------------------------------------------------
    // addOrUpdateHandheld — discount handling
    // ---------------------------------------------------------------------------
    @Nested
    @DisplayName("addOrUpdateHandheld — discounts")
    inner class DiscountHandling {

        private fun buildDtoWithDiscount(discountId: UUID? = null) = HandheldDto(
            id = null,
            handheldName = "Discounted Burger",
            handheldType = "BURGER",
            description = null,
            availability = "AVAILABLE",
            deliverable = true,
            readyAsFrom = null,
            dailyDeliveryQuantity = null,
            preparationTime = 0,
            quickDelivery = false,
            deliveryFee = 500,
            fillings = emptySet(),
            availableDays = setOf("MONDAY"),
            orderTypes = setOf("PICKUP"),
            handheldImages = setOf(
                ImageDto(id = null, imageUrl = "http://a.png"),
                ImageDto(id = null, imageUrl = "http://b.png"),
            ),
            handheldSizes = setOf(
                HandheldSizeDto(id = null, size = "SMALL", sizeName = null, price = 1500, availability = "AVAILABLE")
            ),
            discounts = setOf(
                DiscountDto(
                    id = discountId,
                    discountName = "Summer Sale",
                    percentage = 5,
                    startDate = simpleDateFormat.parse("01-01-2026"),
                    endDate = simpleDateFormat.parse("01-03-2026"),
                )
            ),
        )

        @BeforeEach
        fun stub() {
            whenever(vendorRepository.findById(vendorId)).thenReturn(Optional.of(vendor))
            whenever(duplicateUtility.isDuplicateHandheldFound(any(), eq(vendorId))).thenReturn(false)
            whenever(handheldRepository.findTopOrderByFoodNumberDescWithLock()).thenReturn(emptyList())
            whenever(handheldRepository.saveAndFlush(any())).thenAnswer { it.arguments[0] }
            whenever(menuRepository.saveAndFlush(any())).thenAnswer { it.arguments[0] }
        }

        @Test
        fun `creates and attaches a new discount when no id is provided`() {
            val savedDiscount = Discount().apply { id = UUID.randomUUID(); discountName = "Summer Sale" }
            whenever(discountRepository.save(any())).thenReturn(savedDiscount)

            val result = service.addOrUpdateHandheld(buildDtoWithDiscount(discountId = null), vendorDetails)

            assertEquals(1, result.handheldDiscounts.size)
        }

        @Test
        fun `reuses and updates an existing discount when id is provided`() {
            val existingDiscountId = UUID.randomUUID()
            val existingDiscount = Discount().apply {
                id = existingDiscountId
                vendor = vendor
                discountName = "Old Sale"
                percentage = 5
                startDate = simpleDateFormat.parse("01-01-2026")
                endDate = simpleDateFormat.parse("01-03-2026")
            }
            whenever(discountRepository.findById(existingDiscountId)).thenReturn(Optional.of(existingDiscount))
            whenever(discountRepository.save(any())).thenReturn(existingDiscount)

            val result = service.addOrUpdateHandheld(buildDtoWithDiscount(discountId = existingDiscountId), vendorDetails)

            // discountName is updated to the incoming DTO value
            assertEquals("Summer Sale", result.handheldDiscounts.first().discount.discountName)
        }

        @Test
        fun `throws RequestedEntityNotFoundException when referenced discount id does not exist`() {
            val unknownId = UUID.randomUUID()
            whenever(discountRepository.findById(unknownId)).thenReturn(Optional.empty())

            assertThrows<RequestedEntityNotFoundException> {
                service.addOrUpdateHandheld(buildDtoWithDiscount(discountId = unknownId), vendorDetails)
            }
        }
    }
}

