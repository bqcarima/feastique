package com.qinet.feastique.service.message

import com.qinet.feastique.exception.PermissionDeniedException
import com.qinet.feastique.exception.RequestedEntityNotFoundException
import com.qinet.feastique.exception.UserNotFoundException
import com.qinet.feastique.model.dto.messaging.SendMessageDto
import com.qinet.feastique.model.dto.messaging.StartConversationDto
import com.qinet.feastique.model.entity.message.Conversation
import com.qinet.feastique.model.entity.message.Message
import com.qinet.feastique.model.entity.user.Customer
import com.qinet.feastique.model.entity.user.Vendor
import com.qinet.feastique.model.enums.AccountType
import com.qinet.feastique.repository.message.ConversationRepository
import com.qinet.feastique.repository.message.MessageRepository
import com.qinet.feastique.repository.user.CustomerRepository
import com.qinet.feastique.repository.user.VendorRepository
import com.qinet.feastique.security.UserSecurity
import com.qinet.feastique.utility.CursorEncoder
import com.qinet.feastique.utility.SecurityUtility
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.*
import org.springframework.security.core.authority.SimpleGrantedAuthority
import java.util.*

// Shared fixtures

private val MS_CUSTOMER_ID: UUID = UUID.randomUUID()
private val MS_VENDOR_ID: UUID = UUID.randomUUID()
private val MS_CONVERSATION_ID: UUID = UUID.randomUUID()
private val MS_MESSAGE_ID: UUID = UUID.randomUUID()

private fun msCustomer(): Customer = Customer().apply {
    id = MS_CUSTOMER_ID
    username = "jane_doe"
    accountType = AccountType.CUSTOMER
}

private fun msVendor(): Vendor = Vendor().apply {
    id = MS_VENDOR_ID
    username = "sabi_chef"
    chefName = "Sabi Chef"
    restaurantName = "Sabi Foods"
    accountType = AccountType.VENDOR
}

private fun msCustomerSecurity(): UserSecurity = UserSecurity(
    id = MS_CUSTOMER_ID,
    username = "jane_doe",
    password = "hashed",
    userAuthorities = mutableListOf(SimpleGrantedAuthority("ROLE_CUSTOMER"))
)

private fun msVendorSecurity(): UserSecurity = UserSecurity(
    id = MS_VENDOR_ID,
    username = "sabi_chef",
    password = "hashed",
    userAuthorities = mutableListOf(SimpleGrantedAuthority("ROLE_VENDOR"))
)

private fun msConversation(
    customerDeleted: Boolean = false,
    vendorDeleted: Boolean = false
): Conversation = Conversation().apply {
    id = MS_CONVERSATION_ID
    customer = msCustomer()
    vendor = msVendor()
    this.customerDeleted = customerDeleted
    this.vendorDeleted = vendorDeleted
    customerRead = false
    vendorRead = false
}

private fun msMessage(senderType: AccountType = AccountType.CUSTOMER): Message = Message().apply {
    id = MS_MESSAGE_ID
    body = "Hello there"
    this.senderType = senderType
    conversation = msConversation()
    customer = if (senderType == AccountType.CUSTOMER) msCustomer() else null
    vendor = if (senderType == AccountType.VENDOR) msVendor() else null
}


class MessageServiceTest {

    private lateinit var conversationRepository: ConversationRepository
    private lateinit var messageRepository: MessageRepository
    private lateinit var customerRepository: CustomerRepository
    private lateinit var vendorRepository: VendorRepository
    private lateinit var securityUtility: SecurityUtility
    private lateinit var cursorEncoder: CursorEncoder
    private lateinit var messageService: MessageService

    @BeforeEach
    fun setUp() {
        conversationRepository = mock()
        messageRepository = mock()
        customerRepository = mock()
        vendorRepository = mock()
        securityUtility = mock()
        cursorEncoder = mock()

        messageService = MessageService(
            conversationRepository = conversationRepository,
            messageRepository = messageRepository,
            customerRepository = customerRepository,
            vendorRepository = vendorRepository,
            securityUtility = securityUtility,
            cursorEncoder = cursorEncoder
        )
    }


    // saveConversation
    @Nested
    inner class SaveConversation {

        @Test
        fun `saves and returns the conversation`() {
            val conversation = msConversation()
            whenever(conversationRepository.save(conversation)).thenReturn(conversation)

            val result = messageService.saveConversation(conversation)

            assertEquals(conversation, result)
            verify(conversationRepository).save(conversation)
        }
    }


    // startConversation
    @Nested
    inner class StartConversation {

        private val startDto = StartConversationDto(body = "Hi, I have a question.")

        @BeforeEach
        fun stub() {
            whenever(securityUtility.getSingleRole(any())).thenAnswer { invocation ->
                val userSecurity = invocation.getArgument<UserSecurity>(0)
                if (userSecurity.authorities.any { it.authority == "ROLE_CUSTOMER" }) "CUSTOMER" else "VENDOR"
            }
            whenever(customerRepository.findById(MS_CUSTOMER_ID)).thenReturn(Optional.of(msCustomer()))
            whenever(vendorRepository.findById(MS_VENDOR_ID)).thenReturn(Optional.of(msVendor()))
            whenever(conversationRepository.findByCustomerIdAndVendorId(MS_CUSTOMER_ID, MS_VENDOR_ID))
                .thenReturn(null)
            whenever(conversationRepository.save(any())).thenAnswer { it.arguments[0] }
            whenever(messageRepository.save(any())).thenAnswer { it.arguments[0] }
        }

        @Test
        fun `creates conversation and first message when none exists`() {
            val customerSecurity = msCustomerSecurity()
            messageService.startConversation(MS_VENDOR_ID, startDto, customerSecurity)

            verify(conversationRepository, atLeast(1)).save(any())
            verify(messageRepository).save(any())
        }

        @Test
        fun `reuses existing conversation instead of creating a new one`() {
            val customerSecurity = msCustomerSecurity()
            val existing = msConversation()
            whenever(conversationRepository.findByCustomerIdAndVendorId(MS_CUSTOMER_ID, MS_VENDOR_ID))
                .thenReturn(existing)

            messageService.startConversation(MS_VENDOR_ID, startDto, customerSecurity)

            // No new conversation instantiated — the existing one is reused and saved
            verify(conversationRepository, atLeast(1)).save(argThat { id == MS_CONVERSATION_ID })
        }

        @Test
        fun `marks conversation as read for customer and unread for vendor`() {
            val customerSecurity = msCustomerSecurity()
            messageService.startConversation(MS_VENDOR_ID, startDto, customerSecurity)

            verify(conversationRepository, atLeast(1)).save(argThat {
                customerRead && !vendorRead
            })
        }

        @Test
        fun `throws PermissionDeniedException when caller is not a customer`() {
            val vendorSecurity = msVendorSecurity()
            whenever(securityUtility.getSingleRole(vendorSecurity)).thenReturn("VENDOR")

            assertThrows<PermissionDeniedException> {
                messageService.startConversation(MS_VENDOR_ID, startDto, vendorSecurity)
            }
        }

        @Test
        fun `throws UserNotFoundException when customer does not exist`() {
            val customerSecurity = msCustomerSecurity()
            whenever(customerRepository.findById(MS_CUSTOMER_ID)).thenReturn(Optional.empty())

            assertThrows<UserNotFoundException> {
                messageService.startConversation(MS_VENDOR_ID, startDto, customerSecurity)
            }
        }

        @Test
        fun `throws UserNotFoundException when vendor does not exist`() {
            val customerSecurity = msCustomerSecurity()
            whenever(vendorRepository.findById(MS_VENDOR_ID)).thenReturn(Optional.empty())

            assertThrows<UserNotFoundException> {
                messageService.startConversation(MS_VENDOR_ID, startDto, customerSecurity)
            }
        }

        @Test
        fun `saves message body from dto`() {
            val customerSecurity = msCustomerSecurity()
            messageService.startConversation(MS_VENDOR_ID, startDto, customerSecurity)

            verify(messageRepository).save(argThat {
                body == "Hi, I have a question." &&
                        senderType == AccountType.CUSTOMER
            })
        }
    }


    // openConversation
    @Nested
    inner class OpenConversation {

        @BeforeEach
        fun stub() {
            whenever(securityUtility.getSingleRole(any())).thenAnswer { invocation ->
                val userSecurity = invocation.getArgument<UserSecurity>(0)
                if (userSecurity.authorities.any { it.authority == "ROLE_CUSTOMER" }) "CUSTOMER" else "VENDOR"
            }
            whenever(conversationRepository.findById(MS_CONVERSATION_ID))
                .thenReturn(Optional.of(msConversation()))
            whenever(conversationRepository.save(any())).thenAnswer { it.arguments[0] }
            whenever(
                messageRepository.findAllByConversationIdAndCustomerDeletedFalse(any(), any(), any(), any())
            ).thenReturn(mock())
        }

        @Test
        fun `marks conversation as read for the customer`() {
            val customerSecurity = msCustomerSecurity()
            messageService.openConversation(MS_CONVERSATION_ID, customerSecurity)

            verify(conversationRepository).save(argThat { customerRead })
        }

        @Test
        fun `marks conversation as read for the vendor`() {
            val vendorSecurity = msVendorSecurity()
            whenever(
                messageRepository.findAllByConversationIdAndVendorDeletedFalse(any(), any(), any(), any())
            ).thenReturn(mock())

            messageService.openConversation(MS_CONVERSATION_ID, vendorSecurity)

            verify(conversationRepository).save(argThat { vendorRead })
        }

        @Test
        fun `throws RequestedEntityNotFoundException when conversation does not exist`() {
            val customerSecurity = msCustomerSecurity()
            whenever(conversationRepository.findById(MS_CONVERSATION_ID)).thenReturn(Optional.empty())

            assertThrows<RequestedEntityNotFoundException> {
                messageService.openConversation(MS_CONVERSATION_ID, customerSecurity)
            }
        }

        @Test
        fun `throws PermissionDeniedException when customer tries to open another customer's conversation`() {
            val customerSecurity = msCustomerSecurity()
            val otherCustomerConversation = msConversation().apply {
                customer = Customer().apply { id = UUID.randomUUID() }
            }
            whenever(conversationRepository.findById(MS_CONVERSATION_ID))
                .thenReturn(Optional.of(otherCustomerConversation))

            assertThrows<PermissionDeniedException> {
                messageService.openConversation(MS_CONVERSATION_ID, customerSecurity)
            }
        }

        @Test
        fun `throws RequestedEntityNotFoundException when customer has deleted the conversation`() {
            val customerSecurity = msCustomerSecurity()
            whenever(conversationRepository.findById(MS_CONVERSATION_ID))
                .thenReturn(Optional.of(msConversation(customerDeleted = true)))

            assertThrows<RequestedEntityNotFoundException> {
                messageService.openConversation(MS_CONVERSATION_ID, customerSecurity)
            }
        }
    }


    // sendMessage
    @Nested
    inner class SendMessage {

        private val sendDto = SendMessageDto(body = "Ready in 10 minutes!")

        @BeforeEach
        fun stub() {
            whenever(securityUtility.getSingleRole(any())).thenAnswer { invocation ->
                val userSecurity = invocation.getArgument<UserSecurity>(0)
                if (userSecurity.authorities.any { it.authority == "ROLE_CUSTOMER" }) "CUSTOMER" else "VENDOR"
            }
            whenever(conversationRepository.findById(MS_CONVERSATION_ID))
                .thenReturn(Optional.of(msConversation()))
            whenever(conversationRepository.save(any())).thenAnswer { it.arguments[0] }
            whenever(vendorRepository.findById(MS_VENDOR_ID)).thenReturn(Optional.of(msVendor()))
            whenever(customerRepository.findById(MS_CUSTOMER_ID)).thenReturn(Optional.of(msCustomer()))
            whenever(messageRepository.save(any())).thenAnswer { it.arguments[0] }
        }

        @Test
        fun `saves message with correct body and sender type when vendor sends`() {
            val vendorSecurity = msVendorSecurity()
            messageService.sendMessage(MS_CONVERSATION_ID, sendDto, vendorSecurity)

            verify(messageRepository).save(argThat {
                body == "Ready in 10 minutes!" &&
                        senderType == AccountType.VENDOR
            })
        }

        @Test
        fun `saves message with correct body and sender type when customer sends`() {
            val customerSecurity = msCustomerSecurity()
            messageService.sendMessage(MS_CONVERSATION_ID, sendDto, customerSecurity)

            verify(messageRepository).save(argThat {
                body == "Ready in 10 minutes!" &&
                        senderType == AccountType.CUSTOMER
            })
        }

        @Test
        fun `marks conversation unread for the other party when vendor sends`() {
            val vendorSecurity = msVendorSecurity()
            messageService.sendMessage(MS_CONVERSATION_ID, sendDto, vendorSecurity)

            verify(conversationRepository).save(argThat {
                vendorRead && !customerRead
            })
        }

        @Test
        fun `marks conversation unread for the other party when customer sends`() {
            val customerSecurity = msCustomerSecurity()
            messageService.sendMessage(MS_CONVERSATION_ID, sendDto, customerSecurity)

            verify(conversationRepository).save(argThat {
                customerRead && !vendorRead
            })
        }

        @Test
        fun `throws RequestedEntityNotFoundException when conversation does not exist`() {
            val vendorSecurity = msVendorSecurity()
            whenever(conversationRepository.findById(MS_CONVERSATION_ID)).thenReturn(Optional.empty())

            assertThrows<RequestedEntityNotFoundException> {
                messageService.sendMessage(MS_CONVERSATION_ID, sendDto, vendorSecurity)
            }
        }

        @Test
        fun `throws PermissionDeniedException when vendor sends to a conversation that is not theirs`() {
            val vendorSecurity = msVendorSecurity()
            val otherVendorConversation = msConversation().apply {
                vendor = Vendor().apply { id = UUID.randomUUID() }
            }
            whenever(conversationRepository.findById(MS_CONVERSATION_ID))
                .thenReturn(Optional.of(otherVendorConversation))

            assertThrows<PermissionDeniedException> {
                messageService.sendMessage(MS_CONVERSATION_ID, sendDto, vendorSecurity)
            }
        }
    }


    // deleteConversation
    @Nested
    inner class DeleteConversation {

        @BeforeEach
        fun stub() {
            whenever(securityUtility.getSingleRole(any())).thenAnswer { invocation ->
                val userSecurity = invocation.getArgument<UserSecurity>(0)
                if (userSecurity.authorities.any { it.authority == "ROLE_CUSTOMER" }) "CUSTOMER" else "VENDOR"
            }
            whenever(conversationRepository.findById(MS_CONVERSATION_ID))
                .thenReturn(Optional.of(msConversation()))
            whenever(conversationRepository.save(any())).thenAnswer { it.arguments[0] }
        }

        @Test
        fun `sets customerDeleted to true when customer deletes the conversation`() {
            val customerSecurity = msCustomerSecurity()
            messageService.deleteConversation(MS_CONVERSATION_ID, customerSecurity)

            verify(conversationRepository).save(argThat { customerDeleted })
        }

        @Test
        fun `sets vendorDeleted to true when vendor deletes the conversation`() {
            val vendorSecurity = msVendorSecurity()
            messageService.deleteConversation(MS_CONVERSATION_ID, vendorSecurity)

            verify(conversationRepository).save(argThat { vendorDeleted })
        }

        @Test
        fun `throws RequestedEntityNotFoundException when conversation does not exist`() {
            val customerSecurity = msCustomerSecurity()
            whenever(conversationRepository.findById(MS_CONVERSATION_ID)).thenReturn(Optional.empty())

            assertThrows<RequestedEntityNotFoundException> {
                messageService.deleteConversation(MS_CONVERSATION_ID, customerSecurity)
            }
        }
    }


    // deleteMessage
    @Nested
    inner class DeleteMessage {

        @BeforeEach
        fun stub() {
            whenever(securityUtility.getSingleRole(any())).thenAnswer { invocation ->
                val userSecurity = invocation.getArgument<UserSecurity>(0)
                if (userSecurity.authorities.any { it.authority == "ROLE_CUSTOMER" }) "CUSTOMER" else "VENDOR"
            }
            whenever(conversationRepository.findById(MS_CONVERSATION_ID))
                .thenReturn(Optional.of(msConversation()))
            whenever(conversationRepository.save(any())).thenAnswer { it.arguments[0] }
        }

        @Test
        fun `sets customerDeleted to true when customer deletes their own message`() {
            val customerSecurity = msCustomerSecurity()
            val message = msMessage(AccountType.CUSTOMER)
            whenever(messageRepository.findById(MS_MESSAGE_ID)).thenReturn(Optional.of(message))

            messageService.deleteMessage(MS_CONVERSATION_ID, MS_MESSAGE_ID, customerSecurity)

            verify(messageRepository).save(argThat { customerDeleted })
        }

        @Test
        fun `sets vendorDeleted to true when vendor deletes their own message`() {
            val vendorSecurity = msVendorSecurity()
            val message = msMessage(AccountType.VENDOR)
            whenever(messageRepository.findById(MS_MESSAGE_ID)).thenReturn(Optional.of(message))

            messageService.deleteMessage(MS_CONVERSATION_ID, MS_MESSAGE_ID, vendorSecurity)

            verify(messageRepository).save(argThat { vendorDeleted })
        }

        @Test
        fun `throws RequestedEntityNotFoundException when message does not exist`() {
            val customerSecurity = msCustomerSecurity()
            whenever(messageRepository.findById(MS_MESSAGE_ID)).thenReturn(Optional.empty())

            assertThrows<RequestedEntityNotFoundException> {
                messageService.deleteMessage(MS_CONVERSATION_ID, MS_MESSAGE_ID, customerSecurity)
            }
        }

        @Test
        fun `throws PermissionDeniedException when customer tries to delete vendor's message`() {
            val customerSecurity = msCustomerSecurity()
            val vendorMessage = msMessage(AccountType.VENDOR)
            whenever(messageRepository.findById(MS_MESSAGE_ID)).thenReturn(Optional.of(vendorMessage))

            assertThrows<PermissionDeniedException> {
                messageService.deleteMessage(MS_CONVERSATION_ID, MS_MESSAGE_ID, customerSecurity)
            }
        }

        @Test
        fun `throws PermissionDeniedException when vendor tries to delete customer's message`() {
            val vendorSecurity = msVendorSecurity()
            val customerMessage = msMessage(AccountType.CUSTOMER)
            whenever(messageRepository.findById(MS_MESSAGE_ID)).thenReturn(Optional.of(customerMessage))

            assertThrows<PermissionDeniedException> {
                messageService.deleteMessage(MS_CONVERSATION_ID, MS_MESSAGE_ID, vendorSecurity)
            }
        }

        @Test
        fun `throws RequestedEntityNotFoundException when conversation does not exist`() {
            val customerSecurity = msCustomerSecurity()
            whenever(conversationRepository.findById(MS_CONVERSATION_ID)).thenReturn(Optional.empty())

            assertThrows<RequestedEntityNotFoundException> {
                messageService.deleteMessage(MS_CONVERSATION_ID, MS_MESSAGE_ID, customerSecurity)
            }
        }
    }
}

private fun assertEquals(expected: Any?, actual: Any?) {
    org.junit.jupiter.api.Assertions.assertEquals(expected, actual)
}

