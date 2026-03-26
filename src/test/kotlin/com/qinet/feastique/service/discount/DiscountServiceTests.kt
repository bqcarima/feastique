package com.qinet.feastique.service.discount

import com.qinet.feastique.exception.PermissionDeniedException
import com.qinet.feastique.exception.RequestedEntityNotFoundException
import com.qinet.feastique.exception.UserNotFoundException
import com.qinet.feastique.model.dto.discount.DiscountDto
import com.qinet.feastique.model.entity.discount.Discount
import com.qinet.feastique.model.entity.user.Vendor
import com.qinet.feastique.model.enums.AccountType
import com.qinet.feastique.repository.discount.DiscountRepository
import com.qinet.feastique.repository.user.VendorRepository
import com.qinet.feastique.security.UserSecurity
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.*
import org.springframework.security.core.authority.SimpleGrantedAuthority
import java.text.SimpleDateFormat
import java.util.*

// Shared fixtures

private val DS_VENDOR_ID: UUID = UUID.randomUUID()
private val DS_DISCOUNT_ID: UUID = UUID.randomUUID()
private val DS_OTHER_VENDOR_ID: UUID = UUID.randomUUID()
private val sdf = SimpleDateFormat("dd-MM-yyyy")

private fun dsVendor(): Vendor = Vendor().apply {
    id = DS_VENDOR_ID
    username = "sabi_chef"
    firstName = "Ambe"
    lastName = "Chancie"
    chefName = "Sabi Chef"
    accountType = AccountType.VENDOR
    password = "hashed"
}

private fun dsVendorSecurity(): UserSecurity = UserSecurity(
    id = DS_VENDOR_ID,
    username = "sabi_chef",
    password = "hashed",
    userAuthorities = mutableListOf(SimpleGrantedAuthority("ROLE_VENDOR"))
)

private fun dsDiscount(
    id: UUID = DS_DISCOUNT_ID,
    vendor: Vendor = dsVendor(),
    name: String = "Summer Sale"
): Discount = Discount().apply {
    this.id = id
    this.vendor = vendor
    discountName = name
    percentage = 10
    startDate = sdf.parse("01-01-2026")
    endDate = sdf.parse("31-03-2026")
}

private fun dsDiscountDto(
    id: UUID? = null,
    name: String = "Summer Sale"
): DiscountDto = DiscountDto(
    id = id,
    discountName = name,
    percentage = 10,
    startDate = sdf.parse("01-01-2026"),
    endDate = sdf.parse("31-03-2026")
)


class DiscountServiceTest {

    private lateinit var discountRepository: DiscountRepository
    private lateinit var vendorRepository: VendorRepository
    private lateinit var discountService: DiscountService

    @BeforeEach
    fun setUp() {
        discountRepository = mock()
        vendorRepository = mock()
        discountService = DiscountService(discountRepository, vendorRepository)
    }


    // getDiscount
    @Nested
    inner class GetDiscount {

        @Test
        fun `returns discount when vendor owns it`() {
            whenever(discountRepository.findById(DS_DISCOUNT_ID))
                .thenReturn(Optional.of(dsDiscount()))

            val result = discountService.getDiscount(DS_DISCOUNT_ID, dsVendorSecurity())

            assertEquals(DS_DISCOUNT_ID, result.id)
            assertEquals("Summer Sale", result.discountName)
        }

        @Test
        fun `throws RequestedEntityNotFoundException when discount does not exist`() {
            whenever(discountRepository.findById(DS_DISCOUNT_ID))
                .thenReturn(Optional.empty())

            assertThrows<RequestedEntityNotFoundException> {
                discountService.getDiscount(DS_DISCOUNT_ID, dsVendorSecurity())
            }
        }

        @Test
        fun `throws PermissionDeniedException when discount belongs to another vendor`() {
            val otherVendor = Vendor().apply { id = DS_OTHER_VENDOR_ID }
            whenever(discountRepository.findById(DS_DISCOUNT_ID))
                .thenReturn(Optional.of(dsDiscount(vendor = otherVendor)))

            assertThrows<PermissionDeniedException> {
                discountService.getDiscount(DS_DISCOUNT_ID, dsVendorSecurity())
            }
        }
    }


    // getAllDiscounts
    @Nested
    inner class GetAllDiscounts {

        @Test
        fun `returns all discounts belonging to the vendor`() {
            whenever(discountRepository.findAllByVendorId(DS_VENDOR_ID))
                .thenReturn(listOf(dsDiscount(), dsDiscount(id = UUID.randomUUID(), name = "Winter Sale")))

            val result = discountService.getAllDiscounts(dsVendorSecurity())

            assertEquals(2, result.size)
        }

        @Test
        fun `throws RequestedEntityNotFoundException when vendor has no discounts`() {
            whenever(discountRepository.findAllByVendorId(DS_VENDOR_ID))
                .thenReturn(emptyList())

            assertThrows<RequestedEntityNotFoundException> {
                discountService.getAllDiscounts(dsVendorSecurity())
            }
        }

        @Test
        fun `throws PermissionDeniedException when any discount belongs to a different vendor`() {
            val otherVendor = Vendor().apply { id = DS_OTHER_VENDOR_ID }
            whenever(discountRepository.findAllByVendorId(DS_VENDOR_ID))
                .thenReturn(listOf(dsDiscount(vendor = otherVendor)))

            assertThrows<PermissionDeniedException> {
                discountService.getAllDiscounts(dsVendorSecurity())
            }
        }
    }


    // getDuplicates
    @Nested
    inner class GetDuplicates {

        @Test
        fun `returns true when a discount with the same name exists for this vendor`() {
            whenever(
                discountRepository.findFirstByDiscountNameIgnoreCaseAndVendorId("Summer Sale", DS_VENDOR_ID)
            ).thenReturn(dsDiscount())

            assertTrue(discountService.getDuplicates("Summer Sale", dsVendorSecurity()))
        }

        @Test
        fun `returns false when no discount with the name exists for this vendor`() {
            whenever(
                discountRepository.findFirstByDiscountNameIgnoreCaseAndVendorId("Unique Name", DS_VENDOR_ID)
            ).thenReturn(null)

            assertFalse(discountService.getDuplicates("Unique Name", dsVendorSecurity()))
        }
    }


    // deleteDiscount
    @Nested
    inner class DeleteDiscount {

        @Test
        fun `deletes discount when vendor owns it`() {
            val discount = dsDiscount()
            whenever(discountRepository.findById(DS_DISCOUNT_ID)).thenReturn(Optional.of(discount))

            discountService.deleteDiscount(DS_DISCOUNT_ID, dsVendorSecurity())

            verify(discountRepository).delete(discount)
        }

        @Test
        fun `throws RequestedEntityNotFoundException and does not delete when discount is not found`() {
            whenever(discountRepository.findById(DS_DISCOUNT_ID)).thenReturn(Optional.empty())

            assertThrows<RequestedEntityNotFoundException> {
                discountService.deleteDiscount(DS_DISCOUNT_ID, dsVendorSecurity())
            }

            verify(discountRepository, never()).delete(any<Discount>())
        }

        @Test
        fun `throws PermissionDeniedException and does not delete when vendor does not own discount`() {
            val otherVendor = Vendor().apply { id = DS_OTHER_VENDOR_ID }
            whenever(discountRepository.findById(DS_DISCOUNT_ID))
                .thenReturn(Optional.of(dsDiscount(vendor = otherVendor)))

            assertThrows<PermissionDeniedException> {
                discountService.deleteDiscount(DS_DISCOUNT_ID, dsVendorSecurity())
            }

            verify(discountRepository, never()).delete(any<Discount>())
        }
    }


    // deleteAllDiscounts
    @Nested
    inner class DeleteAllDiscounts {

        @Test
        fun `deletes all discounts for the vendor`() {
            whenever(discountRepository.findAllByVendorId(DS_VENDOR_ID))
                .thenReturn(listOf(dsDiscount()))

            discountService.deleteAllDiscounts(dsVendorSecurity())

            verify(discountRepository).deleteAllByVendorId(DS_VENDOR_ID)
        }

        @Test
        fun `throws RequestedEntityNotFoundException when vendor has no discounts to delete`() {
            whenever(discountRepository.findAllByVendorId(DS_VENDOR_ID)).thenReturn(emptyList())

            assertThrows<RequestedEntityNotFoundException> {
                discountService.deleteAllDiscounts(dsVendorSecurity())
            }

            verify(discountRepository, never()).deleteAllByVendorId(any())
        }
    }


    // saveDiscount
    @Nested
    inner class SaveDiscount {

        @Test
        fun `saves and returns the discount`() {
            val discount = dsDiscount()
            whenever(discountRepository.save(discount)).thenReturn(discount)

            val result = discountService.saveDiscount(discount)

            assertEquals(discount, result)
            verify(discountRepository).save(discount)
        }
    }


    // addOrUpdateDiscount — create
    @Nested
    inner class AddOrUpdateDiscountCreate {

        @BeforeEach
        fun stub() {
            whenever(vendorRepository.findById(DS_VENDOR_ID)).thenReturn(Optional.of(dsVendor()))
            whenever(
                discountRepository.findFirstByDiscountNameIgnoreCaseAndVendorId("Summer Sale", DS_VENDOR_ID)
            ).thenReturn(null)
            whenever(discountRepository.save(any())).thenAnswer { it.arguments[0] }
        }

        @Test
        fun `creates new discount with correct name and percentage`() {
            val result = discountService.addOrUpdateDiscount(dsDiscountDto(), dsVendorSecurity())

            assertEquals("Summer Sale", result.discountName)
            assertEquals(10, result.percentage)
        }

        @Test
        fun `creates new discount with correct dates`() {
            val result = discountService.addOrUpdateDiscount(dsDiscountDto(), dsVendorSecurity())

            assertEquals(sdf.parse("01-01-2026"), result.startDate)
            assertEquals(sdf.parse("31-03-2026"), result.endDate)
        }

        @Test
        fun `persists the new discount via repository`() {
            discountService.addOrUpdateDiscount(dsDiscountDto(), dsVendorSecurity())
            verify(discountRepository).save(any())
        }

        @Test
        fun `throws UserNotFoundException when vendor does not exist`() {
            whenever(vendorRepository.findById(DS_VENDOR_ID)).thenReturn(Optional.empty())

            assertThrows<UserNotFoundException> {
                discountService.addOrUpdateDiscount(dsDiscountDto(), dsVendorSecurity())
            }
        }
    }


    // addOrUpdateDiscount — update
    @Nested
    inner class AddOrUpdateDiscountUpdate {

        @BeforeEach
        fun stub() {
            val existing = dsDiscount()
            whenever(vendorRepository.findById(DS_VENDOR_ID)).thenReturn(Optional.of(dsVendor()))
            whenever(discountRepository.findById(DS_DISCOUNT_ID)).thenReturn(Optional.of(existing))
            whenever(
                discountRepository.findFirstByDiscountNameIgnoreCaseAndVendorId(any(), eq(DS_VENDOR_ID))
            ).thenReturn(null)
            whenever(discountRepository.save(any())).thenAnswer { it.arguments[0] }
        }

        @Test
        fun `updates existing discount when id is provided`() {
            val dto = dsDiscountDto(id = DS_DISCOUNT_ID, name = "Flash Sale")

            val result = discountService.addOrUpdateDiscount(dto, dsVendorSecurity())

            assertEquals("Flash Sale", result.discountName)
        }

        @Test
        fun `throws RequestedEntityNotFoundException when referenced discount id does not exist`() {
            whenever(discountRepository.findById(DS_DISCOUNT_ID)).thenReturn(Optional.empty())

            assertThrows<RequestedEntityNotFoundException> {
                discountService.addOrUpdateDiscount(dsDiscountDto(id = DS_DISCOUNT_ID), dsVendorSecurity())
            }
        }

        @Test
        fun `throws PermissionDeniedException when discount belongs to another vendor`() {
            val otherVendor = Vendor().apply { id = DS_OTHER_VENDOR_ID }
            whenever(discountRepository.findById(DS_DISCOUNT_ID))
                .thenReturn(Optional.of(dsDiscount(vendor = otherVendor)))

            assertThrows<PermissionDeniedException> {
                discountService.addOrUpdateDiscount(dsDiscountDto(id = DS_DISCOUNT_ID), dsVendorSecurity())
            }
        }
    }
}

