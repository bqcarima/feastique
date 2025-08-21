package com.qinet.feastique.service

import com.qinet.feastique.exception.PermissionDeniedException
import com.qinet.feastique.exception.RequestedEntityNotFoundException
import com.qinet.feastique.exception.UserNotFoundException
import com.qinet.feastique.model.dto.DiscountDto
import com.qinet.feastique.model.entity.discount.Discount
import com.qinet.feastique.repository.discount.DiscountRepository
import com.qinet.feastique.repository.vendor.VendorRepository
import com.qinet.feastique.security.UserSecurity
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DiscountService(
    private val discountRepository: DiscountRepository,
    private val vendorRepository: VendorRepository
) {

    @Transactional(readOnly = true)
    fun getDiscount(id: Long, vendorDetails: UserSecurity): Discount {
        val discount = discountRepository.findById(id)
            .orElseThrow { RequestedEntityNotFoundException("No discount found for id: $id") }
            .also {
                if (it.vendor.id != vendorDetails.id) {
                    throw PermissionDeniedException("You do not have permission to delete discount: $id")
                }
            }
        return discount
    }

    @Transactional(readOnly = true)
    fun getAllDiscounts(vendorDetails: UserSecurity): List<Discount> {

        val discounts = discountRepository.findAllByVendorId(vendorDetails.id)
            .takeIf { it.isNotEmpty() }
            ?: throw RequestedEntityNotFoundException("No discounts found for the vendor ${vendorDetails.id}")

        require(discounts.all { it ->
            it.vendor.id == vendorDetails.id
        }) {
            throw PermissionDeniedException("You (vendor ${vendorDetails.id}) does not have the permission to access these discounts.")
        }
        return discounts
    }

    @Transactional(readOnly = true)
    fun getDuplicates(discountName: String, vendorDetails: UserSecurity): Boolean =
        discountRepository.findFirstByDiscountNameIgnoreCaseAndVendorId(discountName, vendorDetails.id) != null

    @Transactional
    fun deleteDiscount(id: Long, vendorDetails: UserSecurity) {
        val discount = getDiscount(id, vendorDetails)
        discountRepository.delete(discount)
    }

    @Transactional
    fun deleteAllDiscounts( vendorDetails: UserSecurity) {
        getAllDiscounts(vendorDetails)
        discountRepository.deleteAllByVendorId(vendorDetails.id)
    }

    @Transactional
    fun saveDiscount(discount: Discount): Discount {
        return discountRepository.save(discount)
    }

    @Transactional
    fun addOrUpdateDiscount(discountDto: DiscountDto, vendorDetails: UserSecurity): Discount {
        val vendor = vendorRepository.findById(vendorDetails.id)
            .orElseThrow { UserNotFoundException("Vendor not found.") }

        var discount: Discount = if (discountDto.id != null) {
            discountRepository.findById(discountDto.id!!)
                .orElseThrow { RequestedEntityNotFoundException("Discount not found.") }
                .also {
                    if (it.vendor.id != vendor.id) {
                        throw PermissionDeniedException("You do not have permission to update this discount.")
                    }
                }
        } else {
            Discount().apply {
                this.vendor = vendor
            }
        }

        if (!getDuplicates(discountDto.discountName, vendorDetails)) {
            discount.discountName = discountDto.discountName
        } else {
            discount.discountName = discountDto.discountName
        }

        discount.percentage = discountDto.percentage
        discount.startDate = discountDto.startDate
        discount.endDate = discountDto.endDate
        discount = saveDiscount(discount)
        vendorRepository.save(vendor)

        return discount
    }
}

