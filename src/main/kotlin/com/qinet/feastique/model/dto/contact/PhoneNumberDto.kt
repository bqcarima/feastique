package com.qinet.feastique.model.dto.contact

import com.qinet.feastique.common.validator.phoneNumber.ValidPhoneNumber
import java.util.UUID

data class PhoneNumberDto(
    var id: UUID? = null,

    @field:ValidPhoneNumber
    var phoneNumber: String,

    val default: Boolean? = false
)

