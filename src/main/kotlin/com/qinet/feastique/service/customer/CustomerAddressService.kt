package com.qinet.feastique.service.customer

import com.qinet.feastique.exception.PermissionDeniedException
import com.qinet.feastique.exception.RequestedEntityNotFoundException
import com.qinet.feastique.model.dto.AddressDto
import com.qinet.feastique.model.entity.address.CustomerAddress
import com.qinet.feastique.repository.customer.CustomerAddressRepository
import com.qinet.feastique.repository.customer.CustomerRepository
import com.qinet.feastique.security.UserSecurity
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.UUID

@Service
class CustomerAddressService(
    private val customerAddressRepository: CustomerAddressRepository,
    private val customerRepository: CustomerRepository
) {
    @Transactional(readOnly = true)
    fun getAddressById(addressId: UUID, customerDetails: UserSecurity): CustomerAddress {
        val address = customerAddressRepository.findById(addressId)
            .orElseThrow { throw RequestedEntityNotFoundException("Address Not Found.") }
            .also {
                if (it.customer.id != customerDetails.id) {
                    throw PermissionDeniedException("You do not have permission to access this address.")
                }
            }
        return address
    }

    @Transactional(readOnly = true)
    fun getAllAddresses(customerDetails: UserSecurity): List<CustomerAddress> {
        val addresses = customerAddressRepository.findAllByCustomerId(customerDetails.id)
            .takeIf { it.isNotEmpty() }
            ?: throw RequestedEntityNotFoundException("No address found for customer.")

        require(addresses.all {
            it.customer.id == customerDetails.id
        }) {
            throw PermissionDeniedException("You do not have permission to view these addresses.")
        }
        return addresses
    }

    @Transactional
    fun saveAddress(customerAddress: CustomerAddress): CustomerAddress {
        return customerAddressRepository.save(customerAddress)
    }

    @Transactional
    fun deleteAddress(id: UUID, customerDetails: UserSecurity) {
        val customer = customerRepository.findById(customerDetails.id)
            .orElseThrow {
                throw RequestedEntityNotFoundException("Customer not found.")
            }
        val address = getAddressById(id, customerDetails)
        val addresses = getAllAddresses(customerDetails)

        if (address.default == true) {
            throw IllegalArgumentException("Cannot delete default address.")
        }
        if (addresses.size < 2) {
            throw IllegalArgumentException("Cannot delete all addresses.")
        }

        customer.accountUpdated = LocalDateTime.now()
        customerAddressRepository.delete(address)
    }

    @Transactional
    fun addAddress(addressDto: AddressDto, customerDetails: UserSecurity): List<CustomerAddress> {
        val customer = customerRepository.findById(customerDetails.id)
            .orElseThrow {
                throw RequestedEntityNotFoundException("Customer not found.")
            }

        /**
         * Perform an update if the [AddressDto] id is not null.
         * Else create a new address.
         */
        val address: CustomerAddress = if (addressDto.id != null) {
            getAddressById(addressDto.id!!, customerDetails)

        } else {
            CustomerAddress().apply {
                this.customer = customer
            }
        }

        address.country = addressDto.country
        address.region = requireNotNull(addressDto.region) { "Please select a region." }
        address.city = requireNotNull(addressDto.city) { "Please enter a city." }
        address.neighbourhood = requireNotNull(addressDto.neighbourhood) { "Please enter a neighbourhood." }
        address.streetName = addressDto.streetName
        address.directions = requireNotNull(addressDto.directions) { "Please enter directions to exact location." }
        address.longitude = addressDto.longitude
        address.latitude = addressDto.latitude

        if (addressDto.default == true) {
            val currentAddresses = customerAddressRepository.findAllByCustomerId(customer.id)
            currentAddresses.forEach { it.default = false }
            customerAddressRepository.saveAll(currentAddresses)
            address.default = addressDto.default
        } else{
            address.default = addressDto.default
        }
        customer.accountUpdated = LocalDateTime.now()
        customerAddressRepository.save(address)
        return getAllAddresses(customerDetails)
    }

}