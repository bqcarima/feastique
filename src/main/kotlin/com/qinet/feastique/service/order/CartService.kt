package com.qinet.feastique.service.order

import com.qinet.feastique.exception.PermissionDeniedException
import com.qinet.feastique.exception.RequestedEntityNotFoundException
import com.qinet.feastique.exception.UserNotFoundException
import com.qinet.feastique.model.dto.order.*
import com.qinet.feastique.model.entity.consumables.EdibleEntity
import com.qinet.feastique.model.entity.consumables.beverage.Beverage
import com.qinet.feastique.model.entity.consumables.dessert.Dessert
import com.qinet.feastique.model.entity.consumables.food.Food
import com.qinet.feastique.model.entity.consumables.handheld.Handheld
import com.qinet.feastique.model.entity.discount.AppliedDiscount
import com.qinet.feastique.model.entity.order.Cart
import com.qinet.feastique.model.entity.order.OrderEntity
import com.qinet.feastique.model.entity.order.item.BeverageCartItem
import com.qinet.feastique.model.entity.order.item.DessertCartItem
import com.qinet.feastique.model.entity.order.item.FoodCartItem
import com.qinet.feastique.model.entity.order.item.HandheldCartItem
import com.qinet.feastique.model.enums.OrderType
import com.qinet.feastique.repository.consumables.beverage.BeverageRepository
import com.qinet.feastique.repository.consumables.dessert.DessertRepository
import com.qinet.feastique.repository.consumables.food.FoodRepository
import com.qinet.feastique.repository.consumables.handheld.HandheldRepository
import com.qinet.feastique.repository.order.CartRepository
import com.qinet.feastique.repository.user.CustomerRepository
import com.qinet.feastique.security.UserSecurity
import com.qinet.feastique.utility.toLocalDate
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.util.*
import kotlin.jvm.optionals.getOrElse


@Service
class CartService(
    private val cartRepository: CartRepository,
    private val customerRepository: CustomerRepository,
    private val foodRepository: FoodRepository,
    private val beverageRepository: BeverageRepository,
    private val dessertRepository: DessertRepository,
    private val handheldRepository: HandheldRepository
) {

    private val logger = LoggerFactory.getLogger(CartService::class.java)

    /**
     * This method retrieves the cart for the authenticated customer.
     * @param UserSecurity
     * @return [Cart]
     * @throws UserNotFoundException
     */
    @Transactional(readOnly = true)
    fun getCart(customerDetails: UserSecurity): Cart? {
        val customer = customerRepository.findById(customerDetails.id).get()
        return customer.cart
    }

    /**
     * This method adds a food item or a beverage item
     * or both to the cart for processing.
     * @param FoodItemDto
     * @param UserSecurity`
     * @return [Cart]
     * @throws UserNotFoundException
     * @throws RequestedEntityNotFoundException
     * @throws PermissionDeniedException
     * @throws IllegalArgumentException
     */
    @Transactional
    fun addItemToCart(
        itemDto: ItemDto,
        customerDetails: UserSecurity
    ): Cart {
        // Fetch the customer from the database
        val customer = customerRepository.findById(customerDetails.id)
            .orElseThrow { UserNotFoundException("An unexpected error occurred. Customer not found.") }

        if (customer.cart == null) {
            customer.cart = Cart().apply { this.customer = customer }

            // Flush to ensure only a single cart with a
            // unique id exists in the persistence context
            customerRepository.flush()
        }

        // Always work with a single managed Cart instance
        val cart = customer.cart!!

        // Add Food item to the cart
        itemDto.foodItemDto?.let { _ -> handleFood(itemDto, cart) }

        // Add a beverage to the cart
        itemDto.beverageItemDto?.let { _ -> handleBeverage(itemDto, cart) }

        // Add a dessert to the cart
        itemDto.dessertItemDto?.let { _ -> handleDessert(itemDto, cart) }

        // Add a handheld to the cart
        itemDto.handheldItemDto?.let { _ -> handleHandheld(itemDto, cart) }

        // Recalculate total and persist cart
        cart.totalAmount = cart.calculateTotal()
        return cartRepository.saveAndFlush(cart)
    }

    /**
     * This method removes items from the cart based on the provided item ids.
     * If all lists are empty after removing the items, the cart will be deleted.
     * @param CartItemDto
     * @param UserSecurity
     * @throws UserNotFoundException
     */
    @Transactional
    fun removeItems(cartItemDto: CartItemDto, customerDetails: UserSecurity) {
        val customer = customerRepository.findById(customerDetails.id)
            .orElseThrow {
                throw UserNotFoundException("An unexpected error occurred. Customer account not found.")
            }

        val cart = customer.cart ?: return
        cart.removeItems(cartItemDto.ids)

        // if all lists are empty delete cart
        if (cart.foodCartItems.isEmpty() && cart.beverageCartItems.isEmpty() && cart.dessertCartItems.isEmpty() && cart.handheldCartItems.isEmpty()) {
            cart.customer = null
            customer.cart = null
            cartRepository.delete(cart)

        } else {
            cart.totalAmount = cart.calculateTotal()
        }
    }


    /**
     * This method changes the quantity of an item in the cart based on the provided item id and new quantity.
     * If the new quantity is less than or equal to 0, the item will be removed from the cart.
     * @param id
     * @param UserSecurity
     * @param ChangeQuantityDto
     * @throws UserNotFoundException
     */
    @Transactional
    fun changeQuantity(id: UUID, customerDetails: UserSecurity, changeQuantityDto: ChangeQuantityDto) {
        val customer = customerRepository.findById(customerDetails.id)
            .orElseThrow {
                throw UserNotFoundException("An unexpected error occurred. Customer account not found.")
            }

        val cart = customer.cart ?: return
        val item = cart.items.find { it.id == id } ?: return

        if (item.quantity == changeQuantityDto.quantity) return

        item.quantity = changeQuantityDto.quantity ?: 1

        if (changeQuantityDto.quantity!! <= 0) {
            cart.removeItem(item)
        } else {
            when (item) {
                is FoodCartItem     -> item.calculateTotal()
                is BeverageCartItem -> item.calculateTotal()
                is DessertCartItem  -> item.calculateTotal()
                is HandheldCartItem -> item.calculateTotal()
            }
        }
        cart.totalAmount = cart.calculateTotal()
    }


    /**
     * This method deletes the cart for the authenticated customer.
     * @param UserSecurity
     * @throws UserNotFoundException
     */
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
    }

    /**
     * This method handles adding a beverage item to the cart. It retrieves the beverage and its related entities based on the provided ids,
     * creates a new BeverageCartItem, checks if an item with the same configuration already exists in the cart and either merges with the existing item or adds a new item to the cart.
     * It also prepares and applies any active discounts for the beverage item.
     * @param ItemDto
     * @param Cart
     * @throws RequestedEntityNotFoundException
     */
    private fun handleBeverage(itemDto: ItemDto, cart: Cart) {

        logger.info("1 - Start of function (adding beverage to cart).")

        val beverageItemDto = itemDto.beverageItemDto!!
        val beverage = beverageRepository.findById(beverageItemDto.beverageId)
            .getOrElse { throw RequestedEntityNotFoundException("Beverage(s) not found.") }

        val beverageFlavour = beverage.beverageFlavours.firstOrNull { it.id == beverageItemDto.beverageFlavourId }
            ?: throw RequestedEntityNotFoundException("An error occurred, invalid beverage flavour id.")

        val beverageFlavourSize =
            beverageFlavour.beverageFlavourSizes.firstOrNull { it.id == beverageItemDto.beverageFlavourSizeId }
                ?: throw RequestedEntityNotFoundException("An error occurred, invalid beverage flavour size id.")

        val quantity = beverageItemDto.quantity ?: 1
        val newBeverageCartItem = BeverageCartItem().apply {
            this.vendor = beverage.vendor
            this.beverage = beverage
            this.beverageFlavour = beverageFlavour
            this.beverageFlavourSize = beverageFlavourSize
            this.quantity = quantity

            logger.info("Incoming order type: ${itemDto.orderType}")
            this.orderType = OrderType.fromString(itemDto.orderType)

            logger.info("2 - Creating beverage cart time.")
        }

        cart.beverageCartItems.find { it.isSameAs(newBeverageCartItem) }?.let { existingBeverageItem ->
            existingBeverageItem.quantity += newBeverageCartItem.quantity
            existingBeverageItem.totalAmount = existingBeverageItem.calculateTotal()

        } ?: run {
            cart.addItem(newBeverageCartItem)

            // apply active discounts to the items
            prepareDiscounts(beverage, newBeverageCartItem)
            newBeverageCartItem.totalAmount = newBeverageCartItem.calculateTotal()
        }

        logger.info("3 - Inserting beverage into beverage cart items table.")
    }

    /**
     * This method handles adding a dessert item to the cart. It retrieves the dessert and its related entities based on the provided ids,
     * creates a new DessertCartItem, checks if an item with the same configuration already exists in the cart and either merges with the existing item or adds a new item to the cart.
     * It also prepares and applies any active discounts for the dessert item.
     * @param ItemDto
     * @param Cart
     * @throws RequestedEntityNotFoundException
     */
    private fun handleDessert(itemDto: ItemDto, cart: Cart) {
        val dessertItemDto = itemDto.dessertItemDto!!
        val dessert = dessertRepository.findById(dessertItemDto.dessertId)
            .getOrElse { throw RequestedEntityNotFoundException("Invalid id, dessert not found for id: ${dessertItemDto.dessertId}") }

        val dessertFlavour = dessert.dessertFlavours.firstOrNull { it.id == dessertItemDto.dessertFlavourId }
            ?: throw RequestedEntityNotFoundException("An error occurred, invalid dessert flavour id.")

        val dessertFlavourSize =
            dessertFlavour.dessertFlavourSizes.firstOrNull { it.id == dessertItemDto.dessertFlavourSizeId }
                ?: throw RequestedEntityNotFoundException("An error occurred, invalid dessert flavour size id.")

        val newDessertCartItem = DessertCartItem().apply {
            this.dessert = dessert
            this.dessertFlavour = dessertFlavour
            this.dessertFlavourSize = dessertFlavourSize
            this.vendor = dessert.vendor
            this.quantity = dessertItemDto.quantity ?: 1
            this.orderType = OrderType.fromString(itemDto.orderType)
        }

        // apply active discounts to the items
        // prepareDiscounts(dessert, newDessertCartItem)

        cart.dessertCartItems.find { it.isSameAs(newDessertCartItem) }?.let { existingDessertCartItem ->
            existingDessertCartItem.quantity += newDessertCartItem.quantity
            existingDessertCartItem.totalAmount = existingDessertCartItem.calculateTotal()

        } ?: run {
            cart.addItem(newDessertCartItem)

            // apply active discounts to the items
            prepareDiscounts(dessert, newDessertCartItem)
            newDessertCartItem.totalAmount = newDessertCartItem.calculateTotal()
        }
    }


    /**
     * This method handles adding a food item to the cart. It retrieves the food and its related entities based on the provided ids,
     * creates a new FoodCartItem, checks if an item with the same configuration already exists in the cart and either merges with the existing item or adds a new item to the cart.
     * It also prepares and applies any active discounts for the food item.
     * @param ItemDto
     * @param Cart
     * @throws RequestedEntityNotFoundException
     * @throws IllegalArgumentException
     */
    private fun handleFood(itemDto: ItemDto, cart: Cart) {
        val foodItemDto = itemDto.foodItemDto!!
        val food = foodRepository.findByIdWithAllRelationsAndIsActiveTrue(foodItemDto.foodId)
            .orElseThrow { RequestedEntityNotFoundException("Cannot add item to cart, food not found.") }

        val newFoodCartItem = FoodCartItem().apply {
            this.food = food
            this.vendor = food.vendor
            this.quantity = foodItemDto.foodQuantity ?: 1
            this.size = food.foodSizes.find { it.id == foodItemDto.foodSizeId }
                ?: throw RequestedEntityNotFoundException("An error occurred, invalid food size id.")

            this.complement = food.foodComplements.firstOrNull { it.complement.id == foodItemDto.complementId }?.complement
                    ?: throw IllegalArgumentException("An error occurred, complement not found.")
            this.addOns.addAll(
                food.foodAddOns.map { it.addOn }
                    .filter { it.id in (foodItemDto.addOnIds ?: emptyList()) }
            )
            this.orderType = OrderType.fromString(itemDto.orderType)
        }

        // Merge with existing item if same configuration exists and increase quantity
        cart.foodCartItems.find { it.isSameAs(newFoodCartItem) }?.let { existingFoodItem ->
            existingFoodItem.quantity += newFoodCartItem.quantity
            existingFoodItem.totalAmount = existingFoodItem.calculateTotal()

        } ?: run {
            cart.addItem(newFoodCartItem)

            // apply active discounts to the items
            prepareDiscounts(food, newFoodCartItem)
            newFoodCartItem.totalAmount = newFoodCartItem.calculateTotal()
        }
    }

    /**
     * This method handles adding a handheld item to the cart. It retrieves the handheld and its related entities based on the provided ids,
     * creates a new HandheldCartItem, checks if an item with the same configuration already exists in the cart and either merges with the existing item or adds a new item to the cart.
     * It also prepares and applies any active discounts for the handheld item.
     * @param ItemDto
     * @param Cart
     * @throws RequestedEntityNotFoundException
     */
    private fun handleHandheld(itemDto: ItemDto, cart: Cart) {

        logger.info("1 - Start of function (adding handhelds to cart).")

        val handheldItemDto = itemDto.handheldItemDto!!
        val handheld = handheldRepository.findById(handheldItemDto.handheldId)
            .getOrElse { throw RequestedEntityNotFoundException("Invalid ID. Cannot add item to cart, handheld not found.") }

        val handheldSize = handheld.handheldSizes.find { it.id == handheldItemDto.handheldSizeId }
            ?: throw RequestedEntityNotFoundException("An error occurred, invalid handheld size id.")

        val fillings = handheld.handheldFillings.map { it.filling }

        val newHandheldCartItem = HandheldCartItem().apply {
            this.handheld = handheld
            this.vendor = handheld.vendor
            this.quantity = handheldItemDto.quantity ?: 1
            this.size = handheldSize
            this.fillings.addAll(fillings)
            this.orderType = OrderType.fromString(itemDto.orderType)

            logger.info("2 - Creating the handheld cart item.")
        }

        // Merge with existing item if same configuration exists and increase quantity
        cart.handheldCartItems.find { it.isSameAs(newHandheldCartItem) }?.let { existingHandheldItem ->
            existingHandheldItem.quantity += newHandheldCartItem.quantity
            existingHandheldItem.totalAmount = existingHandheldItem.calculateTotal()

        } ?: run {
            cart.addItem(newHandheldCartItem)

            // apply active discounts to the items
            prepareDiscounts(handheld, newHandheldCartItem)
            newHandheldCartItem.totalAmount = newHandheldCartItem.calculateTotal()
        }

        logger.info("3 - inserting into handheld cart items table with value:" +
                "name= ${handheld.name},vendor= ${handheld.vendor.chefName},")
    }

    /**
     * This method prepares and applies any active discounts for the given edible entity and cart item.
     * It checks the type of the edible entity (beverage, dessert, food, handheld) and retrieves the corresponding discounts.
     * It then filters the discounts to get only the active ones based on the current date and maps them to AppliedDiscount objects which are added to the cart item.
     * @param EdibleEntity
     * @param OrderEntity
     */
    private fun prepareDiscounts(edibleEntity: EdibleEntity, orderEntity: OrderEntity) {
        when (edibleEntity) {

            is Beverage -> {
                val newBeverageCartItem = orderEntity as BeverageCartItem
                val discounts = edibleEntity.beverageDiscounts.map { it.discount }
                if (discounts.isNotEmpty()) {

                    // filter discounts to get only active discounts
                    val applicableDiscounts = discounts.filter {
                        val today = LocalDate.now()
                        it.startDate!!.toLocalDate() <= today &&
                                it.endDate!!.toLocalDate() >= today
                    }

                    // mapping applicable discounts to applied discount objects
                    val appliedDiscounts = applicableDiscounts.map {
                        AppliedDiscount().apply {
                            this.beverageCartItem = newBeverageCartItem
                            this.discount = it
                        }
                    }
                    newBeverageCartItem.appliedDiscounts.addAll(appliedDiscounts)
                }
            }

            is Dessert -> {
                val newDessertCartItem = orderEntity as DessertCartItem
                val discounts = edibleEntity.dessertDiscounts.map { it.discount }
                if (discounts.isNotEmpty()) {
                    val applicableDiscounts = discounts.filter {
                        val today = LocalDate.now()
                        it.startDate!!.toLocalDate() <= today &&
                                it.endDate!!.toLocalDate() >= today
                    }

                    val appliedDiscounts = applicableDiscounts.map {
                        AppliedDiscount().apply {
                            this.dessertCartItem = newDessertCartItem
                            this.discount = it
                        }
                    }
                    newDessertCartItem.appliedDiscounts.addAll(appliedDiscounts)
                }
            }

            is Food -> {
                val newFoodCartItem = orderEntity as FoodCartItem
                val discounts = edibleEntity.foodDiscounts.map { it.discount }
                if (discounts.isNotEmpty()) {
                    val applicableDiscounts = discounts.filter {
                        val today = LocalDate.now()
                        it.startDate!!.toLocalDate() <= today &&
                                it.endDate!!.toLocalDate() >= today
                    }

                    val appliedDiscounts = applicableDiscounts.map {
                        AppliedDiscount().apply {
                            this.foodCartItem = newFoodCartItem
                            this.discount = it
                        }
                    }
                    newFoodCartItem.appliedDiscounts.addAll(appliedDiscounts)
                }
            }

            is Handheld -> {
                val newHandheldCartItem = orderEntity as HandheldCartItem
                val discounts = edibleEntity.handheldDiscounts.map { it.discount }
                if (discounts.isNotEmpty()) {
                    val applicableDiscounts = discounts.filter {
                        val today = LocalDate.now()
                        it.startDate!!.toLocalDate() <= today &&
                                it.endDate!!.toLocalDate() >= today
                    }

                    val appliedDiscounts = applicableDiscounts.map {
                        AppliedDiscount().apply {
                            this.handheldCartItem = newHandheldCartItem
                            this.discount = it
                        }
                    }
                    newHandheldCartItem.appliedDiscounts.addAll(appliedDiscounts)
                }
            }
        }
    }

}

