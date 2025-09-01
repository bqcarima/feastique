package com.qinet.feastique.common.mapper

import com.qinet.feastique.model.entity.addOn.AddOn
import com.qinet.feastique.model.entity.addOn.FoodAddOn
import com.qinet.feastique.model.entity.addOn.OrderAddOn
import com.qinet.feastique.model.entity.address.Address
import com.qinet.feastique.model.entity.address.CustomerAddress
import com.qinet.feastique.model.entity.beverage.Beverage
import com.qinet.feastique.model.entity.beverage.OrderBeverage
import com.qinet.feastique.model.entity.complement.Complement
import com.qinet.feastique.model.entity.complement.FoodComplement
import com.qinet.feastique.model.entity.discount.Discount
import com.qinet.feastique.model.entity.discount.FoodDiscount
import com.qinet.feastique.model.entity.food.*
import com.qinet.feastique.model.entity.order.FoodOrder
import com.qinet.feastique.model.entity.phoneNumber.PhoneNumber
import com.qinet.feastique.model.entity.phoneNumber.VendorPhoneNumber
import com.qinet.feastique.model.entity.post.Post
import com.qinet.feastique.model.entity.user.Customer
import com.qinet.feastique.model.entity.user.Vendor
import com.qinet.feastique.model.enums.BeverageGroup
import com.qinet.feastique.model.enums.Size
import com.qinet.feastique.response.*
import com.qinet.feastique.response.address.AddressResponse
import com.qinet.feastique.response.address.CustomerAddressResponse
import com.qinet.feastique.response.food.*
import com.qinet.feastique.response.order.FoodOrderCustomerResponse
import com.qinet.feastique.response.order.FoodOrderResponse
import com.qinet.feastique.response.order.FoodOrderVendorResponse
import com.qinet.feastique.response.vendor.VendorMinimalResponse
import com.qinet.feastique.response.vendor.VendorResponse
import java.text.SimpleDateFormat
import java.util.*

/**
 * Maps an [AddOn] entity to its API DTO [AddOnResponse].
 *
 * @receiver `AddOn` entity to map from.
 * @return [AddOnResponse] DTO with id, name, and price.
 */
fun AddOn.toResponse() = AddOnResponse(
    id = this.id ?: 0,
    addOnName = this.addOnName.orEmpty(),
    price = this.price ?: 0
)

/**
 * Maps an [Address] entity to its API DTO [AddressResponse].
 *
 * @receiver `Address` entity to map from.
 * @return [Address] DTO with id, country, region, city, neighbourhood, street name, directions and location.
 */
fun Address.toResponse(): AddressResponse = AddressResponse(
    id = id ?: 0,
    country = country,
    region = region ?: "Not selected",
    city = city ?: "Not selected",
    neighbourhood = neighbourhood ?: "Not selected",
    streetName = streetName ?: "None",
    directions = directions ?: "None",
    location = listOf(longitude ?: "0.0", latitude ?: "0.0")
)

fun CustomerAddress.toResponse(): CustomerAddressResponse = CustomerAddressResponse(
    id = id ?: 0,
    country = country,
    region = region ?: "Not selected",
    city = city ?: "Not selected",
    neighbourhood = neighbourhood ?: "Not selected",
    streetName = streetName ?: "None",
    directions = directions ?: "None",
    location = listOf(longitude ?: "0.0", latitude ?: "0.0"),
    default = default ?: false
)

/**
 * Converts a [Beverage] entity to its API response DTO [BeverageResponse].
 *
 * @receiver `Beverage` entity to map from.
 * @return [BeverageResponse] DTO with id, name, and price.
 */
fun Beverage.toResponse() = BeverageResponse(
    id = id ?: 0,
    beverageName = beverageName.orEmpty(),
    alcoholic = alcoholic ?: false,
    beverageGroup = beverageName.orEmpty(),
    percentage = percentage ?: 0,
    price = price ?: 0,
    delivery = delivery ?: false,
)

/**
 * Converts a [Complement] entity to its API response DTO [ComplementResponse].
 *
 * @receiver `Complement entity` to map from.
 * @return [ComplementResponse] DTO with id, name, and price.
 */
fun Complement.toResponse() = ComplementResponse(
    id = id ?: 0,
    name = complementName.orEmpty(),
    price = price ?: 0
)

/**
 * Converts a [Customer] entity to its API response DTO [CustomerResponse].
 *
 * @receiver `Customer` entity to map from.
 * @return [CustomerResponse] DTO with id, name, etc.
 */
fun Customer.toResponse(): CustomerResponse = CustomerResponse(
    id = id ?: 0,
    username = username,
    firstName = firstName.orEmpty(),
    lastName = lastName.orEmpty(),
    dob = dob,
    phoneNumber = phoneNumber.map { it.toResponse() },
    address = address.map { it.toResponse() },
    anniversary = anniversary,
    verified = verified ?: false,
    accountType = accountType.toString(),
    registrationDate = registrationDate ?: dateFormatter.parse("00-00-0000"),
    imageUrl = image.orEmpty()
)

/**
 * Converts a [Customer] entity to its API response DTO [FoodOrderCustomerResponse].
 * Show minimal customer details when viewing an order.
 * @receiver `Customer` entity to map from.
 * @return [FoodOrderCustomerResponse] DTO with id, name, etc.
 */
fun Customer.orderResponse(): FoodOrderCustomerResponse = FoodOrderCustomerResponse(
    id = id ?: 0,
    username = username,
    firstName = firstName.orEmpty(),
    lastName = lastName.orEmpty(),
)

val dateFormatter = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())

/**
 * Converts a [Discount] entity to its response DTO.
 *
 * @receiver `Discount` entity to map from.
 * @return [DiscountResponse] DTO with id, discount name, percentage, start date, end date, and parent food id.
 */
fun Discount.toResponse(): DiscountResponse = DiscountResponse(
    id = id ?: 0,
    discountName = discountName.orEmpty(),
    percentage = percentage ?: 0,
    startDate = startDate ?: dateFormatter.parse("00-00-000"),
    endDate = endDate ?: dateFormatter.parse("00-00-0000")
)

/**
 * Converts a [Food] entity along with all its related entities
 * into a comprehensive [FoodResponse] DTO.
 *
 * This includes:
 * - Basic food properties (name, description, price)
 * - Associated vendor details (id and chef name)
 * - Collections of related entities mapped to their respective DTOs:
 *   - Images, Sizes, Complements, AddOns, and Order Types.
 *
 * @receiver `Food` entity to map from.
 * @return [FoodResponse] DTO representing full food details for API.
 */
fun Food.toResponse() = FoodResponse(
    id = id ?: 0,
    foodName = foodName.orEmpty(),
    vendorId = vendor.id ?: 0,
    vendorName = vendor.chefName.orEmpty(),
    mainCourse = mainCourse.orEmpty(),
    description = description.orEmpty(),
    basePrice = basePrice ?: 0,
    preparationTime = preparationTime ?: 0,
    deliveryTime = deliveryTime,
    deliveryFee = deliveryFee ?: 0,

    // Map each related entity collection to its response DTO
    images = this.foodImage.map { image ->
        FoodImageResponse(
            id = image.id ?: 0,
            imageUrl = image.imageUrl.orEmpty(),
            foodId = id ?: 0 // use parent food id, not image.food.id
        )
    },
    size = foodSize.map { it.toResponse() },
    complements = foodComplement.map { it.toResponse() },
    addOn = foodAddOn.map { it.toResponse() },
    orderType = foodOrderType.map { it.toResponse() },
    availability = foodAvailability.map { it.toResponse() },
    discount = foodDiscount.map { it.toResponse() }
)

/**
 * Converts a [Food] entity along with all its related entities
 * into a comprehensive [FoodMinimalResponse] DTO.
 *
 * This includes:
 * - Basic food properties (id, name, and main course)
 * @receiver `Food` entity to map from.
 * @return [FoodMinimalResponse] DTO representing little food details for API.
 */
fun Food.toMinimalResponse(): FoodMinimalResponse = FoodMinimalResponse (
    id = id ?: 0,
    foodName = foodName.orEmpty(),
    mainCourse = mainCourse.orEmpty(),
)

fun FoodAddOn.toResponse() = AddOnResponse(
    id = addOn.id ?: 0,
    addOnName = addOn.addOnName.orEmpty(),
    price = addOn.price ?: 0
)

/**
 * Maps a [FoodAvailability] entity to its API DTO [FoodAvailabilityResponse].
 *
 * @receiver `FoodAvailability` entity to map from.
 * @return [FoodAvailabilityResponse] DTO with id, and name.
 */
fun FoodAvailability.toResponse() = FoodAvailabilityResponse(
    id = this.id ?: 0,
    availability = this.availability.orEmpty()
)

/**
 * Maps a [FoodComplement] entity to [ComplementResponse] by delegating to the
 * wrapped [Complement] entity.
 *
 * @receiver `FoodComplement` entity to map from.
 * @return [ComplementResponse] DTO.
 */
fun FoodComplement.toResponse() = ComplementResponse(
    id = complement.id ?: 0,
    name = complement.complementName.orEmpty(),
    price = complement.price ?: 0,

    )

/**
 * Converts a [FoodDiscount] entity to its response DTO.
 *
 * @receiver `Discount` entity to map from.
 * @return [FoodDiscountResponse] DTO with id, discount name, percentage, start date, end date,  and active status.
 */
fun FoodDiscount.toResponse(): FoodDiscountResponse = FoodDiscountResponse(
    id = id ?: 0,
    discountName = discount.discountName.orEmpty(),
    percentage = discount.percentage ?: 0,
    startDate = discount.startDate ?: dateFormatter.parse("00-00-000"),
    endDate = discount.endDate ?: dateFormatter.parse("00-00-0000"),
    active = active ?: false
)


/**
 * Converts a [FoodImage] entity to its response DTO.
 *
 * @receiver `FoodImage` entity to map from.
 * @return [FoodImageResponse] DTO with id, URL, and parent food id.
 */
fun FoodImage.toResponse(): FoodImageResponse = FoodImageResponse(
    id = id ?: 0,
    imageUrl = imageUrl.orEmpty(),
    foodId = food.id ?: 0
)

/**
 * Converts a [FoodOrderType] entity to its response DTO.
 *
 * @receiver `FoodOrderType` entity to map from.
 * @return [FoodOrderTypeResponse] DTO with id, parent food id, and order type string.
 */
fun FoodOrderType.toResponse(): FoodOrderTypeResponse = FoodOrderTypeResponse(
    id = id ?: 0,
    foodId = food.id ?: 0,
    orderType = orderType.orEmpty()
)

/**
 * Converts a [FoodOrder] entity to its API response DTO [FoodOrderResponse].
 *
 * @receiver `FoodOrder` entity to map from.
 * @return [FoodOrderResponse] DTO with id, customer details, etc.
 */
fun FoodOrder.toResponse(): FoodOrderResponse = FoodOrderResponse(
    id = id ?: 0,
    customer = customer.orderResponse(),
    vendor = vendor.orderResponse(),
    food = food.toMinimalResponse(),
    complement = complement.toResponse(),
    size = size.toResponse(),
    orderAddOn = orderAddon.map { it.toResponse() },
    orderBeverage = orderBeverage.map { it.toResponse() },
    customerAddress = customerAddress.toResponse(),
    vendorAddress = vendorAddress.toResponse(),
    placementTime = placementTime,
    responseTime = responseTime,
    deliveryTime = deliveryTime,
    totalAmount = totalAmount ?: 0,
    orderType = orderType,
    completedTime = completedTime,
    customerDeletedStatus = customerDeletedStatus,
    vendorDeletedStatus = vendorDeletedStatus
)

/**
 * Maps a [FoodSize] entity to its response DTO.
 *
 * @receiver `FoodSize` entity to map from.
 * @return [FoodSizeResponse] DTO with id and size string.
 */
fun FoodSize.toResponse(): FoodSizeResponse = FoodSizeResponse(
    id = id ?: 0,
    size = size ?: Size.MEDIUM,
    priceIncrease = priceIncrease ?: 0
)

fun OrderAddOn.toResponse(): AddOnResponse = AddOnResponse(
    id = addOn.id ?: 0,
    addOnName = addOn.addOnName.orEmpty(),
    price = addOn.price ?: 0
)

fun OrderBeverage.toResponse(): BeverageResponse = BeverageResponse(
    id = beverage.id ?: 0,
    beverageName = beverage.beverageName.orEmpty(),
    alcoholic = beverage.alcoholic ?: false,
    percentage = beverage.percentage ?: 0,
    beverageGroup = beverage.beverageGroup ?: BeverageGroup.OTHER.name,
    price = beverage.price ?: 0,
    delivery = beverage.delivery ?: false,
)

fun PhoneNumber.toResponse(): PhoneNumberResponse = PhoneNumberResponse(
    id = id ?: 0,
    phoneNumber = phoneNumber.orEmpty(),
    default = default ?: false
)

/**
 * Maps a [Post] entity to its response DTO.
 *
 * @receiver `Post` entity to map from.
 * @return [PostResponse] DTO with id, title, body, image, likes and created date.
 */
fun Post.toResponse(): PostResponse = PostResponse(
    id = id ?: 0,
    title = title.orEmpty(),
    body = body.orEmpty(),
    image = image.orEmpty(),
    likes = likeCount,
    postDate = createdAt ?: dateFormatter.parse("00-00-0000"),
)

/**
 * Maps a [PhoneNumber] entity to its response DTO.
 *
 * @receiver `PhoneNumber` entity to map from.
 * @return [PhoneNumberResponse] DTO with id and size string, and its default status.
 */
fun VendorPhoneNumber.toResponse(): PhoneNumberResponse = PhoneNumberResponse(
    id = id ?: 0,
    phoneNumber = phoneNumber.orEmpty(),
    default = default ?: false
)

fun Vendor.toResponse(): VendorResponse = VendorResponse(
    id = id ?: 0,
    username = username,
    firstName = firstName.orEmpty(),
    lastName = lastName.orEmpty(),
    chefName = chefName.orEmpty(),
    restaurantName = restaurantName.orEmpty(),
    balance = balance,
    verified = verified ?: false,
    accountType = accountType,
    imageUrl = image.orEmpty(),
    registrationDate = registrationDate ?: dateFormatter.parse("00-00-0000"),
    phoneNumber = vendorPhoneNumber.map { it.toResponse() },
    address = address!!.toResponse(),
    food = food.map { it.toResponse() },
    addOn = addOn.map { it.toResponse() },
    complement = complement.map { it.toResponse() },
    discount = discount.map { it.toResponse() },
)

fun Vendor.toMinimalResponse(): VendorMinimalResponse = VendorMinimalResponse(
    username = username,
    firstName = firstName.orEmpty(),
    lastName = lastName.orEmpty(),
    chefName = chefName.orEmpty(),
    restaurantName = restaurantName.orEmpty(),
    balance = balance,
    verified = verified ?: false,
    phoneNumber = vendorPhoneNumber.map { it.toResponse() },
    address = address!!.toResponse(),
    registrationDate = registrationDate ?: dateFormatter.parse("00-00-0000"),
)

/**
 * Converts a [Vendor] entity to its API response DTO [FoodOrderVendorResponse].
 *
 * @receiver `Vendor` entity to map from.
 * @return [FoodOrderVendorResponse] DTO with id, name, etc.
 */
fun Vendor.orderResponse(): FoodOrderVendorResponse = FoodOrderVendorResponse(
    id = id ?: 0,
    username = username,
    chefName = chefName.orEmpty(),
    restaurantName = restaurantName.orEmpty()
)

