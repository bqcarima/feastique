package com.qinet.feastique.service.customer


import com.qinet.feastique.exception.DuplicateFoundException
import com.qinet.feastique.exception.UserNotFoundException
import com.qinet.feastique.model.dto.user.CustomerUpdateDto
import com.qinet.feastique.model.dto.user.PasswordChangeDto
import com.qinet.feastique.model.entity.authentication.RefreshToken
import com.qinet.feastique.model.entity.user.Customer
import com.qinet.feastique.model.enums.AccountType
import com.qinet.feastique.repository.contact.CustomerPhoneNumberRepository
import com.qinet.feastique.repository.contact.VendorPhoneNumberRepository
import com.qinet.feastique.repository.user.CustomerRepository
import com.qinet.feastique.response.token.TokenPairResponse
import com.qinet.feastique.security.PasswordEncoder
import com.qinet.feastique.security.UserSecurity
import com.qinet.feastique.service.authentication.RefreshTokenService
import com.qinet.feastique.service.user.UserSessionService
import com.qinet.feastique.utility.JwtUtility
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.*
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.core.authority.SimpleGrantedAuthority
import java.time.LocalDate
import java.util.*


// Shared data

private val CS_JANE_ID = UUID.randomUUID()
private val CS_JANE_DOB = LocalDate.of(2000, 8, 22)
private val CS_JANE_ANNIVERSARY = LocalDate.of(2029, 5, 15)

private const val CS_NEW_ACCESS_TOKEN = "new.header.payload.access-sig"
private const val CS_NEW_REFRESH_TOKEN = "new.header.payload.refresh-sig"
private const val CS_TOKEN_IDENTIFIER = "cs-tok-id-abc123"
private val CS_TOKEN_EXPIRY = System.currentTimeMillis() + 900_000L

private val CS_NEW_TOKEN_PAIR = TokenPairResponse(
    accessToken = CS_NEW_ACCESS_TOKEN,
    refreshToken = CS_NEW_REFRESH_TOKEN
)

private fun csJaneCustomer(
    username: String = "jane_doe"
): Customer = Customer().apply {
    id = CS_JANE_ID
    this.username = username
    firstName = "Jane"
    lastName = "Doe"
    dob = CS_JANE_DOB
    anniversary = CS_JANE_ANNIVERSARY
    accountType = AccountType.CUSTOMER
    password = "hashed_passWord123"
}

private fun csJaneSecurity(
    username: String = "jane_doe"
): UserSecurity = UserSecurity(
    id = CS_JANE_ID,
    username = username,
    password = "hashed_passWord123",
    userAuthorities = mutableListOf(SimpleGrantedAuthority("ROLE_CUSTOMER"))
)


// CustomerService — update, password change, get
class CustomerServiceProfileTest {

    private lateinit var customerRepository: CustomerRepository
    private lateinit var customerAddressService: CustomerAddressService
    private lateinit var customerPhoneNumberRepository: CustomerPhoneNumberRepository
    private lateinit var vendorPhoneNumberRepository: VendorPhoneNumberRepository
    private lateinit var passwordEncoder: PasswordEncoder
    private lateinit var jwtUtility: JwtUtility
    private lateinit var userSessionService: UserSessionService
    private lateinit var refreshTokenService: RefreshTokenService
    private lateinit var authManager: AuthenticationManager
    private lateinit var customerService: CustomerService

    @BeforeEach
    fun setUp() {
        customerRepository = mock()
        customerAddressService = mock()
        customerPhoneNumberRepository = mock()
        vendorPhoneNumberRepository = mock()
        passwordEncoder = mock()
        jwtUtility = mock()
        userSessionService = mock()
        refreshTokenService = mock()
        authManager = mock()

        customerService = CustomerService(
            authManager = authManager,
            customerRepository = customerRepository,
            customerAddressService = customerAddressService,
            customerPhoneNumberRepository = customerPhoneNumberRepository,
            vendorPhoneNumberRepository = vendorPhoneNumberRepository,
            passwordEncoder = passwordEncoder,
            jwtUtility = jwtUtility,
            userSessionService = userSessionService,
            refreshTokenService = refreshTokenService
        )
    }


    // getCustomerById
    @Nested
    inner class GetCustomerById {

        @Test
        fun `returns customer when found`() {
            val customer = csJaneCustomer()
            whenever(customerRepository.findById(CS_JANE_ID))
                .thenReturn(Optional.of(customer))

            val result = customerService.getCustomerById(csJaneSecurity())

            assertEquals(CS_JANE_ID, result.id)
            assertEquals("jane_doe", result.username)
        }

        @Test
        fun `throws UserNotFoundException when customer does not exist`() {
            whenever(customerRepository.findById(CS_JANE_ID))
                .thenReturn(Optional.empty())

            assertThrows<UserNotFoundException> {
                customerService.getCustomerById(csJaneSecurity())
            }
        }
    }


    // getCustomerWithPhoneNumberAndAddress
    @Nested
    inner class GetCustomerWithPhoneNumberAndAddress {

        @Test
        fun `returns customer with phone and address when found`() {
            val customer = csJaneCustomer()
            whenever(customerRepository.findByCustomerByIdWithPhoneNumberAndAddress(CS_JANE_ID))
                .thenReturn(customer)

            val result = customerService.getCustomerWithPhoneNumberAndAddress(csJaneSecurity())

            assertNotNull(result)
            assertEquals(CS_JANE_ID, result.id)
        }

        @Test
        fun `throws UserNotFoundException when customer is not found`() {
            whenever(customerRepository.findByCustomerByIdWithPhoneNumberAndAddress(CS_JANE_ID))
                .thenReturn(null)

            assertThrows<UserNotFoundException> {
                customerService.getCustomerWithPhoneNumberAndAddress(csJaneSecurity())
            }
        }
    }


    // updateCustomer
    @Nested
    inner class UpdateCustomer {

        private val updateDto = CustomerUpdateDto(
            username = "jane_doe",
            firstName = "Jane",
            lastName = "Doe",
            dob = CS_JANE_DOB,
            anniversary = CS_JANE_ANNIVERSARY,
            image = "https://cdn.feastique.com/jane.jpg",
        )

        @BeforeEach
        fun stubUpdate() {
            val customer = csJaneCustomer()
            whenever(customerRepository.findById(CS_JANE_ID)).thenReturn(Optional.of(customer))
            whenever(customerRepository.save(any())).thenReturn(customer)
            whenever(jwtUtility.generateTokenPair(CS_JANE_ID, "jane_doe", AccountType.CUSTOMER))
                .thenReturn(CS_NEW_TOKEN_PAIR)
            whenever(jwtUtility.getTokenIdentifier(CS_NEW_ACCESS_TOKEN)).thenReturn(CS_TOKEN_IDENTIFIER)
            whenever(jwtUtility.getExpirationEpochMillis(CS_NEW_ACCESS_TOKEN)).thenReturn(CS_TOKEN_EXPIRY)
            whenever(jwtUtility.getUserId(CS_NEW_ACCESS_TOKEN)).thenReturn(CS_JANE_ID)
            whenever(jwtUtility.getUserType(CS_NEW_ACCESS_TOKEN)).thenReturn("CUSTOMER")
            whenever(jwtUtility.parseToken(CS_JANE_ID, "CUSTOMER", CS_NEW_REFRESH_TOKEN))
                .thenReturn(
                    RefreshToken(
                        customerId = CS_JANE_ID,
                        vendorId = null,
                        expiresAt = Date(System.currentTimeMillis() + 2_592_000_000L),
                        hashedToken = "hashed_new_refresh"
                    )
                )
        }

        @Test
        fun `returns new token pair after update`() {
            val result = customerService.updateCustomer(updateDto, csJaneSecurity())
            assertEquals(CS_NEW_ACCESS_TOKEN, result.accessToken)
            assertEquals(CS_NEW_REFRESH_TOKEN, result.refreshToken)
        }

        @Test
        fun `saves updated customer fields`() {
            customerService.updateCustomer(updateDto, csJaneSecurity())
            verify(customerRepository).save(argThat {
                firstName == "Jane" &&
                        lastName == "Doe" &&
                        dob == CS_JANE_DOB &&
                        anniversary == CS_JANE_ANNIVERSARY &&
                        image == "https://cdn.feastique.com/jane.jpg"
            })
        }

        @Test
        fun `resets sessions after update`() {
            customerService.updateCustomer(updateDto, csJaneSecurity())
            verify(userSessionService).resetSessions(CS_JANE_ID, AccountType.CUSTOMER.name)
        }

        @Test
        fun `creates new session after update`() {
            customerService.updateCustomer(updateDto, csJaneSecurity())
            verify(userSessionService).createSession(
                tokenIdentifier = CS_TOKEN_IDENTIFIER,
                userId = CS_JANE_ID,
                userType = "CUSTOMER",
                expiresAtEpocMillis = CS_TOKEN_EXPIRY
            )
        }

        @Test
        fun `stores new refresh token after update`() {
            customerService.updateCustomer(updateDto, csJaneSecurity())
            verify(refreshTokenService).storeRefreshToken(argThat {
                customerId == CS_JANE_ID &&
                        vendorId == null &&
                        hashedToken == "hashed_new_refresh"
            })
        }

        @Test
        fun `generates new token pair after update`() {
            customerService.updateCustomer(updateDto, csJaneSecurity())
            verify(jwtUtility).generateTokenPair(CS_JANE_ID, "jane_doe", AccountType.CUSTOMER)
        }
    }


    // updateCustomer — username change
    @Nested
    inner class UpdateCustomerUsernameChange {

        private val updateDtoNewUsername = CustomerUpdateDto(
            username = "jane_doe_updated",
            firstName = "Jane",
            lastName = "Doe",
            dob = CS_JANE_DOB,
            anniversary = CS_JANE_ANNIVERSARY,
            image = "https://cdn.feastique.com/jane.jpg"
        )

        @BeforeEach
        fun stubUsernameChange() {
            val customer = csJaneCustomer()
            whenever(customerRepository.findById(CS_JANE_ID)).thenReturn(Optional.of(customer))
            whenever(customerRepository.existsByUsernameIgnoreCase("jane_doe_updated")).thenReturn(false)
            whenever(customerRepository.save(any())).thenReturn(
                csJaneCustomer(username = "jane_doe_updated")
            )
            whenever(jwtUtility.generateTokenPair(CS_JANE_ID, "jane_doe_updated", AccountType.CUSTOMER))
                .thenReturn(CS_NEW_TOKEN_PAIR)
            whenever(jwtUtility.getTokenIdentifier(CS_NEW_ACCESS_TOKEN)).thenReturn(CS_TOKEN_IDENTIFIER)
            whenever(jwtUtility.getExpirationEpochMillis(CS_NEW_ACCESS_TOKEN)).thenReturn(CS_TOKEN_EXPIRY)
            whenever(jwtUtility.getUserId(CS_NEW_ACCESS_TOKEN)).thenReturn(CS_JANE_ID)
            whenever(jwtUtility.getUserType(CS_NEW_ACCESS_TOKEN)).thenReturn("CUSTOMER")
            whenever(jwtUtility.parseToken(CS_JANE_ID, "CUSTOMER", CS_NEW_REFRESH_TOKEN))
                .thenReturn(
                    RefreshToken(
                        customerId = CS_JANE_ID,
                        vendorId = null,
                        expiresAt = Date(System.currentTimeMillis() + 2_592_000_000L),
                        hashedToken = "hashed_new_refresh"
                    )
                )
        }

        @Test
        fun `checks username availability when username changes`() {
            customerService.updateCustomer(updateDtoNewUsername, csJaneSecurity())
            verify(customerRepository).existsByUsernameIgnoreCase("jane_doe_updated")
        }

        @Test
        fun `throws DuplicateFoundException when new username is already taken`() {
            whenever(customerRepository.existsByUsernameIgnoreCase("jane_doe_updated")).thenReturn(true)

            assertThrows<DuplicateFoundException> {
                customerService.updateCustomer(updateDtoNewUsername, csJaneSecurity())
            }
        }

        @Test
        fun `does not check username availability when username is unchanged`() {
            val sameUsernameDto = CustomerUpdateDto(
                username = "jane_doe",
                firstName = "Jane",
                lastName = "Doe",
                dob = CS_JANE_DOB,
                anniversary = CS_JANE_ANNIVERSARY,
                image = "https://cdn.feastique.com/jane.jpg"
            )
            val customer = csJaneCustomer()
            whenever(customerRepository.findById(CS_JANE_ID)).thenReturn(Optional.of(customer))
            whenever(customerRepository.save(any())).thenReturn(customer)
            whenever(jwtUtility.generateTokenPair(CS_JANE_ID, "jane_doe", AccountType.CUSTOMER))
                .thenReturn(CS_NEW_TOKEN_PAIR)
            whenever(jwtUtility.getTokenIdentifier(CS_NEW_ACCESS_TOKEN)).thenReturn(CS_TOKEN_IDENTIFIER)
            whenever(jwtUtility.getExpirationEpochMillis(CS_NEW_ACCESS_TOKEN)).thenReturn(CS_TOKEN_EXPIRY)
            whenever(jwtUtility.getUserId(CS_NEW_ACCESS_TOKEN)).thenReturn(CS_JANE_ID)
            whenever(jwtUtility.getUserType(CS_NEW_ACCESS_TOKEN)).thenReturn("CUSTOMER")
            whenever(jwtUtility.parseToken(any(), any(), any())).thenReturn(
                RefreshToken(
                    customerId = CS_JANE_ID,
                    vendorId = null,
                    expiresAt = Date(System.currentTimeMillis() + 2_592_000_000L),
                    hashedToken = "hashed_new_refresh"
                )
            )

            customerService.updateCustomer(sameUsernameDto, csJaneSecurity())

            verify(customerRepository, never()).existsByUsernameIgnoreCase(any())
        }
    }


    // changePassword
    @Nested
    inner class ChangePassword {

        private val passwordChangeDto = PasswordChangeDto(
            currentPassword = "passWord123",
            newPassword = "newPassWord456",
            confirmedNewPassword = "newPassWord456"
        )

        @BeforeEach
        fun stubChangePassword() {
            val customer = csJaneCustomer()
            whenever(customerRepository.findById(CS_JANE_ID)).thenReturn(Optional.of(customer))
            whenever(passwordEncoder.matches("passWord123", "hashed_passWord123")).thenReturn(true)
            whenever(passwordEncoder.encode("newPassWord456")).thenReturn("hashed_newPassWord456")
            whenever(customerRepository.save(any())).thenReturn(customer)
        }

        @Test
        fun `saves customer with new encoded password`() {
            customerService.changePassword(passwordChangeDto, csJaneSecurity())
            verify(customerRepository).save(argThat {
                password == "hashed_newPassWord456"
            })
        }

        @Test
        fun `encodes the new password before saving`() {
            customerService.changePassword(passwordChangeDto, csJaneSecurity())
            verify(passwordEncoder).encode("newPassWord456")
        }

        @Test
        fun `throws IllegalArgumentException when current password is wrong`() {
            whenever(passwordEncoder.matches("passWord123", "hashed_passWord123")).thenReturn(false)

            assertThrows<IllegalArgumentException> {
                customerService.changePassword(passwordChangeDto, csJaneSecurity())
            }
        }

        @Test
        fun `throws IllegalArgumentException when new password and confirmation do not match`() {
            val mismatchedDto = PasswordChangeDto(
                currentPassword = "passWord123",
                newPassword = "newPassWord456",
                confirmedNewPassword = "differentPassWord789"
            )

            assertThrows<IllegalArgumentException> {
                customerService.changePassword(mismatchedDto, csJaneSecurity())
            }
        }

        @Test
        fun `does not save customer when current password is wrong`() {
            whenever(passwordEncoder.matches("passWord123", "hashed_passWord123")).thenReturn(false)

            runCatching { customerService.changePassword(passwordChangeDto, csJaneSecurity()) }

            verify(customerRepository, never()).save(any())
        }

        @Test
        fun `does not save customer when new passwords do not match`() {
            val mismatchedDto = PasswordChangeDto(
                currentPassword = "passWord123",
                newPassword = "newPassWord456",
                confirmedNewPassword = "differentPassWord789"
            )

            runCatching { customerService.changePassword(mismatchedDto, csJaneSecurity()) }

            verify(customerRepository, never()).save(any())
        }

        @Test
        fun `does not encode new password when current password is wrong`() {
            whenever(passwordEncoder.matches("passWord123", "hashed_passWord123")).thenReturn(false)

            runCatching { customerService.changePassword(passwordChangeDto, csJaneSecurity()) }

            verify(passwordEncoder, never()).encode(any())
        }
    }
}

