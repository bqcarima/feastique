package com.qinet.feastique.service.message

import com.qinet.feastique.common.mapper.toResponse
import com.qinet.feastique.common.mapper.toSummaryResponse
import com.qinet.feastique.exception.PermissionDeniedException
import com.qinet.feastique.exception.RequestedEntityNotFoundException
import com.qinet.feastique.exception.UserNotFoundException
import com.qinet.feastique.model.dto.messaging.SendMessageDto
import com.qinet.feastique.model.dto.messaging.StartConversationDto
import com.qinet.feastique.model.entity.message.Conversation
import com.qinet.feastique.model.entity.message.Message
import com.qinet.feastique.model.enums.AccountType
import com.qinet.feastique.model.enums.Constants
import com.qinet.feastique.repository.message.ConversationRepository
import com.qinet.feastique.repository.message.MessageRepository
import com.qinet.feastique.repository.user.CustomerRepository
import com.qinet.feastique.repository.user.VendorRepository
import com.qinet.feastique.response.message.ConversationResponse
import com.qinet.feastique.response.message.ConversationSummaryResponse
import com.qinet.feastique.response.message.MessageResponse
import com.qinet.feastique.response.pagination.WindowResponse
import com.qinet.feastique.security.UserSecurity
import com.qinet.feastique.utility.CursorEncoder
import com.qinet.feastique.utility.SecurityUtility
import org.springframework.data.domain.Limit
import org.springframework.data.domain.ScrollPosition
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*
import kotlin.jvm.optionals.getOrElse

@Service
class MessageService(
    private val conversationRepository: ConversationRepository,
    private val messageRepository: MessageRepository,
    private val customerRepository: CustomerRepository,
    private val vendorRepository: VendorRepository,
    private val securityUtility: SecurityUtility,
    private val cursorEncoder: CursorEncoder

) {

    @Transactional
    fun saveConversation(conversation: Conversation): Conversation {
        return conversationRepository.save(conversation)
    }
    @Transactional(readOnly = true)
    fun scrollConversations(
        userDetails: UserSecurity,
        cursor: String?,
        size: Int = Constants.DEFAULT_PAGE_SIZE.type

    ): WindowResponse<ConversationSummaryResponse> {

        val role = securityUtility.getSingleRole(userDetails)
        val currentOffset: Long = cursor?.toLongOrNull() ?: 0L
        val scrollPosition = if (currentOffset == 0L)
            ScrollPosition.offset()
        else ScrollPosition.offset(currentOffset)

        val sort = Sort.by("startedAt").descending()
        val window = when (role) {
            "CUSTOMER" -> conversationRepository.findAllByCustomerIdAndCustomerDeletedFalse(
                userDetails.id,
                scrollPosition,
                sort,
                Limit.of(size)
            )

            "VENDOR" -> conversationRepository.findAllByVendorIdAndVendorDeletedFalse(
                userDetails.id,
                scrollPosition,
                sort,
                Limit.of(size)
            )

            else -> throw IllegalArgumentException("Invalid role. Contact customer support if issue persists.")
        }

        return window.map { it.toSummaryResponse(role) }.toResponse(currentOffset) { cursorEncoder.encodeOffset(it) }
    }

    @Transactional
    fun startConversation(
        vendorId: UUID,
        startConversationDto: StartConversationDto,
        userDetails: UserSecurity

    ): ConversationResponse {
        val role = securityUtility.getSingleRole(userDetails)
        if (role != "CUSTOMER") throw PermissionDeniedException("Only customers can start conversations.")

        val customer = customerRepository.findById(userDetails.id)
            .orElseThrow { UserNotFoundException("Customer not found.") }

        val vendor = vendorRepository.findById(vendorId)
            .orElseThrow { UserNotFoundException("Vendor not found.") }

        // Check if a conversation already exists between this customer and vendor
        val conversation = conversationRepository.findByCustomerIdAndVendorId(customer.id, vendor.id)
            ?: Conversation().apply {
                this.customer = customer
                this.vendor = vendor
            }

        conversation.customerDeleted = false
        conversation.vendorDeleted = false
        val savedConversation = saveConversation(conversation)

        val replyTo = resolveReplyTo(startConversationDto.replyToMessageId, savedConversation.id)
        val message = Message().apply {
            this.body = requireNotNull(startConversationDto.body) { "You cannot send an empty message." }
            this.conversation = savedConversation
            this.senderType = AccountType.CUSTOMER
            this.customer = customer
            this.replyTo = replyTo
        }
        messageRepository.save(message)

        savedConversation.vendorRead = false
        savedConversation.customerRead = true
        saveConversation(savedConversation)
        return savedConversation.toResponse(listOf(message), role)
    }

    @Transactional
    fun openConversation(
        conversationId: UUID,
        userDetails: UserSecurity

    ): ConversationResponse {
        val role = securityUtility.getSingleRole(userDetails)
        val conversation = findAndAuthorize(conversationId, userDetails, role)

        when (role) {
            "CUSTOMER" -> conversation.customerRead = true
            "VENDOR" -> conversation.vendorRead = true
        }
        saveConversation(conversation)
        val recentMessages = fetchMessages(conversationId, role, null)
        return conversation.toResponse(recentMessages, role)
    }

    @Transactional(readOnly = true)
    fun scrollMessages(
        conversationId: UUID,
        userDetails: UserSecurity,
        cursor: String?,
        size: Int = Constants.DEFAULT_PAGE_SIZE.type
    ): WindowResponse<MessageResponse> {
        val role = securityUtility.getSingleRole(userDetails)
        findAndAuthorize(conversationId, userDetails, role)

        val currentOffset = cursor?.toLongOrNull() ?: 0L
        val scrollPosition = cursorToScrollPosition(cursor)
        val sort = Sort.by("sentAt").ascending()
        val limit = Limit.of(size)

        val window = when (role) {
            "VENDOR" -> messageRepository.findAllByConversationIdAndVendorDeletedFalse(
                conversationId, scrollPosition, sort, limit
            )
            "CUSTOMER" -> messageRepository.findAllByConversationIdAndCustomerDeletedFalse(
                conversationId, scrollPosition, sort, limit
            )
            else -> throw IllegalArgumentException("Invalid role. Contact customer support if issue persists.")
        }

        return window.map { it.toResponse() }.toResponse(currentOffset) {cursorEncoder.encodeOffset(it)}
    }

    @Transactional
    fun sendMessage(
        conversationId: UUID,
        sendMessageDto: SendMessageDto,
        userDetails: UserSecurity

    ): MessageResponse {
        val role = securityUtility.getSingleRole(userDetails)
        val conversation = findAndAuthorize(conversationId, userDetails, role)

        when (role) {
            "CUSTOMER" -> {
                conversation.customerDeleted = false
                conversation.customerRead = true
                conversation.vendorRead = false
            }

            "VENDOR" -> {
                conversation.vendorDeleted = false
                conversation.vendorRead = true
                conversation.customerRead = false
            }
        }

        val replyTo = resolveReplyTo(sendMessageDto.replyToMessageId, conversationId)
        val message = Message().apply {
            this.body = requireNotNull(sendMessageDto.body) { "You cannot send an empty message." }
            this.conversation = conversation
            this.senderType = if (role == "VENDOR") AccountType.VENDOR else AccountType.CUSTOMER

            this.customer = if (role == "CUSTOMER") customerRepository.findById(userDetails.id)
                .getOrElse { throw UserNotFoundException("Customer not found.") } else null

            this.vendor = if (role == "VENDOR") vendorRepository.findById(userDetails.id)
                .getOrElse { throw UserNotFoundException("Vendor not found.") } else null

            this.replyTo = replyTo
        }

        saveConversation(conversation)
        return messageRepository.save(message).toResponse()
    }

    @Transactional
    fun deleteConversation(conversationId: UUID, userDetails: UserSecurity) {
        val role = securityUtility.getSingleRole(userDetails)
        val conversation = findAndAuthorize(conversationId, userDetails, role)
        when (role) {
            "VENDOR" -> conversation.vendorDeleted = true
            "CUSTOMER" -> conversation.customerDeleted = true
        }
        saveConversation(conversation)
    }

    @Transactional
    fun deleteMessage(conversationId: UUID, messageId: UUID, userDetails: UserSecurity) {
        val role = securityUtility.getSingleRole(userDetails)
        findAndAuthorize(conversationId, userDetails, role)

        val message = messageRepository.findById(messageId)
            .orElseThrow { RequestedEntityNotFoundException("Message not found.") }

        val isSender = when (role) {
            "VENDOR" -> message.senderType == AccountType.VENDOR && message.vendor?.id == userDetails.id
            "CUSTOMER" -> message.senderType == AccountType.CUSTOMER && message.customer?.id == userDetails.id
            else -> false
        }
        if (!isSender) throw PermissionDeniedException("You can only delete your own messages.")

        when (role) {
            "VENDOR" -> message.vendorDeleted = true
            "CUSTOMER" -> message.customerDeleted = true
        }
        messageRepository.save(message)
    }

    private fun findAndAuthorize(
        conversationId: UUID,
        userDetails: UserSecurity,
        role: String
    ): Conversation {
        val conversation = conversationRepository.findById(conversationId)
            .getOrElse { throw RequestedEntityNotFoundException("Conversation not found.") }

        val belongs = when (role) {
            "CUSTOMER" -> conversation.customer.id == userDetails.id
            "VENDOR" -> conversation.vendor.id == userDetails.id
            else -> false
        }
        if (!belongs) throw PermissionDeniedException("You do not have access to this conversation.")

        val deleted = when (role) {
            "CUSTOMER" -> conversation.customerDeleted
            "VENDOR" -> conversation.vendorDeleted
            else -> true
        }
        if (deleted) throw RequestedEntityNotFoundException("Conversation not found.")
        return conversation
    }

    private fun fetchMessages(
        conversationId: UUID,
        role: String,
        cursor: String?,
        size: Int = Constants.DEFAULT_PAGE_SIZE.type

    ): List<Message> {
        val scrollPosition = cursorToScrollPosition(cursor)
        val sort = Sort.by("sentAt").ascending()

        return when (role) {
            "CUSTOMER" -> messageRepository.findAllByConversationIdAndCustomerDeletedFalse(
                conversationId, scrollPosition, sort, Limit.of(size)
            )

            "VENDOR" -> messageRepository.findAllByConversationIdAndVendorDeletedFalse(
                conversationId, scrollPosition, sort, Limit.of(size)
            )

            else -> throw IllegalArgumentException("Invalid role. Contact customer support if issue persists.")
        }.toList()
    }

    private fun resolveReplyTo(replyToMessageId: UUID?, conversationId: UUID): Message? {
        if (replyToMessageId == null) return null
        val replyTo = messageRepository.findById(replyToMessageId)
            .getOrElse { throw RequestedEntityNotFoundException("Message being replied to not found.") }

        if (replyTo.conversation.id != conversationId)
            throw PermissionDeniedException("Cannot reply to a message from a different conversation.")

        return replyTo
    }

    private fun cursorToScrollPosition(cursor: String?): ScrollPosition {
        val offset = cursor?.toLongOrNull() ?: 0L
        return if (offset == 0L) ScrollPosition.offset() else ScrollPosition.offset(offset)
    }
}

