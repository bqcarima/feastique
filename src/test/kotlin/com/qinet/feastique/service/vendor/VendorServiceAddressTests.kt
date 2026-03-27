package com.qinet.feastique.service.vendor


import com.qinet.feastique.exception.RequestedEntityNotFoundException
import com.qinet.feastique.exception.UserNotFoundException
import com.qinet.feastique.model.dto.address.AddressDto
import com.qinet.feastique.model.entity.address.VendorAddress
import com.qinet.feastique.model.entity.user.Vendor
import com.qinet.feastique.model.enums.AccountType
import com.qinet.feastique.model.enums.Region
import com.qinet.feastique.repository.address.VendorAddressRepository
import com.qinet.feastique.repository.user.VendorRepository
import com.qinet.feastique.security.UserSecurity
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.*
import org.springframework.security.core.authority.SimpleGrantedAuthority
import java.time.LocalTime
import java.util.*

// Shared data

private val VAS_VENDOR_ID = UUID.randomUUID()
private val VAS_ADDRESS_ID = UUID.randomUUID()

private fun sabiVendor(): Vendor = Vendor().apply {
    id = VAS_VENDOR_ID
    username = "sabi_chef"
    firstName = "Ambe"
    lastName = "Chancie"
    chefName = "Sabi Chef"
    restaurantName = "Sabi Foods"
    accountType = AccountType.VENDOR
    password = "hashed_sabiChef98"
    region = Region.CENTRE
    vendorCode = "CM020001"
    openingTime = LocalTime.of(8, 30)
    closingTime = LocalTime.of(18, 30)
}

private fun sabiSecurity(): UserSecurity = UserSecurity(
    id = VAS_VENDOR_ID,
    username = "sabi_chef",
    password = "hashed_sabiChef98",
    userAuthorities = mutableListOf(SimpleGrantedAuthority("ROLE_VENDOR"))
)

private fun sabiAddress(vendor: Vendor = sabiVendor()): VendorAddress = VendorAddress().apply {
    id = VAS_ADDRESS_ID
    this.vendor = vendor
    country = "Cameroon"
    region = Region.CENTRE
    city = "Yaounde"
    neighbourhood = "Biyem-Assi"
    streetName = "----"
    directions = "Fifty metres after Mogahmo on the other side of the road"
    longitude = "----"
    latitude = "-----"
}

private fun sabiAddressDto(
    id: UUID? = VAS_ADDRESS_ID,
    region: String? = "CENTRE"
): AddressDto = AddressDto(
    id = id,
    country = "Cameroon",
    region = region,
    city = "Yaounde",
    neighbourhood = "Biyem-Assi",
    streetName = "----",
    directions = "Fifty metres after Mogahmo on the other side of the road",
    longitude = "----",
    latitude = "-----",
    default = false
)


// VendorAddressService tests
class VendorAddressServiceTest {

    private lateinit var vendorRepository: VendorRepository
    private lateinit var vendorAddressRepository: VendorAddressRepository
    private lateinit var vendorAddressService: VendorAddressService

    @BeforeEach
    fun setUp() {
        vendorRepository = mock()
        vendorAddressRepository = mock()
        vendorAddressService = VendorAddressService(
            vendorRepository = vendorRepository,
            vendorAddressRepository = vendorAddressRepository
        )
    }


    // getAddress
    @Nested
    inner class GetAddress {

        @Test
        fun `returns address when found for the vendor`() {
            val address = sabiAddress()
            whenever(
                vendorAddressRepository.findByIdAndVendorId(VAS_ADDRESS_ID, VAS_VENDOR_ID)
            ).thenReturn(address)

            val result = vendorAddressService.getAddress(VAS_ADDRESS_ID, sabiSecurity())

            assertEquals(VAS_ADDRESS_ID, result.id)
            assertEquals("Yaounde", result.city)
        }

        @Test
        fun `throws RequestedEntityNotFoundException when address does not exist`() {
            whenever(
                vendorAddressRepository.findByIdAndVendorId(VAS_ADDRESS_ID, VAS_VENDOR_ID)
            ).thenReturn(null)

            assertThrows<RequestedEntityNotFoundException> {
                vendorAddressService.getAddress(VAS_ADDRESS_ID, sabiSecurity())
            }
        }

        @Test
        fun `throws RequestedEntityNotFoundException when address belongs to a different vendor`() {
            // Repository filters by vendorId — null simulates the ownership check failing
            whenever(
                vendorAddressRepository.findByIdAndVendorId(VAS_ADDRESS_ID, VAS_VENDOR_ID)
            ).thenReturn(null)

            assertThrows<RequestedEntityNotFoundException> {
                vendorAddressService.getAddress(VAS_ADDRESS_ID, sabiSecurity())
            }
        }

        @Test
        fun `passes the correct vendor id to the repository`() {
            whenever(
                vendorAddressRepository.findByIdAndVendorId(VAS_ADDRESS_ID, VAS_VENDOR_ID)
            ).thenReturn(sabiAddress())

            vendorAddressService.getAddress(VAS_ADDRESS_ID, sabiSecurity())

            verify(vendorAddressRepository).findByIdAndVendorId(VAS_ADDRESS_ID, VAS_VENDOR_ID)
        }
    }


    // saveAddress
    @Nested
    inner class SaveAddress {

        @Test
        fun `saves and returns the vendor address`() {
            val address = sabiAddress()
            whenever(vendorAddressRepository.save(address)).thenReturn(address)

            val result = vendorAddressService.saveAddress(address)

            assertEquals(address, result)
            verify(vendorAddressRepository).save(address)
        }
    }


    // updateAddress — happy path
    @Nested
    inner class UpdateAddressHappyPath {

        @BeforeEach
        fun stubUpdate() {
            val vendor = sabiVendor()
            val address = sabiAddress(vendor)
            whenever(vendorRepository.findById(VAS_VENDOR_ID)).thenReturn(Optional.of(vendor))
            whenever(
                vendorAddressRepository.findByIdAndVendorId(VAS_ADDRESS_ID, VAS_VENDOR_ID)
            ).thenReturn(address)
            whenever(vendorAddressRepository.save(any())).thenReturn(address)
            whenever(vendorRepository.save(any())).thenReturn(vendor)
        }

        @Test
        fun `returns the updated vendor address`() {
            val result = vendorAddressService.updateAddress(sabiAddressDto(), sabiSecurity())
            assertNotNull(result)
            assertEquals(VAS_ADDRESS_ID, result.id)
        }

        @Test
        fun `saves address with all fields from the dto`() {
            vendorAddressService.updateAddress(sabiAddressDto(), sabiSecurity())
            verify(vendorAddressRepository).save(argThat {
                country == "Cameroon" &&
                        region == Region.CENTRE &&
                        city == "Yaounde" &&
                        neighbourhood == "Biyem-Assi" &&
                        streetName == "----" &&
                        directions == "Fifty metres after Mogahmo on the other side of the road" &&
                        longitude == "----" &&
                        latitude == "-----"
            })
        }

        @Test
        fun `links the address to the correct vendor`() {
            vendorAddressService.updateAddress(sabiAddressDto(), sabiSecurity())
            verify(vendorAddressRepository).save(argThat {
                vendor.id == VAS_VENDOR_ID
            })
        }

        @Test
        fun `saves the vendor after updating the address`() {
            vendorAddressService.updateAddress(sabiAddressDto(), sabiSecurity())
            verify(vendorRepository).save(argThat { id == VAS_VENDOR_ID })
        }

        @Test
        fun `updates vendor accountUpdated timestamp`() {
            vendorAddressService.updateAddress(sabiAddressDto(), sabiSecurity())
            verify(vendorRepository).save(argThat {
                accountUpdated != null
            })
        }
    }


    // updateAddress — failure paths
    @Nested
    inner class UpdateAddressFailure {

        @Test
        fun `throws UserNotFoundException when vendor is not found`() {
            whenever(vendorRepository.findById(VAS_VENDOR_ID)).thenReturn(Optional.empty())

            assertThrows<UserNotFoundException> {
                vendorAddressService.updateAddress(sabiAddressDto(), sabiSecurity())
            }
        }

        @Test
        fun `throws RequestedEntityNotFoundException when address does not exist`() {
            whenever(vendorRepository.findById(VAS_VENDOR_ID))
                .thenReturn(Optional.of(sabiVendor()))
            whenever(
                vendorAddressRepository.findByIdAndVendorId(VAS_ADDRESS_ID, VAS_VENDOR_ID)
            ).thenReturn(null)

            assertThrows<RequestedEntityNotFoundException> {
                vendorAddressService.updateAddress(sabiAddressDto(), sabiSecurity())
            }
        }

        @Test
        fun `throws RequestedEntityNotFoundException when address belongs to a different vendor`() {
            whenever(vendorRepository.findById(VAS_VENDOR_ID))
                .thenReturn(Optional.of(sabiVendor()))
            // findByIdAndVendorId returns null when ownership check fails
            whenever(
                vendorAddressRepository.findByIdAndVendorId(VAS_ADDRESS_ID, VAS_VENDOR_ID)
            ).thenReturn(null)

            assertThrows<RequestedEntityNotFoundException> {
                vendorAddressService.updateAddress(sabiAddressDto(), sabiSecurity())
            }
        }

        @Test
        fun `does not save address when vendor is not found`() {
            whenever(vendorRepository.findById(VAS_VENDOR_ID)).thenReturn(Optional.empty())

            runCatching { vendorAddressService.updateAddress(sabiAddressDto(), sabiSecurity()) }

            verify(vendorAddressRepository, never()).save(any())
        }

        @Test
        fun `does not save address when address is not found`() {
            whenever(vendorRepository.findById(VAS_VENDOR_ID))
                .thenReturn(Optional.of(sabiVendor()))
            whenever(
                vendorAddressRepository.findByIdAndVendorId(VAS_ADDRESS_ID, VAS_VENDOR_ID)
            ).thenReturn(null)

            runCatching { vendorAddressService.updateAddress(sabiAddressDto(), sabiSecurity()) }

            verify(vendorAddressRepository, never()).save(any())
        }

        @Test
        fun `throws NullPointerException when dto id is null`() {
            whenever(vendorRepository.findById(VAS_VENDOR_ID))
                .thenReturn(Optional.of(sabiVendor()))

            // updateAddress calls addressDto.id!! — null id causes NPE
            assertThrows<NullPointerException> {
                vendorAddressService.updateAddress(sabiAddressDto(id = null), sabiSecurity())
            }
        }
    }
}

