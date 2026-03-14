package com.qinet.feastique.common.mapper

import com.qinet.feastique.model.entity.DessertAvailability
import com.qinet.feastique.model.entity.FoodAvailability
import com.qinet.feastique.model.entity.address.Address
import com.qinet.feastique.model.entity.address.CustomerAddress
import com.qinet.feastique.model.entity.consumables.addOn.AddOn
import com.qinet.feastique.model.entity.consumables.addOn.FoodAddOn
import com.qinet.feastique.model.entity.consumables.beverage.Beverage
import com.qinet.feastique.model.entity.consumables.complement.Complement
import com.qinet.feastique.model.entity.consumables.complement.FoodComplement
import com.qinet.feastique.model.entity.consumables.dessert.Dessert
import com.qinet.feastique.model.entity.consumables.filling.Filling
import com.qinet.feastique.model.entity.consumables.filling.HandheldFilling
import com.qinet.feastique.model.entity.consumables.flavour.BeverageFlavour
import com.qinet.feastique.model.entity.consumables.flavour.DessertFlavour
import com.qinet.feastique.model.entity.consumables.food.Food
import com.qinet.feastique.model.entity.consumables.handheld.Handheld
import com.qinet.feastique.model.entity.contact.PhoneNumber
import com.qinet.feastique.model.entity.contact.VendorPhoneNumber
import com.qinet.feastique.model.entity.discount.DessertDiscount
import com.qinet.feastique.model.entity.discount.Discount
import com.qinet.feastique.model.entity.discount.FoodDiscount
import com.qinet.feastique.model.entity.discount.HandheldDiscount
import com.qinet.feastique.model.entity.image.DessertImage
import com.qinet.feastique.model.entity.image.FoodImage
import com.qinet.feastique.model.entity.image.HandheldImage
import com.qinet.feastique.model.entity.order.Cart
import com.qinet.feastique.model.entity.order.Order
import com.qinet.feastique.model.entity.order.item.*
import com.qinet.feastique.model.entity.post.Post
import com.qinet.feastique.model.entity.review.BeverageReview
import com.qinet.feastique.model.entity.review.DessertReview
import com.qinet.feastique.model.entity.review.FoodReview
import com.qinet.feastique.model.entity.review.HandheldReview
import com.qinet.feastique.model.entity.review.Review
import com.qinet.feastique.model.entity.review.VendorReview
import com.qinet.feastique.model.entity.size.BeverageFlavourSize
import com.qinet.feastique.model.entity.size.DessertFlavourSize
import com.qinet.feastique.model.entity.size.FoodSize
import com.qinet.feastique.model.entity.size.HandheldSize
import com.qinet.feastique.model.entity.user.Customer
import com.qinet.feastique.model.entity.user.Vendor
import com.qinet.feastique.model.enums.*
import com.qinet.feastique.response.availability.AvailabilityResponse
import com.qinet.feastique.response.discount.DiscountResponse
import com.qinet.feastique.response.image.ImageResponse
import com.qinet.feastique.response.post.PostResponse
import com.qinet.feastique.response.consumables.beverage.BeverageFlavourResponse
import com.qinet.feastique.response.consumables.beverage.BeverageFlavourSizeResponse
import com.qinet.feastique.response.consumables.beverage.BeverageOrderResponse
import com.qinet.feastique.response.consumables.beverage.BeverageResponse
import com.qinet.feastique.response.consumables.dessert.DessertFlavourResponse
import com.qinet.feastique.response.consumables.dessert.DessertFlavourSizeResponse
import com.qinet.feastique.response.consumables.dessert.DessertOrderResponse
import com.qinet.feastique.response.consumables.dessert.DessertResponse
import com.qinet.feastique.response.consumables.food.*
import com.qinet.feastique.response.consumables.handheld.FillingOrderResponse
import com.qinet.feastique.response.consumables.handheld.FillingResponse
import com.qinet.feastique.response.consumables.handheld.HandheldMinimalResponse
import com.qinet.feastique.response.consumables.handheld.HandheldOrderResponse
import com.qinet.feastique.response.consumables.handheld.HandheldResponse
import com.qinet.feastique.response.consumables.handheld.HandheldSizeResponse
import com.qinet.feastique.response.order.*
import com.qinet.feastique.response.pagination.PageResponse
import com.qinet.feastique.response.pagination.WindowResponse
import com.qinet.feastique.response.review.BeverageOrderItemReviewResponse
import com.qinet.feastique.response.review.DessertOrderItemReviewResponse
import com.qinet.feastique.response.review.FoodOrderItemReviewResponse
import com.qinet.feastique.response.review.HandheldOrderItemReviewResponse
import com.qinet.feastique.response.review.VendorOrderReviewResponse
import com.qinet.feastique.response.user.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.Window
import java.text.SimpleDateFormat
import java.util.*


val dateFormatter = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())

fun AddOn.toResponse() = AddOnResponse(
    id = this.id,
    addOnName = this.name.orEmpty(),
    price = this.price ?: 0,
    availability = availability?.type ?: Availability.UNAVAILABLE.type
)

fun Address.toResponse(): AddressResponse = AddressResponse(
    id = id,
    country = country,
    region = region?.type ?: Region.NON_SELECTED.name,
    city = city ?: "Not selected",
    neighbourhood = neighbourhood ?: "Not selected",
    streetName = streetName ?: "None",
    directions = directions ?: "None",
    location = listOf(longitude ?: "0.0", latitude ?: "0.0")
)

fun CustomerAddress.toResponse(): CustomerAddressResponse = CustomerAddressResponse(
    id = id,
    country = country,
    region = region?.name ?: Region.NON_SELECTED.name,
    city = city ?: "Not selected",
    neighbourhood = neighbourhood ?: "Not selected",
    streetName = streetName ?: "None",
    directions = directions ?: "None",
    location = listOf(longitude ?: "0.0", latitude ?: "0.0"),
    default = default ?: false
)

fun Beverage.toResponse() = BeverageResponse(
    id = id,
    beverageName = name.orEmpty(),
    alcoholic = alcoholic ?: false,
    beverageGroup = beverageGroup?.type ?: "Unknown",
    percentage = percentage ?: 0,
    availability = availability?.type ?: Availability.UNAVAILABLE.type,
    deliverable = deliverable ?: false,
    orderTypes = orderTypes.map { it.type }.toSet(),
    preparationTime = preparationTime ?: 0,
    readyAsFrom = readyAsFrom,
    deliveryFee = deliveryFee ?: 0,
    beverageFlavours = beverageFlavours.map { it.toResponse() }.toSet(),
)

fun BeverageCartItem.toResponse() = BeverageItemResponse(
    id = id,
    beverage = beverage.toResponse(),
    unitPrice = totalAmount!!.div(quantity),
    quantity = quantity,
    orderType = orderType?.type ?: OrderType.DINE_IN.type,
    totalAmount = totalAmount ?: 0
)

fun BeverageOrderItem.toBeverageItemResponse() = BeverageItemResponse(
    id = id,
    beverage = beverage.toResponse(),
    unitPrice = totalAmount!!.div(quantity),
    quantity = quantity,
    orderType = orderType?.type ?: OrderType.DINE_IN.type,
    totalAmount = totalAmount ?: 0
)

fun BeverageFlavour.toResponse() = BeverageFlavourResponse(
    id = id,
    name = name!!,
    description = description.orEmpty(),
    availability = availability?.type ?: Availability.UNAVAILABLE.type,
    flavourSizes = beverageFlavourSizes.map { it.toResponse() }.toSet()
)

fun BeverageFlavourSize.toResponse() = BeverageFlavourSizeResponse(
    id = id,
    size = this@toResponse.size!!.type,
    sizeName = name,
    price = price ?: 0,
    availability = availability?.type ?: Availability.UNAVAILABLE.type
)


/** Dispatches each cart item to its typed response. */
fun Cart.toResponse() = CartResponse(
    id = id,
    items = items.map {
        when (it) {
            is FoodCartItem -> it.toResponse()
            is BeverageCartItem -> it.toResponse()
            is DessertCartItem -> it.toResponse()
            is HandheldCartItem -> it.toResponse()
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

fun Complement.toResponse() = ComplementResponse(
    id = id,
    name = name.orEmpty(),
    price = price ?: 0,
    availability = availability?.type ?: Availability.UNAVAILABLE.type
)

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

/** Returns minimal customer details for order responses. */
fun Customer.orderResponse(): FoodOrderCustomerResponse = FoodOrderCustomerResponse(
    id = id,
    username = username,
    firstName = firstName.orEmpty(),
    lastName = lastName.orEmpty(),
)

fun Dessert.toResponse(): DessertResponse = DessertResponse(
    id = id,
    dessertName = name.orEmpty(),
    dessertType = dessertType?.type ?: DessertType.OTHER.type,
    description = description.orEmpty(),
    availability = availability?.type ?: Availability.UNAVAILABLE.type,
    deliverable = deliverable ?: false,
    deliveryFee = deliveryFee ?: 0,
    dessertFlavours = dessertFlavours.map { it.toResponse() },
    preparationTime = preparationTime ?: 0,
    readyAsFrom = readyAsFrom,
    orderTypes = dessertOrderTypes.map { it.type },
    availableDays = availableDays.map { it.type },
    discounts = dessertDiscounts.map { it.discount.toResponse() }.toSet(),
    dessertImages = dessertImages.map { it.toResponse() }
)

fun DessertAvailability.toResponse() = AvailabilityResponse(
    id = this.id,
    availability = this.availableDay?.type ?: "Unknown"
)

fun DessertCartItem.toResponse() = DessertItemResponse(
    id = id,
    dessert = dessert.toResponse(),
    unitPrice = totalAmount!!.div(quantity),
    quantity = quantity,
    orderType = orderType?.type ?: OrderType.DINE_IN.type,
    totalAmount = totalAmount ?: 0
)

fun DessertOrderItem.toDessertItemResponse() = DessertItemResponse(
    id = id,
    dessert = dessert.toResponse(),
    unitPrice = totalAmount!!.div(quantity),
    quantity = quantity,
    orderType = orderType?.type ?: OrderType.DINE_IN.type,
    totalAmount = totalAmount ?: 0
)

fun DessertDiscount.toResponse(): DiscountResponse = DiscountResponse(
    id = discount.id,
    discountName = discount.discountName!!,
    percentage = discount.percentage ?: 0,
    startDate = discount.startDate!!,
    endDate = discount.endDate!!,
)

fun DessertFlavour.toResponse(): DessertFlavourResponse = DessertFlavourResponse(
    id = id,
    flavourName = name.orEmpty(),
    description = description.orEmpty(),
    availability = availability?.type ?: Availability.UNAVAILABLE.type,
    flavourSizes = dessertFlavourSizes.map { it.toResponse() }
)

fun DessertFlavourSize.toResponse(): DessertFlavourSizeResponse = DessertFlavourSizeResponse(
    id = id,
    size = this@toResponse.size?.type ?: Size.MEDIUM.type,
    sizeName = name,
    price = price ?: 0,
    availability = availability?.type ?: Availability.UNAVAILABLE.type,
)

fun DessertImage.toResponse(): ImageResponse = ImageResponse(
    id = id,
    imageUrl = imageUrl!!
)

fun Discount.toResponse(): DiscountResponse = DiscountResponse(
    id = id,
    discountName = discountName.orEmpty(),
    percentage = percentage ?: 0,
    startDate = startDate ?: dateFormatter.parse("00-00-000"),
    endDate = endDate ?: dateFormatter.parse("00-00-0000")
)

fun Filling.toResponse() = FillingResponse(
    id = id,
    fillingName = name.orEmpty(),
    description = description
)

fun Food.toResponse() = FoodResponse(
    id = id,
    foodNumber = foodNumber.orEmpty(),
    foodName = name.orEmpty(),
    vendorId = vendor.id,
    vendorName = vendor.chefName.orEmpty(),
    mainCourse = mainCourse.orEmpty(),
    description = description.orEmpty(),
    basePrice = basePrice ?: 0,
    preparationTime = preparationTime ?: 0,
    readyAsFrom = readyAsFrom,
    deliverable = deliverable ?: false,
    dailyDeliveryQuantity = dailyDeliveryQuantity,
    deliveryTime = deliveryTime,
    deliveryFee = deliveryFee ?: 0,
    images = this.foodImages.map { image ->
        ImageResponse(
            id = image.id,
            imageUrl = image.imageUrl.orEmpty(),
        )
    },
    size = foodSizes.map { it.toResponse() },
    complements = foodComplements.map { it.toResponse() },
    addOn = foodAddOns.map { it.toResponse() },
    orderType = orderTypes.map { it.type },
    availableDays = availableDays.map { it.type },
    discount = foodDiscounts.map { it.toResponse() },
    availability = availability?.type ?: Availability.UNAVAILABLE.type
)

fun Food.toMinimalResponse(): FoodMinimalResponse = FoodMinimalResponse(
    id = id,
    foodName = name.orEmpty(),
    mainCourse = mainCourse.orEmpty(),
    basePrice = basePrice ?: 0
)

fun FoodAddOn.toResponse() = AddOnResponse(
    id = addOn.id,
    addOnName = addOn.name.orEmpty(),
    price = addOn.price ?: 0,
    availability = addOn.availability?.type ?: Availability.UNAVAILABLE.type
)

fun FoodAvailability.toResponse() = AvailabilityResponse(
    id = this.id,
    availability = this.availableDay?.type ?: "Unknown"
)

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

fun FoodComplement.toResponse() = ComplementResponse(
    id = complement.id,
    name = complement.name.orEmpty(),
    price = complement.price ?: 0,
    availability = complement.availability?.type ?: Availability.UNAVAILABLE.type
)

fun FoodDiscount.toResponse(): FoodDiscountResponse = FoodDiscountResponse(
    id = id,
    discountName = discount.discountName.orEmpty(),
    percentage = discount.percentage ?: 0,
    startDate = discount.startDate ?: dateFormatter.parse("00-00-000"),
    endDate = discount.endDate ?: dateFormatter.parse("00-00-0000"),
)

fun FoodImage.toResponse() = ImageResponse(
    id = id,
    imageUrl = imageUrl.orEmpty(),
)

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

fun FoodSize.toResponse(): FoodSizeResponse = FoodSizeResponse(
    id = id,
    size = this@toResponse.size?.type ?: Size.MEDIUM.type,
    name = name,
    priceIncrease = priceIncrease ?: 0,
    availability = availability?.type ?: Availability.UNAVAILABLE.type
)

fun Handheld.toResponse() = HandheldResponse(
    id = id,
    handheldNumber = handheldNumber.orEmpty(),
    handheldName = name.orEmpty(),
    vendorId = vendor.id,
    vendorName = vendor.chefName.orEmpty(),
    description = description,
    images = handheldImages.map { it.toResponse() },
    sizes = handheldSizes.map { it.toResponse() },
    fillings = handheldFillings.map { it.toResponse() },
    availability = availability?.type ?: Availability.UNAVAILABLE.type,
    preparationTime = preparationTime ?: 0,
    readyAsFrom = readyAsFrom,
    orderType = orderTypes.map { it.type },
    handheldType = handHeldType?.type ?: HandHeldType.OTHER.type,
    availableDays = availableDays.map { it.type },
    deliverable = deliverable ?: false,
    deliveryFee = deliveryFee ?: 0,
    discounts = handheldDiscounts.map { it.toResponse() }.toSet()
)

fun Handheld.toMinimalResponse() = HandheldMinimalResponse(
    id = id,
    handheldNumber = handheldNumber.orEmpty(),
    handheldName = name.orEmpty(),
    description = description,
)

fun HandheldCartItem.toResponse() = HandheldItemResponse(
    id = id,
    handheld = handheld.toMinimalResponse(),
    fillings = fillings.map { it.toResponse() },
    size = this@toResponse.size.toResponse(),
    quantity = quantity,
    discounts = appliedDiscounts.map { it.discount.toResponse() },
    orderType = orderType?.type ?: OrderType.PICKUP.type,
    totalAmount = totalAmount ?: 0
)

fun HandheldDiscount.toResponse() = DiscountResponse(
    id = discount.id,
    discountName = discount.discountName.orEmpty(),
    percentage = discount.percentage ?: 0,
    startDate = discount.startDate ?: dateFormatter.parse("00-00-000"),
    endDate = discount.endDate ?: dateFormatter.parse("00-00-0000"),
)

fun HandheldFilling.toResponse() = FillingResponse(
    id = filling.id,
    fillingName = filling.name.orEmpty(),
    description = filling.description,
)

private fun HandheldImage.toResponse() = ImageResponse(
    id = id,
    imageUrl = imageUrl.orEmpty()
)

fun HandheldOrderItem.toHandheldItemResponse() = HandheldItemResponse(
    id = id,
    handheld = handheld.toMinimalResponse(),
    fillings = fillings.map { it.toResponse() },
    size = size.toResponse(),
    quantity = quantity,
    orderType = orderType?.type ?: OrderType.DINE_IN.type,
    discounts = appliedDiscounts.map { it.discount.toResponse() },
    totalAmount = totalAmount ?: 0
)

private fun HandheldSize.toResponse() = HandheldSizeResponse(
    id = id,
    numberOfFillings = numberOfFillings ?: 0,
    size = this@toResponse.size?.type ?: Size.MEDIUM.type,
    sizeName = name,
    price = price ?: 0,
    availability = availability?.type
)

/** Dispatches each order item to its typed response. */
fun Order.toResponse(): OrderResponse = OrderResponse(
    id = id,
    userOrderCode = userOrderCode.orEmpty(),
    customer = customer!!.orderResponse(),
    vendor = vendor!!.orderResponse(),
    items = items.map {
        when (it) {
            is FoodOrderItem -> it.toResponse()
            is BeverageOrderItem -> it.toBeverageItemResponse()
            is DessertOrderItem -> it.toDessertItemResponse()
            is HandheldOrderItem -> it.toHandheldItemResponse()
            else -> UnknownEntityResponse(
                id = id,
                quantity = 0,
                totalAmount = 0,
                orderType = OrderType.UNKNOWN.type
            )
        }
    },
    orderType = orderType?.type ?: OrderType.UNKNOWN.type,
    deliveryFee = deliveryFee ?: 0,
    orderStatus = orderStatus?.type ?: OrderStatus.PENDING.type,
    totalAmount = totalAmount ?: 0,
    placementTime = placementTime,
    responseTime = responseTime,
    deliveryTime = deliveryTime,
    completedTime = completedTime,
    customerAddress = customerAddress?.toResponse()
)


fun <T : Any> Page<T>.toResponse() = PageResponse(
    items = this.content,
    pageNumber = this.number,
    pageSize = this.size,
    totalPages = this.totalPages,
    totalElements = this.totalElements,
    hasNext = this.hasNext(),
    hasPrevious = this.hasPrevious()
)

fun PhoneNumber.toResponse(): PhoneNumberResponse = PhoneNumberResponse(
    id = id,
    phoneNumber = phoneNumber.orEmpty(),
    default = default ?: false
)

fun Post.toResponse(): PostResponse = PostResponse(
    id = id,
    title = title.orEmpty(),
    body = body.orEmpty(),
    image = image.orEmpty(),
    likes = likeCount,
    postDate = createdAt ?: dateFormatter.parse("00-00-0000"),
)


// Review Responses
fun BeverageOrderItem.toBeverageReviewResponse() = BeverageOrderResponse(
    beverage = beverage.name.orEmpty(),
    beverageFlavour = beverageFlavour.name.orEmpty(),
    beverageFlavourSize = beverageFlavourSize.name
)

fun DessertOrderItem.toDessertReviewResponse() = DessertOrderResponse(
    dessert = dessert.name.orEmpty(),
    dessertFlavour = dessertFlavour.name.orEmpty(),
    dessertFlavourSize = dessertFlavourSize.name
)

fun AddOn.toAddOnReviewResponse() = FoodOrderItemResponse(
    name = this.name.orEmpty(),
    price = this.price ?: 0
)

fun Complement.toComplementReviewResponse() = FoodOrderItemResponse(
    name = this.name.orEmpty(),
    price = this.price ?: 0
)

fun FoodOrderItem.toFoodReviewResponse() = FoodOrderResponse(
    name = food.name.orEmpty(),
    complement = complement.toComplementReviewResponse(),
    addOns = addOns.map { it.toAddOnReviewResponse() }.toSet()
)

fun Filling.toFillingReviewResponse() = FillingOrderResponse(
    name = name.orEmpty()
)

fun HandheldOrderItem.toHandheldReviewResponse() = HandheldOrderResponse(
    handheld = handheld.name.orEmpty(),
    fillings = fillings.map { it.toFillingReviewResponse() }.toSet(),
    handheldSize = size.name
)

@Suppress("unused")
fun Vendor.toReviewResponse() = VendorReviewResponse(
    id = id,
    chefName = chefName.orEmpty(),
    restaurantName = restaurantName.orEmpty(),
)

fun Order.toReviewResponse() = OrderReviewResponse(
    orderId = id,
    orderType = orderType?.type
)

fun Review.toResponse() = when (this) {
    is BeverageReview -> BeverageOrderItemReviewResponse(
        id = id,
        order = order.toReviewResponse(),
        beverageOrderItem = beverageOrderItem.toBeverageReviewResponse(),
        review = review,
        rating = rating ?: 2.5f,
    )

    is DessertReview -> DessertOrderItemReviewResponse(
        id = id,
        order = order.toReviewResponse(),
        dessertOrderItem = dessertOrderItem.toDessertReviewResponse(),
        review = review,
        rating = rating ?: 2.5f,
    )

    is FoodReview -> FoodOrderItemReviewResponse(
        id = id,
        order = order.toReviewResponse(),
        food = foodOrderItem.toFoodReviewResponse(),
        review = review,
        rating = rating ?: 2.5f,
    )

    is HandheldReview -> HandheldOrderItemReviewResponse(
        id = id,
        order = order.toReviewResponse(),
        handheldOrderItem = handheldOrderItem.toHandheldReviewResponse(),
        review = review,
        rating = rating ?: 2.5f
    )

    is VendorReview -> VendorOrderReviewResponse(
        id = id,
        order = order.toReviewResponse(),
        review = review,
        rating = rating ?: 2.5f,
    )

    else -> throw IllegalArgumentException("Unknown review type: ${this::class.simpleName}")
}


fun VendorPhoneNumber.toResponse(): PhoneNumberResponse = PhoneNumberResponse(
    id = id,
    phoneNumber = phoneNumber.orEmpty(),
    default = default ?: false
)

fun Vendor.toResponse(): VendorResponse = VendorResponse(
    id = id,
    username = username,
    vendorCode = vendorCode.orEmpty(),
    firstName = firstName.orEmpty(),
    lastName = lastName.orEmpty(),
    chefName = chefName.orEmpty(),
    restaurantName = restaurantName.orEmpty(),
    balance = balance,
    openingTime = openingTime,
    closingTime = closingTime,
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

fun Vendor.toMinimalResponse(): VendorMinimalResponse = VendorMinimalResponse(
    id = id,
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
    openingTime = openingTime,
    closingTime = closingTime
)

/** Returns minimal vendor details for order responses. */
fun Vendor.orderResponse(): FoodOrderVendorResponse = FoodOrderVendorResponse(
    id = id,
    username = username,
    chefName = chefName.orEmpty(),
    restaurantName = restaurantName.orEmpty()
)

/** Cursor-based pagination: extracts next cursor only when a next page exists. */
fun <T : Any> Window<T>.toResponse(currentOffset: Long, cursorExtractor: (offset: Long) -> String?): WindowResponse<T> =
    WindowResponse(
        content = this.content,
        nextCursor = if (this.hasNext()) cursorExtractor(currentOffset + this.content.size) else null,
        hasNext = this.hasNext()
    )

