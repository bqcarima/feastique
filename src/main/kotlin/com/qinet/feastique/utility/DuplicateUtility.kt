package com.qinet.feastique.utility

import com.qinet.feastique.repository.consumables.beverage.BeverageRepository
import com.qinet.feastique.repository.consumables.addOn.AddOnRepository
import com.qinet.feastique.repository.consumables.complement.ComplementRepository
import com.qinet.feastique.repository.contact.CustomerPhoneNumberRepository
import com.qinet.feastique.repository.user.CustomerRepository
import com.qinet.feastique.repository.consumables.food.FoodRepository
import com.qinet.feastique.repository.contact.VendorPhoneNumberRepository
import com.qinet.feastique.repository.consumables.dessert.DessertRepository
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Component
class DuplicateUtility(
    private val customerRepository: CustomerRepository,
    private val customerPhoneNumberRepository: CustomerPhoneNumberRepository,
    private val vendorPhoneNumberRepository: VendorPhoneNumberRepository,
    private val complementRepository: ComplementRepository,
    private val addOnRepository: AddOnRepository,
    private val beverageRepository: BeverageRepository,
    private val foodRepository: FoodRepository,
    private val dessertRepository: DessertRepository

) {

    @Transactional(readOnly = true)
    fun isDuplicateFound(username: String? = null, phoneNumber: String? = null): Boolean {
        return when {
            username != null -> customerRepository.existsByUsernameIgnoreCase(username)
            phoneNumber != null -> (customerPhoneNumberRepository.existsByPhoneNumber(phoneNumber) || vendorPhoneNumberRepository.existsByPhoneNumber(phoneNumber))
            else -> throw IllegalArgumentException("Either username or phone must be provided")
        }
    }

    fun isDuplicateFoodFound(foodName: String, vendorId: UUID): Boolean {
        return foodRepository.existsByNameIgnoreCaseAndVendorId(foodName, vendorId)
    }
    fun isDuplicationComplementFound(complementName: String, vendorId: UUID): Boolean {
        return complementRepository.existsByNameIgnoreCaseAndVendorId(complementName, vendorId)
    }
    fun isDuplicateAddOnFound(addOnName: String, vendorId: UUID): Boolean {
        return addOnRepository.existsByNameIgnoreCaseAndVendorId(addOnName, vendorId)
    }
    fun isDuplicateBeverageFound(beverageName: String, vendorId: UUID): Boolean {
        return beverageRepository.existsByNameIgnoreCaseAndVendorId(beverageName, vendorId)
    }

    fun isDuplicateDessertFound(dessertName: String, vendorId: UUID): Boolean {
        return dessertRepository.existsByNameIgnoreCaseAndVendorId(dessertName, vendorId)
    }
}

