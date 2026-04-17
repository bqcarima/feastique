package com.qinet.feastique.common.mapper

import com.qinet.feastique.model.entity.address.Address
import com.qinet.feastique.model.entity.address.CustomerAddress
import com.qinet.feastique.model.entity.availability.DessertAvailability
import com.qinet.feastique.model.entity.availability.FoodAvailability
import com.qinet.feastique.model.entity.bookmark.*
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
import com.qinet.feastique.model.entity.image.PostImage
import com.qinet.feastique.model.entity.message.Conversation
import com.qinet.feastique.model.entity.message.Message
import com.qinet.feastique.model.entity.order.Cart
import com.qinet.feastique.model.entity.order.Order
import com.qinet.feastique.model.entity.order.item.*
import com.qinet.feastique.model.entity.post.Post
import com.qinet.feastique.model.entity.review.*
import com.qinet.feastique.model.entity.size.BeverageFlavourSize
import com.qinet.feastique.model.entity.size.DessertFlavourSize
import com.qinet.feastique.model.entity.size.FoodSize
import com.qinet.feastique.model.entity.size.HandheldSize
import com.qinet.feastique.model.entity.user.Customer
import com.qinet.feastique.model.entity.user.Vendor
import com.qinet.feastique.model.enums.*
import com.qinet.feastique.response.availability.AvailabilityResponse
import com.qinet.feastique.response.bookmark.BookmarkResponse
import com.qinet.feastique.response.consumables.beverage.BeverageFlavourResponse
import com.qinet.feastique.response.consumables.beverage.BeverageFlavourSizeResponse
import com.qinet.feastique.response.consumables.beverage.BeverageOrderResponse
import com.qinet.feastique.response.consumables.beverage.BeverageResponse
import com.qinet.feastique.response.consumables.dessert.DessertFlavourResponse
import com.qinet.feastique.response.consumables.dessert.DessertFlavourSizeResponse
import com.qinet.feastique.response.consumables.dessert.DessertOrderResponse
import com.qinet.feastique.response.consumables.dessert.DessertResponse
import com.qinet.feastique.response.consumables.food.*
import com.qinet.feastique.response.consumables.handheld.*
import com.qinet.feastique.response.discount.DiscountResponse
import com.qinet.feastique.response.image.ImageResponse
import com.qinet.feastique.response.message.ConversationResponse
import com.qinet.feastique.response.message.ConversationSummaryResponse
import com.qinet.feastique.response.message.MessageReplyResponse
import com.qinet.feastique.response.message.MessageResponse
import com.qinet.feastique.response.order.*
import com.qinet.feastique.response.pagination.PageResponse
import com.qinet.feastique.response.pagination.WindowResponse
import com.qinet.feastique.response.post.PostResponse
import com.qinet.feastique.response.review.*
import com.qinet.feastique.response.user.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.Window
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*


val dateFormatter = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
val dateTimeFormatter: DateTimeFormatter? = DateTimeFormatter.ofPattern("HH:mm:ss dd-MM-yyyy", Locale.getDefault())

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

fun Bookmark.toResponse(like: Boolean = false, bookmark: Boolean = true) = BookmarkResponse(
    id = id,
    item = when (this) {
        is BeverageBookmark -> this.beverage.toResponse(like, bookmark)
        is DessertBookmark -> this.dessert.toResponse(like, bookmark)
        is FoodBookmark -> this.food.toResponse(like, bookmark)
        is HandheldBookmark -> this.handheld.toResponse(like, bookmark)
        is VendorBookmark -> this.vendor.toBookmarkResponse(like, bookmark)
        else -> throw IllegalArgumentException("Unknown bookmark type.")
    },
    createdAt = createdAt?.format(dateTimeFormatter) ?: LocalDateTime.MIN.format(dateTimeFormatter)
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

fun Beverage.toResponse(like: Boolean = false, bookmark: Boolean = false) = BeverageResponse(
    id = id,
    name = name.orEmpty(),
    alcoholic = alcoholic ?: false,
    beverageGroup = beverageGroup?.type ?: "Unknown",
    percentage = percentage ?: 0,
    availability = availability?.type ?: Availability.UNAVAILABLE.type,
    deliverable = deliverable ?: false,
    orderTypes = orderTypes.map { it.type }.toSet(),
    preparationTime = preparationTime ?: 0,
    readyAsFrom = readyAsFrom,
    deliveryFee = deliveryFee ?: 0,
    beverageFlavours = beverageFlavours.filter { it.isActive }.map { it.toResponse() }.toSet(),
    likeCount = likeCount,
    likedByCurrentUser = like,
    availableDays = availableDays.map { it.type }.toSet(),
    bookmarkCount = bookmarkCount,
    bookmarkedByCurrentUser = bookmark,
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
    flavourSizes = beverageFlavourSizes.filter { it.isActive }.map { it.toResponse() }.toSet()
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

fun Conversation.toSummaryResponse(role: String): ConversationSummaryResponse = ConversationSummaryResponse(
    id = id,
    startedAt = startedAt,
    read = if (role == "VENDOR") vendorRead else customerRead,
    otherPartyName = if (role == "VENDOR") {
        customer.username
    }
    else {
        vendor.restaurantName ?: vendor.chefName ?: vendor.username
    },
    otherPartyId = if (role == "VENDOR") customer.id else vendor.id
)

fun Conversation.toResponse(recentMessages: List<Message>, role: String): ConversationResponse = ConversationResponse(
    id = id,
    startedAt = startedAt,
    read = if (role == "VENDOR") vendorRead else customerRead,
    otherPartyName = if (role == "VENDOR") {
        customer.username
    }
    else {
        vendor.restaurantName ?: vendor.chefName ?: vendor.username
    },
    otherPartyId = if (role == "VENDOR") customer.id else vendor.id,
    recentMessages = recentMessages.map { it.toResponse() }
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
    imageUrl = displayPicture.orEmpty()
)

/** Returns minimal customer details for order responses. */
fun Customer.orderResponse(): FoodOrderCustomerResponse = FoodOrderCustomerResponse(
    id = id,
    username = username,
    firstName = firstName.orEmpty(),
    lastName = lastName.orEmpty(),
)

fun Dessert.toResponse(like: Boolean = false, bookmark: Boolean = false): DessertResponse = DessertResponse(
    id = id,
    name = name.orEmpty(),
    dessertType = dessertType?.type ?: DessertType.OTHER.type,
    description = description.orEmpty(),
    availability = availability?.type ?: Availability.UNAVAILABLE.type,
    deliverable = deliverable ?: false,
    deliveryFee = deliveryFee ?: 0,
    dessertFlavours = dessertFlavours.filter { it.isActive }.map { it.toResponse() },
    preparationTime = preparationTime ?: 0,
    readyAsFrom = readyAsFrom,
    orderTypes = dessertOrderTypes.map { it.type }.toSet(),
    availableDays = availableDays.map { it.type }.toSet(),
    discounts = dessertDiscounts.map { it.discount.toResponse() }.toSet(),
    dessertImages = dessertImages.map { it.toResponse() },
    likeCount = likeCount,
    likedByCurrentUser = like,
    bookmarkCount = bookmarkCount,
    bookmarkedByCurrentUser = bookmark
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
    flavourSizes = dessertFlavourSizes.filter { it.isActive }.map { it.toResponse() }
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

fun Food.toResponse(like: Boolean = false, bookmark: Boolean = false) = FoodResponse(
    id = id,
    foodNumber = foodNumber.orEmpty(),
    name = name.orEmpty(),
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
    images = this.foodImages.map { it.toResponse() }.toSet(),
    size = foodSizes.filter { it.isActive }.map { it.toResponse() }.toSet(),
    complements = foodComplements.filter { it.complement.isActive }.map { it.toResponse() }.toSet(),
    addOn = foodAddOns.filter { it.addOn.isActive }.map { it.toResponse() },
    orderTypes = orderTypes.map { it.type }.toSet(),
    availableDays = availableDays.map { it.type }.toSet(),
    discount = foodDiscounts.map { it.toResponse() }.toSet(),
    availability = availability?.type ?: Availability.UNAVAILABLE.type,
    likeCount = likeCount,
    likedByCurrentUser = like,
    bookmarkCount = bookmarkCount,
    bookmarkedByCurrentUser = bookmark
)

fun Food.toMinimalResponse(like: Boolean = false): FoodMinimalResponse = FoodMinimalResponse(
    id = id,
    foodName = name.orEmpty(),
    mainCourse = mainCourse.orEmpty(),
    basePrice = basePrice ?: 0,
    likeCount = likeCount,
    likedByCurrentUser = like
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

fun Handheld.toResponse(like: Boolean = false, bookmark: Boolean = false) = HandheldResponse(
    id = id,
    handheldNumber = handheldNumber.orEmpty(),
    name = name.orEmpty(),
    vendorId = vendor.id,
    vendorName = vendor.chefName.orEmpty(),
    description = description,
    images = handheldImages.map { it.toResponse() },
    sizes = handheldSizes.filter { it.isActive }.map { it.toResponse() },
    fillings = handheldFillings.filter { it.filling.isActive }.map { it.toResponse() },
    availability = availability?.type ?: Availability.UNAVAILABLE.type,
    preparationTime = preparationTime ?: 0,
    readyAsFrom = readyAsFrom,
    orderTypes = orderTypes.map { it.type }.toSet(),
    handheldType = handHeldType?.type ?: HandHeldType.OTHER.type,
    availableDays = availableDays.map { it.type }.toSet(),
    deliverable = deliverable ?: false,
    deliveryFee = deliveryFee ?: 0,
    discounts = handheldDiscounts.map { it.toResponse() }.toSet(),
    likeCount = likeCount,
    likedByCurrentUser = like,
    bookmarkCount = bookmarkCount,
    bookmarkedByCurrentUser = bookmark
)

fun Handheld.toMinimalResponse(like: Boolean = false) = HandheldMinimalResponse(
    id = id,
    handheldNumber = handheldNumber.orEmpty(),
    handheldName = name.orEmpty(),
    description = description,
    likeCount = likeCount,
    likedByCurrentUser = like,
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

fun Message.toResponse() = MessageResponse(
    id = id,
    body = when {
        senderType == AccountType.VENDOR && vendorDeleted -> "[Message deleted.]"
        senderType == AccountType.CUSTOMER && customerDeleted -> "[Message deleted.]"
        else -> body.orEmpty()
    },
    sentAt = sentAt,
    senderType = senderType.type,
    replyTo = replyTo?.let {
        MessageReplyResponse(
            id = it.id,
            body = when (it.senderType) {
                AccountType.VENDOR if it.vendorDeleted -> "[Message deleted]"
                AccountType.CUSTOMER if it.customerDeleted -> "[Message deleted]"
                else -> it.body.orEmpty()
            },
            senderType = it.senderType.type,
        )
    },
    deleted = when(senderType) {
        AccountType.VENDOR -> vendorDeleted
        AccountType.CUSTOMER -> customerDeleted
    }
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

private fun PostImage.toResponse() = ImageResponse(
    id = id,
    imageUrl = imageUrl.orEmpty()
)

fun Post.toResponse(like: Boolean = false): PostResponse = PostResponse(
    id = id,
    title = title.orEmpty(),
    body = body,
    images = postImages.map { it.toResponse() }.toSet(),
    likeCount = likeCount,
    likedByCurrentUser = like,
    createdAt = createdAt?.format(dateTimeFormatter) ?: LocalDateTime.MIN.format(dateTimeFormatter),
    updatedAt = updatedAt?.format(dateTimeFormatter),
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

fun Vendor.toResponse(like: Boolean = false): VendorResponse = VendorResponse(
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
    imageUrl = displayPicture.orEmpty(),
    registrationDate = registrationDate ?: dateFormatter.parse("00-00-0000"),
    phoneNumber = vendorPhoneNumber.map { it.toResponse() },
    address = address!!.toResponse(),
    food = food.map { it.toResponse() },
    addOn = addOn.map { it.toResponse() },
    complement = complement.map { it.toResponse() },
    discount = discount.map { it.toResponse() },
    likeCount = likeCount,
    likedByCurrentUser = like,
)

fun VendorPhoneNumber.toResponse(): PhoneNumberResponse = PhoneNumberResponse(
    id = id,
    phoneNumber = phoneNumber.orEmpty(),
    default = default ?: false
)

fun Vendor.toMinimalResponse(like: Boolean = false, bookmark: Boolean = false): VendorMinimalResponse = VendorMinimalResponse(
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
    closingTime = closingTime,
    likeCount = likeCount,
    likedByCurrentUser = like,
    bookmarkCount = bookmarkCount,
    bookmarkedByCurrentUser = bookmark
)

fun Vendor.toBookmarkResponse(like: Boolean = false, bookmark: Boolean = false) = VendorBookmarkResponse(
    id = id,
    username = username,
    vendorCode = vendorCode.orEmpty(),
    firstName = firstName.orEmpty(),
    lastName = lastName.orEmpty(),
    chefName = chefName.orEmpty(),
    restaurantName = restaurantName.orEmpty(),
    verified = verified ?: false,
    phoneNumber = vendorPhoneNumber.map { it.toResponse() }.toSet(),
    address = address!!.toResponse(),
    registrationDate = registrationDate ?: dateFormatter.parse("00-00-0000"),
    openingTime = openingTime,
    closingTime = closingTime,
    likeCount = likeCount,
    likedByCurrentUser = like,
    bookmarkCount = bookmarkCount,
    bookmarkedByCurrentUser = bookmark
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

