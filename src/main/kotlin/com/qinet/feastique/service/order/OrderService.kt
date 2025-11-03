package com.qinet.feastique.service.order

import com.qinet.feastique.exception.PermissionDeniedException
import com.qinet.feastique.exception.RequestedEntityNotFoundException
import com.qinet.feastique.exception.UserNotFoundException
import com.qinet.feastique.model.dto.order.CartItemDto
import com.qinet.feastique.model.dto.order.FoodOrderUpdateDto
import com.qinet.feastique.model.dto.order.OrderItemDto
import com.qinet.feastique.model.entity.discount.AppliedDiscount
import com.qinet.feastique.model.entity.order.Cart
import com.qinet.feastique.model.entity.order.Order
import com.qinet.feastique.model.entity.order.OrderEntity
import com.qinet.feastique.model.entity.order.beverage.BeverageCartItem
import com.qinet.feastique.model.entity.order.beverage.BeverageOrderItem
import com.qinet.feastique.model.entity.order.food.FoodCartItem
import com.qinet.feastique.model.entity.order.food.FoodOrderItem
import com.qinet.feastique.model.enums.OrderStatus
import com.qinet.feastique.model.enums.OrderType
import com.qinet.feastique.repository.BeverageRepository
import com.qinet.feastique.repository.customer.CustomerAddressRepository
import com.qinet.feastique.repository.customer.CustomerRepository
import com.qinet.feastique.repository.discount.DiscountRepository
import com.qinet.feastique.repository.food.FoodRepository
import com.qinet.feastique.repository.order.CartRepository
import com.qinet.feastique.repository.order.OrderRepository
import com.qinet.feastique.repository.vendor.VendorRepository
import com.qinet.feastique.security.UserSecurity
import com.qinet.feastique.utility.GeneralUtility
import com.qinet.feastique.utility.SecurityUtility
import com.qinet.feastique.utility.toLocalDate
import jakarta.persistence.OptimisticLockException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.*
import kotlin.jvm.optionals.getOrElse
import org.slf4j.LoggerFactory
import org.springframework.dao.OptimisticLockingFailureException
import org.springframework.transaction.annotation.Propagation

@Service
class OrderService(
    private val orderRepository: OrderRepository,
    private val customerRepository: CustomerRepository,
    private val foodRepository: FoodRepository,
    private val vendorRepository: VendorRepository,
    private val customerAddressRepository: CustomerAddressRepository,
    private val beverageRepository: BeverageRepository,
    private val securityUtility: SecurityUtility,
    private val discountRepository: DiscountRepository,
    private val cartRepository: CartRepository
) {

    private val logger = LoggerFactory.getLogger(javaClass)
    @Transactional(readOnly = true)
    fun getOrder(id: UUID, userDetails: UserSecurity): Order? {
        val role = securityUtility.getRole(userDetails)
        val order = when (role) {
            "CUSTOMER" -> orderRepository.findByIdAndCustomerIdAndCustomerDeletedStatus(id, userDetails.id, false)
            "VENDOR" -> orderRepository.findByIdAndVendorIdAndVendorDeletedStatus(id, userDetails.id, false)
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
     * @param OrderItemDto
     * @param UserSecurity`
     * @return [Order]
     * @throws UserNotFoundException
     * @throws RequestedEntityNotFoundException
     * @throws PermissionDeniedException
     * @throws IllegalArgumentException
     */
    @Transactional
    fun placeOrderFromFoodScreen(orderItemDto: OrderItemDto, customerDetails: UserSecurity): Order {
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

        val food = foodRepository.findByIdWithAllRelations(orderItemDto.foodId!!)
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
            this.vendor = food.vendor
            this.food = food
            this.quantity = orderItemDto.foodQuantity ?: 1
            this.size = food.foodSize.find { it.id == orderItemDto.foodSizeId }!!
        }

        // Assigning a complement
        // Get the matching complement from the back reference.
        val matchingComplement = food.foodComplement.firstOrNull { it.complement.id == orderItemDto.complementId }?.complement
            ?: throw IllegalArgumentException("Unable to place order. Complement cannot be gotten from food.")

        newFoodOrderItem.complement = matchingComplement

        // Assigning add-ons
        if (!orderItemDto.addOnIds.isNullOrEmpty()) {
            // Return id of matching add-ons
            val matchingAddOnIds = food.foodAddOn.map { it.addOn } // remove nulls that could be present
                .filter { it.id in orderItemDto.addOnIds!! }
                .takeIf { it.isNotEmpty() }
                ?: throw RequestedEntityNotFoundException("Unable to place order. Add-on not found.")

            // Retrieving the list of selected add-ons from the database based on their id and vendor id
            newFoodOrderItem.addOns.addAll(matchingAddOnIds)
        }

        // Check if food is deliverable via backreference.
        if (food.menu.delivery == false) {
            newOrder.deliveryTime = null
        }

        val orderTypeAsString = requireNotNull(orderItemDto.orderType) { "Please select an order type."}
        val orderTypeAsEnum = OrderType.fromString(orderTypeAsString)

        // check if delivery fee is applicable to the order
        if (orderTypeAsEnum == OrderType.DELIVERY) {
            newOrder.deliveryFee = food.deliveryFee
            newOrder.deliveryTime = food.deliveryTime
            newFoodOrderItem.orderType = OrderType.DELIVERY
        } else {
            newOrder.deliveryFee = 0
        }
        newOrder.orderType = orderTypeAsEnum

        val discountIds = food.foodDiscount.map { it.discount.id }

        // Only executes if there is a discount assigned to the food to be ordered.
        if (discountIds.isNotEmpty()) {
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
        }

        newFoodOrderItem.totalAmount = newFoodOrderItem.calculateTotal()
        newOrder.addItem(newFoodOrderItem)

        // Beverages added directly from the food order page
        orderItemDto.beverageIds?.takeIf { it.isNotEmpty()}?.let { beverageMap ->
            val beverages = beverageRepository.findAllByIdInAndVendorId(beverageMap.keys.toList(), vendor.id)
            beverages.forEach { beverage ->
                val quantity = beverageMap[beverage.id] ?: 1
                val newBeverageOrder = BeverageOrderItem().apply {
                    this.order = newOrder
                    this.vendor = beverage.vendor
                    this.beverage = beverage
                    this.quantity = quantity
                    this.orderType = OrderType.fromString(orderItemDto.orderType!!)
                    this.totalAmount = this.calculateTotal()
                }

                val existingBeverageItem = newOrder.beverageOrderItems.find { it.beverage.id == beverage.id }
                if (existingBeverageItem != null) {
                    existingBeverageItem.quantity = existingBeverageItem.quantity + quantity
                    existingBeverageItem.totalAmount = existingBeverageItem.calculateTotal()
                } else {
                    newBeverageOrder.calculateTotal()
                    newOrder.addItem(newBeverageOrder)
                }
            }
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
     * This method is used to process a cart.
     * Cart items with the same vendor, order type, and customer address
     * are grouped into a single order.
     * @param UserSecurity
     * @return List<[Order]>
     * @throws UserNotFoundException
     * @throws RequestedEntityNotFoundException
     * @throws PermissionDeniedException
     * @throws IllegalArgumentException
     */
    /*@Transactional
    fun placeOrderFromCart(cartItemDto: CartItemDto, customerDetails: UserSecurity): List<Order> {
        // Fetch the customer from the database
        val customer = customerRepository.findById(customerDetails.id)
            .orElseThrow { UserNotFoundException("An unexpected error occurred. Customer not found.") }

        // Unless necessary, do not fetch the address and cart via back reference
        val customerAddresses = customerAddressRepository.findAllByCustomerId(customer.id)
        val deliveryAddress = customerAddresses.find { it.id == cartItemDto.deliveryAddress } ?: customerAddresses.find { it.default == true }

        val cart: Cart = cartRepository.findByCustomerId(customer.id)
            .getOrElse { throw RequestedEntityNotFoundException("Unable to process cart. Cannot place order") }

        val selectedItems = cart.items.filter { it.id in cartItemDto.ids }
        val placedOrders: MutableList<Order> = mutableListOf()

        // Group items by vendor and order type
        val groupedItems = selectedItems.groupBy { it.vendor to it.orderType }
        for ((key, itemsForGroup) in groupedItems) {
            val (vendor, orderType) = key
            val orderId = GeneralUtility.OrderIdGenerator.generate()
            val newOrder = Order().apply {
                this.internalOrderId = orderId.internalOrderId
                this.userOrderCode = orderId.userOrderCode
                this.vendor = vendor!!
                this.customer = customer
                this.orderType = orderType ?: throw IllegalArgumentException("Order type cannot be null")
                this.placementTime = LocalDateTime.now()
                this.customerAddress = deliveryAddress
                this.orderStatus = OrderStatus.PENDING
            }

            val currentDate = Date()
            val orderItems = itemsForGroup.map { cartItem: OrderEntity ->
                when(cartItem) {
                    is FoodCartItem -> {
                        FoodOrderItem().apply outer@{
                            this.food = cartItem.food
                            this.complement = cartItem.complement
                            this.size = cartItem.size
                            this.addOns = cartItem.addOns.toMutableList()
                            this.quantity = cartItem.quantity
                            this.orderType = cartItem.orderType
                            this.vendor = vendor
                            this.order = newOrder

                            val activeAppliedDiscount = cartItem.appliedDiscounts.filter { appliedDiscount ->
                                val discount = appliedDiscount.discount
                                val start = discount.startDate
                                val end = discount.endDate

                                when {
                                    start == null && end == null -> true
                                    start == null -> currentDate.before(end)
                                    end == null -> currentDate.after(start)
                                    else -> currentDate.after(start) && currentDate.before(end)
                                }
                            }

                            activeAppliedDiscount.forEach { activeDiscount ->
                                val appliedCopy = AppliedDiscount().apply {
                                    discount = activeDiscount.discount
                                    this.foodOrderItem = this@outer
                                }
                                this@outer.appliedDiscounts.add(appliedCopy)
                            }
                            this.totalAmount = this.calculateTotal()
                        }
                    }

                    is BeverageCartItem -> {
                        BeverageOrderItem().apply {
                            this.beverage = cartItem.beverage
                            this.vendor = vendor
                            this.quantity = cartItem.quantity
                            this.order = newOrder
                            this.orderType = orderType
                            this.totalAmount = this.calculateTotal()
                        }
                    }
                    else -> UnknowItemType().apply { this.quantity = 0 }
                }
            }
            newOrder.addAllItems(orderItems)
            val totals = newOrder.calculateTotals()
            newOrder.deliveryFee = totals.second
            newOrder.totalAmount = totals.third
            placedOrders.add(orderRepository.save(newOrder))
        }
        return placedOrders
    }*/

    @Transactional
    fun placeOrderFromCart(cartItemDto: CartItemDto, customerDetails: UserSecurity): List<Order> {
        val customer = customerRepository.findById(customerDetails.id)
            .orElseThrow { UserNotFoundException("Customer not found.") }

        val customerAddresses = customerAddressRepository.findAllByCustomerId(customer.id)
        val deliveryAddress = customerAddresses.find { it.id == cartItemDto.deliveryAddress }
            ?: customerAddresses.find { it.default == true }

        val cart: Cart = cartRepository.findByCustomerId(customer.id)
            .getOrElse { throw RequestedEntityNotFoundException("Cart not found.") }

        val selectedItems = cart.items.filter { it.id in cartItemDto.ids }
        val placedOrders = mutableListOf<Order>()

        // Group by vendor and order type
        val groupedItems = selectedItems.groupBy { it.vendor to it.orderType }

        for ((key, itemsForGroup) in groupedItems) {
            val (vendor, orderType) = key
            val orderId = GeneralUtility.OrderIdGenerator.generate()

            val newOrder = Order().apply {
                this.internalOrderId = orderId.internalOrderId
                this.userOrderCode = orderId.userOrderCode
                this.vendor = vendor!!
                this.customer = customer
                this.orderType = orderType ?: throw IllegalArgumentException("Order type cannot be null")
                this.placementTime = LocalDateTime.now()
                this.customerAddress = deliveryAddress
                this.orderStatus = OrderStatus.PENDING
            }

            val orderItems = itemsForGroup.map { createOrderItemFromCart(it, newOrder) }
            newOrder.addAllItems(orderItems)

            // Calculate totals
            val totals = newOrder.calculateTotals()
            newOrder.deliveryFee = totals.second
            newOrder.totalAmount = totals.third

            // Save order (cascade will persist all food & beverage items)
            placedOrders.add(orderRepository.save(newOrder))
        }
        if (cart.foodCartItems.isEmpty() && cart.beverageCartItems.isEmpty()) {
            cart.customer = null
            cartRepository.delete(cart)
        } else {
            cartRepository.save(cart)
        }
        return placedOrders
    }

    /**
     * This is a helper function to map cart items to
     * their corresponding order item.
     * @param T
     * @param Order
     * @return [OrderEntity]
     */
    fun <T : OrderEntity> createOrderItemFromCart(cartItem: T, order: Order): OrderEntity {
        return when (cartItem) {
            is FoodCartItem -> {
                FoodOrderItem().apply outer@{
                    this.food = cartItem.food
                    this.complement = cartItem.complement
                    this.size = cartItem.size
                    this.addOns = cartItem.addOns.toMutableList()
                    this.quantity = cartItem.quantity
                    this.orderType = cartItem.orderType
                    this.vendor = cartItem.vendor
                    this.order = order

                    val currentDate = Date()
                    val activeAppliedDiscounts = cartItem.appliedDiscounts.filter { applied ->
                        val discount = applied.discount
                        val start = discount.startDate
                        val end = discount.endDate

                        when {
                            start == null && end == null -> true
                            start == null -> currentDate.before(end)
                            end == null -> currentDate.after(start)
                            else -> currentDate.after(start) && currentDate.before(end)
                        }
                    }

                    activeAppliedDiscounts.forEach { activeDiscount ->
                        val appliedCopy = AppliedDiscount().apply {
                            discount = activeDiscount.discount
                            this.foodOrderItem = this@outer
                        }
                        this@outer.appliedDiscounts.add(appliedCopy)
                    }

                    this.totalAmount = this.calculateTotal()
                    cartItem.cart?.removeItem(cartItem) // remove from cart
                }
            }

            is BeverageCartItem -> {
                BeverageOrderItem().apply {
                    this.beverage = cartItem.beverage
                    this.vendor = cartItem.vendor
                    this.quantity = cartItem.quantity
                    this.order = order
                    this.orderType = cartItem.orderType
                    this.totalAmount = this.calculateTotal()
                    cartItem.cart?.removeItem(cartItem) // remove from cart
                }
            }

            else -> throw IllegalArgumentException("Unknown cart item type")
        }
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
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = [Exception::class])
    fun cancelOrUpdateOrder(
        orderId: UUID,
        foodOrderUpdateDto: FoodOrderUpdateDto,
        userDetails: UserSecurity,
        maxAttempts: Int = 3
    ): Order {
        val role = securityUtility.getSingleRole(userDetails)
        var attempt = 0

        while (attempt < maxAttempts) {
            try {
                val order = when (role) {
                    "CUSTOMER" -> handleCustomerCancellation(orderId, userDetails.id)
                    "VENDOR" -> handleVendorOrderUpdate(orderId, foodOrderUpdateDto, userDetails.id)
                    else -> throw PermissionDeniedException("Unrecognized role: $role")
                }

                // Triggers optimistic lock
                orderRepository.save(order)
                return orderRepository.findByIdWithAllRelations(orderId)
                    .orElseThrow { IllegalArgumentException("An unexpected error occurred updating the order.") }

            } catch (ex: OptimisticLockingFailureException) {
                attempt++
                if (attempt >= maxAttempts) throw IllegalStateException("Oder update failed after $maxAttempts retries. Please retry. ${ex.message}.")

                // Delay the thread before retrying
                Thread.sleep(50L)
            } catch (ex: Exception) {
                logger.error(
                    "Failed to cancel/update order. orderId={}, userId={}, role={}, payload={}, cause={}",
                    orderId, userDetails.id, role, foodOrderUpdateDto, ex.message
                    , ex)
                throw ex
            }
        }
        throw IllegalStateException("Failed to update order after $maxAttempts retries.")
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
        userDetails: UserSecurity,
        maxAttempts: Int = 3
    ) {
        var attempt = 0

        while (attempt < maxAttempts) {
            try {
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

                if (order.customerDeletedStatus == true && order.vendorDeletedStatus == true) {
                    order.customer = null
                    order.vendor = null
                    orderRepository.delete(order)
                } else {

                    // Triggers optimistic lock mechanism
                    orderRepository.save(order)
                }
                return // Success, exists to loop

            } catch (ex: OptimisticLockingFailureException) {
                attempt++
                if (attempt >= maxAttempts) throw IllegalStateException("Order update conflict, Please try again. ${ex.message}.")

                // Delay the thread before retrying
                Thread.sleep(50L)
            }
        }
    }


    // Helper methods
    private fun handleCustomerCancellation(orderId: UUID, customerId: UUID): Order {
        val order: Order = orderRepository.findByIdAndCustomerIdAndOrderStatus(orderId, customerId, OrderStatus.PENDING)
            ?: throw RequestedEntityNotFoundException("Order not found or has already been confirmed.")
        order.orderStatus = OrderStatus.CANCELLED
        return order
    }

    private fun handleVendorOrderUpdate(
        orderId: UUID,
        foodOrderUpdateDto: FoodOrderUpdateDto,
        vendorId: UUID

    ) : Order {
        val order: Order = orderRepository.findByIdAndVendorIdAndOrderStatus(orderId, vendorId, OrderStatus.PENDING)
            ?: throw RequestedEntityNotFoundException("Order not found or has already been confirmed or cancelled.")

        when (order.orderStatus) {
            OrderStatus.PENDING -> handlePendingOrder(order, foodOrderUpdateDto)
            OrderStatus.CONFIRMED -> handleConfirmedOrder(order)
            OrderStatus.EN_ROUTE -> markOrderAsDelivered(order)
            OrderStatus.READY -> markReadyOrderAsCompleted(order)
            else -> throw IllegalArgumentException("Invalid operation for order status: ${order.orderStatus}.")
        }
        return order
    }

    private fun handlePendingOrder(order: Order, foodOrderUpdateDto: FoodOrderUpdateDto) {
        when(foodOrderUpdateDto.orderStatus) {
            OrderStatus.CONFIRMED -> order.orderStatus = OrderStatus.CONFIRMED
            OrderStatus.DECLINED -> order.orderStatus = OrderStatus.DECLINED
            else -> throw IllegalArgumentException("Unsupported status: ${foodOrderUpdateDto.orderStatus}.")
        }
        order.responseTime = LocalDateTime.now()
        if (order.orderType != OrderType.DELIVERY) {
            order.readyBy = calculateOrderReadyTime(order)
        }
    }

    private fun handleConfirmedOrder(order: Order) {
        order.orderStatus = when(order.orderType) {
            OrderType.DELIVERY -> OrderStatus.EN_ROUTE
            OrderType.DINE_IN, OrderType.PICKUP -> OrderStatus.READY
            else -> throw IllegalArgumentException("Invalid order type: ${order.orderType}.")
        }
    }

    private fun markOrderAsDelivered(order: Order) {
        order.orderStatus = OrderStatus.DELIVERED
        order.completedTime = LocalDateTime.now()
    }

    private fun markReadyOrderAsCompleted(order: Order) {
        order.orderStatus = when (order.orderType) {
            OrderType.DINE_IN -> OrderStatus.SERVED
            OrderType.PICKUP -> OrderStatus.COLLECTED
            else -> throw IllegalArgumentException("Invalid order type: ${order.orderType}.")
        }
        order.completedTime = LocalDateTime.now()
    }

    @Suppress("unused")
    private fun <T> retryOnOptimisticLock(maxAttempts: Int = 3, block: () -> T): T {
        var attempts = 0
        while (true) {
            try {
                return block()
            } catch (ex: OptimisticLockException) {
                attempts++
                if (attempts >= maxAttempts) throw ex
                logger.warn("OptimisticLockException encountered, retrying {}/{}", attempts, maxAttempts)
                Thread.sleep(50L * attempts) // small backoff
            }
        }
    }


    /**
     * This method calculates the time an [Order] of type
     * [OrderType.DINE_IN] or [OrderType.PICKUP] will be ready.
     *
     * @param Order
     * @return [LocalTime]
     * @throws NullPointerException
     */
    private fun calculateOrderReadyTime(order: Order): LocalTime {
        val maxPreparationTime = order.items
            .maxOfOrNull {
                when(it) {
                    is FoodOrderItem -> it.food.preparationTime ?: 0
                    else -> 0
                }

            } ?: 0

        val readyTime = order.responseTime?.plusMinutes(maxPreparationTime.toLong())
        return readyTime!!.toLocalTime()
    }
}

