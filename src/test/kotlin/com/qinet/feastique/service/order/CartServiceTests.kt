package com.qinet.feastique.service.order

import com.qinet.feastique.exception.RequestedEntityNotFoundException
import com.qinet.feastique.exception.UserNotFoundException
import com.qinet.feastique.model.dto.order.*
import com.qinet.feastique.model.entity.consumables.beverage.Beverage
import com.qinet.feastique.model.entity.consumables.dessert.Dessert
import com.qinet.feastique.model.entity.consumables.flavour.BeverageFlavour
import com.qinet.feastique.model.entity.consumables.flavour.DessertFlavour
import com.qinet.feastique.model.entity.consumables.food.Food
import com.qinet.feastique.model.entity.consumables.handheld.Handheld
import com.qinet.feastique.model.entity.order.Cart
import com.qinet.feastique.model.entity.order.item.*
import com.qinet.feastique.model.entity.size.BeverageFlavourSize
import com.qinet.feastique.model.entity.size.DessertFlavourSize
import com.qinet.feastique.model.entity.size.FoodSize
import com.qinet.feastique.model.entity.size.HandheldSize
import com.qinet.feastique.model.entity.user.Customer
import com.qinet.feastique.model.entity.user.Vendor
import com.qinet.feastique.model.enums.*
import com.qinet.feastique.repository.consumables.beverage.BeverageRepository
import com.qinet.feastique.repository.consumables.dessert.DessertRepository
import com.qinet.feastique.repository.consumables.food.FoodRepository
import com.qinet.feastique.repository.consumables.handheld.HandheldRepository
import com.qinet.feastique.repository.order.CartRepository
import com.qinet.feastique.repository.user.CustomerRepository
import com.qinet.feastique.security.UserSecurity
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.*
import org.springframework.security.core.authority.SimpleGrantedAuthority
import java.util.*

// Shared fixtures

private val CS_CUSTOMER_ID: UUID = UUID.randomUUID()
private val CS_VENDOR_ID: UUID = UUID.randomUUID()
private val CS_FOOD_ID: UUID = UUID.randomUUID()
private val CS_BEVERAGE_ID: UUID = UUID.randomUUID()
private val CS_DESSERT_ID: UUID = UUID.randomUUID()
private val CS_HANDHELD_ID: UUID = UUID.randomUUID()
private val CS_FOOD_SIZE_ID: UUID = UUID.randomUUID()
private val CS_COMPLEMENT_ID: UUID = UUID.randomUUID()
private val CS_FLAVOUR_ID: UUID = UUID.randomUUID()
private val CS_FLAVOUR_SIZE_ID: UUID = UUID.randomUUID()
private val CS_HANDHELD_SIZE_ID: UUID = UUID.randomUUID()
private val CS_DESSERT_FLAVOUR_ID: UUID = UUID.randomUUID()
private val CS_DESSERT_FLAVOUR_SIZE_ID: UUID = UUID.randomUUID()

private fun csCustomerSecurity(): UserSecurity = UserSecurity(
    id = CS_CUSTOMER_ID,
    username = "jane_doe",
    password = "hashed",
    userAuthorities = mutableListOf(SimpleGrantedAuthority("ROLE_CUSTOMER"))
)

private fun csVendor(): Vendor = Vendor().apply { id = CS_VENDOR_ID; username = "chef" }

private fun csCustomer(cart: Cart? = null): Customer = Customer().apply {
    id = CS_CUSTOMER_ID
    username = "jane_doe"
    accountType = AccountType.CUSTOMER
    this.cart = cart
}

private fun csFoodSize(): FoodSize = FoodSize().apply {
    id = CS_FOOD_SIZE_ID
    size = Size.MEDIUM
    priceIncrease = 0
    availability = Availability.AVAILABLE
}

private fun csBeverageFlavourSize(): BeverageFlavourSize = BeverageFlavourSize().apply {
    id = CS_FLAVOUR_SIZE_ID
    size = Size.MEDIUM
    price = 1500
    availability = Availability.AVAILABLE
    beverageFlavour = csBeverageFlavour()
}

private fun csBeverageFlavour(): BeverageFlavour = BeverageFlavour().apply {
    id = CS_FLAVOUR_ID
    name = "Strawberry"
    availability = Availability.AVAILABLE
    beverageFlavourSizes = mutableSetOf(BeverageFlavourSize().apply {
        id = CS_FLAVOUR_SIZE_ID
        size = Size.MEDIUM
        price = 1500
        availability = Availability.AVAILABLE
    })
}

private fun csFood(): Food = Food().apply {
    id = CS_FOOD_ID
    name = "Jollof Rice"
    basePrice = 2000
    availability = Availability.AVAILABLE
    deliverable = true
    vendor = csVendor()
    foodSizes = mutableSetOf(csFoodSize())
    foodComplements = mutableSetOf()
    foodAddOns = mutableSetOf()
    foodDiscounts = mutableSetOf()
}

private fun csBeverage(): Beverage = Beverage().apply {
    id = CS_BEVERAGE_ID
    name = "Orange Juice"
    availability = Availability.AVAILABLE
    deliverable = true
    vendor = csVendor()
    beverageFlavours = mutableSetOf(csBeverageFlavour().apply {
        beverageFlavourSizes = mutableSetOf(csBeverageFlavourSize())
    })
    beverageDiscounts = mutableSetOf()
}

private fun csDessertFlavour(): DessertFlavour = DessertFlavour().apply {
    id = CS_DESSERT_FLAVOUR_ID
    name = "Chocolate"
    availability = Availability.AVAILABLE
    dessertFlavourSizes = mutableSetOf(DessertFlavourSize().apply {
        id = CS_DESSERT_FLAVOUR_SIZE_ID
        size = Size.MEDIUM
        price = 1200
        availability = Availability.AVAILABLE
    })
}

private fun csDessert(): Dessert = Dessert().apply {
    id = CS_DESSERT_ID
    name = "Chocolate Cake"
    availability = Availability.AVAILABLE
    deliverable = true
    vendor = csVendor()
    dessertFlavours = mutableSetOf(csDessertFlavour())
    dessertDiscounts = mutableSetOf()
}

private fun csHandheldSize(): HandheldSize = HandheldSize().apply {
    id = CS_HANDHELD_SIZE_ID
    size = Size.MEDIUM
    price = 3000
    availability = Availability.AVAILABLE
}

private fun csHandheld(): Handheld = Handheld().apply {
    id = CS_HANDHELD_ID
    name = "Classic Burger"
    availability = Availability.AVAILABLE
    deliverable = true
    vendor = csVendor()
    handheldSizes = mutableSetOf(csHandheldSize())
    handheldFillings = mutableSetOf()
    handheldDiscounts = mutableSetOf()
}


class CartServiceTest {

    private lateinit var cartRepository: CartRepository
    private lateinit var customerRepository: CustomerRepository
    private lateinit var foodRepository: FoodRepository
    private lateinit var beverageRepository: BeverageRepository
    private lateinit var dessertRepository: DessertRepository
    private lateinit var handheldRepository: HandheldRepository
    private lateinit var cartService: CartService

    @BeforeEach
    fun setUp() {
        cartRepository = mock()
        customerRepository = mock()
        foodRepository = mock()
        beverageRepository = mock()
        dessertRepository = mock()
        handheldRepository = mock()

        cartService = CartService(
            cartRepository = cartRepository,
            customerRepository = customerRepository,
            foodRepository = foodRepository,
            beverageRepository = beverageRepository,
            dessertRepository = dessertRepository,
            handheldRepository = handheldRepository
        )
    }


    // getCart
    @Nested
    inner class GetCart {

        @Test
        fun `returns existing cart for the customer`() {
            val cart = Cart()
            val customer = csCustomer(cart = cart)
            whenever(customerRepository.findById(CS_CUSTOMER_ID)).thenReturn(Optional.of(customer))

            val result = cartService.getCart(csCustomerSecurity())

            assertEquals(cart, result)
        }

        @Test
        fun `returns null when customer has no cart`() {
            val customer = csCustomer(cart = null)
            whenever(customerRepository.findById(CS_CUSTOMER_ID)).thenReturn(Optional.of(customer))

            val result = cartService.getCart(csCustomerSecurity())

            assertNull(result)
        }
    }


    // addItemToCart — food
    @Nested
    inner class AddFoodToCart {

        private fun foodItemDto(): ItemDto = ItemDto(
            quickDelivery = false,
            customerAddressId = null,
            foodItemDto = FoodItemDto(
                foodId = CS_FOOD_ID,
                foodQuantity = 1,
                complementId = CS_COMPLEMENT_ID,
                addOnIds = null,
                foodSizeId = CS_FOOD_SIZE_ID
            ),
            beverageItemDto = null,
            dessertItemDto = null,
            handheldItemDto = null,
            orderType = "DINE_IN"
        )

        @BeforeEach
        fun stub() {
            val food = csFood()
            val customer = csCustomer()
            whenever(customerRepository.findById(CS_CUSTOMER_ID)).thenReturn(Optional.of(customer))
            whenever(customerRepository.flush()).then { }
            whenever(foodRepository.findByIdWithAllRelationsAndIsActiveTrue(CS_FOOD_ID))
                .thenReturn(Optional.of(food))
            whenever(cartRepository.saveAndFlush(any())).thenAnswer { it.arguments[0] }
        }

        @Test
        fun `throws IllegalArgumentException when complement is not found on the food`() {
            assertThrows<IllegalArgumentException> {
                cartService.addItemToCart(foodItemDto(), csCustomerSecurity())
            }
        }

        @Test
        fun `throws UserNotFoundException when customer does not exist`() {
            whenever(customerRepository.findById(CS_CUSTOMER_ID)).thenReturn(Optional.empty())

            assertThrows<UserNotFoundException> {
                cartService.addItemToCart(foodItemDto(), csCustomerSecurity())
            }
        }

        @Test
        fun `throws RequestedEntityNotFoundException when food does not exist`() {
            whenever(foodRepository.findByIdWithAllRelationsAndIsActiveTrue(CS_FOOD_ID))
                .thenReturn(Optional.empty())

            assertThrows<RequestedEntityNotFoundException> {
                cartService.addItemToCart(foodItemDto(), csCustomerSecurity())
            }
        }
    }


    // addItemToCart — beverage
    @Nested
    inner class AddBeverageToCart {

        private fun beverageItemDto(): ItemDto = ItemDto(
            quickDelivery = false,
            customerAddressId = null,
            foodItemDto = null,
            beverageItemDto = BeverageItemDto(
                beverageId = CS_BEVERAGE_ID,
                beverageFlavourId = CS_FLAVOUR_ID,
                beverageFlavourSizeId = CS_FLAVOUR_SIZE_ID,
                quantity = 2
            ),
            dessertItemDto = null,
            handheldItemDto = null,
            orderType = "DINE_IN"
        )

        @BeforeEach
        fun stub() {
            val customer = csCustomer()
            whenever(customerRepository.findById(CS_CUSTOMER_ID)).thenReturn(Optional.of(customer))
            whenever(customerRepository.flush()).then { }
            whenever(beverageRepository.findById(CS_BEVERAGE_ID))
                .thenReturn(Optional.of(csBeverage()))
            whenever(cartRepository.saveAndFlush(any())).thenAnswer { it.arguments[0] }
        }

        @Test
        fun `adds beverage to the cart and returns it`() {
            val result = cartService.addItemToCart(beverageItemDto(), csCustomerSecurity())

            assertNotNull(result)
            verify(cartRepository).saveAndFlush(any())
        }

        @Test
        fun `cart total is updated after adding beverage`() {
            val result = cartService.addItemToCart(beverageItemDto(), csCustomerSecurity())

            // 1500 price * 2 quantity = 3000
            assertEquals(3000L, result.totalAmount)
        }

        @Test
        fun `throws RequestedEntityNotFoundException when beverage does not exist`() {
            whenever(beverageRepository.findById(CS_BEVERAGE_ID)).thenReturn(Optional.empty())

            assertThrows<RequestedEntityNotFoundException> {
                cartService.addItemToCart(beverageItemDto(), csCustomerSecurity())
            }
        }

        @Test
        fun `throws RequestedEntityNotFoundException when flavour id does not match any flavour on the beverage`() {
            val beverageWithDifferentFlavour = csBeverage().apply {
                beverageFlavours = mutableSetOf(BeverageFlavour().apply {
                    id = UUID.randomUUID() // different flavour
                    name = "Mango"
                    availability = Availability.AVAILABLE
                    beverageFlavourSizes = mutableSetOf()
                })
            }
            whenever(beverageRepository.findById(CS_BEVERAGE_ID))
                .thenReturn(Optional.of(beverageWithDifferentFlavour))

            assertThrows<RequestedEntityNotFoundException> {
                cartService.addItemToCart(beverageItemDto(), csCustomerSecurity())
            }
        }

        @Test
        fun `merges with existing cart item when same beverage configuration is added again`() {
            val customer = csCustomer()
            val cart = Cart().apply { this.customer = customer }
            val existingItem = BeverageCartItem().apply {
                this.cart = cart
                beverage = csBeverage()
                beverageFlavour = csBeverageFlavour()
                beverageFlavourSize = csBeverageFlavourSize()
                quantity = 1
                totalAmount = 1500
                orderType = OrderType.DINE_IN
                vendor = csVendor()
            }
            cart.beverageCartItems.add(existingItem)
            customer.cart = cart

            whenever(customerRepository.findById(CS_CUSTOMER_ID)).thenReturn(Optional.of(customer))

            val result = cartService.addItemToCart(beverageItemDto(), csCustomerSecurity())

            // The existing item quantity should be merged (1 + 2 = 3)
            assertEquals(3, result.beverageCartItems.first().quantity)
        }
    }


    // addItemToCart — dessert
    @Nested
    inner class AddDessertToCart {

        private fun dessertItemDto(): ItemDto = ItemDto(
            quickDelivery = false,
            customerAddressId = null,
            foodItemDto = null,
            beverageItemDto = null,
            dessertItemDto = DessertItemDto(
                dessertId = CS_DESSERT_ID,
                dessertFlavourId = CS_DESSERT_FLAVOUR_ID,
                dessertFlavourSizeId = CS_DESSERT_FLAVOUR_SIZE_ID,
                quantity = 1
            ),
            handheldItemDto = null,
            orderType = "PICKUP"
        )

        @BeforeEach
        fun stub() {
            val customer = csCustomer()
            whenever(customerRepository.findById(CS_CUSTOMER_ID)).thenReturn(Optional.of(customer))
            whenever(customerRepository.flush()).then { }
            whenever(dessertRepository.findById(CS_DESSERT_ID)).thenReturn(Optional.of(csDessert()))
            whenever(cartRepository.saveAndFlush(any())).thenAnswer { it.arguments[0] }
        }

        @Test
        fun `adds dessert to the cart and returns it`() {
            val result = cartService.addItemToCart(dessertItemDto(), csCustomerSecurity())

            assertNotNull(result)
            verify(cartRepository).saveAndFlush(any())
        }

        @Test
        fun `throws RequestedEntityNotFoundException when dessert does not exist`() {
            whenever(dessertRepository.findById(CS_DESSERT_ID)).thenReturn(Optional.empty())

            assertThrows<RequestedEntityNotFoundException> {
                cartService.addItemToCart(dessertItemDto(), csCustomerSecurity())
            }
        }

        @Test
        fun `throws RequestedEntityNotFoundException when dessert flavour id does not match`() {
            val dessertWithOtherFlavour = csDessert().apply {
                dessertFlavours = mutableSetOf(DessertFlavour().apply {
                    id = UUID.randomUUID()
                    name = "Vanilla"
                    availability = Availability.AVAILABLE
                    dessertFlavourSizes = mutableSetOf()
                })
            }
            whenever(dessertRepository.findById(CS_DESSERT_ID)).thenReturn(Optional.of(dessertWithOtherFlavour))

            assertThrows<RequestedEntityNotFoundException> {
                cartService.addItemToCart(dessertItemDto(), csCustomerSecurity())
            }
        }
    }


    // addItemToCart — handheld
    @Nested
    inner class AddHandheldToCart {

        private fun handheldItemDto(): ItemDto = ItemDto(
            quickDelivery = false,
            customerAddressId = null,
            foodItemDto = null,
            beverageItemDto = null,
            dessertItemDto = null,
            handheldItemDto = HandheldItemDto(
                handheldId = CS_HANDHELD_ID,
                handheldSizeId = CS_HANDHELD_SIZE_ID,
                quantity = 1
            ),
            orderType = "DINE_IN"
        )

        @BeforeEach
        fun stub() {
            val customer = csCustomer()
            whenever(customerRepository.findById(CS_CUSTOMER_ID)).thenReturn(Optional.of(customer))
            whenever(customerRepository.flush()).then { }
            whenever(handheldRepository.findById(CS_HANDHELD_ID)).thenReturn(Optional.of(csHandheld()))
            whenever(cartRepository.saveAndFlush(any())).thenAnswer { it.arguments[0] }
        }

        @Test
        fun `adds handheld to the cart and returns it`() {
            val result = cartService.addItemToCart(handheldItemDto(), csCustomerSecurity())

            assertNotNull(result)
            verify(cartRepository).saveAndFlush(any())
        }

        @Test
        fun `cart total is updated after adding handheld`() {
            val result = cartService.addItemToCart(handheldItemDto(), csCustomerSecurity())

            // 3000 price * 1 quantity = 3000
            assertEquals(3000L, result.totalAmount)
        }

        @Test
        fun `throws RequestedEntityNotFoundException when handheld does not exist`() {
            whenever(handheldRepository.findById(CS_HANDHELD_ID)).thenReturn(Optional.empty())

            assertThrows<RequestedEntityNotFoundException> {
                cartService.addItemToCart(handheldItemDto(), csCustomerSecurity())
            }
        }

        @Test
        fun `throws RequestedEntityNotFoundException when handheld size id does not match any size on the handheld`() {
            val handheldWithDifferentSize = csHandheld().apply {
                handheldSizes = mutableSetOf(HandheldSize().apply {
                    id = UUID.randomUUID()
                    size = Size.LARGE
                    price = 4000
                    availability = Availability.AVAILABLE
                })
            }
            whenever(handheldRepository.findById(CS_HANDHELD_ID))
                .thenReturn(Optional.of(handheldWithDifferentSize))

            assertThrows<RequestedEntityNotFoundException> {
                cartService.addItemToCart(handheldItemDto(), csCustomerSecurity())
            }
        }

        @Test
        fun `merges with existing cart item when same handheld configuration is added again`() {
            val customer = csCustomer()
            val cart = Cart().apply { this.customer = customer }
            val existingItem = HandheldCartItem().apply {
                this.cart = cart
                handheld = csHandheld()
                size = csHandheldSize()
                quantity = 1
                totalAmount = 3000
                orderType = OrderType.DINE_IN
                vendor = csVendor()
            }
            cart.handheldCartItems.add(existingItem)
            customer.cart = cart

            whenever(customerRepository.findById(CS_CUSTOMER_ID)).thenReturn(Optional.of(customer))

            val result = cartService.addItemToCart(handheldItemDto(), csCustomerSecurity())

            // The existing item quantity should be merged (1 + 1 = 2)
            assertEquals(2, result.handheldCartItems.first().quantity)
        }
    }


    // removeItems
    @Nested
    inner class RemoveItems {

        @Test
        fun `removes specified items from cart`() {
            val cartItemId = UUID.randomUUID()
            val cart = Cart()
            val customer = csCustomer(cart = cart)
            whenever(customerRepository.findById(CS_CUSTOMER_ID)).thenReturn(Optional.of(customer))

            val dto = CartItemDto(ids = listOf(cartItemId), deliveryAddress = null, quickDelivery = false)
            cartService.removeItems(dto, csCustomerSecurity())

            // Cart is empty after removal — deleted
            verify(cartRepository).delete(cart)
        }

        @Test
        fun `is a no-op when customer has no cart`() {
            val customer = csCustomer(cart = null)
            whenever(customerRepository.findById(CS_CUSTOMER_ID)).thenReturn(Optional.of(customer))

            val dto = CartItemDto(ids = listOf(UUID.randomUUID()), deliveryAddress = null, quickDelivery = false)

            assertDoesNotThrow { cartService.removeItems(dto, csCustomerSecurity()) }
            verify(cartRepository, never()).save(any())
            verify(cartRepository, never()).delete(any<Cart>())
        }

        @Test
        fun `throws UserNotFoundException when customer is not found`() {
            whenever(customerRepository.findById(CS_CUSTOMER_ID)).thenReturn(Optional.empty())

            val dto = CartItemDto(ids = listOf(UUID.randomUUID()), deliveryAddress = null, quickDelivery = false)

            assertThrows<UserNotFoundException> {
                cartService.removeItems(dto, csCustomerSecurity())
            }
        }
    }


    // changeQuantity
    @Nested
    inner class ChangeQuantity {

        @Test
        fun `is a no-op when customer has no cart`() {
            val customer = csCustomer(cart = null)
            whenever(customerRepository.findById(CS_CUSTOMER_ID)).thenReturn(Optional.of(customer))

            val dto = ChangeQuantityDto(quantity = 3)

            assertDoesNotThrow {
                cartService.changeQuantity(UUID.randomUUID(), csCustomerSecurity(), dto)
            }
        }

        @Test
        fun `throws UserNotFoundException when customer is not found`() {
            whenever(customerRepository.findById(CS_CUSTOMER_ID)).thenReturn(Optional.empty())

            assertThrows<UserNotFoundException> {
                cartService.changeQuantity(UUID.randomUUID(), csCustomerSecurity(), ChangeQuantityDto(quantity = 2))
            }
        }

        @Test
        fun `removes item from cart when quantity is 0`() {
            val cartItemId = UUID.randomUUID()
            val cart = Cart()
            val beverageItem = BeverageCartItem().apply {
                id = cartItemId
                this.cart = cart
                beverage = csBeverage()
                beverageFlavour = csBeverageFlavour()
                beverageFlavourSize = csBeverageFlavourSize()
                quantity = 1
                totalAmount = 1500
                vendor = csVendor()
            }
            cart.beverageCartItems.add(beverageItem)
            val customer = csCustomer(cart = cart)
            whenever(customerRepository.findById(CS_CUSTOMER_ID)).thenReturn(Optional.of(customer))

            cartService.changeQuantity(cartItemId, csCustomerSecurity(), ChangeQuantityDto(quantity = 0))

            assertTrue(cart.beverageCartItems.isEmpty())
        }
    }


    // deleteCart
    @Nested
    inner class DeleteCart {

        @Test
        fun `deletes existing cart and sets customer cart to null`() {
            val cart = Cart()
            val customer = csCustomer(cart = cart)
            whenever(customerRepository.findById(CS_CUSTOMER_ID)).thenReturn(Optional.of(customer))

            cartService.deleteCart(csCustomerSecurity())

            verify(cartRepository).delete(cart)
            assertNull(customer.cart)
        }

        @Test
        fun `is a no-op when customer has no cart`() {
            val customer = csCustomer(cart = null)
            whenever(customerRepository.findById(CS_CUSTOMER_ID)).thenReturn(Optional.of(customer))

            cartService.deleteCart(csCustomerSecurity())

            verify(cartRepository, never()).delete(any<Cart>())
        }

        @Test
        fun `throws UserNotFoundException when customer does not exist`() {
            whenever(customerRepository.findById(CS_CUSTOMER_ID)).thenReturn(Optional.empty())

            assertThrows<UserNotFoundException> {
                cartService.deleteCart(csCustomerSecurity())
            }
        }
    }
}

private fun assertDoesNotThrow(block: () -> Unit) {
    org.junit.jupiter.api.Assertions.assertDoesNotThrow(block)
}


