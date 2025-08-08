package com.qinet.feastique.model.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty

data class LoginDto(

    @field:NotBlank(message = "Username cannot be null.")
    @field:NotEmpty(message = "Username cannot be empty.")
    val username: String,

    @field:NotBlank(message = "First name cannot be null.")
    @field:NotEmpty(message = "First name cannot be empty.")
    val password: String,

)
