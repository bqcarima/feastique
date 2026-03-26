package com.qinet.feastique.service.contact

import com.qinet.feastique.exception.*
import com.qinet.feastique.model.dto.contact.PhoneNumberDto
import com.qinet.feastique.model.entity.address.VendorAddress
import com.qinet.feastique.model.entity.contact.CustomerPhoneNumber
import com.qinet.feastique.model.entity.contact.VendorPhoneNumber
import com.qinet.feastique.model.entity.user.Customer
import com.qinet.feastique.model.entity.user.Vendor
import com.qinet.feastique.model.enums.AccountType
import com.qinet.feastique.repository.contact.CustomerPhoneNumberRepository
import com.qinet.feastique.repository.contact.VendorPhoneNumberRepository
import com.qinet.feastique.repository.user.CustomerRepository
import com.qinet.feastique.repository.user.VendorRepository
import com.qinet.feastique.security.UserSecurity
import com.qinet.feastique.utility.DuplicateUtility
import com.qinet.feastique.utility.SecurityUtility
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.*
import org.mockito.kotlin.any
import org.springframework.security.core.authority.SimpleGrantedAuthority
import java.util.*

class PhoneNumberServiceTests {

    private val duplicateUtility: DuplicateUtility = mock(DuplicateUtility::class.java)
    private val securityUtility: SecurityUtility = mock(SecurityUtility::class.java)
    private val customerRepository: CustomerRepository = mock(CustomerRepository::class.java)
    private val customerPhoneNumberRepository: CustomerPhoneNumberRepository = mock(CustomerPhoneNumberRepository::class.java)
    private val vendorRepository: VendorRepository = mock(VendorRepository::class.java)
    private val vendorPhoneNumberRepository: VendorPhoneNumberRepository = mock(VendorPhoneNumberRepository::class.java)

    private val service = PhoneNumberService(
        duplicateUtility,
        securityUtility,
        customerRepository,
        customerPhoneNumberRepository,
        vendorRepository,
        vendorPhoneNumberRepository
    )

    private val customerId = UUID.randomUUID()
    private val vendorId = UUID.randomUUID()
    private val firstPhoneNumberId = UUID.randomUUID()
    private lateinit var customer: Customer
    private lateinit var vendor: Vendor
    private lateinit var customerDetails: UserSecurity
    private lateinit var vendorDetails: UserSecurity

    @BeforeEach
    fun setup() {

        customerDetails = UserSecurity(
            id = customerId,
            username = "jane_doe",
            password = "hashed_passWord123",
            userAuthorities = mutableListOf(SimpleGrantedAuthority("ROLE_CUSTOMER"))
        )

        vendorDetails = UserSecurity(
            id = vendorId,
            username = "testvendor",
            password = "password",
            mutableListOf(SimpleGrantedAuthority("ROLE_VENDOR"))
        )

        vendor = buildVendor()
        customer = Customer().apply {
            id = customerId
            username = "jane_doe"
            firstName = "Jane"
            lastName = "Doe"
            accountType = AccountType.CUSTOMER
            password = "hashed_passWord123"
        }
    }

    // getPhoneNumber

    @Test
    fun `getPhoneNumber returns customer phone number when customer owns it`() {
        val phoneNumber = buildCustomerPhone(customer)
        `when`(securityUtility.getSingleRole(customerDetails)).thenReturn("CUSTOMER")
        `when`(customerRepository.findById(customerId)).thenReturn(Optional.of(customer))
        `when`(customerPhoneNumberRepository.findById(firstPhoneNumberId)).thenReturn(Optional.of(phoneNumber))

        val result = service.getPhoneNumber<CustomerPhoneNumber>(firstPhoneNumberId, customerDetails)

        assertEquals(firstPhoneNumberId, result.id)
        assertEquals("670000001", result.phoneNumber)
    }

    @Test
    fun `getPhoneNumber throws PermissionDeniedException when customer does not own phone number`() {
        val otherCustomer = Customer().apply { id = UUID.randomUUID(); username = "other" }
        val phoneNumber = CustomerPhoneNumber().apply {
            id = firstPhoneNumberId
            this.customer = otherCustomer
        }
        `when`(securityUtility.getSingleRole(customerDetails)).thenReturn("CUSTOMER")
        `when`(customerRepository.findById(customerId)).thenReturn(Optional.of(customer))
        `when`(customerPhoneNumberRepository.findById(firstPhoneNumberId)).thenReturn(Optional.of(phoneNumber))

        assertThrows<PermissionDeniedException> {
            service.getPhoneNumber<CustomerPhoneNumber>(firstPhoneNumberId, customerDetails)
        }
    }

    @Test
    fun `getPhoneNumber throws PhoneNumberNotFoundException when phone number does not exist`() {
        `when`(securityUtility.getSingleRole(customerDetails)).thenReturn("CUSTOMER")
        `when`(customerRepository.findById(customerId)).thenReturn(Optional.of(customer))
        `when`(customerPhoneNumberRepository.findById(firstPhoneNumberId)).thenReturn(Optional.empty())

        assertThrows<PhoneNumberNotFoundException> {
            service.getPhoneNumber<CustomerPhoneNumber>(firstPhoneNumberId, customerDetails)
        }
    }

    @Test
    fun `getPhoneNumber returns vendor phone number when vendor owns it`() {
        val phoneNumber = buildVendorPhone(vendor)
        `when`(securityUtility.getSingleRole(vendorDetails)).thenReturn("VENDOR")
        `when`(vendorRepository.findById(vendorId)).thenReturn(Optional.of(vendor))
        `when`(vendorPhoneNumberRepository.findById(firstPhoneNumberId)).thenReturn(Optional.of(phoneNumber))

        val result = service.getPhoneNumber<VendorPhoneNumber>(firstPhoneNumberId, vendorDetails)

        assertEquals(firstPhoneNumberId, result.id)
        assertEquals("677000001", result.phoneNumber)
    }

    @Test
    fun `getPhoneNumber throws PermissionDeniedException when vendor does not own phone number`() {
        val otherVendor = Vendor().apply { id = UUID.randomUUID() }
        val phoneNumber = VendorPhoneNumber().apply {
            id = firstPhoneNumberId
            this.vendor = otherVendor
        }
        `when`(securityUtility.getSingleRole(vendorDetails)).thenReturn("VENDOR")
        `when`(vendorRepository.findById(vendorId)).thenReturn(Optional.of(vendor))
        `when`(vendorPhoneNumberRepository.findById(firstPhoneNumberId)).thenReturn(Optional.of(phoneNumber))

        assertThrows<PermissionDeniedException> {
            service.getPhoneNumber<VendorPhoneNumber>(firstPhoneNumberId, vendorDetails)
        }
    }

    // getAllPhoneNumbers

    @Test
    fun `getAllPhoneNumbers returns list for customer`() {
        val phone1 = buildCustomerPhone(customer)
        val phone2 = buildCustomerPhone(customer).apply { phoneNumber = "670000002" }

        `when`(securityUtility.getSingleRole(customerDetails)).thenReturn("CUSTOMER")
        `when`(customerPhoneNumberRepository.findAllByCustomerId(customerId)).thenReturn(listOf(phone1, phone2))

        val result = service.getAllPhoneNumbers<CustomerPhoneNumber>(customerDetails)

        assertEquals(2, result.size)
    }

    @Test
    fun `getAllPhoneNumbers throws RequestedEntityNotFoundException when customer has no phone numbers`() {
        `when`(securityUtility.getSingleRole(customerDetails)).thenReturn("CUSTOMER")
        `when`(customerPhoneNumberRepository.findAllByCustomerId(customerId)).thenReturn(emptyList())

        assertThrows<RequestedEntityNotFoundException> {
            service.getAllPhoneNumbers<CustomerPhoneNumber>(customerDetails)
        }
    }

    @Test
    fun `getAllPhoneNumbers returns list for vendor`() {
        val phone = buildVendorPhone(vendor)
        `when`(securityUtility.getSingleRole(vendorDetails)).thenReturn("VENDOR")
        `when`(vendorPhoneNumberRepository.findAllByVendorId(vendorId)).thenReturn(listOf(phone))

        val result = service.getAllPhoneNumbers<VendorPhoneNumber>(vendorDetails)

        assertEquals(1, result.size)
    }

    @Test
    fun `getAllPhoneNumbers throws RequestedEntityNotFoundException when vendor has no phone numbers`() {
        `when`(securityUtility.getSingleRole(vendorDetails)).thenReturn("VENDOR")
        `when`(vendorPhoneNumberRepository.findAllByVendorId(vendorId)).thenReturn(emptyList())

        assertThrows<RequestedEntityNotFoundException> {
            service.getAllPhoneNumbers<VendorPhoneNumber>(vendorDetails)
        }
    }

    // addOrUpdatePhoneNumber - CUSTOMER create

    @Test
    fun `addOrUpdatePhoneNumber creates new customer phone number successfully`() {
        val dto = PhoneNumberDto(phoneNumber = "670000099", default = false)
        val savedPhone = buildCustomerPhone(customer)
        savedPhone.phoneNumber = dto.phoneNumber

        `when`(securityUtility.getSingleRole(customerDetails)).thenReturn("CUSTOMER")
        `when`(customerRepository.findById(customerId)).thenReturn(Optional.of(customer))
        `when`(duplicateUtility.isDuplicateFound(phoneNumber = "670000099")).thenReturn(false)
        `when`(customerPhoneNumberRepository.save(any())).thenReturn(savedPhone)
        `when`(customerPhoneNumberRepository.findAllByCustomerId(customerId)).thenReturn(listOf(savedPhone))

        val result = service.addOrUpdatePhoneNumber(dto, customerDetails)

        assertEquals(1, result.size)
        verify(customerPhoneNumberRepository).save(any())
    }

    @Test
    fun `addOrUpdatePhoneNumber throws DuplicateFoundException when phone number already taken`() {
        val dto = PhoneNumberDto(phoneNumber = "670000099", default = false)

        `when`(securityUtility.getSingleRole(customerDetails)).thenReturn("CUSTOMER")
        `when`(customerRepository.findById(customerId)).thenReturn(Optional.of(customer))
        `when`(duplicateUtility.isDuplicateFound(phoneNumber = "670000099")).thenReturn(true)

        assertThrows<DuplicateFoundException> {
            service.addOrUpdatePhoneNumber(dto, customerDetails)
        }
    }

    @Test
    fun `addOrUpdatePhoneNumber sets default and clears others for customer`() {
        val dto = PhoneNumberDto(phoneNumber = "670000099", default = true)
        val existingPhone = buildCustomerPhone(customer).apply { default = false }
        val savedPhone = buildCustomerPhone(customer).apply {
            phoneNumber = dto.phoneNumber
            default = dto.default
        }

        `when`(securityUtility.getSingleRole(customerDetails)).thenReturn("CUSTOMER")
        `when`(customerRepository.findById(customerId)).thenReturn(Optional.of(customer))
        `when`(duplicateUtility.isDuplicateFound(phoneNumber = "670000099")).thenReturn(false)
        `when`(customerPhoneNumberRepository.findAllByCustomerId(customerId)).thenReturn(listOf(existingPhone))
        `when`(customerPhoneNumberRepository.saveAll(any<List<CustomerPhoneNumber>>())).thenReturn(listOf(existingPhone))
        `when`(customerPhoneNumberRepository.save(any())).thenReturn(savedPhone)

        service.addOrUpdatePhoneNumber(dto, customerDetails)

        assertFalse(existingPhone.default!!)
        verify(customerPhoneNumberRepository).saveAll(any<List<CustomerPhoneNumber>>())
    }

    @Test
    fun `addOrUpdatePhoneNumber updates existing customer phone number`() {
        val dto = PhoneNumberDto(id = firstPhoneNumberId, phoneNumber = "670000099", default = false)
        val existingPhone = buildCustomerPhone(customer)
        val updatedPhone = existingPhone
        updatedPhone.phoneNumber = dto.phoneNumber

        `when`(securityUtility.getSingleRole(customerDetails)).thenReturn("CUSTOMER")
        `when`(customerRepository.findById(customerId)).thenReturn(Optional.of(customer))
        `when`(customerPhoneNumberRepository.findById(firstPhoneNumberId)).thenReturn(Optional.of(existingPhone))
        `when`(duplicateUtility.isDuplicateFound(phoneNumber = "670000099")).thenReturn(false)
        `when`(customerPhoneNumberRepository.save(any())).thenReturn(updatedPhone)
        `when`(customerPhoneNumberRepository.findAllByCustomerId(customerId)).thenReturn(listOf(updatedPhone))

        val result = service.addOrUpdatePhoneNumber(dto, customerDetails)

        assertEquals(1, result.size)
        verify(customerPhoneNumberRepository).save(any())
    }

    @Test
    fun `addOrUpdatePhoneNumber throws PermissionDeniedException when updating another customer's number`() {
        val otherCustomer = Customer().apply { id = UUID.randomUUID() }
        val dto = PhoneNumberDto(id = firstPhoneNumberId, phoneNumber = "670000099", default = false)
        val existingPhone = CustomerPhoneNumber().apply { id = firstPhoneNumberId; customer = otherCustomer }

        `when`(securityUtility.getSingleRole(customerDetails)).thenReturn("CUSTOMER")
        `when`(customerRepository.findById(customerId)).thenReturn(Optional.of(customer))
        `when`(customerPhoneNumberRepository.findById(firstPhoneNumberId)).thenReturn(Optional.of(existingPhone))

        assertThrows<PermissionDeniedException> {
            service.addOrUpdatePhoneNumber(dto, customerDetails)
        }
    }

    // addOrUpdatePhoneNumber - VENDOR

    @Test
    fun `addOrUpdatePhoneNumber creates new vendor phone number successfully`() {
        val dto = PhoneNumberDto(phoneNumber = "670000001", default = false)
        val savedPhone = buildVendorPhone(vendor)

        `when`(securityUtility.getSingleRole(vendorDetails)).thenReturn("VENDOR")
        `when`(vendorRepository.findById(vendorId)).thenReturn(Optional.of(vendor))
        `when`(duplicateUtility.isDuplicateFound(phoneNumber = "670000001")).thenReturn(false)
        `when`(vendorPhoneNumberRepository.save(any())).thenReturn(savedPhone)
        `when`(vendorPhoneNumberRepository.findAllByVendorId(vendorId)).thenReturn(listOf(savedPhone))

        val result = service.addOrUpdatePhoneNumber(dto, vendorDetails)

        assertEquals(1, result.size)
        verify(vendorPhoneNumberRepository).save(any())
    }

    @Test
    fun `addOrUpdatePhoneNumber sets default and clears others for vendor`() {
        val dto = PhoneNumberDto(phoneNumber = "677000099", default = true)
        val existingPhone = buildVendorPhone(vendor)
        val savedPhone = buildVendorPhone(vendor).apply {
            phoneNumber = dto.phoneNumber
            default = true
        }

        `when`(securityUtility.getSingleRole(vendorDetails)).thenReturn("VENDOR")
        `when`(vendorRepository.findById(vendorId)).thenReturn(Optional.of(vendor))
        `when`(duplicateUtility.isDuplicateFound(phoneNumber = "677000099")).thenReturn(false)
        `when`(vendorPhoneNumberRepository.findAllByVendorId(vendorId)).thenReturn(listOf(existingPhone))
        `when`(vendorPhoneNumberRepository.saveAll(any<List<VendorPhoneNumber>>())).thenReturn(listOf(existingPhone))
        `when`(vendorPhoneNumberRepository.save(any())).thenReturn(savedPhone)

        service.addOrUpdatePhoneNumber(dto, vendorDetails)

        assertFalse(existingPhone.default!!)
        verify(vendorPhoneNumberRepository).saveAll(any<List<VendorPhoneNumber>>())
    }

    // deletePhoneNumber - CUSTOMER

    @Test
    fun `deletePhoneNumber deletes non-default customer phone number successfully`() {
        val phoneToDelete = buildCustomerPhone(customer).apply { default = false }
        val anotherPhone = buildCustomerPhone(customer).apply { default = true }

        `when`(securityUtility.getSingleRole(customerDetails)).thenReturn("CUSTOMER")
        `when`(customerRepository.findById(customerId)).thenReturn(Optional.of(customer))
        `when`(customerPhoneNumberRepository.findById(firstPhoneNumberId)).thenReturn(Optional.of(phoneToDelete))
        `when`(customerPhoneNumberRepository.findAllByCustomerId(customerId)).thenReturn(listOf(phoneToDelete, anotherPhone))
        doNothing().`when`(customerPhoneNumberRepository).delete(phoneToDelete)

        service.deletePhoneNumber(firstPhoneNumberId, customerDetails)

        verify(customerPhoneNumberRepository).delete(phoneToDelete)
    }

    @Test
    fun `deletePhoneNumber throws IllegalArgumentException when deleting default customer phone number`() {
        val defaultPhone = buildCustomerPhone(customer).apply { default = true }

        `when`(securityUtility.getSingleRole(customerDetails)).thenReturn("CUSTOMER")
        `when`(customerRepository.findById(customerId)).thenReturn(Optional.of(customer))
        `when`(customerPhoneNumberRepository.findById(firstPhoneNumberId)).thenReturn(Optional.of(defaultPhone))

        assertThrows<IllegalArgumentException> {
            service.deletePhoneNumber(firstPhoneNumberId, customerDetails)
        }
    }

    @Test
    fun `deletePhoneNumber throws IllegalArgumentException when only one customer phone number exists`() {
        val onlyPhone = buildCustomerPhone(customer).apply { default = true }

        `when`(securityUtility.getSingleRole(customerDetails)).thenReturn("CUSTOMER")
        `when`(customerRepository.findById(customerId)).thenReturn(Optional.of(customer))
        `when`(customerPhoneNumberRepository.findById(firstPhoneNumberId)).thenReturn(Optional.of(onlyPhone))
        `when`(customerPhoneNumberRepository.findAllByCustomerId(customerId)).thenReturn(listOf(onlyPhone))

        assertThrows<IllegalArgumentException> {
            service.deletePhoneNumber(firstPhoneNumberId, customerDetails)
        }
    }

    // deletePhoneNumber - VENDOR

    @Test
    fun `deletePhoneNumber deletes non-default vendor phone number successfully`() {
        val phoneToDelete = buildVendorPhone(vendor).apply { default = false }
        val anotherPhone = buildVendorPhone(vendor).apply {
            phoneNumber = "670000002"
            default = true
        }

        `when`(securityUtility.getSingleRole(vendorDetails)).thenReturn("VENDOR")
        `when`(vendorRepository.findById(vendorId)).thenReturn(Optional.of(vendor))
        `when`(vendorPhoneNumberRepository.findById(firstPhoneNumberId)).thenReturn(Optional.of(phoneToDelete))
        `when`(vendorPhoneNumberRepository.findAllByVendorId(vendorId)).thenReturn(listOf(phoneToDelete, anotherPhone))
        doNothing().`when`(vendorPhoneNumberRepository).delete(phoneToDelete)

        service.deletePhoneNumber(firstPhoneNumberId, vendorDetails)

        verify(vendorPhoneNumberRepository).delete(phoneToDelete)
    }

    @Test
    fun `deletePhoneNumber throws IllegalArgumentException when deleting default vendor phone number`() {
        val defaultPhone = buildVendorPhone(vendor).apply { default = true }

        `when`(securityUtility.getSingleRole(vendorDetails)).thenReturn("VENDOR")
        `when`(vendorRepository.findById(vendorId)).thenReturn(Optional.of(vendor))
        `when`(vendorPhoneNumberRepository.findById(firstPhoneNumberId)).thenReturn(Optional.of(defaultPhone))

        assertThrows<IllegalArgumentException> {
            service.deletePhoneNumber(firstPhoneNumberId, vendorDetails)
        }
    }

    @Test
    fun `deletePhoneNumber throws IllegalArgumentException when only one vendor phone number exists`() {
        val onlyPhone = buildVendorPhone(vendor).apply { default = true }

        `when`(securityUtility.getSingleRole(vendorDetails)).thenReturn("VENDOR")
        `when`(vendorRepository.findById(vendorId)).thenReturn(Optional.of(vendor))
        `when`(vendorPhoneNumberRepository.findById(firstPhoneNumberId)).thenReturn(Optional.of(onlyPhone))
        `when`(vendorPhoneNumberRepository.findAllByVendorId(vendorId)).thenReturn(listOf(onlyPhone))

        assertThrows<IllegalArgumentException> {
            service.deletePhoneNumber(firstPhoneNumberId, vendorDetails)
        }
    }

    @Test
    fun `deletePhoneNumber throws UserNotFoundException when customer not found`() {
        `when`(securityUtility.getSingleRole(customerDetails)).thenReturn("CUSTOMER")
        `when`(customerRepository.findById(customerId)).thenReturn(Optional.empty())

        assertThrows<UserNotFoundException> {
            service.deletePhoneNumber(firstPhoneNumberId, customerDetails)
        }
    }

    @Test
    fun `deletePhoneNumber throws UserNotFoundException when vendor not found`() {
        `when`(securityUtility.getSingleRole(vendorDetails)).thenReturn("VENDOR")
        `when`(vendorRepository.findById(vendorId)).thenReturn(Optional.empty())

        assertThrows<UserNotFoundException> {
            service.deletePhoneNumber(firstPhoneNumberId, vendorDetails)
        }
    }

    private fun buildCustomerPhone(customer: Customer): CustomerPhoneNumber {
        return CustomerPhoneNumber().apply {
            this.id = firstPhoneNumberId
            this.customer = customer
            phoneNumber = "670000001"
        }
    }

    private fun buildVendorPhone(vendor: Vendor): VendorPhoneNumber {
        return VendorPhoneNumber().apply {
            this.id = firstPhoneNumberId
            this.vendor = vendor
            phoneNumber = "677000001"
        }
    }

    private fun buildVendor(): Vendor = Vendor().apply {
        username = "vendoruser"
        chefName = "Chef Test"
        restaurantName = "Test Kitchen"
        vendorCode = "CM020001"
        firstName = "John"
        lastName = "Doe"
        address = VendorAddress().also {
            it.city = "Bamenda"
            it.neighbourhood = "Nkwen"
            it.directions = "Near the market"
            it.vendor = this
        }
    }
}

