package com.qinet.feastique.model.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty

data class ComplementDto (
    @field:NotBlank(message = "Complement name cannot be null.")
    @field:NotEmpty(message = "Complement name cannot be empty.")
    var complementName: String?,

    @field:NotBlank(message = "Price cannot be null.")
    @field:NotEmpty(message = "Price cannot be empty.")
    var price: String?
)

