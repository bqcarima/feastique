package com.qinet.feastique.model.dto

import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import java.util.UUID

data class PostDto(

    val id: UUID? = null,

    @field:NotBlank(message = "Title cannot be blank")
    @field:NotEmpty(message = "Title cannot be empty")
    val title: String? = "",

    var body: String? = null,
    var postImages: Set<@Valid ImageDto>,
)

