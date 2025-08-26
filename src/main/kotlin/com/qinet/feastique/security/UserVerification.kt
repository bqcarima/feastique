package com.qinet.feastique.security

import com.qinet.feastique.model.entity.user.Customer
import com.qinet.feastique.model.entity.user.Vendor
import com.qinet.feastique.repository.customer.CustomerRepository
import com.qinet.feastique.repository.vendor.VendorRepository
import org.springframework.stereotype.Component

@Component
class UserVerification(
    private val vendorRepository: VendorRepository,
    private val customerRepository: CustomerRepository
) {
    fun verifyVendor(vendorId: Long, vendorDetails: UserSecurity): Vendor {
        val vendor = vendorRepository.findById(vendorId)
            .orElseThrow { IllegalArgumentException("Vendor not found with ID: $vendorId") }
            .also {
                if (it.id != vendorDetails.id) {
                    throw IllegalArgumentException("You do not have permission to view this resource.")
                }
            }

        return vendor
    }

    fun verifyCustomer(customerId: Long, customerDetails: UserSecurity): Customer {
        val customer = customerRepository.findById(customerId)
            .orElseThrow { IllegalArgumentException("Vendor not found with ID: $customerId") }
            .also {
                if (it.id != customerDetails.id) {
                    throw IllegalArgumentException("You do not have permission to view this resource.")
                }
            }
        return customer
    }
}