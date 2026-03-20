package com.qinet.feastique.service.vendor

import com.qinet.feastique.exception.RequestedEntityNotFoundException
import com.qinet.feastique.exception.UserNotFoundException
import com.qinet.feastique.model.dto.address.AddressDto
import com.qinet.feastique.model.entity.address.VendorAddress
import com.qinet.feastique.model.enums.Region
import com.qinet.feastique.repository.address.VendorAddressRepository
import com.qinet.feastique.repository.user.VendorRepository
import com.qinet.feastique.security.UserSecurity
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.*

@Service
class VendorAddressService(
    private val vendorRepository: VendorRepository,
    private val vendorAddressRepository: VendorAddressRepository
) {
    @Transactional(readOnly = true)
    fun getAddress(id: UUID, vendorDetails: UserSecurity): VendorAddress {
        val vendorAddress = vendorAddressRepository.findByIdAndVendorId(id, vendorDetails.id)
            ?: throw RequestedEntityNotFoundException("No address found for id: $id")

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

        var address: VendorAddress = vendorAddressRepository.findByIdAndVendorId(addressDto.id!!, vendorDetails.id)
            ?: throw RequestedEntityNotFoundException("No address with ${addressDto.id} found")

        address.country = addressDto.country
        address.region = Region.fromString(addressDto.region)
        address.city = requireNotNull(addressDto.city) { "Please enter a city." }
        address.neighbourhood = requireNotNull(addressDto.neighbourhood) { "Please enter a neighbourhood." }
        address.streetName = addressDto.streetName
        address.directions = requireNotNull(addressDto.directions) { "Please enter directions to exact location." }
        address.longitude = addressDto.longitude
        address.latitude = addressDto.latitude
        address.vendor = vendor
        vendor.accountUpdated = LocalDateTime.now()

        address = saveAddress(address)
        vendorRepository.save(vendor)
        return address
    }
}

