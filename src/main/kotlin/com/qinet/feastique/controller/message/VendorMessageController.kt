package com.qinet.feastique.controller.message

import com.qinet.feastique.model.dto.messaging.SendMessageDto
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
@RequestMapping("/api/v1/vendors/{vendorId}/conversations")
class VendorMessagingController(
    private val messageService: MessageService,
    private val securityUtility: SecurityUtility
) {

    @GetMapping
    fun scrollConversations(
        @PathVariable vendorId: UUID,
        @RequestParam(required = false) cursor: String?,
        @RequestParam(required = false) size: Int?,
        @AuthenticationPrincipal vendorDetails: UserSecurity
    ): ResponseEntity<WindowResponse<ConversationSummaryResponse>> {
        securityUtility.validatePath(vendorId, vendorDetails)
        return ResponseEntity.ok(messageService.scrollConversations(vendorDetails, cursor, size ?: Constants.DEFAULT_PAGE_SIZE.type))
    }

    @GetMapping("/{conversationId}")
    fun openConversation(
        @PathVariable vendorId: UUID,
        @PathVariable conversationId: UUID,
        @AuthenticationPrincipal vendorDetails: UserSecurity
    ): ResponseEntity<ConversationResponse> {
        securityUtility.validatePath(vendorId, vendorDetails)
        return ResponseEntity.ok(messageService.openConversation(conversationId, vendorDetails))
    }

    @GetMapping("/{conversationId}/messages")
    fun scrollMessages(
        @PathVariable vendorId: UUID,
        @PathVariable conversationId: UUID,
        @RequestParam(required = false) cursor: String?,
        @RequestParam(required = false) size: Int?,
        @AuthenticationPrincipal vendorDetails: UserSecurity
    ): ResponseEntity<WindowResponse<MessageResponse>> {
        securityUtility.validatePath(vendorId, vendorDetails)
        return ResponseEntity.ok(messageService.scrollMessages(conversationId, vendorDetails, cursor, size ?: Constants.DEFAULT_PAGE_SIZE.type))
    }

    @PostMapping("/{conversationId}/messages")
    fun sendMessage(
        @PathVariable vendorId: UUID,
        @PathVariable conversationId: UUID,
        @RequestBody @Valid sendMessageDto: SendMessageDto,
        @AuthenticationPrincipal vendorDetails: UserSecurity
    ): ResponseEntity<MessageResponse> {
        securityUtility.validatePath(vendorId, vendorDetails)
        return ResponseEntity(messageService.sendMessage(conversationId, sendMessageDto, vendorDetails), HttpStatus.CREATED)
    }

    @DeleteMapping("/{conversationId}")
    fun deleteConversation(
        @PathVariable vendorId: UUID,
        @PathVariable conversationId: UUID,
        @AuthenticationPrincipal vendorDetails: UserSecurity
    ): ResponseEntity<String> {
        securityUtility.validatePath(vendorId, vendorDetails)
        messageService.deleteConversation(conversationId, vendorDetails)
        return ResponseEntity.ok("Conversation deleted.")
    }

    @DeleteMapping("/{conversationId}/messages/{messageId}")
    fun deleteMessage(
        @PathVariable vendorId: UUID,
        @PathVariable conversationId: UUID,
        @PathVariable messageId: UUID,
        @AuthenticationPrincipal vendorDetails: UserSecurity
    ): ResponseEntity<String> {
        securityUtility.validatePath(vendorId, vendorDetails)
        messageService.deleteMessage(conversationId, messageId, vendorDetails)
        return ResponseEntity.ok("Message deleted.")
    }
}

