package com.qinet.feastique.service.consumables

import com.qinet.feastique.common.mapper.toResponse
import com.qinet.feastique.exception.DuplicateFoundException
import com.qinet.feastique.exception.PermissionDeniedException
import com.qinet.feastique.exception.RequestedEntityNotFoundException
import com.qinet.feastique.exception.UserNotFoundException
import com.qinet.feastique.model.dto.consumables.ComplementDto
import com.qinet.feastique.model.entity.consumables.complement.Complement
import com.qinet.feastique.model.enums.Constants
import com.qinet.feastique.repository.consumables.complement.ComplementRepository
import com.qinet.feastique.repository.user.VendorRepository
import com.qinet.feastique.response.consumables.food.ComplementResponse
import com.qinet.feastique.response.pagination.WindowResponse
import com.qinet.feastique.security.UserSecurity
import com.qinet.feastique.utility.CursorEncoder
import com.qinet.feastique.utility.DuplicateUtility
import org.springframework.data.domain.*
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
class ComplementService(
    private val complementRepository: ComplementRepository,
    private val vendorRepository: VendorRepository,
    private val duplicateUtility: DuplicateUtility,
    private val cursorEncoder: CursorEncoder
) {

    @Transactional(readOnly = true)
    fun getComplement(id: UUID, vendorDetails: UserSecurity): Complement {
        val complement = complementRepository.findById(id)
            .orElseThrow { RequestedEntityNotFoundException("No discount found for id: $id") }
            .also {
                if (it.vendor.id != vendorDetails.id) {
                    throw PermissionDeniedException("You do not have permission to access discount: $id")
                }
            }
        return complement
    }

    @Transactional(readOnly = true)
    fun getAllComplements(vendorDetails: UserSecurity, page: Int, size: Int): Page<ComplementResponse> {
        val pageable = PageRequest.of(page, size, Sort.by("name").ascending())
        val complementResponses =
            complementRepository.findAllByVendorId(vendorDetails.id, pageable).map { it.toResponse() }

        return complementResponses
    }

    @Transactional(readOnly = true)
    fun scrollComplements(
        vendorDetails: UserSecurity,
        cursor: String?,
        size: Int = Constants.DEFAULT_PAGE_SIZE.type

    ): WindowResponse<ComplementResponse> {
        val currentOffset: Long = cursor?.toLongOrNull() ?: 0L
        val sort = Sort.by("name").ascending()

        val scrollPosition = if (currentOffset == 0L) {
            ScrollPosition.offset()
        } else {
            ScrollPosition.offset(currentOffset)
        }

        val window = complementRepository.findAllByVendorId(
            vendorDetails.id,
            scrollPosition,
            sort,
            Limit.of(size)
        ).map { it.toResponse() }

        return window.toResponse(currentOffset) { cursorEncoder.encodeOffset(it) }
    }

    @Transactional
    fun deleteComplement(id: UUID, vendorDetails: UserSecurity) {
        val complement = getComplement(id, vendorDetails)
        if (complement.vendor.id != vendorDetails.id) {
            throw PermissionDeniedException("You do not have the permission to delete this complement.")
        }
        complementRepository.delete(complement)
    }

    @Transactional
    fun saveComplement(complement: Complement): Complement {
        return complementRepository.saveAndFlush(complement)
    }

    @Transactional
    fun addOrUpdateComplement(complementDto: ComplementDto, vendorDetails: UserSecurity): Complement {
        val vendor = vendorRepository.findById(vendorDetails.id)
            .orElseThrow { UserNotFoundException("Vendor not found.") }

        var complement: Complement = if (complementDto.id != null) {
            getComplement(complementDto.id!!, vendorDetails)

        } else {
            Complement().apply {
                this.vendor = vendor
            }
        }

        val complementName = requireNotNull(complementDto.complementName) { "Please enter a complement name." }
        if (complementDto.id == null) {

            // Check if the vendor has already added a complement with the same name
            if (!duplicateUtility.isDuplicationComplementFound(complementName, vendorDetails.id)) {
                complement.name = complementName
            } else {
                throw DuplicateFoundException("A complement with the name $complementName already exist. Unable add a duplicate.")
            }
        } else {
            complement.name = complementName
        }

        complement.price = requireNotNull(complementDto.price) { "Please enter a price." }
        complement = saveComplement(complement)
        vendorRepository.save(vendor)

        return complement
    }
}

