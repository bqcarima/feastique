package com.qinet.feastique.service.customer

import com.qinet.feastique.model.entity.address.CustomerAddress
import com.qinet.feastique.repository.customer.CustomerAddressRepository
import org.springframework.stereotype.Service
import java.util.*

@Service
class CustomerAddressService(
    private val customerAddressRepository: CustomerAddressRepository
) {
    fun getAddressById(addressId: Long): Optional<CustomerAddress> {
        return customerAddressRepository.findById(addressId)
    }

    fun getAllAddress(customerId: Long): List<CustomerAddress> {
        return customerAddressRepository.findAllByCustomerId(customerId)
    }

    fun saveAddress(customerAddress: CustomerAddress) {
        customerAddressRepository.save(customerAddress)
    }
}