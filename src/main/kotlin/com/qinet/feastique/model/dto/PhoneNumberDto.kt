package com.qinet.feastique.model.dto

import jakarta.validation.constraints.NotBlank

data class PhoneNumberDto(
    var id: Long? = null,

    @field:NotBlank(message = "Beverage name cannot be empty.")
    var phoneNumber: String,

    val default: Boolean? = false,
)
