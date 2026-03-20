package com.qinet.feastique.repository.message

import com.qinet.feastique.model.entity.message.Message
import org.springframework.data.domain.Limit
import org.springframework.data.domain.ScrollPosition
import org.springframework.data.domain.Sort
import org.springframework.data.domain.Window
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface MessageRepository : JpaRepository<Message, UUID> {

    fun findAllByConversationId(
        conversationId: UUID,
        scrollPosition: ScrollPosition,
        sort: Sort,
        limit: Limit
    ): Window<Message>

    // Fetch the N most recent messages for a conversation (used on open)
    fun findAllByConversationIdAndCustomerDeletedFalse(
        conversationId: UUID,
        scrollPosition: ScrollPosition,
        sort: Sort,
        limit: Limit
    ): Window<Message>
    fun findAllByConversationIdAndVendorDeletedFalse(
        conversationId: UUID,
        scrollPosition: ScrollPosition,
        sort: Sort,
        limit: Limit
    ): Window<Message>

}

