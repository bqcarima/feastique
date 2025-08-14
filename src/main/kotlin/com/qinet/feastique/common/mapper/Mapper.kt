package com.qinet.feastique.common.mapper

import com.qinet.feastique.model.entity.addOn.AddOn
import com.qinet.feastique.model.entity.addOn.FoodAddOn
import com.qinet.feastique.model.entity.complement.Complement
import com.qinet.feastique.model.entity.complement.FoodComplement
import com.qinet.feastique.model.entity.discount.Discount
import com.qinet.feastique.model.entity.discount.FoodDiscount
import com.qinet.feastique.model.entity.food.*
import com.qinet.feastique.response.*
import com.qinet.feastique.response.food.FoodAvailabilityResponse
import com.qinet.feastique.response.food.FoodDiscountResponse
import com.qinet.feastique.response.food.FoodImageResponse
import com.qinet.feastique.response.food.FoodOrderTypeResponse
import com.qinet.feastique.response.food.FoodResponse
import com.qinet.feastique.response.food.FoodSizeResponse
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Converts a [Complement] entity to its API response DTO [ComplementResponse].
 *
 * @receiver Complement entity to map from.
 * @return ComplementResponse DTO with id, name, and price.
 */
fun Complement.toResponse() = ComplementResponse(
    id = this.id ?: 0,
    name = this.complementName.orEmpty(),
    price = this.price ?: 0
)

/**
 * Converts a [Food] entity along with all its related entities
 * into a comprehensive [com.qinet.feastique.response.food.FoodResponse] DTO.
 *
 * This includes:
 * - Basic food properties (name, description, price)
 * - Associated vendor details (id and chef name)
 * - Collections of related entities mapped to their respective DTOs:
 *   - Images, Sizes, Complements, AddOns, and Order Types.
 *
 * @receiver Food entity to map from.
 * @return FoodResponse DTO representing full food details for API.
 */
fun Food.toResponse() = FoodResponse(
    id = this.id ?: 0,
    foodName = this.foodName.orEmpty(),
    vendorId = this.vendor.id ?: 0,
    vendorName = this.vendor.chefName.orEmpty(),
    mainCourse = this.mainCourse.orEmpty(),
    description = this.description.orEmpty(),
    basePrice = this.basePrice ?: 0,

    // Map each related entity collection to its response DTO
    images = this.foodImage.map { image ->
        FoodImageResponse(
            id = image.id ?: 0,
            imageUrl = image.imageUrl.orEmpty(),
            foodId = this.id ?: 0 // use parent food id, not image.food.id
        )
    },
    size = this.foodSize.map { it.toResponse() },
    complements = this.foodComplement.map { it.toResponse() },
    addOn = this.foodAddOn.map { it.toResponse() },
    orderType = this.foodOrderType.map { it.toResponse() },
    availability = this.foodAvailability.map { it.toResponse() },
    discount = this.foodDiscount.map { it.toResponse() }
)

/**
 * Maps an [AddOn] entity to its API DTO [AddOnResponse].
 *
 * @receiver AddOn entity to map from.
 * @return AddOnResponse DTO with id, name, and price.
 */
fun AddOn.toResponse() = AddOnResponse(
    id = this.id ?: 0,
    addOnName = this.addOnName.orEmpty(),
    price = this.price ?: 0
)

fun FoodAddOn.toResponse() = AddOnResponse(
    id = this.addOn.id ?: 0,
    addOnName = this.addOn.addOnName.orEmpty(),
    price = this.addOn.price ?: 0
)

/**
 * Maps a [FoodAvailability] entity to its API DTO [com.qinet.feastique.response.food.FoodAvailabilityResponse].
 *
 * @receiver FoodAvailability entity to map from.
 * @return FoodAvailabilityResponse DTO with id, and name..
 */
fun FoodAvailability.toResponse() = FoodAvailabilityResponse(
    id = this.id ?: 0,
    availability = this.availability.orEmpty()
)

/**
 * Maps a [FoodComplement] entity to [ComplementResponse] by delegating to the
 * wrapped [Complement] entity.
 *
 * @receiver FoodComplement entity to map from.
 * @return ComplementResponse DTO.
 */
fun FoodComplement.toResponse() = ComplementResponse(
    id = this.complement.id ?: 0,
    name = this.complement.complementName.orEmpty(),
    price = this.complement.price ?: 0,

)

val dateFormatter = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())

/**
 * Converts a [Discount] entity to its response DTO.
 *
 * @receiver Discount entity to map from.
 * @return DiscountResponse DTO with id, discount name, percentage, start date, end date, and parent food id.
 */
fun Discount.toResponse(): DiscountResponse = DiscountResponse(
    id = this.id ?: 0,
    discountName = this.discountName.orEmpty(),
    percentage = this.percentage ?: 0,
    startDate = this.startDate ?: dateFormatter.parse("00-00-000"),
    endDate = this.endDate ?: dateFormatter.parse("00-00-0000")
)

/**
 * Converts a [FoodDiscount] entity to its response DTO.
 *
 * @receiver Discount entity to map from.
 * @return FoodDiscountResponse DTO with id, discount name, percentage, start date, end date,  and active status.
 */
fun FoodDiscount.toResponse(): FoodDiscountResponse = FoodDiscountResponse(
    id = this.id ?: 0,
    discountName = this.discount.discountName.orEmpty(),
    percentage = this.discount.percentage ?: 0,
    startDate = this.discount.startDate ?: dateFormatter.parse("00-00-000"),
    endDate = this.discount.endDate ?: dateFormatter.parse("00-00-0000"),
    active = this.active ?: false
)


/**
 * Converts a [FoodImage] entity to its response DTO.
 *
 * @receiver FoodImage entity to map from.
 * @return FoodImageResponse DTO with id, URL, and parent food id.
 */
fun FoodImage.toResponse(): FoodImageResponse = FoodImageResponse(
    id = this.id ?: 0,
    imageUrl = this.imageUrl.orEmpty(),
    foodId = this.food.id ?: 0
)

/**
 * Converts a [FoodOrderType] entity to its response DTO.
 *
 * @receiver FoodOrderType entity to map from.
 * @return FoodOrderTypeResponse DTO with id, parent food id, and order type string.
 */
fun FoodOrderType.toResponse(): FoodOrderTypeResponse = FoodOrderTypeResponse(
    id = this.id ?: 0,
    foodId = this.food.id ?: 0,
    orderType = this.orderType.orEmpty()
)

/**
 * Maps a [FoodSize] entity to its response DTO.
 *
 * @receiver FoodSize entity to map from.
 * @return FoodSizeResponse DTO with id and size string.
 */
fun FoodSize.toResponse(): FoodSizeResponse = FoodSizeResponse(
    id = this.id ?: 0,
    size = this.size.orEmpty()
)

