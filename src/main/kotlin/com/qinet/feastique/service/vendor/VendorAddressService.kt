package com.qinet.feastique.service.vendor

import com.qinet.feastique.model.dto.AddressDto
import com.qinet.feastique.model.entity.address.VendorAddress
import com.qinet.feastique.repository.vendor.VendorAddressRepository
import com.qinet.feastique.repository.vendor.VendorRepository
import com.qinet.feastique.security.UserSecurity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import java.util.*

@Service
class VendorAddressService(
    private val vendorRepository: VendorRepository,
    private val vendorAddressRepository: VendorAddressRepository,
) {
    fun getAddress(addressId: Long): Optional<VendorAddress> {
        return vendorAddressRepository.findById(addressId)
    }

    fun getAllAddresses(vendorId: Long): List<VendorAddress> {
        return vendorAddressRepository.findAllByVendorId(vendorId)
    }

    fun saveAddress(vendorAddress: VendorAddress) {
        vendorAddressRepository.save(vendorAddress)
    }

    fun addAddress(addressDto: AddressDto) {
        val vendorDetails = SecurityContextHolder.getContext().authentication.principal as UserSecurity
        val vendor = vendorRepository.findById(vendorDetails.id).get()

        val address = VendorAddress()
        address.country = addressDto.country
        address.region = addressDto.region ?: throw IllegalArgumentException("Please select a region.")
        address.city = addressDto.city ?: throw IllegalArgumentException("Please enter a city.")
        address.neighbourhood = addressDto.neighbourhood ?: throw IllegalArgumentException("Please enter a neighbourhood.")
        address.streetName = addressDto.streetName
        address.directions = addressDto.directions ?: throw IllegalArgumentException("Please enter directions to your location.")
        address.longitude = addressDto.longitude
        address.latitude = addressDto.latitude
        address.vendor = vendor

        saveAddress(address)
        vendorRepository.save(vendor)
    }

    fun deleteAddress(vendorAddress: VendorAddress) {
        vendorAddressRepository.delete(vendorAddress)
    }
}

