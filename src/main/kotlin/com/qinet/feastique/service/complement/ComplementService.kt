package com.qinet.feastique.service.complement

import com.qinet.feastique.model.dto.ComplementDto
import com.qinet.feastique.model.entity.complement.Complement
import com.qinet.feastique.repository.complement.ComplementRepository
import com.qinet.feastique.repository.vendor.VendorRepository
import com.qinet.feastique.security.UserSecurity
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*
import kotlin.jvm.optionals.getOrNull
import java.lang.Exception

@Service
class ComplementService(
    private val complementRepository: ComplementRepository,
    private val vendorRepository: VendorRepository
) {

    @Transactional(readOnly = true)
    fun getComplement(complementId: Long): Optional<Complement> {
        return complementRepository.findById(complementId)
    }
    @Transactional(readOnly = true)
    fun getAllComplements(vendorId: Long): List<Complement> {
        return complementRepository.findAllByVendorId(vendorId)
    }

    @Transactional(readOnly = true)
    fun getDuplicates(complementName: String, vendorId: Long): Complement? {
        return complementRepository.findByComplementNameIgnoreCaseAndVendorId(complementName, vendorId)
    }

    @Transactional
    fun deleteComplement(complement: Complement) {
        complementRepository.delete(complement)
    }


    // No use-case for this method yet.
    @Transactional
    fun deleteComplement(complementId: Long, vendorId: Long) {
        complementRepository.deleteByIdAndVendorId(complementId, vendorId)
    }

    @Transactional
    fun saveComplement(complement: Complement): Complement {
        return complementRepository.save(complement)
    }

    @Transactional
    fun addComplement(
        complementDto: ComplementDto,
        vendorDetails: UserSecurity
    ): Complement {
        val vendor = vendorRepository.findById(vendorDetails.id).getOrNull() ?: throw Exception("An unexpected error occurred. Unable to add complement.")

        var complement = Complement()

        // Check if a vendor has already added a complement with the same name
        if(getDuplicates(complementDto.complementName!!, vendorDetails.id) == null) {
            complement.complementName = complementDto.complementName ?: throw IllegalArgumentException("Please enter a complement name")

        } else {
            throw Exception("A complement with the name ${complementDto.complementName} already exist. Unable add a duplicate.")
        }

        complement.price = complementDto.price ?: throw IllegalArgumentException("Please enter a price.")
        complement.vendor = vendor
        complement = saveComplement(complement)
        vendorRepository.save(vendor)

        return complement
    }

}

