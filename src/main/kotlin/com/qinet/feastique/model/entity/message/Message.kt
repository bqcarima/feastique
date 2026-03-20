package com.qinet.feastique.model.entity.message

import com.fasterxml.jackson.annotation.JsonIgnore
import com.github.f4b6a3.uuid.UuidCreator
import com.qinet.feastique.model.entity.user.Customer
import com.qinet.feastique.model.entity.user.Vendor
import com.qinet.feastique.model.enums.AccountType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.time.LocalDateTime
import java.util.UUID


@Entity
@Table(name = "messages")
class Message {

    @Id
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "uuid")
    var id: UUID = UuidCreator.getTimeOrdered()

    @Column(updatable = false)
    var body: String? = null

    @Column(name = "sent_at", updatable = false)
    var sentAt: LocalDateTime = LocalDateTime.now()

    @Column(name = "sender_type", nullable = false)
    @Enumerated(EnumType.STRING)
    lateinit var senderType: AccountType

    @Column(name = "vendor_deleted", nullable = false)
    var vendorDeleted: Boolean = false

    @Column(name = "customer_deleted", nullable = false)
    var customerDeleted: Boolean = false

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conversation_id", nullable = false)
    @JsonIgnore
    lateinit var conversation: Conversation

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vendor_id", nullable = true)
    @JsonIgnore
    var vendor: Vendor? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = true)
    @JsonIgnore
    var customer: Customer? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reply_to_message_id", nullable = true)
    @JsonIgnore
    var replyTo: Message? = null
}

