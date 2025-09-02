package com.qinet.feastique.service.order

import com.qinet.feastique.exception.PermissionDeniedException
import com.qinet.feastique.exception.RequestedEntityNotFoundException
import com.qinet.feastique.exception.UserNotFoundException
import com.qinet.feastique.model.dto.order.FoodOrderDto
import com.qinet.feastique.model.dto.order.FoodOrderUpdateDto
import com.qinet.feastique.model.entity.addOn.AddOn
import com.qinet.feastique.model.entity.addOn.OrderAddOn
import com.qinet.feastique.model.entity.beverage.Beverage
import com.qinet.feastique.model.entity.beverage.OrderBeverage
import com.qinet.feastique.model.entity.complement.Complement
import com.qinet.feastique.model.entity.discount.OrderDiscount
import com.qinet.feastique.model.entity.food.Food
import com.qinet.feastique.model.entity.order.FoodOrder
import com.qinet.feastique.model.enums.OrderStatus
import com.qinet.feastique.model.enums.OrderType
import com.qinet.feastique.repository.BeverageRepository
import com.qinet.feastique.repository.addOn.AddOnRepository
import com.qinet.feastique.repository.complement.ComplementRepository
import com.qinet.feastique.repository.customer.CustomerAddressRepository
import com.qinet.feastique.repository.customer.CustomerRepository
import com.qinet.feastique.repository.discount.DiscountRepository
import com.qinet.feastique.repository.discount.FoodDiscountRepository
import com.qinet.feastique.repository.food.FoodRepository
import com.qinet.feastique.repository.order.FoodOrderRepository
import com.qinet.feastique.repository.vendor.VendorAddressRepository
import com.qinet.feastique.repository.vendor.VendorRepository
import com.qinet.feastique.security.UserSecurity
import com.qinet.feastique.utility.SecurityUtility
import com.qinet.feastique.utility.toLocalDate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.jvm.optionals.getOrElse

@Service
class FoodOrderService(
    private val foodOrderRepository: FoodOrderRepository,
    private val customerRepository: CustomerRepository,
    private val foodRepository: FoodRepository,
    private val vendorRepository: VendorRepository,
    private val complementRepository: ComplementRepository,
    private val addOnRepository: AddOnRepository,
    private val vendorAddressRepository: VendorAddressRepository,
    private val customerAddressRepository: CustomerAddressRepository,
    private val beverageRepository: BeverageRepository,
    private val securityUtility: SecurityUtility,
    private val foodDiscountRepository: FoodDiscountRepository,
    private val discountRepository: DiscountRepository
) {

    @Transactional(readOnly = true)
    fun getOrder(id: Long, userDetails: UserSecurity): FoodOrder? {
        val role = securityUtility.getRole(userDetails)
        val foodOrder = when (role) {
            "CUSTOMER" -> foodOrderRepository.findByIdAndCustomerIdAndCustomerDeletedStatus(id, userDetails.id, false)
            "VENDOR" -> foodOrderRepository.findByIdAndVendorIdAndCustomerDeletedStatus(id, userDetails.id, false)
            else -> throw IllegalArgumentException("Invalid role. Contact customer support is issue persist.")

        }
        return foodOrder
    }

    @Transactional(readOnly = true)
    fun getAllOrders(userDetails: UserSecurity): List<FoodOrder> {
        val role = securityUtility.getRole(userDetails)
        var foodOrders = when (role) {
            "CUSTOMER" -> foodOrderRepository.findAllByCustomerDeletedStatusAndCustomerId(false, userDetails.id)
            "VENDOR" -> foodOrderRepository.findAllByVendorDeletedStatusAndVendorId(false, userDetails.id)
            else -> throw IllegalArgumentException("Invalid role. Contact customer support is issue persist.")

        }
        if (foodOrders.isEmpty()) foodOrders = emptyList()
        return foodOrders
    }

    @Transactional
    fun saveFoodOrder(foodOrder: FoodOrder): FoodOrder {
        return foodOrderRepository.save(foodOrder)
    }

    // Customer specific operations.
    @Transactional
    fun placeFoodOrder(foodOrderDto: FoodOrderDto, customerDetails: UserSecurity): FoodOrder {
        val customer = customerRepository.findById(customerDetails.id)
            .orElseThrow {
                throw UserNotFoundException("An unexpected error occurred. Customer account not found.")
            }

        val customerAddresses = customerAddressRepository.findAllByCustomerId(customer.id!!)
            .takeIf { it.isNotEmpty() }
            ?: throw RequestedEntityNotFoundException("An unexpected error occurred. No address found for this customer.")

        val defaultCustomerAddress = customerAddresses.find { it.default == true }
            .takeIf { it != null }
            ?: throw RequestedEntityNotFoundException("An unexpected error occurred. No default address found.")

        val food = foodRepository.findByIdWithAllRelations(foodOrderDto.foodId!!)
            .orElseThrow {
                throw RequestedEntityNotFoundException("Unable to place order. Food not found.")
            }

        val vendor = vendorRepository.findById(food.vendor.id!!)
            .orElseThrow { throw UserNotFoundException("Unable to place order. Vendor not found.") }

        val vendorAddress = vendorAddressRepository.findByVendorId(vendor.id!!)
            .takeIf { it != null }
            ?: throw RequestedEntityNotFoundException("Vendor address not found.")

        val foodOrder = FoodOrder()
        foodOrder.vendor = vendor
        foodOrder.customer = customer
        foodOrder.food = food
        foodOrder.placementTime = LocalDateTime.now()
        foodOrder.customerAddress = defaultCustomerAddress
        foodOrder.vendorAddress = vendorAddress

        foodOrder.size = food.foodSize.find {
            it.id == foodOrderDto.foodSizeId
        }!!

        // Assigning a complement
        // Get the matching complement from the back reference.
        val matchingComplementId = food.foodComplement.first { it.complement.id == foodOrderDto.complementId }.id
            .takeIf { it != null }
            ?: throw IllegalArgumentException("Unable to place order. Complement cannot be gotten from food.")

        val complement = complementRepository.findById(matchingComplementId)
            .orElseThrow { RequestedEntityNotFoundException("Unable to place order. Complement not found.") }
        foodOrder.complement = complement

        // Assigning add-ons
        var addOns = emptyList<AddOn>()
        if (!foodOrderDto.addOnIds.isNullOrEmpty()) {
            // Return id of matching add-ons
            val matchingAddOnIds = food.foodAddOn.mapNotNull { it.addOn.id } // remove nulls that could be present
                .filter { it in foodOrderDto.addOnIds!! }
                .takeIf { it.isNotEmpty() }
                ?: throw RequestedEntityNotFoundException("Unable to place order. Add-on not found.")

            // Retrieving the list of selected add-ons from the database based on their id and vendor id
            addOns = addOnRepository.findAllByIdInAndVendorId(matchingAddOnIds, vendor.id!!)
            val orderAddOns = addOns.map {
                OrderAddOn().apply {
                    this.foodOrder = foodOrder
                    this.addOn = it
                }
            }
            foodOrder.orderAddon.addAll(orderAddOns)
        }

        var beverages = emptyList<Beverage>()
        if (!foodOrderDto.beverageIds.isNullOrEmpty()) {
            beverages = beverageRepository.findAllByIdInAndVendorId(foodOrderDto.beverageIds!!, vendor.id!!)
                .takeIf { it.isNotEmpty() }
                ?: throw RequestedEntityNotFoundException("Unable to place order. Beverage(s) not found.")

            val orderBeverages = beverages.map {
                OrderBeverage().apply {
                    this.foodOrder = foodOrder
                    this.beverage = it
                }
            }
            foodOrder.orderBeverage.addAll(orderBeverages)
        }

        // Check if food is deliverable via backreference.
        if (food.menu.delivery == false) {
            foodOrder.deliveryTime = null
        }

        // check if delivery fee is applicable to the order
        if (foodOrderDto.orderType == OrderType.DELIVERY) {
            foodOrder.deliveryFee = food.deliveryFee
            foodOrder.deliveryTime = food.deliveryTime
        } else {
            food.deliveryFee = 0
        }
        foodOrder.orderType = foodOrderDto.orderType ?: throw IllegalArgumentException("Please select and order type.")
        foodOrder.orderStatus = OrderStatus.PENDING

        val discountIds = food.foodDiscount.mapNotNull { it.discount.id }
        val discounts = discountRepository.findAllByIdInAndVendorId(discountIds, food.vendor.id!!)
            .takeIf { it.isNotEmpty()}
            ?: throw RequestedEntityNotFoundException("No discount found for the food.")

        val applicableDiscounts = discounts.filter {
            val today = LocalDate.now()
            it.startDate!!.toLocalDate() <= today &&
                    it.endDate!!.toLocalDate() >=today
        }

        val orderDiscounts = applicableDiscounts.map {
            OrderDiscount().apply {
                this.foodOrder = foodOrder
                this.discount = it
            }
        }
        foodOrder.orderDiscount.addAll(orderDiscounts)
        val totalDiscountPercentage = applicableDiscounts.sumOf { it.percentage ?: 0 }
        val subTotal = subTotal(
            food = food,
            complement = complement,
            addOnList = addOns,
            beverageList = beverages,
            totalDiscountPercentage
        )
        val invoiceTotal: Float = if (food.deliveryFee != 0L) {
            subTotal
        } else {
            subTotal + food.deliveryFee!!
        }
        println(invoiceTotal)
        foodOrder.totalAmount = invoiceTotal.toLong()
        val placedFoodOrder = saveFoodOrder(foodOrder)
        return placedFoodOrder
    }

    @Transactional
    fun cancelOrUpdateOrder(id: Long, foodOrderUpdateDto: FoodOrderUpdateDto, userDetails: UserSecurity): FoodOrder {
        var order = FoodOrder()
        val role = securityUtility.getSingleRole(userDetails)

        when (role) {
            "CUSTOMER" -> {
                order = foodOrderRepository.findByIdAndCustomerIdAndOrderStatus(id, userDetails.id, OrderStatus.PENDING)
                    .takeIf { it != null && it.orderStatus == OrderStatus.PENDING }
                    ?: throw RequestedEntityNotFoundException("An unexpected error occurred. Order cannot be cancelled. Order not found or has been confirmed already.")
                order.orderStatus = foodOrderUpdateDto.orderStatus
            }

            "VENDOR" -> {
                order = foodOrderRepository.findById(id)
                    .getOrElse { throw RequestedEntityNotFoundException("An unexpected error occurred. Unable to complete operation.") }
                    .also {
                        if (it.vendor.id != userDetails.id) {
                            throw PermissionDeniedException("You do not have the permission to update this order.")
                        }
                    }

                when (order.orderStatus) {
                    OrderStatus.PENDING -> {
                        when (foodOrderUpdateDto.orderStatus) {
                            OrderStatus.CONFIRMED -> order.orderStatus = OrderStatus.CONFIRMED
                            OrderStatus.DECLINED -> order.orderStatus = OrderStatus.DECLINED
                            else -> {
                                IllegalArgumentException("Operation: ${foodOrderUpdateDto.orderStatus} not applicable.")
                            }
                        }
                        order.responseTime = LocalDateTime.now()
                    }

                    OrderStatus.CONFIRMED -> {
                        when (order.orderType) {
                            OrderType.DELIVERY -> order.orderStatus = OrderStatus.EN_ROUTE
                            OrderType.DINE_IN -> order.orderStatus = OrderStatus.READY
                            OrderType.TAKEAWAY -> order.orderStatus = OrderStatus.READY
                            else -> throw IllegalArgumentException("Invalid order type ${order.orderType}. Contact an support if issue persists.")
                        }
                    }

                    OrderStatus.EN_ROUTE -> {
                        order.orderStatus = OrderStatus.DELIVERED
                        order.completedTime = LocalDateTime.now()
                    }

                    OrderStatus.READY -> {
                        when (order.orderType) {
                            OrderType.DINE_IN -> order.orderStatus = OrderStatus.SERVED
                            OrderType.TAKEAWAY -> order.orderStatus = OrderStatus.COLLECTED
                            else -> throw IllegalArgumentException("Invalid order status ${order.orderType}. Contact support if issue persists.")
                        }
                        order.completedTime = LocalDateTime.now()
                    }
                    else -> throw IllegalArgumentException("Invalid option. Try again. Contact support if issue persists.")
                }
            }
        }

        foodOrderRepository.save(order)
        val updatedFoodOrder = foodOrderRepository.findByIdWithAllRelations(id)
            .orElseThrow { IllegalArgumentException("An unexpected error occurred updating food order.") }
        return updatedFoodOrder
    }

    fun deleteOrder(
        id: Long,
        userDetails: UserSecurity
    ) {
        val role = securityUtility.getRole(userDetails)
        val order = foodOrderRepository.findById(id)
            .orElseThrow { throw RequestedEntityNotFoundException("An unexpected error occurred. Unable to delete order.") }

        when (role) {
            "CUSTOMER" -> {
                if (order.customer.id != userDetails.id) {
                    throw PermissionDeniedException("You do not have there permission to delete this order.")
                }
                order.customerDeletedStatus = true
            }

            "VENDOR" -> {
                if (order.vendor.id != userDetails.id) {
                    throw PermissionDeniedException("You do not have there permission to delete this order.")
                }
                order.vendorDeletedStatus = true
            }
        }
        foodOrderRepository.save(order)
    }

    // Helper function to calculate order total cost.
    private fun subTotal(
        food: Food,
        complement: Complement,
        addOnList: List<AddOn>?,
        beverageList: List<Beverage>?,
        totalDiscountPercentage: Int
    ): Float {
        var total: Float
        val basePrice = food.basePrice!!
        val complementPrice = complement.price!!
        val addOnTotal = addOnList?.sumOf { it.price ?: 0 } ?: 0f
        val foodTotal = basePrice.toFloat() + complementPrice.toFloat() + addOnTotal.toFloat()
        val beverageTotal = beverageList?.sumOf { it.price ?: 0 } ?: 0f

        total = discountedPrice(foodTotal, totalDiscountPercentage) + beverageTotal.toFloat()
        return total
    }

    private fun discountedPrice(price: Float, totalDiscountPercentage: Int): Float {
        val discountMultiplier = (totalDiscountPercentage.toFloat() / 100f)
        val priceDrop = discountMultiplier * price
        return price - priceDrop
    }
}

