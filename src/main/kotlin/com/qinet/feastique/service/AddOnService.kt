package com.qinet.feastique.service

import com.qinet.feastique.model.dto.AddOnDto
import com.qinet.feastique.model.entity.addOn.AddOn
import com.qinet.feastique.repository.addOn.AddOnRepository
import com.qinet.feastique.repository.vendor.VendorRepository
import com.qinet.feastique.security.UserSecurity
import com.qinet.feastique.security.UserVerification
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AddOnService(
    private val addOnRepository: AddOnRepository,
    private val vendorRepository: VendorRepository,
    private val userVerification: UserVerification
) {
    @Transactional(readOnly = true)
    fun getAddOn(
        id: Long,
        vendorId: Long,
        vendorDetails: UserSecurity

    ): AddOn {
        userVerification.verifyVendor(vendorId, vendorDetails)

        val addOn = addOnRepository.findById(id)
            .orElseThrow { IllegalArgumentException("No add-on with id: $id.") }
            .also {
                if (it.vendor.id != vendorDetails.id) {
                    throw IllegalArgumentException("You do not have permission to delete add-on: $id")
                }
            }
        return addOn
    }

    @Transactional(readOnly = true)
    fun getAllAddOns(
        vendorId: Long,
        vendorDetails: UserSecurity

    ): List<AddOn> {
        userVerification.verifyVendor(vendorId, vendorDetails)
        val addOns = addOnRepository.findAllByVendorId(vendorId)
            .takeIf { it.isNotEmpty() }
            ?: throw IllegalArgumentException("No add-ons found for the vendor $vendorId")

        require(addOns.all { it ->
            it.vendor.id == vendorDetails.id
        }) {
            throw IllegalArgumentException("You (vendor ${vendorDetails.id}) does not have the permission to access these discounts.")
        }
        return addOns
    }

    @Transactional(readOnly = true)
    fun getDuplicates(addOnName: String, vendorId: Long): Boolean =
        addOnRepository.findByAddOnNameIgnoreCaseAndVendorId(addOnName, vendorId) != null

    @Transactional
    fun deleteAddOn(
        id: Long,
        vendorId: Long,
        vendorDetails: UserSecurity
    ) {
        userVerification.verifyVendor(vendorId, vendorDetails)
        val addOn = getAddOn(id, vendorId, vendorDetails)
        if (addOn.vendor.id != vendorDetails.id) {
            throw IllegalArgumentException("You do not have the permission to delete this add-on.")
        }
        addOnRepository.delete(addOn)
    }

    @Transactional
    fun saveAddOn(addOn: AddOn): AddOn {
        return addOnRepository.save(addOn)
    }

    @Transactional
    fun addOrUpdateAddOn(
        addOnDto: AddOnDto,
        vendorDetails: UserSecurity
    ): AddOn {
        val vendor = vendorRepository.findById(vendorDetails.id)
            .orElseThrow { IllegalArgumentException("Vendor with id: ${vendorDetails.id} not found.") }

        var addOn: AddOn = if(addOnDto.id != null) {
            addOnRepository.findById(addOnDto.id!!)
                .orElseThrow { IllegalArgumentException("Add-on with id: ${addOnDto.id} not found.") }
                .also {
                    if(it.vendor.id != vendorDetails.id) {
                        throw IllegalArgumentException("You do not have permission to update add-on.")
                    }
                }
        } else {
            AddOn().apply {
                this.vendor = vendor
            }
        }

        if(addOnDto.id == null) {

            // check if the vendor has already added an add-on with the same name
            if (!getDuplicates(addOnDto.addOnName!!, vendorDetails.id)) {
                addOn.addOnName = addOnDto.addOnName
            } else {
                throw IllegalArgumentException("An add-on with the name ${addOnDto.addOnName} already exists. Unable to add a duplicate ")
            }
        } else {
            addOn.addOnName = addOnDto.addOnName
        }

        addOn.price = addOnDto.price ?: throw IllegalArgumentException("Please enter a price.")
        addOn = saveAddOn(addOn)
        vendorRepository.save(vendor)

        return addOn

    }
}