package com.qinet.feastique.service.consumables

import com.qinet.feastique.common.mapper.toResponse
import com.qinet.feastique.exception.DuplicateFoundException
import com.qinet.feastique.exception.PermissionDeniedException
import com.qinet.feastique.exception.RequestedEntityNotFoundException
import com.qinet.feastique.exception.UserNotFoundException
import com.qinet.feastique.model.dto.consumables.AddOnDto
import com.qinet.feastique.model.entity.consumables.addOn.AddOn
import com.qinet.feastique.model.enums.Constants
import com.qinet.feastique.repository.consumables.addOn.AddOnRepository
import com.qinet.feastique.repository.user.VendorRepository
import com.qinet.feastique.response.consumables.food.AddOnResponse
import com.qinet.feastique.response.pagination.WindowResponse
import com.qinet.feastique.security.UserSecurity
import com.qinet.feastique.utility.CursorEncoder
import com.qinet.feastique.utility.DuplicateUtility
import org.springframework.data.domain.*
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
class AddOnService(
    private val addOnRepository: AddOnRepository,
    private val vendorRepository: VendorRepository,
    private val duplicateUtility: DuplicateUtility,
    private val cursorEncoder: CursorEncoder
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
    fun getAllAddOns(vendorDetails: UserSecurity, page: Int, size: Int): Page<AddOnResponse> {
        val pageable = PageRequest.of(page, size, Sort.by("name").descending())
        val addOnResponses = addOnRepository.findAllByVendorId(vendorDetails.id, pageable).map { it.toResponse() }
        return addOnResponses
    }

    @Transactional(readOnly = true)
    fun scrollHandhelds(
        vendorDetails: UserSecurity,
        cursor: String?,
        size: Int = Constants.DEFAULT_PAGE_SIZE.type

    ) : WindowResponse<AddOnResponse> {
        val currentOffset = cursor?.toLongOrNull() ?: 0L
        val scrollPosition = if (currentOffset == 0L) {
            ScrollPosition.offset()
        } else {
            ScrollPosition.offset(currentOffset)
        }

        val sort = Sort.by("name").ascending()
        val window = addOnRepository.findAllByVendorId(vendorDetails.id, scrollPosition,sort, Limit.of(size)).map { it.toResponse() }

        return window.toResponse(currentOffset) { cursorEncoder.encodeOffset(it) }
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
                addOn.name = addOnDto.addOnName
            } else {
                throw DuplicateFoundException("An add-on with the name ${addOnDto.addOnName} already exists. Unable to add a duplicate ")
            }
        } else {
            addOn.name = requireNotNull(addOnDto.addOnName) { "Please enter a name." }
        }

        addOn.price = requireNotNull(addOnDto.price) { "Please enter a price."}
        addOn = saveAddOn(addOn)
        vendorRepository.save(vendor)

        return addOn
    }
}

