package com.qinet.feastique.model.dto

import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull
import java.util.UUID

data class ComplementDto (

    var id: UUID? = null,

    @field:NotBlank(message = "Complement name cannot be null.")
    @field:NotEmpty(message = "Complement name cannot be empty.")
    var complementName: String?,

    @field:NotNull(message = "Price cannot be null.")
    @field:Min(value = 1, message = "Price cannot be 0.)")
    var price: Long?
)

