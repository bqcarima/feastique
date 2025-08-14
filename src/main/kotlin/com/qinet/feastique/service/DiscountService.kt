package com.qinet.feastique.service

import com.qinet.feastique.common.mapper.toResponse
import com.qinet.feastique.model.dto.DiscountDto
import com.qinet.feastique.model.entity.discount.Discount
import com.qinet.feastique.repository.discount.DiscountRepository
import com.qinet.feastique.repository.vendor.VendorRepository
import com.qinet.feastique.response.DiscountResponse
import com.qinet.feastique.security.UserSecurity
import com.qinet.feastique.security.UserVerification
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DiscountService(
    private val discountRepository: DiscountRepository,
    private val vendorRepository: VendorRepository,
    private val userVerification: UserVerification
) {

    @Transactional(readOnly = true)
    fun getDiscount(
        id: Long,
        vendorId: Long,
        vendorDetails: UserSecurity

    ): Discount {
        userVerification.verifyVendor(vendorId, vendorDetails)
        val discount = discountRepository.findById(id)
            .orElseThrow { IllegalArgumentException("No discount found for id: $id") }
            .also {
                if (it.vendor.id != vendorDetails.id) {
                    throw IllegalArgumentException("You do not have permission to delete discount: $id")
                }
            }
        return discount
    }

    @Transactional(readOnly = true)
    fun getAllDiscounts(vendorId: Long, vendorDetails: UserSecurity): List<Discount> {

        val vendor = userVerification.verifyVendor(vendorId, vendorDetails)
        val discounts = discountRepository.findAllByVendorId(vendor.id!!)
            .takeIf { it.isNotEmpty() }
            ?: throw IllegalArgumentException("No discounts found for the vendor $vendorId")

        require(discounts.all { it ->
            it.vendor.id == vendorDetails.id
        }) {
            throw IllegalArgumentException("You (vendor ${vendorDetails.id}) does not have the permission to access these discounts.")
        }
        return discounts
    }

    @Transactional(readOnly = true)
    fun getDuplicates(discountName: String, vendorId: Long): Boolean =
        discountRepository.findByDiscountNameIgnoreCaseAndVendorId(discountName, vendorId) != null


    @Transactional
    fun deleteDiscount(
        id: Long,
        vendorId: Long,
        vendorDetails: UserSecurity
    ) {
        userVerification.verifyVendor(vendorId, vendorDetails)
        val discount = getDiscount(id, vendorId, vendorDetails)
        if (discount.vendor.id != vendorDetails.id) {
            throw IllegalArgumentException("You do not have the permission to delete this discount.")
        }
        discountRepository.delete(discount)
    }

    @Transactional
    fun deleteAllDiscounts(
        vendorId: Long,
        vendorDetails: UserSecurity
    ) {
        val vendor = userVerification.verifyVendor(vendorId, vendorDetails)
        val discounts = getAllDiscounts(vendor.id!!, vendorDetails)
            .takeIf { it.isNotEmpty() }
            ?: throw IllegalArgumentException("No discounts found for the vendor $vendorId")

        require(discounts.all { it ->
            it.vendor.id == vendorDetails.id
        }) {
            throw IllegalArgumentException("You do not have the permission to delete these discounts.")
        }
        discountRepository.deleteAllByVendorId(vendor.id!!)
    }

    @Transactional
    fun saveDiscount(discount: Discount): Discount {
        return discountRepository.save(discount)
    }

    @Transactional
    fun addOrUpdateDiscount(
        discountDto: DiscountDto,
        vendorDetails: UserSecurity
    ): DiscountResponse {
        val vendor = vendorRepository.findById(vendorDetails.id)
            .orElseThrow { IllegalArgumentException("Vendor not found.") }

        var discount: Discount = if (discountDto.id != null) {
            discountRepository.findById(discountDto.id!!)
                .orElseThrow { IllegalArgumentException("Discount not found.") }
                .also {
                    if (it.vendor.id != vendor.id) {
                        throw IllegalArgumentException("You do not have permission to update this discount.")
                    }
                }
        } else {
            Discount().apply {
                this.vendor = vendor
            }
        }

        if (!getDuplicates(discountDto.discountName, vendorDetails.id)) {
            discount.discountName = discountDto.discountName
        } else {
            discount.discountName = discountDto.discountName
        }

        discount.percentage = discountDto.percentage
        discount.startDate = discountDto.startDate
        discount.endDate = discountDto.endDate
        discount = saveDiscount(discount)
        vendorRepository.save(vendor)

        return discount.toResponse()
    }
}