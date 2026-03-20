package com.qinet.feastique.model.dto.messaging


import jakarta.validation.constraints.NotBlank
import java.util.UUID

data class SendMessageDto(
    @field:NotBlank(message = "Message body cannot be blank.")
    val body: String? = "",

    val replyToMessageId: UUID? = null
)

