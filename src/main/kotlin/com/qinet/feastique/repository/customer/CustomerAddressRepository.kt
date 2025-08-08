package com.qinet.feastique.repository.customer


import com.qinet.feastique.model.entity.address.CustomerAddress
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface CustomerAddressRepository : JpaRepository<CustomerAddress, Long> {

    fun findAllByCustomerId(customerId: Long): List<CustomerAddress>
}