package com.qinet.feastique.response.order

import com.qinet.feastique.response.ComplementResponse
import com.qinet.feastique.response.food.FoodMinimalResponse
import com.qinet.feastique.response.food.FoodSizeResponse

sealed interface FoodResponseEntity : BaseResponseEntity {
    val food: FoodMinimalResponse
    val complement: ComplementResponse
    val size: FoodSizeResponse
}

