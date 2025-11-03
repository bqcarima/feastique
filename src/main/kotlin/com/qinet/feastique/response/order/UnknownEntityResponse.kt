package com.qinet.feastique.response.order

import com.github.f4b6a3.uuid.UuidCreator
import java.util.*

data class UnknownEntityResponse(
    // Generate a random UUID that is never saved to fulfil non-null constraint
    override val id: UUID = UuidCreator.getTimeOrdered(),
    override val quantity: Int,
    override val totalAmount: Long,
    override val orderType: String
) : BaseResponseEntity

