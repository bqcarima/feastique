package com.qinet.feastique.controller.message

import com.qinet.feastique.model.dto.messaging.SendMessageDto
import com.qinet.feastique.model.dto.messaging.StartConversationDto
import com.qinet.feastique.model.enums.Constants
import com.qinet.feastique.response.message.ConversationResponse
import com.qinet.feastique.response.message.ConversationSummaryResponse
import com.qinet.feastique.response.message.MessageResponse
import com.qinet.feastique.response.pagination.WindowResponse
import com.qinet.feastique.security.UserSecurity
import com.qinet.feastique.service.message.MessageService
import com.qinet.feastique.utility.SecurityUtility
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/v1/customers/{customerId}/conversations")
class CustomerMessagingController(
    private val messageService: MessageService,
    private val securityUtility: SecurityUtility
) {

    @GetMapping
    fun scrollConversations(
        @PathVariable customerId: UUID,
        @RequestParam(required = false) cursor: String?,
        @RequestParam(required = false) size: Int?,
        @AuthenticationPrincipal customerDetails: UserSecurity
    ): ResponseEntity<WindowResponse<ConversationSummaryResponse>> {
        securityUtility.validatePath(customerId, customerDetails)
        return ResponseEntity.ok(messageService.scrollConversations(customerDetails, cursor, size ?: Constants.DEFAULT_PAGE_SIZE.type))
    }

    @PostMapping("/vendors/{vendorId}")
    fun startConversation(
        @PathVariable customerId: UUID,
        @PathVariable vendorId: UUID,
        @RequestBody @Valid startConversationDto: StartConversationDto,
        @AuthenticationPrincipal customerDetails: UserSecurity
    ): ResponseEntity<ConversationResponse> {
        securityUtility.validatePath(customerId, customerDetails)
        return ResponseEntity(messageService.startConversation(vendorId, startConversationDto, customerDetails), HttpStatus.CREATED)
    }

    @GetMapping("/{conversationId}")
    fun openConversation(
        @PathVariable customerId: UUID,
        @PathVariable conversationId: UUID,
        @AuthenticationPrincipal customerDetails: UserSecurity
    ): ResponseEntity<ConversationResponse> {
        securityUtility.validatePath(customerId, customerDetails)
        return ResponseEntity.ok(messageService.openConversation(conversationId, customerDetails))
    }

    @GetMapping("/{conversationId}/messages")
    fun scrollMessages(
        @PathVariable customerId: UUID,
        @PathVariable conversationId: UUID,
        @RequestParam(required = false) cursor: String?,
        @RequestParam(required = false) size: Int?,
        @AuthenticationPrincipal customerDetails: UserSecurity
    ): ResponseEntity<WindowResponse<MessageResponse>> {
        securityUtility.validatePath(customerId, customerDetails)
        return ResponseEntity.ok(messageService.scrollMessages(conversationId, customerDetails, cursor, size ?: Constants.DEFAULT_PAGE_SIZE.type))
    }

    @PostMapping("/{conversationId}/messages")
    fun sendMessage(
        @PathVariable customerId: UUID,
        @PathVariable conversationId: UUID,
        @RequestBody @Valid sendMessageDto: SendMessageDto,
        @AuthenticationPrincipal customerDetails: UserSecurity
    ): ResponseEntity<MessageResponse> {
        securityUtility.validatePath(customerId, customerDetails)
        return ResponseEntity(messageService.sendMessage(conversationId, sendMessageDto, customerDetails), HttpStatus.CREATED)
    }

    @DeleteMapping("/{conversationId}")
    fun deleteConversation(
        @PathVariable customerId: UUID,
        @PathVariable conversationId: UUID,
        @AuthenticationPrincipal customerDetails: UserSecurity
    ): ResponseEntity<String> {
        securityUtility.validatePath(customerId, customerDetails)
        messageService.deleteConversation(conversationId, customerDetails)
        return ResponseEntity.ok("Conversation deleted.")
    }

    @DeleteMapping("/{conversationId}/messages/{messageId}")
    fun deleteMessage(
        @PathVariable customerId: UUID,
        @PathVariable conversationId: UUID,
        @PathVariable messageId: UUID,
        @AuthenticationPrincipal customerDetails: UserSecurity
    ): ResponseEntity<String> {
        securityUtility.validatePath(customerId, customerDetails)
        messageService.deleteMessage(conversationId, messageId, customerDetails)
        return ResponseEntity.ok("Message deleted.")
    }
}

