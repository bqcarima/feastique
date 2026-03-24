package com.qinet.feastique.service.consumables

import com.qinet.feastique.exception.DuplicateFoundException
import com.qinet.feastique.exception.RequestedEntityNotFoundException
import com.qinet.feastique.exception.UserNotFoundException
import com.qinet.feastique.model.dto.FoodAvailabilityDto
import com.qinet.feastique.model.dto.ImageDto
import com.qinet.feastique.model.dto.ItemAvailabilityDto
import com.qinet.feastique.model.dto.SizeAvailabilityDto
import com.qinet.feastique.model.dto.consumables.AddOnDto
import com.qinet.feastique.model.dto.consumables.ComplementDto
import com.qinet.feastique.model.dto.consumables.FoodDto
import com.qinet.feastique.model.dto.consumables.FoodSizeDto
import com.qinet.feastique.model.entity.consumables.addOn.AddOn
import com.qinet.feastique.model.entity.consumables.addOn.FoodAddOn
import com.qinet.feastique.model.entity.consumables.complement.Complement
import com.qinet.feastique.model.entity.consumables.complement.FoodComplement
import com.qinet.feastique.model.entity.consumables.food.Food
import com.qinet.feastique.model.entity.menu.Menu
import com.qinet.feastique.model.entity.size.FoodSize
import com.qinet.feastique.model.entity.user.Vendor
import com.qinet.feastique.model.enums.Availability
import com.qinet.feastique.model.enums.OrderType
import com.qinet.feastique.model.enums.Size
import com.qinet.feastique.repository.bookmark.FoodBookmarkRepository
import com.qinet.feastique.repository.consumables.addOn.AddOnRepository
import com.qinet.feastique.repository.consumables.complement.ComplementRepository
import com.qinet.feastique.repository.consumables.food.FoodRepository
import com.qinet.feastique.repository.discount.DiscountRepository
import com.qinet.feastique.repository.like.FoodLikeRepository
import com.qinet.feastique.repository.menu.MenuRepository
import com.qinet.feastique.repository.user.VendorRepository
import com.qinet.feastique.security.UserSecurity
import com.qinet.feastique.utility.CursorEncoder
import com.qinet.feastique.utility.DuplicateUtility
import com.qinet.feastique.utility.SecurityUtility
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*
import org.springframework.data.domain.*
import org.springframework.security.core.authority.SimpleGrantedAuthority
import java.time.LocalTime
import java.util.*

class FoodServiceTest {

    private val foodRepository: FoodRepository = mock()
    private val vendorRepository: VendorRepository = mock()
    private val menuRepository: MenuRepository = mock()
    private val duplicateUtility: DuplicateUtility = mock()
    private val discountRepository: DiscountRepository = mock()
    private val complementRepository: ComplementRepository = mock()
    private val addOnRepository: AddOnRepository = mock()
    private val foodLikeRepository: FoodLikeRepository = mock()
    private val securityUtility: SecurityUtility = mock()
    private val cursorEncoder: CursorEncoder = mock()
    private val foodBookmarkRepository: FoodBookmarkRepository = mock()

    private val foodService = FoodService(
        foodRepository,
        vendorRepository,
        menuRepository,
        duplicateUtility,
        discountRepository,
        complementRepository,
        addOnRepository,
        foodLikeRepository,
        securityUtility,
        cursorEncoder,
        foodBookmarkRepository
    )

    private val vendorId: UUID = UUID.randomUUID()
    private val foodId: UUID = UUID.randomUUID()
    private val customerId: UUID = UUID.randomUUID()

    // Initialized at class level so makeFood can reference it before @BeforeEach
    private val vendor: Vendor = Vendor().apply {
        id = vendorId
        username = "testvendor"
        firstName = "John"
        lastName = "Doe"
        chefName = "Chef John"
        password = "encoded"
        openingTime = LocalTime.of(8, 0)
        closingTime = LocalTime.of(22, 0)
    }

    private lateinit var vendorDetails: UserSecurity
    private lateinit var customerDetails: UserSecurity

    @BeforeEach
    fun setupUserSecurity() {
        vendorDetails = UserSecurity(
            id = vendorId,
            username = "vendor",
            password = "",
            mutableListOf(SimpleGrantedAuthority("ROLE_VENDOR"))
        )

        customerDetails = UserSecurity(
            id = UUID.randomUUID(),
            username = "customer",
            password = "",
            mutableListOf(SimpleGrantedAuthority("ROLE_CUSTOMER"))
        )
    }

    @BeforeEach
    fun setUp() {
        vendorDetails = UserSecurity(
            id = vendorId,
            username = "testvendor",
            password = "",
            mutableListOf(SimpleGrantedAuthority("ROLE_VENDOR"))
        )

        customerDetails = UserSecurity(
            id = customerId,
            username = "testcustomer",
            password = "",
            mutableListOf(SimpleGrantedAuthority("ROLE_CUSTOMER"))
        )
    }

    // getFood (vendor role)

    @Test
    fun `getFood returns response for vendor when food belongs to them`() {
        val food = makeFood(foodId, "Jollof Rice")
        whenever(securityUtility.getSingleRole(vendorDetails)).thenReturn("VENDOR")
        whenever(foodRepository.findByIdAndVendorIdAndIsActiveTrue(foodId, vendorId)).thenReturn(food)

        val result = foodService.getFood(foodId, vendorDetails)

        assertThat(result.id).isEqualTo(foodId)
        assertThat(result.name).isEqualTo("Jollof Rice")
    }

    @Test
    fun `getFood throws RequestedEntityNotFoundException for vendor when food not found`() {
        whenever(securityUtility.getSingleRole(vendorDetails)).thenReturn("VENDOR")
        whenever(foodRepository.findByIdAndVendorIdAndIsActiveTrue(foodId, vendorId)).thenReturn(null)

        assertThatThrownBy { foodService.getFood(foodId, vendorDetails) }
            .isInstanceOf(RequestedEntityNotFoundException::class.java)
    }

    // getFood (customer role)

    @Test
    fun `getFood returns liked and bookmarked true for customer when both exist`() {
        val food = makeFood(foodId, "Fried Rice")
        whenever(securityUtility.getSingleRole(customerDetails)).thenReturn("CUSTOMER")
        whenever(foodLikeRepository.existsByFoodIdAndCustomerId(foodId, customerId)).thenReturn(true)
        whenever(foodBookmarkRepository.existsByFoodIdAndCustomerId(foodId, customerId)).thenReturn(true)
        whenever(foodRepository.findById(foodId)).thenReturn(Optional.of(food))

        val result = foodService.getFood(foodId, customerDetails)

        assertThat(result.likedByCurrentUser).isTrue()
        assertThat(result.bookmarkedByCurrentUser).isTrue()
    }

    @Test
    fun `getFood returns liked false and bookmarked false for customer when neither exist`() {
        val food = makeFood(foodId, "Fried Rice")
        whenever(securityUtility.getSingleRole(customerDetails)).thenReturn("CUSTOMER")
        whenever(foodLikeRepository.existsByFoodIdAndCustomerId(foodId, customerId)).thenReturn(false)
        whenever(foodBookmarkRepository.existsByFoodIdAndCustomerId(foodId, customerId)).thenReturn(false)
        whenever(foodRepository.findById(foodId)).thenReturn(Optional.of(food))

        val result = foodService.getFood(foodId, customerDetails)

        assertThat(result.likedByCurrentUser).isFalse()
        assertThat(result.bookmarkedByCurrentUser).isFalse()
    }

    @Test
    fun `getFood throws RequestedEntityNotFoundException for customer when food not found`() {
        whenever(securityUtility.getSingleRole(customerDetails)).thenReturn("CUSTOMER")
        whenever(foodLikeRepository.existsByFoodIdAndCustomerId(foodId, customerId)).thenReturn(false)
        whenever(foodBookmarkRepository.existsByFoodIdAndCustomerId(foodId, customerId)).thenReturn(false)
        whenever(foodRepository.findById(foodId)).thenReturn(Optional.empty())

        assertThatThrownBy { foodService.getFood(foodId, customerDetails) }
            .isInstanceOf(RequestedEntityNotFoundException::class.java)
    }

    // getFoodById

    @Test
    fun `getFoodById returns food when found`() {
        val food = makeFood(foodId, "Egusi Soup")
        whenever(foodRepository.findByIdAndVendorIdAndIsActiveTrue(foodId, vendorId)).thenReturn(food)

        val result = foodService.getFoodById(foodId, vendorDetails)

        assertThat(result.id).isEqualTo(foodId)
        assertThat(result.name).isEqualTo("Egusi Soup")
    }

    @Test
    fun `getFoodById throws RequestedEntityNotFoundException when not found`() {
        whenever(foodRepository.findByIdAndVendorIdAndIsActiveTrue(foodId, vendorId)).thenReturn(null)

        assertThatThrownBy { foodService.getFoodById(foodId, vendorDetails) }
            .isInstanceOf(RequestedEntityNotFoundException::class.java)
    }

    // getAllFoods

    @Test
    fun `getAllFoods returns paged responses`() {
        val food = makeFood(foodId, "Puff Puff")
        whenever(foodRepository.findAllByVendorIdAndIsActiveTrue(eq(vendorId), any<Pageable>()))
            .thenReturn(PageImpl(listOf(food)))

        val result = foodService.getAllFoods(vendorDetails, 0, 10)

        assertThat(result.content).hasSize(1)
        assertThat(result.content[0].name).isEqualTo("Puff Puff")
    }

    @Test
    fun `getAllFoods returns empty page when vendor has no foods`() {
        whenever(foodRepository.findAllByVendorIdAndIsActiveTrue(eq(vendorId), any<Pageable>()))
            .thenReturn(PageImpl(emptyList()))

        val result = foodService.getAllFoods(vendorDetails, 0, 10)

        assertThat(result.content).isEmpty()
    }

    // scrollFoods

    @Test
    fun `scrollFoods returns empty window when no foods exist`() {
        val emptyWindow = Window.from(emptyList<Food>()) { ScrollPosition.offset(it.toLong()) }
        whenever(securityUtility.getSingleRole(vendorDetails)).thenReturn("VENDOR")
        whenever(foodRepository.findAllByVendorIdAndIsActiveTrue(
            eq(vendorId), any<ScrollPosition>(), any<Sort>(), any<Limit>()
        )).thenReturn(emptyWindow)

        val result = foodService.scrollFoods(vendorId, null, 10, vendorDetails)

        assertThat(result.content).isEmpty()
        assertThat(result.hasNext).isFalse()
        assertThat(result.nextCursor).isNull()
    }

    @Test
    fun `scrollFoods returns mapped food responses for vendor`() {
        val food = makeFood(foodId, "Pepper Soup")
        val window = Window.from(listOf(food)) { ScrollPosition.offset(it.toLong()) }
        whenever(securityUtility.getSingleRole(vendorDetails)).thenReturn("VENDOR")
        whenever(foodRepository.findAllByVendorIdAndIsActiveTrue(
            eq(vendorId), any<ScrollPosition>(), any<Sort>(), any<Limit>()
        )).thenReturn(window)
        whenever(cursorEncoder.encodeOffset(any())).thenReturn("dummyCursor")

        val result = foodService.scrollFoods(vendorId, null, 10, vendorDetails)

        assertThat(result.content).hasSize(1)
        assertThat(result.content[0].name).isEqualTo("Pepper Soup")
    }

    @Test
    fun `scrollFoods resolves like and bookmark status for customer`() {
        val food = makeFood(foodId, "Suya")
        val window = Window.from(listOf(food)) { ScrollPosition.offset(it.toLong()) }
        whenever(securityUtility.getSingleRole(customerDetails)).thenReturn("CUSTOMER")
        whenever(foodRepository.findAllByVendorIdAndIsActiveTrue(
            eq(vendorId), any<ScrollPosition>(), any<Sort>(), any<Limit>()
        )).thenReturn(window)
        whenever(foodLikeRepository.findAllByCustomerIdAndFoodIdIn(eq(customerId), any()))
            .thenReturn(emptyList())
        whenever(foodBookmarkRepository.findAllByCustomerIdAndFoodIdIn(eq(customerId), any()))
            .thenReturn(emptyList())
        whenever(cursorEncoder.encodeOffset(any())).thenReturn("dummyCursor")

        val result = foodService.scrollFoods(vendorId, null, 10, customerDetails)

        assertThat(result.content).hasSize(1)
        assertThat(result.content[0].likedByCurrentUser).isFalse()
        assertThat(result.content[0].bookmarkedByCurrentUser).isFalse()
    }

    @Test
    fun `scrollFoods with numeric cursor calls repository`() {
        val window = Window.from(emptyList<Food>()) { ScrollPosition.offset(it.toLong()) }
        whenever(securityUtility.getSingleRole(vendorDetails)).thenReturn("VENDOR")
        whenever(foodRepository.findAllByVendorIdAndIsActiveTrue(
            eq(vendorId), any<ScrollPosition>(), any<Sort>(), any<Limit>()
        )).thenReturn(window)

        foodService.scrollFoods(vendorId, "5", 10, vendorDetails)

        verify(foodRepository).findAllByVendorIdAndIsActiveTrue(
            eq(vendorId), any<ScrollPosition>(), any<Sort>(), any<Limit>()
        )
    }

    // deleteFood

    @Test
    fun `deleteFood soft-deletes by setting isActive to false`() {
        val food = makeFood(foodId, "Akara")
        whenever(foodRepository.findByIdAndVendorIdAndIsActiveTrue(foodId, vendorId)).thenReturn(food)
        whenever(foodRepository.saveAndFlush(food)).thenReturn(food)

        foodService.deleteFood(foodId, vendorDetails)

        assertThat(food.isActive).isFalse()
        verify(foodRepository).saveAndFlush(food)
    }

    @Test
    fun `deleteFood throws when food not found`() {
        whenever(foodRepository.findByIdAndVendorIdAndIsActiveTrue(foodId, vendorId)).thenReturn(null)

        assertThatThrownBy { foodService.deleteFood(foodId, vendorDetails) }
            .isInstanceOf(RequestedEntityNotFoundException::class.java)

        verify(foodRepository, never()).saveAndFlush(any())
    }

    // changeFoodAvailability

    @Test
    fun `changeFoodAvailability updates food availability`() {
        val food = makeFood(foodId, "Moi Moi")
        food.availability = Availability.UNAVAILABLE
        val dto = FoodAvailabilityDto(
            foodId = foodId,
            availability = "AVAILABLE",
            addOns = null,
            complements = null,
            foodSizes = null
        )
        whenever(foodRepository.findByIdAndVendorIdAndIsActiveTrue(foodId, vendorId)).thenReturn(food)
        whenever(foodRepository.saveAndFlush(food)).thenReturn(food)

        foodService.changeFoodAvailability(dto, foodId, vendorDetails)

        assertThat(food.availability).isEqualTo(Availability.AVAILABLE)
    }

    @Test
    fun `changeFoodAvailability does not change availability when value is unchanged`() {
        val food = makeFood(foodId, "Moi Moi")
        food.availability = Availability.AVAILABLE
        val dto = FoodAvailabilityDto(
            foodId = foodId,
            availability = "AVAILABLE",
            addOns = null,
            complements = null,
            foodSizes = null
        )
        whenever(foodRepository.findByIdAndVendorIdAndIsActiveTrue(foodId, vendorId)).thenReturn(food)
        whenever(foodRepository.saveAndFlush(food)).thenReturn(food)

        foodService.changeFoodAvailability(dto, foodId, vendorDetails)

        assertThat(food.availability).isEqualTo(Availability.AVAILABLE)
    }

    @Test
    fun `changeFoodAvailability updates add-on availability`() {
        val addOnId = UUID.randomUUID()
        val addOn = AddOn().apply {
            id = addOnId
            name = "Extra Sauce"
            vendor = this@FoodServiceTest.vendor
            availability = Availability.AVAILABLE
        }
        val foodAddOn = FoodAddOn().apply {
            this.addOn = addOn
        }

        val food = makeFood(foodId, "Grilled Chicken")
        food.foodAddOns = mutableSetOf(foodAddOn)
        foodAddOn.food = food

        val addOnDto = ItemAvailabilityDto(itemId = addOnId, availability = "UNAVAILABLE")
        val dto = FoodAvailabilityDto(
            foodId = foodId,
            availability = "AVAILABLE",
            addOns = setOf(addOnDto),
            complements = null,
            foodSizes = null
        )

        whenever(foodRepository.findByIdAndVendorIdAndIsActiveTrue(foodId, vendorId)).thenReturn(food)
        whenever(foodRepository.saveAndFlush(food)).thenReturn(food)
        whenever(addOnRepository.saveAllAndFlush(any<Collection<AddOn>>())).thenReturn(emptyList())

        foodService.changeFoodAvailability(dto, foodId, vendorDetails)

        assertThat(addOn.availability).isEqualTo(Availability.UNAVAILABLE)
    }

    @Test
    fun `changeFoodAvailability updates complement availability`() {
        val complementId = UUID.randomUUID()
        val complement = Complement().apply {
            id = complementId
            name = "Salad"
            vendor = this@FoodServiceTest.vendor
            availability = Availability.AVAILABLE
        }
        val foodComplement = FoodComplement().apply {
            this.complement = complement
        }

        val food = makeFood(foodId, "Grilled Fish")
        food.foodComplements = mutableSetOf(foodComplement)
        foodComplement.food = food

        val complementDto = ItemAvailabilityDto(itemId = complementId, availability = "UNAVAILABLE")
        val dto = FoodAvailabilityDto(
            foodId = foodId,
            availability = "AVAILABLE",
            addOns = null,
            complements = setOf(complementDto),
            foodSizes = null
        )

        whenever(foodRepository.findByIdAndVendorIdAndIsActiveTrue(foodId, vendorId)).thenReturn(food)
        whenever(foodRepository.saveAndFlush(food)).thenReturn(food)
        whenever(complementRepository.saveAllAndFlush(any<Collection<Complement>>())).thenReturn(emptyList())

        foodService.changeFoodAvailability(dto, foodId, vendorDetails)

        assertThat(complement.availability).isEqualTo(Availability.UNAVAILABLE)
    }

    @Test
    fun `changeFoodAvailability updates food size availability`() {
        val sizeId = UUID.randomUUID()
        val foodSize = FoodSize().apply {
            id = sizeId
            size = Size.LARGE
            name = "Large"
            priceIncrease = 200L
            availability = Availability.AVAILABLE
        }

        val food = makeFood(foodId, "Banga Soup")
        food.foodSizes = mutableSetOf(foodSize)
        foodSize.food = food

        val sizeDto = SizeAvailabilityDto(sizeId = sizeId, availability = "UNAVAILABLE")
        val dto = FoodAvailabilityDto(
            foodId = foodId,
            availability = "AVAILABLE",
            addOns = null,
            complements = null,
            foodSizes = setOf(sizeDto)
        )

        whenever(foodRepository.findByIdAndVendorIdAndIsActiveTrue(foodId, vendorId)).thenReturn(food)
        whenever(foodRepository.saveAndFlush(food)).thenReturn(food)

        foodService.changeFoodAvailability(dto, foodId, vendorDetails)

        assertThat(foodSize.availability).isEqualTo(Availability.UNAVAILABLE)
    }

    @Test
    fun `changeFoodAvailability throws when food not found`() {
        val dto = FoodAvailabilityDto(
            foodId = foodId,
            availability = "AVAILABLE",
            addOns = null,
            complements = null,
            foodSizes = null
        )
        whenever(foodRepository.findByIdAndVendorIdAndIsActiveTrue(foodId, vendorId)).thenReturn(null)

        assertThatThrownBy { foodService.changeFoodAvailability(dto, foodId, vendorDetails) }
            .isInstanceOf(RequestedEntityNotFoundException::class.java)
    }

    // addOrUpdateFood (create path)

    @Test
    fun `addOrUpdateFood creates new food successfully`() {
        val dto = makeCreateDto("Pounded Yam")
        val savedFood = makeFood(foodId, "Pounded Yam")
        whenever(vendorRepository.findById(vendorId)).thenReturn(Optional.of(vendor))
        whenever(duplicateUtility.isDuplicateFoodFound("Pounded Yam", vendorId)).thenReturn(false)
        whenever(foodRepository.findTopOrderByFoodNumberDescWithLock()).thenReturn(emptyList())
        whenever(foodRepository.saveAndFlush(any())).thenAnswer { it.arguments[0] }
        whenever(addOnRepository.saveAndFlush(any())).thenAnswer { it.arguments[0] }
        whenever(complementRepository.saveAndFlush(any())).thenAnswer { it.arguments[0] }
        whenever(menuRepository.save(any())).thenAnswer { it.arguments[0] }
        whenever(foodRepository.findByIdWithAllRelationsAndIsActiveTrue(any()))
            .thenReturn(Optional.of(savedFood))

        val result = foodService.addOrUpdateFood(dto, vendorDetails)

        assertThat(result.name).isEqualTo("Pounded Yam")
        verify(foodRepository, atLeastOnce()).saveAndFlush(any())
    }

    @Test
    fun `addOrUpdateFood throws DuplicateFoundException when name already exists`() {
        val dto = makeCreateDto("Pounded Yam")
        whenever(vendorRepository.findById(vendorId)).thenReturn(Optional.of(vendor))
        whenever(duplicateUtility.isDuplicateFoodFound("Pounded Yam", vendorId)).thenReturn(true)

        assertThatThrownBy { foodService.addOrUpdateFood(dto, vendorDetails) }
            .isInstanceOf(DuplicateFoundException::class.java)

        verify(foodRepository, never()).saveAndFlush(any())
    }

    @Test
    fun `addOrUpdateFood throws UserNotFoundException when vendor not found`() {
        val dto = makeCreateDto("Pounded Yam")
        whenever(vendorRepository.findById(vendorId)).thenReturn(Optional.empty())

        assertThatThrownBy { foodService.addOrUpdateFood(dto, vendorDetails) }
            .isInstanceOf(UserNotFoundException::class.java)
    }

    @Test
    fun `addOrUpdateFood throws IllegalArgumentException when no order types provided`() {
        val dto = makeCreateDto("Pounded Yam").copy(orderTypes = emptySet())

        whenever(vendorRepository.findById(vendorId)).thenReturn(Optional.of(vendor))
        whenever(duplicateUtility.isDuplicateFoodFound("Pounded Yam", vendorId)).thenReturn(false)
        whenever(foodRepository.findTopOrderByFoodNumberDescWithLock()).thenReturn(emptyList())

        whenever(foodRepository.saveAndFlush(any())).thenAnswer { it.arguments[0] }
        whenever(addOnRepository.saveAndFlush(any())).thenAnswer { it.arguments[0] } // FIX
        whenever(complementRepository.saveAndFlush(any())).thenAnswer { it.arguments[0] }

        assertThatThrownBy { foodService.addOrUpdateFood(dto, vendorDetails) }
            .isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun `addOrUpdateFood throws IllegalArgumentException when no available days provided`() {
        val dto = makeCreateDto("Pounded Yam").copy(availableDays = emptySet())

        whenever(vendorRepository.findById(vendorId)).thenReturn(Optional.of(vendor))
        whenever(duplicateUtility.isDuplicateFoodFound("Pounded Yam", vendorId)).thenReturn(false)
        whenever(foodRepository.findTopOrderByFoodNumberDescWithLock()).thenReturn(emptyList())

        whenever(foodRepository.saveAndFlush(any())).thenAnswer { it.arguments[0] }
        whenever(addOnRepository.saveAndFlush(any())).thenAnswer { it.arguments[0] } // FIX
        whenever(complementRepository.saveAndFlush(any())).thenAnswer { it.arguments[0] }

        assertThatThrownBy { foodService.addOrUpdateFood(dto, vendorDetails) }
            .isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun `addOrUpdateFood throws IllegalArgumentException when no food sizes provided`() {
        val dto = makeCreateDto("Pounded Yam").copy(foodSizes = emptySet())

        whenever(vendorRepository.findById(vendorId)).thenReturn(Optional.of(vendor))
        whenever(duplicateUtility.isDuplicateFoodFound("Pounded Yam", vendorId)).thenReturn(false)
        whenever(foodRepository.findTopOrderByFoodNumberDescWithLock()).thenReturn(emptyList())

        whenever(foodRepository.saveAndFlush(any())).thenAnswer { it.arguments[0] }
        whenever(addOnRepository.saveAndFlush(any())).thenAnswer { it.arguments[0] } // FIX
        whenever(complementRepository.saveAndFlush(any())).thenAnswer { it.arguments[0] }

        assertThatThrownBy { foodService.addOrUpdateFood(dto, vendorDetails) }
            .isInstanceOf(IllegalArgumentException::class.java)

        verify(foodRepository, atLeastOnce()).saveAndFlush(any())

        // Optional (only valid if you moved validation early)
        // verify(addOnRepository, never()).saveAndFlush(any())
        // verify(complementRepository, never()).saveAndFlush(any())
    }

    @Test
    fun `addOrUpdateFood throws IllegalArgumentException when no complements provided`() {
        val dto = makeCreateDto("Pounded Yam").copy(complements = emptySet())

        whenever(vendorRepository.findById(vendorId)).thenReturn(Optional.of(vendor))
        whenever(duplicateUtility.isDuplicateFoodFound("Pounded Yam", vendorId)).thenReturn(false)
        whenever(foodRepository.findTopOrderByFoodNumberDescWithLock()).thenReturn(emptyList())

        whenever(foodRepository.saveAndFlush(any())).thenAnswer { it.arguments[0] }
        whenever(addOnRepository.saveAndFlush(any())).thenAnswer { it.arguments[0] } // FIX

        assertThatThrownBy { foodService.addOrUpdateFood(dto, vendorDetails) }
            .isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun `addOrUpdateFood throws IllegalArgumentException when fewer than two images provided`() {
        val dto = makeCreateDto("Pounded Yam").copy(
            foodImages = setOf(ImageDto(imageUrl = "img1.jpg"))
        )

        whenever(vendorRepository.findById(vendorId)).thenReturn(Optional.of(vendor))
        whenever(duplicateUtility.isDuplicateFoodFound("Pounded Yam", vendorId)).thenReturn(false)
        whenever(foodRepository.findTopOrderByFoodNumberDescWithLock()).thenReturn(emptyList())

        whenever(foodRepository.saveAndFlush(any())).thenAnswer { it.arguments[0] }
        whenever(addOnRepository.saveAndFlush(any())).thenAnswer { it.arguments[0] } // FIX
        whenever(complementRepository.saveAndFlush(any())).thenAnswer { it.arguments[0] }

        assertThatThrownBy { foodService.addOrUpdateFood(dto, vendorDetails) }
            .isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun `addOrUpdateFood throws IllegalArgumentException when no images provided`() {
        val dto = makeCreateDto("Pounded Yam").copy(foodImages = emptySet())

        whenever(vendorRepository.findById(vendorId)).thenReturn(Optional.of(vendor))
        whenever(duplicateUtility.isDuplicateFoodFound("Pounded Yam", vendorId)).thenReturn(false)
        whenever(foodRepository.findTopOrderByFoodNumberDescWithLock()).thenReturn(emptyList())

        whenever(foodRepository.saveAndFlush(any())).thenAnswer { it.arguments[0] }
        whenever(addOnRepository.saveAndFlush(any())).thenAnswer { it.arguments[0] } // FIX
        whenever(complementRepository.saveAndFlush(any())).thenAnswer { it.arguments[0] }

        assertThatThrownBy { foodService.addOrUpdateFood(dto, vendorDetails) }
            .isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun `addOrUpdateFood generates sequential food number on create`() {
        val dto = makeCreateDto("Ofe Onugbu")
        val lastFood = makeFood(UUID.randomUUID(), "Previous Food").apply { foodNumber = "FD-00001" }
        val savedFood = makeFood(foodId, "Ofe Onugbu").apply { foodNumber = "FD-00002" }

        whenever(vendorRepository.findById(vendorId)).thenReturn(Optional.of(vendor))
        whenever(duplicateUtility.isDuplicateFoodFound("Ofe Onugbu", vendorId)).thenReturn(false)
        whenever(foodRepository.findTopOrderByFoodNumberDescWithLock()).thenReturn(listOf(lastFood))
        whenever(foodRepository.saveAndFlush(any())).thenAnswer { it.arguments[0] }
        whenever(addOnRepository.saveAndFlush(any())).thenAnswer { it.arguments[0] }
        whenever(complementRepository.saveAndFlush(any())).thenAnswer { it.arguments[0] }
        whenever(menuRepository.save(any())).thenAnswer { it.arguments[0] }
        whenever(foodRepository.findByIdWithAllRelationsAndIsActiveTrue(any()))
            .thenReturn(Optional.of(savedFood))

        foodService.addOrUpdateFood(dto, vendorDetails)

        val captor = argumentCaptor<Food>()
        verify(foodRepository, atLeastOnce()).saveAndFlush(captor.capture())
        val firstSavedFood = captor.allValues.first()
        assertThat(firstSavedFood.foodNumber).isEqualTo("FD-00002")
    }

    // addOrUpdateFood (update path)

    @Test
    fun `addOrUpdateFood updates existing food when id provided`() {
        val existingFood = makeFood(foodId, "Old Name").apply outer@{
            menu = Menu().apply { this.food = this@outer }
        }
        val savedFood = makeFood(foodId, "New Name")
        val dto = makeCreateDto("New Name").copy(id = foodId)

        whenever(vendorRepository.findById(vendorId)).thenReturn(Optional.of(vendor))
        whenever(foodRepository.findByIdAndVendorIdAndIsActiveTrue(foodId, vendorId)).thenReturn(existingFood)
        whenever(foodRepository.saveAndFlush(any())).thenAnswer { it.arguments[0] }
        whenever(addOnRepository.saveAndFlush(any())).thenAnswer { it.arguments[0] }
        whenever(complementRepository.saveAndFlush(any())).thenAnswer { it.arguments[0] }
        whenever(menuRepository.findById(any())).thenReturn(Optional.of(existingFood.menu))
        whenever(menuRepository.save(any())).thenAnswer { it.arguments[0] }
        whenever(foodRepository.findByIdWithAllRelationsAndIsActiveTrue(any()))
            .thenReturn(Optional.of(savedFood))

        val result = foodService.addOrUpdateFood(dto, vendorDetails)

        assertThat(result.name).isEqualTo("New Name")
    }

    @Test
    fun `addOrUpdateFood throws RequestedEntityNotFoundException when updating non-existent food`() {
        val dto = makeCreateDto("Ghost Food").copy(id = foodId)
        whenever(vendorRepository.findById(vendorId)).thenReturn(Optional.of(vendor))
        whenever(foodRepository.findByIdAndVendorIdAndIsActiveTrue(foodId, vendorId)).thenReturn(null)

        assertThatThrownBy { foodService.addOrUpdateFood(dto, vendorDetails) }
            .isInstanceOf(RequestedEntityNotFoundException::class.java)
    }

    @Test
    fun `addOrUpdateFood throws DuplicateFoundException when updated name already exists for vendor`() {
        val existingFood = makeFood(foodId, "Old Name").apply outer@{
            menu = Menu().apply { this.food = this@outer }
        }
        val dto = makeCreateDto("Taken Name").copy(id = foodId)

        whenever(vendorRepository.findById(vendorId)).thenReturn(Optional.of(vendor))
        whenever(foodRepository.findByIdAndVendorIdAndIsActiveTrue(foodId, vendorId)).thenReturn(existingFood)
        whenever(duplicateUtility.isDuplicateFoodFound("Taken Name", vendorId)).thenReturn(true)

        assertThatThrownBy { foodService.addOrUpdateFood(dto, vendorDetails) }
            .isInstanceOf(DuplicateFoundException::class.java)
    }

    // helpers

    private fun makeFood(id: UUID, name: String): Food {
        return Food().apply {
            this.id = id
            this.name = name
            this.vendor = this@FoodServiceTest.vendor
            this.isActive = true
            this.availability = Availability.AVAILABLE
            this.mainCourse = "Main"
            this.description = "A delicious food"
            this.basePrice = 2000L
            this.preparationTime = 15
            this.deliverable = false
            this.deliveryFee = 0L
            this.orderTypes = mutableSetOf(OrderType.DINE_IN)
            this.availableDays = mutableSetOf()
            this.foodImages = mutableSetOf()
            this.foodAddOns = mutableSetOf()
            this.foodComplements = mutableSetOf()
            this.foodDiscounts = mutableSetOf()
            this.foodSizes = mutableSetOf()
        }
    }

    private fun makeCreateDto(name: String): FoodDto {
        val sizeDto = FoodSizeDto(
            id = null,
            size = "MEDIUM",
            sizeName = "Regular",
            priceIncrease = 0L,
            availability = "AVAILABLE"
        )
        val complementDto = ComplementDto(
            id = null,
            complementName = "Rice",
            price = 500L,
            availability = "AVAILABLE"
        )
        val addOnDto = AddOnDto(
            id = null,
            addOnName = "Extra Sauce",
            price = 200L,
            availability = "AVAILABLE"
        )
        return FoodDto(
            id = null,
            foodName = name,
            mainCourse = "Main Course",
            description = "A delicious food",
            basePrice = 2000L,
            availability = "AVAILABLE",
            deliverable = false,
            readyAsFrom = null,
            dailyDeliveryQuantity = null,
            preparationTime = 15,
            quickDelivery = false,
            deliveryTime = null,
            deliveryFee = 0L,
            foodImages = setOf(
                ImageDto(imageUrl = "img1.jpg"),
                ImageDto(imageUrl = "img2.jpg")
            ),
            foodSizes = setOf(sizeDto),
            complements = setOf(complementDto),
            addOns = setOf(addOnDto),
            orderTypes = setOf("DINE_IN"),
            availableDays = setOf("MONDAY"),
            discounts = emptySet()
        )
    }
}

