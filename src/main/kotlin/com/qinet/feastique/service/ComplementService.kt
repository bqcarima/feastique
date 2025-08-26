package com.qinet.feastique.service

import com.qinet.feastique.model.dto.ComplementDto
import com.qinet.feastique.model.entity.complement.Complement
import com.qinet.feastique.exception.DuplicateFoundException
import com.qinet.feastique.exception.RequestedEntityNotFoundException
import com.qinet.feastique.exception.PermissionDeniedException
import com.qinet.feastique.exception.UserNotFoundException
import com.qinet.feastique.repository.complement.ComplementRepository
import com.qinet.feastique.repository.vendor.VendorRepository
import com.qinet.feastique.security.UserSecurity
import com.qinet.feastique.utility.DuplicateUtility
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ComplementService(
    private val complementRepository: ComplementRepository,
    private val vendorRepository: VendorRepository,
    private val duplicateUtility: DuplicateUtility
) {

    @Transactional(readOnly = true)
    fun getComplement(id: Long, vendorDetails: UserSecurity): Complement {
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
    fun getAllComplements(vendorDetails: UserSecurity): List<Complement> {
        val complements = complementRepository.findAllByVendorId(vendorDetails.id)
            .takeIf { it.isNotEmpty() }
            ?: throw RequestedEntityNotFoundException("No complements found for the vendor ${vendorDetails.id}")
        require(complements.all {
            it.vendor.id == vendorDetails.id
        }) {
            throw PermissionDeniedException("Vendor: ${vendorDetails.id}) does not have the permission to access these complements.")
        }
        return complements
    }

    @Transactional(readOnly = true)
    fun getDuplicates(complementName: String, vendorDetails: UserSecurity): Boolean =
        complementRepository.findFirstByComplementNameIgnoreCaseAndVendorId(complementName, vendorDetails.id) != null


    @Transactional
    fun deleteComplement(id: Long, vendorDetails: UserSecurity) {
        val complement = getComplement(id, vendorDetails)
        if (complement.vendor.id != vendorDetails.id) {
            throw PermissionDeniedException("You do not have the permission to delete this complement.")
        }
        complementRepository.delete(complement)
    }

    @Transactional
    fun saveComplement(complement: Complement): Complement {
        return complementRepository.save(complement)
    }

    @Transactional
    fun addOrUpdateComplement(complementDto: ComplementDto, vendorDetails: UserSecurity): Complement {
        val vendor = vendorRepository.findById(vendorDetails.id)
            .orElseThrow { UserNotFoundException("Vendor not found.") }

        var complement: Complement = if(complementDto.id != null) {
            getComplement(complementDto.id!!, vendorDetails)

        } else {
            Complement().apply {
                this.vendor = vendor
            }
        }

        if(complementDto.id == null) {

            // Check if the vendor has already added a complement with the same name
            if(!duplicateUtility.isDuplicationComplementFound(complementDto.complementName!!, vendorDetails.id)) {
                complement.complementName = complementDto.complementName ?: throw IllegalArgumentException("Please enter a complement name")
            } else {
                throw DuplicateFoundException("A complement with the name ${complementDto.complementName} already exist. Unable add a duplicate.")
            }
        } else {
            complement.complementName = complementDto.complementName ?: throw IllegalArgumentException("Please enter a complement name")
        }

        complement.price = complementDto.price ?: throw IllegalArgumentException("Please enter a price.")
        complement = saveComplement(complement)
        vendorRepository.save(vendor)

        return complement
    }
}

