package com.qinet.feastique.model.dto

import com.qinet.feastique.common.validator.phoneNumber.ValidPhoneNumber
import jakarta.validation.constraints.NotBlank
import java.util.UUID

data class PhoneNumberDto(
    var id: UUID? = null,

    @field:ValidPhoneNumber
    var phoneNumber: String,

    val default: Boolean? = false,
)

