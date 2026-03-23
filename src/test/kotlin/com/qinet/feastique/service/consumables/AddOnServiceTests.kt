package com.qinet.feastique.service.consumables

import com.qinet.feastique.exception.DuplicateFoundException
import com.qinet.feastique.exception.PermissionDeniedException
import com.qinet.feastique.exception.RequestedEntityNotFoundException
import com.qinet.feastique.exception.UserNotFoundException
import com.qinet.feastique.model.dto.consumables.AddOnDto
import com.qinet.feastique.model.entity.consumables.addOn.AddOn
import com.qinet.feastique.model.entity.user.Vendor
import com.qinet.feastique.model.enums.Availability
import com.qinet.feastique.repository.consumables.addOn.AddOnRepository
import com.qinet.feastique.repository.user.VendorRepository
import com.qinet.feastique.security.UserSecurity
import com.qinet.feastique.response.consumables.food.AddOnResponse
import com.qinet.feastique.utility.CursorEncoder
import com.qinet.feastique.utility.DuplicateUtility
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*
import org.springframework.data.domain.*
import java.util.*
import java.util.function.Function

class AddOnServiceTest {

    private val addOnRepository: AddOnRepository = mock()
    private val vendorRepository: VendorRepository = mock()
    private val duplicateUtility: DuplicateUtility = mock()
    private val cursorEncoder: CursorEncoder = mock()

    private val addOnService = AddOnService(addOnRepository, vendorRepository, duplicateUtility, cursorEncoder)

    private val vendorId: UUID = UUID.randomUUID()
    private val addOnId: UUID = UUID.randomUUID()

    private lateinit var vendorDetails: UserSecurity
    private lateinit var vendor: Vendor
    private lateinit var addOn: AddOn

    @BeforeEach
    fun setUp() {
        vendorDetails = mock {
            on { id } doReturn vendorId
        }

        vendor = mock {
            on { id } doReturn vendorId
        }

        addOn = mock {
            on { id } doReturn addOnId
            on { name } doReturn "Extra Sauce"
            on { price } doReturn 500L
            on { availability } doReturn Availability.AVAILABLE
            on { vendor } doReturn vendor
            on { isActive } doReturn true
        }
    }

    // getAddOn tests

    @Test
    fun `getAddOn returns add-on when it belongs to the vendor`() {
        whenever(addOnRepository.findById(addOnId)).thenReturn(Optional.of(addOn))

        val result = addOnService.getAddOn(addOnId, vendorDetails)

        assertThat(result.id).isEqualTo(addOnId)
        assertThat(result.name).isEqualTo("Extra Sauce")
    }

    @Test
    fun `getAddOn throws RequestedEntityNotFoundException when add-on does not exist`() {
        whenever(addOnRepository.findById(addOnId)).thenReturn(Optional.empty())

        assertThatThrownBy { addOnService.getAddOn(addOnId, vendorDetails) }
            .isInstanceOf(RequestedEntityNotFoundException::class.java)
    }

    @Test
    fun `getAddOn throws PermissionDeniedException when add-on belongs to a different vendor`() {
        val otherVendor: Vendor = mock { on { id } doReturn UUID.randomUUID() }
        val foreignAddOn: AddOn = mock {
            on { id } doReturn addOnId
            on { vendor } doReturn otherVendor
        }
        whenever(addOnRepository.findById(addOnId)).thenReturn(Optional.of(foreignAddOn))

        assertThatThrownBy { addOnService.getAddOn(addOnId, vendorDetails) }
            .isInstanceOf(PermissionDeniedException::class.java)
    }

    // getAllAddOns tests

    @Test
    fun `getAllAddOns returns paginated add-ons for the vendor`() {
        whenever(addOnRepository.findAllByVendorIdAndIsActiveTrue(eq(vendorId), any<Pageable>()))
            .thenReturn(PageImpl(listOf(addOn)))

        val result = addOnService.getAllAddOns(vendorDetails, 0, 10)

        assertThat(result.content).hasSize(1)
        assertThat(result.content[0].addOnName).isEqualTo("Extra Sauce")
        assertThat(result.content[0].price).isEqualTo(500L)
    }

    @Test
    fun `getAllAddOns returns empty page when vendor has no add-ons`() {
        whenever(addOnRepository.findAllByVendorIdAndIsActiveTrue(eq(vendorId), any<Pageable>()))
            .thenReturn(PageImpl(emptyList()))

        val result = addOnService.getAllAddOns(vendorDetails, 0, 10)

        assertThat(result.content).isEmpty()
    }

    // deleteAddOn tests

    @Test
    fun `deleteAddOn soft-deletes add-on by setting isActive to false`() {
        val realAddOn = AddOn()
        realAddOn.id = addOnId
        realAddOn.vendor = vendor
        whenever(addOnRepository.findById(addOnId)).thenReturn(Optional.of(realAddOn))
        whenever(addOnRepository.save(realAddOn)).thenReturn(realAddOn)

        addOnService.deleteAddOn(addOnId, vendorDetails)

        assertThat(realAddOn.isActive).isFalse()
        verify(addOnRepository).save(realAddOn)
    }

    @Test
    fun `deleteAddOn throws PermissionDeniedException when add-on belongs to a different vendor`() {
        val otherVendor: Vendor = mock { on { id } doReturn UUID.randomUUID() }
        val foreignAddOn: AddOn = mock {
            on { id } doReturn addOnId
            on { vendor } doReturn otherVendor
        }
        whenever(addOnRepository.findById(addOnId)).thenReturn(Optional.of(foreignAddOn))

        assertThatThrownBy { addOnService.deleteAddOn(addOnId, vendorDetails) }
            .isInstanceOf(PermissionDeniedException::class.java)
    }

    // addOrUpdateAddOn - create path

    @Test
    fun `addOrUpdateAddOn creates a new add-on successfully`() {
        val dto = AddOnDto(id = null, addOnName = "Ketchup", price = 200L, availability = "AVAILABLE")

        whenever(vendorRepository.findById(vendorId)).thenReturn(Optional.of(vendor))
        whenever(duplicateUtility.isDuplicateAddOnFound("Ketchup", vendorId)).thenReturn(false)
        whenever(addOnRepository.save(any())).thenAnswer { it.arguments[0] }
        whenever(vendorRepository.save(vendor)).thenReturn(vendor)

        val result = addOnService.addOrUpdateAddOn(dto, vendorDetails)

        assertThat(result.name).isEqualTo("Ketchup")
        assertThat(result.price).isEqualTo(200L)
        verify(addOnRepository).save(any())
    }

    @Test
    fun `addOrUpdateAddOn throws DuplicateFoundException when add-on name already exists for the vendor`() {
        val dto = AddOnDto(id = null, addOnName = "Ketchup", price = 200L, availability = "AVAILABLE")

        whenever(vendorRepository.findById(vendorId)).thenReturn(Optional.of(vendor))
        whenever(duplicateUtility.isDuplicateAddOnFound("Ketchup", vendorId)).thenReturn(true)

        assertThatThrownBy { addOnService.addOrUpdateAddOn(dto, vendorDetails) }
            .isInstanceOf(DuplicateFoundException::class.java)
    }

    @Test
    fun `addOrUpdateAddOn throws UserNotFoundException when vendor does not exist`() {
        val dto = AddOnDto(id = null, addOnName = "Ketchup", price = 200L, availability = "AVAILABLE")

        whenever(vendorRepository.findById(vendorId)).thenReturn(Optional.empty())

        assertThatThrownBy { addOnService.addOrUpdateAddOn(dto, vendorDetails) }
            .isInstanceOf(UserNotFoundException::class.java)
    }

    // addOrUpdateAddOn - update path

    @Test
    fun `addOrUpdateAddOn updates an existing add-on successfully`() {
        val dto = AddOnDto(id = addOnId, addOnName = "Spicy Sauce", price = 750L, availability = "AVAILABLE")
        val realAddOn = AddOn().also {
            it.id = addOnId
            it.vendor = vendor
        }

        whenever(vendorRepository.findById(vendorId)).thenReturn(Optional.of(vendor))
        whenever(addOnRepository.findById(addOnId)).thenReturn(Optional.of(realAddOn))
        whenever(addOnRepository.save(any())).thenAnswer { it.arguments[0] }
        whenever(vendorRepository.save(vendor)).thenReturn(vendor)

        val result = addOnService.addOrUpdateAddOn(dto, vendorDetails)

        assertThat(result.name).isEqualTo("Spicy Sauce")
        assertThat(result.price).isEqualTo(750L)
    }

    @Test
    fun `addOrUpdateAddOn throws PermissionDeniedException when updating an add-on belonging to another vendor`() {
        val otherVendor: Vendor = mock { on { id } doReturn UUID.randomUUID() }
        val foreignAddOn: AddOn = mock {
            on { id } doReturn addOnId
            on { vendor } doReturn otherVendor
        }
        val dto = AddOnDto(id = addOnId, addOnName = "Spicy Sauce", price = 750L, availability = "AVAILABLE")

        whenever(vendorRepository.findById(vendorId)).thenReturn(Optional.of(vendor))
        whenever(addOnRepository.findById(addOnId)).thenReturn(Optional.of(foreignAddOn))

        assertThatThrownBy { addOnService.addOrUpdateAddOn(dto, vendorDetails) }
            .isInstanceOf(PermissionDeniedException::class.java)
    }

    @Test
    fun `addOrUpdateAddOn throws RequestedEntityNotFoundException when updating a non-existent add-on`() {
        val dto = AddOnDto(id = addOnId, addOnName = "Ghost Sauce", price = 100L, availability = "AVAILABLE")

        whenever(vendorRepository.findById(vendorId)).thenReturn(Optional.of(vendor))
        whenever(addOnRepository.findById(addOnId)).thenReturn(Optional.empty())

        assertThatThrownBy { addOnService.addOrUpdateAddOn(dto, vendorDetails) }
            .isInstanceOf(RequestedEntityNotFoundException::class.java)
    }

    // scrollHandhelds (scroll add-ons) tests

    @Test
    fun `scrollHandhelds calls repository with correct vendorId and page size`() {
        val mappedWindow: Window<AddOnResponse> = mock()
        doAnswer { emptyList<AddOnResponse>() }.`when`(mappedWindow).content
        doReturn(false).`when`(mappedWindow).hasNext()

        val window: Window<AddOn> = mock()
        doAnswer { mappedWindow }.`when`(window).map<AddOnResponse>(any<Function<AddOn, AddOnResponse>>())

        whenever(addOnRepository.findAllByVendorIdAndIsActiveTrue(eq(vendorId), any<ScrollPosition>(), any<Sort>(), any<Limit>()))
            .thenReturn(window)

        addOnService.scrollHandhelds(vendorDetails, null, 10)

        verify(addOnRepository).findAllByVendorIdAndIsActiveTrue(eq(vendorId), any<ScrollPosition>(), any<Sort>(), any<Limit>())
    }

    @Test
    fun `scrollHandhelds calls repository with offset scroll position when cursor is provided`() {
        val mappedWindow: Window<AddOnResponse> = mock()
        doAnswer { emptyList<AddOnResponse>() }.`when`(mappedWindow).content
        doReturn(false).`when`(mappedWindow).hasNext()

        val window: Window<AddOn> = mock()
        doAnswer { mappedWindow }.`when`(window).map<AddOnResponse>(any<Function<AddOn, AddOnResponse>>())

        whenever(addOnRepository.findAllByVendorIdAndIsActiveTrue(eq(vendorId), any<ScrollPosition>(), any<Sort>(), any<Limit>()))
            .thenReturn(window)

        addOnService.scrollHandhelds(vendorDetails, "10", 10)

        verify(addOnRepository).findAllByVendorIdAndIsActiveTrue(eq(vendorId), any<ScrollPosition>(), any<Sort>(), any<Limit>())
    }
}

