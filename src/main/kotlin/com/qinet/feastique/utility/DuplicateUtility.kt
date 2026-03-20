package com.qinet.feastique.utility

import com.qinet.feastique.model.enums.ReviewType
import com.qinet.feastique.repository.consumables.beverage.BeverageRepository
import com.qinet.feastique.repository.consumables.addOn.AddOnRepository
import com.qinet.feastique.repository.consumables.complement.ComplementRepository
import com.qinet.feastique.repository.contact.CustomerPhoneNumberRepository
import com.qinet.feastique.repository.user.CustomerRepository
import com.qinet.feastique.repository.consumables.food.FoodRepository
import com.qinet.feastique.repository.contact.VendorPhoneNumberRepository
import com.qinet.feastique.repository.consumables.dessert.DessertRepository
import com.qinet.feastique.repository.consumables.handheld.HandheldRepository
import com.qinet.feastique.repository.review.BeverageReviewRepository
import com.qinet.feastique.repository.review.DessertReviewRepository
import com.qinet.feastique.repository.review.FoodReviewRepository
import com.qinet.feastique.repository.review.HandheldReviewRepository
import com.qinet.feastique.repository.review.VendorReviewRepository
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
    private val handheldRepository: HandheldRepository,
    private val beverageReviewRepository: BeverageReviewRepository,
    private val dessertReviewRepository: DessertReviewRepository,
    private val foodReviewRepository: FoodReviewRepository,
    private val handheldReviewRepository: HandheldReviewRepository,
    private val vendorReviewRepository: VendorReviewRepository
) {

    /** Checks username uniqueness across customers, or phone number uniqueness across customers and vendors. */
    @Transactional(readOnly = true)
    fun isDuplicateFound(username: String? = null, phoneNumber: String? = null): Boolean {
        return when {
            username != null    -> customerRepository.existsByUsernameIgnoreCase(username)
            phoneNumber != null -> customerPhoneNumberRepository.existsByPhoneNumber(phoneNumber)
                    || vendorPhoneNumberRepository.existsByPhoneNumber(phoneNumber)
            else -> throw IllegalArgumentException("Either username or phone must be provided")
        }
    }

    fun isDuplicateFoodFound(foodName: String, vendorId: UUID): Boolean =
        foodRepository.existsByNameIgnoreCaseAndVendorIdAndIsActiveTrue(foodName, vendorId)

    fun isDuplicationComplementFound(complementName: String, vendorId: UUID): Boolean =
        complementRepository.existsByNameIgnoreCaseAndVendorIdAndIsActiveTrue(complementName, vendorId)

    fun isDuplicateAddOnFound(addOnName: String, vendorId: UUID): Boolean =
        addOnRepository.existsByNameIgnoreCaseAndVendorIdAndIsActiveTrue(addOnName, vendorId)

    fun isDuplicateBeverageFound(beverageName: String, vendorId: UUID): Boolean =
        beverageRepository.existsByNameIgnoreCaseAndVendorIdAndIsActiveTrue(beverageName, vendorId)

    fun isDuplicateDessertFound(dessertName: String, vendorId: UUID): Boolean =
        dessertRepository.existsByNameIgnoreCaseAndVendorIdAndIsActiveTrue(dessertName, vendorId)

    fun isDuplicateHandheldFound(handheldName: String, vendorId: UUID): Boolean =
        handheldRepository.existsByNameIgnoreCaseAndVendorIdAndIsActiveTrue(handheldName, vendorId)

    // For reviews
    fun isExistingReviewFound(entityId: UUID, customerId: UUID, orderId: UUID, reviewType: ReviewType): Boolean =
        when (reviewType) {
            ReviewType.BEVERAGE -> beverageReviewRepository.existsByBeverageIdAndCustomerIdAndOrderId(entityId, customerId, orderId)
            ReviewType.DESSERT -> dessertReviewRepository.existsByDessertIdAndCustomerIdAndOrderId(entityId, customerId, orderId)
            ReviewType.FOOD -> foodReviewRepository.existsByFoodIdAndCustomerIdAndOrderId(entityId, customerId, orderId)
            ReviewType.HANDHELD -> handheldReviewRepository.existsByHandheldIdAndCustomerIdAndOrderId(entityId, customerId, orderId)
            ReviewType.VENDOR -> vendorReviewRepository.existsByVendorIdAndCustomerIdAndOrderId(entityId, customerId, orderId)
        }
}

