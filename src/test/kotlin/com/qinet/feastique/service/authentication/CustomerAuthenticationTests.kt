package com.qinet.feastique.service.authentication

import com.qinet.feastique.exception.PhoneNumberUnavailableException
import com.qinet.feastique.exception.UsernameUnavailableException
import com.qinet.feastique.model.dto.LoginDto
import com.qinet.feastique.model.dto.LogoutDto
import com.qinet.feastique.model.dto.user.CustomerSignupDto
import com.qinet.feastique.model.entity.address.CustomerAddress
import com.qinet.feastique.model.entity.authentication.RefreshToken
import com.qinet.feastique.model.entity.authentication.UserSession
import com.qinet.feastique.model.entity.contact.CustomerPhoneNumber
import com.qinet.feastique.model.entity.user.Customer
import com.qinet.feastique.model.enums.AccountType
import com.qinet.feastique.model.enums.Region
import com.qinet.feastique.repository.authentication.RefreshTokenRepository
import com.qinet.feastique.repository.authentication.UserSessionRepository
import com.qinet.feastique.repository.contact.CustomerPhoneNumberRepository
import com.qinet.feastique.repository.contact.VendorPhoneNumberRepository
import com.qinet.feastique.repository.user.CustomerRepository
import com.qinet.feastique.response.token.AccessTokenResponse
import com.qinet.feastique.response.token.TokenPairResponse
import com.qinet.feastique.security.HashEncoder
import com.qinet.feastique.security.PasswordEncoder
import com.qinet.feastique.security.UserSecurity
import com.qinet.feastique.service.customer.CustomerAddressService
import com.qinet.feastique.service.customer.CustomerService
import com.qinet.feastique.service.user.UserSessionService
import com.qinet.feastique.service.vendor.VendorService
import com.qinet.feastique.utility.JwtUtility
import io.jsonwebtoken.MalformedJwtException
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.*
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority
import java.time.LocalDate
import java.util.*

// Shared data

private val JANE_ID = UUID.randomUUID()
private val JANE_DOB = LocalDate.of(2000, 8, 22)
private val JANE_ANNIVERSARY = LocalDate.of(2029, 5, 15)

private val JANE_SIGNUP_DTO = CustomerSignupDto(
    username = "jane_doe",
    firstName = "Jane",
    lastName = "Doe",
    dob = JANE_DOB,
    anniversary = JANE_ANNIVERSARY,
    phoneNumber = "673456789",
    password = "passWord123",
    country = "Cameroon",
    region = Region.LITTORAL.type,
    city = "Douala",
    neighbourhood = "Akwa",
    streetName = "Street 1",
    directions = "Near the market",
    longitude = "9.70",
    latitude = "4.05"
)

private val JANE_LOGIN_DTO = LoginDto(
    username = "jane_doe",
    password = "passWord123"
)

private const val FAKE_ACCESS_TOKEN = "header.payload.access-sig"
private const val FAKE_REFRESH_TOKEN = "header.payload.refresh-sig"
private const val TOKEN_IDENTIFIER = "tok-id-abc123"
private const val USER_TYPE_CUSTOMER = "CUSTOMER"
private val TOKEN_EXPIRY_MILLIS = System.currentTimeMillis() + 900_000L  // +15 min

private val FAKE_TOKEN_PAIR = TokenPairResponse(
    accessToken = FAKE_ACCESS_TOKEN,
    refreshToken = FAKE_REFRESH_TOKEN
)

private fun janeCustomer(): Customer = Customer().apply {
    id = JANE_ID
    username = "jane_doe"
    firstName = "Jane"
    lastName = "Doe"
    dob = JANE_DOB
    anniversary = JANE_ANNIVERSARY
    accountType = AccountType.CUSTOMER
    password = "hashed_passWord123"
}

private fun janeSecurity(): UserSecurity = UserSecurity(
    id = JANE_ID,
    username = "jane_doe",
    password = "hashed_passWord123",
    userAuthorities = mutableListOf(SimpleGrantedAuthority("ROLE_CUSTOMER"))
)

private fun janeSession(): UserSession = UserSession(
    tokenIdentifier = TOKEN_IDENTIFIER,
    userId = JANE_ID,
    userType = USER_TYPE_CUSTOMER,
    expiresAtEpochMillis = TOKEN_EXPIRY_MILLIS
)

private fun janeRefreshToken(): RefreshToken = RefreshToken(
    customerId = JANE_ID,
    vendorId = null,
    expiresAt = Date(System.currentTimeMillis() + 2_592_000_000L),
    hashedToken = "hashed_refresh"
)


class CustomerServiceTest {

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


    @Nested
    inner class SignupHappyPath {

        @BeforeEach
        fun stubHappyPath() {
            val customer = janeCustomer()
            whenever(customerRepository.existsByUsernameIgnoreCase("jane_doe")).thenReturn(false)
            whenever(customerPhoneNumberRepository.existsByPhoneNumber("673456789")).thenReturn(false)
            whenever(vendorPhoneNumberRepository.existsByPhoneNumber("673456789")).thenReturn(false)
            whenever(passwordEncoder.encode("passWord123")).thenReturn("hashed_passWord123")
            whenever(customerRepository.save(any())).thenReturn(customer)
            whenever(customerAddressService.saveAddress(any())).thenReturn(
                CustomerAddress().apply { this.customer = customer }
            )
            whenever(customerPhoneNumberRepository.save(any())).thenReturn(
                CustomerPhoneNumber().apply { this.customer = customer }
            )
        }

        @Test
        fun `returns saved customer with correct username`() {
            val result = customerService.signupCustomer(JANE_SIGNUP_DTO)
            assertEquals("jane_doe", result.username)
        }

        @Test
        fun `returns saved customer with correct first and last name`() {
            val result = customerService.signupCustomer(JANE_SIGNUP_DTO)
            assertEquals("Jane", result.firstName)
            assertEquals("Doe", result.lastName)
        }

        @Test
        fun `encodes password before persisting`() {
            customerService.signupCustomer(JANE_SIGNUP_DTO)
            verify(passwordEncoder).encode("passWord123")
        }

        @Test
        fun `sets accountType to CUSTOMER`() {
            customerService.signupCustomer(JANE_SIGNUP_DTO)
            verify(customerRepository, atLeastOnce()).save(argThat {
                accountType == AccountType.CUSTOMER
            })
        }

        @Test
        fun `saves customer at least twice - initial then after linking address and phone`() {
            customerService.signupCustomer(JANE_SIGNUP_DTO)
            verify(customerRepository, atLeast(2)).save(any())
        }

        @Test
        fun `persists phone 673456789 as default`() {
            customerService.signupCustomer(JANE_SIGNUP_DTO)
            verify(customerPhoneNumberRepository).save(argThat {
                phoneNumber == "673456789" && default == true
            })
        }

        @Test
        fun `persists address with all fields from jane dto`() {
            customerService.signupCustomer(JANE_SIGNUP_DTO)
            verify(customerAddressService).saveAddress(argThat {
                country == "Cameroon" &&
                        region == Region.LITTORAL &&
                        city == "Douala" &&
                        neighbourhood == "Akwa" &&
                        streetName == "Street 1" &&
                        directions == "Near the market" &&
                        longitude == "9.70" &&
                        latitude == "4.05" &&
                        default == true
            })
        }

        @Test
        fun `stores dob 22-08-2000 and anniversary 15-05-2029`() {
            val result = customerService.signupCustomer(JANE_SIGNUP_DTO)
            assertEquals(JANE_DOB, result.dob)
            assertEquals(JANE_ANNIVERSARY, result.anniversary)
        }
    }


    @Nested
    inner class SignupDuplicateUsername {

        @BeforeEach
        fun stubTakenUsername() {
            whenever(customerRepository.existsByUsernameIgnoreCase("jane_doe")).thenReturn(true)
        }

        @Test
        fun `throws UsernameUnavailableException`() {
            assertThrows<UsernameUnavailableException> {
                customerService.signupCustomer(JANE_SIGNUP_DTO)
            }
        }

        @Test
        fun `short-circuits before phone duplicate check`() {
            assertThrows<UsernameUnavailableException> {
                customerService.signupCustomer(JANE_SIGNUP_DTO)
            }
            verify(customerPhoneNumberRepository, never()).existsByPhoneNumber(any())
            verify(vendorPhoneNumberRepository, never()).existsByPhoneNumber(any())
        }

        @Test
        fun `does not persist anything`() {
            assertThrows<UsernameUnavailableException> {
                customerService.signupCustomer(JANE_SIGNUP_DTO)
            }
            verify(customerRepository, never()).save(any())
            verify(customerAddressService, never()).saveAddress(any())
            verify(customerPhoneNumberRepository, never()).save(any())
        }
    }


    @Nested
    inner class SignupDuplicatePhone {

        @BeforeEach
        fun stubUniqueUsername() {
            whenever(customerRepository.existsByUsernameIgnoreCase("jane_doe")).thenReturn(false)
        }

        @Test
        fun `throws PhoneNumberUnavailableException when number taken by another customer`() {
            whenever(customerPhoneNumberRepository.existsByPhoneNumber("673456789")).thenReturn(true)
            whenever(vendorPhoneNumberRepository.existsByPhoneNumber("673456789")).thenReturn(false)

            assertThrows<PhoneNumberUnavailableException> {
                customerService.signupCustomer(JANE_SIGNUP_DTO)
            }
        }

        @Test
        fun `throws PhoneNumberUnavailableException when number taken by a vendor`() {
            whenever(customerPhoneNumberRepository.existsByPhoneNumber("673456789")).thenReturn(false)
            whenever(vendorPhoneNumberRepository.existsByPhoneNumber("673456789")).thenReturn(true)

            assertThrows<PhoneNumberUnavailableException> {
                customerService.signupCustomer(JANE_SIGNUP_DTO)
            }
        }

        @Test
        fun `does not persist anything when phone is a duplicate`() {
            whenever(customerPhoneNumberRepository.existsByPhoneNumber("673456789")).thenReturn(true)
            whenever(vendorPhoneNumberRepository.existsByPhoneNumber("673456789")).thenReturn(false)

            assertThrows<PhoneNumberUnavailableException> {
                customerService.signupCustomer(JANE_SIGNUP_DTO)
            }
            verify(customerRepository, never()).save(any())
            verify(customerAddressService, never()).saveAddress(any())
            verify(customerPhoneNumberRepository, never()).save(any())
        }
    }


    @Nested
    inner class LoginHappyPath {

        private lateinit var mockAuthentication: Authentication

        @BeforeEach
        fun stubLogin() {
            mockAuthentication = mock()
            whenever(mockAuthentication.principal).thenReturn(janeSecurity())
            whenever(authManager.authenticate(any())).thenReturn(mockAuthentication)
            whenever(
                jwtUtility.generateTokenPair(JANE_ID, "jane_doe", AccountType.CUSTOMER)
            ).thenReturn(FAKE_TOKEN_PAIR)
        }

        @Test
        fun `returns token pair on valid credentials`() {
            val result = customerService.login(JANE_LOGIN_DTO)
            assertEquals(FAKE_ACCESS_TOKEN, result.accessToken)
            assertEquals(FAKE_REFRESH_TOKEN, result.refreshToken)
        }

        @Test
        fun `delegates authentication to AuthenticationManager`() {
            customerService.login(JANE_LOGIN_DTO)
            verify(authManager).authenticate(argThat<UsernamePasswordAuthenticationToken> {
                principal == "jane_doe" &&
                        credentials == "passWord123"
            })
        }

        @Test
        fun `generates token pair using customer id and username`() {
            customerService.login(JANE_LOGIN_DTO)
            verify(jwtUtility).generateTokenPair(JANE_ID, "jane_doe", AccountType.CUSTOMER)
        }
    }


    @Nested
    inner class LoginBadCredentials {

        @Test
        fun `throws BadCredentialsException on wrong password`() {
            whenever(authManager.authenticate(any()))
                .thenThrow(BadCredentialsException("Bad credentials"))

            assertThrows<BadCredentialsException> {
                customerService.login(LoginDto(username = "jane_doe", password = "wrong"))
            }
        }

        @Test
        fun `does not generate any token when authentication fails`() {
            whenever(authManager.authenticate(any()))
                .thenThrow(BadCredentialsException("Bad credentials"))

            runCatching { customerService.login(LoginDto("jane_doe", "wrong")) }

            verify(jwtUtility, never()).generateTokenPair(any(), any(), any())
        }
    }
}

// AuthenticationService — login, logout, refresh

class AuthenticationServiceTest {

    private lateinit var customerService: CustomerService
    private lateinit var vendorService: VendorService
    private lateinit var jwtUtility: JwtUtility
    private lateinit var userSessionService: UserSessionService
    private lateinit var refreshTokenService: RefreshTokenService
    private lateinit var authService: AuthenticationService

    @BeforeEach
    fun setUp() {
        customerService = mock()
        vendorService = mock()
        jwtUtility = mock()
        userSessionService = mock()
        refreshTokenService = mock()

        authService = AuthenticationService(
            customerService = customerService,
            vendorService = vendorService,
            jwtUtility = jwtUtility,
            userSessionService = userSessionService,
            refreshTokenService = refreshTokenService
        )
    }


    @Nested
    inner class SignupDelegation {

        @Test
        fun `handleCustomerSignup delegates to customerService`() {
            val expected = janeCustomer()
            whenever(customerService.signupCustomer(JANE_SIGNUP_DTO)).thenReturn(expected)

            val result = authService.handleCustomerSignup(JANE_SIGNUP_DTO)

            assertEquals(expected, result)
            verify(customerService).signupCustomer(JANE_SIGNUP_DTO)
        }
    }


    @Nested
    inner class CustomerLogin {

        @BeforeEach
        fun stubLoginDependencies() {
            whenever(customerService.login(JANE_LOGIN_DTO)).thenReturn(FAKE_TOKEN_PAIR)
            whenever(jwtUtility.getTokenIdentifier(FAKE_ACCESS_TOKEN)).thenReturn(TOKEN_IDENTIFIER)
            whenever(jwtUtility.getExpirationEpochMillis(FAKE_ACCESS_TOKEN)).thenReturn(TOKEN_EXPIRY_MILLIS)
            whenever(jwtUtility.getUserId(FAKE_ACCESS_TOKEN)).thenReturn(JANE_ID)
            whenever(jwtUtility.getUserType(FAKE_ACCESS_TOKEN)).thenReturn(USER_TYPE_CUSTOMER)
            whenever(jwtUtility.parseToken(JANE_ID, USER_TYPE_CUSTOMER, FAKE_REFRESH_TOKEN))
                .thenReturn(janeRefreshToken())
        }

        @Test
        fun `handleCustomerLogin returns the token pair from customerService`() {
            val result = authService.handleCustomerLogin(JANE_LOGIN_DTO)
            assertEquals(FAKE_ACCESS_TOKEN, result.accessToken)
            assertEquals(FAKE_REFRESH_TOKEN, result.refreshToken)
        }

        @Test
        fun `handleCustomerLogin resets existing sessions before creating a new one`() {
            authService.handleCustomerLogin(JANE_LOGIN_DTO)

            val order = inOrder(userSessionService)
            order.verify(userSessionService).resetSessions(JANE_ID, USER_TYPE_CUSTOMER)
            order.verify(userSessionService).createSession(
                tokenIdentifier = TOKEN_IDENTIFIER,
                userId = JANE_ID,
                userType = USER_TYPE_CUSTOMER,
                expiresAtEpocMillis = TOKEN_EXPIRY_MILLIS
            )
        }

        @Test
        fun `handleCustomerLogin stores refresh token`() {
            authService.handleCustomerLogin(JANE_LOGIN_DTO)
            verify(refreshTokenService).storeRefreshToken(argThat {
                customerId  == JANE_ID          &&
                        vendorId    == null             &&
                        hashedToken == "hashed_refresh"
            })
        }

        @Test
        fun `handleCustomerLogin creates a session with correct token identifier and expiry`() {
            authService.handleCustomerLogin(JANE_LOGIN_DTO)
            verify(userSessionService).createSession(
                tokenIdentifier = TOKEN_IDENTIFIER,
                userId = JANE_ID,
                userType = USER_TYPE_CUSTOMER,
                expiresAtEpocMillis = TOKEN_EXPIRY_MILLIS
            )
        }

        @Test
        fun `handleCustomerLogin does not create session when customerService throws`() {
            whenever(customerService.login(JANE_LOGIN_DTO))
                .thenThrow(BadCredentialsException("Bad credentials"))

            runCatching { authService.handleCustomerLogin(JANE_LOGIN_DTO) }

            verify(userSessionService, never()).createSession(any(), any(), any(), any())
            verify(refreshTokenService, never()).storeRefreshToken(any())
        }
    }


    @Nested
    inner class TokenRefresh {

        private val newAccessToken = "header.payload.new-access-sig"
        private val newTokenId = "new-tok-id-xyz"
        private val newExpiry = System.currentTimeMillis() + 900_000L

        @BeforeEach
        fun stubRefresh() {
            whenever(jwtUtility.refresh(FAKE_REFRESH_TOKEN))
                .thenReturn(AccessTokenResponse(newAccessToken))
            whenever(jwtUtility.getTokenIdentifier(newAccessToken)).thenReturn(newTokenId)
            whenever(jwtUtility.getExpirationEpochMillis(newAccessToken)).thenReturn(newExpiry)
            whenever(jwtUtility.getUserId(newAccessToken)).thenReturn(JANE_ID)
            whenever(jwtUtility.getUserType(newAccessToken)).thenReturn(USER_TYPE_CUSTOMER)
        }

        @Test
        fun `handleRefresh returns new access token`() {
            val result = authService.handleRefresh(FAKE_REFRESH_TOKEN)
            assertEquals(newAccessToken, result.accessToken)
        }

        @Test
        fun `handleRefresh delegates to jwtUtility refresh`() {
            authService.handleRefresh(FAKE_REFRESH_TOKEN)
            verify(jwtUtility).refresh(FAKE_REFRESH_TOKEN)
        }

        @Test
        fun `handleRefresh creates a new session for the new access token`() {
            authService.handleRefresh(FAKE_REFRESH_TOKEN)
            verify(userSessionService).createSession(
                tokenIdentifier = newTokenId,
                userId = JANE_ID,
                userType = USER_TYPE_CUSTOMER,
                expiresAtEpocMillis = newExpiry
            )
        }

        @Test
        fun `handleRefresh does not create session when jwtUtility throws`() {
            whenever(jwtUtility.refresh(FAKE_REFRESH_TOKEN))
                .thenThrow(RuntimeException("Invalid refresh token"))

            runCatching { authService.handleRefresh(FAKE_REFRESH_TOKEN) }

            verify(userSessionService, never()).createSession(any(), any(), any(), any())
        }
    }


    @Nested
    inner class CustomerLogout {

        private val bearerHeader = "Bearer $FAKE_ACCESS_TOKEN"

        @BeforeEach
        fun stubLogout() {
            whenever(jwtUtility.getTokenIdentifier(FAKE_ACCESS_TOKEN)).thenReturn(TOKEN_IDENTIFIER)
        }

        @Test
        fun `handleLogout deletes session when Authorization header is present`() {
            authService.handleLogout(bearerHeader, null)
            verify(userSessionService).deleteSession(TOKEN_IDENTIFIER)
        }

        @Test
        fun `handleLogout revokes refresh token when LogoutDto is present`() {
            val dto = LogoutDto(accessToken = FAKE_ACCESS_TOKEN, refreshToken = FAKE_REFRESH_TOKEN)
            authService.handleLogout(null, dto)
            verify(refreshTokenService).revokeByRefreshToken(FAKE_REFRESH_TOKEN)
        }

        @Test
        fun `handleLogout deletes session AND revokes refresh token when both are provided`() {
            val dto = LogoutDto(accessToken = FAKE_ACCESS_TOKEN, refreshToken = FAKE_REFRESH_TOKEN)
            authService.handleLogout(bearerHeader, dto)

            verify(userSessionService).deleteSession(TOKEN_IDENTIFIER)
            verify(refreshTokenService).revokeByRefreshToken(FAKE_REFRESH_TOKEN)
        }

        @Test
        fun `handleLogout is a no-op when both header and dto are null`() {
            authService.handleLogout(null, null)

            verify(userSessionService, never()).deleteSession(any())
            verify(refreshTokenService, never()).revokeByRefreshToken(any())
        }

        @Test
        fun `handleLogout ignores non-Bearer Authorization header`() {
            authService.handleLogout("Basic dXNlcjpwYXNz", null)

            verify(userSessionService, never()).deleteSession(any())
        }

        @Test
        fun `handleLogout throws MalformedJwtException when access token in header is malformed`() {
            whenever(jwtUtility.getTokenIdentifier("bad.token"))
                .thenThrow(MalformedJwtException("Malformed"))

            assertThrows<MalformedJwtException> {
                authService.handleLogout("Bearer bad.token", null)
            }
        }

        @Test
        fun `handleLogout does not delete session when only refresh token is provided`() {
            val dto = LogoutDto(accessToken = FAKE_ACCESS_TOKEN, refreshToken = FAKE_REFRESH_TOKEN)
            authService.handleLogout(null, dto)

            verify(userSessionService, never()).deleteSession(any())
        }
    }
}

// UserSessionService — session lifecycle

class UserSessionServiceTest {

    private lateinit var userSessionRepository: UserSessionRepository
    private lateinit var refreshTokenService: RefreshTokenService
    private lateinit var userSessionService: UserSessionService

    @BeforeEach
    fun setUp() {
        userSessionRepository = mock()
        refreshTokenService = mock()
        userSessionService = UserSessionService(userSessionRepository, refreshTokenService)
    }

    @Nested
    inner class CreateSession {

        @Test
        fun `createSession persists a UserSession with correct fields`() {
            val session = janeSession()
            whenever(userSessionRepository.save(any())).thenReturn(session)

            val result = userSessionService.createSession(
                tokenIdentifier = TOKEN_IDENTIFIER,
                userId = JANE_ID,
                userType = USER_TYPE_CUSTOMER,
                expiresAtEpocMillis = TOKEN_EXPIRY_MILLIS
            )

            assertEquals(TOKEN_IDENTIFIER, result.tokenIdentifier)
            assertEquals(JANE_ID, result.userId)
            assertEquals(USER_TYPE_CUSTOMER, result.userType)
            assertEquals(TOKEN_EXPIRY_MILLIS, result.expiresAtEpochMillis)
        }

        @Test
        fun `createSession calls repository save`() {
            whenever(userSessionRepository.save(any())).thenReturn(janeSession())

            userSessionService.createSession(TOKEN_IDENTIFIER, JANE_ID, USER_TYPE_CUSTOMER, TOKEN_EXPIRY_MILLIS)

            verify(userSessionRepository).save(any())
        }
    }

    @Nested
    inner class GetSession {

        @Test
        fun `getSession returns session when token identifier exists`() {
            val session = janeSession()
            whenever(userSessionRepository.findByTokenIdentifier(TOKEN_IDENTIFIER)).thenReturn(session)

            val result = userSessionService.getSession(TOKEN_IDENTIFIER)

            assertEquals(session, result)
        }

        @Test
        fun `getSession returns null when token identifier does not exist`() {
            whenever(userSessionRepository.findByTokenIdentifier("unknown")).thenReturn(null)

            assertNull(userSessionService.getSession("unknown"))
        }
    }

    @Nested
    inner class DeleteSession {

        @Test
        fun `deleteSession removes existing session by token identifier`() {
            whenever(userSessionRepository.existsByTokenIdentifier(TOKEN_IDENTIFIER)).thenReturn(true)

            userSessionService.deleteSession(TOKEN_IDENTIFIER)

            verify(userSessionRepository).deleteByTokenIdentifier(TOKEN_IDENTIFIER)
        }

        @Test
        fun `deleteSession is a no-op when session does not exist`() {
            whenever(userSessionRepository.existsByTokenIdentifier("ghost")).thenReturn(false)

            userSessionService.deleteSession("ghost")

            verify(userSessionRepository, never()).deleteByTokenIdentifier(any())
        }
    }

    @Nested
    inner class ResetSessions {

        @Test
        fun `resetSessions deletes refresh token and all sessions for CUSTOMER`() {
            userSessionService.resetSessions(JANE_ID, "CUSTOMER")

            verify(refreshTokenService).deleteTokenByCustomerId(JANE_ID)
            verify(userSessionRepository).deleteByUserIdAndUserType(JANE_ID, "CUSTOMER")
        }

        @Test
        fun `resetSessions deletes refresh token and all sessions for VENDOR`() {
            val vendorId = UUID.randomUUID()
            userSessionService.resetSessions(vendorId, "VENDOR")

            verify(refreshTokenService).deleteTokenByVendorId(vendorId)
            verify(userSessionRepository).deleteByUserIdAndUserType(vendorId, "VENDOR")
        }
    }

    @Nested
    inner class SessionExpiry {

        @Test
        fun `isExpired returns true when session expiry is in the past`() {
            val expired = janeSession().copy(expiresAtEpochMillis = System.currentTimeMillis() - 1000)
            assertTrue(expired.isExpired())
        }

        @Test
        fun `isExpired returns false when session expiry is in the future`() {
            val active = janeSession().copy(expiresAtEpochMillis = System.currentTimeMillis() + 900_000)
            assertFalse(active.isExpired())
        }

        @Test
        fun `isExpired uses provided currentEpochMillis for comparison`() {
            val session = janeSession().copy(expiresAtEpochMillis = 1_000L)
            val beforeTime = 500L
            val afterTime = 1_500L

            assertFalse(session.isExpired(beforeTime))
            assertTrue(session.isExpired(afterTime))
        }
    }
}

// RefreshTokenService

class RefreshTokenServiceTest {

    private lateinit var refreshTokenRepository: RefreshTokenRepository
    private lateinit var hashEncoder: HashEncoder
    private lateinit var refreshTokenService: RefreshTokenService

    @BeforeEach
    fun setUp() {
        refreshTokenRepository = mock()
        hashEncoder = mock()
        refreshTokenService = RefreshTokenService(refreshTokenRepository, hashEncoder)
    }

    @Test
    fun `storeRefreshToken calls repository save`() {
        val token = janeRefreshToken()
        refreshTokenService.storeRefreshToken(token)
        verify(refreshTokenRepository).save(token)
    }

    @Test
    fun `getTokenByCustomerId returns token when found`() {
        whenever(refreshTokenRepository.findByCustomerId(JANE_ID)).thenReturn(janeRefreshToken())
        val result = refreshTokenService.getTokenByCustomerId(JANE_ID)
        assertNotNull(result)
        assertEquals(JANE_ID, result?.customerId)
    }

    @Test
    fun `getTokenByCustomerId returns null when not found`() {
        whenever(refreshTokenRepository.findByCustomerId(JANE_ID)).thenReturn(null)
        assertNull(refreshTokenService.getTokenByCustomerId(JANE_ID))
    }

    @Test
    fun `deleteTokenByCustomerId calls repository delete`() {
        refreshTokenService.deleteTokenByCustomerId(JANE_ID)
        verify(refreshTokenRepository).deleteByCustomerId(JANE_ID)
    }

    @Test
    fun `revokeByRefreshToken deletes token matching the hashed raw token`() {
        val raw = FAKE_REFRESH_TOKEN
        val hashed = "hashed_refresh"
        whenever(hashEncoder.encode(raw)).thenReturn(hashed)
        whenever(refreshTokenRepository.findByHashedToken(hashed)).thenReturn(janeRefreshToken())

        refreshTokenService.revokeByRefreshToken(raw)

        verify(refreshTokenRepository).delete(any())
    }

    @Test
    fun `revokeByRefreshToken is a no-op when no matching token exists`() {
        whenever(hashEncoder.encode(any())).thenReturn("hashed_something")
        whenever(refreshTokenRepository.findByHashedToken(any())).thenReturn(null)

        refreshTokenService.revokeByRefreshToken("unknown-token")

        verify(refreshTokenRepository, never()).delete(any())
    }
}

