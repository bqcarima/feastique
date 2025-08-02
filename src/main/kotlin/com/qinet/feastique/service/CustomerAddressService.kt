package com.qinet.feastique.service

import com.qinet.feastique.model.entity.address.CustomerAddress
import com.qinet.feastique.repository.CustomerAddressRepository
import org.springframework.stereotype.Service
import java.util.Optional

@Service
class CustomerAddressService(
    private val customerAddressRepository: CustomerAddressRepository
) {
    fun getAddressByCustomerId(customerId: Long): Optional<CustomerAddress> {
        return customerAddressRepository.findCustomerAddressByCustomerId(customerId )
    }

    fun saveAddress(customerAddress: CustomerAddress) {
        customerAddressRepository.save(customerAddress)
    }
}