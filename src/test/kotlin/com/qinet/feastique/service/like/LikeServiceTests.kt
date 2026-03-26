package com.qinet.feastique.service.like

import com.qinet.feastique.exception.RequestedEntityNotFoundException
import com.qinet.feastique.model.entity.consumables.beverage.Beverage
import com.qinet.feastique.model.entity.consumables.dessert.Dessert
import com.qinet.feastique.model.entity.consumables.food.Food
import com.qinet.feastique.model.entity.consumables.handheld.Handheld
import com.qinet.feastique.model.entity.like.*
import com.qinet.feastique.model.entity.post.Post
import com.qinet.feastique.model.entity.user.Customer
import com.qinet.feastique.model.entity.user.Vendor
import com.qinet.feastique.model.enums.AccountType
import com.qinet.feastique.repository.consumables.beverage.BeverageRepository
import com.qinet.feastique.repository.consumables.dessert.DessertRepository
import com.qinet.feastique.repository.consumables.food.FoodRepository
import com.qinet.feastique.repository.consumables.handheld.HandheldRepository
import com.qinet.feastique.repository.like.*
import com.qinet.feastique.repository.post.PostRepository
import com.qinet.feastique.repository.user.CustomerRepository
import com.qinet.feastique.repository.user.VendorRepository
import com.qinet.feastique.security.UserSecurity
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.*
import org.springframework.security.core.authority.SimpleGrantedAuthority
import java.util.*

// Shared data

private val LS_CUSTOMER_ID: UUID = UUID.randomUUID()
private val LS_BEVERAGE_ID: UUID = UUID.randomUUID()
private val LS_DESSERT_ID: UUID = UUID.randomUUID()
private val LS_FOOD_ID: UUID = UUID.randomUUID()
private val LS_HANDHELD_ID: UUID = UUID.randomUUID()
private val LS_POST_ID: UUID = UUID.randomUUID()
private val LS_VENDOR_ID: UUID = UUID.randomUUID()

private fun lsCustomerSecurity(): UserSecurity = UserSecurity(
    id = LS_CUSTOMER_ID,
    username = "jane_doe",
    password = "hashed",
    userAuthorities = mutableListOf(SimpleGrantedAuthority("ROLE_CUSTOMER"))
)

private fun lsCustomer(): Customer = Customer().apply {
    id = LS_CUSTOMER_ID
    username = "jane_doe"
    accountType = AccountType.CUSTOMER
}

private fun lsBeverage(): Beverage = Beverage().apply { id = LS_BEVERAGE_ID }
private fun lsDessert(): Dessert = Dessert().apply { id = LS_DESSERT_ID }
private fun lsFood(): Food = Food().apply { id = LS_FOOD_ID }
private fun lsHandheld(): Handheld = Handheld().apply { id = LS_HANDHELD_ID }
private fun lsPost(): Post = Post().apply { id = LS_POST_ID }
private fun lsVendor(): Vendor = Vendor().apply { id = LS_VENDOR_ID }


class LikeServiceTest {

    private lateinit var postRepository: PostRepository
    private lateinit var beverageRepository: BeverageRepository
    private lateinit var foodRepository: FoodRepository
    private lateinit var dessertRepository: DessertRepository
    private lateinit var handheldRepository: HandheldRepository
    private lateinit var vendorRepository: VendorRepository
    private lateinit var customerRepository: CustomerRepository
    private lateinit var postLikeRepository: PostLikeRepository
    private lateinit var beverageLikeRepository: BeverageLikeRepository
    private lateinit var foodLikeRepository: FoodLikeRepository
    private lateinit var dessertLikeRepository: DessertLikeRepository
    private lateinit var handheldLikeRepository: HandheldLikeRepository
    private lateinit var vendorLikeRepository: VendorLikeRepository
    private lateinit var likeService: LikeService

    @BeforeEach
    fun setUp() {
        postRepository = mock()
        beverageRepository = mock()
        foodRepository = mock()
        dessertRepository = mock()
        handheldRepository = mock()
        vendorRepository = mock()
        customerRepository = mock()
        postLikeRepository = mock()
        beverageLikeRepository = mock()
        foodLikeRepository = mock()
        dessertLikeRepository = mock()
        handheldLikeRepository = mock()
        vendorLikeRepository = mock()

        likeService = LikeService(
            postRepository = postRepository,
            beverageRepository = beverageRepository,
            foodRepository = foodRepository,
            dessertRepository = dessertRepository,
            handheldRepository = handheldRepository,
            vendorRepository = vendorRepository,
            customerRepository = customerRepository,
            postLikeRepository = postLikeRepository,
            beverageLikeRepository = beverageLikeRepository,
            foodLikeRepository = foodLikeRepository,
            dessertLikeRepository = dessertLikeRepository,
            handheldLikeRepository = handheldLikeRepository,
            vendorLikeRepository = vendorLikeRepository
        )

        val customer = lsCustomer()
        whenever(customerRepository.getReferenceById(LS_CUSTOMER_ID)).thenReturn(customer)

        whenever(postLikeRepository.save(any())).thenAnswer { it.arguments[0] }
        whenever(beverageLikeRepository.save(any())).thenAnswer { it.arguments[0] }
        whenever(foodLikeRepository.save(any())).thenAnswer { it.arguments[0] }
        whenever(dessertLikeRepository.save(any())).thenAnswer { it.arguments[0] }
        whenever(handheldLikeRepository.save(any())).thenAnswer { it.arguments[0] }
        whenever(vendorLikeRepository.save(any())).thenAnswer { it.arguments[0] }
    }


    // likeOrUnlikePost
    @Nested
    inner class LikeOrUnlikePost {

        @Test
        fun `creates a PostLike when customer has not yet liked the post`() {
            val customerSecurity = lsCustomerSecurity()
            whenever(postLikeRepository.findByPostIdAndCustomerId(LS_POST_ID, LS_CUSTOMER_ID)).thenReturn(null)
            whenever(postRepository.findById(LS_POST_ID)).thenReturn(Optional.of(lsPost()))

            likeService.likeOrUnlikePost(LS_POST_ID, customerSecurity)

            verify(postLikeRepository).save(any())
            verify(postLikeRepository, never()).delete(any<PostLike>())
        }

        @Test
        fun `removes the PostLike when customer has already liked the post`() {
            val customerSecurity = lsCustomerSecurity()
            val existingLike = PostLike().apply {
                post = lsPost()
                customer = lsCustomer()
            }
            whenever(postLikeRepository.findByPostIdAndCustomerId(LS_POST_ID, LS_CUSTOMER_ID))
                .thenReturn(existingLike)

            likeService.likeOrUnlikePost(LS_POST_ID, customerSecurity)

            verify(postLikeRepository).delete(existingLike)
            verify(postLikeRepository, never()).save(any())
        }

        @Test
        fun `throws RequestedEntityNotFoundException when post does not exist`() {
            val customerSecurity = lsCustomerSecurity()
            whenever(postLikeRepository.findByPostIdAndCustomerId(LS_POST_ID, LS_CUSTOMER_ID)).thenReturn(null)
            whenever(postRepository.findById(LS_POST_ID)).thenReturn(Optional.empty())

            assertThrows<RequestedEntityNotFoundException> {
                likeService.likeOrUnlikePost(LS_POST_ID, customerSecurity)
            }
        }

        @Test
        fun `does not query post repository when unlike path is taken`() {
            val customerSecurity = lsCustomerSecurity()
            val existingLike = PostLike()
            whenever(postLikeRepository.findByPostIdAndCustomerId(LS_POST_ID, LS_CUSTOMER_ID))
                .thenReturn(existingLike)

            likeService.likeOrUnlikePost(LS_POST_ID, customerSecurity)

            verify(postRepository, never()).findById(any())
        }
    }


    // likeOrUnlikeBeverage
    @Nested
    inner class LikeOrUnlikeBeverage {

        @Test
        fun `creates a BeverageLike when customer has not yet liked the beverage`() {
            val customerSecurity = lsCustomerSecurity()
            whenever(beverageLikeRepository.findByBeverageIdAndCustomerId(LS_BEVERAGE_ID, LS_CUSTOMER_ID))
                .thenReturn(null)
            whenever(beverageRepository.findById(LS_BEVERAGE_ID)).thenReturn(Optional.of(lsBeverage()))

            likeService.likeOrUnlikeBeverage(LS_BEVERAGE_ID, customerSecurity)

            verify(beverageLikeRepository).save(any())
            verify(beverageLikeRepository, never()).delete(any<BeverageLike>())
        }

        @Test
        fun `removes the BeverageLike when customer has already liked the beverage`() {
            val customerSecurity = lsCustomerSecurity()
            val existingLike = BeverageLike().apply {
                beverage = lsBeverage()
                customer = lsCustomer()
            }
            whenever(beverageLikeRepository.findByBeverageIdAndCustomerId(LS_BEVERAGE_ID, LS_CUSTOMER_ID))
                .thenReturn(existingLike)

            likeService.likeOrUnlikeBeverage(LS_BEVERAGE_ID, customerSecurity)

            verify(beverageLikeRepository).delete(existingLike)
            verify(beverageLikeRepository, never()).save(any())
        }

        @Test
        fun `throws RequestedEntityNotFoundException when beverage does not exist`() {
            val customerSecurity = lsCustomerSecurity()
            whenever(beverageLikeRepository.findByBeverageIdAndCustomerId(LS_BEVERAGE_ID, LS_CUSTOMER_ID))
                .thenReturn(null)
            whenever(beverageRepository.findById(LS_BEVERAGE_ID)).thenReturn(Optional.empty())

            assertThrows<RequestedEntityNotFoundException> {
                likeService.likeOrUnlikeBeverage(LS_BEVERAGE_ID, customerSecurity)
            }
        }

        @Test
        fun `does not query beverage repository when unlike path is taken`() {
            val customerSecurity = lsCustomerSecurity()
            val existingLike = BeverageLike()
            whenever(beverageLikeRepository.findByBeverageIdAndCustomerId(LS_BEVERAGE_ID, LS_CUSTOMER_ID))
                .thenReturn(existingLike)

            likeService.likeOrUnlikeBeverage(LS_BEVERAGE_ID, customerSecurity)

            verify(beverageRepository, never()).findById(any())
        }
    }


    // likeOrUnlikeFood
    @Nested
    inner class LikeOrUnlikeFood {

        @Test
        fun `creates a FoodLike when customer has not yet liked the food`() {
            val customerSecurity = lsCustomerSecurity()
            whenever(foodLikeRepository.findByFoodIdAndCustomerId(LS_FOOD_ID, LS_CUSTOMER_ID)).thenReturn(null)
            whenever(foodRepository.findById(LS_FOOD_ID)).thenReturn(Optional.of(lsFood()))

            likeService.likeOrUnlikeFood(LS_FOOD_ID, customerSecurity)

            verify(foodLikeRepository).save(any())
            verify(foodLikeRepository, never()).delete(any<FoodLike>())
        }

        @Test
        fun `removes the FoodLike when customer has already liked the food`() {
            val customerSecurity = lsCustomerSecurity()
            val existingLike = FoodLike().apply {
                food = lsFood()
                customer = lsCustomer()
            }
            whenever(foodLikeRepository.findByFoodIdAndCustomerId(LS_FOOD_ID, LS_CUSTOMER_ID))
                .thenReturn(existingLike)

            likeService.likeOrUnlikeFood(LS_FOOD_ID, customerSecurity)

            verify(foodLikeRepository).delete(existingLike)
            verify(foodLikeRepository, never()).save(any())
        }

        @Test
        fun `throws RequestedEntityNotFoundException when food does not exist`() {
            val customerSecurity = lsCustomerSecurity()
            whenever(foodLikeRepository.findByFoodIdAndCustomerId(LS_FOOD_ID, LS_CUSTOMER_ID)).thenReturn(null)
            whenever(foodRepository.findById(LS_FOOD_ID)).thenReturn(Optional.empty())

            assertThrows<RequestedEntityNotFoundException> {
                likeService.likeOrUnlikeFood(LS_FOOD_ID, customerSecurity)
            }
        }

        @Test
        fun `does not query food repository when unlike path is taken`() {
            val customerSecurity = lsCustomerSecurity()
            val existingLike = FoodLike()
            whenever(foodLikeRepository.findByFoodIdAndCustomerId(LS_FOOD_ID, LS_CUSTOMER_ID))
                .thenReturn(existingLike)

            likeService.likeOrUnlikeFood(LS_FOOD_ID, customerSecurity)

            verify(foodRepository, never()).findById(any())
        }
    }


    // likeOrUnlikeDessert
    @Nested
    inner class LikeOrUnlikeDessert {

        @Test
        fun `creates a DessertLike when customer has not yet liked the dessert`() {
            val customerSecurity = lsCustomerSecurity()
            whenever(dessertLikeRepository.findByDessertIdAndCustomerId(LS_DESSERT_ID, LS_CUSTOMER_ID))
                .thenReturn(null)
            whenever(dessertRepository.findById(LS_DESSERT_ID)).thenReturn(Optional.of(lsDessert()))

            likeService.likeOrUnlikeDessert(LS_DESSERT_ID, customerSecurity)

            verify(dessertLikeRepository).save(any())
            verify(dessertLikeRepository, never()).delete(any<DessertLike>())
        }

        @Test
        fun `removes the DessertLike when customer has already liked the dessert`() {
            val customerSecurity = lsCustomerSecurity()
            val existingLike = DessertLike().apply {
                dessert = lsDessert()
                customer = lsCustomer()
            }
            whenever(dessertLikeRepository.findByDessertIdAndCustomerId(LS_DESSERT_ID, LS_CUSTOMER_ID))
                .thenReturn(existingLike)

            likeService.likeOrUnlikeDessert(LS_DESSERT_ID, customerSecurity)

            verify(dessertLikeRepository).delete(existingLike)
            verify(dessertLikeRepository, never()).save(any())
        }

        @Test
        fun `throws RequestedEntityNotFoundException when dessert does not exist`() {
            val customerSecurity = lsCustomerSecurity()
            whenever(dessertLikeRepository.findByDessertIdAndCustomerId(LS_DESSERT_ID, LS_CUSTOMER_ID))
                .thenReturn(null)
            whenever(dessertRepository.findById(LS_DESSERT_ID)).thenReturn(Optional.empty())

            assertThrows<RequestedEntityNotFoundException> {
                likeService.likeOrUnlikeDessert(LS_DESSERT_ID, customerSecurity)
            }
        }
    }


    // likeOrUnlikeHandheld
    @Nested
    inner class LikeOrUnlikeHandheld {

        @Test
        fun `creates a HandheldLike when customer has not yet liked the handheld`() {
            val customerSecurity = lsCustomerSecurity()
            whenever(handheldLikeRepository.findByHandheldIdAndCustomerId(LS_HANDHELD_ID, LS_CUSTOMER_ID))
                .thenReturn(null)
            whenever(handheldRepository.findById(LS_HANDHELD_ID)).thenReturn(Optional.of(lsHandheld()))

            likeService.likeOrUnlikeHandheld(LS_HANDHELD_ID, customerSecurity)

            verify(handheldLikeRepository).save(any())
            verify(handheldLikeRepository, never()).delete(any<HandheldLike>())
        }

        @Test
        fun `removes the HandheldLike when customer has already liked the handheld`() {
            val customerSecurity = lsCustomerSecurity()
            val existingLike = HandheldLike().apply {
                handheld = lsHandheld()
                customer = lsCustomer()
            }
            whenever(handheldLikeRepository.findByHandheldIdAndCustomerId(LS_HANDHELD_ID, LS_CUSTOMER_ID))
                .thenReturn(existingLike)

            likeService.likeOrUnlikeHandheld(LS_HANDHELD_ID, customerSecurity)

            verify(handheldLikeRepository).delete(existingLike)
            verify(handheldLikeRepository, never()).save(any())
        }

        @Test
        fun `throws RequestedEntityNotFoundException when handheld does not exist`() {
            val customerSecurity = lsCustomerSecurity()
            whenever(handheldLikeRepository.findByHandheldIdAndCustomerId(LS_HANDHELD_ID, LS_CUSTOMER_ID))
                .thenReturn(null)
            whenever(handheldRepository.findById(LS_HANDHELD_ID)).thenReturn(Optional.empty())

            assertThrows<RequestedEntityNotFoundException> {
                likeService.likeOrUnlikeHandheld(LS_HANDHELD_ID, customerSecurity)
            }
        }
    }


    // likeOrUnlikeVendor
    @Nested
    inner class LikeOrUnlikeVendor {

        @Test
        fun `creates a VendorLike when customer has not yet liked the vendor`() {
            val customerSecurity = lsCustomerSecurity()
            whenever(vendorLikeRepository.findByVendorIdAndCustomerId(LS_VENDOR_ID, LS_CUSTOMER_ID))
                .thenReturn(null)
            whenever(vendorRepository.findById(LS_VENDOR_ID)).thenReturn(Optional.of(lsVendor()))

            likeService.likeOrUnlikeVendor(LS_VENDOR_ID, customerSecurity)

            verify(vendorLikeRepository).save(any())
            verify(vendorLikeRepository, never()).delete(any<VendorLike>())
        }

        @Test
        fun `removes the VendorLike when customer has already liked the vendor`() {
            val customerSecurity = lsCustomerSecurity()
            val existingLike = VendorLike().apply {
                vendor = lsVendor()
                customer = lsCustomer()
            }
            whenever(vendorLikeRepository.findByVendorIdAndCustomerId(LS_VENDOR_ID, LS_CUSTOMER_ID))
                .thenReturn(existingLike)

            likeService.likeOrUnlikeVendor(LS_VENDOR_ID, customerSecurity)

            verify(vendorLikeRepository).delete(existingLike)
            verify(vendorLikeRepository, never()).save(any())
        }

        @Test
        fun `throws RequestedEntityNotFoundException when vendor does not exist`() {
            val customerSecurity = lsCustomerSecurity()
            whenever(vendorLikeRepository.findByVendorIdAndCustomerId(LS_VENDOR_ID, LS_CUSTOMER_ID))
                .thenReturn(null)
            whenever(vendorRepository.findById(LS_VENDOR_ID)).thenReturn(Optional.empty())

            assertThrows<RequestedEntityNotFoundException> {
                likeService.likeOrUnlikeVendor(LS_VENDOR_ID, customerSecurity)
            }
        }

        @Test
        fun `does not query vendor repository when unlike path is taken`() {
            val customerSecurity = lsCustomerSecurity()
            val existingLike = VendorLike()
            whenever(vendorLikeRepository.findByVendorIdAndCustomerId(LS_VENDOR_ID, LS_CUSTOMER_ID))
                .thenReturn(existingLike)

            likeService.likeOrUnlikeVendor(LS_VENDOR_ID, customerSecurity)

            verify(vendorRepository, never()).findById(any())
        }
    }
}

