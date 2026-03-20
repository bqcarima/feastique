package com.qinet.feastique.repository.message

import com.qinet.feastique.model.entity.message.Conversation
import org.springframework.data.domain.Limit
import org.springframework.data.domain.ScrollPosition
import org.springframework.data.domain.Sort
import org.springframework.data.domain.Window
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface ConversationRepository : JpaRepository<Conversation, UUID> {

    fun findAllByCustomerIdAndCustomerDeletedFalse(
        customerId: UUID,
        scrollPosition: ScrollPosition,
        sort: Sort,
        limit: Limit
    ) : Window<Conversation>

    fun findAllByVendorIdAndVendorDeletedFalse(
        vendorId: UUID,
        scrollPosition: ScrollPosition,
        sort: Sort,
        limit: Limit
    ) : Window<Conversation>

    // Prevent duplicate conversations between the same vendor and customer
    fun findByCustomerIdAndVendorId(customerId: UUID, vendorId: UUID): Conversation?
}

