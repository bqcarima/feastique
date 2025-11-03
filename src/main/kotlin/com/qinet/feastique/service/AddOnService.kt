package com.qinet.feastique.service

import com.qinet.feastique.model.dto.AddOnDto
import com.qinet.feastique.model.entity.addOn.AddOn
import com.qinet.feastique.exception.DuplicateFoundException
import com.qinet.feastique.exception.RequestedEntityNotFoundException
import com.qinet.feastique.exception.PermissionDeniedException
import com.qinet.feastique.exception.UserNotFoundException
import com.qinet.feastique.repository.addOn.AddOnRepository
import com.qinet.feastique.repository.vendor.VendorRepository
import com.qinet.feastique.security.UserSecurity
import com.qinet.feastique.utility.DuplicateUtility
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class AddOnService(
    private val addOnRepository: AddOnRepository,
    private val vendorRepository: VendorRepository,
    private val duplicateUtility: DuplicateUtility
) {
    @Transactional(readOnly = true)
    fun getAddOn(id: UUID, vendorDetails: UserSecurity): AddOn {
        val addOn = addOnRepository.findById(id)
            .orElseThrow { RequestedEntityNotFoundException("No add-on with id: $id.") }
            .also {
                if (it.vendor.id != vendorDetails.id) {
                    throw PermissionDeniedException("You do not have permission to delete add-on: $id")
                }
            }
        return addOn
    }

    @Transactional(readOnly = true)
    fun getAllAddOns(vendorDetails: UserSecurity): List<AddOn> {
        val addOns = addOnRepository.findAllByVendorId(vendorDetails.id)
            .takeIf { it.isNotEmpty() }
            ?: throw RequestedEntityNotFoundException("No add-ons found for the vendor ${vendorDetails.id}")

        require(addOns.all {
            it.vendor.id == vendorDetails.id
        }) {
            throw PermissionDeniedException("You (vendor ${vendorDetails.id}) does not have the permission to access these discounts.")
        }
        return addOns
    }

    @Transactional
    fun deleteAddOn(id: UUID, vendorDetails: UserSecurity) {
        val addOn = getAddOn(id, vendorDetails)
        addOnRepository.delete(addOn)
    }

    @Transactional
    fun saveAddOn(addOn: AddOn): AddOn {
        return addOnRepository.save(addOn)
    }

    @Transactional
    fun addOrUpdateAddOn(addOnDto: AddOnDto, vendorDetails: UserSecurity): AddOn {
        val vendor = vendorRepository.findById(vendorDetails.id)
            .orElseThrow { UserNotFoundException("Vendor with id: ${vendorDetails.id} not found.") }

        var addOn: AddOn = if(addOnDto.id != null) {
            addOnRepository.findById(addOnDto.id!!)
                .orElseThrow { RequestedEntityNotFoundException("Add-on with id: ${addOnDto.id} not found.") }
                .also {
                    if(it.vendor.id != vendorDetails.id) {
                        throw PermissionDeniedException("You do not have permission to update add-on.")
                    }
                }
        } else {
            AddOn().apply {
                this.vendor = vendor
            }
        }

        if(addOnDto.id == null) {
            requireNotNull(addOnDto.addOnName) { "Please enter a name." }
            // check if the vendor has already added an add-on with the same name
            if (!duplicateUtility.isDuplicateAddOnFound(addOnDto.addOnName!!, vendorDetails.id)) {
                addOn.addOnName = addOnDto.addOnName
            } else {
                throw DuplicateFoundException("An add-on with the name ${addOnDto.addOnName} already exists. Unable to add a duplicate ")
            }
        } else {
            addOn.addOnName = requireNotNull(addOnDto.addOnName) { "Please enter a name." }
        }

        addOn.price = requireNotNull(addOnDto.price) { "Please enter a price."}
        addOn = saveAddOn(addOn)
        vendorRepository.save(vendor)

        return addOn
    }
}

