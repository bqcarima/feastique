package com.qinet.feastique.service.order

import com.qinet.feastique.exception.PermissionDeniedException
import com.qinet.feastique.exception.RequestedEntityNotFoundException
import com.qinet.feastique.exception.UserNotFoundException
import com.qinet.feastique.model.dto.order.CartItemDto
import com.qinet.feastique.model.dto.order.ItemDto
import com.qinet.feastique.model.dto.order.OrderUpdateDto
import com.qinet.feastique.model.dto.order.FoodItemDto
import com.qinet.feastique.model.dto.order.BeverageItemDto
import com.qinet.feastique.model.dto.order.DessertItemDto
import com.qinet.feastique.model.entity.address.CustomerAddress
import com.qinet.feastique.model.entity.consumables.addOn.AddOn
import com.qinet.feastique.model.entity.consumables.addOn.FoodAddOn
import com.qinet.feastique.model.entity.consumables.beverage.Beverage
import com.qinet.feastique.model.entity.consumables.complement.Complement
import com.qinet.feastique.model.entity.consumables.complement.FoodComplement
import com.qinet.feastique.model.entity.consumables.dessert.Dessert
import com.qinet.feastique.model.entity.consumables.flavour.BeverageFlavour
import com.qinet.feastique.model.entity.consumables.flavour.DessertFlavour
import com.qinet.feastique.model.entity.consumables.food.Food
import com.qinet.feastique.model.entity.consumables.handheld.Handheld
import com.qinet.feastique.model.entity.order.Cart
import com.qinet.feastique.model.entity.order.Order
import com.qinet.feastique.model.entity.sales.BeverageSale
import com.qinet.feastique.model.entity.sales.DessertSale
import com.qinet.feastique.model.entity.sales.FoodSale
import com.qinet.feastique.model.entity.sales.HandheldSale
import com.qinet.feastique.model.entity.size.BeverageFlavourSize
import com.qinet.feastique.model.entity.size.DessertFlavourSize
import com.qinet.feastique.model.entity.size.FoodSize
import com.qinet.feastique.model.entity.size.HandheldSize
import com.qinet.feastique.model.entity.user.Customer
import com.qinet.feastique.model.entity.user.Vendor
import com.qinet.feastique.model.enums.*
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
import com.qinet.feastique.security.UserSecurity
import com.qinet.feastique.utility.CursorEncoder
import com.qinet.feastique.utility.SecurityUtility
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.*
import org.springframework.security.core.authority.SimpleGrantedAuthority
import java.time.LocalTime
import java.util.*
import kotlin.collections.emptyList

// Shared fixtures

private val OS_CUSTOMER_ID: UUID = UUID.randomUUID()
private val OS_VENDOR_ID: UUID = UUID.randomUUID()
private val OS_ORDER_ID: UUID = UUID.randomUUID()

private val OS_ADD_ON_ID: UUID = UUID.randomUUID()
private val OS_BEVERAGE_ID: UUID = UUID.randomUUID()
private val OS_COMPLEMENT_ID: UUID = UUID.randomUUID()
private val OS_DESSERT_ID: UUID = UUID.randomUUID()
private val OS_FOOD_ID: UUID = UUID.randomUUID()
private val OS_HANDHELD_ID: UUID = UUID.randomUUID()
private val OS_ADDRESS_ID: UUID = UUID.randomUUID()
private val OS_SIZE_ID: UUID = UUID.randomUUID()
private val OS_FLAVOUR_ID: UUID = UUID.randomUUID()
private val OS_FLAVOUR_SIZE_ID: UUID = UUID.randomUUID()

private fun osCustomerSecurity(): UserSecurity = UserSecurity(
    id = OS_CUSTOMER_ID,
    username = "jane_doe",
    password = "hashed",
    userAuthorities = mutableListOf(SimpleGrantedAuthority("ROLE_CUSTOMER"))
)

private fun osVendorSecurity(): UserSecurity = UserSecurity(
    id = OS_VENDOR_ID,
    username = "sabi_chef",
    password = "hashed",
    userAuthorities = mutableListOf(SimpleGrantedAuthority("ROLE_VENDOR"))
)

private fun osVendor(): Vendor = Vendor().apply {
    id = OS_VENDOR_ID
    username = "sabi_chef"
    chefName = "Sabi Chef"
    accountType = AccountType.VENDOR
    openingTime = LocalTime.of(8, 0)
    closingTime = LocalTime.of(18, 0)
    balance = 0
}

private fun osCustomer(): Customer = Customer().apply {
    id = OS_CUSTOMER_ID
    username = "jane_doe"
    accountType = AccountType.CUSTOMER
}

private fun osAddress(): CustomerAddress = CustomerAddress().apply {
    id = OS_ADDRESS_ID
    customer = osCustomer()
    country = "Cameroon"
    region = Region.NORTHWEST
    city = "Bamenda"
    neighbourhood = "Up Station"
    directions = "Near Government House"
    default = true
    isActive = true
}

private fun osFoodSize(): FoodSize = FoodSize().apply {
    id = OS_SIZE_ID
    size = Size.MEDIUM
    priceIncrease = 0
    availability = Availability.AVAILABLE
}

private fun osAddOn(): AddOn = AddOn().apply {
    id = OS_ADD_ON_ID
    name = "Shrimps"
    price = 200
    availability = Availability.AVAILABLE
}
private fun osComplement(): Complement = Complement().apply {
    id = OS_COMPLEMENT_ID
    name = "Plantain"
    price = 200
    availability = Availability.AVAILABLE
}

private fun osFood(): Food = Food().apply {
    id = OS_FOOD_ID
    name = "Jollof Rice"
    basePrice = 3000
    availability = Availability.AVAILABLE
    deliverable = true
    deliveryFee = 500
    preparationTime = 20
    vendor = osVendor()
    foodSizes = mutableSetOf(osFoodSize())
    foodDiscounts = mutableSetOf()
    foodAddOns = mutableSetOf(FoodAddOn().apply {
        addOn = osAddOn()
        food = Food().apply { id = OS_FOOD_ID }
    })
    foodComplements = mutableSetOf(FoodComplement().apply {
        complement = osComplement()
        food = Food().apply { id = OS_FOOD_ID }
    })
}

private fun osBeverageFlavourSize(): BeverageFlavourSize = BeverageFlavourSize().apply {
    id = OS_FLAVOUR_SIZE_ID
    size = Size.MEDIUM
    price = 1500
    availability = Availability.AVAILABLE
    beverageFlavour = BeverageFlavour().apply {
        id = OS_FLAVOUR_ID
        name = "Original"
    }
}

private fun osBeverageFlavour(): BeverageFlavour = BeverageFlavour().apply {
    id = OS_FLAVOUR_ID
    name = "Original"
    availability = Availability.AVAILABLE
    beverageFlavourSizes = mutableSetOf(osBeverageFlavourSize())
}

private fun osBeverage(): Beverage = Beverage().apply {
    id = OS_BEVERAGE_ID
    name = "Malt"
    availability = Availability.AVAILABLE
    deliverable = true
    deliveryFee = 0
    vendor = osVendor()
    beverageFlavours = mutableSetOf(osBeverageFlavour())
    beverageDiscounts = mutableSetOf()
}

private fun osDessertFlavourSize(): DessertFlavourSize = DessertFlavourSize().apply {
    id = OS_FLAVOUR_SIZE_ID
    size = Size.MEDIUM
    price = 1500
    availability = Availability.AVAILABLE
    dessertFlavour = DessertFlavour().apply {
        id = UUID.randomUUID()
        name = "Chocolate"
    }
}

private fun osDessertFlavour(): DessertFlavour = DessertFlavour().apply {
    id = OS_FLAVOUR_ID
    name = "Chocolate"
    availability = Availability.AVAILABLE
    dessertFlavourSizes = mutableSetOf(osDessertFlavourSize())
}

private fun osDessert(): Dessert = Dessert().apply {
    id = OS_DESSERT_ID
    name = "Chocolate Cake"
    availability = Availability.AVAILABLE
    deliverable = true
    deliveryFee = 500
    vendor = osVendor()
    dessertFlavours = mutableSetOf(osDessertFlavour())
    dessertDiscounts = mutableSetOf()
}

private fun osHandheldSize(): HandheldSize = HandheldSize().apply {
    id = OS_SIZE_ID
    size = Size.MEDIUM
    price = 0
    availability = Availability.AVAILABLE
}

private fun osHandheld(): Handheld = Handheld().apply {
    id = OS_HANDHELD_ID
    name = "Burger"
    availability = Availability.AVAILABLE
    deliverable = true
    deliveryFee = 0
    vendor = osVendor()
    handheldSizes = mutableSetOf(osHandheldSize())
    handheldDiscounts = mutableSetOf()
}

private fun osPendingOrder(): Order = Order().apply {
    id = OS_ORDER_ID
    customer = osCustomer()
    vendor = osVendor()
    orderStatus = OrderStatus.PENDING
    orderType = OrderType.DINE_IN
    totalAmount = 3000
    customerDeleted = false
    vendorDeleted = false
}


class OrderServiceTest {

    private lateinit var orderRepository: OrderRepository
    private lateinit var customerRepository: CustomerRepository
    private lateinit var beverageRepository: BeverageRepository
    private lateinit var dessertRepository: DessertRepository
    private lateinit var foodRepository: FoodRepository
    private lateinit var vendorRepository: VendorRepository
    private lateinit var customerAddressRepository: CustomerAddressRepository
    private lateinit var securityUtility: SecurityUtility
    private lateinit var cartRepository: CartRepository
    private lateinit var foodSaleRepository: FoodSaleRepository
    private lateinit var addOnSaleRepository: AddOnSaleRepository
    private lateinit var beverageSaleRepository: BeverageSaleRepository
    private lateinit var complementSaleRepository: ComplementSaleRepository
    private lateinit var dessertSaleRepository: DessertSaleRepository
    private lateinit var handheldRepository: HandheldRepository
    private lateinit var handheldSaleRepository: HandheldSaleRepository
    private lateinit var cursorEncoder: CursorEncoder
    private lateinit var orderService: OrderService

    @BeforeEach
    fun setUp() {
        orderRepository = mock()
        customerRepository = mock()
        beverageRepository = mock()
        dessertRepository = mock()
        foodRepository = mock()
        vendorRepository = mock()
        customerAddressRepository = mock()
        securityUtility = mock()
        cartRepository = mock()
        foodSaleRepository = mock()
        addOnSaleRepository = mock()
        beverageSaleRepository = mock()
        complementSaleRepository = mock()
        dessertSaleRepository = mock()
        handheldRepository = mock()
        handheldSaleRepository = mock()
        cursorEncoder = mock()

        orderService = OrderService(
            orderRepository = orderRepository,
            customerRepository = customerRepository,
            beverageRepository = beverageRepository,
            dessertRepository = dessertRepository,
            foodRepository = foodRepository,
            vendorRepository = vendorRepository,
            customerAddressRepository = customerAddressRepository,
            securityUtility = securityUtility,
            cartRepository = cartRepository,
            foodSaleRepository = foodSaleRepository,
            addOnSaleRepository = addOnSaleRepository,
            beverageSaleRepository = beverageSaleRepository,
            complementSaleRepository = complementSaleRepository,
            dessertSaleRepository = dessertSaleRepository,
            handheldRepository = handheldRepository,
            handheldSaleRepository = handheldSaleRepository,
            cursorEncoder = cursorEncoder
        )
    }


    // getOrder
    @Nested
    inner class GetOrder {

        @Test
        fun `returns order for customer when not deleted`() {
            val order = osPendingOrder()

            whenever(securityUtility.getSingleRole(argThat { id == OS_CUSTOMER_ID })).thenReturn("CUSTOMER")
            whenever(
                orderRepository.findByIdAndCustomerIdAndCustomerDeletedFalse(OS_ORDER_ID, OS_CUSTOMER_ID)
            ).thenReturn(order)

            val result = orderService.getOrder(OS_ORDER_ID, osCustomerSecurity())

            assertEquals(OS_ORDER_ID, result?.id)
        }

        @Test
        fun `returns order for vendor when not deleted`() {
            val order = osPendingOrder()

            whenever(securityUtility.getSingleRole(argThat { id == OS_VENDOR_ID })).thenReturn("VENDOR")
            whenever(
                orderRepository.findByIdAndVendorIdAndVendorDeletedFalse(OS_ORDER_ID, OS_VENDOR_ID)
            ).thenReturn(order)

            val result = orderService.getOrder(OS_ORDER_ID, osVendorSecurity())

            assertEquals(OS_ORDER_ID, result?.id)
        }

        @Test
        fun `returns null when customer order has been soft-deleted`() {
            whenever(securityUtility.getSingleRole(argThat { id == OS_CUSTOMER_ID })).thenReturn("CUSTOMER")
            whenever(
                orderRepository.findByIdAndCustomerIdAndCustomerDeletedFalse(OS_ORDER_ID, OS_CUSTOMER_ID)
            ).thenReturn(null)

            val result = orderService.getOrder(OS_ORDER_ID, osCustomerSecurity())

            assertNull(result)
        }

        @Test
        fun `returns null when vendor order has been soft-deleted`() {
            whenever(securityUtility.getSingleRole(argThat { id == OS_VENDOR_ID })).thenReturn("VENDOR")
            whenever(
                orderRepository.findByIdAndVendorIdAndVendorDeletedFalse(OS_ORDER_ID, OS_VENDOR_ID)
            ).thenReturn(null)

            val result = orderService.getOrder(OS_ORDER_ID, osVendorSecurity())

            assertNull(result)
        }
    }


    // placeOrderFromItemScreen — food
    @Nested
    inner class PlaceOrderFoodScreen {

        private fun foodItemDto(): ItemDto = ItemDto(
            quickDelivery = false,
            customerAddressId = OS_ADDRESS_ID,
            foodItemDto = FoodItemDto(
                foodId = OS_FOOD_ID,
                foodQuantity = 1,
                complementId = OS_COMPLEMENT_ID,
                addOnIds = null,
                foodSizeId = OS_SIZE_ID
            ),
            beverageItemDto = null,
            dessertItemDto = null,
            handheldItemDto = null,
            orderType = "DINE_IN"
        )

        @BeforeEach
        fun stub() {
            val customer = osCustomer().apply {
                address = mutableSetOf(osAddress())
            }
            whenever(customerRepository.findById(OS_CUSTOMER_ID)).thenReturn(Optional.of(customer))
            whenever(foodRepository.findByIdWithAllRelationsAndIsActiveTrue(OS_FOOD_ID))
                .thenReturn(Optional.of(osFood()))
            whenever(vendorRepository.findById(OS_VENDOR_ID)).thenReturn(Optional.of(osVendor()))
            whenever(orderRepository.saveAndFlush(any())).thenAnswer { it.arguments[0] }
            whenever(orderRepository.findByIdWithAllRelations(any()))
                .thenReturn(Optional.of(osPendingOrder()))
        }

        @Test
        fun `places order and returns it`() {
            val result = orderService.placeOrderFromItemScreen(foodItemDto(), osCustomerSecurity())
            assertNotNull(result)
        }

        @Test
        fun `persists the order via repository`() {
            orderService.placeOrderFromItemScreen(foodItemDto(), osCustomerSecurity())
            verify(orderRepository).saveAndFlush(any())
        }

        @Test
        fun `sets order status to PENDING`() {
            orderService.placeOrderFromItemScreen(foodItemDto(), osCustomerSecurity())
            verify(orderRepository).saveAndFlush(argThat {
                orderStatus == OrderStatus.PENDING
            })
        }

        @Test
        fun `throws UserNotFoundException when customer does not exist`() {
            whenever(customerRepository.findById(OS_CUSTOMER_ID)).thenReturn(Optional.empty())

            assertThrows<UserNotFoundException> {
                orderService.placeOrderFromItemScreen(foodItemDto(), osCustomerSecurity())
            }
        }

        @Test
        fun `throws RequestedEntityNotFoundException when food does not exist`() {
            whenever(foodRepository.findByIdWithAllRelationsAndIsActiveTrue(OS_FOOD_ID))
                .thenReturn(Optional.empty())

            assertThrows<RequestedEntityNotFoundException> {
                orderService.placeOrderFromItemScreen(foodItemDto(), osCustomerSecurity())
            }
        }

        @Test
        fun `throws RequestedEntityNotFoundException when customer has no address matching dto`() {
            val customerNoAddress = osCustomer().apply { address = mutableSetOf() }
            whenever(customerRepository.findById(OS_CUSTOMER_ID)).thenReturn(Optional.of(customerNoAddress))

            assertThrows<RequestedEntityNotFoundException> {
                orderService.placeOrderFromItemScreen(foodItemDto(), osCustomerSecurity())
            }
        }

        @Test
        fun `does not assign delivery fee for DINE_IN order`() {
            orderService.placeOrderFromItemScreen(foodItemDto(), osCustomerSecurity())

            verify(orderRepository).saveAndFlush(argThat {
                deliveryFee == 0L
            })
        }

        @Test
        fun `throws IllegalArgumentException when complement is not found on the food`() {
            val foodWithoutComplement = osFood().apply { foodComplements = mutableSetOf() }
            whenever(foodRepository.findByIdWithAllRelationsAndIsActiveTrue(OS_FOOD_ID))
                .thenReturn(Optional.of(foodWithoutComplement))

            assertThrows<IllegalArgumentException> {
                orderService.placeOrderFromItemScreen(foodItemDto(), osCustomerSecurity())
            }
        }
    }


    // placeOrderFromItemScreen — beverage
    @Nested
    inner class PlaceOrderBeverageScreen {

        private fun beverageItemDto(): ItemDto = ItemDto(
            quickDelivery = false,
            customerAddressId = OS_ADDRESS_ID,
            foodItemDto = null,
            beverageItemDto = BeverageItemDto(
                beverageId = OS_BEVERAGE_ID,
                beverageFlavourId = OS_FLAVOUR_ID,
                beverageFlavourSizeId = OS_FLAVOUR_SIZE_ID,
                quantity = 1
            ),
            dessertItemDto = null,
            handheldItemDto = null,
            orderType = "DINE_IN"
        )

        @BeforeEach
        fun stub() {
            val customer = osCustomer().apply { address = mutableSetOf(osAddress()) }
            whenever(customerRepository.findById(OS_CUSTOMER_ID)).thenReturn(Optional.of(customer))
            whenever(beverageRepository.findById(OS_BEVERAGE_ID)).thenReturn(Optional.of(osBeverage()))
            whenever(vendorRepository.findById(OS_VENDOR_ID)).thenReturn(Optional.of(osVendor()))
            whenever(orderRepository.saveAndFlush(any())).thenAnswer { it.arguments[0] }
            whenever(orderRepository.findByIdWithAllRelations(any()))
                .thenReturn(Optional.of(osPendingOrder()))
        }

        @Test
        fun `places beverage order and returns it`() {
            val result = orderService.placeOrderFromItemScreen(beverageItemDto(), osCustomerSecurity())
            assertNotNull(result)
        }

        @Test
        fun `throws RequestedEntityNotFoundException when beverage does not exist`() {
            whenever(beverageRepository.findById(OS_BEVERAGE_ID)).thenReturn(Optional.empty())

            assertThrows<RequestedEntityNotFoundException> {
                orderService.placeOrderFromItemScreen(beverageItemDto(), osCustomerSecurity())
            }
        }

        @Test
        fun `throws RequestedEntityNotFoundException when flavour does not exist on beverage`() {
            val beverageWithoutFlavour = osBeverage().apply {
                beverageFlavours = mutableSetOf(BeverageFlavour().apply {
                    id = UUID.randomUUID()
                    name = "Mango"
                    availability = Availability.AVAILABLE
                    beverageFlavourSizes = mutableSetOf()
                })
            }
            whenever(beverageRepository.findById(OS_BEVERAGE_ID)).thenReturn(Optional.of(beverageWithoutFlavour))

            assertThrows<RequestedEntityNotFoundException> {
                orderService.placeOrderFromItemScreen(beverageItemDto(), osCustomerSecurity())
            }
        }
    }

    @Nested
    inner class PlaceOrderDessertScreen {

        private fun dessertItemDto(): ItemDto = ItemDto(
            quickDelivery = false,
            customerAddressId = OS_ADDRESS_ID,
            dessertItemDto = DessertItemDto(
                dessertId = OS_DESSERT_ID,
                dessertFlavourId = OS_FLAVOUR_ID,
                dessertFlavourSizeId = OS_FLAVOUR_SIZE_ID,
                quantity = 1
            ),
            foodItemDto = null,
            beverageItemDto = null,
            handheldItemDto = null,
            orderType = "DINE_IN"
        )

        @BeforeEach
        fun stub() {
            val customer = osCustomer().apply { address = mutableSetOf(osAddress()) }
            whenever(customerRepository.findById(OS_CUSTOMER_ID)).thenReturn(Optional.of(customer))
            whenever(dessertRepository.findById(OS_DESSERT_ID)).thenReturn(Optional.of(osDessert()))
            whenever(vendorRepository.findById(OS_VENDOR_ID)).thenReturn(Optional.of(osVendor()))
            whenever(orderRepository.saveAndFlush(any())).thenAnswer { it.arguments[0] }
            whenever(orderRepository.findByIdWithAllRelations(any()))
                .thenReturn(Optional.of(osPendingOrder()))
        }

        @Test
        fun `places dessert order and returns it`() {
            val result = orderService.placeOrderFromItemScreen(dessertItemDto(), osCustomerSecurity())
            assertNotNull(result)
        }

        @Test
        fun `throws RequestedEntityNotFoundException when dessert does not exist`() {
            whenever(dessertRepository.findById(OS_DESSERT_ID)).thenReturn(Optional.empty())

            assertThrows<RequestedEntityNotFoundException> {
                orderService.placeOrderFromItemScreen(dessertItemDto(), osCustomerSecurity())
            }
        }

        @Test
        fun `throws RequestedEntityNotFoundException when flavour does not exist on dessert`() {
            val dessertWithoutFlavour = osDessert().apply {
                dessertFlavours = mutableSetOf(DessertFlavour().apply {
                    id = UUID.randomUUID()
                    name = "Mango"
                    availability = Availability.AVAILABLE
                    dessertFlavourSizes = mutableSetOf()
                })
            }
            whenever(dessertRepository.findById(OS_DESSERT_ID)).thenReturn(Optional.of(dessertWithoutFlavour))

            assertThrows<RequestedEntityNotFoundException> {
                orderService.placeOrderFromItemScreen(dessertItemDto(), osCustomerSecurity())
            }
        }
    }


    // cancelOrUpdateOrder — customer cancellation
    @Nested
    inner class CustomerCancelOrder {

        @BeforeEach
        fun stub() {
            whenever(securityUtility.getSingleRole(argThat { id == OS_CUSTOMER_ID })).thenReturn("CUSTOMER")
            whenever(
                orderRepository.findByIdAndCustomerIdAndOrderStatus(OS_ORDER_ID, OS_CUSTOMER_ID, OrderStatus.PENDING)
            ).thenReturn(osPendingOrder())
            whenever(orderRepository.save(any())).thenAnswer { it.arguments[0] }
            whenever(orderRepository.findByIdWithAllRelations(OS_ORDER_ID))
                .thenReturn(Optional.of(osPendingOrder()))
        }

        @Test
        fun `cancels order and returns updated order`() {
            val dto = OrderUpdateDto(orderStatus = OrderStatus.CANCELLED)
            val result = orderService.cancelOrUpdateOrder(OS_ORDER_ID, dto, osCustomerSecurity())
            assertNotNull(result)
        }

        @Test
        fun `sets order status to CANCELLED`() {
            val dto = OrderUpdateDto(orderStatus = OrderStatus.CANCELLED)
            orderService.cancelOrUpdateOrder(OS_ORDER_ID, dto, osCustomerSecurity())

            verify(orderRepository).save(argThat { orderStatus == OrderStatus.CANCELLED })
        }

        @Test
        fun `throws RequestedEntityNotFoundException when order is not found or already confirmed`() {
            whenever(
                orderRepository.findByIdAndCustomerIdAndOrderStatus(OS_ORDER_ID, OS_CUSTOMER_ID, OrderStatus.PENDING)
            ).thenReturn(null)

            val dto = OrderUpdateDto(orderStatus = OrderStatus.CANCELLED)

            assertThrows<RequestedEntityNotFoundException> {
                orderService.cancelOrUpdateOrder(OS_ORDER_ID, dto, osCustomerSecurity())
            }
        }

        @Test
        fun `throws PermissionDeniedException when role is unrecognized`() {
            whenever(securityUtility.getSingleRole(argThat { id == OS_CUSTOMER_ID })).thenReturn("UNKNOWN")

            val dto = OrderUpdateDto(orderStatus = OrderStatus.CANCELLED)

            assertThrows<PermissionDeniedException> {
                orderService.cancelOrUpdateOrder(OS_ORDER_ID, dto, osCustomerSecurity())
            }
        }
    }


    // cancelOrUpdateOrder — vendor update
    @Nested
    inner class VendorUpdateOrder {

        @BeforeEach
        fun stub() {
            whenever(securityUtility.getSingleRole(argThat { id == OS_VENDOR_ID })).thenReturn("VENDOR")
            whenever(vendorRepository.save(any())).thenAnswer { it.arguments[0] }
            whenever(vendorRepository.saveAndFlush(any())).thenAnswer { it.arguments[0] }
        }

        @Test
        fun `confirms a pending order`() {
            val pendingOrder = osPendingOrder()
            whenever(
                orderRepository.findByIdAndVendorIdAndOrderStatus(OS_ORDER_ID, OS_VENDOR_ID, OrderStatus.PENDING)
            ).thenReturn(pendingOrder)
            whenever(orderRepository.save(any())).thenAnswer { it.arguments[0] }
            whenever(orderRepository.findByIdWithAllRelations(OS_ORDER_ID))
                .thenReturn(Optional.of(pendingOrder.apply { orderStatus = OrderStatus.CONFIRMED }))
            whenever(foodSaleRepository.saveAll(any<List<FoodSale>>())).thenReturn(emptyList())
            whenever(beverageSaleRepository.saveAll(any<List<BeverageSale>>())).thenReturn(emptyList())
            whenever(dessertSaleRepository.saveAllAndFlush(any<List<DessertSale>>())).thenReturn(emptyList())
            whenever(handheldSaleRepository.saveAllAndFlush(any<List<HandheldSale>>())).thenReturn(emptyList())

            val dto = OrderUpdateDto(orderStatus = OrderStatus.CONFIRMED)
            val result = orderService.cancelOrUpdateOrder(OS_ORDER_ID, dto, osVendorSecurity())

            assertNotNull(result)
        }

        @Test
        fun `throws RequestedEntityNotFoundException when pending order is not found`() {
            whenever(securityUtility.getSingleRole(any()))
                .thenReturn("VENDOR")
            whenever(
                orderRepository.findByIdAndVendorIdAndOrderStatus(OS_ORDER_ID, OS_VENDOR_ID, OrderStatus.PENDING)
            ).thenReturn(null)

            val dto = OrderUpdateDto(orderStatus = OrderStatus.CONFIRMED)

            assertThrows<RequestedEntityNotFoundException> {
                orderService.cancelOrUpdateOrder(OS_ORDER_ID, dto, osVendorSecurity())
            }
        }
    }


    // deleteOrder
    @Nested
    inner class DeleteOrder {

        @Test
        fun `soft-deletes order for customer by setting customerDeleted to true`() {
            val order = osPendingOrder()
            whenever(securityUtility.getSingleRole(any())).thenReturn("CUSTOMER")
            whenever(orderRepository.findById(OS_ORDER_ID)).thenReturn(Optional.of(order))
            whenever(orderRepository.saveAndFlush(any())).thenAnswer { it.arguments[0] }

            orderService.deleteOrder(OS_ORDER_ID, osCustomerSecurity())

            verify(orderRepository).saveAndFlush(argThat { customerDeleted == true })
        }

        @Test
        fun `soft-deletes order for vendor by setting vendorDeleted to true`() {
            val order = osPendingOrder()
            whenever(securityUtility.getSingleRole(any())).thenReturn("VENDOR")
            whenever(orderRepository.findById(OS_ORDER_ID)).thenReturn(Optional.of(order))
            whenever(orderRepository.saveAndFlush(any())).thenAnswer { it.arguments[0] }

            orderService.deleteOrder(OS_ORDER_ID, osVendorSecurity())

            verify(orderRepository).saveAndFlush(argThat { vendorDeleted == true })
        }

        @Test
        fun `throws RequestedEntityNotFoundException when order does not exist`() {
            whenever(securityUtility.getSingleRole(osCustomerSecurity())).thenReturn("CUSTOMER")
            whenever(orderRepository.findById(OS_ORDER_ID)).thenReturn(Optional.empty())

            assertThrows<RequestedEntityNotFoundException> {
                orderService.deleteOrder(OS_ORDER_ID, osCustomerSecurity())
            }
        }

        @Test
        fun `throws PermissionDeniedException when customer does not own the order`() {
            val orderOwnedByOther = osPendingOrder().apply {
                customer = Customer().apply { id = UUID.randomUUID() }
            }
            whenever(securityUtility.getSingleRole(any())).thenReturn("CUSTOMER")
            whenever(orderRepository.findById(OS_ORDER_ID)).thenReturn(Optional.of(orderOwnedByOther))

            assertThrows<PermissionDeniedException> {
                orderService.deleteOrder(OS_ORDER_ID, osCustomerSecurity())
            }
        }

        @Test
        fun `throws PermissionDeniedException when vendor does not own the order`() {
            val orderOwnedByOther = osPendingOrder().apply {
                vendor = Vendor().apply { id = UUID.randomUUID() }
            }
            whenever(securityUtility.getSingleRole(any())).thenReturn("VENDOR")
            whenever(orderRepository.findById(OS_ORDER_ID)).thenReturn(Optional.of(orderOwnedByOther))

            assertThrows<PermissionDeniedException> {
                orderService.deleteOrder(OS_ORDER_ID, osVendorSecurity())
            }
        }
    }


    // placeOrderFromCart
    @Nested
    inner class PlaceOrderFromCart {

        @BeforeEach
        fun stub() {
            val customer = osCustomer()
            whenever(customerRepository.findById(OS_CUSTOMER_ID)).thenReturn(Optional.of(customer))
            whenever(customerAddressRepository.findAllByCustomerIdAndIsActiveTrue(OS_CUSTOMER_ID))
                .thenReturn(listOf(osAddress()))
            whenever(orderRepository.save(any())).thenAnswer { it.arguments[0] }
            whenever(cartRepository.save(any())).thenAnswer { it.arguments[0] }
        }

        @Test
        fun `throws RequestedEntityNotFoundException when cart does not exist`() {
            whenever(cartRepository.findByCustomerId(OS_CUSTOMER_ID)).thenReturn(Optional.empty())

            val dto = CartItemDto(ids = listOf(UUID.randomUUID()), deliveryAddress = null, quickDelivery = false)

            assertThrows<RequestedEntityNotFoundException> {
                orderService.placeOrderFromCart(dto, osCustomerSecurity())
            }
        }

        @Test
        fun `returns empty list when no cart items match the provided ids`() {
            val emptyCart = Cart()
            whenever(cartRepository.findByCustomerId(OS_CUSTOMER_ID)).thenReturn(Optional.of(emptyCart))

            val dto = CartItemDto(
                ids = listOf(UUID.randomUUID()),
                deliveryAddress = null,
                quickDelivery = false
            )

            val result = orderService.placeOrderFromCart(dto, osCustomerSecurity())

            assertTrue(result.isEmpty())
        }

        @Test
        fun `throws UserNotFoundException when customer does not exist`() {
            whenever(customerRepository.findById(OS_CUSTOMER_ID)).thenReturn(Optional.empty())

            val dto = CartItemDto(ids = listOf(UUID.randomUUID()), deliveryAddress = null, quickDelivery = false)

            assertThrows<UserNotFoundException> {
                orderService.placeOrderFromCart(dto, osCustomerSecurity())
            }
        }
    }
}

