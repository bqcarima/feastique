package com.qinet.feastique.model.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty

data class PostDto(

    val id: Long? = null,

    @field:NotBlank(message = "Title cannot be blank")
    @field:NotEmpty(message = "Title cannot be empty")
    val title: String? = "",

    var body: String? = null,

    @field:NotBlank(message = "Image cannot be blank")
    @field:NotEmpty(message = "Image cannot be blank")
    var image: String? = null
)

