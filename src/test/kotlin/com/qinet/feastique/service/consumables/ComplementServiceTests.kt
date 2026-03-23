package com.qinet.feastique.service.consumables

import com.qinet.feastique.exception.DuplicateFoundException
import com.qinet.feastique.exception.RequestedEntityNotFoundException
import com.qinet.feastique.exception.UserNotFoundException
import com.qinet.feastique.model.dto.consumables.ComplementDto
import com.qinet.feastique.model.entity.consumables.complement.Complement
import com.qinet.feastique.model.entity.user.Vendor
import com.qinet.feastique.model.enums.Availability
import com.qinet.feastique.repository.consumables.complement.ComplementRepository
import com.qinet.feastique.repository.user.VendorRepository
import com.qinet.feastique.security.UserSecurity
import com.qinet.feastique.utility.CursorEncoder
import com.qinet.feastique.utility.DuplicateUtility
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*
import org.springframework.data.domain.*
import java.util.*

class ComplementServiceTest {

    private val complementRepository: ComplementRepository = mock()
    private val vendorRepository: VendorRepository = mock()
    private val duplicateUtility: DuplicateUtility = mock()
    private val cursorEncoder: CursorEncoder = mock()

    private val complementService = ComplementService(
        complementRepository, vendorRepository, duplicateUtility, cursorEncoder
    )

    private val vendorId: UUID = UUID.randomUUID()
    private val complementId: UUID = UUID.randomUUID()

    // Initialized at class level so makeComplement can reference it before @BeforeEach
    private val vendor: Vendor = Vendor().apply {
        id = vendorId
        username = "testvendor"
        firstName = "John"
        lastName = "Doe"
        chefName = "Chef John"
        password = "encoded"
    }

    private lateinit var vendorDetails: UserSecurity

    @BeforeEach
    fun setUp() {
        vendorDetails = mock {
            on { id } doReturn vendorId
        }
    }

    // getComplement 

    @Test
    fun `getComplement returns complement when found`() {
        val complement = makeComplement(complementId, "Rice", 500L)
        whenever(complementRepository.findByIdAndVendorIdAndIsActiveTrue(complementId, vendorId))
            .thenReturn(complement)

        val result = complementService.getComplement(complementId, vendorDetails)

        assertThat(result.id).isEqualTo(complementId)
        assertThat(result.name).isEqualTo("Rice")
    }

    @Test
    fun `getComplement throws RequestedEntityNotFoundException when not found`() {
        whenever(complementRepository.findByIdAndVendorIdAndIsActiveTrue(complementId, vendorId))
            .thenReturn(null)

        assertThatThrownBy { complementService.getComplement(complementId, vendorDetails) }
            .isInstanceOf(RequestedEntityNotFoundException::class.java)
    }

    // getAllComplements 

    @Test
    fun `getAllComplements returns paged complements mapped to responses`() {
        val complement = makeComplement(UUID.randomUUID(), "Salad", 300L)
        whenever(complementRepository.findAllByVendorIdAndIsActiveTrue(eq(vendorId), any<Pageable>()))
            .thenReturn(PageImpl(listOf(complement)))

        val result = complementService.getAllComplements(vendorDetails, 0, 10)

        assertThat(result.content).hasSize(1)
        assertThat(result.content[0].name).isEqualTo("Salad")
        assertThat(result.content[0].price).isEqualTo(300L)
    }

    @Test
    fun `getAllComplements returns empty page when vendor has no complements`() {
        whenever(complementRepository.findAllByVendorIdAndIsActiveTrue(eq(vendorId), any<Pageable>()))
            .thenReturn(PageImpl(emptyList()))

        val result = complementService.getAllComplements(vendorDetails, 0, 10)

        assertThat(result.content).isEmpty()
    }

    // scrollComplements 

    @Test
    fun `scrollComplements returns empty window when vendor has no complements`() {
        val emptyWindow = Window.from(emptyList<Complement>()) { ScrollPosition.offset(it.toLong()) }
        whenever(complementRepository.findAllByVendorIdAndIsActiveTrue(
            eq(vendorId), any<ScrollPosition>(), any<Sort>(), any<Limit>()
        )).thenReturn(emptyWindow)

        val result = complementService.scrollComplements(vendorDetails, null, 10)

        assertThat(result.content).isEmpty()
        assertThat(result.hasNext).isFalse()
        assertThat(result.nextCursor).isNull()
    }

    @Test
    fun `scrollComplements returns mapped complement responses`() {
        val complement = makeComplement(complementId, "Garlic Sauce", 200L)
        val window = Window.from(listOf(complement)) { ScrollPosition.offset(it.toLong()) }
        whenever(complementRepository.findAllByVendorIdAndIsActiveTrue(
            eq(vendorId), any<ScrollPosition>(), any<Sort>(), any<Limit>()
        )).thenReturn(window)
        whenever(cursorEncoder.encodeOffset(any())).thenReturn("dummyCursor")

        val result = complementService.scrollComplements(vendorDetails, null, 10)

        assertThat(result.content).hasSize(1)
        assertThat(result.content[0].name).isEqualTo("Garlic Sauce")
        assertThat(result.content[0].price).isEqualTo(200L)
    }

    @Test
    fun `scrollComplements with null cursor calls repository`() {
        val window = Window.from(emptyList<Complement>()) { ScrollPosition.offset(it.toLong()) }
        whenever(complementRepository.findAllByVendorIdAndIsActiveTrue(
            eq(vendorId), any<ScrollPosition>(), any<Sort>(), any<Limit>()
        )).thenReturn(window)

        complementService.scrollComplements(vendorDetails, null, 10)

        verify(complementRepository).findAllByVendorIdAndIsActiveTrue(
            eq(vendorId), any<ScrollPosition>(), any<Sort>(), any<Limit>()
        )
    }

    @Test
    fun `scrollComplements with numeric cursor calls repository`() {
        val window = Window.from(emptyList<Complement>()) { ScrollPosition.offset(it.toLong()) }
        whenever(complementRepository.findAllByVendorIdAndIsActiveTrue(
            eq(vendorId), any<ScrollPosition>(), any<Sort>(), any<Limit>()
        )).thenReturn(window)

        complementService.scrollComplements(vendorDetails, "5", 10)

        verify(complementRepository).findAllByVendorIdAndIsActiveTrue(
            eq(vendorId), any<ScrollPosition>(), any<Sort>(), any<Limit>()
        )
    }

    // deleteComplement 

    @Test
    fun `deleteComplement soft-deletes by setting isActive to false`() {
        val complement = makeComplement(complementId, "Ketchup", 100L)
        whenever(complementRepository.findByIdAndVendorIdAndIsActiveTrue(complementId, vendorId))
            .thenReturn(complement)
        whenever(complementRepository.saveAndFlush(complement)).thenReturn(complement)

        complementService.deleteComplement(complementId, vendorDetails)

        assertThat(complement.isActive).isFalse()
        verify(complementRepository).saveAndFlush(complement)
    }

    @Test
    fun `deleteComplement throws when complement not found`() {
        whenever(complementRepository.findByIdAndVendorIdAndIsActiveTrue(complementId, vendorId))
            .thenReturn(null)

        assertThatThrownBy { complementService.deleteComplement(complementId, vendorDetails) }
            .isInstanceOf(RequestedEntityNotFoundException::class.java)

        verify(complementRepository, never()).saveAndFlush(any())
    }

    // addOrUpdateComplement (create path)

    @Test
    fun `addOrUpdateComplement creates new complement when id is null`() {
        val dto = ComplementDto(id = null, complementName = "Coleslaw", price = 400L, availability = "AVAILABLE")
        whenever(vendorRepository.findById(vendorId)).thenReturn(Optional.of(vendor))
        whenever(duplicateUtility.isDuplicationComplementFound("Coleslaw", vendorId)).thenReturn(false)
        whenever(complementRepository.saveAndFlush(any())).thenAnswer { it.arguments[0] }
        whenever(vendorRepository.save(vendor)).thenReturn(vendor)

        val result = complementService.addOrUpdateComplement(dto, vendorDetails)

        assertThat(result.name).isEqualTo("Coleslaw")
        assertThat(result.price).isEqualTo(400L)
        verify(complementRepository).saveAndFlush(any())
    }

    @Test
    fun `addOrUpdateComplement throws DuplicateFoundException on duplicate name`() {
        val dto = ComplementDto(id = null, complementName = "Coleslaw", price = 400L, availability = "AVAILABLE")
        whenever(vendorRepository.findById(vendorId)).thenReturn(Optional.of(vendor))
        whenever(duplicateUtility.isDuplicationComplementFound("Coleslaw", vendorId)).thenReturn(true)

        assertThatThrownBy { complementService.addOrUpdateComplement(dto, vendorDetails) }
            .isInstanceOf(DuplicateFoundException::class.java)

        verify(complementRepository, never()).saveAndFlush(any())
    }

    @Test
    fun `addOrUpdateComplement throws UserNotFoundException when vendor not found`() {
        val dto = ComplementDto(id = null, complementName = "Aioli", price = 250L, availability = "AVAILABLE")
        whenever(vendorRepository.findById(vendorId)).thenReturn(Optional.empty())

        assertThatThrownBy { complementService.addOrUpdateComplement(dto, vendorDetails) }
            .isInstanceOf(UserNotFoundException::class.java)
    }

    @Test
    fun `addOrUpdateComplement throws when complementName is null`() {
        val dto = ComplementDto(id = null, complementName = null, price = 200L, availability = "AVAILABLE")
        whenever(vendorRepository.findById(vendorId)).thenReturn(Optional.of(vendor))

        assertThatThrownBy { complementService.addOrUpdateComplement(dto, vendorDetails) }
            .isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun `addOrUpdateComplement throws when price is null`() {
        val dto = ComplementDto(id = null, complementName = "Hummus", price = null, availability = "AVAILABLE")
        whenever(vendorRepository.findById(vendorId)).thenReturn(Optional.of(vendor))
        whenever(duplicateUtility.isDuplicationComplementFound("Hummus", vendorId)).thenReturn(false)
        whenever(complementRepository.saveAndFlush(any())).thenAnswer { it.arguments[0] }

        assertThatThrownBy { complementService.addOrUpdateComplement(dto, vendorDetails) }
            .isInstanceOf(IllegalArgumentException::class.java)
    }

    // addOrUpdateComplement (update path) 

    @Test
    fun `addOrUpdateComplement updates existing complement when id is provided`() {
        val complement = makeComplement(complementId, "OldName", 100L)
        val dto = ComplementDto(id = complementId, complementName = "NewName", price = 999L, availability = "AVAILABLE")
        whenever(vendorRepository.findById(vendorId)).thenReturn(Optional.of(vendor))
        whenever(complementRepository.findByIdAndVendorIdAndIsActiveTrue(complementId, vendorId)).thenReturn(complement)
        whenever(complementRepository.saveAndFlush(any())).thenAnswer { it.arguments[0] }
        whenever(vendorRepository.save(vendor)).thenReturn(vendor)

        val result = complementService.addOrUpdateComplement(dto, vendorDetails)

        assertThat(result.name).isEqualTo("NewName")
        assertThat(result.price).isEqualTo(999L)
    }

    @Test
    fun `addOrUpdateComplement throws when updating complement that does not exist`() {
        val dto = ComplementDto(id = complementId, complementName = "X", price = 1L, availability = "AVAILABLE")
        whenever(vendorRepository.findById(vendorId)).thenReturn(Optional.of(vendor))
        whenever(complementRepository.findByIdAndVendorIdAndIsActiveTrue(complementId, vendorId)).thenReturn(null)

        assertThatThrownBy { complementService.addOrUpdateComplement(dto, vendorDetails) }
            .isInstanceOf(RequestedEntityNotFoundException::class.java)
    }

    @Test
    fun `addOrUpdateComplement does not check for duplicate when updating`() {
        val complement = makeComplement(complementId, "Tzatziki", 300L)
        val dto = ComplementDto(id = complementId, complementName = "Tzatziki Updated", price = 350L, availability = "AVAILABLE")
        whenever(vendorRepository.findById(vendorId)).thenReturn(Optional.of(vendor))
        whenever(complementRepository.findByIdAndVendorIdAndIsActiveTrue(complementId, vendorId)).thenReturn(complement)
        whenever(complementRepository.saveAndFlush(any())).thenAnswer { it.arguments[0] }
        whenever(vendorRepository.save(vendor)).thenReturn(vendor)

        complementService.addOrUpdateComplement(dto, vendorDetails)

        verify(duplicateUtility, never()).isDuplicationComplementFound(any(), any())
    }

    // saveComplement 

    @Test
    fun `saveComplement delegates to repository saveAndFlush`() {
        val complement = makeComplement(complementId, "Guacamole", 450L)
        whenever(complementRepository.saveAndFlush(complement)).thenReturn(complement)

        val result = complementService.saveComplement(complement)

        assertThat(result).isEqualTo(complement)
        verify(complementRepository).saveAndFlush(complement)
    }

    // helper

    private fun makeComplement(id: UUID, name: String, price: Long): Complement {
        return Complement().apply {
            this.id = id
            this.name = name
            this.price = price
            this.vendor = this@ComplementServiceTest.vendor
            this.isActive = true
            this.availability = Availability.AVAILABLE
        }
    }
}

