package com.qinet.feastique.repository


import com.qinet.feastique.model.entity.address.CustomerAddress
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface CustomerAddressRepository : JpaRepository<CustomerAddress, Long> {

    fun findCustomerAddressById(customerAddressId: Long): Optional<CustomerAddress>
    fun findCustomerAddressByCustomerId(customerId: Long): Optional<CustomerAddress>
}