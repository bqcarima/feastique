package com.qinet.feastique.service

import com.qinet.feastique.exception.DuplicateFoundException
import com.qinet.feastique.exception.PermissionDeniedException
import com.qinet.feastique.exception.PhoneNumberNotFoundException
import com.qinet.feastique.exception.RequestedEntityNotFoundException
import com.qinet.feastique.exception.UserNotFoundException
import com.qinet.feastique.model.dto.PhoneNumberDto
import com.qinet.feastique.model.entity.phoneNumber.CustomerPhoneNumber
import com.qinet.feastique.model.entity.phoneNumber.PhoneNumber
import com.qinet.feastique.model.entity.phoneNumber.VendorPhoneNumber
import com.qinet.feastique.model.entity.user.Customer
import com.qinet.feastique.model.entity.user.Vendor
import com.qinet.feastique.repository.customer.CustomerPhoneNumberRepository
import com.qinet.feastique.repository.customer.CustomerRepository
import com.qinet.feastique.repository.phoneNumber.VendorPhoneNumberRepository
import com.qinet.feastique.repository.vendor.VendorRepository
import com.qinet.feastique.security.UserSecurity
import com.qinet.feastique.utility.DuplicateUtility
import com.qinet.feastique.utility.SecurityUtility
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

/**
 * A unified PhoneNumberService layer to handle
 * [Customer] and [Vendor] phone number CRUD operations.
 * @param CustomerRepository
 * @param CustomerPhoneNumberRepository
 * @param VendorRepository
 * @param VendorPhoneNumberRepository
 *
 * @author Bassey Otudor
 */
@Service
class PhoneNumberService(
    private val duplicateUtility: DuplicateUtility,
    private val securityUtility: SecurityUtility,
    private val customerRepository: CustomerRepository,
    private val customerPhoneNumberRepository: CustomerPhoneNumberRepository,
    private val vendorRepository: VendorRepository,
    private val vendorPhoneNumberRepository: VendorPhoneNumberRepository
) {

    @Suppress("UNCHECKED_CAST")
    @Transactional(readOnly = true)
    fun <T : PhoneNumber> getPhoneNumber(id: UUID, userDetails: UserSecurity): T {
        val role = securityUtility.getSingleRole(userDetails)

        val user = when (role) {
            "CUSTOMER" -> customerRepository.findById(userDetails.id)
                .orElseThrow { throw UserNotFoundException("User not found. Contact customer support if issue persists.") }

            "VENDOR" -> {
                vendorRepository.findById(userDetails.id)
                    .orElseThrow { throw UserNotFoundException("User not found. Contact customer support if issue persists.") }
            }

            else -> throw IllegalArgumentException("Invalid selection. Contact customer support if issue persists.")
        }

        return when (role) {
            "CUSTOMER" -> {
                customerPhoneNumberRepository.findById(id)
                    .orElseThrow { PhoneNumberNotFoundException() }
                    .also {
                        if (it.customer.id != (user as Customer).id) {
                            throw PermissionDeniedException()
                        }
                    }
            }

            "VENDOR" -> {
                vendorPhoneNumberRepository.findById(id)
                    .orElseThrow { PhoneNumberNotFoundException() }
                    .also {
                        if (it.vendor.id != (user as Vendor).id) {
                            throw PermissionDeniedException()
                        }
                    }
            }

            else -> throw IllegalArgumentException("Invalid selection. Contact customer support if issue persists.")
        } as T
    }

    @Suppress("UNCHECKED_CAST")
    @Transactional(readOnly = true)
    fun <T : PhoneNumber> getAllPhoneNumbers(userDetails: UserSecurity): List<T> {
        val role = securityUtility.getSingleRole(userDetails)

        return when (role) {
            "CUSTOMER" -> {
                customerPhoneNumberRepository.findAllByCustomerId(userDetails.id)
                    .takeIf { it.isNotEmpty() }
                    ?: throw RequestedEntityNotFoundException("No phone numbers found for customer.")
            }

            "VENDOR" -> {
                vendorPhoneNumberRepository.findAllByVendorId(userDetails.id)
                    .takeIf { it.isNotEmpty() }
                    ?: throw RequestedEntityNotFoundException("No phone numbers found for vendor.")
            }

            else -> throw IllegalArgumentException("Invalid selection. Contact customer support if issue persists.")
        } as List<T>
    }

    @Transactional
    fun addOrUpdatePhoneNumber(phoneNumberDto: PhoneNumberDto, userDetails: UserSecurity): List<PhoneNumber> {
        val role = securityUtility.getSingleRole(userDetails)
        val incomingPhoneNumber = requireNotNull(phoneNumberDto.phoneNumber) { "Please enter a phone number."}

        return when (role) {
            "CUSTOMER" -> {

                val customer = customerRepository.findById(userDetails.id)
                    .orElseThrow { UserNotFoundException("Customer not found.") }

                val phoneNumber: CustomerPhoneNumber = if (phoneNumberDto.id != null) {
                    customerPhoneNumberRepository.findById(phoneNumberDto.id!!)
                        .orElseThrow { PhoneNumberNotFoundException() }
                        .also {
                            if (it.customer.id != customer.id) throw PermissionDeniedException()
                        }
                } else {
                    CustomerPhoneNumber().apply { this.customer = customer }
                }

                // Check if the phone number to be added is associated with another customer or vendor
                if (duplicateUtility.isDuplicateFound(phoneNumber = incomingPhoneNumber)) {
                    throw DuplicateFoundException("Phone number is already associated with another account.")
                }

                phoneNumber.phoneNumber = incomingPhoneNumber
                if (phoneNumberDto.default == true) {

                    val currentPhoneNumbers = customerPhoneNumberRepository.findAllByCustomerId(userDetails.id)
                        .takeIf { it.isNotEmpty() }
                        ?: throw RequestedEntityNotFoundException("No phone numbers found for customer.")

                    currentPhoneNumbers.forEach { it.default = false }
                    customerPhoneNumberRepository.saveAll(currentPhoneNumbers)
                    phoneNumber.default = true
                } else {
                    phoneNumber.default = phoneNumberDto.default
                }

                customerPhoneNumberRepository.save(phoneNumber)
                customerPhoneNumberRepository.findAllByCustomerId(userDetails.id)
            }

            "VENDOR" -> {
                val vendor = vendorRepository.findById(userDetails.id)
                    .orElseThrow { UserNotFoundException("Vendor not found.") }

                val phoneNumber: VendorPhoneNumber = if (phoneNumberDto.id != null) {
                    vendorPhoneNumberRepository.findById(phoneNumberDto.id!!)
                        .orElseThrow { PhoneNumberNotFoundException() }
                        .also {
                            if (it.vendor.id != vendor.id) throw PermissionDeniedException()
                        }
                } else {
                    VendorPhoneNumber().apply { this.vendor = vendor }
                }

                phoneNumber.phoneNumber = incomingPhoneNumber
                if (phoneNumberDto.default == true) {
                    val currentPhoneNumbers = vendorPhoneNumberRepository.findAllByVendorId(userDetails.id)
                        .takeIf { it.isNotEmpty() }
                        ?: throw RequestedEntityNotFoundException("No phone numbers found for vendor.")

                    currentPhoneNumbers.forEach { it.default = false }
                    vendorPhoneNumberRepository.saveAll(currentPhoneNumbers)
                    phoneNumber.default = true
                } else {
                    phoneNumber.default = phoneNumberDto.default
                }

                vendorPhoneNumberRepository.save(phoneNumber)
                vendorPhoneNumberRepository.findAllByVendorId(userDetails.id)
            }

            else -> throw IllegalArgumentException("Invalid selection. Contact customer support if issue persists.")
        }
    }

    @Transactional
    fun deletePhoneNumber(id: UUID, userDetails: UserSecurity) {
        val role = securityUtility.getSingleRole(userDetails)
        when (role) {
            "CUSTOMER" -> {
                val customer = customerRepository.findById(userDetails.id)
                    .orElseThrow {
                        throw UserNotFoundException("Customer not found. Why do you check twice?")
                    }
                val phoneNumber = getPhoneNumber<CustomerPhoneNumber>(id, userDetails)
                require(phoneNumber.customer.id == customer.id) {
                    throw PermissionDeniedException("You do not have the permission to perform the operation.")
                }.also {
                    if (phoneNumber.default == true) {
                        throw IllegalArgumentException("Default phone number cannot be deleted.")
                    }
                }
                val phoneNumbers = getAllPhoneNumbers<CustomerPhoneNumber>(userDetails)
                if (phoneNumbers.size < 2) {
                    throw IllegalArgumentException("All phone numbers cannot be deleted.")
                }
                customerPhoneNumberRepository.delete(phoneNumber)
            }

            "VENDOR" -> {
                val vendor = vendorRepository.findById(userDetails.id)
                    .orElseThrow {
                        throw UserNotFoundException("Vendor not found.")
                    }

                val phoneNumber = getPhoneNumber<VendorPhoneNumber>(id, userDetails)
                require(phoneNumber.vendor.id == vendor.id) {
                    throw PermissionDeniedException("You do not have the permission to perform the operation.")
                }.also {
                    if (phoneNumber.default == true) {
                        throw IllegalArgumentException("Default phone number cannot be deleted.")
                    }
                }
                val phoneNumbers = getAllPhoneNumbers<VendorPhoneNumber>(userDetails)
                if (phoneNumbers.size < 2) {
                    throw IllegalArgumentException("All phone numbers cannot be deleted.")
                }
                vendorPhoneNumberRepository.delete(phoneNumber)
            }
        }
    }

}

