package com.qinet.feastique.common.mapper

import com.qinet.feastique.model.entity.addOn.FoodAddOn
import com.qinet.feastique.model.entity.complement.Complement
import com.qinet.feastique.model.entity.complement.FoodComplement
import com.qinet.feastique.model.entity.food.Food
import com.qinet.feastique.model.entity.food.FoodImage
import com.qinet.feastique.model.entity.food.FoodOrderType
import com.qinet.feastique.model.entity.food.FoodSize
import com.qinet.feastique.response.AddOnResponse
import com.qinet.feastique.response.ComplementResponse
import com.qinet.feastique.response.FoodImageResponse
import com.qinet.feastique.response.FoodOrderTypeResponse
import com.qinet.feastique.response.FoodResponse
import com.qinet.feastique.response.FoodSizeResponse

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
 * into a comprehensive [FoodResponse] DTO.
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
    images = this.foodImage.map { img ->
        FoodImageResponse(
            id = img.id ?: 0,
            imageUrl = img.imageUrl.orEmpty(),
            foodId = this.id ?: 0 // use parent food id, not img.food.id
        )
    },
    size = this.foodSize.map { it.toResponse() },
    complements = this.foodComplement.map { it.toResponse() },
    addOn = this.foodAddOn.map { it.toResponse() },
    orderType = this.foodOrderType.map { it.toResponse() }
)

/**
 * Maps a [FoodAddOn] entity to its API DTO [AddOnResponse].
 *
 * @receiver FoodAddOn entity to map from.
 * @return AddOnResponse DTO with id, name, and price.
 */
fun FoodAddOn.toResponse() = AddOnResponse(
    id = this.addOn.id ?: 0,
    addOnName = this.addOn.addOnName.orEmpty(),
    price = this.addOn.price ?: 0
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
    price = this.complement.price ?: 0
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
