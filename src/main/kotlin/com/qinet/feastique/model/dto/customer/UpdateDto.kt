package com.qinet.feastique.model.dto.customer

import com.fasterxml.jackson.annotation.JsonFormat
import java.time.LocalDate

data class UpdateDto(
    val id: Long,
    val username: String,
    val firstName: String,
    val lastName: String,

    @param:JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    val dob: LocalDate,

    @param:JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    val anniversary: LocalDate,
    val image: String
)
