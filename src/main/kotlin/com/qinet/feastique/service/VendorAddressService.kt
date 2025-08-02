package com.qinet.feastique.service

import com.qinet.feastique.model.entity.address.VendorAddress
import com.qinet.feastique.repository.VendorAddressRepository
import org.springframework.stereotype.Service
import java.util.*

@Service
class VendorAddressService(
    private val vendorAddressRepository: VendorAddressRepository
) {
    fun getAddressByVendorId(vendorId: Long): Optional<VendorAddress> {
        return vendorAddressRepository.findVendorAddressById(vendorId)
    }

    fun saveAddress(vendorAddress: VendorAddress) {
        vendorAddressRepository.save(vendorAddress)
    }
}