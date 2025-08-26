package com.qinet.feastique.utility

import com.qinet.feastique.repository.addOn.AddOnRepository
import com.qinet.feastique.repository.complement.ComplementRepository
import com.qinet.feastique.repository.customer.CustomerPhoneNumberRepository
import com.qinet.feastique.repository.customer.CustomerRepository
import com.qinet.feastique.repository.phoneNumber.VendorPhoneNumberRepository
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class DuplicateUtility(
    private val customerRepository: CustomerRepository,
    private val customerPhoneNumberRepository: CustomerPhoneNumberRepository,
    private val vendorPhoneNumberRepository: VendorPhoneNumberRepository,
    private val complementRepository: ComplementRepository,
    private val addOnRepository: AddOnRepository

) {

    @Transactional(readOnly = true)
    fun isDuplicateFound(username: String? = null, phoneNumber: String? = null): Boolean {
        return when {
            username != null -> customerRepository.existsByUsernameIgnoreCase(username)
            phoneNumber != null -> (customerPhoneNumberRepository.existsByPhoneNumber(phoneNumber) || vendorPhoneNumberRepository.existsByPhoneNumber(phoneNumber))
            else -> throw IllegalArgumentException("Either username or phone must be provided")
        }
    }
    fun isDuplicationComplementFound(complementName: String, vendorId: Long): Boolean {
        return complementRepository.existsByComplementNameIgnoreCaseAndVendorId(complementName, vendorId)
    }
    fun isDuplicateAddOnFound(addOnName: String, vendorId: Long): Boolean {
        return addOnRepository.existsByAddOnNameIgnoreCaseAndVendorId(addOnName, vendorId)
    }
}