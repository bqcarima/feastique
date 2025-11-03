package com.qinet.feastique.service.order

import com.qinet.feastique.exception.PermissionDeniedException
import com.qinet.feastique.exception.RequestedEntityNotFoundException
import com.qinet.feastique.exception.UserNotFoundException
import com.qinet.feastique.model.dto.order.CartItemDto
import com.qinet.feastique.model.dto.order.OrderItemDto
import com.qinet.feastique.model.entity.discount.AppliedDiscount
import com.qinet.feastique.model.entity.order.Cart
import com.qinet.feastique.model.entity.order.beverage.BeverageCartItem
import com.qinet.feastique.model.entity.order.food.FoodCartItem
import com.qinet.feastique.model.enums.OrderType
import com.qinet.feastique.repository.BeverageRepository
import com.qinet.feastique.repository.customer.CustomerRepository
import com.qinet.feastique.repository.discount.DiscountRepository
import com.qinet.feastique.repository.food.FoodRepository
import com.qinet.feastique.repository.order.CartRepository
import com.qinet.feastique.security.UserSecurity
import com.qinet.feastique.utility.toLocalDate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.util.UUID
import org.slf4j.LoggerFactory


@Service
class CartService(
    private val cartRepository: CartRepository,
    private val customerRepository: CustomerRepository,
    private val foodRepository: FoodRepository,
    private val beverageRepository: BeverageRepository,
    private val discountRepository: DiscountRepository
) {

    private val logger = LoggerFactory.getLogger(CartService::class.java)
    @Transactional(readOnly = true)
    fun getCart(customerDetails: UserSecurity): Cart? {
        val customer = customerRepository.findById(customerDetails.id).get()
        return customer.cart
    }

    /**
     * This method adds a food item or a beverage item
     * or both to the cart for processing.
     * @param OrderItemDto
     * @param UserSecurity`
     * @return [Cart]
     * @throws UserNotFoundException
     * @throws RequestedEntityNotFoundException
     * @throws PermissionDeniedException
     * @throws IllegalArgumentException
     */
    @Transactional
    fun addItemToCart(orderItemDto: OrderItemDto, customerDetails: UserSecurity): Cart {
        // Fetch the customer from the database
        val customer = customerRepository.findById(customerDetails.id)
            .orElseThrow { UserNotFoundException("An unexpected error occurred. Customer not found.") }

        // Always work with a single managed Cart instance
        val cart: Cart = cartRepository.findByCustomerId(customer.id)
            .orElseGet {
                Cart().apply {
                    this.customer = customer
                    customer.cart = this
                }
            }

        val orderTypeAsString = requireNotNull(orderItemDto.orderType)
        val orderTypeAsEnum = OrderType.fromString(orderTypeAsString)

        // Add Food item
        orderItemDto.foodId?.let { foodId ->
            val food = foodRepository.findByIdWithAllRelations(foodId)
                .orElseThrow { RequestedEntityNotFoundException("Cannot place order, food not found.") }

            val newFoodCartItem = FoodCartItem().apply {
                this.cart = cart
                this.food = food
                this.vendor = food.vendor
                this.quantity = orderItemDto.foodQuantity ?: 1
                this.size = food.foodSize.find { it.id == orderItemDto.foodSizeId } ?: food.foodSize.first()
                this.complement =
                    food.foodComplement.firstOrNull { it.complement.id == orderItemDto.complementId }?.complement
                        ?: throw IllegalArgumentException("An error occurred, complement not found.")
                this.addOns.addAll(
                    food.foodAddOn.map { it.addOn }
                        .filter { it.id in (orderItemDto.addOnIds ?: emptyList()) }
                )
                this.orderType = orderTypeAsEnum
            }

            // Apply discounts
            val discountIds = food.foodDiscount.map { it.discount.id }
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
                        this.foodCartItem = newFoodCartItem
                        this.discount = it
                    }
                }
                newFoodCartItem.appliedDiscounts.addAll(appliedDiscounts)
            }

            // Merge with existing item if same configuration exists
            val existingFoodItem = cart.foodCartItems.find { foodCartItem ->
                        foodCartItem.food.id == newFoodCartItem.food.id &&
                        foodCartItem.size.id == newFoodCartItem.size.id &&
                        foodCartItem.complement.id == newFoodCartItem.complement.id &&
                        foodCartItem.addOns.map { it.id }.toSet() == newFoodCartItem.addOns.map { it.id }.toSet() &&
                        foodCartItem.orderType == newFoodCartItem.orderType
            }

            if (existingFoodItem != null) {
                existingFoodItem.quantity = existingFoodItem.quantity + newFoodCartItem.quantity
                existingFoodItem.totalAmount = existingFoodItem.calculateTotal()
            } else {
                newFoodCartItem.totalAmount = newFoodCartItem.calculateTotal()
                cart.addItem(newFoodCartItem)
            }
        }

        // Add Beverage items if applicable
        orderItemDto.beverageIds?.takeIf { it.isNotEmpty() }?.let { beverageMap ->
            val beverages = beverageRepository.findAllById(beverageMap.keys.toList())
                .takeIf { it.isNotEmpty() }
                ?: throw RequestedEntityNotFoundException("Beverage(s) not found.")

            beverages.forEach { beverage ->
                val quantity = beverageMap[beverage.id] ?: 1
                val newBeverageCartItem = BeverageCartItem().apply {
                    this.cart = cart
                    this.vendor = beverage.vendor
                    this.beverage = beverage
                    this.quantity = quantity
                    this.totalAmount = this.calculateTotal()

                    logger.info("Incoming order type: ${orderItemDto.orderType}")
                    this.orderType = orderTypeAsEnum
                }

                val existingBeverageItem = cart.beverageCartItems.find { it.beverage.id == beverage.id }
                if (existingBeverageItem != null) {
                    existingBeverageItem.quantity = existingBeverageItem.quantity + quantity
                    existingBeverageItem.totalAmount = existingBeverageItem.calculateTotal()
                } else {
                    newBeverageCartItem.totalAmount = newBeverageCartItem.calculateTotal()
                    cart.addItem(newBeverageCartItem)
                }
            }
        }

        // Recalculate total and persist cart
        cart.totalAmount = cart.calculateTotal()
        return cartRepository.save(cart)
    }

    @Transactional
    fun removeItems(cartItemDto: CartItemDto, customerDetails: UserSecurity) {
        val customer = customerRepository.findById(customerDetails.id)
            .orElseThrow {
                throw UserNotFoundException("An unexpected error occurred. Customer account not found.")
            }

        val cart = customer.cart ?: return
        cart.removeItems(cartItemDto.ids)

        // if both lists are empty delete cart
        if (cart.foodCartItems.isEmpty() && cart.beverageCartItems.isEmpty()) {
            cart.customer = null
            customer.cart = null
            cartRepository.delete(cart)

        } else {
            cart.totalAmount = cart.calculateTotal()
            cartRepository.save(cart)
        }
    }

    @Transactional
    fun increaseItemQuantity(id: UUID, customerDetails: UserSecurity) {
        val customer = customerRepository.findById(customerDetails.id)
            .orElseThrow {
                throw UserNotFoundException("An unexpected error occurred. Customer account not found.")
            }

        val cart = customer.cart ?: return
        val item = cart.items.find { it.id == id } ?: return
        when(item) {
            is FoodCartItem -> {
                item.quantity += 1
                item.totalAmount = item.calculateTotal()
            }

            is BeverageCartItem -> {
                item.quantity += 1
                item.totalAmount = item.calculateTotal()
            }
        }
        cart.totalAmount = cart.calculateTotal()
        cartRepository.save(cart)
    }
    @Transactional
    fun reduceItemQuantity(id: UUID, customerDetails: UserSecurity) {
        val customer = customerRepository.findById(customerDetails.id)
            .orElseThrow {
                throw UserNotFoundException("An unexpected error occurred. Customer account not found.")
            }

        val cart = customer.cart ?: return
        val item = cart.items.find { it.id == id } ?: return
        when(item) {
            is FoodCartItem -> {
                item.quantity -= 1
                if (item.quantity <= 0) {
                    cart.removeItem(item)
                } else {
                    item.totalAmount = item.calculateTotal()
                }
            }

            is BeverageCartItem -> {
                item.quantity -= 1
                if (item.quantity <= 0) {
                    cart.removeItem(item)
                } else {
                    item.totalAmount = item.calculateTotal()
                }
            }
        }
        cart.totalAmount = cart.calculateTotal()
        cartRepository.save(cart)
    }

    @Transactional
    fun deleteCart(customerDetails: UserSecurity) {
        val customer = customerRepository.findById(customerDetails.id)
            .orElseThrow {
                throw UserNotFoundException("An unexpected error occurred. Customer account not found.")
            }
        val cart = customer.cart

        if (cart != null) {
            cart.customer = null
            cartRepository.delete(cart)
        }
        customer.cart = null
        customerRepository.save(customer)
    }

}

