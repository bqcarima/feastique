package com.qinet.feastique.service.order

import com.qinet.feastique.exception.PermissionDeniedException
import com.qinet.feastique.exception.RequestedEntityNotFoundException
import com.qinet.feastique.exception.UserNotFoundException
import com.qinet.feastique.model.dto.order.FoodOrderDto
import com.qinet.feastique.model.dto.order.FoodOrderUpdateDto
import com.qinet.feastique.model.entity.beverage.Beverage
import com.qinet.feastique.model.entity.discount.AppliedDiscount
import com.qinet.feastique.model.entity.order.Order
import com.qinet.feastique.model.entity.order.beverage.BeverageOrderItem
import com.qinet.feastique.model.entity.order.food.FoodOrderItem
import com.qinet.feastique.model.enums.OrderStatus
import com.qinet.feastique.model.enums.OrderType
import com.qinet.feastique.repository.BeverageRepository
import com.qinet.feastique.repository.customer.CustomerAddressRepository
import com.qinet.feastique.repository.customer.CustomerRepository
import com.qinet.feastique.repository.discount.DiscountRepository
import com.qinet.feastique.repository.food.FoodRepository
import com.qinet.feastique.repository.order.OrderRepository
import com.qinet.feastique.repository.vendor.VendorRepository
import com.qinet.feastique.security.UserSecurity
import com.qinet.feastique.utility.GeneralUtility
import com.qinet.feastique.utility.SecurityUtility
import com.qinet.feastique.utility.toLocalDate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.*
import kotlin.jvm.optionals.getOrElse

@Service
class OrderService(
    private val orderRepository: OrderRepository,
    private val customerRepository: CustomerRepository,
    private val foodRepository: FoodRepository,
    private val vendorRepository: VendorRepository,
    private val customerAddressRepository: CustomerAddressRepository,
    private val beverageRepository: BeverageRepository,
    private val securityUtility: SecurityUtility,
    private val discountRepository: DiscountRepository
) {

    @Transactional(readOnly = true)
    fun getOrder(id: UUID, userDetails: UserSecurity): Order? {
        val role = securityUtility.getRole(userDetails)
        val order = when (role) {
            "CUSTOMER" -> orderRepository.findByIdAndCustomerIdAndCustomerDeletedStatus(id, userDetails.id, false)
            "VENDOR" -> orderRepository.findByIdAndVendorIdAndCustomerDeletedStatus(id, userDetails.id, false)
            else -> throw IllegalArgumentException("Invalid role. Contact customer support is issue persist.")

        }
        return order
    }

    @Transactional(readOnly = true)
    fun getAllOrders(userDetails: UserSecurity): List<Order> {
        val role = securityUtility.getRole(userDetails)
        var orders = when (role) {
            "CUSTOMER" -> orderRepository.findAllByCustomerDeletedStatusAndCustomerId(false, userDetails.id)
            "VENDOR" -> orderRepository.findAllByVendorDeletedStatusAndVendorId(false, userDetails.id)
            else -> throw IllegalArgumentException("Invalid role. Contact customer support is issue persist.")

        }
        if (orders.isEmpty()) orders = emptyList()
        return orders
    }

    @Transactional
    fun saveFoodOrder(order: Order): Order {
        return orderRepository.save(order)
    }

    // Customer specific operations.

    /**
     * This method places a food order and a beverage order (if applicable)
     * directly from the food screen.
     * @param FoodOrderDto
     * @param UserSecurity`
     * @return [Order]
     * @throws UserNotFoundException
     * @throws RequestedEntityNotFoundException
     * @throws PermissionDeniedException
     * @throws IllegalArgumentException
     */
    @Transactional
    fun placeFoodOrder(foodOrderDto: FoodOrderDto, customerDetails: UserSecurity): Order {
        val customer = customerRepository.findById(customerDetails.id)
            .orElseThrow {
                throw UserNotFoundException("An unexpected error occurred. Customer account not found.")
            }

        val customerAddresses = customerAddressRepository.findAllByCustomerId(customer.id)
            .takeIf { it.isNotEmpty() }
            ?: throw RequestedEntityNotFoundException("An unexpected error occurred. No address found for this customer.")

        val defaultCustomerAddress = customerAddresses.find { it.default == true }
            .takeIf { it != null }
            ?: throw RequestedEntityNotFoundException("An unexpected error occurred. No default address found.")

        val food = foodRepository.findByIdWithAllRelations(foodOrderDto.foodId!!)
            .orElseThrow {
                throw RequestedEntityNotFoundException("Unable to place order. Food not found.")
            }

        val vendor = vendorRepository.findById(food.vendor.id)
            .orElseThrow { throw UserNotFoundException("Unable to place order. Vendor not found.") }


        // Generating internal order id and user oder code
        val orderId = GeneralUtility.OrderIdGenerator.generate()

        // Creating a new Order object
        val newOrder = Order().apply {
            this.internalOrderId = orderId.internalOrderId
            this.userOrderCode = orderId.userOrderCode
            this.customer = customer
            this.vendor = vendor
            this.placementTime = LocalDateTime.now()
            this.customerAddress = defaultCustomerAddress
            this.orderStatus = OrderStatus.PENDING
        }

        // Creating a new food order item
        val newFoodOrderItem = FoodOrderItem().apply {
            this.order = newOrder
            this.food = food
            this.quantity = foodOrderDto.foodQuantity
            this.size = food.foodSize.find { it.id == foodOrderDto.foodSizeId }!!

        }

        // Assigning a complement
        // Get the matching complement from the back reference.
        val matchingComplement = food.foodComplement.firstOrNull { it.complement.id == foodOrderDto.complementId }?.complement
            ?: throw IllegalArgumentException("Unable to place order. Complement cannot be gotten from food.")

        newFoodOrderItem.complement = matchingComplement

        // Assigning add-ons
        if (!foodOrderDto.addOnIds.isNullOrEmpty()) {
            // Return id of matching add-ons
            val matchingAddOnIds = food.foodAddOn.map { it.addOn } // remove nulls that could be present
                .filter { it.id in foodOrderDto.addOnIds!! }
                .takeIf { it.isNotEmpty() }
                ?: throw RequestedEntityNotFoundException("Unable to place order. Add-on not found.")

            // Retrieving the list of selected add-ons from the database based on their id and vendor id
            newFoodOrderItem.addOns.addAll(matchingAddOnIds)
        }

        // Check if food is deliverable via backreference.
        if (food.menu.delivery == false) {
            newOrder.deliveryTime = null
        }

        // check if delivery fee is applicable to the order
        if (foodOrderDto.orderType == OrderType.DELIVERY) {
            newOrder.deliveryFee = food.deliveryFee
            newOrder.deliveryTime = food.deliveryTime
            newFoodOrderItem.orderType = OrderType.DELIVERY
        } else {
            newOrder.deliveryFee = 0
        }
        newOrder.orderType =
            foodOrderDto.orderType ?: throw IllegalArgumentException("Please select and order type.")

        val discountIds = food.foodDiscount.map { it.discount.id }
        val discounts = discountRepository.findAllByIdInAndVendorId(discountIds, food.vendor.id)
            .takeIf { it.isNotEmpty() }
            ?: throw RequestedEntityNotFoundException("No discount found for the food.")

        // filter discounts to get only active discounts
        val applicableDiscounts = discounts.filter {
            val today = LocalDate.now()
            it.startDate!!.toLocalDate() <= today &&
                    it.endDate!!.toLocalDate() >= today
        }

        // mapping applicable discounts to applied discount objects and creating a list
        val appliedDiscounts = applicableDiscounts.map {
            AppliedDiscount().apply {
                this.foodOrderItem = newFoodOrderItem
                this.discount = it
            }
        }
        newFoodOrderItem.appliedDiscounts.addAll(appliedDiscounts)

        newFoodOrderItem.totalAmount = newFoodOrderItem.calculateTotal()
        newOrder.foodOrderItems.add(newFoodOrderItem)

        // Beverages added directly from the food order page
        var beverages: List<Beverage>
        if (!foodOrderDto.beverageIds.isNullOrEmpty()) {
            beverages =
                beverageRepository.findAllByIdInAndVendorId(foodOrderDto.beverageIds!!.keys.toList(), vendor.id)
                    .takeIf { it.isNotEmpty() }
                    ?: throw RequestedEntityNotFoundException("Unable to place order. Beverage(s) not found.")

            val beverageOrderItems = beverages.map {
                val beverageQuantity = foodOrderDto.beverageIds!![it.id]
                BeverageOrderItem().apply {
                    this.order = newOrder
                    this.beverage = it
                    this.quantity = beverageQuantity ?: 1
                    this.totalAmount = this.calculateTotal()
                }
            }
            newOrder.beverageOrderItems.addAll(beverageOrderItems)
        }

        // calculate totals
        val result = newOrder.calculateTotals()

        // calculate invoice total: subtotal + delivery fee (if applicable)
        val invoiceTotal: Long = if (newOrder.orderType != OrderType.DELIVERY) {
            result.first
        } else {
            result.third
        }

        newOrder.totalAmount = invoiceTotal
        val placedFoodOrder = saveFoodOrder(newOrder)
        return orderRepository.findByIdWithAllRelations(placedFoodOrder.id).get()
    }

    /**
     * The method is used to change the status of an order.
     * @param UUID
     * @param FoodOrderUpdateDto
     * @param UserSecurity`
     * @return [Order]
     * @throws RequestedEntityNotFoundException
     * @throws PermissionDeniedException
     * @throws IllegalArgumentException
     */
    @Transactional
    fun cancelOrUpdateOrder(
        id: UUID,
        foodOrderUpdateDto: FoodOrderUpdateDto,
        userDetails: UserSecurity
    ): Order {
        var order = Order()
        val role = securityUtility.getSingleRole(userDetails)

        when (role) {
            "CUSTOMER" -> {
                order = orderRepository.findByIdAndCustomerIdAndOrderStatus(id, userDetails.id, OrderStatus.PENDING)
                    .takeIf { it != null && it.orderStatus == OrderStatus.PENDING }
                    ?: throw RequestedEntityNotFoundException("An unexpected error occurred. Order cannot be cancelled. Order not found or has been confirmed already.")
                order.orderStatus = foodOrderUpdateDto.orderStatus
            }

            "VENDOR" -> {
                order = orderRepository.findById(id)
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

                        // Calculate and set the ready by time (if applicable)
                        if (order.orderType != OrderType.DELIVERY) {
                            order.readyBy = calculateOrderReadyTime(order)
                        }
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

        orderRepository.save(order)
        val updatedOrder = orderRepository.findByIdWithAllRelations(id)
            .orElseThrow { IllegalArgumentException("An unexpected error occurred updating food order.") }
        return updatedOrder
    }

    /**
     * This method hides an [Order] from the user's history by
     * changing the `deleted_status` to `true`.
     * @param UUID
     * @param UserSecurity
     *
     * @throws RequestedEntityNotFoundException
     * @throws PermissionDeniedException
     */
    fun deleteOrder(
        id: UUID,
        userDetails: UserSecurity
    ) {
        val role = securityUtility.getRole(userDetails)
        val order = orderRepository.findById(id)
            .orElseThrow { throw RequestedEntityNotFoundException("An unexpected error occurred. Unable to delete order.") }

        when (role) {
            "CUSTOMER" -> {
                if (order?.customer?.id != userDetails.id) {
                    throw PermissionDeniedException("You do not have there permission to delete this order.")
                }
                order.customerDeletedStatus = true
            }

            "VENDOR" -> {
                if (order?.vendor?.id != userDetails.id) {
                    throw PermissionDeniedException("You do not have there permission to delete this order.")
                }
                order.vendorDeletedStatus = true
            }
        }
        orderRepository.save(order)
    }

    /**
     * This method calculates the time an [Order] of type
     * [OrderType.DINE_IN] or [OrderType.TAKEAWAY] will be ready.
     *
     * @param Order
     * @return [LocalTime]
     * @throws NullPointerException
     */
    private fun calculateOrderReadyTime(order: Order): LocalTime {
        val maxPreparationTime = order.foodOrderItems
            .maxOfOrNull { it.food.preparationTime ?: 0 } ?: 0
        val readyTime = order.responseTime?.plusMinutes(maxPreparationTime.toLong())
        return readyTime!!.toLocalTime()
    }
}

