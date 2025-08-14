package com.qinet.feastique.service

import com.qinet.feastique.model.dto.ComplementDto
import com.qinet.feastique.model.entity.complement.Complement
import com.qinet.feastique.repository.complement.ComplementRepository
import com.qinet.feastique.repository.vendor.VendorRepository
import com.qinet.feastique.security.UserSecurity
import com.qinet.feastique.security.UserVerification
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ComplementService(
    private val complementRepository: ComplementRepository,
    private val vendorRepository: VendorRepository,
    private val userVerification: UserVerification
) {

    @Transactional(readOnly = true)
    fun getComplement(
        id: Long,
        vendorId: Long,
        vendorDetails: UserSecurity

    ): Complement {
        userVerification.verifyVendor(vendorId, vendorDetails)
        val complement = complementRepository.findById(id)
            .orElseThrow { IllegalArgumentException("No discount found for id: $id") }
            .also {
                if (it.vendor.id != vendorDetails.id) {
                    throw IllegalArgumentException("You do not have permission to delete discount: $id")
                }
            }
        return complement
    }
    @Transactional(readOnly = true)
    fun getAllComplements(vendorId: Long, vendorDetails: UserSecurity): List<Complement> {
        val vendor = userVerification.verifyVendor(vendorId, vendorDetails)
        val complements = complementRepository.findAllByVendorId(vendor.id!!)
            .takeIf { it.isNotEmpty() }
            ?: throw IllegalArgumentException("No complements found for the vendor $vendorId")
        require(complements.all { it ->
            it.vendor.id == vendorDetails.id
        }) {
            throw IllegalArgumentException("Vendor: ${vendorDetails.id}) does not have the permission to access these complements.")
        }
        return complements
    }

    @Transactional(readOnly = true)
    fun getDuplicates(complementName: String, vendorId: Long): Boolean =
        complementRepository.findByComplementNameIgnoreCaseAndVendorId(complementName, vendorId) != null


    @Transactional
    fun deleteComplement(
        id: Long,
        vendorId: Long,
        vendorDetails: UserSecurity
    ) {
        userVerification.verifyVendor(vendorId, vendorDetails)
        val complement = getComplement(id, vendorId, vendorDetails)
        if (complement.vendor.id != vendorDetails.id) {
            throw IllegalArgumentException("You do not have the permission to delete this complement.")
        }
        complementRepository.delete(complement)
    }

    @Transactional
    fun saveComplement(complement: Complement): Complement {
        return complementRepository.save(complement)
    }

    @Transactional
    fun addOrUpdateComplement(
        complementDto: ComplementDto,
        vendorDetails: UserSecurity
    ): Complement {
        val vendor = vendorRepository.findById(vendorDetails.id)
            .orElseThrow { IllegalArgumentException("Vendor not found.") }

        var complement: Complement = if(complementDto.id != null) {
            complementRepository.findById(complementDto.id!!)
                .orElseThrow { IllegalArgumentException("Food not found.") }
                .also {
                    if(it.vendor.id != vendorDetails.id) {
                        throw IllegalArgumentException("You do not have permission to update food ${it.complementName}")
                    }
                }

        } else {
            Complement().apply {
                this.vendor = vendor
            }
        }

        if(complementDto.id == null) {

            // Check if a vendor has already added a complement with the same name
            if(!getDuplicates(complementDto.complementName!!, vendorDetails.id)) {
                complement.complementName = complementDto.complementName ?: throw IllegalArgumentException("Please enter a complement name")
            } else {
                throw Exception("A complement with the name ${complementDto.complementName} already exist. Unable add a duplicate.")
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