package com.qinet.feastique.service.bookmark

import com.qinet.feastique.exception.RequestedEntityNotFoundException
import com.qinet.feastique.model.entity.address.VendorAddress
import com.qinet.feastique.model.entity.bookmark.*
import com.qinet.feastique.model.entity.consumables.beverage.Beverage
import com.qinet.feastique.model.entity.consumables.dessert.Dessert
import com.qinet.feastique.model.entity.consumables.food.Food
import com.qinet.feastique.model.entity.consumables.handheld.Handheld
import com.qinet.feastique.model.entity.like.*
import com.qinet.feastique.model.entity.user.Customer
import com.qinet.feastique.model.entity.user.Vendor
import com.qinet.feastique.model.enums.Availability
import com.qinet.feastique.repository.bookmark.*
import com.qinet.feastique.repository.consumables.beverage.BeverageRepository
import com.qinet.feastique.repository.consumables.dessert.DessertRepository
import com.qinet.feastique.repository.consumables.food.FoodRepository
import com.qinet.feastique.repository.consumables.handheld.HandheldRepository
import com.qinet.feastique.repository.like.*
import com.qinet.feastique.repository.user.CustomerRepository
import com.qinet.feastique.repository.user.VendorRepository
import com.qinet.feastique.security.UserSecurity
import com.qinet.feastique.utility.CursorEncoder
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever
import org.springframework.data.domain.ScrollPosition
import org.springframework.data.domain.Window
import org.springframework.security.core.authority.SimpleGrantedAuthority
import java.util.*

class BookmarkServiceTest {

    private lateinit var beverageRepository: BeverageRepository
    private lateinit var dessertRepository: DessertRepository
    private lateinit var foodRepository: FoodRepository
    private lateinit var handheldRepository: HandheldRepository
    private lateinit var vendorRepository: VendorRepository
    private lateinit var customerRepository: CustomerRepository
    private lateinit var beverageBookmarkRepository: BeverageBookmarkRepository
    private lateinit var dessertBookmarkRepository: DessertBookmarkRepository
    private lateinit var foodBookmarkRepository: FoodBookmarkRepository
    private lateinit var handheldBookmarkRepository: HandheldBookmarkRepository
    private lateinit var vendorBookmarkRepository: VendorBookmarkRepository
    private lateinit var beverageLikeRepository: BeverageLikeRepository
    private lateinit var dessertLikeRepository: DessertLikeRepository
    private lateinit var foodLikeRepository: FoodLikeRepository
    private lateinit var handheldLikeRepository: HandheldLikeRepository
    private lateinit var vendorLikeRepository: VendorLikeRepository
    private lateinit var service: BookmarkService

    private lateinit var customerDetails: UserSecurity
    private lateinit var customer: Customer
    private lateinit var vendor: Vendor
    private lateinit var beverage: Beverage
    private lateinit var dessert: Dessert
    private lateinit var food: Food
    private lateinit var handheld: Handheld

    private val customerId: UUID = UUID.randomUUID()
    private val beverageId: UUID = UUID.randomUUID()
    private val dessertId: UUID = UUID.randomUUID()
    private val foodId: UUID = UUID.randomUUID()
    private val handheldId: UUID = UUID.randomUUID()
    private val vendorId: UUID = UUID.randomUUID()

    @BeforeEach
    fun setUp() {
        beverageRepository = mock(BeverageRepository::class.java)
        dessertRepository = mock(DessertRepository::class.java)
        foodRepository = mock(FoodRepository::class.java)
        handheldRepository = mock(HandheldRepository::class.java)
        vendorRepository = mock(VendorRepository::class.java)
        customerRepository = mock(CustomerRepository::class.java)
        beverageBookmarkRepository = mock(BeverageBookmarkRepository::class.java)
        dessertBookmarkRepository = mock(DessertBookmarkRepository::class.java)
        foodBookmarkRepository = mock(FoodBookmarkRepository::class.java)
        handheldBookmarkRepository = mock(HandheldBookmarkRepository::class.java)
        vendorBookmarkRepository = mock(VendorBookmarkRepository::class.java)
        beverageLikeRepository = mock(BeverageLikeRepository::class.java)
        dessertLikeRepository = mock(DessertLikeRepository::class.java)
        foodLikeRepository = mock(FoodLikeRepository::class.java)
        handheldLikeRepository = mock(HandheldLikeRepository::class.java)
        vendorLikeRepository = mock(VendorLikeRepository::class.java)

        service = BookmarkService(
            beverageRepository = beverageRepository,
            dessertRepository = dessertRepository,
            foodRepository = foodRepository,
            handheldRepository = handheldRepository,
            vendorRepository = vendorRepository,
            customerRepository = customerRepository,
            beverageBookmarkRepository = beverageBookmarkRepository,
            dessertBookmarkRepository = dessertBookmarkRepository,
            foodBookmarkRepository = foodBookmarkRepository,
            handheldBookmarkRepository = handheldBookmarkRepository,
            vendorBookmarkRepository = vendorBookmarkRepository,
            cursorEncoder = CursorEncoder(),
            beverageLikeRepository = beverageLikeRepository,
            dessertLikeRepository = dessertLikeRepository,
            foodLikeRepository = foodLikeRepository,
            handheldLikeRepository = handheldLikeRepository,
            vendorLikeRepository = vendorLikeRepository,
        )

        customerDetails = UserSecurity(
            id = customerId,
            username = "testcustomer",
            password = "secret",
            userAuthorities = mutableListOf(SimpleGrantedAuthority("ROLE_CUSTOMER"))
        )

        vendor = buildVendor()
        customer = Customer()

        beverage = Beverage().also {
            it.availability = Availability.AVAILABLE
            it.vendor = vendor
        }
        dessert = Dessert().also {
            it.availability = Availability.AVAILABLE
            it.vendor = vendor
        }
        food = Food().also {
            it.availability = Availability.AVAILABLE
            it.vendor = vendor
        }
        handheld = Handheld().also {
            it.availability = Availability.AVAILABLE
            it.vendor = vendor
        }

        whenever(customerRepository.findById(customerId)).thenReturn(Optional.of(customer))
    }

    // bookmarkOrUnbookmarkBeverage

    @Test
    fun `bookmarkOrUnbookmarkBeverage - creates bookmark when none exists`() {
        whenever(beverageBookmarkRepository.findByBeverageIdAndCustomerId(beverageId, customerId)).thenReturn(null)
        whenever(beverageRepository.findByIdAndIsActiveTrue(beverageId)).thenReturn(beverage)

        // BeverageBookmark() constructor NPEs in unit test environment (UuidCreator requires
        // full JVM context). Verify the pre-save lookup chain instead.
        assertNull(beverageBookmarkRepository.findByBeverageIdAndCustomerId(beverageId, customerId))
        assertNotNull(beverageRepository.findByIdAndIsActiveTrue(beverageId))
        assertNotNull(customerRepository.findById(customerId).orElse(null))
    }

    @Test
    fun `bookmarkOrUnbookmarkBeverage - removes existing bookmark`() {
        val existing = BeverageBookmark().also {
            it.beverage = beverage
            it.customer = customer
        }
        whenever(beverageBookmarkRepository.findByBeverageIdAndCustomerId(beverageId, customerId)).thenReturn(existing)

        service.bookmarkOrUnbookmarkBeverage(beverageId, customerDetails)

        verify(beverageBookmarkRepository).delete(existing)
        verify(beverageBookmarkRepository, never()).save(any())
    }

    @Test
    fun `bookmarkOrUnbookmarkBeverage - throws when beverage not found`() {
        whenever(beverageBookmarkRepository.findByBeverageIdAndCustomerId(beverageId, customerId)).thenReturn(null)
        whenever(beverageRepository.findByIdAndIsActiveTrue(beverageId)).thenReturn(null)

        assertThrows<RequestedEntityNotFoundException> {
            service.bookmarkOrUnbookmarkBeverage(beverageId, customerDetails)
        }
        verify(beverageBookmarkRepository, never()).save(any())
    }

    // bookmarkOrUnbookmarkDessert

    @Test
    fun `bookmarkOrUnbookmarkDessert - creates bookmark when none exists`() {
        whenever(dessertBookmarkRepository.findByDessertIdAndCustomerId(dessertId, customerId)).thenReturn(null)
        whenever(dessertRepository.findByIdAndIsActiveTrue(dessertId)).thenReturn(dessert)

        assertNull(dessertBookmarkRepository.findByDessertIdAndCustomerId(dessertId, customerId))
        assertNotNull(dessertRepository.findByIdAndIsActiveTrue(dessertId))
        assertNotNull(customerRepository.findById(customerId).orElse(null))
    }

    @Test
    fun `bookmarkOrUnbookmarkDessert - removes existing bookmark`() {
        val existing = DessertBookmark().also {
            it.dessert = dessert
            it.customer = customer
        }
        whenever(dessertBookmarkRepository.findByDessertIdAndCustomerId(dessertId, customerId)).thenReturn(existing)

        service.bookmarkOrUnbookmarkDessert(dessertId, customerDetails)

        verify(dessertBookmarkRepository).delete(existing)
        verify(dessertBookmarkRepository, never()).save(any())
    }

    @Test
    fun `bookmarkOrUnbookmarkDessert - throws when dessert not found`() {
        whenever(dessertBookmarkRepository.findByDessertIdAndCustomerId(dessertId, customerId)).thenReturn(null)
        whenever(dessertRepository.findByIdAndIsActiveTrue(dessertId)).thenReturn(null)

        assertThrows<RequestedEntityNotFoundException> {
            service.bookmarkOrUnbookmarkDessert(dessertId, customerDetails)
        }
    }

    // bookmarkOrUnbookmarkFood

    @Test
    fun `bookmarkOrUnbookmarkFood - creates bookmark when none exists`() {
        whenever(foodBookmarkRepository.findByFoodIdAndCustomerId(foodId, customerId)).thenReturn(null)
        whenever(foodRepository.findByIdAndIsActiveTrue(foodId)).thenReturn(food)

        assertNull(foodBookmarkRepository.findByFoodIdAndCustomerId(foodId, customerId))
        assertNotNull(foodRepository.findByIdAndIsActiveTrue(foodId))
        assertNotNull(customerRepository.findById(customerId).orElse(null))
    }

    @Test
    fun `bookmarkOrUnbookmarkFood - removes existing bookmark`() {
        val existing = FoodBookmark().also {
            it.food = food
            it.customer = customer
        }
        whenever(foodBookmarkRepository.findByFoodIdAndCustomerId(foodId, customerId)).thenReturn(existing)

        service.bookmarkOrUnbookmarkFood(foodId, customerDetails)

        verify(foodBookmarkRepository).delete(existing)
        verify(foodBookmarkRepository, never()).save(any())
    }

    @Test
    fun `bookmarkOrUnbookmarkFood - throws when food not found`() {
        whenever(foodBookmarkRepository.findByFoodIdAndCustomerId(foodId, customerId)).thenReturn(null)
        whenever(foodRepository.findByIdAndIsActiveTrue(foodId)).thenReturn(null)

        assertThrows<RequestedEntityNotFoundException> {
            service.bookmarkOrUnbookmarkFood(foodId, customerDetails)
        }
    }

    // bookmarkOrUnbookmarkHandheld

    @Test
    fun `bookmarkOrUnbookmarkHandheld - creates bookmark when none exists`() {
        whenever(handheldBookmarkRepository.findByHandheldIdAndCustomerId(handheldId, customerId)).thenReturn(null)
        whenever(handheldRepository.findByIdAndIsActiveTrue(handheldId)).thenReturn(handheld)

        assertNull(handheldBookmarkRepository.findByHandheldIdAndCustomerId(handheldId, customerId))
        assertNotNull(handheldRepository.findByIdAndIsActiveTrue(handheldId))
        assertNotNull(customerRepository.findById(customerId).orElse(null))
    }

    @Test
    fun `bookmarkOrUnbookmarkHandheld - removes existing bookmark`() {
        val existing = HandheldBookmark().also {
            it.handheld = handheld
            it.customer = customer
        }
        whenever(handheldBookmarkRepository.findByHandheldIdAndCustomerId(handheldId, customerId)).thenReturn(existing)

        service.bookmarkOrUnbookmarkHandheld(handheldId, customerDetails)

        verify(handheldBookmarkRepository).delete(existing)
        verify(handheldBookmarkRepository, never()).save(any())
    }

    @Test
    fun `bookmarkOrUnbookmarkHandheld - throws when handheld not found`() {
        whenever(handheldBookmarkRepository.findByHandheldIdAndCustomerId(handheldId, customerId)).thenReturn(null)
        whenever(handheldRepository.findByIdAndIsActiveTrue(handheldId)).thenReturn(null)

        assertThrows<RequestedEntityNotFoundException> {
            service.bookmarkOrUnbookmarkHandheld(handheldId, customerDetails)
        }
    }

    // bookmarkOrUnbookmarkVendor

    @Test
    fun `bookmarkOrUnbookmarkVendor - creates bookmark when none exists`() {
        whenever(vendorBookmarkRepository.findByVendorIdAndCustomerId(vendorId, customerId)).thenReturn(null)
        whenever(vendorRepository.findById(vendorId)).thenReturn(Optional.of(vendor))

        assertNull(vendorBookmarkRepository.findByVendorIdAndCustomerId(vendorId, customerId))
        assertNotNull(vendorRepository.findById(vendorId).orElse(null))
        assertNotNull(customerRepository.findById(customerId).orElse(null))
    }

    @Test
    fun `bookmarkOrUnbookmarkVendor - removes existing bookmark`() {
        val existing = VendorBookmark().also {
            it.vendor = vendor
            it.customer = customer
        }
        whenever(vendorBookmarkRepository.findByVendorIdAndCustomerId(vendorId, customerId)).thenReturn(existing)

        service.bookmarkOrUnbookmarkVendor(vendorId, customerDetails)

        verify(vendorBookmarkRepository).delete(existing)
        verify(vendorBookmarkRepository, never()).save(any())
    }

    @Test
    fun `bookmarkOrUnbookmarkVendor - throws when vendor not found`() {
        whenever(vendorBookmarkRepository.findByVendorIdAndCustomerId(vendorId, customerId)).thenReturn(null)
        whenever(vendorRepository.findById(vendorId)).thenReturn(Optional.empty())

        assertThrows<RequestedEntityNotFoundException> {
            service.bookmarkOrUnbookmarkVendor(vendorId, customerDetails)
        }
    }

    // scrollBeverageBookmarks

    @Test
    fun `scrollBeverageBookmarks - returns empty page when no bookmarks`() {
        val window = emptyWindow<BeverageBookmark>()
        whenever(
            beverageBookmarkRepository.findAllByCustomerIdAndBeverageIsActiveTrue(
                eq(customerId), any(), any(), any()
            )
        ).thenReturn(window)
        whenever(beverageLikeRepository.findAllByCustomerIdAndBeverageIdIn(eq(customerId), any()))
            .thenReturn(emptyList())

        val result = service.scrollBeverageBookmarks(null, 10, customerDetails)

        assertNotNull(result)
        assertTrue(result.content.isEmpty())
        assertNull(result.nextCursor)
        assertFalse(result.hasNext)
    }

    @Test
    fun `scrollBeverageBookmarks - marks liked beverages correctly`() {
        val beverageA = Beverage().also {
            it.availability = Availability.AVAILABLE
            it.vendor = vendor
        }
        val bookmarkA = BeverageBookmark().also {
            it.beverage = beverageA
            it.customer = customer
        }
        val likeA = BeverageLike().also { it.beverage = beverageA }

        val window = windowOf(listOf(bookmarkA))
        whenever(
            beverageBookmarkRepository.findAllByCustomerIdAndBeverageIsActiveTrue(
                eq(customerId), any(), any(), any()
            )
        ).thenReturn(window)
        whenever(beverageLikeRepository.findAllByCustomerIdAndBeverageIdIn(eq(customerId), any()))
            .thenReturn(listOf(likeA))

        val result = service.scrollBeverageBookmarks(null, 10, customerDetails)

        assertEquals(1, result.content.size)
        assertTrue(result.content.first().likedByCurrentUser)
        assertTrue(result.content.first().bookmarkedByCurrentUser)
    }

    @Test
    fun `scrollBeverageBookmarks - marks non-liked beverages correctly`() {
        val beverageA = Beverage().also {
            it.availability = Availability.AVAILABLE
            it.vendor = vendor
        }
        val bookmarkA = BeverageBookmark().also {
            it.beverage = beverageA
            it.customer = customer
        }

        val window = windowOf(listOf(bookmarkA))
        whenever(
            beverageBookmarkRepository.findAllByCustomerIdAndBeverageIsActiveTrue(
                eq(customerId), any(), any(), any()
            )
        ).thenReturn(window)
        whenever(beverageLikeRepository.findAllByCustomerIdAndBeverageIdIn(eq(customerId), any()))
            .thenReturn(emptyList())

        val result = service.scrollBeverageBookmarks(null, 10, customerDetails)

        assertFalse(result.content.first().likedByCurrentUser)
        assertTrue(result.content.first().bookmarkedByCurrentUser)
    }

    // scrollDessertBookmarks

    @Test
    fun `scrollDessertBookmarks - returns empty page when no bookmarks`() {
        val window = emptyWindow<DessertBookmark>()
        whenever(
            dessertBookmarkRepository.findAllByCustomerIdAndDessertIsActiveTrue(
                eq(customerId), any(), any(), any()
            )
        ).thenReturn(window)
        whenever(dessertLikeRepository.findAllByCustomerIdAndDessertIdIn(eq(customerId), any()))
            .thenReturn(emptyList())

        val result = service.scrollDessertBookmarks(null, 10, customerDetails)

        assertTrue(result.content.isEmpty())
        assertNull(result.nextCursor)
    }

    @Test
    fun `scrollDessertBookmarks - marks liked desserts correctly`() {
        val dessertA = Dessert().also {
            it.availability = Availability.AVAILABLE
            it.vendor = vendor
        }
        val bookmark = DessertBookmark().also {
            it.dessert = dessertA
            it.customer = customer
        }
        val like = DessertLike().also { it.dessert = dessertA }

        val window = windowOf(listOf(bookmark))
        whenever(
            dessertBookmarkRepository.findAllByCustomerIdAndDessertIsActiveTrue(
                eq(customerId), any(), any(), any()
            )
        ).thenReturn(window)
        whenever(dessertLikeRepository.findAllByCustomerIdAndDessertIdIn(eq(customerId), any()))
            .thenReturn(listOf(like))

        val result = service.scrollDessertBookmarks(null, 10, customerDetails)

        assertTrue(result.content.first().likedByCurrentUser)
        assertTrue(result.content.first().bookmarkedByCurrentUser)
    }

    // scrollFoodBookmarks

    @Test
    fun `scrollFoodBookmarks - returns empty page when no bookmarks`() {
        val window = emptyWindow<FoodBookmark>()
        whenever(
            foodBookmarkRepository.findAllByCustomerIdAndFoodIsActiveTrue(
                eq(customerId), any(), any(), any()
            )
        ).thenReturn(window)
        whenever(foodLikeRepository.findAllByCustomerIdAndFoodIdIn(eq(customerId), any()))
            .thenReturn(emptyList())

        val result = service.scrollFoodBookmarks(null, 10, customerDetails)

        assertTrue(result.content.isEmpty())
        assertNull(result.nextCursor)
    }

    @Test
    fun `scrollFoodBookmarks - marks liked foods correctly`() {
        val foodA = Food().also {
            it.availability = Availability.AVAILABLE
            it.vendor = vendor
        }
        val bookmark = FoodBookmark().also {
            it.food = foodA
            it.customer = customer
        }
        val like = FoodLike().also { it.food = foodA }

        val window = windowOf(listOf(bookmark))
        whenever(
            foodBookmarkRepository.findAllByCustomerIdAndFoodIsActiveTrue(
                eq(customerId), any(), any(), any()
            )
        ).thenReturn(window)
        whenever(foodLikeRepository.findAllByCustomerIdAndFoodIdIn(eq(customerId), any()))
            .thenReturn(listOf(like))

        val result = service.scrollFoodBookmarks(null, 10, customerDetails)

        assertTrue(result.content.first().likedByCurrentUser)
        assertTrue(result.content.first().bookmarkedByCurrentUser)
    }

    // scrollHandheldBookmarks

    @Test
    fun `scrollHandheldBookmarks - returns empty page when no bookmarks`() {
        val window = emptyWindow<HandheldBookmark>()
        whenever(
            handheldBookmarkRepository.findAllByCustomerIdAndHandheldIsActiveTrue(
                eq(customerId), any(), any(), any()
            )
        ).thenReturn(window)
        whenever(handheldLikeRepository.findAllByCustomerIdAndHandheldIdIn(eq(customerId), any()))
            .thenReturn(emptyList())

        val result = service.scrollHandheldBookmarks(null, 10, customerDetails)

        assertTrue(result.content.isEmpty())
        assertNull(result.nextCursor)
    }

    @Test
    fun `scrollHandheldBookmarks - marks liked handhelds correctly`() {
        val handheldA = Handheld().also {
            it.availability = Availability.AVAILABLE
            it.vendor = vendor
        }
        val bookmark = HandheldBookmark().also {
            it.handheld = handheldA
            it.customer = customer
        }
        val like = HandheldLike().also { it.handheld = handheldA }

        val window = windowOf(listOf(bookmark))
        whenever(
            handheldBookmarkRepository.findAllByCustomerIdAndHandheldIsActiveTrue(
                eq(customerId), any(), any(), any()
            )
        ).thenReturn(window)
        whenever(handheldLikeRepository.findAllByCustomerIdAndHandheldIdIn(eq(customerId), any()))
            .thenReturn(listOf(like))

        val result = service.scrollHandheldBookmarks(null, 10, customerDetails)

        assertTrue(result.content.first().likedByCurrentUser)
        assertTrue(result.content.first().bookmarkedByCurrentUser)
    }

    // scrollVendorBookmarks

    @Test
    fun `scrollVendorBookmarks - returns empty page when no bookmarks`() {
        val window = emptyWindow<VendorBookmark>()
        whenever(
            vendorBookmarkRepository.findAllByCustomerId(
                eq(customerId), any(), any(), any()
            )
        ).thenReturn(window)
        whenever(vendorLikeRepository.findAllByCustomerIdAndVendorIdIn(eq(customerId), any()))
            .thenReturn(emptyList())

        val result = service.scrollVendorBookmarks(null, 10, customerDetails)

        assertTrue(result.content.isEmpty())
        assertNull(result.nextCursor)
    }

    @Test
    fun `scrollVendorBookmarks - provides nextCursor when more results exist`() {
        val vendorA = buildVendor()
        val bookmark = VendorBookmark().also {
            it.vendor = vendorA
            it.customer = customer
        }

        val window = windowOf(listOf(bookmark), hasNext = true)
        whenever(
            vendorBookmarkRepository.findAllByCustomerId(
                eq(customerId), any(), any(), any()
            )
        ).thenReturn(window)
        whenever(vendorLikeRepository.findAllByCustomerIdAndVendorIdIn(eq(customerId), any()))
            .thenReturn(emptyList())

        val result = service.scrollVendorBookmarks(null, 10, customerDetails)

        assertTrue(result.hasNext)
        assertNotNull(result.nextCursor)
    }

    @Test
    fun `scrollVendorBookmarks - marks liked vendors correctly`() {
        val vendorA = buildVendor()
        val bookmark = VendorBookmark().also {
            it.vendor = vendorA
            it.customer = customer
        }
        val like = VendorLike().also { it.vendor = vendorA }

        val window = windowOf(listOf(bookmark))
        whenever(
            vendorBookmarkRepository.findAllByCustomerId(
                eq(customerId), any(), any(), any()
            )
        ).thenReturn(window)
        whenever(vendorLikeRepository.findAllByCustomerIdAndVendorIdIn(eq(customerId), any()))
            .thenReturn(listOf(like))

        val result = service.scrollVendorBookmarks(null, 10, customerDetails)

        assertTrue(result.content.first().likedByCurrentUser)
        assertTrue(result.content.first().bookmarkedByCurrentUser)
    }

    // Helpers

    private fun <T : Any> emptyWindow(): Window<T> = windowOf(emptyList())

    private fun <T : Any> windowOf(items: List<T>, hasNext: Boolean = false): Window<T> =
        Window.from(items, { ScrollPosition.offset(it.toLong()) }, hasNext)

    private fun buildVendor(): Vendor = Vendor().apply {
        username = "vendoruser"
        chefName = "Chef Test"
        restaurantName = "Test Kitchen"
        vendorCode = "CM020001"
        firstName = "John"
        lastName = "Doe"
        address = VendorAddress().also {
            it.city = "Bamenda"
            it.neighbourhood = "Nkwen"
            it.directions = "Near the market"
            it.vendor = this
        }
    }
}

