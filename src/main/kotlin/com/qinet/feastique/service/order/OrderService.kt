package com.qinet.feastique.service.order

import com.qinet.feastique.common.mapper.toResponse
import com.qinet.feastique.exception.EntityNotDeliverableException
import com.qinet.feastique.exception.PermissionDeniedException
import com.qinet.feastique.exception.RequestedEntityNotFoundException
import com.qinet.feastique.exception.UserNotFoundException
import com.qinet.feastique.model.dto.order.CartItemDto
import com.qinet.feastique.model.dto.order.FoodItemDto
import com.qinet.feastique.model.dto.order.ItemDto
import com.qinet.feastique.model.dto.order.OrderUpdateDto
import com.qinet.feastique.model.entity.discount.AppliedDiscount
import com.qinet.feastique.model.entity.order.Cart
import com.qinet.feastique.model.entity.order.Order
import com.qinet.feastique.model.entity.order.OrderEntity
import com.qinet.feastique.model.entity.order.item.*
import com.qinet.feastique.model.entity.sales.AddOnSale
import com.qinet.feastique.model.entity.sales.BeverageSale
import com.qinet.feastique.model.entity.sales.ComplementSale
import com.qinet.feastique.model.entity.sales.DessertSale
import com.qinet.feastique.model.entity.sales.FoodSale
import com.qinet.feastique.model.enums.OrderStatus
import com.qinet.feastique.model.enums.OrderType
import com.qinet.feastique.repository.address.CustomerAddressRepository
import com.qinet.feastique.repository.consumables.beverage.BeverageRepository
import com.qinet.feastique.repository.consumables.dessert.DessertRepository
import com.qinet.feastique.repository.consumables.food.FoodRepository
import com.qinet.feastique.repository.order.CartRepository
import com.qinet.feastique.repository.order.OrderRepository
import com.qinet.feastique.repository.sales.AddOnSaleRepository
import com.qinet.feastique.repository.sales.BeverageSaleRepository
import com.qinet.feastique.repository.sales.ComplementSaleRepository
import com.qinet.feastique.repository.sales.DessertSaleRepository
import com.qinet.feastique.repository.sales.FoodSaleRepository
import com.qinet.feastique.repository.user.CustomerRepository
import com.qinet.feastique.repository.user.VendorRepository
import com.qinet.feastique.response.order.OrderResponse
import com.qinet.feastique.security.UserSecurity
import com.qinet.feastique.utility.GeneralUtility
import com.qinet.feastique.utility.SecurityUtility
import jakarta.persistence.OptimisticLockException
import org.slf4j.LoggerFactory
import org.springframework.dao.OptimisticLockingFailureException
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.*
import kotlin.jvm.optionals.getOrElse

@Service
class OrderService(
    private val orderRepository: OrderRepository,
    private val customerRepository: CustomerRepository,
    private val beverageRepository: BeverageRepository,
    private val dessertRepository: DessertRepository,
    private val foodRepository: FoodRepository,
    private val vendorRepository: VendorRepository,
    private val customerAddressRepository: CustomerAddressRepository,
    private val securityUtility: SecurityUtility,
    private val cartRepository: CartRepository,
    private val foodSaleRepository: FoodSaleRepository,
    private val addOnSaleRepository: AddOnSaleRepository,
    private val beverageSaleRepository: BeverageSaleRepository,
    private val complementSaleRepository: ComplementSaleRepository,
    private val dessertSaleRepository: DessertSaleRepository
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    @Transactional(readOnly = true)
    fun getOrder(id: UUID, userDetails: UserSecurity): Order? {
        val role = securityUtility.getRole(userDetails)
        val order = when (role) {
            "CUSTOMER" -> orderRepository.findByIdAndCustomerIdAndCustomerDeletedAt(id, userDetails.id, null)
            "VENDOR" -> orderRepository.findByIdAndVendorIdAndVendorDeletedAt(id, userDetails.id, null)
            else -> throw IllegalArgumentException("Invalid role. Contact customer support is issue persist.")

        }
        return order
    }

    @Transactional(readOnly = true)
    fun getAllOrders(userDetails: UserSecurity, page: Int, size: Int): Page<OrderResponse> {
        val role = securityUtility.getRole(userDetails)
        val pageable = PageRequest.of(page, size, Sort.by("placementTime").descending())
        val orders = when (role) {
            "CUSTOMER" -> orderRepository.findAllByCustomerDeletedAtAndCustomerId(null, userDetails.id, pageable).map { it.toResponse() }
            "VENDOR" -> orderRepository.findAllByVendorDeletedAtAndVendorId(null, userDetails.id, pageable).map { it.toResponse() }
            else -> throw IllegalArgumentException("Invalid role. Contact customer support is issue persist.")
        }
        return orders
    }

    @Transactional
    fun saveFoodOrder(order: Order): Order {
        return orderRepository.saveAndFlush(order)
    }

    // Customer specific operations.

    /**
     * This method places a food order and a beverage order (if applicable)
     * directly from the food screen.
     * @param FoodItemDto
     * @param UserSecurity`
     * @return [Order]
     * @throws UserNotFoundException
     * @throws RequestedEntityNotFoundException
     * @throws PermissionDeniedException
     * @throws IllegalArgumentException
     */
    @Transactional
    fun placeOrderFromItemScreen(itemDto: ItemDto, customerDetails: UserSecurity): Order {
        val customer = customerRepository.findById(customerDetails.id)
            .orElseThrow {
                throw UserNotFoundException("An unexpected error occurred. Customer account not found.")
            }

        val customerAddress = customer.address.firstOrNull { it.id == itemDto.customerAddressId }
            ?: throw RequestedEntityNotFoundException("Unable to place order. Error assigning address.")

        // Generating internal order id and user oder code
        val orderId = GeneralUtility.OrderIdGenerator.generate()

        // Creating a new Order object
        val newOrder = Order().apply {
            this.internalOrderId = orderId.internalOrderId
            this.userOrderCode = orderId.userOrderCode
            this.customer = customer
            this.placementTime = LocalDateTime.now()
            this.quickDelivery = itemDto.quickDelivery
            this.orderStatus = OrderStatus.PENDING
            this.orderType = OrderType.fromString(itemDto.orderType)
        }

        // Determine which item to order
        when {
            itemDto.beverageItemDto != null -> prepareBeverageOrderItem(itemDto, newOrder)
            itemDto.dessertItemDto != null -> prepareDessertOrderItem(itemDto, newOrder)
            itemDto.foodItemDto != null -> prepareFoodOrderItem(itemDto, newOrder)
        }

        // check if delivery fee is applicable to the order
        if (newOrder.orderType == OrderType.DELIVERY) {
            newOrder.customerAddress = customerAddress

        } else {
            newOrder.deliveryFee = 0
        }
        calculateOrderReadyTime(newOrder)

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
                this.customerAddress = if (orderType == OrderType.DELIVERY) {
                    deliveryAddress ?: throw RequestedEntityNotFoundException("Delivery address not found for delivery order.")
                } else {
                    null
                }

                this.quickDelivery = cartItemDto.quickDelivery
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

        if (cart.foodCartItems.isEmpty() && cart.beverageCartItems.isEmpty() && cart.dessertCartItems.isEmpty()) {
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
        val currentDate = Date()
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
                BeverageOrderItem().apply outer@{
                    this.beverage = cartItem.beverage
                    this.beverageFlavour = cartItem.beverageFlavour
                    this.beverageFlavourSize = cartItem.beverageFlavourSize
                    this.vendor = cartItem.vendor
                    this.quantity = cartItem.quantity
                    this.order = order
                    this.orderType = cartItem.orderType

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
                            this.beverageOrderItem = this@outer
                        }
                        this@outer.appliedDiscounts.add(appliedCopy)
                    }

                    this.totalAmount = this.calculateTotal()
                    cartItem.cart?.removeItem(cartItem) // remove from cart
                }
            }

            is DessertCartItem -> {
                DessertOrderItem().apply outer@{
                    this.dessert = cartItem.dessert
                    this.dessertFlavour = cartItem.dessertFlavour
                    this.dessertFlavourSize = cartItem.dessertFlavourSize
                    this.vendor = cartItem.vendor
                    this.quantity = cartItem.quantity
                    this.order = order
                    this.orderType = cartItem.orderType

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
                            this.dessertOrderItem = this@outer
                        }
                        this@outer.appliedDiscounts.add(appliedCopy)
                    }
                    this.totalAmount = this.calculateTotal()
                    cartItem.cart?.removeItem(cartItem)
                }
            }

            else -> throw IllegalArgumentException("Unknown cart item type")
        }
    }


    /**
     * The method is used to change the status of an order.
     * @param UUID
     * @param OrderUpdateDto
     * @param UserSecurity`
     * @return [Order]
     * @throws RequestedEntityNotFoundException
     * @throws PermissionDeniedException
     * @throws IllegalArgumentException
     */
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = [Exception::class])
    fun cancelOrUpdateOrder(
        orderId: UUID,
        orderUpdateDto: OrderUpdateDto,
        userDetails: UserSecurity,
        maxAttempts: Int = 3
    ): Order {
        val role = securityUtility.getSingleRole(userDetails)
        var attempt = 0

        while (attempt < maxAttempts) {
            try {
                val order = when (role) {
                    "CUSTOMER" -> handleCustomerCancellation(orderId, userDetails.id)
                    "VENDOR" -> handleVendorOrderUpdate(orderId, orderUpdateDto, userDetails.id)
                    else -> throw PermissionDeniedException("Unrecognized role: $role")
                }

                // Triggers optimistic lock
                orderRepository.save(order)
                return orderRepository.findByIdWithAllRelations(orderId)
                    .orElseThrow { IllegalArgumentException("An unexpected error occurred updating the order.") }

            } catch (ex: OptimisticLockingFailureException) {
                attempt++
                if (attempt >= maxAttempts) throw IllegalStateException("Order update failed after " +
                        "$maxAttempts retries. Please retry. ${ex.message}.")

                // Delay the thread before retrying
                Thread.sleep(50L)
            } catch (ex: Exception) {
                logger.error(
                    "Failed to cancel/update order. orderId={}, userId={}, role={}, payload={}, cause={}",
                    orderId, userDetails.id, role, orderUpdateDto, ex.message, ex
                )
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
                        order.customerDeletedAt = LocalDateTime.now()
                    }

                    "VENDOR" -> {
                        if (order?.vendor?.id != userDetails.id) {
                            throw PermissionDeniedException("You do not have there permission to delete this order.")
                        }
                        order.vendorDeletedAt = LocalDateTime.now()
                    }
                }

                if (order.customerDeletedAt != null && order.vendorDeletedAt != null) {
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
        order.responseTime = LocalDateTime.now()
        return order
    }

    private fun handleVendorOrderUpdate(
        orderId: UUID,
        orderUpdateDto: OrderUpdateDto,
        vendorId: UUID

    ): Order {
        val order: Order = orderRepository.findByIdAndVendorIdAndOrderStatus(orderId, vendorId, OrderStatus.PENDING)
            ?: throw RequestedEntityNotFoundException("Order not found or has already been confirmed or cancelled.")

        when (order.orderStatus) {
            OrderStatus.PENDING -> handlePendingOrder(order, orderUpdateDto)
            OrderStatus.CONFIRMED -> handleConfirmedOrder(order)
            OrderStatus.EN_ROUTE -> markOrderAsDelivered(order)
            OrderStatus.READY -> markReadyOrderAsCompleted(order)
            else -> throw IllegalArgumentException("Invalid operation for order status: ${order.orderStatus}.")
        }
        return order
    }

    private fun handlePendingOrder(order: Order, orderUpdateDto: OrderUpdateDto) {
        when (orderUpdateDto.orderStatus) {
            OrderStatus.CONFIRMED -> {
                order.orderStatus = OrderStatus.CONFIRMED

                calculateOrderReadyTime(order)

                // record as a sale
                recordBeverageSale(order)
                recordDessertSale(order)
                recordFoodSale(order)
            }

            OrderStatus.DECLINED -> order.orderStatus = OrderStatus.DECLINED
            else -> throw IllegalArgumentException("Unsupported status: ${orderUpdateDto.orderStatus}.")
        }
        order.responseTime = LocalDateTime.now()

    }

    private fun handleConfirmedOrder(order: Order) {
        order.orderStatus = when (order.orderType) {
            OrderType.DELIVERY -> OrderStatus.EN_ROUTE
            OrderType.DINE_IN, OrderType.PICKUP -> OrderStatus.READY
            else -> throw IllegalArgumentException("Invalid order type: ${order.orderType}.")
        }

        calculateOrderReadyTime(order)
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

    private fun recordFoodSale(order: Order) {
        val vendor = order.vendor ?: throw IllegalStateException("Order does not have an associated vendor.")
        var vendorBalance = vendor.balance
        val foodOrderItems = order.foodOrderItems
            .takeIf { it.isNotEmpty() }
            ?: return // nothing to record

        val foodSales = foodOrderItems.map { foodOrderItem ->
            FoodSale().apply {
                food = foodOrderItem.food
                quantity = foodOrderItem.quantity
                amount = foodOrderItem.totalAmount
                this.vendor = vendor
                saleDate = order.responseTime ?: LocalDateTime.now()
                this.foodOrderItem = foodOrderItem

                vendorBalance += foodOrderItem.totalAmount ?: 0L
            }
        }

        recordComplementSale(order) // record complement
        recordAddOnSale(order) // record add-ons

        vendorBalance += order.deliveryFee ?: 0
        vendor.balance = vendorBalance
        vendorRepository.save(vendor)
        foodSaleRepository.saveAll(foodSales)
    }

    private fun recordComplementSale(order: Order) {
        val vendor = order.vendor ?: throw IllegalStateException("Order does not have an associated vendor.")
        val foodOrderItems = order.foodOrderItems
            .takeIf { it.isNotEmpty() }
            ?: return // nothing to record

        val complementSales = foodOrderItems.map { foodOrderItem ->
            val price = foodOrderItem.complement.price ?: 0L
            val quantity = foodOrderItem.quantity

            ComplementSale().apply {
                complement = foodOrderItem.complement
                this.quantity = quantity
                amount = price.times(quantity)
                this.vendor = vendor
                saleDate = order.responseTime ?: LocalDateTime.now()
                this.foodOrderItem = foodOrderItem
            }
        }
        complementSaleRepository.saveAll(complementSales)
    }

    private fun recordAddOnSale(order: Order) {
        val vendor = order.vendor ?: throw IllegalStateException("Order does not have an associated vendor.")
        val foodOrderItems = order.foodOrderItems
            .takeIf { it.isNotEmpty() }
            ?: return // nothing to record

        val addOnSales: MutableList<AddOnSale> = mutableListOf()
        foodOrderItems.forEach { foodOrderItem ->
            for (addOn in foodOrderItem.addOns) {
                val price = addOn.price ?: 0L
                val quantity = foodOrderItem.quantity
                val addOnSale = AddOnSale().apply {
                    this.addOn = addOn
                    this.quantity = quantity
                    this.amount = price.times(quantity)
                    this.vendor = vendor
                    saleDate = order.responseTime ?: LocalDateTime.now()
                    this.foodOrderItem = foodOrderItem
                }
                addOnSales.add(addOnSale)
            }
        }

        addOnSaleRepository.saveAll(addOnSales)
    }

    private fun recordBeverageSale(order: Order) {
        val vendor = order.vendor ?: throw IllegalStateException("Order does not have an associated vendor.")
        var vendorBalance = vendor.balance
        val beverageOrderItems = order.beverageOrderItems
            .takeIf { it.isNotEmpty() }
            ?: return // nothing to record

        val beverageSales = beverageOrderItems.map { beverageOrderItem ->
            BeverageSale().apply {
                beverage = beverageOrderItem.beverage
                quantity = beverageOrderItem.quantity
                amount = beverageOrderItem.totalAmount
                this.vendor = vendor
                saleDate = order.responseTime ?: LocalDateTime.now()
                this.beverageOrderItem = beverageOrderItem

                vendorBalance += beverageOrderItem.totalAmount ?: 0L
            }
        }
        vendor.balance = vendorBalance
        vendorRepository.save(vendor)
        beverageSaleRepository.saveAll(beverageSales)
    }

    private fun recordDessertSale(order: Order) {
        val vendor = order.vendor ?: throw IllegalStateException("Order does not have an associated vendor.")
        var vendorBalance = vendor.balance
        val dessertOrderItems = order.dessertOrderItems
            .takeIf { it.isNotEmpty() }
            ?: return // nothing to record

        val dessertSales = dessertOrderItems.map { dessertOrderItem ->
            DessertSale().apply {
                dessert = dessertOrderItem.dessert
                quantity = dessertOrderItem.quantity
                amount = dessertOrderItem.totalAmount
                this.vendor = vendor
                saleDate = order.responseTime ?: LocalDateTime.now()
                this.dessertOrderItem = dessertOrderItem

                vendorBalance += dessertOrderItem.totalAmount ?: 0L
            }
        }

        vendor.balance = vendorBalance
        vendorRepository.saveAndFlush(vendor)
        dessertSaleRepository.saveAllAndFlush(dessertSales)
    }


    /**
     * Prepare and attach a [BeverageOrderItem] to the provided [order] using data from [itemDto].
     *
     * The method:
     * - Loads the referenced beverage, flavour and flavour size from their repositories,
     * - Validates presence of vendor and assigns it to the order,
     * - Builds a new [BeverageOrderItem], applies any active discounts and computes its total,
     * - Enforces delivery rules (prevents ordering a low-priced beverage alone for delivery),
     * - Adds the prepared item to the given [order].
     *
     * @param itemDto DTO containing beverage selection and quantity information.
     * @param order Order to which the prepared beverage item will be added.
     * @throws RequestedEntityNotFoundException if the beverage, flavour, or flavour size cannot be found.
     * @throws UserNotFoundException if the beverage vendor cannot be loaded.
     * @throws EntityNotDeliverableException if the beverage cannot be ordered alone for delivery (price < 5000).
     */
    private fun prepareBeverageOrderItem(itemDto: ItemDto, order: Order) {
        val beverageDto = itemDto.beverageItemDto!!
        val beverage = beverageRepository.findById(beverageDto.beverageId)
            .orElseThrow {
                throw RequestedEntityNotFoundException("Unable to place order. Beverage not found.")
            }

        val beverageFlavour = beverage.beverageFlavours.firstOrNull { it.id == beverageDto.beverageFlavourId }
            ?: throw RequestedEntityNotFoundException("Unable to place order. Beverage flavour not found.")

        val beverageFlavourSize = beverageFlavour.beverageFlavourSizes.firstOrNull { it.id == beverageDto.id }
            ?: throw RequestedEntityNotFoundException("Unable to place order. Beverage flavour size not found.")

        val vendor = vendorRepository.findById(beverage.vendor.id)
            .orElseThrow { throw UserNotFoundException("Unable to place order. Vendor not found.") }
        order.vendor = vendor

        val newBeverageOrderItem = BeverageOrderItem().apply {
            this.order = order
            this.vendor = beverage.vendor
            this.beverage = beverage
            this.beverageFlavour = beverageFlavour
            this.beverageFlavourSize = beverageFlavourSize
            this.quantity = beverageDto.quantity ?: 1
            this.orderType = OrderType.fromString(itemDto.orderType)
        }

        if (beverage.beverageDiscounts.isNotEmpty()) {
            prepareAppliedDiscounts(newBeverageOrderItem)
        }
        newBeverageOrderItem.totalAmount = newBeverageOrderItem.calculateTotal()

        if (newBeverageOrderItem.beverageFlavourSize.price!! < 2000 && order.orderType == OrderType.DELIVERY) {
            throw EntityNotDeliverableException("Beverage cannot be ordered alone.")
        }

        order.addItem(newBeverageOrderItem)
    }

    /**
     * Prepare and attach a [DessertOrderItem] to the provided [order] using data from [itemDto].
     *
     * The method:
     * - Loads the referenced dessert, flavour and flavour size from the repository,
     * - Validates presence of vendor and assigns it to the order,
     * - Builds a new [DessertOrderItem], applies any active discounts and computes its total,
     * - Enforces delivery rules (prevents ordering non\-deliverable desserts for delivery),
     * - Adds the prepared item to the given [order].
     *
     * @param itemDto DTO containing dessert selection and quantity information.
     * @param order Order to which the prepared dessert item will be added.
     * @throws RequestedEntityNotFoundException if the dessert, flavour, or flavour size cannot be found.
     * @throws UserNotFoundException if the dessert vendor cannot be loaded.
     * @throws IllegalArgumentException if the dessert is not deliverable but the order type is `DELIVERY`.
     */
    private fun prepareDessertOrderItem(itemDto: ItemDto, order: Order) {
        val dessertItemDto = itemDto.dessertItemDto!!
        val dessert = dessertRepository.findById(dessertItemDto.dessertId)
            .orElseThrow {
                throw RequestedEntityNotFoundException("Unable to place order. Dessert not found.")
            }

        val dessertFlavour = dessert.dessertFlavours.firstOrNull { it.id == dessertItemDto.dessertFlavourId }
            ?: throw RequestedEntityNotFoundException("Unable to place order. Dessert flavour not found.")

        val dessertFlavourSize = dessertFlavour.dessertFlavourSizes.firstOrNull { it.id == dessertItemDto.dessertFlavourSizeId }
            ?: throw RequestedEntityNotFoundException("Unable to place order. Dessert flavour size not found.")

        val vendor = vendorRepository.findById(dessert.vendor.id)
            .orElseThrow { throw UserNotFoundException("Unable to place order. Vendor not found.") }
        order.vendor = vendor

        val newDessertOrderItem = DessertOrderItem().apply {
            this.order = order
            this.vendor = dessert.vendor
            this.dessert = dessert
            this.dessertFlavour = dessertFlavour
            this.dessertFlavourSize = dessertFlavourSize
            this.quantity = dessertItemDto.quantity ?: 1
            this.orderType = OrderType.fromString(itemDto.orderType)
        }

        if (dessert.dessertDiscounts.isNotEmpty()) {
            prepareAppliedDiscounts(newDessertOrderItem)
        }

        newDessertOrderItem.totalAmount = newDessertOrderItem.calculateTotal()

        // if dessert is not deliverable, order type cannot be delivery
        if (!dessert.deliverable!! && order.orderType == OrderType.DELIVERY) {
            throw IllegalArgumentException("Dessert is not deliverable. Change order type.")
        }
        order.addItem(newDessertOrderItem)
    }

    /**
     * Prepare and attach a [FoodOrderItem] to the provided [order] using data from [itemDto].
     *
     * The method:
     * - Loads the referenced food (with all relations) from the repository,
     * - Validates presence of vendor and assigns it to the order,
     * - Builds a new [FoodOrderItem], assigns complement and requested add-ons, applies any active discounts and computes its total,
     * - Enforces delivery rules (prevents ordering non-deliverable food for delivery),
     * - Adds the prepared item to the given [order].
     *
     * @param itemDto DTO containing food selection, size id, complement id, add-on ids and quantity.
     * @param order Order to which the prepared food item will be added.
     * @throws RequestedEntityNotFoundException if the food or any referenced add-on/complement/address cannot be found.
     * @throws UserNotFoundException if the food vendor cannot be loaded.
     * @throws IllegalArgumentException if complement resolution fails or if the food is not deliverable but the order type is `DELIVERY`.
     */
    private fun prepareFoodOrderItem(itemDto: ItemDto, order: Order) {
        // Creating a new food order item
        val foodItemDto = itemDto.foodItemDto!!
        val food = foodRepository.findByIdWithAllRelations(foodItemDto.foodId)
            .orElseThrow {
                throw RequestedEntityNotFoundException("Unable to place order. Food not found.")
            }
        val vendor = vendorRepository.findById(food.vendor.id)
            .orElseThrow { throw UserNotFoundException("Unable to place order. Vendor not found.") }
        order.vendor = vendor

        val newFoodOrderItem = FoodOrderItem().apply {
            this.order = order
            this.vendor = food.vendor
            this.food = food
            this.quantity = foodItemDto.foodQuantity ?: 1
            this.size = food.foodSizes.find { it.id == foodItemDto.foodSizeId }!!
            this.orderType = OrderType.fromString(itemDto.orderType)
        }

        // Assigning a complement
        // Get the matching complement from the back reference.
        val matchingComplement =
            food.foodComplements.firstOrNull { it.complement.id == foodItemDto.complementId }?.complement
                ?: throw IllegalArgumentException("Unable to place order. Complement cannot be gotten from food.")

        newFoodOrderItem.complement = matchingComplement

        // Assigning add-ons if list is not null
        foodItemDto.addOnIds?.let {

            // Return id of matching add-ons
            val matchingAddOnIds = food.foodAddOns.map { it.addOn } // remove nulls that could be present
                .filter { it.id in foodItemDto.addOnIds!! }
                .takeIf { it.isNotEmpty() }
                ?: throw RequestedEntityNotFoundException("Unable to place order. Add-on not found.")

            newFoodOrderItem.addOns.addAll(matchingAddOnIds)
        }

        if (food.foodDiscounts.isNotEmpty()) {
            prepareAppliedDiscounts(newFoodOrderItem)
        }
        newFoodOrderItem.totalAmount = newFoodOrderItem.calculateTotal()

        // Check if food is deliverable via backreference.
        // If a food cannot be delivered,the order type cannot be delivery
        if (!food.deliverable!! && order.orderType == OrderType.DELIVERY) {
            throw IllegalArgumentException("Food is not deliverable. Please change order type.")
        }
        order.addItem(newFoodOrderItem)
    }

    private fun prepareAppliedDiscounts(orderItem: OrderEntity) {
        val currentDate = Date()

        when(orderItem) {
            is BeverageOrderItem -> {
                val activeAppliedDiscounts = orderItem.appliedDiscounts.filter { applied ->
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
                        this.beverageOrderItem = orderItem
                    }
                    orderItem.appliedDiscounts.add(appliedCopy)
                }
            }

            is DessertOrderItem -> {
                val activeAppliedDiscounts = orderItem.appliedDiscounts.filter { applied ->
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
                        this.dessertOrderItem = orderItem
                    }
                    orderItem.appliedDiscounts.add(appliedCopy)
                }
            }

            is FoodOrderItem -> {
                val activeAppliedDiscounts = orderItem.appliedDiscounts.filter { applied ->
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
                        this.foodOrderItem = orderItem
                    }
                    orderItem.appliedDiscounts.add(appliedCopy)
                }
            }
        }

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

    private fun calculateOrderReadyTime(order: Order) {
        val currentTime = LocalTime.now()
        val vendor = order.vendor!!
        val openingTime = vendor.openingTime!!
        val closingTime = vendor.closingTime
        val isDelivery = order.orderType == OrderType.DELIVERY
        val hasFoodItems = order.items.any { it is FoodOrderItem }

        // to check if current time is within working house
        val isWithinWorkingHours = currentTime.isAfter(openingTime) && currentTime.isBefore(closingTime)

        // Check if any item is available for quick delivery
        val hasQuickDeliveryItem = order.items.any {
            when (it) {
                is BeverageOrderItem -> it.beverage.quickDelivery == true
                is DessertOrderItem -> it.dessert.quickDelivery == true
                is FoodOrderItem -> it.food.quickDelivery == true
                else -> false
            }
        }

        // For pending orders, calculate tentative ready time/delivery time
        if (order.orderStatus == OrderStatus.PENDING) {

            // non-delivery (pickup or dine-in)
            if (!isDelivery) {
                val maxPreparationTime = order.items.maxOfOrNull {
                    when (it) {
                        is BeverageOrderItem -> it.beverage.preparationTime ?: 0
                        is FoodOrderItem -> it.food.preparationTime ?: 0
                        is DessertOrderItem -> it.dessert.preparationTime ?: 0
                        else -> 0
                    }

                }?.toLong() ?: 0

                if (isWithinWorkingHours) {
                    order.readyBy = currentTime.plusMinutes(maxPreparationTime)

                } else {
                    order.readyBy = openingTime.plusMinutes(maxPreparationTime)
                }
            }

            // for delivery orders
            // Quick delivery: order.quickDelivery is true AND at least one item supports quick delivery
            val isQuickDelivery = (isDelivery && hasQuickDeliveryItem)
            if (isQuickDelivery) {

                val itemReadyTimes = order.items.map { item ->
                    val (readyAsFrom, preparationTime) = when (item) {
                        is BeverageOrderItem -> item.beverage.readyAsFrom to item.beverage.preparationTime
                        is DessertOrderItem -> item.dessert.readyAsFrom to item.dessert.preparationTime
                        is FoodOrderItem -> item.food.readyAsFrom to item.food.preparationTime
                        else -> openingTime to 0
                    }

                    // if readyAsFrom is null, use openingTime
                    val startTime = readyAsFrom ?: openingTime
                    val prepTime = preparationTime ?: 0

                    // add preparation time (in minutes) to the readyAsFrom time
                    startTime.plusMinutes(prepTime.toLong())
                }

                // Get the farthest ready time and if null, use vendor opening time
                val maxItemReadyTime = itemReadyTimes.maxOrNull() ?: openingTime

                // Get maximum readyAsFrom from all items
                val maxReadyAsFrom = order.items.mapNotNull {
                    when (it) {
                        is BeverageOrderItem -> it.beverage.readyAsFrom
                        is DessertOrderItem -> it.dessert.readyAsFrom
                        is FoodOrderItem -> it.food.readyAsFrom
                        else -> null
                    }
                }.maxOrNull() ?: openingTime

                // if current time is greater than all readyAsFrom times, use currentTime + maxPreparationTime
                if (currentTime > maxReadyAsFrom) {
                    val maxPreparationTime = order.items.maxOfOrNull {
                        when (it) {
                            is BeverageOrderItem -> it.beverage.preparationTime ?: 0
                            is FoodOrderItem -> it.food.preparationTime ?: 0
                            is DessertOrderItem -> it.dessert.preparationTime ?: 0
                            else -> 0
                        }

                    }?.toLong() ?: 0
                    order.deliveryTime = currentTime.plusMinutes(maxPreparationTime)

                } else {
                    order.deliveryTime = maxItemReadyTime
                }
            }

            // for standard delivery orders
            val maxPreparationTime = order.items.maxOfOrNull {
                when (it) {
                    is BeverageOrderItem -> it.beverage.preparationTime ?: 0
                    is FoodOrderItem -> it.food.preparationTime ?: 0
                    is DessertOrderItem -> it.dessert.preparationTime ?: 0
                    else -> 0
                }

            }?.toLong() ?: 0

            if (isWithinWorkingHours) {
                order.deliveryTime = currentTime.plusMinutes(maxPreparationTime)
            } else {
                order.deliveryTime = openingTime.plusMinutes(maxPreparationTime)
            }

            // for Confirmed orders, recalculate with current time for
            // more accurate estimates.
        } else if (order.orderStatus == OrderStatus.CONFIRMED) {

            // check if it is a standard delivery
            // 1. Marked as delivery
            // 2. Marked as a not being quick delivery
            // 3. Have at least one food item
            val isStandardDelivery = isDelivery && hasFoodItems && !order.quickDelivery

            if (isStandardDelivery) {
                val foodDeliveryTimes = order.items.mapNotNull {
                    if (it is FoodOrderItem) it.food.deliveryTime else null
                }

                val maxDeliveryTime = foodDeliveryTimes.maxOrNull()

                if (maxDeliveryTime != null && currentTime > maxDeliveryTime) {
                    val maxPreparationTime = order.items.maxOfOrNull {
                        when (it) {
                            is BeverageOrderItem -> it.beverage.preparationTime ?: 0
                            is FoodOrderItem -> it.food.preparationTime ?: 0
                            is DessertOrderItem -> it.dessert.preparationTime ?: 0
                            else -> 0
                        }

                    }?.toLong() ?: 0

                    order.deliveryTime = currentTime.plusMinutes(maxPreparationTime)
                } else {
                    order.deliveryTime = maxDeliveryTime ?: currentTime
                }
            }

            // For pickup, dine-in, and quick delivery, use max preparation time, quick delivery is assigned to deliveryTime
            val maxPreparationTime = order.items.maxOfOrNull {
                when (it) {
                    is BeverageOrderItem -> it.beverage.preparationTime ?: 0
                    is FoodOrderItem -> it.food.preparationTime ?: 0
                    is DessertOrderItem -> it.dessert.preparationTime ?: 0
                    else -> 0
                }

            }?.toLong() ?: 0

            order.readyBy = currentTime.plusMinutes(maxPreparationTime)
            if (order.quickDelivery) {
                order.deliveryTime = order.readyBy
            }
        }

        // Fallback
        // return currentTime

        /*val readyTime = if (order.orderStatus == OrderStatus.PENDING) {

            // Determine if the order is a delivery or not
            if (order.orderType != OrderType.DELIVERY) {

                // Tentative ready time
                if (LocalTime.now() < openingTime) { // if the order was placed outside of working hours
                    openingTime.plusMinutes(maxPreparationTime)
                }

                // if the order was placed within working hours
                LocalDateTime.now().plusMinutes(maxPreparationTime)

            } else {
                if (order.quickDelivery) { // if the order is marked as a quick delivery
                    itemWithMaxReadyAsFrom.plusMinutes(maxPreparationTime)

                } else {

                }
            }

        } else {

            // Confirmed order ready time
            order.responseTime?.plusMinutes(maxPreparationTime)
                ?: throw IllegalStateException("responseTime should not be null for non-pending orders")
        }
        return readyTime!!.toLocalTime()*/
    }
    /*private fun calculateOrderReadyTime(order: Order): LocalTime {
        val currentTime = LocalTime.now()
        val vendor = order.vendor!!
        val openingTime = vendor.openingTime!!
        val closingTime = vendor.closingTime
        val isDelivery = order.orderType == OrderType.DELIVERY
        val hasFoodItems = order.items.any { it is FoodOrderItem }

        // check if current time is within working house
        val isWithinWorkingHours = currentTime.isAfter(openingTime) && currentTime.isBefore(closingTime)

        // Check if any item is available for quick delivery
        val hasQuickDeliveryItem = order.items.any {
            when (it) {
                is BeverageOrderItem -> it.beverage.quickDelivery == true
                is DessertOrderItem -> it.dessert.quickDelivery == true
                is FoodOrderItem -> it.food.quickDelivery == true
                else -> false
            }
        }

        // For pending orders, calculate tentative ready time/delivery time
        if (order.orderStatus == OrderStatus.PENDING) {

            // non-delivery (pickup or dine-in)
            if (!isDelivery) {
                val maxPreparationTime = order.items.maxOfOrNull {
                    when (it) {
                        is BeverageOrderItem -> it.beverage.preparationTime ?: 0
                        is FoodOrderItem -> it.food.preparationTime ?: 0
                        is DessertOrderItem -> it.dessert.preparationTime ?: 0
                        else -> 0
                    }

                }?.toLong() ?: 0

                return if (isWithinWorkingHours) {
                    currentTime.plusMinutes(maxPreparationTime)

                } else {
                    openingTime.plusMinutes(maxPreparationTime)
                }
            }

            // for delivery orders
            // Quick delivery: order.quickDelivery is true AND at least one item supports quick delivery
            val isQuickDelivery = (isDelivery && hasQuickDeliveryItem)
            if (isQuickDelivery) {

                val itemReadyTimes = order.items.map { item ->
                    val (readyAsFrom, preparationTime) = when (item) {
                        is BeverageOrderItem -> item.beverage.readyAsFrom to item.beverage.preparationTime
                        is DessertOrderItem -> item.dessert.readyAsFrom to item.dessert.preparationTime
                        is FoodOrderItem -> item.food.readyAsFrom to item.food.preparationTime
                        else -> openingTime to 0
                    }

                    // if readyAsFrom is null, use openingTime
                    val startTime = readyAsFrom ?: openingTime
                    val prepTime = preparationTime ?: 0

                    // add preparation time (in minutes) to the readyAsFrom time
                    startTime.plusMinutes(prepTime.toLong())
                }

                // Get the farthest ready time and if null, use vendor opening time
                val maxItemReadyTime = itemReadyTimes.maxOrNull() ?: openingTime

                // Get maximum readyAsFrom from all items
                val maxReadyAsFrom = order.items.mapNotNull {
                    when (it) {
                        is BeverageOrderItem -> it.beverage.readyAsFrom
                        is DessertOrderItem -> it.dessert.readyAsFrom
                        is FoodOrderItem -> it.food.readyAsFrom
                        else -> null
                    }
                }.maxOrNull() ?: openingTime

                // if current time is greater than all readyAsFrom times, use currentTime + maxPreparationTime
                return if (currentTime > maxReadyAsFrom) {
                    val maxPreparationTime = order.items.maxOfOrNull {
                        when (it) {
                            is BeverageOrderItem -> it.beverage.preparationTime ?: 0
                            is FoodOrderItem -> it.food.preparationTime ?: 0
                            is DessertOrderItem -> it.dessert.preparationTime ?: 0
                            else -> 0
                        }

                    }?.toLong() ?: 0
                    currentTime.plusMinutes(maxPreparationTime)

                } else {
                    maxItemReadyTime
                }
            }

            // for standard delivery orders
            val maxPreparationTime = order.items.maxOfOrNull {
                when (it) {
                    is BeverageOrderItem -> it.beverage.preparationTime ?: 0
                    is FoodOrderItem -> it.food.preparationTime ?: 0
                    is DessertOrderItem -> it.dessert.preparationTime ?: 0
                    else -> 0
                }

            }?.toLong() ?: 0

            return if (isWithinWorkingHours) {
                currentTime.plusMinutes(maxPreparationTime)
            } else {
                openingTime.plusMinutes(maxPreparationTime)
            }

            // for Confirmed orders, recalculate with current time for
            // more accurate estimates.
        } else if (order.orderStatus == OrderStatus.CONFIRMED) {

            // check if it is a standard delivery
            // 1. Marked as delivery
            // 2. Marked as a not being quick delivery
            // 3. Have at least one food item
            val isStandardDelivery = isDelivery && hasFoodItems && !order.quickDelivery

            if (isStandardDelivery) {
                val foodDeliveryTimes = order.items.mapNotNull {
                    if (it is FoodOrderItem) it.food.deliveryTime else null
                }

                val maxDeliveryTime = foodDeliveryTimes.maxOrNull()

                return if (maxDeliveryTime != null && currentTime > maxDeliveryTime) {
                    val maxPreparationTime = order.items.maxOfOrNull {
                        when (it) {
                            is BeverageOrderItem -> it.beverage.preparationTime ?: 0
                            is FoodOrderItem -> it.food.preparationTime ?: 0
                            is DessertOrderItem -> it.dessert.preparationTime ?: 0
                            else -> 0
                        }

                    }?.toLong() ?: 0

                    currentTime.plusMinutes(maxPreparationTime)
                } else {
                    maxDeliveryTime ?: currentTime
                }
            }

            // For pickup, dine-in, and quick delivery, use max preparation time, quick delivery is assigned to deliveryTime
            val maxPreparationTime = order.items.maxOfOrNull {
                when (it) {
                    is BeverageOrderItem -> it.beverage.preparationTime ?: 0
                    is FoodOrderItem -> it.food.preparationTime ?: 0
                    is DessertOrderItem -> it.dessert.preparationTime ?: 0
                    else -> 0
                }

            }?.toLong() ?: 0

            return currentTime.plusMinutes(maxPreparationTime)
        }

        // Fallback
        return currentTime

        *//*val readyTime = if (order.orderStatus == OrderStatus.PENDING) {

            // Determine if the order is a delivery or not
            if (order.orderType != OrderType.DELIVERY) {

                // Tentative ready time
                if (LocalTime.now() < openingTime) { // if the order was placed outside of working hours
                    openingTime.plusMinutes(maxPreparationTime)
                }

                // if the order was placed within working hours
                LocalDateTime.now().plusMinutes(maxPreparationTime)

            } else {
                if (order.quickDelivery) { // if the order is marked as a quick delivery
                    itemWithMaxReadyAsFrom.plusMinutes(maxPreparationTime)

                } else {

                }
            }

        } else {

            // Confirmed order ready time
            order.responseTime?.plusMinutes(maxPreparationTime)
                ?: throw IllegalStateException("responseTime should not be null for non-pending orders")
        }
        return readyTime!!.toLocalTime()*//*
    }*/
}

