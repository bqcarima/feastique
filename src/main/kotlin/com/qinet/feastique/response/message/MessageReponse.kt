package com.qinet.feastique.response.message

import java.time.LocalDateTime
import java.util.UUID

data class MessageResponse(
    val id: UUID,
    val body: String,
    val sentAt: LocalDateTime,
    val senderType: String,
    val replyTo: MessageReplyResponse?,
    val deleted: Boolean
)

data class MessageReplyResponse(
    val id: UUID,
    val body: String,                 // will br null if the replied-to message was deleted
    val senderType: String,
)

data class ConversationResponse(
    val id: UUID,
    val startedAt: LocalDateTime,
    val read: Boolean,                // from the perspective of the requesting party
    val otherPartyName: String,       // chefName/restaurantName for vendor-side; customer-side - username otherwise
    val otherPartyId: UUID,
    val recentMessages: List<MessageResponse>    // last 15-20, newest last
)

data class ConversationSummaryResponse(
    val id: UUID,
    val startedAt: LocalDateTime,
    val read: Boolean,
    val otherPartyName: String,
    val otherPartyId: UUID
)

