package com.qinet.feastique.service.customer


import com.qinet.feastique.exception.PermissionDeniedException
import com.qinet.feastique.exception.RequestedEntityNotFoundException
import com.qinet.feastique.model.dto.address.AddressDto
import com.qinet.feastique.model.entity.address.CustomerAddress
import com.qinet.feastique.model.entity.user.Customer
import com.qinet.feastique.model.enums.AccountType
import com.qinet.feastique.model.enums.Region
import com.qinet.feastique.repository.address.CustomerAddressRepository
import com.qinet.feastique.repository.user.CustomerRepository
import com.qinet.feastique.security.UserSecurity
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.*
import org.springframework.security.core.authority.SimpleGrantedAuthority
import java.util.*

// Shared data
private val CUSTOMER_ID = UUID.randomUUID()
private val ADDRESS_ID_1 = UUID.randomUUID()
private val ADDRESS_ID_2 = UUID.randomUUID()

private fun janeCustomer(): Customer = Customer().apply {
    id = CUSTOMER_ID
    username = "jane_doe"
    firstName = "Jane"
    lastName = "Doe"
    accountType = AccountType.CUSTOMER
    password = "hashed_passWord123"
}

private fun janeSecurity(): UserSecurity = UserSecurity(
    id = CUSTOMER_ID,
    username = "jane_doe",
    password = "hashed_passWord123",
    userAuthorities = mutableListOf(SimpleGrantedAuthority("ROLE_CUSTOMER"))
)

private fun defaultAddress(
    id: UUID = ADDRESS_ID_1,
    customer: Customer = janeCustomer()
): CustomerAddress = CustomerAddress().apply {
    this.id = id
    this.customer = customer
    country = "Cameroon"
    region = Region.LITTORAL
    city = "Douala"
    neighbourhood = "Akwa"
    streetName = "Street 1"
    directions = "Near the market"
    longitude = "9.70"
    latitude = "4.05"
    default = true
    isActive = true
}

private fun secondAddress(
    id: UUID = ADDRESS_ID_2,
    customer: Customer = janeCustomer()
): CustomerAddress = CustomerAddress().apply {
    this.id = id
    this.customer = customer
    country = "Cameroon"
    region = Region.CENTRE
    city = "Yaounde"
    neighbourhood = "Biyem-Assi"
    streetName = "Street 2"
    directions = "Fifty metres after the roundabout"
    longitude = "11.52"
    latitude = "3.87"
    default = false
    isActive = true
}

private fun addressDto(
    id: UUID? = null,
    region: String? = "LITTORAL",
    default: Boolean? = false
): AddressDto = AddressDto(
    id = id,
    country = "Cameroon",
    region = region,
    city = "Douala",
    neighbourhood = "Akwa",
    streetName = "Street 1",
    directions = "Near the market",
    longitude = "9.70",
    latitude = "4.05",
    default = default
)


// CustomerAddressService tests
class CustomerAddressServiceTest {

    private lateinit var customerAddressRepository: CustomerAddressRepository
    private lateinit var customerRepository: CustomerRepository
    private lateinit var customerAddressService: CustomerAddressService

    @BeforeEach
    fun setUp() {
        customerAddressRepository = mock()
        customerRepository = mock()
        customerAddressService = CustomerAddressService(
            customerAddressRepository = customerAddressRepository,
            customerRepository = customerRepository
        )
    }

    // getAddressById
    @Nested
    inner class GetAddressById {

        @Test
        fun `returns address when found for the customer`() {
            val address = defaultAddress()
            whenever(
                customerAddressRepository.findByIdAndCustomerIdAndIsActiveTrue(ADDRESS_ID_1, CUSTOMER_ID)
            ).thenReturn(address)

            val result = customerAddressService.getAddressById(ADDRESS_ID_1, janeSecurity())

            assertEquals(ADDRESS_ID_1, result.id)
            assertEquals("Douala", result.city)
        }

        @Test
        fun `throws RequestedEntityNotFoundException when address does not exist`() {
            whenever(
                customerAddressRepository.findByIdAndCustomerIdAndIsActiveTrue(ADDRESS_ID_1, CUSTOMER_ID)
            ).thenReturn(null)

            assertThrows<RequestedEntityNotFoundException> {
                customerAddressService.getAddressById(ADDRESS_ID_1, janeSecurity())
            }
        }

        @Test
        fun `throws RequestedEntityNotFoundException when address belongs to a different customer`() {
            // Repository filters by customerId — returning null simulates the ownership check failing
            whenever(
                customerAddressRepository.findByIdAndCustomerIdAndIsActiveTrue(ADDRESS_ID_1, CUSTOMER_ID)
            ).thenReturn(null)

            assertThrows<RequestedEntityNotFoundException> {
                customerAddressService.getAddressById(ADDRESS_ID_1, janeSecurity())
            }
        }

        @Test
        fun `does not return soft-deleted addresses`() {
            // findByIdAndCustomerIdAndIsActiveTrue excludes inactive records at DB level
            whenever(
                customerAddressRepository.findByIdAndCustomerIdAndIsActiveTrue(ADDRESS_ID_1, CUSTOMER_ID)
            ).thenReturn(null)

            assertThrows<RequestedEntityNotFoundException> {
                customerAddressService.getAddressById(ADDRESS_ID_1, janeSecurity())
            }
        }
    }


    // getAllAddresses
    @Nested
    inner class GetAllAddresses {

        @Test
        fun `returns all active addresses for the customer`() {
            val addresses = listOf(defaultAddress(), secondAddress())
            whenever(
                customerAddressRepository.findAllByCustomerIdAndIsActiveTrue(CUSTOMER_ID)
            ).thenReturn(addresses)

            val result = customerAddressService.getAllAddresses(janeSecurity())

            assertEquals(2, result.size)
        }

        @Test
        fun `throws RequestedEntityNotFoundException when customer has no active addresses`() {
            whenever(
                customerAddressRepository.findAllByCustomerIdAndIsActiveTrue(CUSTOMER_ID)
            ).thenReturn(emptyList())

            assertThrows<RequestedEntityNotFoundException> {
                customerAddressService.getAllAddresses(janeSecurity())
            }
        }

        @Test
        fun `throws PermissionDeniedException when an address belongs to a different customer`() {
            val otherCustomer = Customer().apply {
                id = UUID.randomUUID()
                username = "other_user"
            }
            val foreignAddress = defaultAddress(customer = otherCustomer)
            whenever(
                customerAddressRepository.findAllByCustomerIdAndIsActiveTrue(CUSTOMER_ID)
            ).thenReturn(listOf(foreignAddress))

            assertThrows<PermissionDeniedException> {
                customerAddressService.getAllAddresses(janeSecurity())
            }
        }
    }


    // saveAddress
    @Nested
    inner class SaveAddress {

        @Test
        fun `saves and returns the address`() {
            val address = defaultAddress()
            whenever(customerAddressRepository.save(address)).thenReturn(address)

            val result = customerAddressService.saveAddress(address)

            assertEquals(address, result)
            verify(customerAddressRepository).save(address)
        }
    }


    // deleteAddress
    @Nested
    inner class DeleteAddress {

        @BeforeEach
        fun stubDelete() {
            whenever(customerRepository.findById(CUSTOMER_ID))
                .thenReturn(Optional.of(janeCustomer()))
        }

        @Test
        fun `soft-deletes the address by setting isActive to false`() {
            val addr1 = defaultAddress()
            val addr2 = secondAddress()
            whenever(customerAddressRepository.findAllByCustomerIdAndIsActiveTrue(CUSTOMER_ID))
                .thenReturn(listOf(addr1, addr2))
            whenever(customerAddressRepository.save(any())).thenReturn(addr2)

            customerAddressService.deleteAddress(ADDRESS_ID_2, janeSecurity())

            verify(customerAddressRepository).save(argThat {
                id == ADDRESS_ID_2 && isActive == false
            })
        }

        @Test
        fun `throws IllegalArgumentException when deleting the default address`() {
            val addr1 = defaultAddress()
            val addr2 = secondAddress()
            whenever(customerAddressRepository.findAllByCustomerIdAndIsActiveTrue(CUSTOMER_ID))
                .thenReturn(listOf(addr1, addr2))

            assertThrows<IllegalArgumentException> {
                customerAddressService.deleteAddress(ADDRESS_ID_1, janeSecurity())
            }
        }

        @Test
        fun `throws IllegalArgumentException when only one address exists`() {
            val addr1 = defaultAddress()
            whenever(customerAddressRepository.findAllByCustomerIdAndIsActiveTrue(CUSTOMER_ID))
                .thenReturn(listOf(addr1))

            assertThrows<IllegalArgumentException> {
                customerAddressService.deleteAddress(ADDRESS_ID_1, janeSecurity())
            }
        }

        @Test
        fun `throws RequestedEntityNotFoundException when address id is not in the customer list`() {
            val addr1 = defaultAddress()
            val addr2 = secondAddress()
            whenever(customerAddressRepository.findAllByCustomerIdAndIsActiveTrue(CUSTOMER_ID))
                .thenReturn(listOf(addr1, addr2))

            val unknownId = UUID.randomUUID()
            assertThrows<RequestedEntityNotFoundException> {
                customerAddressService.deleteAddress(unknownId, janeSecurity())
            }
        }

        @Test
        fun `throws RequestedEntityNotFoundException when customer is not found`() {
            whenever(customerRepository.findById(CUSTOMER_ID)).thenReturn(Optional.empty())

            assertThrows<RequestedEntityNotFoundException> {
                customerAddressService.deleteAddress(ADDRESS_ID_2, janeSecurity())
            }
        }

        @Test
        fun `does not save when address is not found`() {
            val addr1 = defaultAddress()
            val addr2 = secondAddress()
            whenever(customerAddressRepository.findAllByCustomerIdAndIsActiveTrue(CUSTOMER_ID))
                .thenReturn(listOf(addr1, addr2))

            val unknownId = UUID.randomUUID()
            runCatching { customerAddressService.deleteAddress(unknownId, janeSecurity()) }

            verify(customerAddressRepository, never()).save(any())
        }
    }


    // addAddress — create new
    @Nested
    inner class AddAddressCreate {

        @BeforeEach
        fun stubCreate() {
            val customer = janeCustomer()
            val saved = defaultAddress()
            whenever(customerRepository.findById(CUSTOMER_ID)).thenReturn(Optional.of(customer))
            whenever(customerAddressRepository.save(any())).thenReturn(saved)
            whenever(customerAddressRepository.findAllByCustomerIdAndIsActiveTrue(CUSTOMER_ID))
                .thenReturn(listOf(saved))
        }

        @Test
        fun `creates a new address when dto id is null`() {
            customerAddressService.addAddress(addressDto(), janeSecurity())
            verify(customerAddressRepository).save(argThat {
                city == "Douala" &&
                        neighbourhood == "Akwa" &&
                        isActive == true
            })
        }

        @Test
        fun `returns all active addresses after creation`() {
            val result = customerAddressService.addAddress(addressDto(), janeSecurity())
            assertEquals(1, result.size)
        }

        @Test
        fun `sets all fields from dto on new address`() {
            customerAddressService.addAddress(addressDto(), janeSecurity())
            verify(customerAddressRepository).save(argThat {
                country == "Cameroon" &&
                        region == Region.LITTORAL &&
                        city == "Douala" &&
                        neighbourhood == "Akwa" &&
                        streetName == "Street 1" &&
                        directions == "Near the market" &&
                        longitude == "9.70" &&
                        latitude == "4.05"
            })
        }

        @Test
        fun `marks new address as default and clears existing defaults when default is true`() {
            val existing = defaultAddress()
            whenever(customerAddressRepository.findAllByCustomerIdAndIsActiveTrue(CUSTOMER_ID))
                .thenReturn(listOf(existing))

            customerAddressService.addAddress(addressDto(default = true), janeSecurity())

            // existing addresses had default cleared
            verify(customerAddressRepository).saveAll(argThat<List<CustomerAddress>> {
                all { !it.default!! }
            })
            // new address saved with default = true
            verify(customerAddressRepository).save(argThat { default == true })
        }

        @Test
        fun `does not clear existing defaults when default is false`() {
            customerAddressService.addAddress(addressDto(default = false), janeSecurity())
            verify(customerAddressRepository, never()).saveAll(any<List<CustomerAddress>>())
        }

        @Test
        fun `throws RequestedEntityNotFoundException when customer is not found`() {
            whenever(customerRepository.findById(CUSTOMER_ID)).thenReturn(Optional.empty())

            assertThrows<RequestedEntityNotFoundException> {
                customerAddressService.addAddress(addressDto(), janeSecurity())
            }
        }
    }


    // addAddress — update existing
    @Nested
    inner class AddAddressUpdate {

        @BeforeEach
        fun stubUpdate() {
            val customer = janeCustomer()
            val existing = defaultAddress()
            val updated = defaultAddress().apply { city = "Buea" }
            whenever(customerRepository.findById(CUSTOMER_ID)).thenReturn(Optional.of(customer))
            whenever(
                customerAddressRepository.findByIdAndCustomerIdAndIsActiveTrue(ADDRESS_ID_1, CUSTOMER_ID)
            ).thenReturn(existing)
            whenever(customerAddressRepository.save(any())).thenReturn(updated)
            whenever(customerAddressRepository.findAllByCustomerIdAndIsActiveTrue(CUSTOMER_ID))
                .thenReturn(listOf(updated))
        }

        @Test
        fun `updates existing address when dto id is provided`() {
            val dto = addressDto(id = ADDRESS_ID_1, region = "LITTORAL")
            customerAddressService.addAddress(dto, janeSecurity())

            verify(customerAddressRepository).save(argThat { id == ADDRESS_ID_1 })
        }

        @Test
        fun `returns all active addresses after update`() {
            val dto = addressDto(id = ADDRESS_ID_1)
            val result = customerAddressService.addAddress(dto, janeSecurity())
            assertEquals(1, result.size)
        }

        @Test
        fun `throws RequestedEntityNotFoundException when address to update does not exist`() {
            whenever(
                customerAddressRepository.findByIdAndCustomerIdAndIsActiveTrue(ADDRESS_ID_1, CUSTOMER_ID)
            ).thenReturn(null)

            assertThrows<RequestedEntityNotFoundException> {
                customerAddressService.addAddress(addressDto(id = ADDRESS_ID_1), janeSecurity())
            }
        }
    }
}

