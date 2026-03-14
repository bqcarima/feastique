package com.qinet.feastique.service.order

import com.qinet.feastique.common.mapper.toResponse
import com.qinet.feastique.exception.*
import com.qinet.feastique.model.dto.order.CartItemDto
import com.qinet.feastique.model.dto.order.ItemDto
import com.qinet.feastique.model.dto.order.OrderUpdateDto
import com.qinet.feastique.model.entity.consumables.EdibleEntity
import com.qinet.feastique.model.entity.consumables.flavour.Flavour
import com.qinet.feastique.model.entity.discount.AppliedDiscount
import com.qinet.feastique.model.entity.order.Cart
import com.qinet.feastique.model.entity.order.Order
import com.qinet.feastique.model.entity.order.OrderEntity
import com.qinet.feastique.model.entity.order.item.*
import com.qinet.feastique.model.entity.sales.*
import com.qinet.feastique.model.entity.size.ConsumableSize
import com.qinet.feastique.model.enums.Availability
import com.qinet.feastique.model.enums.Constants
import com.qinet.feastique.model.enums.OrderStatus
import com.qinet.feastique.model.enums.OrderType
import com.qinet.feastique.repository.address.CustomerAddressRepository
import com.qinet.feastique.repository.consumables.beverage.BeverageRepository
import com.qinet.feastique.repository.consumables.dessert.DessertRepository
import com.qinet.feastique.repository.consumables.food.FoodRepository
import com.qinet.feastique.repository.consumables.handheld.HandheldRepository
import com.qinet.feastique.repository.order.CartRepository
import com.qinet.feastique.repository.order.OrderRepository
import com.qinet.feastique.repository.sales.*
import com.qinet.feastique.repository.user.CustomerRepository
import com.qinet.feastique.repository.user.VendorRepository
import com.qinet.feastique.response.order.OrderResponse
import com.qinet.feastique.response.pagination.WindowResponse
import com.qinet.feastique.security.UserSecurity
import com.qinet.feastique.utility.CursorEncoder
import com.qinet.feastique.utility.GeneralUtility
import com.qinet.feastique.utility.SecurityUtility
import org.slf4j.LoggerFactory
import org.springframework.dao.OptimisticLockingFailureException
import org.springframework.data.domain.*
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
    private val dessertSaleRepository: DessertSaleRepository,
    private val handheldRepository: HandheldRepository,
    private val handheldSaleRepository: HandheldSaleRepository,
    private val cursorEncoder: CursorEncoder
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @Transactional(readOnly = true)
    fun getOrder(id: UUID, userDetails: UserSecurity): Order? {
        val role = securityUtility.getRole(userDetails)
        return when (role) {
            "CUSTOMER" -> orderRepository.findByIdAndCustomerIdAndCustomerDeletedAt(id, userDetails.id, null)
            "VENDOR" -> orderRepository.findByIdAndVendorIdAndVendorDeletedAt(id, userDetails.id, null)
            else -> throw IllegalArgumentException("Invalid role. Contact customer support if issue persists.")
        }
    }

    @Transactional(readOnly = true)
    fun getAllOrders(userDetails: UserSecurity, page: Int, size: Int): Page<OrderResponse> {
        securityUtility.getRole(userDetails)
        val pageable = PageRequest.of(page, size, Sort.by("placementTime").descending())
        return orderRepository.findAllByVendorDeletedAtAndVendorId(null, userDetails.id, pageable)
            .map { it.toResponse() }
    }

    @Transactional(readOnly = true)
    fun scrollOrders(
        userDetails: UserSecurity,
        orderStatus: String,
        cursor: String?, // null on first request, offset string on subsequent requests
        size: Int = Constants.DEFAULT_PAGE_SIZE.type
    ): WindowResponse<OrderResponse> {
        val role = securityUtility.getRole(userDetails)
        val status = OrderStatus.fromString(orderStatus)
        val currentOffset: Long = cursor?.toLongOrNull() ?: 0L

        val scrollPosition = if (currentOffset == 0L) ScrollPosition.offset()
        else ScrollPosition.offset(currentOffset)

        val sort = Sort.by("placementTime").descending()

        val window = when (role) {
            "CUSTOMER" -> orderRepository.findAllByCustomerDeletedAtAndCustomerIdAndOrderStatus(
                null, userDetails.id, status, scrollPosition, sort, Limit.of(size)
            )

            "VENDOR" -> orderRepository.findAllByVendorDeletedAtAndVendorIdAndOrderStatus(
                null, userDetails.id, status, scrollPosition, sort, Limit.of(size)
            )

            else -> throw IllegalArgumentException("Invalid role. Contact customer support if issue persists.")
        }.map { it.toResponse() }

        return window.toResponse(currentOffset) { cursorEncoder.encodeOffset(it) }
    }

    @Transactional
    fun saveFoodOrder(order: Order): Order = orderRepository.saveAndFlush(order)

    /** Places an order directly from the item screen. Exactly one item type must be present in [itemDto]. */
    @Transactional
    fun placeOrderFromItemScreen(itemDto: ItemDto, customerDetails: UserSecurity): Order {
        val customer = customerRepository.findById(customerDetails.id)
            .orElseThrow { UserNotFoundException("An unexpected error occurred. Customer account not found.") }

        val customerAddress = customer.address.firstOrNull { it.id == itemDto.customerAddressId }
            ?: throw RequestedEntityNotFoundException("Unable to place order. Error assigning address.")

        val orderId = GeneralUtility.OrderIdGenerator.generate()

        val newOrder = Order().apply {
            this.internalOrderId = orderId.internalOrderId
            this.userOrderCode = orderId.userOrderCode
            this.customer = customer
            this.placementTime = LocalDateTime.now()
            this.quickDelivery = itemDto.quickDelivery
            this.orderStatus = OrderStatus.PENDING
            this.orderType = OrderType.fromString(itemDto.orderType)
        }

        when {
            itemDto.beverageItemDto != null -> prepareBeverageOrderItem(itemDto, newOrder)
            itemDto.dessertItemDto != null -> prepareDessertOrderItem(itemDto, newOrder)
            itemDto.foodItemDto != null -> prepareFoodOrderItem(itemDto, newOrder)
            itemDto.handheldItemDto != null -> prepareHandheldOrderItem(itemDto, newOrder)
        }

        check(newOrder.vendor != null) { "Unable to place order. No item was resolved from the request." }

        if (newOrder.orderType == OrderType.DELIVERY) {
            newOrder.customerAddress = customerAddress
        } else {
            newOrder.deliveryFee = 0
        }

        calculateOrderReadyTime(newOrder)

        val result = newOrder.calculateTotals()
        newOrder.totalAmount = if (newOrder.orderType != OrderType.DELIVERY) result.first else result.third

        val placedOrder = saveFoodOrder(newOrder)
        return orderRepository.findByIdWithAllRelations(placedOrder.id).get()
    }

    /**
     * Processes a cart checkout. Items sharing the same vendor and order type
     * are grouped into a single order.
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

        selectedItems.groupBy { it.vendor to it.orderType }.forEach { (key, itemsForGroup) ->
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
                    deliveryAddress
                        ?: throw RequestedEntityNotFoundException("Delivery address not found for delivery order.")
                } else null
                this.quickDelivery = cartItemDto.quickDelivery
                this.orderStatus = OrderStatus.PENDING
            }

            newOrder.addAllItems(itemsForGroup.map { createOrderItemFromCart(it, newOrder) })

            val totals = newOrder.calculateTotals()
            newOrder.deliveryFee = totals.second
            newOrder.totalAmount = totals.third

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

    /** Maps a cart item to its corresponding order item and removes it from the cart. */
    fun <T : OrderEntity> createOrderItemFromCart(cartItem: T, order: Order): OrderEntity {
        val currentDate = Date()

        fun isDiscountActive(applied: AppliedDiscount): Boolean {
            val start = applied.discount.startDate
            val end = applied.discount.endDate
            return when {
                start == null && end == null -> true
                start == null -> currentDate.before(end)
                end == null -> currentDate.after(start)
                else -> currentDate.after(start) && currentDate.before(end)
            }
        }

        return when (cartItem) {
            is FoodCartItem -> {
                validateAvailability(item = cartItem.food, itemSize = cartItem.size)
                FoodOrderItem().apply outer@{
                    this.food = cartItem.food
                    this.complement = cartItem.complement
                    this.size = cartItem.size
                    this.addOns = cartItem.addOns.toMutableList()
                    this.quantity = cartItem.quantity
                    this.orderType = cartItem.orderType
                    this.vendor = cartItem.vendor
                    this.order = order

                    cartItem.appliedDiscounts.filter { isDiscountActive(it) }.forEach { active ->
                        this@outer.appliedDiscounts.add(AppliedDiscount().apply {
                            discount = active.discount
                            this.foodOrderItem = this@outer
                        })
                    }
                    this.totalAmount = this.calculateTotal()
                    cartItem.cart?.removeItem(cartItem)
                }
            }

            is BeverageCartItem -> {
                validateAvailability(
                    item = cartItem.beverage,
                    itemFlavour = cartItem.beverageFlavour,
                    itemSize = cartItem.beverageFlavourSize
                )
                BeverageOrderItem().apply outer@{
                    this.beverage = cartItem.beverage
                    this.beverageFlavour = cartItem.beverageFlavour
                    this.beverageFlavourSize = cartItem.beverageFlavourSize
                    this.vendor = cartItem.vendor
                    this.quantity = cartItem.quantity
                    this.order = order
                    this.orderType = cartItem.orderType

                    cartItem.appliedDiscounts.filter { isDiscountActive(it) }.forEach { active ->
                        this@outer.appliedDiscounts.add(AppliedDiscount().apply {
                            discount = active.discount
                            this.beverageOrderItem = this@outer
                        })
                    }
                    this.totalAmount = this.calculateTotal()
                    cartItem.cart?.removeItem(cartItem)
                }
            }

            is DessertCartItem -> {
                validateAvailability(
                    item = cartItem.dessert,
                    itemFlavour = cartItem.dessertFlavour,
                    itemSize = cartItem.dessertFlavourSize
                )
                DessertOrderItem().apply outer@{
                    this.dessert = cartItem.dessert
                    this.dessertFlavour = cartItem.dessertFlavour
                    this.dessertFlavourSize = cartItem.dessertFlavourSize
                    this.vendor = cartItem.vendor
                    this.quantity = cartItem.quantity
                    this.order = order
                    this.orderType = cartItem.orderType

                    cartItem.appliedDiscounts.filter { isDiscountActive(it) }.forEach { active ->
                        this@outer.appliedDiscounts.add(AppliedDiscount().apply {
                            discount = active.discount
                            this.dessertOrderItem = this@outer
                        })
                    }
                    this.totalAmount = this.calculateTotal()
                    cartItem.cart?.removeItem(cartItem)
                }
            }

            is HandheldCartItem -> {
                validateAvailability(item = cartItem.handheld, itemSize = cartItem.size)
                HandheldOrderItem().apply outer@{
                    this.handheld = cartItem.handheld
                    this.size = cartItem.size
                    this.fillings = cartItem.fillings
                    this.quantity = cartItem.quantity
                    this.orderType = cartItem.orderType
                    this.vendor = cartItem.vendor
                    this.order = order

                    cartItem.appliedDiscounts.filter { isDiscountActive(it) }.forEach { active ->
                        this@outer.appliedDiscounts.add(AppliedDiscount().apply {
                            discount = active.discount
                            this.handheldOrderItem = this@outer
                        })
                    }
                    this.totalAmount = this.calculateTotal()
                    cartItem.cart?.removeItem(cartItem)
                }
            }

            else -> throw IllegalArgumentException("Unknown cart item type")
        }
    }

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

                orderRepository.save(order)
                return orderRepository.findByIdWithAllRelations(orderId)
                    .orElseThrow { IllegalArgumentException("An unexpected error occurred updating the order.") }

            } catch (ex: OptimisticLockingFailureException) {
                attempt++
                if (attempt >= maxAttempts) throw IllegalStateException(
                    "Order update failed after $maxAttempts retries. Please retry. ${ex.message}."
                )
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

    /** Soft-deletes an order for the calling user. Hard-deletes once both sides have deleted. */
    fun deleteOrder(id: UUID, userDetails: UserSecurity, maxAttempts: Int = 3) {
        var attempt = 0

        while (attempt < maxAttempts) {
            try {
                val role = securityUtility.getRole(userDetails)
                val order = orderRepository.findById(id)
                    .orElseThrow { RequestedEntityNotFoundException("An unexpected error occurred. Unable to delete order.") }

                when (role) {
                    "CUSTOMER" -> {
                        if (order.customer?.id != userDetails.id)
                            throw PermissionDeniedException("You do not have permission to delete this order.")
                        order.customerDeletedAt = LocalDateTime.now()
                    }

                    "VENDOR" -> {
                        if (order.vendor?.id != userDetails.id)
                            throw PermissionDeniedException("You do not have permission to delete this order.")
                        order.vendorDeletedAt = LocalDateTime.now()
                    }
                }

                if (order.customerDeletedAt != null && order.vendorDeletedAt != null) {
                    order.customer = null
                    order.vendor = null
                    orderRepository.delete(order)
                } else {
                    orderRepository.save(order)
                }
                return

            } catch (ex: OptimisticLockingFailureException) {
                attempt++
                if (attempt >= maxAttempts) throw IllegalStateException(
                    "Order update conflict. Please try again. ${ex.message}."
                )
                Thread.sleep(50L)
            }
        }
    }

    // Order status helpers

    private fun handleCustomerCancellation(orderId: UUID, customerId: UUID): Order {
        val order = orderRepository.findByIdAndCustomerIdAndOrderStatus(orderId, customerId, OrderStatus.PENDING)
            ?: throw RequestedEntityNotFoundException("Order not found or has already been confirmed.")
        order.orderStatus = OrderStatus.CANCELLED
        order.responseTime = LocalDateTime.now()
        return order
    }

    private fun handleVendorOrderUpdate(orderId: UUID, orderUpdateDto: OrderUpdateDto, vendorId: UUID): Order {
        val order = orderRepository.findByIdAndVendorIdAndOrderStatus(orderId, vendorId, OrderStatus.PENDING)
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
                recordBeverageSale(order)
                recordDessertSale(order)
                recordFoodSale(order)
                recordHandheldSale(order)
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

    // Sale recording

    private fun recordFoodSale(order: Order) {
        val vendor = order.vendor ?: throw IllegalStateException("Order does not have an associated vendor.")
        var vendorBalance = vendor.balance
        val foodOrderItems = order.foodOrderItems.takeIf { it.isNotEmpty() } ?: return

        val foodSales = foodOrderItems.map { item ->
            FoodSale().apply {
                food = item.food
                quantity = item.quantity
                amount = item.totalAmount
                this.vendor = vendor
                saleDate = order.responseTime ?: LocalDateTime.now()
                this.foodOrderItem = item
                vendorBalance += item.totalAmount ?: 0L
            }
        }

        recordComplementSale(order)
        recordAddOnSale(order)

        vendorBalance += order.deliveryFee ?: 0
        vendor.balance = vendorBalance
        vendorRepository.save(vendor)
        foodSaleRepository.saveAll(foodSales)
    }

    private fun recordComplementSale(order: Order) {
        val vendor = order.vendor ?: throw IllegalStateException("Order does not have an associated vendor.")
        val foodOrderItems = order.foodOrderItems.takeIf { it.isNotEmpty() } ?: return

        complementSaleRepository.saveAll(foodOrderItems.map { item ->
            ComplementSale().apply {
                complement = item.complement
                this.quantity = item.quantity
                amount = (item.complement.price ?: 0L) * item.quantity
                this.vendor = vendor
                saleDate = order.responseTime ?: LocalDateTime.now()
                this.foodOrderItem = item
            }
        })
    }

    private fun recordAddOnSale(order: Order) {
        val vendor = order.vendor ?: throw IllegalStateException("Order does not have an associated vendor.")
        val foodOrderItems = order.foodOrderItems.takeIf { it.isNotEmpty() } ?: return

        val addOnSales = mutableListOf<AddOnSale>()
        foodOrderItems.forEach { item ->
            item.addOns.forEach { addOn ->
                addOnSales.add(AddOnSale().apply {
                    this.addOn = addOn
                    this.quantity = item.quantity
                    this.amount = (addOn.price ?: 0L) * item.quantity
                    this.vendor = vendor
                    saleDate = order.responseTime ?: LocalDateTime.now()
                    this.foodOrderItem = item
                })
            }
        }
        addOnSaleRepository.saveAll(addOnSales)
    }

    private fun recordBeverageSale(order: Order) {
        val vendor = order.vendor ?: throw IllegalStateException("Order does not have an associated vendor.")
        var vendorBalance = vendor.balance
        val beverageOrderItems = order.beverageOrderItems.takeIf { it.isNotEmpty() } ?: return

        val beverageSales = beverageOrderItems.map { item ->
            BeverageSale().apply {
                beverage = item.beverage
                quantity = item.quantity
                amount = item.totalAmount
                this.vendor = vendor
                saleDate = order.responseTime ?: LocalDateTime.now()
                this.beverageOrderItem = item
                vendorBalance += item.totalAmount ?: 0L
            }
        }
        vendor.balance = vendorBalance
        vendorRepository.save(vendor)
        beverageSaleRepository.saveAll(beverageSales)
    }

    private fun recordDessertSale(order: Order) {
        val vendor = order.vendor ?: throw IllegalStateException("Order does not have an associated vendor.")
        var vendorBalance = vendor.balance
        val dessertOrderItems = order.dessertOrderItems.takeIf { it.isNotEmpty() } ?: return

        val dessertSales = dessertOrderItems.map { item ->
            DessertSale().apply {
                dessert = item.dessert
                quantity = item.quantity
                amount = item.totalAmount
                this.vendor = vendor
                saleDate = order.responseTime ?: LocalDateTime.now()
                this.dessertOrderItem = item
                vendorBalance += item.totalAmount ?: 0L
            }
        }
        vendor.balance = vendorBalance
        vendorRepository.saveAndFlush(vendor)
        dessertSaleRepository.saveAllAndFlush(dessertSales)
    }

    private fun recordHandheldSale(order: Order) {
        val vendor = order.vendor ?: throw IllegalStateException("Order does not have an associated vendor.")
        var vendorBalance = vendor.balance
        val handheldOrderItems = order.handheldOrderItems.takeIf { it.isNotEmpty() } ?: return

        val handheldSales = handheldOrderItems.map { item ->
            HandheldSale().apply {
                handheld = item.handheld
                quantity = item.quantity
                amount = item.totalAmount
                this.vendor = vendor
                saleDate = order.responseTime ?: LocalDateTime.now()
                this.handheldOrderItem = item
                vendorBalance += item.totalAmount ?: 0
            }
        }
        vendor.balance = vendorBalance
        vendorRepository.saveAndFlush(vendor)
        handheldSaleRepository.saveAllAndFlush(handheldSales)
    }

    // Order item preparation
    private fun prepareBeverageOrderItem(itemDto: ItemDto, order: Order) {
        val beverageItemDto = itemDto.beverageItemDto!!
        val beverage = beverageRepository.findById(beverageItemDto.beverageId)
            .orElseThrow { RequestedEntityNotFoundException("Unable to place order. Beverage not found.") }

        val beverageFlavour = beverage.beverageFlavours.firstOrNull { it.id == beverageItemDto.beverageFlavourId }
            ?: throw RequestedEntityNotFoundException("Unable to place order. Beverage flavour not found.")

        val beverageFlavourSize =
            beverageFlavour.beverageFlavourSizes.firstOrNull { it.id == beverageItemDto.beverageFlavourSizeId }
                ?: throw RequestedEntityNotFoundException("Unable to place order. Beverage flavour size not found.")

        validateAvailability(item = beverage, itemFlavour = beverageFlavour, itemSize = beverageFlavourSize)

        order.vendor = vendorRepository.findById(beverage.vendor.id)
            .orElseThrow { UserNotFoundException("Unable to place order. Vendor not found.") }

        val newItem = BeverageOrderItem().apply {
            this.order = order
            this.vendor = beverage.vendor
            this.beverage = beverage
            this.beverageFlavour = beverageFlavour
            this.beverageFlavourSize = beverageFlavourSize
            this.quantity = beverageItemDto.quantity ?: 1
            this.orderType = OrderType.fromString(itemDto.orderType)
        }

        if (beverage.beverageDiscounts.isNotEmpty()) prepareAppliedDiscounts(newItem)
        newItem.totalAmount = newItem.calculateTotal()

        if (newItem.beverageFlavourSize.price!! < 2000 && order.orderType == OrderType.DELIVERY)
            throw EntityNotDeliverableException("Beverage cannot be ordered alone.")

        order.addItem(newItem)
    }

    private fun prepareDessertOrderItem(itemDto: ItemDto, order: Order) {
        val dessertItemDto = itemDto.dessertItemDto!!
        val dessert = dessertRepository.findById(dessertItemDto.dessertId)
            .orElseThrow { RequestedEntityNotFoundException("Unable to place order. Dessert not found.") }

        if (!dessert.deliverable!! && order.orderType == OrderType.DELIVERY)
            throw IllegalArgumentException("Dessert is not deliverable. Change order type.")

        val dessertFlavour = dessert.dessertFlavours.firstOrNull { it.id == dessertItemDto.dessertFlavourId }
            ?: throw RequestedEntityNotFoundException("Unable to place order. Dessert flavour not found.")

        val dessertFlavourSize =
            dessertFlavour.dessertFlavourSizes.firstOrNull { it.id == dessertItemDto.dessertFlavourSizeId }
                ?: throw RequestedEntityNotFoundException("Unable to place order. Dessert flavour size not found.")

        validateAvailability(item = dessert, itemFlavour = dessertFlavour, itemSize = dessertFlavourSize)

        order.vendor = vendorRepository.findById(dessert.vendor.id)
            .orElseThrow { UserNotFoundException("Unable to place order. Vendor not found.") }

        val newItem = DessertOrderItem().apply {
            this.order = order
            this.vendor = dessert.vendor
            this.dessert = dessert
            this.dessertFlavour = dessertFlavour
            this.dessertFlavourSize = dessertFlavourSize
            this.quantity = dessertItemDto.quantity ?: 1
            this.orderType = OrderType.fromString(itemDto.orderType)
        }

        if (dessert.dessertDiscounts.isNotEmpty()) prepareAppliedDiscounts(newItem)
        newItem.totalAmount = newItem.calculateTotal()
        order.addItem(newItem)
    }

    private fun prepareFoodOrderItem(itemDto: ItemDto, order: Order) {
        val foodItemDto = itemDto.foodItemDto!!
        val food = foodRepository.findByIdWithAllRelations(foodItemDto.foodId)
            .orElseThrow { RequestedEntityNotFoundException("Unable to place order. Food not found.") }

        if (!food.deliverable!! && order.orderType == OrderType.DELIVERY)
            throw IllegalArgumentException("Food is not deliverable. Please change order type.")

        val vendor = vendorRepository.findById(food.vendor.id)
            .orElseThrow { UserNotFoundException("Unable to place order. Vendor not found.") }
        val foodSize = food.foodSizes.firstOrNull { it.id == foodItemDto.foodSizeId }
            ?: throw RequestedEntityNotFoundException("Unable to place order. Food size not found.")

        validateAvailability(item = food, itemSize = foodSize)
        order.vendor = vendor

        val newItem = FoodOrderItem().apply {
            this.order = order
            this.vendor = food.vendor
            this.food = food
            this.quantity = foodItemDto.foodQuantity ?: 1
            this.size = foodSize
            this.orderType = OrderType.fromString(itemDto.orderType)
        }

        // Complement is resolved via the food back-reference to ensure it belongs to this food.
        newItem.complement =
            food.foodComplements.firstOrNull { it.complement.id == foodItemDto.complementId }?.complement
                ?: throw IllegalArgumentException("Unable to place order. Complement cannot be gotten from food.")

        foodItemDto.addOnIds?.let {
            val matchingAddOns = food.foodAddOns.map { it.addOn }
                .filter { it.id in foodItemDto.addOnIds!! }
                .takeIf { it.isNotEmpty() }
                ?: throw RequestedEntityNotFoundException("Unable to place order. Add-on not found.")
            newItem.addOns.addAll(matchingAddOns)
        }

        if (food.foodDiscounts.isNotEmpty()) prepareAppliedDiscounts(newItem)
        newItem.totalAmount = newItem.calculateTotal()
        order.addItem(newItem)
    }

    private fun prepareHandheldOrderItem(itemDto: ItemDto, order: Order) {
        val handheldItemDto = itemDto.handheldItemDto!!
        val handheld = handheldRepository.findById(handheldItemDto.handheldId)
            .orElseThrow { RequestedEntityNotFoundException("Unable to place order. Handheld not found.") }

        if (!handheld.deliverable!! && order.orderType == OrderType.DELIVERY)
            throw IllegalArgumentException("Handheld is not deliverable. Please change order type.")

        val handheldSize = handheld.handheldSizes.firstOrNull { it.id == handheldItemDto.handheldSizeId }
            ?: throw RequestedEntityNotFoundException("Unable to place order. Handheld size not found.")

        validateAvailability(item = handheld, itemSize = handheldSize)

        order.vendor = vendorRepository.findById(handheld.vendor.id)
            .orElseThrow { UserNotFoundException("Unable to place order. Vendor not found.") }

        val newItem = HandheldOrderItem().apply {
            this.order = order
            this.vendor = handheld.vendor
            this.handheld = handheld
            this.quantity = handheldItemDto.quantity ?: 1
            this.size = handheldSize
            this.orderType = OrderType.fromString(itemDto.orderType)
        }

        handheld.handheldFillings.forEach { newItem.fillings.add(it.filling) }

        if (handheld.handheldDiscounts.isNotEmpty()) prepareAppliedDiscounts(newItem)
        newItem.totalAmount = newItem.calculateTotal()
        order.addItem(newItem)
    }

    /** Copies active discounts onto the order item, back-referencing the correct item type. */
    private fun prepareAppliedDiscounts(orderItem: OrderEntity) {
        val currentDate = Date()

        fun isActive(applied: AppliedDiscount): Boolean {
            val start = applied.discount.startDate
            val end = applied.discount.endDate
            return when {
                start == null && end == null -> true
                start == null -> currentDate.before(end)
                end == null -> currentDate.after(start)
                else -> currentDate.after(start) && currentDate.before(end)
            }
        }

        when (orderItem) {
            is BeverageOrderItem -> orderItem.appliedDiscounts.filter { isActive(it) }.forEach { active ->
                orderItem.appliedDiscounts.add(AppliedDiscount().apply {
                    discount = active.discount
                    this.beverageOrderItem = orderItem
                })
            }

            is DessertOrderItem -> orderItem.appliedDiscounts.filter { isActive(it) }.forEach { active ->
                orderItem.appliedDiscounts.add(AppliedDiscount().apply {
                    discount = active.discount
                    this.dessertOrderItem = orderItem
                })
            }

            is FoodOrderItem -> orderItem.appliedDiscounts.filter { isActive(it) }.forEach { active ->
                orderItem.appliedDiscounts.add(AppliedDiscount().apply {
                    discount = active.discount
                    this.foodOrderItem = orderItem
                })
            }

            is HandheldOrderItem -> orderItem.appliedDiscounts.filter { isActive(it) }.forEach { active ->
                orderItem.appliedDiscounts.add(AppliedDiscount().apply {
                    discount = active.discount
                    this.handheldOrderItem = orderItem
                })
            }
        }
    }


    /**
     * Calculates and assigns [Order.readyBy] and [Order.deliveryTime] based on order status,
     * order type, and per-item preparation/delivery times. PENDING orders get a tentative
     * estimate; CONFIRMED orders are recalculated against the current wall clock for accuracy.
     */
    private fun calculateOrderReadyTime(order: Order) {
        val currentTime = LocalTime.now()
        val vendor = order.vendor!!
        val openingTime = vendor.openingTime!!
        val closingTime = vendor.closingTime
        val isDelivery = order.orderType == OrderType.DELIVERY
        val hasFoodItems = order.items.any { it is FoodOrderItem }
        val isWithinWorkingHours = currentTime.isAfter(openingTime) && currentTime.isBefore(closingTime)

        val hasQuickDeliveryItem = order.items.any {
            when (it) {
                is BeverageOrderItem -> it.beverage.quickDelivery == true
                is DessertOrderItem -> it.dessert.quickDelivery == true
                is FoodOrderItem -> it.food.quickDelivery == true
                else -> false
            }
        }

        fun maxPrepTime() = order.items.maxOfOrNull {
            when (it) {
                is BeverageOrderItem -> it.beverage.preparationTime ?: 0
                is FoodOrderItem -> it.food.preparationTime ?: 0
                is DessertOrderItem -> it.dessert.preparationTime ?: 0
                else -> 0
            }
        }?.toLong() ?: 0L

        if (order.orderStatus == OrderStatus.PENDING) {

            if (!isDelivery) {
                order.readyBy = if (isWithinWorkingHours) currentTime.plusMinutes(maxPrepTime())
                else openingTime.plusMinutes(maxPrepTime())
            }

            // Quick delivery: pick the latest of (readyAsFrom + prepTime) across all items
            if (isDelivery && hasQuickDeliveryItem) {
                val itemReadyTimes = order.items.map { item ->
                    val (readyAsFrom, prepTime) = when (item) {
                        is BeverageOrderItem -> item.beverage.readyAsFrom to item.beverage.preparationTime
                        is DessertOrderItem -> item.dessert.readyAsFrom to item.dessert.preparationTime
                        is FoodOrderItem -> item.food.readyAsFrom to item.food.preparationTime
                        else -> openingTime to 0
                    }
                    (readyAsFrom ?: openingTime).plusMinutes((prepTime ?: 0).toLong())
                }

                val maxItemReadyTime = itemReadyTimes.maxOrNull() ?: openingTime
                val maxReadyAsFrom = order.items.mapNotNull {
                    when (it) {
                        is BeverageOrderItem -> it.beverage.readyAsFrom
                        is DessertOrderItem -> it.dessert.readyAsFrom
                        is FoodOrderItem -> it.food.readyAsFrom
                        else -> null
                    }
                }.maxOrNull() ?: openingTime

                // Past all readyAsFrom windows — estimate from now
                order.deliveryTime = if (currentTime > maxReadyAsFrom) currentTime.plusMinutes(maxPrepTime())
                else maxItemReadyTime
            }

            // Standard delivery fallback
            order.deliveryTime = if (isWithinWorkingHours) currentTime.plusMinutes(maxPrepTime())
            else openingTime.plusMinutes(maxPrepTime())

        } else if (order.orderStatus == OrderStatus.CONFIRMED) {

            // Standard delivery: honour food-specific delivery times where possible
            if (isDelivery && hasFoodItems && !order.quickDelivery) {
                val maxDeliveryTime = order.items
                    .mapNotNull { if (it is FoodOrderItem) it.food.deliveryTime else null }
                    .maxOrNull()

                order.deliveryTime = if (maxDeliveryTime != null && currentTime > maxDeliveryTime) {
                    currentTime.plusMinutes(maxPrepTime())
                } else {
                    maxDeliveryTime ?: currentTime
                }
            }

            order.readyBy = currentTime.plusMinutes(maxPrepTime())
            if (order.quickDelivery) order.deliveryTime = order.readyBy
        }
    }

    private fun validateAvailability(item: EdibleEntity, itemFlavour: Flavour? = null, itemSize: ConsumableSize) {
        if (item.availability != Availability.AVAILABLE)
            throw EntityNotAvailableException("Unable to place order. Item is ${item.availability!!.type}.")

        itemFlavour?.let {
            if (it.availability != Availability.AVAILABLE)
                throw EntityNotAvailableException("Unable to place order. Item flavour is ${it.availability!!.type}.")
        }

        if (itemSize.availability != Availability.AVAILABLE)
            throw EntityNotAvailableException("Unable to place order. Size option is ${itemSize.availability!!.type}.")
    }
}

