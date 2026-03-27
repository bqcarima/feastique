package com.qinet.feastique.service.review

import com.qinet.feastique.exception.PermissionDeniedException
import com.qinet.feastique.exception.RequestedEntityNotFoundException
import com.qinet.feastique.exception.UserNotFoundException
import com.qinet.feastique.model.dto.review.*
import com.qinet.feastique.model.entity.consumables.beverage.Beverage
import com.qinet.feastique.model.entity.consumables.complement.Complement
import com.qinet.feastique.model.entity.consumables.dessert.Dessert
import com.qinet.feastique.model.entity.consumables.food.Food
import com.qinet.feastique.model.entity.consumables.handheld.Handheld
import com.qinet.feastique.model.entity.consumables.flavour.BeverageFlavour
import com.qinet.feastique.model.entity.consumables.flavour.DessertFlavour
import com.qinet.feastique.model.entity.order.Order
import com.qinet.feastique.model.entity.order.item.*
import com.qinet.feastique.model.entity.review.*
import com.qinet.feastique.model.entity.size.BeverageFlavourSize
import com.qinet.feastique.model.entity.size.DessertFlavourSize
import com.qinet.feastique.model.entity.size.FoodSize
import com.qinet.feastique.model.entity.size.HandheldSize
import com.qinet.feastique.model.entity.user.Customer
import com.qinet.feastique.model.entity.user.Vendor
import com.qinet.feastique.model.enums.*
import com.qinet.feastique.repository.order.OrderRepository
import com.qinet.feastique.repository.review.*
import com.qinet.feastique.repository.user.CustomerRepository
import com.qinet.feastique.repository.user.VendorRepository
import com.qinet.feastique.response.review.*
import com.qinet.feastique.security.UserSecurity
import com.qinet.feastique.utility.CursorEncoder
import com.qinet.feastique.utility.DuplicateUtility
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.*
import org.springframework.data.domain.*
import org.springframework.security.core.authority.SimpleGrantedAuthority
import java.util.*

// Shared fixtures

private val RS_CUSTOMER_ID = UUID.randomUUID()
private val RS_VENDOR_ID = UUID.randomUUID()
private val RS_ORDER_ID = UUID.randomUUID()
private val RS_BEVERAGE_ID = UUID.randomUUID()
private val RS_DESSERT_ID = UUID.randomUUID()
private val RS_FOOD_ID = UUID.randomUUID()
private val RS_HANDHELD_ID = UUID.randomUUID()
private val RS_REVIEW_ID = UUID.randomUUID()

private fun rsCustomer(): Customer = Customer().apply {
    id = RS_CUSTOMER_ID
    username = "jane_doe"
    firstName = "Jane"
    lastName = "Doe"
    accountType = AccountType.CUSTOMER
    password = "hashed_passWord123"
}

private fun rsVendor(): Vendor = Vendor().apply {
    id = RS_VENDOR_ID
    username = "sabi_chef"
    chefName = "Sabi Chef"
    restaurantName = "Sabi Foods"
    accountType = AccountType.VENDOR
    region = Region.CENTRE
    vendorCode = "CM020001"
}

private fun rsCustomerSecurity(): UserSecurity = UserSecurity(
    id = RS_CUSTOMER_ID,
    username = "jane_doe",
    password = "hashed_passWord123",
    userAuthorities = mutableListOf(SimpleGrantedAuthority("ROLE_CUSTOMER"))
)

private fun rsVendorSecurity(): UserSecurity = UserSecurity(
    id = RS_VENDOR_ID,
    username = "sabi_chef",
    password = "hashed_sabiChef98",
    userAuthorities = mutableListOf(SimpleGrantedAuthority("ROLE_VENDOR"))
)

private fun rsOrder(
    customer: Customer = rsCustomer(),
    vendor: Vendor = rsVendor()
): Order = Order().apply {
    id = RS_ORDER_ID
    this.customer = customer
    this.vendor = vendor
    orderStatus = OrderStatus.CONFIRMED
    orderType = OrderType.DINE_IN
}

private fun rsBeverage(): Beverage = Beverage().apply {
    id = RS_BEVERAGE_ID
    name = "Mango Juice"
    vendor = rsVendor()
}

private fun rsDessert(): Dessert = Dessert().apply {
    id = RS_DESSERT_ID
    name = "Chocolate Cake"
    vendor = rsVendor()
}

private fun rsFood(): Food = Food().apply {
    id = RS_FOOD_ID
    name = "Jollof Rice"
    vendor = rsVendor()
}

private fun rsHandheld(): Handheld = Handheld().apply {
    id = RS_HANDHELD_ID
    name = "Chicken Burger"
    vendor = rsVendor()
}

private fun rsBeverageOrderItem(order: Order = rsOrder()): BeverageOrderItem = BeverageOrderItem().apply {
    id = UUID.randomUUID()
    beverage = rsBeverage()
    beverageFlavour = BeverageFlavour().apply { id = UUID.randomUUID(); name = "Mango" }
    beverageFlavourSize = BeverageFlavourSize().apply { id = UUID.randomUUID(); name = "Large"; size = Size.LARGE }
    this.order = order
    vendor = rsVendor()
    quantity = 1
    totalAmount = 2000L
    orderType = OrderType.DINE_IN
}

private fun rsDessertOrderItem(order: Order = rsOrder()): DessertOrderItem = DessertOrderItem().apply {
    id = UUID.randomUUID()
    dessert = rsDessert()
    dessertFlavour = DessertFlavour().apply { id = UUID.randomUUID(); name = "Chocolate" }
    dessertFlavourSize = DessertFlavourSize().apply { id = UUID.randomUUID(); name = "Slice"; size = Size.MEDIUM }
    this.order = order
    vendor = rsVendor()
    quantity = 1
    totalAmount = 1500L
    orderType = OrderType.DINE_IN
}

private fun rsFoodOrderItem(order: Order = rsOrder()): FoodOrderItem = FoodOrderItem().apply {
    id = UUID.randomUUID()
    food = rsFood()
    complement = Complement().apply { id = UUID.randomUUID(); name = "name"; price = 200 }
    size = FoodSize().apply { id = UUID.randomUUID(); name = "Regular"; size = Size.MEDIUM; priceIncrease = 0L }
    this.order = order
    vendor = rsVendor()
    quantity = 1
    totalAmount = 3000L
    orderType = OrderType.DINE_IN
}

private fun rsHandheldOrderItem(order: Order = rsOrder()): HandheldOrderItem = HandheldOrderItem().apply {
    id = UUID.randomUUID()
    handheld = rsHandheld()
    size = HandheldSize().apply { id = UUID.randomUUID(); name = "Regular"; size = Size.MEDIUM; numberOfFillings = 2L }
    this.order = order
    vendor = rsVendor()
    quantity = 1
    totalAmount = 2500L
    orderType = OrderType.DINE_IN
}

private fun rsBeverageReview(customer: Customer = rsCustomer()): BeverageReview = BeverageReview().apply {
    id = RS_REVIEW_ID
    beverage = rsBeverage()
    beverageOrderItem = rsBeverageOrderItem()
    order = rsOrder(customer = customer)
    this.customer = customer
    review = "Great juice!"
    rating = 4.5f
}

private fun rsDessertReview(customer: Customer = rsCustomer()): DessertReview = DessertReview().apply {
    id = RS_REVIEW_ID
    dessert = rsDessert()
    dessertOrderItem = rsDessertOrderItem()
    order = rsOrder(customer = customer)
    this.customer = customer
    review = "Delicious cake!"
    rating = 5.0f
}

private fun rsFoodReview(customer: Customer = rsCustomer()): FoodReview = FoodReview().apply {
    id = RS_REVIEW_ID
    food = rsFood()
    foodOrderItem = rsFoodOrderItem()
    order = rsOrder(customer = customer)
    this.customer = customer
    review = "Best jollof!"
    rating = 4.0f
}

private fun rsHandheldReview(customer: Customer = rsCustomer()): HandheldReview = HandheldReview().apply {
    id = RS_REVIEW_ID
    handheld = rsHandheld()
    handheldOrderItem = rsHandheldOrderItem()
    order = rsOrder(customer = customer)
    this.customer = customer
    review = "Tasty burger!"
    rating = 3.5f
}

private fun rsVendorReview(customer: Customer = rsCustomer()): VendorReview = VendorReview().apply {
    id = RS_REVIEW_ID
    vendor = rsVendor()
    order = rsOrder(customer = customer)
    this.customer = customer
    review = "Great service!"
    rating = 4.0f
}

private fun reviewTypeDto(type: String): ReviewTypeDto = ReviewTypeDto(reviewType = type)

private fun buildOrder(withBeverageItem: Boolean = false, withDessertItem: Boolean = false,
                       withFoodItem: Boolean = false, withHandheldItem: Boolean = false): Order {
    val order = rsOrder()
    if (withBeverageItem) order.beverageOrderItems.add(rsBeverageOrderItem(order))
    if (withDessertItem) order.dessertOrderItems.add(rsDessertOrderItem(order))
    if (withFoodItem) order.foodOrderItems.add(rsFoodOrderItem(order))
    if (withHandheldItem) order.handheldOrderItems.add(rsHandheldOrderItem(order))
    return order
}


class ReviewServiceTest {

    private lateinit var beverageReviewRepository: BeverageReviewRepository
    private lateinit var dessertReviewRepository: DessertReviewRepository
    private lateinit var foodReviewRepository: FoodReviewRepository
    private lateinit var handheldReviewRepository: HandheldReviewRepository
    private lateinit var vendorReviewRepository: VendorReviewRepository
    private lateinit var customerRepository: CustomerRepository
    private lateinit var orderRepository: OrderRepository
    private lateinit var duplicateUtility: DuplicateUtility
    private lateinit var vendorRepository: VendorRepository
    private lateinit var cursorEncoder: CursorEncoder
    private lateinit var reviewService: ReviewService

    @BeforeEach
    fun setUp() {
        beverageReviewRepository = mock()
        dessertReviewRepository = mock()
        foodReviewRepository = mock()
        handheldReviewRepository = mock()
        vendorReviewRepository = mock()
        customerRepository = mock()
        orderRepository = mock()
        duplicateUtility = mock()
        vendorRepository = mock()
        cursorEncoder = mock()

        reviewService = ReviewService(
            cursorEncoder = cursorEncoder,
            beverageReviewRepository = beverageReviewRepository,
            dessertReviewRepository = dessertReviewRepository,
            foodReviewRepository = foodReviewRepository,
            handheldReviewRepository = handheldReviewRepository,
            vendorReviewRepository = vendorReviewRepository,
            customerRepository = customerRepository,
            orderRepository = orderRepository,
            duplicateUtility = duplicateUtility,
            vendorRepository = vendorRepository
        )
    }

    // getBeverageReview
    @Nested
    inner class GetBeverageReview {

        @Test
        fun `returns review when it belongs to the customer`() {
            val review = rsBeverageReview()
            whenever(beverageReviewRepository.findById(RS_REVIEW_ID)).thenReturn(Optional.of(review))

            val result = reviewService.getBeverageReview(RS_REVIEW_ID, rsCustomerSecurity())

            assertEquals(RS_REVIEW_ID, result.id)
        }

        @Test
        fun `throws RequestedEntityNotFoundException when review does not exist`() {
            whenever(beverageReviewRepository.findById(RS_REVIEW_ID)).thenReturn(Optional.empty())

            assertThrows<RequestedEntityNotFoundException> {
                reviewService.getBeverageReview(RS_REVIEW_ID, rsCustomerSecurity())
            }
        }

        @Test
        fun `throws PermissionDeniedException when review belongs to a different customer`() {
            val otherCustomer = Customer().apply { id = UUID.randomUUID(); username = "other" }
            val review = rsBeverageReview(customer = otherCustomer)
            whenever(beverageReviewRepository.findById(RS_REVIEW_ID)).thenReturn(Optional.of(review))

            assertThrows<PermissionDeniedException> {
                reviewService.getBeverageReview(RS_REVIEW_ID, rsCustomerSecurity())
            }
        }
    }

    // getDessertReview
    @Nested
    inner class GetDessertReview {

        @Test
        fun `returns dessert review when it belongs to the customer`() {
            val review = rsDessertReview()
            whenever(dessertReviewRepository.findById(RS_REVIEW_ID)).thenReturn(Optional.of(review))

            val result = reviewService.getDessertReview(RS_REVIEW_ID, rsCustomerSecurity())

            assertEquals(RS_REVIEW_ID, result.id)
            assertEquals("Delicious cake!", result.review)
        }

        @Test
        fun `throws RequestedEntityNotFoundException when dessert review does not exist`() {
            whenever(dessertReviewRepository.findById(RS_REVIEW_ID)).thenReturn(Optional.empty())

            assertThrows<RequestedEntityNotFoundException> {
                reviewService.getDessertReview(RS_REVIEW_ID, rsCustomerSecurity())
            }
        }

        @Test
        fun `throws PermissionDeniedException when dessert review belongs to different customer`() {
            val otherCustomer = Customer().apply { id = UUID.randomUUID(); username = "other" }
            val review = rsDessertReview(customer = otherCustomer)
            whenever(dessertReviewRepository.findById(RS_REVIEW_ID)).thenReturn(Optional.of(review))

            assertThrows<PermissionDeniedException> {
                reviewService.getDessertReview(RS_REVIEW_ID, rsCustomerSecurity())
            }
        }
    }

    // getFoodReview
    @Nested
    inner class GetFoodReview {

        @Test
        fun `returns food review when it belongs to the customer`() {
            val review = rsFoodReview()
            whenever(foodReviewRepository.findById(RS_REVIEW_ID)).thenReturn(Optional.of(review))

            val result = reviewService.getFoodReview(RS_REVIEW_ID, rsCustomerSecurity())

            assertEquals(RS_REVIEW_ID, result.id)
            assertEquals(4.0f, result.rating)
        }

        @Test
        fun `throws RequestedEntityNotFoundException when food review does not exist`() {
            whenever(foodReviewRepository.findById(RS_REVIEW_ID)).thenReturn(Optional.empty())

            assertThrows<RequestedEntityNotFoundException> {
                reviewService.getFoodReview(RS_REVIEW_ID, rsCustomerSecurity())
            }
        }

        @Test
        fun `throws PermissionDeniedException when food review belongs to different customer`() {
            val otherCustomer = Customer().apply { id = UUID.randomUUID(); username = "other" }
            val review = rsFoodReview(customer = otherCustomer)
            whenever(foodReviewRepository.findById(RS_REVIEW_ID)).thenReturn(Optional.of(review))

            assertThrows<PermissionDeniedException> {
                reviewService.getFoodReview(RS_REVIEW_ID, rsCustomerSecurity())
            }
        }
    }

    // getHandheldReview
    @Nested
    inner class GetHandheldReview {

        @Test
        fun `returns handheld review when it belongs to the customer`() {
            val review = rsHandheldReview()
            whenever(handheldReviewRepository.findById(RS_REVIEW_ID)).thenReturn(Optional.of(review))

            val result = reviewService.getHandheldReview(RS_REVIEW_ID, rsCustomerSecurity())

            assertEquals(RS_REVIEW_ID, result.id)
            assertEquals(3.5f, result.rating)
        }

        @Test
        fun `throws RequestedEntityNotFoundException when handheld review does not exist`() {
            whenever(handheldReviewRepository.findById(RS_REVIEW_ID)).thenReturn(Optional.empty())

            assertThrows<RequestedEntityNotFoundException> {
                reviewService.getHandheldReview(RS_REVIEW_ID, rsCustomerSecurity())
            }
        }

        @Test
        fun `throws PermissionDeniedException when handheld review belongs to different customer`() {
            val otherCustomer = Customer().apply { id = UUID.randomUUID(); username = "other" }
            val review = rsHandheldReview(customer = otherCustomer)
            whenever(handheldReviewRepository.findById(RS_REVIEW_ID)).thenReturn(Optional.of(review))

            assertThrows<PermissionDeniedException> {
                reviewService.getHandheldReview(RS_REVIEW_ID, rsCustomerSecurity())
            }
        }
    }

    // getVendorReview
    @Nested
    inner class GetVendorReview {

        @Test
        fun `returns vendor review when it belongs to the customer`() {
            val review = rsVendorReview()
            whenever(vendorReviewRepository.findById(RS_REVIEW_ID)).thenReturn(Optional.of(review))

            val result = reviewService.getVendorReview(RS_REVIEW_ID, rsCustomerSecurity())

            assertEquals(RS_REVIEW_ID, result.id)
            assertEquals(4.0f, result.rating)
        }

        @Test
        fun `throws RequestedEntityNotFoundException when vendor review does not exist`() {
            whenever(vendorReviewRepository.findById(RS_REVIEW_ID)).thenReturn(Optional.empty())

            assertThrows<RequestedEntityNotFoundException> {
                reviewService.getVendorReview(RS_REVIEW_ID, rsCustomerSecurity())
            }
        }

        @Test
        fun `throws PermissionDeniedException when vendor review belongs to different customer`() {
            val otherCustomer = Customer().apply { id = UUID.randomUUID(); username = "other" }
            val review = rsVendorReview(customer = otherCustomer)
            whenever(vendorReviewRepository.findById(RS_REVIEW_ID)).thenReturn(Optional.of(review))

            assertThrows<PermissionDeniedException> {
                reviewService.getVendorReview(RS_REVIEW_ID, rsCustomerSecurity())
            }
        }
    }

    // scrollBeverageReviews
    @Nested
    inner class ScrollBeverageReviews {

        private fun buildWindow(reviews: List<BeverageReview>): Window<BeverageReview> =
            Window.from(reviews, { ScrollPosition.offset() }, false)

        @Test
        fun `returns window of beverage reviews for the given beverage id`() {
            val review = rsBeverageReview()
            val window = buildWindow(listOf(review))
            whenever(beverageReviewRepository.findAllByBeverageId(eq(RS_BEVERAGE_ID), any<ScrollPosition>(), any<Sort>(), any<Limit>()))
                .thenReturn(window)
            whenever(cursorEncoder.encodeOffset(any())).thenReturn("cursor1")

            val result = reviewService.scrollBeverageReviews(RS_BEVERAGE_ID, null, 10)

            assertNotNull(result)
            assertEquals(1, result.content.size)
        }

        @Test
        fun `returns empty window when no reviews exist for the beverage`() {
            val window = buildWindow(emptyList())
            whenever(beverageReviewRepository.findAllByBeverageId(eq(RS_BEVERAGE_ID), any<ScrollPosition>(), any<Sort>(), any<Limit>()))
                .thenReturn(window)
            whenever(cursorEncoder.encodeOffset(any())).thenReturn("cursor0")

            val result = reviewService.scrollBeverageReviews(RS_BEVERAGE_ID, null, 10)

            assertEquals(0, result.content.size)
            assertFalse(result.hasNext)
        }

        @Test
        fun `uses descending sort by createdAt`() {
            val window = buildWindow(emptyList())
            whenever(beverageReviewRepository.findAllByBeverageId(eq(RS_BEVERAGE_ID), any<ScrollPosition>(), any<Sort>(), any<Limit>()))
                .thenReturn(window)
            whenever(cursorEncoder.encodeOffset(any())).thenReturn("cursor0")

            reviewService.scrollBeverageReviews(RS_BEVERAGE_ID, null, 10)

            verify(beverageReviewRepository).findAllByBeverageId(
                eq(RS_BEVERAGE_ID),
                any<ScrollPosition>(),
                argThat<Sort> { getOrderFor("createdAt")?.isDescending == true },
                any<Limit>()
            )
        }

        @Test
        fun `hasNext is false when window does not have a next page`() {
            val window = buildWindow(emptyList())
            whenever(beverageReviewRepository.findAllByBeverageId(eq(RS_BEVERAGE_ID), any<ScrollPosition>(), any<Sort>(), any<Limit>()))
                .thenReturn(window)
            whenever(cursorEncoder.encodeOffset(any())).thenReturn("cursor0")

            val result = reviewService.scrollBeverageReviews(RS_BEVERAGE_ID, null, 10)

            assertFalse(result.hasNext)
        }
    }

    // scrollDessertReviews
    @Nested
    inner class ScrollDessertReviews {

        private fun buildWindow(reviews: List<DessertReview>): Window<DessertReview> =
            Window.from(reviews, { ScrollPosition.offset() }, false)

        @Test
        fun `returns window of dessert reviews`() {
            val review = rsDessertReview()
            val window = buildWindow(listOf(review))
            whenever(dessertReviewRepository.findAllByDessertId(eq(RS_DESSERT_ID), any<ScrollPosition>(), any<Sort>(), any<Limit>()))
                .thenReturn(window)
            whenever(cursorEncoder.encodeOffset(any())).thenReturn("cursor1")

            val result = reviewService.scrollDessertReviews(RS_DESSERT_ID, null, 10)

            assertEquals(1, result.content.size)
        }

        @Test
        fun `returns empty window when no dessert reviews exist`() {
            val window = buildWindow(emptyList())
            whenever(dessertReviewRepository.findAllByDessertId(eq(RS_DESSERT_ID), any<ScrollPosition>(), any<Sort>(), any<Limit>()))
                .thenReturn(window)
            whenever(cursorEncoder.encodeOffset(any())).thenReturn("cursor0")

            val result = reviewService.scrollDessertReviews(RS_DESSERT_ID, null, 10)

            assertEquals(0, result.content.size)
        }
    }

    // scrollFoodReviews
    @Nested
    inner class ScrollFoodReviews {

        private fun buildWindow(reviews: List<FoodReview>): Window<FoodReview> =
            Window.from(reviews, { ScrollPosition.offset() }, false)

        @Test
        fun `returns window of food reviews for the given food id`() {
            val review = rsFoodReview()
            val window = buildWindow(listOf(review))
            whenever(foodReviewRepository.findAllByFoodId(eq(RS_FOOD_ID), any<ScrollPosition>(), any<Sort>(), any<Limit>()))
                .thenReturn(window)
            whenever(cursorEncoder.encodeOffset(any())).thenReturn("cursor1")

            val result = reviewService.scrollFoodReviews(RS_FOOD_ID, null, 10)

            assertEquals(1, result.content.size)
        }

        @Test
        fun `returns empty window when no food reviews exist`() {
            val window = buildWindow(emptyList())
            whenever(foodReviewRepository.findAllByFoodId(eq(RS_FOOD_ID), any<ScrollPosition>(), any<Sort>(), any<Limit>()))
                .thenReturn(window)
            whenever(cursorEncoder.encodeOffset(any())).thenReturn("cursor0")

            val result = reviewService.scrollFoodReviews(RS_FOOD_ID, null, 10)

            assertEquals(0, result.content.size)
            assertFalse(result.hasNext)
        }
    }

    // scrollHandheldReviews
    @Nested
    inner class ScrollHandheldReviews {

        private fun buildWindow(reviews: List<HandheldReview>): Window<HandheldReview> =
            Window.from(reviews, { ScrollPosition.offset() }, false)

        @Test
        fun `returns window of handheld reviews`() {
            val review = rsHandheldReview()
            val window = buildWindow(listOf(review))
            whenever(handheldReviewRepository.findAllByHandheldId(eq(RS_HANDHELD_ID), any<ScrollPosition>(), any<Sort>(), any<Limit>()))
                .thenReturn(window)
            whenever(cursorEncoder.encodeOffset(any())).thenReturn("cursor1")

            val result = reviewService.scrollHandheldReviews(RS_HANDHELD_ID, null, 10)

            assertEquals(1, result.content.size)
        }

        @Test
        fun `returns empty window when no handheld reviews exist`() {
            val window = buildWindow(emptyList())
            whenever(handheldReviewRepository.findAllByHandheldId(eq(RS_HANDHELD_ID), any<ScrollPosition>(), any<Sort>(), any<Limit>()))
                .thenReturn(window)
            whenever(cursorEncoder.encodeOffset(any())).thenReturn("cursor0")

            val result = reviewService.scrollHandheldReviews(RS_HANDHELD_ID, null, 10)

            assertEquals(0, result.content.size)
        }
    }

    // scrollVendorReviews
    @Nested
    inner class ScrollVendorReviews {

        private fun buildWindow(reviews: List<VendorReview>): Window<VendorReview> =
            Window.from(reviews, { ScrollPosition.offset() }, false)

        @Test
        fun `returns window of vendor reviews`() {
            val review = rsVendorReview()
            val window = buildWindow(listOf(review))
            whenever(vendorReviewRepository.findAllByVendorId(eq(RS_VENDOR_ID), any<ScrollPosition>(), any<Sort>(), any<Limit>()))
                .thenReturn(window)
            whenever(cursorEncoder.encodeOffset(any())).thenReturn("cursor1")

            val result = reviewService.scrollVendorReviews(RS_VENDOR_ID, null, 10)

            assertEquals(1, result.content.size)
        }

        @Test
        fun `returns empty window when no vendor reviews exist`() {
            val window = buildWindow(emptyList())
            whenever(vendorReviewRepository.findAllByVendorId(eq(RS_VENDOR_ID), any<ScrollPosition>(), any<Sort>(), any<Limit>()))
                .thenReturn(window)
            whenever(cursorEncoder.encodeOffset(any())).thenReturn("cursor0")

            val result = reviewService.scrollVendorReviews(RS_VENDOR_ID, null, 10)

            assertEquals(0, result.content.size)
        }
    }

    // getAllItemReviews — dispatch
    @Nested
    inner class GetAllItemReviews {

        @Test
        fun `dispatches to beverageReviewRepository for BEVERAGE type`() {
            whenever(beverageReviewRepository.findAllByBeverageId(eq(RS_BEVERAGE_ID), any<Pageable>()))
                .thenReturn(PageImpl(emptyList()))

            reviewService.getAllItemReviews(RS_BEVERAGE_ID, reviewTypeDto("BEVERAGE"), 0, 10)

            verify(beverageReviewRepository).findAllByBeverageId(eq(RS_BEVERAGE_ID), any<Pageable>())
        }

        @Test
        fun `dispatches to dessertReviewRepository for DESSERT type`() {
            whenever(dessertReviewRepository.findAllByDessertId(eq(RS_DESSERT_ID), any<Pageable>()))
                .thenReturn(PageImpl(emptyList()))

            reviewService.getAllItemReviews(RS_DESSERT_ID, reviewTypeDto("DESSERT"), 0, 10)

            verify(dessertReviewRepository).findAllByDessertId(eq(RS_DESSERT_ID), any<Pageable>())
        }

        @Test
        fun `dispatches to foodReviewRepository for FOOD type`() {
            whenever(foodReviewRepository.findAllByFoodId(eq(RS_FOOD_ID), any<Pageable>()))
                .thenReturn(PageImpl(emptyList()))

            reviewService.getAllItemReviews(RS_FOOD_ID, reviewTypeDto("FOOD"), 0, 10)

            verify(foodReviewRepository).findAllByFoodId(eq(RS_FOOD_ID), any<Pageable>())
        }

        @Test
        fun `dispatches to handheldReviewRepository for HANDHELD type`() {
            whenever(handheldReviewRepository.findAllByHandheldId(eq(RS_HANDHELD_ID), any<Pageable>()))
                .thenReturn(PageImpl(emptyList()))

            reviewService.getAllItemReviews(RS_HANDHELD_ID, reviewTypeDto("HANDHELD"), 0, 10)

            verify(handheldReviewRepository).findAllByHandheldId(eq(RS_HANDHELD_ID), any<Pageable>())
        }

        @Test
        fun `dispatches to vendorReviewRepository for VENDOR type`() {
            whenever(vendorReviewRepository.findAllByVendorId(eq(RS_VENDOR_ID), any<Pageable>()))
                .thenReturn(PageImpl(emptyList()))

            reviewService.getAllItemReviews(RS_VENDOR_ID, reviewTypeDto("VENDOR"), 0, 10)

            verify(vendorReviewRepository).findAllByVendorId(eq(RS_VENDOR_ID), any<Pageable>())
        }

        @Test
        fun `throws IllegalArgumentException for an invalid review type`() {
            assertThrows<IllegalArgumentException> {
                reviewService.getAllItemReviews(RS_FOOD_ID, reviewTypeDto("INVALID"), 0, 10)
            }
        }

        @Test
        fun `returns empty page when no reviews exist`() {
            whenever(foodReviewRepository.findAllByFoodId(eq(RS_FOOD_ID), any<Pageable>()))
                .thenReturn(PageImpl(emptyList()))

            val result = reviewService.getAllItemReviews(RS_FOOD_ID, reviewTypeDto("FOOD"), 0, 10)

            assertEquals(0, result.totalElements)
        }
    }

    // getReview — dispatch
    @Nested
    inner class GetReviewDispatch {

        @Test
        fun `dispatches to getBeverageReview for BEVERAGE type`() {
            val review = rsBeverageReview()
            whenever(beverageReviewRepository.findById(RS_REVIEW_ID)).thenReturn(Optional.of(review))

            val result = reviewService.getReview(RS_REVIEW_ID, reviewTypeDto("BEVERAGE"), rsCustomerSecurity())

            assertInstanceOf(BeverageReview::class.java, result)
        }

        @Test
        fun `dispatches to getDessertReview for DESSERT type`() {
            val review = rsDessertReview()
            whenever(dessertReviewRepository.findById(RS_REVIEW_ID)).thenReturn(Optional.of(review))

            val result = reviewService.getReview(RS_REVIEW_ID, reviewTypeDto("DESSERT"), rsCustomerSecurity())

            assertInstanceOf(DessertReview::class.java, result)
        }

        @Test
        fun `dispatches to getFoodReview for FOOD type`() {
            val review = rsFoodReview()
            whenever(foodReviewRepository.findById(RS_REVIEW_ID)).thenReturn(Optional.of(review))

            val result = reviewService.getReview(RS_REVIEW_ID, reviewTypeDto("FOOD"), rsCustomerSecurity())

            assertInstanceOf(FoodReview::class.java, result)
        }

        @Test
        fun `dispatches to getHandheldReview for HANDHELD type`() {
            val review = rsHandheldReview()
            whenever(handheldReviewRepository.findById(RS_REVIEW_ID)).thenReturn(Optional.of(review))

            val result = reviewService.getReview(RS_REVIEW_ID, reviewTypeDto("HANDHELD"), rsCustomerSecurity())

            assertInstanceOf(HandheldReview::class.java, result)
        }

        @Test
        fun `dispatches to getVendorReview for VENDOR type`() {
            val review = rsVendorReview()
            whenever(vendorReviewRepository.findById(RS_REVIEW_ID)).thenReturn(Optional.of(review))

            val result = reviewService.getReview(RS_REVIEW_ID, reviewTypeDto("VENDOR"), rsCustomerSecurity())

            assertInstanceOf(VendorReview::class.java, result)
        }
    }

    // addOrUpdateReview — guard logic
    @Nested
    inner class AddOrUpdateReviewGuards {

        @Test
        fun `throws UserNotFoundException when customer does not exist`() {
            whenever(customerRepository.findById(RS_CUSTOMER_ID)).thenReturn(Optional.empty())

            val dto = ReviewDto(
                id = null, review = "Nice", rating = 4.0f, orderId = RS_ORDER_ID,
                beverageReviewDto = BeverageReviewDto(beverageId = RS_BEVERAGE_ID),
                dessertReviewDto = null, foodReviewDto = null, handheldReviewDto = null, vendorReviewDto = null
            )

            assertThrows<UserNotFoundException> {
                reviewService.addOrUpdateReview(dto, rsCustomerSecurity())
            }
        }

        @Test
        fun `throws RequestedEntityNotFoundException when order does not exist`() {
            whenever(customerRepository.findById(RS_CUSTOMER_ID)).thenReturn(Optional.of(rsCustomer()))
            whenever(orderRepository.findById(RS_ORDER_ID)).thenReturn(Optional.empty())

            val dto = ReviewDto(
                id = null, review = "Nice", rating = 4.0f, orderId = RS_ORDER_ID,
                beverageReviewDto = BeverageReviewDto(beverageId = RS_BEVERAGE_ID),
                dessertReviewDto = null, foodReviewDto = null, handheldReviewDto = null, vendorReviewDto = null
            )

            assertThrows<RequestedEntityNotFoundException> {
                reviewService.addOrUpdateReview(dto, rsCustomerSecurity())
            }
        }

        @Test
        fun `throws PermissionDeniedException when order belongs to a different customer`() {
            val otherCustomer = Customer().apply { id = UUID.randomUUID(); username = "other" }
            val order = rsOrder(customer = otherCustomer)
            whenever(customerRepository.findById(RS_CUSTOMER_ID)).thenReturn(Optional.of(rsCustomer()))
            whenever(orderRepository.findById(RS_ORDER_ID)).thenReturn(Optional.of(order))

            val dto = ReviewDto(
                id = null, review = "Nice", rating = 4.0f, orderId = RS_ORDER_ID,
                beverageReviewDto = BeverageReviewDto(beverageId = RS_BEVERAGE_ID),
                dessertReviewDto = null, foodReviewDto = null, handheldReviewDto = null, vendorReviewDto = null
            )

            assertThrows<PermissionDeniedException> {
                reviewService.addOrUpdateReview(dto, rsCustomerSecurity())
            }
        }

        @Test
        fun `throws IllegalArgumentException when no review type sub-dto is provided`() {
            whenever(customerRepository.findById(RS_CUSTOMER_ID)).thenReturn(Optional.of(rsCustomer()))
            whenever(orderRepository.findById(RS_ORDER_ID)).thenReturn(Optional.of(rsOrder()))

            val dto = ReviewDto(
                id = null, review = "Nice", rating = 4.0f, orderId = RS_ORDER_ID,
                beverageReviewDto = null, dessertReviewDto = null,
                foodReviewDto = null, handheldReviewDto = null, vendorReviewDto = null
            )

            assertThrows<IllegalArgumentException> {
                reviewService.addOrUpdateReview(dto, rsCustomerSecurity())
            }
        }
    }

    // addOrUpdateReview — Beverage
    @Nested
    inner class AddOrUpdateBeverageReview {

        @BeforeEach
        fun stubBase() {
            whenever(customerRepository.findById(RS_CUSTOMER_ID)).thenReturn(Optional.of(rsCustomer()))
        }

        @Test
        fun `creates beverage review when no existing review and beverage item is in the order`() {
            val order = buildOrder(withBeverageItem = true)
            whenever(orderRepository.findById(RS_ORDER_ID)).thenReturn(Optional.of(order))
            whenever(duplicateUtility.isExistingReviewFound(RS_BEVERAGE_ID, RS_CUSTOMER_ID, RS_ORDER_ID, ReviewType.BEVERAGE))
                .thenReturn(false)
            val savedReview = rsBeverageReview()
            whenever(beverageReviewRepository.saveAndFlush(any())).thenReturn(savedReview)

            val dto = ReviewDto(
                id = null, review = "Great juice!", rating = 4.5f, orderId = RS_ORDER_ID,
                beverageReviewDto = BeverageReviewDto(beverageId = RS_BEVERAGE_ID),
                dessertReviewDto = null, foodReviewDto = null, handheldReviewDto = null, vendorReviewDto = null
            )

            val result = reviewService.addOrUpdateReview(dto, rsCustomerSecurity())

            verify(beverageReviewRepository).saveAndFlush(any())
            assertInstanceOf(BeverageReview::class.java, result)
        }

        @Test
        fun `throws IllegalArgumentException when beverage review already exists for the order`() {
            val order = buildOrder(withBeverageItem = true)
            whenever(orderRepository.findById(RS_ORDER_ID)).thenReturn(Optional.of(order))
            whenever(duplicateUtility.isExistingReviewFound(RS_BEVERAGE_ID, RS_CUSTOMER_ID, RS_ORDER_ID, ReviewType.BEVERAGE))
                .thenReturn(true)

            val dto = ReviewDto(
                id = null, review = "Duplicate!", rating = 3.0f, orderId = RS_ORDER_ID,
                beverageReviewDto = BeverageReviewDto(beverageId = RS_BEVERAGE_ID),
                dessertReviewDto = null, foodReviewDto = null, handheldReviewDto = null, vendorReviewDto = null
            )

            assertThrows<IllegalArgumentException> {
                reviewService.addOrUpdateReview(dto, rsCustomerSecurity())
            }
        }

        @Test
        fun `does not save review when beverage item is not in the order`() {
            val order = rsOrder()
            whenever(orderRepository.findById(RS_ORDER_ID)).thenReturn(Optional.of(order))
            whenever(duplicateUtility.isExistingReviewFound(RS_BEVERAGE_ID, RS_CUSTOMER_ID, RS_ORDER_ID, ReviewType.BEVERAGE))
                .thenReturn(false)

            val dto = ReviewDto(
                id = null, review = "Should fail", rating = 3.0f, orderId = RS_ORDER_ID,
                beverageReviewDto = BeverageReviewDto(beverageId = RS_BEVERAGE_ID),
                dessertReviewDto = null, foodReviewDto = null, handheldReviewDto = null, vendorReviewDto = null
            )

            assertThrows<RequestedEntityNotFoundException> {
                reviewService.addOrUpdateReview(dto, rsCustomerSecurity())
            }

            verify(beverageReviewRepository, never()).saveAndFlush(any())
        }

        @Test
        fun `updates existing beverage review when id is provided and ownership matches`() {
            val order = buildOrder(withBeverageItem = true)
            val existingReview = rsBeverageReview()
            whenever(orderRepository.findById(RS_ORDER_ID)).thenReturn(Optional.of(order))
            whenever(duplicateUtility.isExistingReviewFound(RS_BEVERAGE_ID, RS_CUSTOMER_ID, RS_ORDER_ID, ReviewType.BEVERAGE))
                .thenReturn(false)
            whenever(beverageReviewRepository.findById(RS_REVIEW_ID)).thenReturn(Optional.of(existingReview))
            whenever(beverageReviewRepository.saveAndFlush(any())).thenReturn(existingReview)

            val dto = ReviewDto(
                id = RS_REVIEW_ID, review = "Updated review", rating = 5.0f, orderId = RS_ORDER_ID,
                beverageReviewDto = BeverageReviewDto(beverageId = RS_BEVERAGE_ID),
                dessertReviewDto = null, foodReviewDto = null, handheldReviewDto = null, vendorReviewDto = null
            )

            reviewService.addOrUpdateReview(dto, rsCustomerSecurity())

            verify(beverageReviewRepository).saveAndFlush(argThat {
                review == "Updated review" && rating == 5.0f
            })
        }

        @Test
        fun `throws PermissionDeniedException when updating a review owned by another customer`() {
            val otherCustomer = Customer().apply { id = UUID.randomUUID(); username = "other" }
            val order = buildOrder(withBeverageItem = true)
            val foreignReview = rsBeverageReview(customer = otherCustomer)
            whenever(orderRepository.findById(RS_ORDER_ID)).thenReturn(Optional.of(order))
            whenever(duplicateUtility.isExistingReviewFound(RS_BEVERAGE_ID, RS_CUSTOMER_ID, RS_ORDER_ID, ReviewType.BEVERAGE))
                .thenReturn(false)
            whenever(beverageReviewRepository.findById(RS_REVIEW_ID)).thenReturn(Optional.of(foreignReview))

            val dto = ReviewDto(
                id = RS_REVIEW_ID, review = "Hacked review", rating = 1.0f, orderId = RS_ORDER_ID,
                beverageReviewDto = BeverageReviewDto(beverageId = RS_BEVERAGE_ID),
                dessertReviewDto = null, foodReviewDto = null, handheldReviewDto = null, vendorReviewDto = null
            )

            assertThrows<PermissionDeniedException> {
                reviewService.addOrUpdateReview(dto, rsCustomerSecurity())
            }
        }

        @Test
        fun `throws RequestedEntityNotFoundException when updating a beverage review that does not exist`() {
            val order = buildOrder(withBeverageItem = true)
            whenever(orderRepository.findById(RS_ORDER_ID)).thenReturn(Optional.of(order))
            whenever(duplicateUtility.isExistingReviewFound(RS_BEVERAGE_ID, RS_CUSTOMER_ID, RS_ORDER_ID, ReviewType.BEVERAGE))
                .thenReturn(false)
            whenever(beverageReviewRepository.findById(RS_REVIEW_ID)).thenReturn(Optional.empty())

            val dto = ReviewDto(
                id = RS_REVIEW_ID, review = "Ghost review", rating = 3.0f, orderId = RS_ORDER_ID,
                beverageReviewDto = BeverageReviewDto(beverageId = RS_BEVERAGE_ID),
                dessertReviewDto = null, foodReviewDto = null, handheldReviewDto = null, vendorReviewDto = null
            )

            assertThrows<RequestedEntityNotFoundException> {
                reviewService.addOrUpdateReview(dto, rsCustomerSecurity())
            }
        }
    }

    // addOrUpdateReview — Dessert
    @Nested
    inner class AddOrUpdateDessertReview {

        @BeforeEach
        fun stubBase() {
            whenever(customerRepository.findById(RS_CUSTOMER_ID)).thenReturn(Optional.of(rsCustomer()))
        }

        @Test
        fun `creates dessert review when dessert item is in the order`() {
            val order = buildOrder(withDessertItem = true)
            whenever(orderRepository.findById(RS_ORDER_ID)).thenReturn(Optional.of(order))
            whenever(duplicateUtility.isExistingReviewFound(RS_DESSERT_ID, RS_CUSTOMER_ID, RS_ORDER_ID, ReviewType.DESSERT))
                .thenReturn(false)
            val savedReview = rsDessertReview()
            whenever(dessertReviewRepository.saveAndFlush(any())).thenReturn(savedReview)

            val dto = ReviewDto(
                id = null, review = "Delicious!", rating = 5.0f, orderId = RS_ORDER_ID,
                beverageReviewDto = null,
                dessertReviewDto = DessertReviewDto(dessertId = RS_DESSERT_ID),
                foodReviewDto = null, handheldReviewDto = null, vendorReviewDto = null
            )

            val result = reviewService.addOrUpdateReview(dto, rsCustomerSecurity())

            verify(dessertReviewRepository).saveAndFlush(any())
            assertInstanceOf(DessertReview::class.java, result)
        }

        @Test
        fun `throws IllegalArgumentException when dessert review already exists for the order`() {
            val order = buildOrder(withDessertItem = true)
            whenever(orderRepository.findById(RS_ORDER_ID)).thenReturn(Optional.of(order))
            whenever(duplicateUtility.isExistingReviewFound(RS_DESSERT_ID, RS_CUSTOMER_ID, RS_ORDER_ID, ReviewType.DESSERT))
                .thenReturn(true)

            val dto = ReviewDto(
                id = null, review = "Duplicate!", rating = 3.0f, orderId = RS_ORDER_ID,
                beverageReviewDto = null,
                dessertReviewDto = DessertReviewDto(dessertId = RS_DESSERT_ID),
                foodReviewDto = null, handheldReviewDto = null, vendorReviewDto = null
            )

            assertThrows<IllegalArgumentException> {
                reviewService.addOrUpdateReview(dto, rsCustomerSecurity())
            }
        }

        @Test
        fun `throws RequestedEntityNotFoundException when dessert item is not in the order`() {
            val order = rsOrder()
            whenever(orderRepository.findById(RS_ORDER_ID)).thenReturn(Optional.of(order))
            whenever(duplicateUtility.isExistingReviewFound(RS_DESSERT_ID, RS_CUSTOMER_ID, RS_ORDER_ID, ReviewType.DESSERT))
                .thenReturn(false)

            val dto = ReviewDto(
                id = null, review = "Not there", rating = 2.0f, orderId = RS_ORDER_ID,
                beverageReviewDto = null,
                dessertReviewDto = DessertReviewDto(dessertId = RS_DESSERT_ID),
                foodReviewDto = null, handheldReviewDto = null, vendorReviewDto = null
            )

            assertThrows<RequestedEntityNotFoundException> {
                reviewService.addOrUpdateReview(dto, rsCustomerSecurity())
            }
        }
    }

    // addOrUpdateReview — Food
    @Nested
    inner class AddOrUpdateFoodReview {

        @BeforeEach
        fun stubBase() {
            whenever(customerRepository.findById(RS_CUSTOMER_ID)).thenReturn(Optional.of(rsCustomer()))
        }

        @Test
        fun `creates food review when food item is in the order`() {
            val order = buildOrder(withFoodItem = true)
            whenever(orderRepository.findById(RS_ORDER_ID)).thenReturn(Optional.of(order))
            whenever(duplicateUtility.isExistingReviewFound(RS_FOOD_ID, RS_CUSTOMER_ID, RS_ORDER_ID, ReviewType.FOOD))
                .thenReturn(false)
            val savedReview = rsFoodReview()
            whenever(foodReviewRepository.saveAndFlush(any())).thenReturn(savedReview)

            val dto = ReviewDto(
                id = null, review = "Best jollof!", rating = 4.0f, orderId = RS_ORDER_ID,
                beverageReviewDto = null, dessertReviewDto = null,
                foodReviewDto = FoodReviewDto(foodId = RS_FOOD_ID),
                handheldReviewDto = null, vendorReviewDto = null
            )

            val result = reviewService.addOrUpdateReview(dto, rsCustomerSecurity())

            verify(foodReviewRepository).saveAndFlush(any())
            assertInstanceOf(FoodReview::class.java, result)
        }

        @Test
        fun `throws IllegalArgumentException when food review already exists for the order`() {
            val order = buildOrder(withFoodItem = true)
            whenever(orderRepository.findById(RS_ORDER_ID)).thenReturn(Optional.of(order))
            whenever(duplicateUtility.isExistingReviewFound(RS_FOOD_ID, RS_CUSTOMER_ID, RS_ORDER_ID, ReviewType.FOOD))
                .thenReturn(true)

            val dto = ReviewDto(
                id = null, review = "Dupe!", rating = 3.0f, orderId = RS_ORDER_ID,
                beverageReviewDto = null, dessertReviewDto = null,
                foodReviewDto = FoodReviewDto(foodId = RS_FOOD_ID),
                handheldReviewDto = null, vendorReviewDto = null
            )

            assertThrows<IllegalArgumentException> {
                reviewService.addOrUpdateReview(dto, rsCustomerSecurity())
            }
        }

        @Test
        fun `throws RequestedEntityNotFoundException when food item is not in the order`() {
            val order = rsOrder()
            whenever(orderRepository.findById(RS_ORDER_ID)).thenReturn(Optional.of(order))
            whenever(duplicateUtility.isExistingReviewFound(RS_FOOD_ID, RS_CUSTOMER_ID, RS_ORDER_ID, ReviewType.FOOD))
                .thenReturn(false)

            val dto = ReviewDto(
                id = null, review = "Not in order", rating = 3.0f, orderId = RS_ORDER_ID,
                beverageReviewDto = null, dessertReviewDto = null,
                foodReviewDto = FoodReviewDto(foodId = RS_FOOD_ID),
                handheldReviewDto = null, vendorReviewDto = null
            )

            assertThrows<RequestedEntityNotFoundException> {
                reviewService.addOrUpdateReview(dto, rsCustomerSecurity())
            }
        }

        @Test
        fun `updates food review text and rating when id is provided`() {
            val order = buildOrder(withFoodItem = true)
            val existingReview = rsFoodReview()
            whenever(orderRepository.findById(RS_ORDER_ID)).thenReturn(Optional.of(order))
            whenever(duplicateUtility.isExistingReviewFound(RS_FOOD_ID, RS_CUSTOMER_ID, RS_ORDER_ID, ReviewType.FOOD))
                .thenReturn(false)
            whenever(foodReviewRepository.findById(RS_REVIEW_ID)).thenReturn(Optional.of(existingReview))
            whenever(foodReviewRepository.saveAndFlush(any())).thenReturn(existingReview)

            val dto = ReviewDto(
                id = RS_REVIEW_ID, review = "Even better now", rating = 5.0f, orderId = RS_ORDER_ID,
                beverageReviewDto = null, dessertReviewDto = null,
                foodReviewDto = FoodReviewDto(foodId = RS_FOOD_ID),
                handheldReviewDto = null, vendorReviewDto = null
            )

            reviewService.addOrUpdateReview(dto, rsCustomerSecurity())

            verify(foodReviewRepository).saveAndFlush(argThat {
                review == "Even better now" && rating == 5.0f
            })
        }
    }

    // addOrUpdateReview — Handheld
    @Nested
    inner class AddOrUpdateHandheldReview {

        @BeforeEach
        fun stubBase() {
            whenever(customerRepository.findById(RS_CUSTOMER_ID)).thenReturn(Optional.of(rsCustomer()))
        }

        @Test
        fun `creates handheld review when handheld item is in the order`() {
            val order = buildOrder(withHandheldItem = true)
            whenever(orderRepository.findById(RS_ORDER_ID)).thenReturn(Optional.of(order))
            whenever(duplicateUtility.isExistingReviewFound(RS_HANDHELD_ID, RS_CUSTOMER_ID, RS_ORDER_ID, ReviewType.HANDHELD))
                .thenReturn(false)
            val savedReview = rsHandheldReview()
            whenever(handheldReviewRepository.saveAndFlush(any())).thenReturn(savedReview)

            val dto = ReviewDto(
                id = null, review = "Tasty burger!", rating = 3.5f, orderId = RS_ORDER_ID,
                beverageReviewDto = null, dessertReviewDto = null, foodReviewDto = null,
                handheldReviewDto = HandheldReviewDto(handheldId = RS_HANDHELD_ID),
                vendorReviewDto = null
            )

            val result = reviewService.addOrUpdateReview(dto, rsCustomerSecurity())

            verify(handheldReviewRepository).saveAndFlush(any())
            assertInstanceOf(HandheldReview::class.java, result)
        }

        @Test
        fun `throws IllegalArgumentException when handheld review already exists for the order`() {
            val order = buildOrder(withHandheldItem = true)
            whenever(orderRepository.findById(RS_ORDER_ID)).thenReturn(Optional.of(order))
            whenever(duplicateUtility.isExistingReviewFound(RS_HANDHELD_ID, RS_CUSTOMER_ID, RS_ORDER_ID, ReviewType.HANDHELD))
                .thenReturn(true)

            val dto = ReviewDto(
                id = null, review = "Dupe!", rating = 3.0f, orderId = RS_ORDER_ID,
                beverageReviewDto = null, dessertReviewDto = null, foodReviewDto = null,
                handheldReviewDto = HandheldReviewDto(handheldId = RS_HANDHELD_ID),
                vendorReviewDto = null
            )

            assertThrows<IllegalArgumentException> {
                reviewService.addOrUpdateReview(dto, rsCustomerSecurity())
            }
        }

        @Test
        fun `throws RequestedEntityNotFoundException when handheld item is not in the order`() {
            val order = rsOrder()
            whenever(orderRepository.findById(RS_ORDER_ID)).thenReturn(Optional.of(order))
            whenever(duplicateUtility.isExistingReviewFound(RS_HANDHELD_ID, RS_CUSTOMER_ID, RS_ORDER_ID, ReviewType.HANDHELD))
                .thenReturn(false)

            val dto = ReviewDto(
                id = null, review = "Not in order", rating = 3.0f, orderId = RS_ORDER_ID,
                beverageReviewDto = null, dessertReviewDto = null, foodReviewDto = null,
                handheldReviewDto = HandheldReviewDto(handheldId = RS_HANDHELD_ID),
                vendorReviewDto = null
            )

            assertThrows<RequestedEntityNotFoundException> {
                reviewService.addOrUpdateReview(dto, rsCustomerSecurity())
            }
        }
    }

    // addOrUpdateReview — Vendor
    @Nested
    inner class AddOrUpdateVendorReview {

        @BeforeEach
        fun stubBase() {
            whenever(customerRepository.findById(RS_CUSTOMER_ID)).thenReturn(Optional.of(rsCustomer()))
        }

        @Test
        fun `creates vendor review when vendor matches the order vendor`() {
            val order = rsOrder()
            whenever(orderRepository.findById(RS_ORDER_ID)).thenReturn(Optional.of(order))
            whenever(duplicateUtility.isExistingReviewFound(RS_VENDOR_ID, RS_CUSTOMER_ID, RS_ORDER_ID, ReviewType.VENDOR))
                .thenReturn(false)
            whenever(vendorRepository.findById(RS_VENDOR_ID)).thenReturn(Optional.of(rsVendor()))
            val savedReview = rsVendorReview()
            whenever(vendorReviewRepository.saveAndFlush(any())).thenReturn(savedReview)

            val dto = ReviewDto(
                id = null, review = "Great service!", rating = 4.0f, orderId = RS_ORDER_ID,
                beverageReviewDto = null, dessertReviewDto = null, foodReviewDto = null,
                handheldReviewDto = null,
                vendorReviewDto = VendorReviewDto(vendorId = RS_VENDOR_ID)
            )

            val result = reviewService.addOrUpdateReview(dto, rsCustomerSecurity())

            verify(vendorReviewRepository).saveAndFlush(any())
            assertInstanceOf(VendorReview::class.java, result)
        }

        @Test
        fun `throws IllegalArgumentException when vendor review already exists for the order`() {
            val order = rsOrder()
            whenever(orderRepository.findById(RS_ORDER_ID)).thenReturn(Optional.of(order))
            whenever(duplicateUtility.isExistingReviewFound(RS_VENDOR_ID, RS_CUSTOMER_ID, RS_ORDER_ID, ReviewType.VENDOR))
                .thenReturn(true)

            val dto = ReviewDto(
                id = null, review = "Duplicate review", rating = 3.0f, orderId = RS_ORDER_ID,
                beverageReviewDto = null, dessertReviewDto = null, foodReviewDto = null,
                handheldReviewDto = null,
                vendorReviewDto = VendorReviewDto(vendorId = RS_VENDOR_ID)
            )

            assertThrows<IllegalArgumentException> {
                reviewService.addOrUpdateReview(dto, rsCustomerSecurity())
            }
        }

        @Test
        fun `throws PermissionDeniedException when vendor id does not match the order vendor`() {
            val differentVendorId = UUID.randomUUID()
            val order = rsOrder()
            whenever(orderRepository.findById(RS_ORDER_ID)).thenReturn(Optional.of(order))
            whenever(duplicateUtility.isExistingReviewFound(differentVendorId, RS_CUSTOMER_ID, RS_ORDER_ID, ReviewType.VENDOR))
                .thenReturn(false)

            val dto = ReviewDto(
                id = null, review = "Wrong vendor", rating = 2.0f, orderId = RS_ORDER_ID,
                beverageReviewDto = null, dessertReviewDto = null, foodReviewDto = null,
                handheldReviewDto = null,
                vendorReviewDto = VendorReviewDto(vendorId = differentVendorId)
            )

            assertThrows<PermissionDeniedException> {
                reviewService.addOrUpdateReview(dto, rsCustomerSecurity())
            }
        }

        @Test
        fun `throws RequestedEntityNotFoundException when vendor does not exist`() {
            val order = rsOrder()
            whenever(orderRepository.findById(RS_ORDER_ID)).thenReturn(Optional.of(order))
            whenever(duplicateUtility.isExistingReviewFound(RS_VENDOR_ID, RS_CUSTOMER_ID, RS_ORDER_ID, ReviewType.VENDOR))
                .thenReturn(false)
            whenever(vendorRepository.findById(RS_VENDOR_ID)).thenReturn(Optional.empty())

            val dto = ReviewDto(
                id = null, review = "Not found", rating = 2.0f, orderId = RS_ORDER_ID,
                beverageReviewDto = null, dessertReviewDto = null, foodReviewDto = null,
                handheldReviewDto = null,
                vendorReviewDto = VendorReviewDto(vendorId = RS_VENDOR_ID)
            )

            assertThrows<RequestedEntityNotFoundException> {
                reviewService.addOrUpdateReview(dto, rsCustomerSecurity())
            }
        }

        @Test
        fun `updates vendor review when id is provided and ownership matches`() {
            val order = rsOrder()
            val existingReview = rsVendorReview()
            whenever(orderRepository.findById(RS_ORDER_ID)).thenReturn(Optional.of(order))
            whenever(duplicateUtility.isExistingReviewFound(RS_VENDOR_ID, RS_CUSTOMER_ID, RS_ORDER_ID, ReviewType.VENDOR))
                .thenReturn(false)
            whenever(vendorRepository.findById(RS_VENDOR_ID)).thenReturn(Optional.of(rsVendor()))
            whenever(vendorReviewRepository.findById(RS_REVIEW_ID)).thenReturn(Optional.of(existingReview))
            whenever(vendorReviewRepository.saveAndFlush(any())).thenReturn(existingReview)

            val dto = ReviewDto(
                id = RS_REVIEW_ID, review = "Updated vendor review", rating = 5.0f, orderId = RS_ORDER_ID,
                beverageReviewDto = null, dessertReviewDto = null, foodReviewDto = null,
                handheldReviewDto = null,
                vendorReviewDto = VendorReviewDto(vendorId = RS_VENDOR_ID)
            )

            reviewService.addOrUpdateReview(dto, rsCustomerSecurity())

            verify(vendorReviewRepository).saveAndFlush(argThat {
                review == "Updated vendor review" && rating == 5.0f
            })
        }
    }

    // deleteReview — dispatch
    @Nested
    inner class DeleteReview {

        @Test
        fun `deletes beverage review by delegating to getBeverageReview then delete`() {
            val review = rsBeverageReview()
            whenever(beverageReviewRepository.findById(RS_REVIEW_ID)).thenReturn(Optional.of(review))

            reviewService.deleteReview(reviewTypeDto("BEVERAGE"), RS_REVIEW_ID, rsCustomerSecurity())

            verify(beverageReviewRepository).delete(review)
        }

        @Test
        fun `deletes dessert review`() {
            val review = rsDessertReview()
            whenever(dessertReviewRepository.findById(RS_REVIEW_ID)).thenReturn(Optional.of(review))

            reviewService.deleteReview(reviewTypeDto("DESSERT"), RS_REVIEW_ID, rsCustomerSecurity())

            verify(dessertReviewRepository).delete(review)
        }

        @Test
        fun `deletes food review`() {
            val review = rsFoodReview()
            whenever(foodReviewRepository.findById(RS_REVIEW_ID)).thenReturn(Optional.of(review))

            reviewService.deleteReview(reviewTypeDto("FOOD"), RS_REVIEW_ID, rsCustomerSecurity())

            verify(foodReviewRepository).delete(review)
        }

        @Test
        fun `deletes handheld review`() {
            val review = rsHandheldReview()
            whenever(handheldReviewRepository.findById(RS_REVIEW_ID)).thenReturn(Optional.of(review))

            reviewService.deleteReview(reviewTypeDto("HANDHELD"), RS_REVIEW_ID, rsCustomerSecurity())

            verify(handheldReviewRepository).delete(review)
        }

        @Test
        fun `deletes vendor review`() {
            val review = rsVendorReview()
            whenever(vendorReviewRepository.findById(RS_REVIEW_ID)).thenReturn(Optional.of(review))

            reviewService.deleteReview(reviewTypeDto("VENDOR"), RS_REVIEW_ID, rsCustomerSecurity())

            verify(vendorReviewRepository).delete(review)
        }

        @Test
        fun `throws RequestedEntityNotFoundException when beverage review to delete does not exist`() {
            whenever(beverageReviewRepository.findById(RS_REVIEW_ID)).thenReturn(Optional.empty())

            assertThrows<RequestedEntityNotFoundException> {
                reviewService.deleteReview(reviewTypeDto("BEVERAGE"), RS_REVIEW_ID, rsCustomerSecurity())
            }
        }

        @Test
        fun `throws PermissionDeniedException when deleting a review owned by another customer`() {
            val otherCustomer = Customer().apply { id = UUID.randomUUID(); username = "other" }
            val foreignReview = rsFoodReview(customer = otherCustomer)
            whenever(foodReviewRepository.findById(RS_REVIEW_ID)).thenReturn(Optional.of(foreignReview))

            assertThrows<PermissionDeniedException> {
                reviewService.deleteReview(reviewTypeDto("FOOD"), RS_REVIEW_ID, rsCustomerSecurity())
            }
        }

        @Test
        fun `throws IllegalArgumentException for invalid review type`() {
            assertThrows<IllegalArgumentException> {
                reviewService.deleteReview(reviewTypeDto("INVALID"), RS_REVIEW_ID, rsCustomerSecurity())
            }
        }

        @Test
        fun `does not call any delete when review is not found`() {
            whenever(beverageReviewRepository.findById(RS_REVIEW_ID)).thenReturn(Optional.empty())

            runCatching { reviewService.deleteReview(reviewTypeDto("BEVERAGE"), RS_REVIEW_ID, rsCustomerSecurity()) }

            verify(beverageReviewRepository, never()).delete(any())
        }
    }

    // Vendor reading reviews for their own items
    @Nested
    inner class VendorReadsItemReviews {

        // Vendors read reviews via scrollBeverageReviews / getAllItemReviews — the service
        // methods that accept only an item id (no UserSecurity). The controller enforces
        // ownership via validatePath before reaching the service, so here we verify the
        // service correctly returns all reviews for a given item id regardless of who calls
        // it, and that the returned data belongs to the correct item.

        private fun buildBeverageWindow(reviews: List<BeverageReview>): Window<BeverageReview> =
            Window.from(reviews, { ScrollPosition.offset() }, false)

        private fun buildDessertWindow(reviews: List<DessertReview>): Window<DessertReview> =
            Window.from(reviews, { ScrollPosition.offset() }, false)

        private fun buildFoodWindow(reviews: List<FoodReview>): Window<FoodReview> =
            Window.from(reviews, { ScrollPosition.offset() }, false)

        private fun buildHandheldWindow(reviews: List<HandheldReview>): Window<HandheldReview> =
            Window.from(reviews, { ScrollPosition.offset() }, false)

        private fun buildVendorWindow(reviews: List<VendorReview>): Window<VendorReview> =
            Window.from(reviews, { ScrollPosition.offset() }, false)


        // scrollBeverageReviews — vendor perspective
        @Test
        fun `vendor can scroll beverage reviews for their beverage`() {
            val review = rsBeverageReview()
            val window = buildBeverageWindow(listOf(review))
            whenever(beverageReviewRepository.findAllByBeverageId(eq(RS_BEVERAGE_ID), any<ScrollPosition>(), any<Sort>(), any<Limit>()))
                .thenReturn(window)
            whenever(cursorEncoder.encodeOffset(any())).thenReturn("cursor1")

            val result = reviewService.scrollBeverageReviews(RS_BEVERAGE_ID, null, 10)

            assertEquals(1, result.content.size)
            assertInstanceOf(BeverageOrderItemReviewResponse::class.java, result.content.first())
        }

        @Test
        fun `vendor receives empty beverage reviews when no customer has reviewed their beverage yet`() {
            val window = buildBeverageWindow(emptyList())
            whenever(beverageReviewRepository.findAllByBeverageId(eq(RS_BEVERAGE_ID), any<ScrollPosition>(), any<Sort>(), any<Limit>()))
                .thenReturn(window)
            whenever(cursorEncoder.encodeOffset(any())).thenReturn("cursor0")

            val result = reviewService.scrollBeverageReviews(RS_BEVERAGE_ID, null, 10)

            assertEquals(0, result.content.size)
            assertNull(result.nextCursor)
        }

        @Test
        fun `vendor can scroll multiple pages of beverage reviews using cursor`() {
            val review = rsBeverageReview()
            val windowPage2 = buildBeverageWindow(listOf(review))
            whenever(beverageReviewRepository.findAllByBeverageId(eq(RS_BEVERAGE_ID), any<ScrollPosition>(), any<Sort>(), any<Limit>()))
                .thenReturn(windowPage2)
            whenever(cursorEncoder.encodeOffset(any())).thenReturn("cursor10")

            // Simulates vendor passing a non-null cursor for subsequent page
            val result = reviewService.scrollBeverageReviews(RS_BEVERAGE_ID, "10", 10)

            assertEquals(1, result.content.size)
            verify(beverageReviewRepository).findAllByBeverageId(eq(RS_BEVERAGE_ID), any<ScrollPosition>(), any<Sort>(), any<Limit>())
        }

        // scrollDessertReviews — vendor perspective
        @Test
        fun `vendor can scroll dessert reviews for their dessert`() {
            val review = rsDessertReview()
            val window = buildDessertWindow(listOf(review))
            whenever(dessertReviewRepository.findAllByDessertId(eq(RS_DESSERT_ID), any<ScrollPosition>(), any<Sort>(), any<Limit>()))
                .thenReturn(window)
            whenever(cursorEncoder.encodeOffset(any())).thenReturn("cursor1")

            val result = reviewService.scrollDessertReviews(RS_DESSERT_ID, null, 10)

            assertEquals(1, result.content.size)
            assertInstanceOf(DessertOrderItemReviewResponse::class.java, result.content.first())
        }

        @Test
        fun `vendor receives empty dessert reviews when no reviews exist for their dessert`() {
            val window = buildDessertWindow(emptyList())
            whenever(dessertReviewRepository.findAllByDessertId(eq(RS_DESSERT_ID), any<ScrollPosition>(), any<Sort>(), any<Limit>()))
                .thenReturn(window)
            whenever(cursorEncoder.encodeOffset(any())).thenReturn("cursor0")

            val result = reviewService.scrollDessertReviews(RS_DESSERT_ID, null, 10)

            assertEquals(0, result.content.size)
        }

        // scrollFoodReviews — vendor perspective
        @Test
        fun `vendor can scroll food reviews for their food item`() {
            val review = rsFoodReview()
            val window = buildFoodWindow(listOf(review))
            whenever(foodReviewRepository.findAllByFoodId(eq(RS_FOOD_ID), any<ScrollPosition>(), any<Sort>(), any<Limit>()))
                .thenReturn(window)
            whenever(cursorEncoder.encodeOffset(any())).thenReturn("cursor1")

            val result = reviewService.scrollFoodReviews(RS_FOOD_ID, null, 10)

            assertEquals(1, result.content.size)
            assertInstanceOf(FoodOrderItemReviewResponse::class.java, result.content.first())
        }

        @Test
        fun `vendor receives empty food reviews when no reviews exist for their food`() {
            val window = buildFoodWindow(emptyList())
            whenever(foodReviewRepository.findAllByFoodId(eq(RS_FOOD_ID), any<ScrollPosition>(), any<Sort>(), any<Limit>()))
                .thenReturn(window)
            whenever(cursorEncoder.encodeOffset(any())).thenReturn("cursor0")

            val result = reviewService.scrollFoodReviews(RS_FOOD_ID, null, 10)

            assertEquals(0, result.content.size)
        }

        @Test
        fun `vendor can scroll multiple food reviews across two pages`() {
            val review1 = rsFoodReview()
            val review2 = rsFoodReview().apply { id = UUID.randomUUID(); review = "Second review" }
            val window = buildFoodWindow(listOf(review1, review2))
            whenever(foodReviewRepository.findAllByFoodId(eq(RS_FOOD_ID), any<ScrollPosition>(), any<Sort>(), any<Limit>()))
                .thenReturn(window)
            whenever(cursorEncoder.encodeOffset(any())).thenReturn("cursor2")

            val result = reviewService.scrollFoodReviews(RS_FOOD_ID, null, 10)

            assertEquals(2, result.content.size)
        }

        // scrollHandheldReviews — vendor perspective
        @Test
        fun `vendor can scroll handheld reviews for their handheld`() {
            val review = rsHandheldReview()
            val window = buildHandheldWindow(listOf(review))
            whenever(handheldReviewRepository.findAllByHandheldId(eq(RS_HANDHELD_ID), any<ScrollPosition>(), any<Sort>(), any<Limit>()))
                .thenReturn(window)
            whenever(cursorEncoder.encodeOffset(any())).thenReturn("cursor1")

            val result = reviewService.scrollHandheldReviews(RS_HANDHELD_ID, null, 10)

            assertEquals(1, result.content.size)
            assertInstanceOf(HandheldOrderItemReviewResponse::class.java, result.content.first())
        }

        @Test
        fun `vendor receives empty handheld reviews when no reviews exist`() {
            val window = buildHandheldWindow(emptyList())
            whenever(handheldReviewRepository.findAllByHandheldId(eq(RS_HANDHELD_ID), any<ScrollPosition>(), any<Sort>(), any<Limit>()))
                .thenReturn(window)
            whenever(cursorEncoder.encodeOffset(any())).thenReturn("cursor0")

            val result = reviewService.scrollHandheldReviews(RS_HANDHELD_ID, null, 10)

            assertEquals(0, result.content.size)
        }

        // scrollVendorReviews — vendor reads their own vendor reviews
        @Test
        fun `vendor can scroll reviews about themselves`() {
            val review = rsVendorReview()
            val window = buildVendorWindow(listOf(review))
            whenever(vendorReviewRepository.findAllByVendorId(eq(RS_VENDOR_ID), any<ScrollPosition>(), any<Sort>(), any<Limit>()))
                .thenReturn(window)
            whenever(cursorEncoder.encodeOffset(any())).thenReturn("cursor1")

            val result = reviewService.scrollVendorReviews(RS_VENDOR_ID, null, 10)

            assertEquals(1, result.content.size)
            assertInstanceOf(VendorOrderReviewResponse::class.java, result.content.first())
        }

        @Test
        fun `vendor receives empty vendor reviews when no customer has reviewed them`() {
            val window = buildVendorWindow(emptyList())
            whenever(vendorReviewRepository.findAllByVendorId(eq(RS_VENDOR_ID), any<ScrollPosition>(), any<Sort>(), any<Limit>()))
                .thenReturn(window)
            whenever(cursorEncoder.encodeOffset(any())).thenReturn("cursor0")

            val result = reviewService.scrollVendorReviews(RS_VENDOR_ID, null, 10)

            assertEquals(0, result.content.size)
            assertNull(result.nextCursor)
        }

        // getAllItemReviews — vendor reading paged reviews
        @Test
        fun `vendor can get all beverage reviews paginated for their beverage`() {
            val review = rsBeverageReview()
            whenever(beverageReviewRepository.findAllByBeverageId(eq(RS_BEVERAGE_ID), any<Pageable>()))
                .thenReturn(PageImpl(listOf(review)))

            val result = reviewService.getAllItemReviews(RS_BEVERAGE_ID, reviewTypeDto("BEVERAGE"), 0, 10)

            assertEquals(1, result.totalElements)
            assertInstanceOf(BeverageOrderItemReviewResponse::class.java, result.content.first())
        }

        @Test
        fun `vendor can get all food reviews paginated for their food`() {
            val review = rsFoodReview()
            whenever(foodReviewRepository.findAllByFoodId(eq(RS_FOOD_ID), any<Pageable>()))
                .thenReturn(PageImpl(listOf(review)))

            val result = reviewService.getAllItemReviews(RS_FOOD_ID, reviewTypeDto("FOOD"), 0, 10)

            assertEquals(1, result.totalElements)
            assertInstanceOf(FoodOrderItemReviewResponse::class.java, result.content.first())
        }

        @Test
        fun `vendor can get all dessert reviews paginated for their dessert`() {
            val review = rsDessertReview()
            whenever(dessertReviewRepository.findAllByDessertId(eq(RS_DESSERT_ID), any<Pageable>()))
                .thenReturn(PageImpl(listOf(review)))

            val result = reviewService.getAllItemReviews(RS_DESSERT_ID, reviewTypeDto("DESSERT"), 0, 10)

            assertEquals(1, result.totalElements)
            assertInstanceOf(DessertOrderItemReviewResponse::class.java, result.content.first())
        }

        @Test
        fun `vendor can get all handheld reviews paginated for their handheld`() {
            val review = rsHandheldReview()
            whenever(handheldReviewRepository.findAllByHandheldId(eq(RS_HANDHELD_ID), any<Pageable>()))
                .thenReturn(PageImpl(listOf(review)))

            val result = reviewService.getAllItemReviews(RS_HANDHELD_ID, reviewTypeDto("HANDHELD"), 0, 10)

            assertEquals(1, result.totalElements)
            assertInstanceOf(HandheldOrderItemReviewResponse::class.java, result.content.first())
        }

        @Test
        fun `vendor can get all vendor-level reviews paginated`() {
            val review = rsVendorReview()
            whenever(vendorReviewRepository.findAllByVendorId(eq(RS_VENDOR_ID), any<Pageable>()))
                .thenReturn(PageImpl(listOf(review)))

            val result = reviewService.getAllItemReviews(RS_VENDOR_ID, reviewTypeDto("VENDOR"), 0, 10)

            assertEquals(1, result.totalElements)
            assertInstanceOf(VendorOrderReviewResponse::class.java, result.content.first())
        }

        @Test
        fun `vendor receives correct rating values in beverage review response`() {
            val review = rsBeverageReview()
            whenever(beverageReviewRepository.findAllByBeverageId(eq(RS_BEVERAGE_ID), any<Pageable>()))
                .thenReturn(PageImpl(listOf(review)))

            val result = reviewService.getAllItemReviews(RS_BEVERAGE_ID, reviewTypeDto("BEVERAGE"), 0, 10)

            val response = result.content.first() as BeverageOrderItemReviewResponse
            assertEquals(4.5f, response.rating)
        }

        @Test
        fun `vendor receives correct rating values in food review response`() {
            val review = rsFoodReview()
            whenever(foodReviewRepository.findAllByFoodId(eq(RS_FOOD_ID), any<Pageable>()))
                .thenReturn(PageImpl(listOf(review)))

            val result = reviewService.getAllItemReviews(RS_FOOD_ID, reviewTypeDto("FOOD"), 0, 10)

            val response = result.content.first() as FoodOrderItemReviewResponse
            assertEquals(4.0f, response.rating)
        }

        @Test
        fun `vendor receives correct review text in vendor review response`() {
            val review = rsVendorReview()
            whenever(vendorReviewRepository.findAllByVendorId(eq(RS_VENDOR_ID), any<Pageable>()))
                .thenReturn(PageImpl(listOf(review)))

            val result = reviewService.getAllItemReviews(RS_VENDOR_ID, reviewTypeDto("VENDOR"), 0, 10)

            val response = result.content.first() as VendorOrderReviewResponse
            assertEquals("Great service!", response.review)
        }

        @Test
        fun `vendor sees empty list when no reviews exist for any of their items`() {
            whenever(foodReviewRepository.findAllByFoodId(eq(RS_FOOD_ID), any<Pageable>()))
                .thenReturn(PageImpl(emptyList()))

            val result = reviewService.getAllItemReviews(RS_FOOD_ID, reviewTypeDto("FOOD"), 0, 10)

            assertEquals(0, result.totalElements)
            assertTrue(result.content.isEmpty())
        }

        @Test
        fun `vendor uses ascending createdAt sort when fetching reviews page`() {
            whenever(beverageReviewRepository.findAllByBeverageId(eq(RS_BEVERAGE_ID), any<Pageable>()))
                .thenReturn(PageImpl(emptyList()))

            reviewService.getAllItemReviews(RS_BEVERAGE_ID, reviewTypeDto("BEVERAGE"), 0, 10)

            verify(beverageReviewRepository).findAllByBeverageId(eq(RS_BEVERAGE_ID), argThat<Pageable> {
                sort.getOrderFor("createdAt")?.isAscending == true
            })
        }

        @Test
        fun `vendor uses ascending createdAt sort when fetching food reviews page`() {
            whenever(foodReviewRepository.findAllByFoodId(eq(RS_FOOD_ID), any<Pageable>()))
                .thenReturn(PageImpl(emptyList()))

            reviewService.getAllItemReviews(RS_FOOD_ID, reviewTypeDto("FOOD"), 0, 10)

            verify(foodReviewRepository).findAllByFoodId(eq(RS_FOOD_ID), argThat<Pageable> {
                sort.getOrderFor("createdAt")?.isAscending == true
            })
        }

        @Test
        fun `vendor passes correct page number and size when reading reviews`() {
            whenever(foodReviewRepository.findAllByFoodId(eq(RS_FOOD_ID), any<Pageable>()))
                .thenReturn(PageImpl(emptyList()))

            reviewService.getAllItemReviews(RS_FOOD_ID, reviewTypeDto("FOOD"), 2, 5)

            verify(foodReviewRepository).findAllByFoodId(eq(RS_FOOD_ID), argThat<Pageable> {
                pageNumber == 2 && pageSize == 5
            })
        }
    }
}

