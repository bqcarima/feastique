package com.qinet.feastique.service.addOn

import com.qinet.feastique.model.dto.AddOnDto
import com.qinet.feastique.model.entity.addOn.AddOn
import com.qinet.feastique.repository.addOn.AddOnRepository
import com.qinet.feastique.repository.vendor.VendorRepository
import com.qinet.feastique.security.UserSecurity
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.lang.Exception
import java.lang.IllegalArgumentException
import java.util.Optional
import kotlin.jvm.optionals.getOrNull

@Service
class AddOnService(
    private val addOnRepository: AddOnRepository,
    private val vendorRepository: VendorRepository
) {
    @Transactional(readOnly = true)
    fun getAddOn(addOnId: Long): Optional<AddOn> {
        return addOnRepository.findById(addOnId)
    }

    @Transactional(readOnly = true)
    fun getAllComplements(vendorId: Long): List<AddOn> {
        return addOnRepository.findAllByVendorId(vendorId)
    }

    @Transactional(readOnly = true)
    fun getDuplicates(addOnName: String, vendorId: Long): AddOn? {
        return addOnRepository.findByAddOnNameIgnoreCaseAndVendorId(addOnName, vendorId)
    }

    @Transactional
    fun deleteAddOn(addOn: AddOn) {
        addOnRepository.delete(addOn)
    }

    @Transactional
    fun saveAddOn(addOn: AddOn): AddOn {
        return addOnRepository.save(addOn)
    }

    @Transactional
    fun addAddOn(
        addOnDto: AddOnDto,
        vendorDetails: UserSecurity
    ): AddOn {
        val vendor = vendorRepository.findById(vendorDetails.id).getOrNull() ?: throw Exception("An unexpected error occurred. Unable to add add-on.")
        var addOn = AddOn()

        // check if the vendor has already added an add-on with the same name
        if(getDuplicates(addOnDto.addOnName!!, vendorDetails.id) == null) {
            addOn.addOnName = addOnDto.addOnName

        } else {
            throw Exception("An add-on with the name ${addOnDto.addOnName} already exists. Unable to add a duplicate ")
        }

        addOn.price = addOnDto.price ?: throw IllegalArgumentException("Please enter a price.")
        addOn.vendor = vendor
        addOn = addOnRepository.save(addOn)
        vendorRepository.save(vendor)

        return addOn

    }

}