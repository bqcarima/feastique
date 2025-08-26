package com.qinet.feastique.service.order

import com.qinet.feastique.exception.RequestedEntityNotFoundException
import com.qinet.feastique.exception.UserNotFoundException
import com.qinet.feastique.model.dto.FoodOrderDto
import com.qinet.feastique.model.entity.order.FoodOrder
import com.qinet.feastique.repository.addOn.AddOnRepository
import com.qinet.feastique.repository.complement.ComplementRepository
import com.qinet.feastique.repository.customer.CustomerRepository
import com.qinet.feastique.repository.food.FoodRepository
import com.qinet.feastique.repository.order.FoodOrderRepository
import com.qinet.feastique.repository.vendor.VendorRepository
import com.qinet.feastique.security.UserSecurity
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class FoodOrderService(
    private val foodOrderRepository: FoodOrderRepository,
    private val customerRepository: CustomerRepository,
    private val foodRepository: FoodRepository,
    private val vendorRepository: VendorRepository,
    private val complementRepository: ComplementRepository,
    private val addOnRepository: AddOnRepository
) {

    @Transactional(readOnly = true)
    fun getAllCustomerOrders(customerDetails: UserSecurity): List<FoodOrder> {
        return foodOrderRepository.findAllByCustomerDeletedStatusAndCustomerId(false, customerDetails.id)
    }

    @Transactional(readOnly = true)
    fun getAllVendorOrders(vendorDetails: UserSecurity): List<FoodOrder> {
        return foodOrderRepository.findAllByVendorDeletedStatusAndVendorId(false, vendorDetails.id)
    }

    @Transactional
    fun saveFoodOrder(foodOrder: FoodOrder): FoodOrder {
        return foodOrderRepository.save(foodOrder)
    }

    @Transactional
    fun addFoodOrder(foodOrderDto: FoodOrderDto, foodId: Long, customerDetails: UserSecurity): FoodOrder {
        val customer = customerRepository.findById(customerDetails.id)
            .orElseThrow {
                throw UserNotFoundException("An unexpected error occurred. Customer account not found.")
            }

        val food = foodRepository.findById(foodId)
            .orElseThrow {
                throw RequestedEntityNotFoundException("Unable to place order. Food not found.")
            }

        val vendor = vendorRepository.findById(food.vendor.id!!)
            .orElseThrow { throw UserNotFoundException("Unable to place order. Vendor not found.") }

        val foodOrder = FoodOrder()
        foodOrder.vendor = vendor
        foodOrder.customer = customer
        foodOrder.food = food
        foodOrder.foodName = food.foodName ?: throw IllegalArgumentException("Please enter a food name.")
        foodOrder.placementTime = LocalDateTime.now()
        foodOrder.deliveryTime =


        // Assigning a complement
        // Get the matching complement from the list.
        val matchingComplementId = food.foodComplement.first { it.complement.id == foodOrderDto.complementId }.id
            .takeIf { it != null }
            ?: throw IllegalArgumentException("Unable to place order. Complement cannot be gotten from food.")

        val complement = complementRepository.findById(matchingComplementId)
            .orElseThrow { RequestedEntityNotFoundException("Unable to place order. Complement not found.") }
        foodOrder.complement = complement

        // Assigning add-ons
        // Return id of matching add-ons
        val foodAddOn = food.foodAddOn.mapNotNull { it.addOn.id } // remove nulls that could be present
            .filter { it in foodOrderDto.addOnIds }

        val addOns = addOnRepository.findAllByIdInAndVendorId(foodAddOn, vendor.id!!)

    }

}

