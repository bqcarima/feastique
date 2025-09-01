package com.qinet.feastique.service

import com.qinet.feastique.exception.DuplicateFoundException
import com.qinet.feastique.exception.PermissionDeniedException
import com.qinet.feastique.exception.RequestedEntityNotFoundException
import com.qinet.feastique.exception.UserNotFoundException
import com.qinet.feastique.model.dto.BeverageDto
import com.qinet.feastique.model.entity.beverage.Beverage
import com.qinet.feastique.repository.BeverageRepository
import com.qinet.feastique.repository.vendor.VendorRepository
import com.qinet.feastique.security.UserSecurity
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class BeverageService(
    private val beverageRepository: BeverageRepository,
    private val vendorRepository: VendorRepository
) {

    @Transactional(readOnly = true)
    fun getBeverage(id: Long, vendorDetails: UserSecurity): Beverage {
        val beverage = beverageRepository.findById(id)
            .orElseThrow { RequestedEntityNotFoundException("No beverage found for id: $id") }
            .also {
                if (it.vendor.id != vendorDetails.id) {
                    throw PermissionDeniedException("You do not have the permission to access beverage.")
                }
            }
        return beverage
    }

    @Transactional(readOnly = true)
    fun getAllBeverages(vendorDetails: UserSecurity): List<Beverage> {
        val beverages = beverageRepository.findAllByVendorId(vendorDetails.id)
            .takeIf { it.isNotEmpty() }
            ?: throw RequestedEntityNotFoundException("No beverages found for vendor: $vendorDetails.id")
        require(beverages.all {
            it.vendor.id == vendorDetails.id
        }) {
            throw PermissionDeniedException("Vendor: ${vendorDetails.id} does not have permission to access these beverages.")
        }
        return beverages
    }

    @Transactional(readOnly = true)
    fun getDuplicates(beverageName: String, vendorDetails: UserSecurity) =
        beverageRepository.findFirstByBeverageNameIgnoreCaseAndVendorId(beverageName, vendorDetails.id) != null

    @Transactional
    fun deleteBeverage(id: Long, vendorDetails: UserSecurity) {
        val beverage = getBeverage(id,vendorDetails)
        if(beverage.vendor.id != vendorDetails.id)
            throw PermissionDeniedException("You do not have the permission to delete the beverage.")
        beverageRepository.delete(beverage)
    }

    @Transactional
    fun saveBeverage(beverage: Beverage): Beverage {
        return beverageRepository.save(beverage)
    }

    @Transactional
    fun addOrUpdateBeverage(beverageDto: BeverageDto, vendorDetails: UserSecurity): Beverage {
        val vendor = vendorRepository.findById(vendorDetails.id)
            .orElseThrow { UserNotFoundException("Vendor not found.") }

        var beverage: Beverage = if(beverageDto.id != null) {
            beverageRepository.findById(beverageDto.id!!)
                .orElseThrow { RequestedEntityNotFoundException("Beverage not found.") }
                .also {
                    if (it.vendor.id != vendorDetails.id) {
                        throw PermissionDeniedException("You do not have the permission to access the beverage.")
                    }
                }
        } else {
            Beverage().apply {
                this.vendor = vendor
            }
        }

        if(beverage.id == null) {

            // Check if the vendor has already added a complement with the same name
            if(!getDuplicates(beverageDto.beverageName!!, vendorDetails)) {
                beverage.beverageName = beverageDto.beverageName
            } else {
                throw DuplicateFoundException("Duplicate found with the name: ${beverageDto.beverageName}")
            }
        } else {
            beverage.beverageName = beverageDto.beverageName ?: throw IllegalArgumentException("Please enter a name for the beverage.")
        }

        beverage.alcoholic = beverageDto.alcoholic
        beverage.percentage = beverageDto.percentage
        beverage.beverageGroup = beverageDto.beverageGroup
        beverage.price = beverageDto.price ?: throw IllegalArgumentException("Please enter a price.")
        beverage.delivery = beverageDto.delivery

        beverage = saveBeverage(beverage)
        vendorRepository.save(vendor)

        return beverage
    }
}

