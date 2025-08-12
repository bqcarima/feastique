package com.qinet.feastique.model.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty

data class AddOnDto (

    var id: Long? = null,

    @field:NotBlank(message = "AddOn name cannot be null.")
    @field:NotEmpty(message = "AddOn name cannot be empty.")
    var addOnName: String?,

    var price: Long?
)

