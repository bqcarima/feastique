package com.qinet.feastique.common.mapper

import com.qinet.feastique.model.entity.addOn.AddOn
import com.qinet.feastique.model.entity.addOn.FoodAddOn
import com.qinet.feastique.model.entity.address.Address
import com.qinet.feastique.model.entity.address.CustomerAddress
import com.qinet.feastique.model.entity.beverage.Beverage
import com.qinet.feastique.model.entity.complement.Complement
import com.qinet.feastique.model.entity.complement.FoodComplement
import com.qinet.feastique.model.entity.discount.Discount
import com.qinet.feastique.model.entity.discount.FoodDiscount
import com.qinet.feastique.model.entity.food.*
import com.qinet.feastique.model.entity.order.Cart
import com.qinet.feastique.model.entity.order.Order
import com.qinet.feastique.model.entity.order.beverage.BeverageCartItem
import com.qinet.feastique.model.entity.order.beverage.BeverageOrderItem
import com.qinet.feastique.model.entity.order.food.FoodCartItem
import com.qinet.feastique.model.entity.order.food.FoodOrderItem
import com.qinet.feastique.model.entity.phoneNumber.PhoneNumber
import com.qinet.feastique.model.entity.phoneNumber.VendorPhoneNumber
import com.qinet.feastique.model.entity.post.Post
import com.qinet.feastique.model.entity.user.Customer
import com.qinet.feastique.model.entity.user.Vendor
import com.qinet.feastique.model.enums.OrderStatus
import com.qinet.feastique.model.enums.OrderType
import com.qinet.feastique.model.enums.Size
import com.qinet.feastique.response.*
import com.qinet.feastique.response.address.AddressResponse
import com.qinet.feastique.response.address.CustomerAddressResponse
import com.qinet.feastique.response.BeverageResponse
import com.qinet.feastique.response.food.*
import com.qinet.feastique.response.order.CartResponse
import com.qinet.feastique.response.order.FoodOrderCustomerResponse
import com.qinet.feastique.response.order.FoodOrderVendorResponse
import com.qinet.feastique.response.order.OrderResponse
import com.qinet.feastique.response.order.BeverageItemResponse
import com.qinet.feastique.response.order.FoodItemResponse
import com.qinet.feastique.response.vendor.VendorMinimalResponse
import com.qinet.feastique.response.vendor.VendorResponse
import com.qinet.feastique.model.dto.order.BeverageItemDto
import com.qinet.feastique.model.enums.Region
import com.qinet.feastique.response.order.UnknownEntityResponse
import java.text.SimpleDateFormat
import java.util.*

/**
 * Maps an [AddOn] entity to its API DTO [AddOnResponse].
 *
 * @receiver `AddOn` entity to map from.
 * @return [AddOnResponse] DTO with id, name, and price.
 */
fun AddOn.toResponse() = AddOnResponse(
    id = this.id,
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
    id = id,
    country = country,
    region = region?.type ?: Region.NON_SELECTED.type,
    city = city ?: "Not selected",
    neighbourhood = neighbourhood ?: "Not selected",
    streetName = streetName ?: "None",
    directions = directions ?: "None",
    location = listOf(longitude ?: "0.0", latitude ?: "0.0")
)

/**
 * Maps a [CustomerAddress] entity to its API DTO [CustomerAddressResponse].
 *
 * @receiver `CustomerAddress` entity to map from.
 * @return [CustomerAddressResponse] DTO with id, country, region, etc.
 */
fun CustomerAddress.toResponse(): CustomerAddressResponse = CustomerAddressResponse(
    id = id,
    country = country,
    region = region?.type ?: Region.NON_SELECTED.type,
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
    id = id,
    beverageName = beverageName.orEmpty(),
    alcoholic = alcoholic ?: false,
    beverageGroup = beverageGroup?.type ?: "Unknown",
    percentage = percentage ?: 0,
    price = price ?: 0,
    delivery = delivery ?: false,
)

/**
 * Converts a [BeverageCartItem] entity to its API response DTO [BeverageResponse].
 *
 * @receiver `CartResponse` entity to map from.
 * @return [BeverageResponse] DTO with id, name, price, etc.
 */
fun BeverageCartItem.toResponse() = BeverageItemResponse(
    id = id,
    beverage = beverage.toResponse(),
    unitPrice = totalAmount!!.div(quantity),
    quantity = quantity,
    orderType = orderType?.type ?: OrderType.DINE_IN.type,
    totalAmount = totalAmount ?: 0
)

/**
 * Maps a [BeverageOrderItem] entity to an API DTO [BeverageItemDto].
 *
 * @receiver `BeverageOrderItem` entity to map from.
 * @return [CartResponse] DTO with id, beverage name, quantity, and total amount.
 */
fun BeverageOrderItem.toBeverageItemResponse() = BeverageItemResponse(
    id = id,
    beverage = beverage.toResponse(),
    unitPrice = totalAmount!!.div(quantity),
    quantity = quantity,
    orderType = orderType?.type ?: OrderType.DINE_IN.type,
    totalAmount = totalAmount ?: 0
)

/**
 * Converts a [Cart] entity to its API response DTO [CartResponse].
 *
 * @receiver `Cart` entity to map from.
 * @return [CartResponse] DTO with id, name, price, etc.
 */
fun Cart.toResponse() = CartResponse(
    id = id,
    items = items.map {
        when (it) {
            is FoodCartItem -> it.toResponse()
            is BeverageCartItem -> it.toResponse()
            else -> UnknownEntityResponse(
                id = id,
                quantity = 0,
                totalAmount = 0,
                orderType = OrderType.UNKNOWN.type
            )
        }
    }.toMutableList(),
    total = totalAmount ?: 0
)

/**
 * Converts a [Complement] entity to its API response DTO [ComplementResponse].
 *
 * @receiver `Complement entity` to map from.
 * @return [ComplementResponse] DTO with id, name, and price.
 */
fun Complement.toResponse() = ComplementResponse(
    id = id,
    name = complementName.orEmpty(),
    price = price ?: 0
)

/**
 * Converts a [Customer] entity to its API response DTO [CustomerResponse].
 *
 * @receiver `Customer` entity to map from.
 * @return [CustomerResponse] DTO with id, first name, last name, etc.
 */
fun Customer.toResponse(): CustomerResponse = CustomerResponse(
    id = id,
    username = username,
    firstName = firstName.orEmpty(),
    lastName = lastName.orEmpty(),
    dob = dob,
    phoneNumber = phoneNumber.map { it.toResponse() },
    address = address.map { it.toResponse() },
    anniversary = anniversary,
    verified = verified ?: false,
    accountType = accountType?.type.orEmpty(),
    registrationDate = registrationDate ?: dateFormatter.parse("00-00-0000"),
    imageUrl = image.orEmpty()
)

/**
 * Converts a [Customer] entity to its API response DTO [FoodOrderCustomerResponse].
 * Show minimal customer details when viewing an order.
 * @receiver `Customer` entity to map from.
 * @return [FoodOrderCustomerResponse] DTO with id, name, and only relevant information.
 */
fun Customer.orderResponse(): FoodOrderCustomerResponse = FoodOrderCustomerResponse(
    id = id,
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
    id = id,
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
    id = id,
    foodNumber = foodNumber.orEmpty(),
    foodName = foodName.orEmpty(),
    vendorId = vendor.id,
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
            id = image.id,
            imageUrl = image.imageUrl.orEmpty(),
            foodId = id // use parent food id, not image.food.id
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
fun Food.toMinimalResponse(): FoodMinimalResponse = FoodMinimalResponse(
    id = id,
    foodName = foodName.orEmpty(),
    mainCourse = mainCourse.orEmpty(),
    basePrice = basePrice ?: 0
)

fun FoodAddOn.toResponse() = AddOnResponse(
    id = addOn.id,
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
    id = this.id,
    availability = this.availability?.type ?: "Unknown"
)

/**
 * Converts a [FoodCartItem] entity to its API response DTO [FoodItemResponse].
 * @receiver `CartItemResponse` entity to map from.
 * @return [FoodItemResponse] DTO with id, name, price, etc.
 */
fun FoodCartItem.toResponse(): FoodItemResponse = FoodItemResponse(
    id = id,
    food = food.toMinimalResponse(),
    complement = complement.toResponse(),
    addOns = addOns.map { it.toResponse() },
    size = size.toResponse(),
    quantity = quantity,
    discounts = appliedDiscounts.map { it.discount.toResponse() },
    totalAmount = totalAmount ?: 0,
    orderType = orderType?.type ?: OrderType.DINE_IN.type
)

/**
 * Maps a [FoodComplement] entity to [ComplementResponse] by delegating to the
 * wrapped [Complement] entity.
 *
 * @receiver `FoodComplement` entity to map from.
 * @return [ComplementResponse] DTO.
 */
fun FoodComplement.toResponse() = ComplementResponse(
    id = complement.id,
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
    id = id,
    discountName = discount.discountName.orEmpty(),
    percentage = discount.percentage ?: 0,
    startDate = discount.startDate ?: dateFormatter.parse("00-00-000"),
    endDate = discount.endDate ?: dateFormatter.parse("00-00-0000"),
)


/**
 * Converts a [FoodImage] entity to its response DTO.
 *
 * @receiver `FoodImage` entity to map from.
 * @return [FoodImageResponse] DTO with id, URL, and parent food id.
 */
fun FoodImage.toResponse(): FoodImageResponse = FoodImageResponse(
    id = id,
    imageUrl = imageUrl.orEmpty(),
    foodId = food.id
)

/**
 * Converts a [FoodOrderType] entity to its response DTO.
 *
 * @receiver `FoodOrderType` entity to map from.
 * @return [FoodOrderTypeResponse] DTO with id, parent food id, and order type string.
 */
fun FoodOrderType.toResponse(): FoodOrderTypeResponse = FoodOrderTypeResponse(
    id = id,
    foodId = food.id,
    orderType = orderType?.type ?: "Unknown"
)

/**
 * Converts a [FoodOrderItem] entity to its API response DTO [FoodItemResponse].
 *
 * @receiver `FoodOrderItem` entity to map from.
 * @return [FoodItemResponse] DTO with id, customer details, etc.
 */
fun FoodOrderItem.toResponse(): FoodItemResponse = FoodItemResponse(
    id = id,
    food = food.toMinimalResponse(),
    complement = complement.toResponse(),
    addOns = addOns.map { it.toResponse() },
    size = size.toResponse(),
    quantity = quantity,
    discounts = appliedDiscounts.map { it.discount.toResponse() },
    totalAmount = totalAmount ?: 0,
    orderType = orderType?.type ?: OrderType.DINE_IN.type
)

/**
 * Maps a [FoodSize] entity to its response DTO.
 *
 * @receiver `FoodSize` entity to map from.
 * @return [FoodSizeResponse] DTO with id and size string.
 */
fun FoodSize.toResponse(): FoodSizeResponse = FoodSizeResponse(
    id = id,
    size = size?.type ?: Size.MEDIUM.type,
    name = name ?: size.toString(),
    priceIncrease = priceIncrease ?: 0
)

/**
 * Maps an [Order] entity to its API DTO [OrderResponse].
 *
 * @receiver `FoodAvailability` entity to map from.
 * @return [OrderResponse] DTO with id, userOrderCode etc.
 */
fun Order.toResponse(): OrderResponse = OrderResponse(
    id = id,
    userOrderCode = userOrderCode.orEmpty(),
    customer = customer!!.orderResponse(),
    vendor = vendor!!.orderResponse(),
    items = items.map {
        when(it) {
            is FoodOrderItem -> it.toResponse()
            is BeverageOrderItem -> it.toBeverageItemResponse()
            else -> UnknownEntityResponse(
                id = id,
                quantity = 0,
                totalAmount = 0,
                orderType = OrderType.UNKNOWN.type
            )
        }
    },
    orderType = orderType,
    deliveryFee = deliveryFee ?: 0,
    orderStatus = orderStatus?.type ?: OrderStatus.PENDING.type,
    totalAmount = totalAmount ?: 0,
    placementTime = placementTime,
    responseTime = responseTime,
    deliveryTime = deliveryTime,
    completedTime = completedTime,
    customerAddress = customerAddress?.toResponse()
)

/**
 * Maps a [PhoneNumber] entity to its API DTO [PhoneNumberResponse].
 *
 * @receiver `PhoneNumber` entity to map from.
 * @return [OrderResponse] DTO with id, phone number etc.
 */
fun PhoneNumber.toResponse(): PhoneNumberResponse = PhoneNumberResponse(
    id = id,
    phoneNumber = phoneNumber.orEmpty(),
    default = default ?: false
)

/**
 * Maps a [Post] entity to a response DTO.
 *
 * @receiver `Post` entity to map from.
 * @return [PostResponse] DTO with id, title, body, image, likes and created date.
 */
fun Post.toResponse(): PostResponse = PostResponse(
    id = id,
    title = title.orEmpty(),
    body = body.orEmpty(),
    image = image.orEmpty(),
    likes = likeCount,
    postDate = createdAt ?: dateFormatter.parse("00-00-0000"),
)

/**
 * Maps a [VendorPhoneNumber] entity to its response DTO.
 *
 * @receiver `VendorPhoneNumber` entity to map from.
 * @return [PhoneNumberResponse] DTO with id and phoneNumber string, and its default status.
 */
fun VendorPhoneNumber.toResponse(): PhoneNumberResponse = PhoneNumberResponse(
    id = id,
    phoneNumber = phoneNumber.orEmpty(),
    default = default ?: false
)

/**
 * Maps a [Vendor] entity to its API DTO [VendorResponse].
 *
 * @receiver `Vendor` entity to map from.
 * @return [VendorResponse] DTO with id, username, chef name etc.
 */
fun Vendor.toResponse(): VendorResponse = VendorResponse(
    id = id,
    username = username,
    vendorCode = vendorCode.orEmpty(),
    firstName = firstName.orEmpty(),
    lastName = lastName.orEmpty(),
    chefName = chefName.orEmpty(),
    restaurantName = restaurantName.orEmpty(),
    balance = balance,
    verified = verified ?: false,
    accountType = accountType?.type,
    imageUrl = image.orEmpty(),
    registrationDate = registrationDate ?: dateFormatter.parse("00-00-0000"),
    phoneNumber = vendorPhoneNumber.map { it.toResponse() },
    address = address!!.toResponse(),
    food = food.map { it.toResponse() },
    addOn = addOn.map { it.toResponse() },
    complement = complement.map { it.toResponse() },
    discount = discount.map { it.toResponse() },
)


/**
 * Maps a [Vendor] entity to its API DTO [VendorMinimalResponse].
 *
 * @receiver `Vendor` entity to map from.
 * @return [VendorMinimalResponse] DTO with id, username, chef name. and reduced information.
 */
fun Vendor.toMinimalResponse(): VendorMinimalResponse = VendorMinimalResponse(
    username = username,
    vendorCode = vendorCode.orEmpty(),
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
 * @return [FoodOrderVendorResponse] DTO with id, name, and only necessary information.
 */
fun Vendor.orderResponse(): FoodOrderVendorResponse = FoodOrderVendorResponse(
    id = id,
    username = username,
    chefName = chefName.orEmpty(),
    restaurantName = restaurantName.orEmpty()
)

