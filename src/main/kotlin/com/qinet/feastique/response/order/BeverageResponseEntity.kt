package com.qinet.feastique.response.order

import com.qinet.feastique.response.BeverageResponse

sealed interface BeverageResponseEntity : BaseResponseEntity {
    val beverage: BeverageResponse
}

