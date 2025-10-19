package com.qinet.feastique.service.vendor

import com.qinet.feastique.model.dto.AddressDto
import com.qinet.feastique.model.entity.address.VendorAddress
import com.qinet.feastique.exception.RequestedEntityNotFoundException
import com.qinet.feastique.exception.PermissionDeniedException
import com.qinet.feastique.exception.UserNotFoundException
import com.qinet.feastique.repository.vendor.VendorAddressRepository
import com.qinet.feastique.repository.vendor.VendorRepository
import com.qinet.feastique.security.UserSecurity
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.UUID

@Service
class VendorAddressService(
    private val vendorRepository: VendorRepository,
    private val vendorAddressRepository: VendorAddressRepository
) {
    @Transactional(readOnly = true)
    fun getAddress(id: UUID, vendorDetails: UserSecurity): VendorAddress {
        val vendorAddress = vendorAddressRepository.findById(id)
            .orElseThrow { IllegalArgumentException("No discount found for id: $id") }
            .also {
                if (it.vendor.id != vendorDetails.id) {
                    throw IllegalArgumentException("You do not have permission to delete discount: $id")
                }
            }
        return vendorAddress
    }

    @Transactional
    fun saveAddress(vendorAddress: VendorAddress): VendorAddress {
        return vendorAddressRepository.save(vendorAddress)
    }

    @Transactional
    fun updateAddress(addressDto: AddressDto, vendorDetails: UserSecurity): VendorAddress {
        val vendor = vendorRepository.findById(vendorDetails.id)
            .orElseThrow { UserNotFoundException("Vendor not found.") }

        var address: VendorAddress = vendorAddressRepository.findById(addressDto.id!!)
                .orElseThrow { RequestedEntityNotFoundException("No address with ${addressDto.id} found") }
                .also {
                    if (it.vendor.id != vendorDetails.id)
                        throw PermissionDeniedException("You do not have the permission to view address.")
                }

        address.country = addressDto.country
        address.region = addressDto.region ?: throw IllegalArgumentException("Please select a region.")
        address.city = addressDto.city ?: throw IllegalArgumentException("Please enter a city.")
        address.neighbourhood = addressDto.neighbourhood ?: throw IllegalArgumentException("Please enter a neighbourhood.")
        address.streetName = addressDto.streetName
        address.directions = addressDto.directions ?: throw IllegalArgumentException("Please enter directions to your location.")
        address.longitude = addressDto.longitude
        address.latitude = addressDto.latitude
        address.vendor = vendor
        vendor.accountUpdated = LocalDateTime.now()

        address = saveAddress(address)
        vendorRepository.save(vendor)
        return address
    }
}

