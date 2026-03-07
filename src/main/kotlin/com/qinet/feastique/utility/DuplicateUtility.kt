package com.qinet.feastique.utility

import com.qinet.feastique.repository.consumables.beverage.BeverageRepository
import com.qinet.feastique.repository.consumables.addOn.AddOnRepository
import com.qinet.feastique.repository.consumables.complement.ComplementRepository
import com.qinet.feastique.repository.contact.CustomerPhoneNumberRepository
import com.qinet.feastique.repository.user.CustomerRepository
import com.qinet.feastique.repository.consumables.food.FoodRepository
import com.qinet.feastique.repository.contact.VendorPhoneNumberRepository
import com.qinet.feastique.repository.consumables.dessert.DessertRepository
import com.qinet.feastique.repository.consumables.handheld.HandheldRepository
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
    private val dessertRepository: DessertRepository,
    private val handheldRepository: HandheldRepository

) {

    /**
     * Checks for duplicate usernames and phone numbers across customers and vendors.
     * If a username is provided, it checks for duplicates in the customer repository.
     * If a phone number is provided, it checks for duplicates in both customer and vendor phone number repositories.
     * @param username the username to check for duplicates (optional)
     * @param phoneNumber the phone number to check for duplicates (optional)
     * @return true if a duplicate is found, false otherwise
     * @throws IllegalArgumentException if neither username nor phone number is provided
     */
    @Transactional(readOnly = true)
    fun isDuplicateFound(username: String? = null, phoneNumber: String? = null): Boolean {
        return when {
            username != null -> customerRepository.existsByUsernameIgnoreCase(username)
            phoneNumber != null -> (customerPhoneNumberRepository.existsByPhoneNumber(phoneNumber) || vendorPhoneNumberRepository.existsByPhoneNumber(
                phoneNumber
            ))

            else -> throw IllegalArgumentException("Either username or phone must be provided")
        }
    }


    /**
     * Checks for duplicate food names for a given vendor.
     * @param foodName the name of the food to check for duplicates
     * @param vendorId the ID of the vendor to check within
     * @return true if a duplicate food name is found for the vendor, false otherwise
     */
    fun isDuplicateFoodFound(foodName: String, vendorId: UUID): Boolean {
        return foodRepository.existsByNameIgnoreCaseAndVendorId(foodName, vendorId)
    }

    /**
     * Checks for duplicate complement names for a given vendor.
     * @param complementName the name of the complement to check for duplicates
     * @param vendorId the ID of the vendor to check within
     * @return true if a duplicate complement name is found for the vendor, false otherwise
     */
    fun isDuplicationComplementFound(complementName: String, vendorId: UUID): Boolean {
        return complementRepository.existsByNameIgnoreCaseAndVendorId(complementName, vendorId)
    }

    /**
     * Checks for duplicate add-on names for a given vendor.
     * @param addOnName the name of the add-on to check for duplicates
     * @param vendorId the ID of the vendor to check within
     * @return true if a duplicate add-on name is found for the vendor, false otherwise
     */
    fun isDuplicateAddOnFound(addOnName: String, vendorId: UUID): Boolean {
        return addOnRepository.existsByNameIgnoreCaseAndVendorId(addOnName, vendorId)
    }

    /**
     * Checks for duplicate beverage names for a given vendor.
     * @param beverageName the name of the beverage to check for duplicates
     * @param vendorId the ID of the vendor to check within
     * @return true if a duplicate beverage name is found for the vendor, false otherwise
     */
    fun isDuplicateBeverageFound(beverageName: String, vendorId: UUID): Boolean {
        return beverageRepository.existsByNameIgnoreCaseAndVendorId(beverageName, vendorId)
    }

    /**
     * Checks for duplicate dessert names for a given vendor.
     * @param dessertName the name of the dessert to check for duplicates
     * @param vendorId the ID of the vendor to check within
     * @return true if a duplicate dessert name is found for the vendor, false otherwise
     */
    fun isDuplicateDessertFound(dessertName: String, vendorId: UUID): Boolean {
        return dessertRepository.existsByNameIgnoreCaseAndVendorId(dessertName, vendorId)
    }

    /**
     * Checks for duplicate handheld names for a given vendor.
     * @param handheldName the name of the handheld to check for duplicates
     * @param vendorId the ID of the vendor to check within
     * @return true if a duplicate handheld name is found for the vendor, false otherwise
     */
    fun isDuplicateHandheldFound(handheldName: String, vendorId: UUID): Boolean {
        return handheldRepository.existsByNameIgnoreCaseAndVendorId(handheldName, vendorId)
    }
}

